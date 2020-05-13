package SimuBot.Control;



import java.net.DatagramSocket;
import java.lang.System.Logger.Level;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.*;
import Protobuf.*;
import Protobuf.RemoteCommands.data_request;
import Protobuf.RemoteCommands.remote_commands;
import SimuBot.Control.VirtualBot.*;

// connection to a virtual bot in grSim simulator
public class VirtualBotConnection implements RobotConnection{
    private VirtualBot vBot;
    public int botID;
    public String teamColor; 

    public VirtualBotConnection(VirtualBot vBot, int botID, String teamColor) {
        this.vBot = vBot;
        this.botID = botID;
        this.teamColor = teamColor;
    }

    
    public void connect() {
        if(!VirtualBot.isConnectedToGrSim) {
            VirtualBot.connectToGrSim();
        }
        vBot.setBotID_Color(botID, teamColor);
    }

    public void disconnect() {
        VirtualBot.disconnectToGrSim();
    }
    
    public void initialize(RemoteCommands.static_data staticData) {
        vBot.setStaticData(staticData);
    }

    


    public void sendCommands(RemoteCommands.remote_commands cmds) {
        vBot.setCommands(cmds);
    }



    public RemoteCommands.data_request receiveDataRequested(String dataName) {
        
        RemoteCommands.data_request dr = data_request.newBuilder()
            .setName(dataName).build();
        
        RemoteCommands.remote_commands cmds = RemoteCommands.remote_commands
            .newBuilder().setName("request").setRequest(dr).build();
        
        sendCommands(cmds);
        return vBot.getDataRequested();
    }
        
   

}