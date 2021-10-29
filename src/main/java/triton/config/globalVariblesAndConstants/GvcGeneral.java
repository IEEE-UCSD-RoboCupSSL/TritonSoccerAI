package triton.config.globalVariblesAndConstants;

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
    public static int socketBufferSize = 1024;
}
