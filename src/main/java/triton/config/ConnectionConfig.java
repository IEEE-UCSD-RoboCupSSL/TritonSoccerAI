package triton.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.ini4j.*;

public class ConnectionConfig implements IniConfig {
    // initialized with default values, these values are subject to change based on config files &/ cli args
    public ArrayList<BotConn> botConns = null;
    public UdpMulticastConn sslVisionConn = null;
    public UdpMulticastConn gcConn = null;
    public UdpConn simCmdEndpoint = null;
    public int numAllyRobots = 6;

    public ConnectionConfig() {
        botConns = new ArrayList<BotConn>();
        sslVisionConn = new UdpMulticastConn();
        gcConn = new UdpMulticastConn();
        simCmdEndpoint = new UdpConn();
    }

    public void processFromParsingIni(File iniFIle) throws IOException {
        Wini iniParser = new Wini(iniFIle);

        // ssl-vision (could be either the actual hardware ssl-vision system, or the simulated ssl-vision within a simulator)
        sslVisionConn.ipAddr = iniParser.get("ssl-vision", "mc-addr", String.class);
        sslVisionConn.port = iniParser.get("ssl-vision", "mc-port", int.class);

        simCmdEndpoint.ipAddr = iniParser.get("simulator-console", "cmd-addr", String.class);
        simCmdEndpoint.port = iniParser.get("simulator-console", "cmd-port", int.class);

        // ssl-game-controller (performs start/pause game, command free kicks/penalty kicks, game log, tracking, and auto-referee, etc )
        gcConn.ipAddr = iniParser.get("ssl-game-controller", "mc-addr", String.class);
        gcConn.port = iniParser.get("ssl-game-controller", "mc-port", int.class);

        // robot connections
        numAllyRobots = iniParser.get("robot-connections", "num-robots", int.class);
        String queryStr = "robot-";
        for(int id = 0; id < numAllyRobots; id++) {
            BotConn botConn = new BotConn(id);
            botConn.ipAddr = iniParser.get("robot-connections", queryStr + id + "-ip", String.class);
            int portBase = iniParser.get("robot-connections", "robot-port-base", int.class);
            int idOffset = iniParser.get("robot-connections", "id-base-offset", int.class);
            int tritonBotTcpPortOffset = iniParser.get("robot-connections", "tritonbot-tcp-port-offset", int.class);
            int tritonBotUdpPortOffset = iniParser.get("robot-connections", "tritonbot-udp-port-offset", int.class);
            int vBotTcpPortOffset = iniParser.get("robot-connections", "virtual-robot-tcp-port-offset", int.class);
            int vBotUdpSendPortOffset = iniParser.get("robot-connections", "virtual-robot-udp-read-port-offset", int.class);
            int vBotUdpReceivePortOffset = iniParser.get("robot-connections", "virtual-robot-udp-write-port-offset", int.class);
            botConn.tritonBotTcpPort = portBase + (id * idOffset) + tritonBotTcpPortOffset;
            botConn.tritonBotUdpPort = portBase + (id * idOffset) + tritonBotUdpPortOffset;
            botConn.virtualBotTcpPort = portBase + (id * idOffset) + vBotTcpPortOffset;
            botConn.virtualBotUdpSendPort = portBase + (id * idOffset) + vBotUdpSendPortOffset;
            botConn.virtualBotUdpReceivePort = portBase + (id * idOffset) + vBotUdpReceivePortOffset;

            botConns.add(botConn);
        }


    }

    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "botConns=" + botConns +
                ",\n sslVisionConn=" + sslVisionConn +
                ",\n gcConn=" + gcConn +
                ",\n simCmdEndpoint=" + simCmdEndpoint +
                '}';
    }

    public static class BotConn {
        public BotConn(int id) {
            this.id = id;
        }
        // initialized with default values, these values are subject to change based on config files &/ cli args
        public int id = 0;
        public String ipAddr = "127.0.0.1";
        public int tritonBotTcpPort = 6000;
        public int tritonBotUdpPort = 6001;
        public int virtualBotTcpPort = 6002; // only applicable in virtual mode
        public int virtualBotUdpSendPort = 6003;
        public int virtualBotUdpReceivePort = 6004;

        @Override
        public String toString() {
            return "BotConn{" +
                    " \n id=" + id +
                    ",\n ipAddr='" + ipAddr + '\'' +
                    ",\n tritonBotTcpPort=" + tritonBotTcpPort +
                    ",\n tritonBotUdpPort=" + tritonBotUdpPort +
                    ",\n virtualBotTcpPort=" + virtualBotTcpPort +
                    ",\n virtualBotUdpSendPort=" + virtualBotUdpSendPort +
                    ",\n virtualBotUdpReceivePort=" + virtualBotUdpReceivePort +
                    "\n" + '}' + "\n";
        }
    }

    public static class UdpMulticastConn {
        // initialized with default values, these values are subject to change based on config files &/ cli args
        public String ipAddr = "224.5.23.2";
        public int port = 10020;

        @Override
        public String toString() {
            return "UdpMulticastConn{" +
                    "ipAddr='" + ipAddr + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    public static class UdpConn {
        // initialized with default values, these values are subject to change based on config files &/ cli args
        public String ipAddr = "127.0.0.1";
        public int port = 20011;

        @Override
        public String toString() {
            return "UdpConn{" +
                    "ipAddr='" + ipAddr + '\'' +
                    ", port=" + port +
                    '}';
        }
    }
}
