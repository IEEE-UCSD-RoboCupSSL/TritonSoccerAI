package Triton.Config.GlobalVariblesAndConstants;

public class GvcGeneral {

    public enum ProgramMode {
        Normal,
        Test,
        TestTritonBot
    }

    public enum SimulatorName {
        GrSim,
        ErForceSim
    }


    public static final int TotalNumOfThreads = 150;



    public static int keeperId = 5;

    public static float MAX_KICK_SPEED = 5.00f;
}
