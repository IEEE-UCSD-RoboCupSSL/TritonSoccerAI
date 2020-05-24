package Triton.Robot;


import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import Proto.RemoteCommands;

// Remote connection to a physical robot
public class RemoteBotConnection implements RobotConnection{
    // reserved for future development

    public void connect() {
        // To-do
    }

    public void disconnect() {
        // To-do
    }

    public void initialize(RemoteCommands.static_data sdata) {
        // To-do
    }
    public void sendCommands(RemoteCommands.remote_commands cmds) {
        // To-do
    }
    public RemoteCommands.data_request receiveDataRequested(String dataName) {
        // To-do
        return RemoteCommands.data_request.newBuilder().build();
    }
}