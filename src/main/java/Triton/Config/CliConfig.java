package Triton.Config;

public class CliConfig {
    // initialized with default values, these values are subject to change based on config files &/ cli args
    public boolean isBlueTeam = true;
    public boolean isVirtualMode = true;
    public boolean isTestMode = false;
    public String simulator = "grsim";

    public void processCliArgs(String[] args) {

    }

}
