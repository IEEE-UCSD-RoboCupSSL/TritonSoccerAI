package Triton.VirtualBot;

import Proto.FirmwareAPI;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Util;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class VirtualBotReceive implements Module {
    private String ip = null;
    private int id;
    private int port;
    private DatagramSocket socketIn;
    private InetAddress inetAddress;
    private FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub;

    public VirtualBotReceive(Config config, int id, FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub) {
        this.ip = config.connConfig.botConns.get(id).ipAddr;
        this.port = config.connConfig.botConns.get(id).virtualBotUdpReceivePort;
        this.id = id;
        this.cmdPub = cmdPub;
    }

    private void setup() {
        try {
            inetAddress = InetAddress.getByName(ip);
            socketIn = new DatagramSocket(port, inetAddress);


        } catch (SocketException | UnknownHostException e) {
            System.err.println(">>>" + port);
        }
    }

    @Override
    public void run() {
        setup();
        if (socketIn == null) return;

        try {
            byte[] buf = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
            socketIn.receive(datagramPacket);

            FirmwareAPI.FirmwareCommand receivedCmd =
                    FirmwareAPI.FirmwareCommand.parseFrom(buf);

            cmdPub.publish(receivedCmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
