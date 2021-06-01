package Triton.VirtualBot;

import Proto.FirmwareAPI;
import Triton.Config.Config;
import Triton.Config.ConnectionConfig;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
    private FieldPubSubPair<Boolean> isConnectedToTritonBotPubSub =
            new FieldPubSubPair<>("Internal:VirtualMcuTopModule", "isConnectedToTritonBot", false);

    public VirtualMcuTopModule(Config config, int id,
                               FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub,
                               FieldSubscriber<FirmwareAPI.FirmwareData> dataSub) {
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
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("VirtualBot " + id + " successfully accepted TritonBot(cpp)'s tcp connection request");
            isConnectedToTritonBotPubSub.pub.publish(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if(isFirstRun) {
            setup();
            isFirstRun = false;
        }
        if(socket == null || socketIn == null || socketOut == null) {
            isFirstRun = true;
            return;
        }

        /* Since TritonBot program doesn't use this data at all when in virtual mode,
         * so a default/dummy data packet is repeatedly sent to keep
         * TritonBot's tcp loop running, because dataSub's pairing publisher is never used
         * except for initializing the internal message channel with this dataSub */
        socketOut.println(dataSub.getMsg());


        /* Get commands meant to be sent to the firmware layer (adapter program named McuTop)
        *  from TritonBot, this class is meant to mock that McuTop as a virtual mode version of McuTop */
        try {
            String line = socketIn.readLine();
            FirmwareAPI.FirmwareCommand receivedCmd =
                    FirmwareAPI.FirmwareCommand.parseFrom(line.getBytes(StandardCharsets.UTF_8));
            // System.out.println(receivedCmd);
        } catch (IOException e) {
            // To-do: handle disconnect
            e.printStackTrace();
        }
    }
}
