package Triton;

import Triton.Config.OldConfigs.ObjectConfig;
import Triton.Config.OldConfigs.SystemConfig;
import org.ini4j.*;

import java.io.File;

public class IniFileProcessor {

    /**
     * Read from the default init files
     */
    public static void readIni() {
        try {
            Wini ini = new Wini(new File(SystemConfig.INIT_PATH));

            ObjectConfig.ROBOT_COUNT = ini.get("ROBOT", "ROBOT_COUNT", int.class);
            ObjectConfig.ROBOT_RADIUS = ini.get("ROBOT", "ROBOT_RADIUS", double.class);
            ObjectConfig.ROBOT_MIN_RADIUS = ini.get("ROBOT", "ROBOT_MIN_RADIUS", double.class);
            ObjectConfig.ROBOT_DRIB_WIDTH = ini.get("ROBOT", "ROBOT_DRIB_WIDTH", double.class);
            ObjectConfig.MAX_KICK_VEL = ini.get("ROBOT", "MAX_KICK_VEL", double.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
