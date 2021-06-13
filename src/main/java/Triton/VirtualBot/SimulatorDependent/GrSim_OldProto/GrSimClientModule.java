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

//        delay(1000);
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
        GrSimPacket.grSim_Packet.Builder packet = GrSimPacket.grSim_Packet.newBuilder();
        GrSimCommands.grSim_Commands.Builder grSimCommands = GrSimCommands.grSim_Commands.newBuilder();

        for (int i = 0; i < config.numAllyRobots; i++) {
            GrSimCommands.grSim_Robot_Command.Builder grSimRobotCommands =
                    GrSimCommands.grSim_Robot_Command.newBuilder();

            VirtualBotCmds cmd = virtualBotCmdSubs.get(i).getMsg();
            grSimRobotCommands.setId(cmd.getId());
            grSimRobotCommands.setVeltangent(cmd.getVelX());
            grSimRobotCommands.setVelnormal(cmd.getVelAng());
            grSimRobotCommands.setVelangular(cmd.getVelY());

            grSimCommands.setRobotCommands(i, grSimRobotCommands);
        }

        packet.setCommands(grSimCommands);

        byte[] bytes;
        bytes = packet.build().toByteArray();
        send(bytes);
    }

    /**
     * Sends a packet
     *
     * @param msg message to send as byte array
     */
    private void send(byte[] msg) {
        DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
