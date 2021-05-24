package Triton.VirtualBot;

import Proto.VFirmwareAPI;
import Triton.Config.Config;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Misc.ModulePubSubSystem.Publisher;
import Triton.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;

public class TritonBotReceiveModule implements Module {
    private final static int MAX_BUFFER_SIZE = 67108864;

    private final Publisher<VFirmwareAPI.VF_Commands> vfirmCmdPub;

    private DatagramSocket socket;
    private DatagramPacket packet;

    public TritonBotReceiveModule(String ip, int port, int ID) {
        vfirmCmdPub = new FieldPublisher<VFirmwareAPI.VF_Commands>("vfirm", "cmd" + ID,
                VFirmwareAPI.VF_Commands.getDefaultInstance());

        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        try {
            NetworkInterface netIf = Util.getNetIf(Config.conn().getGrsimNetIf());
            socket = Util.mcSocket(Config.conn().getGrsimMcAddr(),
                    Config.conn().getGrsimMcPort(),
                    netIf);
            packet = new DatagramPacket(buffer, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket.receive(packet);

            ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(),
                    packet.getOffset(), packet.getLength());
            VFirmwareAPI.VF_Commands tritonBotCommands =
                    VFirmwareAPI.VF_Commands.parseFrom(input);

            vfirmCmdPub.publish(tritonBotCommands);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
