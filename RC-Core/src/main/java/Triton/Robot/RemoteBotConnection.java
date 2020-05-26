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

    public void initialize(RemoteCommands.Static_Data sdata) {
        // To-do
    }
    public void sendCommands(RemoteCommands.Remote_Commands cmds) {
        // To-do
    }
    public RemoteCommands.Data_Request receiveDataRequested(String dataName) {
        // To-do
        return RemoteCommands.Data_Request.newBuilder().build();
    }
}