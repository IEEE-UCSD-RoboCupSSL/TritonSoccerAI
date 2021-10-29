package triton.virtualBot;

import proto.FirmwareAPI;
import triton.App;
import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.misc.math.linearAlgebra.LinearAlgebra;
import triton.misc.modulePubSubSystem.FieldPubSubPair;
import triton.misc.modulePubSubSystem.FieldPublisher;
import triton.misc.modulePubSubSystem.Module;
import triton.misc.modulePubSubSystem.Publisher;
import triton.Util;
import org.ejml.simple.SimpleMatrix;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class VirtualBot implements Module {
    private boolean isFirstRun = true;
    private VirtualMcuTopModule mcuTopModule;
    private Config config;

    /* prepare the main pubsubs */
    private FieldPubSubPair<FirmwareAPI.FirmwareCommand> firmCmdPubSubPair;
    private FieldPubSubPair<FirmwareAPI.FirmwareData> firmDataPubSubPair;
    private FieldPubSubPair<Boolean> vbotPauseCmdPair;

    private final Publisher<VirtualBotCmds> virtualBotCmdPub;

    public VirtualBot(Config config, int id) {
        this.config = config;
        firmCmdPubSubPair = new FieldPubSubPair<>("[Pair]DefinedIn:VirtualBot", "FirmCmd " + id,
                FirmwareAPI.FirmwareCommand.newBuilder()
                        .setInit(false).setVx(0.0f).setVy(0.0f).setW(0.0f)
                        .setKx(0.0f).setKz(0.0f).setDribbler(false).build()
        );
        firmDataPubSubPair = new FieldPubSubPair<>("[Pair]DefinedIn:VirtualBot", "FirmData " + id,
                FirmwareAPI.FirmwareData.newBuilder()
                        .setEncX(0.0001f).setEncY(0.0001f).setImuTheta(0.0001f).setImuOmega(0.0001f)
                        .setImuAx(0.0001f).setImuAy(0.0001f).setIsHoldingball(false).build()
        );
        vbotPauseCmdPair = new FieldPubSubPair<>("[Pair]DefinedIn:VirtualBot", "PauseBot " + id, false);
        mcuTopModule = new VirtualMcuTopModule(config, id, firmCmdPubSubPair.pub, firmDataPubSubPair.sub);

        virtualBotCmdPub = new FieldPublisher<VirtualBotCmds>("From:VirtualBot", "Cmd " + id,
                new VirtualBotCmds());
    }

    public boolean isConnectedToTritonBot() {
        return mcuTopModule.isConnectedToTritonBot();
    }

    public void pauseBot() {
        vbotPauseCmdPair.pub.publish(true);
    }
    public void resumeBot() {
        vbotPauseCmdPair.pub.publish(false);
    }

    private void setup() {
        ScheduledFuture<?> sendTCPFuture = App.threadPool.scheduleAtFixedRate(
                mcuTopModule
            ,0, Util.toPeriod(GvcModuleFreqs.VIRTUAL_MCU_TOP_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

    @Override
    public void run() {
        if(isFirstRun) {
            setup();
            isFirstRun = false;
        }

        FirmwareAPI.FirmwareCommand firmCmd = firmCmdPubSubPair.sub.getMsg();

        VirtualBotCmds cmd = new VirtualBotCmds();
        // firmCmd's <vx, vy, w> are unit-less, which is represented as percentage of the maximum
        SimpleMatrix vec = new SimpleMatrix(new double[][]{new double[]{firmCmd.getVx(), firmCmd.getVy(), firmCmd.getW()}}).transpose();

        vec = VirtualBot.constraintMap(VirtualBot.normalizeBodyVector(vec),
                        config.botConfig.robotMaxVerticalSpeed,
                        config.botConfig.robotMaxHorizontalSpeed,
                        config.botConfig.robotMaxAngularSpeed);
        cmd.setVelX((float)vec.get(0, 0));
        cmd.setVelY((float)vec.get(1, 0));
        cmd.setVelAng((float)vec.get(2, 0));
        cmd.setKickX(firmCmd.getKx());
        cmd.setKickZ(firmCmd.getKz());
        cmd.setSpinner(firmCmd.getDribbler());

        if(!vbotPauseCmdPair.sub.getMsg()) {
            virtualBotCmdPub.publish(cmd);
        }
    }

    public static SimpleMatrix normalizeBodyVector(SimpleMatrix bvec) {
        if(bvec.get(2, 0) > 100.00) bvec.set(2, 0, 100.00);
        if(bvec.get(2, 0) < -100.00) bvec.set(2, 0, -100.00);

        double maxMagForXy = Math.sqrt(Math.pow(100.00, 2.00) - Math.pow(bvec.get(2, 0), 2.00));
        SimpleMatrix xy = new SimpleMatrix(new double[][]{
                new double[]{bvec.get(0, 0), bvec.get(1, 0)}
        }).transpose();
        if(LinearAlgebra.norm(xy) > maxMagForXy) {
            xy = LinearAlgebra.normalize(xy).scale(maxMagForXy);
        }
        return new SimpleMatrix(new double[][]{
            new double[]{xy.get(0, 0), xy.get(1, 0), bvec.get(2, 0)}
        }).transpose();
    }

    public static double computeXyConstraintAt(SimpleMatrix vec, double vmax, double hmax) {
        if(Math.abs(vec.get(0, 0)) < LinearAlgebra.zeroEp) {
            if(Math.abs(vec.get(1, 0)) < LinearAlgebra.zeroEp) {
                return 0.00;
            } else {
                return vmax;
            }
        } else {
            if(Math.abs(vec.get(1, 0)) < LinearAlgebra.zeroEp) {
                return hmax;
            }
        }

        /*
        #line1: (top-right vertex): y= a_p1p2 * x + p1[1]
	    #line2: y = a_v * x
	    #intersection: a_v * x = a_p1p2 * x +p1[1]
    	#				x = p1[1] / (a_v - a_p1p2) */
        double av = Math.abs(vec.get(1, 0)) / Math.abs(vec.get(0, 0));
        double ap1p2 = vmax / -hmax;
        double x = vmax / (av - ap1p2);
        double y = ap1p2 * x + vmax;
        return LinearAlgebra.norm(new SimpleMatrix(new double[][]{new double[]{x, y}}).transpose());
    }

    /* #percentage representation's MaxSpeed constraint is a circle on xy plaine for x, y
#but the real constraint for our robot is a 2d diamond determined by the max-vertical speed and max-horizontal speed
#so a mapping is needed */
    public static SimpleMatrix constraintMap(SimpleMatrix bodyVec, double vmax, double hmax, double rmax) {
        SimpleMatrix tvec = new SimpleMatrix(new double[][]{new double[]{bodyVec.get(0, 0), bodyVec.get(1, 0)}}).transpose();
        double rvec = bodyVec.get(2, 0);
        double maxMag = computeXyConstraintAt(tvec, vmax, hmax);
        double mag = (LinearAlgebra.norm(tvec) / 100.00) * maxMag;
        tvec = LinearAlgebra.normalize(tvec).scale(mag);
        rvec = (rvec / 100.00) * rmax;
        return new SimpleMatrix(new double[][]{new double[]{tvec.get(0, 0), tvec.get(1, 0), rvec}}).transpose();
    }

}
