package Triton.Config;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;


public class Config {
    public Config(String[] args) {
        cliConfig = new CliConfig();
        connConfig = new ConnectionConfig();
        botConfig = new RobotConfig();
        this.args = args;
    }


    public CliConfig cliConfig = null ;
    public ConnectionConfig connConfig = null;
    public RobotConfig botConfig = null;
    public Team myTeam = Team.BLUE; // default, subject to change in body code
    public Team foeTeam = Team.YELLOW; // default, subject to change in  body code

    public void processAllConfigs() throws IOException {
        cliConfig.processCliArgs(args);
        connConfig.processFromParsingIni(getIniFileByType("main-setup"));
        // botConfig //...
        // ...

        if(cliConfig.isBlueTeam) {
            myTeam = Team.BLUE;
            foeTeam = Team.YELLOW;
            PerspectiveConverter.setTeam(myTeam);
        } else if(cliConfig.isYellowTeam) {
            myTeam = Team.YELLOW;
            foeTeam = Team.BLUE;
            PerspectiveConverter.setTeam(myTeam);
        }
    }

    private File getIniFileByType(String type) throws IOException {
        if(cliConfig.iniFiles == null) {
            System.out.println("Error: must provide at lease an ini file of type 'main-setup'");
            return null;
        }
        for(File file : cliConfig.iniFiles) {
            Wini iniParser = new Wini(file);
            String typeOfIni = iniParser.get("basic-info", "type", String.class);
            if(typeOfIni.equals(type)) {
                return file;
            }
        }
        return null;
    }


    private final String[] args;

}
