package Triton.VirtualBot;

import Proto.FirmwareAPI;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.ConnectionConfig;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Util;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs.VIRTUAL_MCU_TOP_FREQ;
import static Triton.Util.delay;

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
    private Config config;

    // private FieldPublisher<String> debugStrPub;
    public VirtualMcuTopModule(Config config, int id,
                               FieldPublisher<FirmwareAPI.FirmwareCommand> cmdPub,
                               FieldSubscriber<FirmwareAPI.FirmwareData> dataSub) {
        this.config = config;
        isConnectedToTritonBotPubSub =
                new FieldPubSubPair<>("Internal:VirtualMcuTopModule",
                        "isConnectedToTritonBot " + id, false);
        // debugStrPub = new FieldPublisher<>("From:VirtualMcuTopModule", "DebugString " + id, "???");

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
            severSocket = new ServerSocket(port);
            socket = severSocket.accept();
            socketOut = new PrintWriter(socket.getOutputStream(), true);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            System.out.println("\033[0;32m VirtualBot " + id + " successfully accepted " +
                    "TritonBot(cpp)'s tcp connection request \033[0m");
            isConnectedToTritonBotPubSub.pub.publish(true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        App.runModule(new VirtualBotSend(config, id, dataSub), VIRTUAL_MCU_TOP_FREQ);
        App.runModule(new VirtualBotReceive(config, id, cmdPub), VIRTUAL_MCU_TOP_FREQ);


    }

    @Override
    public void run() {

        if (isFirstRun) {
            setup();
            isFirstRun = false;
        }
        if (socket == null || socketIn == null) {
            isFirstRun = true;
            System.out.println("Something went wrong in VirtualMcuTopModule.java");
            return;
        }

        delay(5000);


    }


}
