package SimuBot.Control;
import Protobuf.*;
import Protobuf.RemoteCommands.data_request;
import Protobuf.RemoteCommands.remote_commands;

public interface RobotConnection {
    public void connect();
    public void disconnect();
    public void initialize(RemoteCommands.static_data sdata);
    public void sendCommands(RemoteCommands.remote_commands cmds);
    public RemoteCommands.data_request receiveDataRequested(String dataName);
}