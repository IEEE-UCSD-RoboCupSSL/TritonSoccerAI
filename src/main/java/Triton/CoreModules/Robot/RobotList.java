package Triton.CoreModules.Robot;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public class RobotList<T> extends ArrayList<T> {

    /* return the number of successful connections */
    public int connectAll() {
        int numSuccessConnect = 0;
        for(T bot : this) {
           if(bot instanceof Ally) {
               if(((Ally) bot).connect()) {
                   numSuccessConnect++;
               }
           }
           else {
               System.out.println("Invalid Type");
           }
        }
        return numSuccessConnect;
    }

    public void runAll(ThreadPoolExecutor threadPool) {
        for(T bot : this) {
            if(bot instanceof Ally) {
                threadPool.submit((Ally) bot);
            }
            else if(bot instanceof Foe) {
                threadPool.submit((Foe) bot);
            } else {
                System.out.println("Invalid Type");
            }
        }
    }


}
