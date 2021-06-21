package Triton.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral.ProgramMode;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral.SimulatorName;

import java.io.File;
import java.util.Arrays;


public class CliConfig {
    // initialized with default values, these values are subject to change based on config files &/ cli args
    @Option(names = {"-b", "--blue"}, description = "set team color to be blue")
    public boolean isBlueTeam = false;

    @Option(names = {"-y", "--yellow"}, description = "set team color to be yellow")
    public boolean isYellowTeam = false;

    @Option(names = {"-l", "--left"}, description = "set team to guard the goal at left side")
    public boolean isGoalToGuardAtLeft  = false;

    @Option(names = {"-r", "--right"}, description = "set team to guard the goal at the right side")
    public boolean isGoalToGuardAtRight = false;


    @Option(names = {"-v", "--virtual"}, description = "run this program for virtual setup (i.e. with a simulator instead of real robots)")
    public boolean isVirtualSetup = false;


    @Option(names = {"-m", "--mode"}, description = "designate a mode: [normal, test, test-tb]")
    private String progModeStr = "test";


    @Parameters( description = "one or more .ini file path(s) for various types of configurations")
    public File[] iniFiles = null;

    @Option(names = {"-s", "--simulator"}, description = "enter name of the simulator to run this program with: [grsim, erforcesim]")
    private String simulatorStr = "grsim";

    public ProgramMode progMode = ProgramMode.Normal;
    public SimulatorName simulator = SimulatorName.GrSim;

    public void processCliArgs(String[] args) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.parseArgs(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            System.exit(0);
        }

        if(iniFiles == null) {
            iniFiles = new File[1];
            iniFiles[0] = new File("example-mainsetup.ini");
        }

        switch (progModeStr) {
            case "normal" -> progMode = ProgramMode.Normal;
            case "test" -> progMode = ProgramMode.Test;
            case "test-tb" -> progMode = ProgramMode.TestTritonBot;
            default -> {
                System.out.println("Error: unknown program mode");
                throw new RuntimeException();
            }
        }

        switch (simulatorStr) {
            case "grsim" -> simulator = SimulatorName.GrSim;
            case "erforcesim" -> simulator = SimulatorName.ErForceSim;
            default -> {
                System.out.println("Error: unknown simulators");
                throw new RuntimeException();
            }
        }
//
//        if(isBlueTeam && isYellowTeam) {
//            System.err.println("Error: can only select one team color, run with -h or --help for more details");
//            throw new RuntimeException();
//        }
//
//        if(!isBlueTeam && !isYellowTeam) {
//            System.
//        }

    }

    @Override
    public String toString() {
        return "CliConfig{" +
                ", \nisVirtualMode=" + isVirtualSetup +
                ", \nprogMode='" + progModeStr + '\'' +
                ", \niniFiles=" + Arrays.toString(iniFiles) +
                ", \nsimulator='" + simulatorStr + '\'' + "\n" +
                '}';
    }

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;
}
