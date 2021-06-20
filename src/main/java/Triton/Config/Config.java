package Triton.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import Triton.CoreModules.Robot.Side;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


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
    public Side mySide = Side.GoalToGuardAtLeft;
    public Side foeSide = Side.GoalToGuardAtRight;
    public int numAllyRobots = 6; // default, subject to change in body code

    public void processAllConfigs() throws IOException {
        cliConfig.processCliArgs(args);
        connConfig.processFromParsingIni(getIniFileByType("main-setup"));
        botConfig.processFromParsingIni(getIniFileByType("robot-specs"));
        // ...

        if(cliConfig.isBlueTeam) {
            myTeam = Team.BLUE;
            foeTeam = Team.YELLOW;
        } else if(cliConfig.isYellowTeam) {
            myTeam = Team.YELLOW;
            foeTeam = Team.BLUE;
        }

        if(cliConfig.isGoalToGuardAtLeft) {
            mySide = Side.GoalToGuardAtLeft;
            PerspectiveConverter.setSide(mySide);
        } else if(cliConfig.isGoalToGuardAtRight) {
            mySide = Side.GoalToGuardAtRight;
            PerspectiveConverter.setSide(mySide);
        }

        this.numAllyRobots = connConfig.numAllyRobots;

        if(cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
            if(myTeam == Team.BLUE) {
                connConfig.simCmdEndpoint.port += 1;
            } else {
                connConfig.simCmdEndpoint.port += 2;
            }
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

    @Override
    public String toString() {
        return "Config{" +
                "myTeam=" + myTeam.toString() +
                ", mySide=" + mySide.toString() +
                ", numAllyRobots=" + numAllyRobots +
                '}' + "\n" +
                cliConfig.toString() + "\n" +
                connConfig.toString() + "\n" +
                botConfig.toString();
    }

    private final String[] args;

}
