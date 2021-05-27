package Triton.Config;

public class Config {
    public Config() {
        cliConfig = new CliConfig();
        connConfig = new ConnectionConfig();
        botConfig = new RobotConfig();
    }


    public CliConfig cliConfig = null ;
    public ConnectionConfig connConfig = null;
    public RobotConfig botConfig = null;


}
