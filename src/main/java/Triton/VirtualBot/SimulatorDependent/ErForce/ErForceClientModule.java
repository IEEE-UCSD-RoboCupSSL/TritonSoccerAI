package Triton.VirtualBot.SimulatorDependent.ErForce;


import Triton.Config.Config;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.VirtualBot.SimClientModule;
import Triton.VirtualBot.VirtualBotCmds;

import java.util.ArrayList;

import Proto.SslSimulationRobotControl.RobotCommand;
import Proto.SslSimulationRobotControl.RobotMoveCommand;
import Proto.SslSimulationRobotControl.MoveLocalVelocity;
import Proto.SslSimulationRobotControl.RobotControl;

public class ErForceClientModule extends SimClientModule {
    private static final float MAX_DRIB_SPEED = 1000.0f;
    private static float dribSpeed = 1000.0f;
    private static final FieldPubSubPair<Boolean> allDribOffPubSub =
            new FieldPubSubPair<>("[Pair]DefinedIn:ErForceClientModule", "AllDribOff", false);


    public ErForceClientModule(Config config) {
        super(config);
        //App.runModule(new FeedBackReceptionModule(config), GvcModuleFreqs.VISION_MODULE_FREQ);
    }

    public static void turnAllDribOff() {
        allDribOffPubSub.pub.publish(true);
    }

    public static void resetTurnAllDribOff() {
        allDribOffPubSub.pub.publish(false);
    }


    @Override
    protected void exec() {
        ArrayList<RobotCommand> robotCmdArr = new ArrayList<>();

        if(allDribOffPubSub.getSub().getMsg()) {
            dribSpeed = 0.0f;
        } else {
            dribSpeed = MAX_DRIB_SPEED;
        }

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
                    .setDribblerSpeed(cmd.getSpinner() ? dribSpeed : 0.0f)
                    .build();

            robotCmdArr.add(robotCmd);
        }

        RobotControl robotCtrl = RobotControl.newBuilder()
                .addAllRobotCommands(robotCmdArr).build();


        byte[] bytes;
        bytes = robotCtrl.toByteArray();



        sendUdpPacket(bytes);




    }

//    private static class FeedBackReceptionModule implements Module {
//        private final ArrayList<FieldPublisher<Boolean>> isBotContactBallPubs = new ArrayList<>();
//        private final Config config;
//        protected byte[] receiveBuffer = new byte[600000];
//        protected InetAddress address;
//        protected DatagramSocket socket;
//        protected int port;
//
//        public FeedBackReceptionModule(Config config) {
//            this.config = config;
//            for(int id = 0; id < config.numAllyRobots; id++) {
//                isBotContactBallPubs.add(new FieldPublisher<>("From:ErForceClientModule", "BallBotContactList " + id, false));
//            }
//            try {
//                socket = new DatagramSocket();
//                address = InetAddress.getByName(config.connConfig.sslVisionConn.ipAddr);
//            } catch (SocketException | UnknownHostException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        @Override
//        public void run() {
//            try {
//                SslSimulationRobotFeedback.RobotControlResponse feedbacks = efcm.receiveResponse();
//                for(int id = 0; id < config.numAllyRobots; id++) {
//                    isBotContactBallPubs.get(id).publish(feedbacks.getFeedback(id).getDribblerBallContact());
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        protected DatagramPacket receiveUdpPacket() { // send already binds to the endpoint
//            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//            try {
//                socket.receive(packet);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return packet;
//        }
//
//        public SslSimulationRobotFeedback.RobotControlResponse receiveResponse() throws IOException {
//            DatagramPacket packet = receiveUdpPacket();
//            ByteArrayInputStream stream = new ByteArrayInputStream(packet.getData(),
//                    packet.getOffset(), packet.getLength());
//            SslSimulationSynchronous.SimulationSyncResponse ssr = SslSimulationSynchronous.SimulationSyncResponse.parseFrom(stream);
//            SslSimulationRobotFeedback.RobotControlResponse robotControlResponse = ssr.getRobotControlResponse();
//            return robotControlResponse;
//        }
//
//    }



}
