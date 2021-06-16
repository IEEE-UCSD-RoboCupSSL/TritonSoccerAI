package Triton.VirtualBot;

import Proto.FirmwareAPI;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.ConnectionConfig;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;

import java.io.*;
import java.net.*;

import static Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs.VIRTUAL_MCU_TOP_FREQ;
import static Triton.Util.delay;

public class VirtualMcuTopModule implements Module {

    protected final static int MAX_BUFFER_SIZE = 67108864;
    private boolean isFirstRun = true;
    private Socket socket = null;
    private final int id;
    private int port;
    //private PrintWriter socketOut;
    //private BufferedReader socketIn;
    private final FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub;
    private final FieldSubscriber<FirmwareAPI.FirmwareData> dataSub;
    private final FieldPubSubPair<Boolean> isConnectedToTritonBotPubSub;
    private final Config config;

    // private FieldPublisher<String> debugStrPub;
    public VirtualMcuTopModule(Config config, int id,
                               FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub,
                               FieldSubscriber<FirmwareAPI.FirmwareData> dataSub) {
        this.config = config;
        isConnectedToTritonBotPubSub =
                new FieldPubSubPair<>("Internal:VirtualMcuTopModule",
                        "isConnectedToTritonBot " + id, false);
        // debugStrPub = new FieldPublisher<>("From:VirtualMcuTopModule", "DebugString " + id, "???");

        String ip = null;
        for (ConnectionConfig.BotConn conn : config.connConfig.botConns) {
            if (conn.id == id) {
                ip = conn.ipAddr;
                port = conn.virtualBotTcpPort;
            }
        }
        if (ip == null) {
            throw new RuntimeException("can't find a IP config with matching bot id");
        }
        this.id = id;
        this.cmdPub = cmdPub;
        this.dataSub = dataSub;
    }


    public boolean isConnectedToTritonBot() {
        Boolean result = isConnectedToTritonBotPubSub.sub.getMsg();
        if (result == null) return false;
        return result;
    }

    private void setup() {
        try {
            ServerSocket severSocket = new ServerSocket(port);
            socket = severSocket.accept();
            //socketOut = new PrintWriter(socket.getOutputStream(), true);
            //socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            System.out.println("\033[0;32m VirtualBot " + id + " successfully accepted " +
                    "TritonBot(cpp)'s tcp connection request \033[0m");
            isConnectedToTritonBotPubSub.pub.publish(true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        App.runModule(new VirtualBotUdpSend(config, id, dataSub), VIRTUAL_MCU_TOP_FREQ);
        App.runModule(new VirtualBotUdpReceive(config, id, cmdPub), VIRTUAL_MCU_TOP_FREQ);


    }

    @Override
    public void run() {

        if (isFirstRun) {
            setup();
            isFirstRun = false;
        }
        if (socket == null) {
            isFirstRun = true;
            System.out.println("Something went wrong in VirtualMcuTopModule.java");
            return;
        }

        delay(5000);


    }


    private static class VirtualBotUdpSend implements Module {
        private final int port;
        private DatagramSocket socketOut;
        private InetAddress inetAddress;

        private final FieldSubscriber<FirmwareAPI.FirmwareData> dataSub;

        public VirtualBotUdpSend(Config config, int id, FieldSubscriber<FirmwareAPI.FirmwareData> dataSub) {
            String ip = config.connConfig.botConns.get(id).ipAddr;
            this.port = config.connConfig.botConns.get(id).virtualBotUdpSendPort;
            this.dataSub = dataSub;
            try {
                socketOut = new DatagramSocket();
                inetAddress = InetAddress.getByName(ip);
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (socketOut == null) return;
            byte[] msg = dataSub.getMsg().toByteArray();
            try {
                socketOut.send(new DatagramPacket(msg, msg.length, inetAddress, port));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class VirtualBotUdpReceive implements Module {
        private final DatagramPacket datagramPacket;
        private String ip = null;
        private final int port;
        private DatagramSocket socket;
        private final FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub;

        public VirtualBotUdpReceive(Config config, int id, FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub) {
            this.ip = config.connConfig.botConns.get(id).ipAddr;
            this.port = config.connConfig.botConns.get(id).virtualBotUdpReceivePort;
            this.cmdPub = cmdPub;
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                socket = new DatagramSocket(port, inetAddress);
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
            byte[] buf = new byte[MAX_BUFFER_SIZE];
            datagramPacket = new DatagramPacket(buf, buf.length);
        }

        @Override
        public void run() {
            if (socket == null) return;
            try {
                socket.receive(datagramPacket);

                ByteArrayInputStream stream = new ByteArrayInputStream(datagramPacket.getData(),
                        datagramPacket.getOffset(), datagramPacket.getLength());


                FirmwareAPI.FirmwareCommand receivedCmd =
                        FirmwareAPI.FirmwareCommand.parseFrom(stream);
                cmdPub.publish(receivedCmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}
