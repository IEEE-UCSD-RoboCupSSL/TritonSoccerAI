package Triton.CoreModules.Robot;

import Triton.App;

import java.util.ArrayList;

public class RobotList<T> extends ArrayList<T> {

    /* return the number of successful connections */
    public int connectAll() {
        int numSuccessConnect = 0;
        for (T bot : this) {
            if (bot instanceof Ally) {
                if (((Ally) bot).connect()) {
                    numSuccessConnect++;
                }
            } else {
                System.out.println("Invalid Type");
            }
        }
        return numSuccessConnect;
    }

    public void runAll() {
        for (T bot : this) {
            if (bot instanceof Robot) {
                App.threadPool.submit((Robot) bot);
            } else {
                System.out.println("Invalid Type");
            }
        }
    }

    public void stopAll() {
        for (T bot : this) {
            if (bot instanceof Ally) {
                ((Ally) bot).stop();
            } else {
                System.out.println("Invalid Type");
            }
        }
    }

    public static int getFoeKeeperID() {
        return 5;
    }
}
