package Triton.VirtualBot.SimulatorDependent.GrSim_OldProto;

import Triton.Config.Config;
import Triton.CoreModules.Robot.Team;
import Triton.Legacy.OldGrSimProto.protosrcs.GrSimCommands;
import Triton.Legacy.OldGrSimProto.protosrcs.GrSimPacket;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.VirtualBot.SimClientModule;
import Triton.VirtualBot.VirtualBotCmds;

import java.util.ArrayList;

public class GrSimClientModule extends SimClientModule {

    public GrSimClientModule(Config config) {
        super(config);
    }

    @Override
    protected void sendCmds() {
        ArrayList<GrSimCommands.grSim_Robot_Command> robotCommandsArr = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            VirtualBotCmds cmd = virtualBotCmdSubs.get(i).getMsg();

            Vec2D audienceVel = PerspectiveConverter.playerToAudience(new Vec2D(cmd.getVelX(), cmd.getVelY()));
            if (config.myTeam == Team.YELLOW) {
                audienceVel.x = -audienceVel.x;
                audienceVel.y = -audienceVel.y;
            }

            GrSimCommands.grSim_Robot_Command robotCommands = GrSimCommands.grSim_Robot_Command.newBuilder()
                    .setId(i)
                    .setWheel2(0)
                    .setWheel1(0)
                    .setWheel3(0)
                    .setWheel4(0)
                    .setKickspeedx(0)
                    .setKickspeedz(0)
                    .setVeltangent((float) audienceVel.x)
                    .setVelnormal((float) audienceVel.y)
                    .setVelangular(cmd.getVelAng())
                    .setSpinner(false)
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
        send(bytes);
    }
}
