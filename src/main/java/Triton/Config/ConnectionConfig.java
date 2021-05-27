package Triton.Config;

import java.util.ArrayList;

public class ConnectionConfig {
    public ConnectionConfig() {
        botConns = new ArrayList<BotConn>();
        sslVisionConn = new UdpMulticastConn();
        gcConn = new UdpMulticastConn();
    }


    public static class BotConn {
        // initialized with default values, these values are subject to change based on config files &/ cli args
        public String ipAddr = "127.0.0.1";
        public int tritonBotTcpPort = 6000;
        public int tritonBotUdpPort = 6001;
        public int virtualBotTcpPort = 6002; // only applicable in virtual mode
    }

    public static class UdpMulticastConn {
        // initialized with default values, these values are subject to change based on config files &/ cli args
        public String ipAddr = "224.5.23.2";
        public int port = 10020;
    }

    public ArrayList<BotConn> botConns = null;
    public UdpMulticastConn sslVisionConn = null;
    public UdpMulticastConn gcConn = null;


}
