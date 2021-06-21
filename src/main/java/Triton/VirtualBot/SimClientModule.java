package Triton.VirtualBot;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Misc.ModulePubSubSystem.Subscriber;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static Triton.Util.delay;

public abstract class SimClientModule implements Module {

    protected InetAddress address;
    protected DatagramSocket socket;
    protected int port;

    protected byte[] receiveBuffer = new byte[1024];

    protected final ArrayList<Subscriber<VirtualBotCmds>> virtualBotCmdSubs = new ArrayList<>();

    protected boolean isFirstRun = true;

    protected Config config;

    public SimClientModule(Config config) {
        this.config = config;

        for (int i = 0; i < config.numAllyRobots; i++) {
            virtualBotCmdSubs.add(new FieldSubscriber<>("From:VirtualBot", "Cmd " + i));
        }
    }

    @Override
    public void run() {
        if (isFirstRun) {
            setup();
            isFirstRun = false;
        }

        exec();
    }

    private void setup() {
        for (Subscriber<VirtualBotCmds> allyCmdSub : virtualBotCmdSubs) {
            try {
                allyCmdSub.subscribe(1000);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }

        try {
//            if(config.cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
//                socket = new DatagramSocket(config.connConfig.simCmdEndpoint.port);
//            } else {
                socket = new DatagramSocket();
//            }
            address = InetAddress.getByName(config.connConfig.sslVisionConn.ipAddr);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        delay(1000);

    }

    protected abstract void exec();

    /**
     * Sends a packet
     *
     * @param msg message to send as byte array
     */
    protected void sendUdpPacket(byte[] msg) {
        try {
            DatagramPacket packet =
                    new DatagramPacket(msg, msg.length, InetAddress.getByName(config.connConfig.simCmdEndpoint.ipAddr),
                            config.connConfig.simCmdEndpoint.port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected DatagramPacket receiveUdpPacketFollowingSend() { // send already binds to the endpoint
        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet;
    }
}
