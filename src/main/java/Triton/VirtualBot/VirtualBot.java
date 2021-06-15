package Triton.VirtualBot;

import Proto.FirmwareAPI;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Misc.ModulePubSubSystem.Publisher;
import Triton.Util;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class VirtualBot implements Module {
    private boolean isFirstRun = true;
    private VirtualMcuTopModule mcuTopModule;
    private Config config;

    /* prepare the main pubsubs */
    private FieldPubSubPair<FirmwareAPI.FirmwareCommand> firmCmdPubSubPair;

    private FieldPubSubPair<FirmwareAPI.FirmwareData> firmDataPubSubPair;

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
        mcuTopModule = new VirtualMcuTopModule(config, id, firmCmdPubSubPair.pub, firmDataPubSubPair.sub);

        virtualBotCmdPub = new FieldPublisher<VirtualBotCmds>("From:VirtualBot", "Cmd " + id,
                new VirtualBotCmds());
    }

    public boolean isConnectedToTritonBot() {
        return mcuTopModule.isConnectedToTritonBot();
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

        double xmax = config.botConfig.robotMaxHorizontalSpeed;
        double ymax = config.botConfig.robotMaxVerticalSpeed;
        double wmax = config.botConfig.robotMaxAngularSpeed;

        VirtualBotCmds cmd = new VirtualBotCmds();
        // firmCmd's <vx, vy, w> are unit-less, which is represented as percentage of the maximum
        cmd.setVelX((float)((firmCmd.getVx() / 100.00f) * xmax));
        cmd.setVelY((float)((firmCmd.getVy() / 100.00f) * ymax));
        cmd.setVelAng((float)((firmCmd.getW() / 100.00f)* wmax));


        virtualBotCmdPub.publish(cmd);

    }
}
