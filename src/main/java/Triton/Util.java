package Triton;

import Triton.Config.OldConfigs.jsonConfig;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

public final class Util {


    /**
     * Acquire the network interface by the name prefix
     */
    public static NetworkInterface getNetIf(String prefix) {
        if (prefix.equals("null")) return null;
        try {
            Enumeration<NetworkInterface> netIfs = NetworkInterface.getNetworkInterfaces();
            NetworkInterface netIf = null;
            while (netIfs.hasMoreElements()) {
                netIf = netIfs.nextElement();
                System.out.println("Discovered network interface: " + netIf.getDisplayName());
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
                socket.setSoTimeout(jsonConfig.conn().getMcTimeout());
                return dc.socket();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     *  Convert frequency in Hz to period of given Unit
     *
     *  Side Note: ScheduledThreadPoolExecutor.schedule is of nano second precision
     */
    public static long toPeriod(double freqInHz, TimeUnit unit) {

        long periodInNanos = (long)(1e9 / freqInHz);

        return unit.convert(periodInNanos, TimeUnit.NANOSECONDS);
    }


    public static void delay(long durationInMillis) {
        try {
            Thread.sleep(durationInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void sleepForever() {
        while (true) {
            delay(1000);
        }
    }


    public static void sleepForever(FieldSubscriber<Boolean> canceller) {
        while (!canceller.getMsg()) {
            delay(1000);
        }
    }
}