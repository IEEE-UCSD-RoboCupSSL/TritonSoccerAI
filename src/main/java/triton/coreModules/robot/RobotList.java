package triton.coreModules.robot;

import triton.App;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.coreModules.robot.ally.Ally;
import triton.Util;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    public ArrayList<ScheduledFuture<?>> runAll() {
        ArrayList<ScheduledFuture<?>> robotFutures = new ArrayList<>();
        for (T bot : this) {
            if (bot instanceof Robot) {
                ScheduledFuture<?> robotFuture = App.threadPool.scheduleAtFixedRate(
                            (Robot) bot,
                        0, Util.toPeriod(GvcModuleFreqs.ROBOT_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
                robotFutures.add(robotFuture);
            } else {
                System.out.println("Invalid Type");
            }
        }
        return robotFutures;
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

    public T get(int id) {
        for(T bot : this) {
            if(((Robot) bot).ID == id) {
                return bot;
            }
        }
        return null;
    }

    public RobotList<T> copy() {
        RobotList<T> list = new RobotList<>();
        list.addAll(this);
        return list;
    }

    public static int getFoeKeeperID() {
        return 5;
    }
}
