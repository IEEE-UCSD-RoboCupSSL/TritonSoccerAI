package Triton.VirtualBot.SimulatorDependent.GrSim_OldProto;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Team;
import Triton.Legacy.OldGrSimProto.protosrcs.GrSimCommands;
import Triton.Legacy.OldGrSimProto.protosrcs.GrSimPacket;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.VirtualBot.SimClientModule;
import Triton.VirtualBot.VirtualBotCmds;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.linsol.svd.SolvePseudoInverseSvd_DDRM;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

public class GrSimClientModule extends SimClientModule {
    private static SimpleMatrix bodyToWheelTransform;
    public GrSimClientModule(Config config) {
        super(config);
        double theta = Math.toRadians(45);
        double phi = Math.toRadians(45);
        SimpleMatrix wheelToBodyTransform = new SimpleMatrix(new double[][]{
                new double[]{Math.cos(theta)/2.0, -Math.cos(phi)/2, -Math.cos(phi)/2, Math.cos(theta)/2},
                new double[]{Math.sin(theta)/2.0, Math.sin(phi)/2, -Math.sin(phi)/2, -Math.sin(theta)/2},
                new double[]{-1.0 / (4 * config.botConfig.robotRadius), -1.0 / (4 * config.botConfig.robotRadius), -1.0 / (4 * config.botConfig.robotRadius), -1.0 / (4 * config.botConfig.robotRadius)}
        });
        DMatrixRMaj wtb = wheelToBodyTransform.copy().getMatrix();
        DMatrixRMaj btw = wheelToBodyTransform.copy().getMatrix();;
        SolvePseudoInverseSvd_DDRM moorePenrosePseudoInverseSolver = new SolvePseudoInverseSvd_DDRM(3, 4);
        moorePenrosePseudoInverseSolver.setA(wtb);
        moorePenrosePseudoInverseSolver.invert(btw);
        bodyToWheelTransform = SimpleMatrix.wrap(btw);
    }

    @Override
    protected void sendCommandsToSim() {
        ArrayList<GrSimCommands.grSim_Robot_Command> robotCommandsArr = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            VirtualBotCmds cmd = virtualBotCmdSubs.get(i).getMsg();

            Vec2D audienceVel = PerspectiveConverter.playerToAudience(new Vec2D(cmd.getVelX(), cmd.getVelY()));
            if (config.myTeam == Team.YELLOW) {
                audienceVel.x = -audienceVel.x;
                audienceVel.y = -audienceVel.y;
            }


//            GrSimCommands.grSim_Robot_Command robotCommands =
//                    debugAsWheelCommands(i, cmd.getVelX(), cmd.getVelY(), cmd.getVelAng(), config);

            GrSimCommands.grSim_Robot_Command robotCommands = GrSimCommands.grSim_Robot_Command.newBuilder()
                    .setId(i)
                    .setWheel2(0)
                    .setWheel1(0)
                    .setWheel3(0)
                    .setWheel4(0)
                    .setKickspeedx(cmd.getKickX())
                    .setKickspeedz(cmd.getKickZ())
                    .setVeltangent((float) audienceVel.x)
                    .setVelnormal((float) audienceVel.y)
                    .setVelangular(cmd.getVelAng())
                    .setSpinner(cmd.getSpinner())
                    .setWheelsspeed(false)
                    .build();

            robotCommandsArr.add(robotCommands);
        }

        GrSimCommands.grSim_Commands command2 = GrSimCommands.grSim_Commands.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setIsteamyellow(config.myTeam == Team.YELLOW)
                .addAllRobotCommands(robotCommandsArr).build();

        GrSimPacket.grSim_Packet packet = GrSimPacket.grSim_Packet.newBuilder()
                .setCommands(command2)
                .build();

        byte[] bytes;
        bytes = packet.toByteArray();
        sendUdpPacket(bytes);
    }


    private static GrSimCommands.grSim_Robot_Command debugAsWheelCommands(int i, double x, double y, double w, Config config) {


        SimpleMatrix bodyVec = new SimpleMatrix(new double[][]{new double[]{x, y, w}}).transpose();
        SimpleMatrix wheelVec = bodyToWheelTransform.mult(bodyVec);

        GrSimCommands.grSim_Robot_Command robotCommands = GrSimCommands.grSim_Robot_Command.newBuilder()
                .setId(i)
                .setWheel1(-(float)(wheelVec.get(0, 0) / config.botConfig.wheelRadius))
                .setWheel2(-(float)(wheelVec.get(1, 0) / config.botConfig.wheelRadius))
                .setWheel3(-(float)(wheelVec.get(2, 0) / config.botConfig.wheelRadius))
                .setWheel4(-(float)(wheelVec.get(3, 0) / config.botConfig.wheelRadius))
                .setKickspeedx(0)
                .setKickspeedz(0)
                .setVeltangent(0)
                .setVelnormal(0)
                .setVelangular(0)
                .setSpinner(false)
                .setWheelsspeed(true)
                .build();
        return robotCommands;
    }
}
