package Triton;

import Triton.Config.Config;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;

public final class Util {

    /**
     * Acquire the network interface by the name prefix
     */
    public static NetworkInterface getNetIf(String prefix) {
        if (s.equals("null")) return null;
        try {
            Enumeration<NetworkInterface> netIfs = NetworkInterface.getNetworkInterfaces();
            NetworkInterface netIf = null;
            while (netIfs.hasMoreElements()) {
                netIf = netIfs.nextElement();
                if (netIf.getDisplayName().startsWith(prefix)) { // use ethernet
                    break;
                }
            } // otherwise, use the last network interface
            return netIf;
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return a joined multicast socket
     */
    public static DatagramSocket mcSocket(String addr, int port, NetworkInterface netIf) {
        DatagramChannel dc = null;
        try {
            if (netIf == null) {
                MulticastSocket socket = new MulticastSocket(port);
                InetSocketAddress group = new InetSocketAddress(addr, port);
                socket.joinGroup(group, null);
                return socket;
            } else {
                dc = DatagramChannel.open(StandardProtocolFamily.INET)
                        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                        .bind(new InetSocketAddress(port))
                        .setOption(StandardSocketOptions.IP_MULTICAST_IF, netIf);
                InetAddress group = InetAddress.getByName(addr);
                dc.join(group, netIf);
                DatagramSocket socket = dc.socket();
                socket.setSoTimeout(Config.conn().getMcTimeout());
                return dc.socket();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
