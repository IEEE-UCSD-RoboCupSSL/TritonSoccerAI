package Triton.Detection;
import Proto.*;
import Proto.RemoteCommands.Data_Request;
import Proto.RemoteCommands.Remote_Commands;
import java.io.IOException;

public interface RobotConnection {
    public void connect() throws IOException;
    public void disconnect() throws IOException;
    public void initialize(RemoteCommands.Static_Data sdata) throws IOException;
    public void sendCommands(RemoteCommands.Remote_Commands cmds) throws IOException;
    public RemoteCommands.Data_Request receiveDataRequested(String dataName) throws IOException;
}