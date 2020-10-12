package Triton.Config;

public class ConnectionConfig {
    // Grsim
    public static final String GRSIM_MC_ADDR = "224.5.23.2";
    public static final int GRSIM_MC_PORT = 10020;

    // TCP
    public static final int[] TCP_PORTS = {8900, 8901, 8902, 8903, 8904, 8905};

    // Multicast
    public static final String MC_ADDR = "224.5.0.1";
    public static final int MC_PORT = 10020;
    public static final long MC_INTERVAL = 5; // 5 ms

    // UDP
    public static final int[] UDP_PORTS = {8950, 8951, 8952, 8953, 8954, 8955};
}