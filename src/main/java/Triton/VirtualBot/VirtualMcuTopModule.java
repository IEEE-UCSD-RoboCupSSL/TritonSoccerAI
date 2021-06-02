package Triton.VirtualBot;

import Proto.FirmwareAPI;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.ConnectionConfig;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Util;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class VirtualMcuTopModule implements Module {
    private boolean isFirstRun = true;
    private ServerSocket severSocket;
    private Socket socket = null;
    private String ip = null;
    private int id;
    private int port;
    private PrintWriter socketOut;
    private BufferedReader socketIn;
    private FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub;
    private FieldSubscriber<FirmwareAPI.FirmwareData> dataSub;
    private FieldPubSubPair<Boolean> isConnectedToTritonBotPubSub;

    private FieldPublisher<String> debugStrPub;
    public VirtualMcuTopModule(Config config, int id,
                               FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub,
                               FieldSubscriber<FirmwareAPI.FirmwareData> dataSub) {
        isConnectedToTritonBotPubSub =
                new FieldPubSubPair<>("Internal:VirtualMcuTopModule",
                                      "isConnectedToTritonBot " + id, false);
        debugStrPub = new FieldPublisher<>("VirtualMcuTopModule", "DebugString " + id, "???");

        for(ConnectionConfig.BotConn conn : config.connConfig.botConns) {
            if(conn.id == id) {
                ip = conn.ipAddr;
                port = conn.virtualBotTcpPort;
            }
        }
        if(ip == null) {
            throw new RuntimeException("can't find a IP config with matching bot id");
        }
        this.id = id;
        this.cmdPub = cmdPub;
        this.dataSub = dataSub;
    }


    public boolean isConnectedToTritonBot() {
        Boolean result = isConnectedToTritonBotPubSub.sub.getMsg();
        if(result == null) return false;
        return result;
    }

    private void setup() {
        try {
            severSocket = new ServerSocket(port);
            socket = severSocket.accept();
            socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            System.out.println("VirtualBot " + id + " successfully accepted TritonBot(cpp)'s tcp connection request");
            isConnectedToTritonBotPubSub.pub.publish(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* background thread */
        App.threadPool.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        if(socket == null || socketOut == null) return;
                        /* Since TritonBot program doesn't use this data at all when in virtual mode,
                         * so a default/dummy data packet is repeatedly sent to keep
                         * TritonBot's tcp loop running, because dataSub's pairing publisher is never used
                         * except for initializing the internal message channel with this dataSub */
                        socketOut.println(dataSub.getMsg().toByteString());
                    }
                }
                , 0, Util.toPeriod(GvcModuleFreqs.VIRTUAL_BOT_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

    }

    @Override
    public void run() {

        if(isFirstRun) {
            setup();
            isFirstRun = false;
        }
        if(socket == null || socketIn == null) {
            isFirstRun = true;
            System.out.println("Something went wrong in VirtualMcuTopModule.java");
            return;
        }

        /* Get commands meant to be sent to the firmware layer (adapter program named McuTop)
         *  from TritonBot, this class is meant to mock that McuTop as a virtual mode version of McuTop */
        try {
            String line;
            line = socketIn.readLine();
            //debugStrPub.publish(line);
            //line = line.substring(0, line.length() - 1);
            ByteArrayInputStream stream = new ByteArrayInputStream(line.getBytes(StandardCharsets.US_ASCII));
            FirmwareAPI.FirmwareCommand receivedCmd =
                    FirmwareAPI.FirmwareCommand.parseFrom(stream);

            cmdPub.publish(receivedCmd);

            // Must check init command here

        } catch (IOException e) {
            // To-do: handle disconnect
            e.printStackTrace();
        }
    }



}
