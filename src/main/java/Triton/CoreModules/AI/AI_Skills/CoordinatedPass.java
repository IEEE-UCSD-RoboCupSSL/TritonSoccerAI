package Triton.CoreModules.AI.AI_Skills;

import Triton.CoreModules.AI.ReceptionPoint;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.ProceduralSkills.Dependency.ProceduralTask;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;

import java.util.concurrent.ExecutionException;

public class CoordinatedPass {
    private final Ally passer;
    private final Ally receiver;
    private final ReceptionPoint receptionPoint;
    private final Ball ball;

    private FieldPubSubPair<Vec2D> ballPosPubSubPair;
    private PassTask passTask;
    private ReceiveTask receiveTask;

    public static enum PassShootResult {
        success,
        fail,
        Executing
    }

    public CoordinatedPass(Ally holder, Ally receiver, ReceptionPoint receptionPoint, Ball ball) {
        this.passer = holder;
        this.receiver = receiver;
        this.receptionPoint = receptionPoint;
        this.ball = ball;
        ballPosPubSubPair = new FieldPubSubPair<>("[Pair]DefinedIn:CoordinatedPass", "ballPos", ball.getPos());
        passTask = new PassTask(passer);
        receiveTask = new ReceiveTask(receiver, receptionPoint);
    }

    /* return null if it's in the middle of execution; return true if successfully compl*/
    public PassShootResult execute() throws ExecutionException, InterruptedException {
        ballPosPubSubPair.pub.publish(ball.getPos());
        if(!passer.isProcedureCompleted()){
            passer.executeProceduralTask(passTask);
        } else {
            passer.resetProceduralTask();
            if(passer.getProcedureReturnStatus()) {
                passer.stop();
            } else {
                return PassShootResult.fail;
            }
        }
        if(!receiver.isProcedureCompleted()) {
            receiver.executeProceduralTask(receiveTask);
        } else {
            receiver.resetProceduralTask();
            if(receiver.getProcedureReturnStatus()){
                receiver.stop();
                return PassShootResult.success;
            } else {
                return PassShootResult.fail;
            }

        }
        return PassShootResult.Executing;
    }




    private static class PassTask extends ProceduralTask {
        private final Ally passer;
        public PassTask(Ally passer) {
            this.passer = passer;
        }

        @Override
        public Boolean call() throws Exception {



            return null;
        }
    }

    private static class ReceiveTask extends ProceduralTask {
        private final Ally receiver;
        private final ReceptionPoint receptionPoint;
        public ReceiveTask(Ally receiver, ReceptionPoint receptionPoint) {
            this.receiver = receiver;
            this.receptionPoint = receptionPoint;
        }

        @Override
        public Boolean call() throws Exception {
            return null;
        }
    }




}
