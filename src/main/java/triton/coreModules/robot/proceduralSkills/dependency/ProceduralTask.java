package triton.coreModules.robot.proceduralSkills.dependency;

import triton.coreModules.robot.ally.Ally;
import triton.misc.modulePubSubSystem.FieldPubSubPair;

import java.util.concurrent.Callable;

public abstract class ProceduralTask implements Callable<Boolean> {
    protected Ally thisRobot = null;

    /* customized cancel mechanism, the FutureTask cancel mechanism alone is not enough */
    private final FieldPubSubPair<Boolean> cancelSignal = new FieldPubSubPair<>("From:ProceduralTask",
            "CancelSignal " + System.identityHashCode(this), false);

    public void sendCancelSignal(boolean signal) {
        cancelSignal.pub.publish(signal);
    }

    public boolean isCancelled() {
        Boolean rtn = cancelSignal.sub.getMsg();
        if(rtn != null) return rtn;
        else return false;
    }

    public void registerThisRobot(Ally bot) {
        thisRobot = bot;
    }



}