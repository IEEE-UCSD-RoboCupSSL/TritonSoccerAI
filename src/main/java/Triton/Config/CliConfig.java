package Triton.Config;
import Triton.Config.GlobalVaribles.General;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Arrays;


public class CliConfig {
    // initialized with default values, these values are subject to change based on config files &/ cli args

    @Option(names = {"-b", "--blue"}, description = "set team color to be blue")
    public boolean isBlueTeam = false;

    @Option(names = {"-y", "--yellow"}, description = "set team color to be yellow")
    public boolean isYellowTeam = false;

    @Option(names = {"-v", "--virtual"}, description = "run this program for virtual setup (i.e. with a simulator instead of real robots)")
    public boolean isVirtualMode = false;

    @Option(names = {"-t", "--test"}, description = "run in interactive test mode")
    public boolean isTestMode = false;

    @Option(names = {"--testtritonbot"}, description = "run in test-tritonbot(.cpp) mode")
    public boolean isTestTritonBotMode = false;

    @Parameters( description = "one or more .ini file(s) for various types of configurations")
    public File[] iniFiles;

    @Option(names = {"-s", "--simulator"}, description = "enter name of the simulator to run this program with: (grsim, erforcesim)")
    public String simulator = "grsim";

    public void processCliArgs(String[] args) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.parseArgs(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            System.exit(0);
        }

        if(!Arrays.asList(General.supportedSimulators).contains(simulator)) {
            System.out.println("Error: unknown simulators, supported simulators are: " + Arrays.toString(General.supportedSimulators));
            throw new RuntimeException();
        }

        if(!isBlueTeam && !isYellowTeam) {
            System.out.println("Error: must select a team color, run with -h or --help for more details");
            throw new RuntimeException();
        }
        if(isTestMode && isTestTritonBotMode) {
            System.out.println("Error: must choose one between test mode and test-tritonbot mode");
            throw new RuntimeException();
        }


    }

    @Override
    public String toString() {
        return "CliConfig{" +
                "\nisBlueTeam=" + isBlueTeam +
                ", \nisYellowTeam=" + isYellowTeam +
                ", \nisVirtualMode=" + isVirtualMode +
                ", \nisTestMode=" + isTestMode +
                ", \nisTestTritonBotMode=" + isTestTritonBotMode +
                ", \niniFiles=" + Arrays.toString(iniFiles) +
                ", \nsimulator='" + simulator + '\'' + "\n" +
                '}';
    }

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;
}
