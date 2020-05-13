package SimuBot.Control;




import Protobuf.*;
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
        RemoteCommands.remote_commands cmds = RemoteCommands.remote_commands
            .newBuilder().setToInit(staticData).build();
        sendCommands(cmds);
    }


    public void sendCommands(RemoteCommands.remote_commands cmds) {
        // receiver from vBot's perspective; sender from this object's perspective
        vBot.receivePacketFromRemote(cmds);  
    }

    public RemoteCommands.data_request receiveDataRequested(String dataName) {
        
        RemoteCommands.data_request dr = RemoteCommands.data_request
            .newBuilder().setName(dataName).build();
        
        RemoteCommands.remote_commands cmds = RemoteCommands.remote_commands
            .newBuilder().setRequest(dr).build();
        
        sendCommands(cmds);

        // sender from vBot's perspective; receiver from this object's perspective
        return vBot.sendDataToRemote();
    }
}