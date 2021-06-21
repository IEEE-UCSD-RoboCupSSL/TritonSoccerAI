package Triton.VirtualBot.SimulatorDependent.ErForce;


import Proto.SslSimulationRobotFeedback;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.CoreModules.Robot.Side;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.VirtualBot.SimClientModule;
import Triton.VirtualBot.VirtualBotCmds;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

import Proto.SslSimulationRobotControl.RobotCommand;
import Proto.SslSimulationRobotControl.RobotMoveCommand;
import Proto.SslSimulationRobotControl.MoveLocalVelocity;
import Proto.SslSimulationRobotControl.RobotControl;
import lombok.Data;

public class ErForceClientModule extends SimClientModule {

    private static final float MAX_DRIB_SPEED = 1000.0f;


    public ErForceClientModule(Config config) {
        super(config);
        App.runModule(new FeedBackReceptionModule(config, this), GvcModuleFreqs.VISION_MODULE_FREQ);
    }



    @Override
    protected void exec() {
        ArrayList<RobotCommand> robotCmdArr = new ArrayList<>();

        for (int i = 0; i < config.numAllyRobots; i++) {
            VirtualBotCmds cmd = virtualBotCmdSubs.get(i).getMsg();
            Vec2D kickXZ = new Vec2D(cmd.getKickX(), cmd.getKickZ());
            float kickSpeed = (float) kickXZ.mag();
            float kickAngle = (float) Math.toDegrees(Math.atan2(kickXZ.y, kickXZ.x));
            RobotCommand robotCmd = RobotCommand.newBuilder()
                    .setId(i)
                    .setMoveCommand(RobotMoveCommand.newBuilder()
                            .setLocalVelocity(MoveLocalVelocity.newBuilder()
                                    .setAngular(cmd.getVelAng())
                                    .setForward(cmd.getVelY())
                                    .setLeft(-cmd.getVelX())
                                    .build())
                            .build())
                    .setKickSpeed(kickSpeed)
                    .setKickAngle(kickAngle)
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

    private static class FeedBackReceptionModule implements Module {
        private final ErForceClientModule efcm;
        private final ArrayList<FieldPublisher<Boolean>> isBotContactBallPubs = new ArrayList<>();
        private final Config config;

        public FeedBackReceptionModule(Config config, ErForceClientModule efcm) {
            this.config = config;
            this.efcm = efcm;
            for(int id = 0; id < config.numAllyRobots; id++) {
                isBotContactBallPubs.add(new FieldPublisher<>("From:ErForceClientModule", "BallBotContactList " + id, false));
            }
        }


        @Override
        public void run() {
            try {
                SslSimulationRobotFeedback.RobotControlResponse feedbacks = efcm.receiveResponse();
                for(int id = 0; id < config.numAllyRobots; id++) {
                    isBotContactBallPubs.get(id).publish(feedbacks.getFeedback(id).getDribblerBallContact());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public SslSimulationRobotFeedback.RobotControlResponse receiveResponse() throws IOException {
        DatagramPacket packet = receiveUdpPacketFollowingSend();
        ByteArrayInputStream stream = new ByteArrayInputStream(packet.getData(),
                packet.getOffset(), packet.getLength());
        SslSimulationRobotFeedback.RobotControlResponse robotControlResponse =
                SslSimulationRobotFeedback.RobotControlResponse.parseFrom(stream);
        return robotControlResponse;
    }
}
