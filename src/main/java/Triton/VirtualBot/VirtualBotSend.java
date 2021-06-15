package Triton.VirtualBot;

import Proto.FirmwareAPI;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Util;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class VirtualBotSend implements Module {
    private String ip = null;
    private int id;
    private int port;
    private DatagramSocket socketOut;
    private InetAddress inetAddress;

    private FieldSubscriber<FirmwareAPI.FirmwareData> dataSub;

    public VirtualBotSend(Config config, int id, FieldSubscriber<FirmwareAPI.FirmwareData> dataSub) {
        this.id = id;
        this.ip = config.connConfig.botConns.get(id).ipAddr;
        this.port = config.connConfig.botConns.get(id).virtualBotUdpSendPort;

        this.dataSub = dataSub;
    }

    private void setup() {
        try {
            socketOut = new DatagramSocket();
            inetAddress = InetAddress.getByName(ip);

        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        setup();
        if (socketOut == null) return;
        byte[] msg = dataSub.getMsg().toByteArray();
        try {
            socketOut.send(new DatagramPacket(msg, msg.length, inetAddress, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
