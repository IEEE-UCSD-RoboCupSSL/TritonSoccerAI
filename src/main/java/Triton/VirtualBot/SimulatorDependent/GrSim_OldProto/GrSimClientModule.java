package Triton.VirtualBot.SimulatorDependent.GrSim_OldProto;
import Triton.Config.Config;
import Triton.Legacy.OldGrSimProto.protosrcs.GrSimCommands;
import Triton.Legacy.OldGrSimProto.protosrcs.GrSimPacket;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.VirtualBot.SimClientModule;
import Triton.VirtualBot.VirtualBotCmds;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class GrSimClientModule implements SimClientModule {

    protected InetAddress address;
    protected DatagramSocket socket;
    protected int port, id;

    private final ArrayList<Subscriber<VirtualBotCmds>> virtualBotCmdSubs = new ArrayList<>();

    private boolean isFirstRun = true;

    private Config config;

    public GrSimClientModule(Config config) {
        this.config = config;

        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(config.connConfig.sslVisionConn.ipAddr);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < config.numAllyRobots; i++) {
            virtualBotCmdSubs.add(new FieldSubscriber<>("From:GrSimCmdTest", "Cmd " + id));
        }
    }

    @Override
    public void run() {
        if (isFirstRun) {
            setup();
            isFirstRun = false;
        }

        sendCmds();
    }

    private void setup() {
        for (Subscriber allyCmdSub : virtualBotCmdSubs) {
            try {
                allyCmdSub.subscribe(1000);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCmds() {

        System.out.println("1");
        GrSimCommands.grSim_Robot_Command command = GrSimCommands.grSim_Robot_Command.newBuilder()
                .setId(0)
                .setWheel2(0)
                .setWheel1(0)
                .setWheel3(0)
                .setWheel4(0)
                .setKickspeedx(0)
                .setKickspeedz(0)
                .setVeltangent(5)
                .setVelnormal(5)
                .setVelangular(0)
                .setSpinner(false)
                .setWheelsspeed(false)
                .build();

        System.out.println("2");
        GrSimCommands.grSim_Commands command2 = GrSimCommands.grSim_Commands.newBuilder()
//                .setTimestamp(0)
                .setIsteamyellow(false)
                .addRobotCommands(command).build();

        System.out.println("3");
        GrSimPacket.grSim_Packet packet = GrSimPacket.grSim_Packet.newBuilder()
                .setCommands(command2)
                .build();

//            GrSimPacket.grSim_Packet.Builder packet = GrSimPacket.grSim_Packet.newBuilder();
//            GrSimCommands.grSim_Commands.Builder grSimCommands = GrSimCommands.grSim_Commands.newBuilder();
//            grSimCommands.setIsteamyellow(config.myTeam == Team.YELLOW);
//            grSimCommands.setTimestamp(0);
//
////        for (int i = 0; i < config.numAllyRobots; i++) {
//            GrSimCommands.grSim_Robot_Command.Builder grSimRobotCommands =
//                    GrSimCommands.grSim_Robot_Command.newBuilder();
//
//            VirtualBotCmds cmd = virtualBotCmdSubs.get(0).getMsg();
//            grSimRobotCommands.setId(cmd.getId());
//            grSimRobotCommands.setVeltangent(cmd.getVelX());
//            grSimRobotCommands.setVelnormal(cmd.getVelAng());
//            grSimRobotCommands.setVelangular(cmd.getVelY());
//            grSimRobotCommands
//
//            System.out.println("1");
//            grSimCommands.addRobotCommands(grSimRobotCommands.build());
//            System.out.println("2");
////        }
//
//            packet.setCommands(grSimCommands.build());

        byte[] bytes;
        bytes = packet.toByteArray();
        send(bytes);
    }

    /**
     * Sends a packet
     *
     * @param msg message to send as byte array
     */
    private void send(byte[] msg) {
        try {
            DatagramPacket packet =
                    new DatagramPacket(msg, msg.length, InetAddress.getByName(config.connConfig.simCmdEndpoint.ipAddr),
                            config.connConfig.simCmdEndpoint.port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
