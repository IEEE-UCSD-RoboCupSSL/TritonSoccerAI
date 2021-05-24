package Triton.VirtualBot.GrSim;

import Proto.GrSimCommands;
import Proto.GrSimPacket;
import Triton.Config.ObjectConfig;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Misc.ModulePubSubSystem.Subscriber;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class GrSimSendModule implements Module {
    protected InetAddress address;
    protected DatagramSocket socket;

    protected int port;
    protected int ID;

    private final ArrayList<Subscriber<GrSimCommands.grSim_Robot_Command>> grSimBotCmdSubs;

    private boolean isFirstRun = true;

    public GrSimSendModule(String ip, int port) {
        this.port = port;
        this.ID = ID;

        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(ip);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        grSimBotCmdSubs = new ArrayList<Subscriber<GrSimCommands.grSim_Robot_Command>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            grSimBotCmdSubs.add(new FieldSubscriber<GrSimCommands.grSim_Robot_Command>(
                    "grSim", "botCmd" + i));
        }
    }

    @Override
    public void run() {
        if (isFirstRun) {
            subscribe();
            isFirstRun = false;
        }

        sendData();
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            for (Subscriber<GrSimCommands.grSim_Robot_Command> grSimBotCmdSub : grSimBotCmdSubs) {
                grSimBotCmdSub.subscribe(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendData() {
        GrSimPacket.grSim_Packet.Builder grSimPacket = GrSimPacket.grSim_Packet.newBuilder();
        GrSimCommands.grSim_Commands.Builder grSimCommands = GrSimCommands.grSim_Commands.newBuilder();

        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            grSimCommands.setRobotCommands(i, grSimBotCmdSubs.get(i).getMsg());
        }
        grSimPacket.setCommands(grSimCommands);

        byte[] bytes;
        bytes = grSimPacket.build().toByteArray();
        send(bytes);
    }

    protected void send(byte[] msg) {
        DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
