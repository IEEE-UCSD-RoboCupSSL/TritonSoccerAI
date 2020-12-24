package Triton.Config;

import org.javatuples.Pair;

public class ConnectionConfig {
    // Grsim
    public static final String GRSIM_MC_ADDR = "224.5.23.2";
    public static final int GRSIM_MC_PORT = 10020;

    // IP Addr & Port Base: Port Base + Offset = TCP Port/3 diff UDP Port
    public static final Pair<String, Integer> ROBOT_0_IP = new Pair<>("localhost", 6000);
    public static final Pair<String, Integer> ROBOT_1_IP = new Pair<>("localhost", 6100);
    public static final Pair<String, Integer> ROBOT_2_IP = new Pair<>("localhost", 6200);
    public static final Pair<String, Integer> ROBOT_3_IP = new Pair<>("localhost", 6300);
    public static final Pair<String, Integer> ROBOT_4_IP = new Pair<>("localhost", 6400);
    public static final Pair<String, Integer> ROBOT_5_IP = new Pair<>("localhost", 6500);

    public static final int TCP_OFFSET = 0;
    public static final int COMMAND_UDP_OFFSET = 1;
    public static final int DATA_UDP_OFFSET = 2;
    public static final int VISION_UDP_OFFSET = 3;

    // Multicast
    public static final String MC_ADDR = "224.5.0.1";
    public static final int MC_PORT = 10020;
    public static final long MC_INTERVAL = 5; // 5 ms

}