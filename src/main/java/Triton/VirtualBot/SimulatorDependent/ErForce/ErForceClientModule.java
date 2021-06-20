package Triton.VirtualBot.SimulatorDependent.ErForce;


import Triton.Config.Config;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.VirtualBot.SimClientModule;
import Triton.VirtualBot.VirtualBotCmds;
import java.util.ArrayList;

import Proto.SslSimulationRobotControl.RobotCommand;
import Proto.SslSimulationRobotControl.RobotMoveCommand;
import Proto.SslSimulationRobotControl.MoveLocalVelocity;
import Proto.SslSimulationRobotControl.RobotControl;

public class ErForceClientModule extends SimClientModule {

    private static final float MAX_DRIB_SPEED = 500.0f;

    public ErForceClientModule(Config config) {
        super(config);
    }

    @Override
    protected void sendCommandsToSim() {
        ArrayList<RobotCommand> robotCmdArr = new ArrayList<>();

        for (int i = 0; i < config.numAllyRobots; i++) {
            VirtualBotCmds cmd = virtualBotCmdSubs.get(i).getMsg();

            Vec2D audienceVel = PerspectiveConverter.playerToAudience(new Vec2D(cmd.getVelX(), cmd.getVelY()));
            if (config.myTeam == Team.BLUE) {
                audienceVel.x = -audienceVel.x;
                audienceVel.y = -audienceVel.y;
            }

            RobotCommand robotCmd = RobotCommand.newBuilder()
                    .setId(i)
                    .setMoveCommand(RobotMoveCommand.newBuilder()
                            .setLocalVelocity(MoveLocalVelocity.newBuilder()
                                    .setAngular(cmd.getVelAng())
                                    .setForward(cmd.getVelY())
                                    .setLeft(-cmd.getVelX())
                                    .build())
                            .build())
                    .setKickSpeed(cmd.getKickX())
                    .setKickAngle(cmd.getKickZ())
                    .setDribblerSpeed(cmd.getSpinner() ? MAX_DRIB_SPEED : 0.0f)
                    .build();

            robotCmdArr.add(robotCmd);
        }

        RobotControl robotCtrl = RobotControl.newBuilder()
                .addAllRobotCommands(robotCmdArr).build();

        byte[] bytes;
        bytes = robotCtrl.toByteArray();
        sendUdpPacket(bytes);
    }
}
