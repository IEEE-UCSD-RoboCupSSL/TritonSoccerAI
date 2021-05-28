package Triton.Config;
import Triton.CoreModules.Robot.Team;
import org.ini4j.Wini;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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
    public Team team = Team.BLUE;

    public void processAllConfigs() throws IOException {
        cliConfig.processCliArgs(args);
        connConfig.processFromParsingIni(getIniFileByType("main-setup"));
        // botConfig //...
        // ...

        if(cliConfig.isBlueTeam) {
            team = Team.BLUE;
        } else if(cliConfig.isYellowTeam) {
            team = Team.YELLOW;
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
