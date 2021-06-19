package Triton.CoreModules.AI.AI_Skills;

import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.ProceduralSkills.Dependency.ProceduralTask;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;

import java.util.concurrent.ExecutionException;

import static Triton.Util.delay;

public class CoordinatedPass {
    private final Ally passer;
    private final Ally receiver;
    private final Ball ball;
    private final BasicEstimator basicEstimator;

    private FieldPubSubPair<Boolean> passTaskCanceller;
    private FieldPubSubPair<Boolean> receiveTaskCanceller;
    private PassTask passTask;
    private ReceiveTask receiveTask;

    public static enum PassShootResult {
        success,
        fail,
        Executing
    }

    public CoordinatedPass(PUAG.AllyPassNode passerNode, PUAG.AllyRecepNode receiverNode, 
                           Ball ball, BasicEstimator estimator) {
        this.basicEstimator = estimator;
        this.passer = passerNode.getBot();
        this.receiver = receiverNode.getBot();
        this.ball = ball;
        passTaskCanceller = new FieldPubSubPair<>("[Pair]DefinedIn:CoordinatedPass", "CancelPassTask", false);
        receiveTaskCanceller = new FieldPubSubPair<>("[Pair]DefinedIn:CoordinatedPass", "CancelReceiveTask", false);

        passTask = new PassTask(passerNode, passTaskCanceller.sub);
        receiveTask = new ReceiveTask(receiverNode, ball, receiveTaskCanceller.sub);
    }

    /* return null if it's in the middle of execution; return true if successfully compl*/
    public PassShootResult execute() throws ExecutionException, InterruptedException {
        if(basicEstimator.getBallHolder() instanceof Foe) {
            passTaskCanceller.pub.publish(true);
            receiveTaskCanceller.pub.publish(true);
        }
        
        if(!passer.isProcedureCompleted()){
            passer.executeProceduralTask(passTask);
        } else {
            if(passer.isProcedureCancelled()) {
                passer.resetProceduralTask();
                return PassShootResult.fail;
            }
            if(passer.getProcedureReturnStatus()) {
                passer.stop();
            } else {
                passer.resetProceduralTask();
                return PassShootResult.fail;
            }
            passer.resetProceduralTask();
        }

        if(!receiver.isProcedureCompleted()) {
            receiver.executeProceduralTask(receiveTask);
        } else {
            if(receiver.isProcedureCancelled()) {
                receiver.resetProceduralTask();
                return PassShootResult.fail;
            }
            PassShootResult rtn;
            if(receiver.getProcedureReturnStatus()){
                receiver.stop();
                rtn = PassShootResult.success;
            } else {
                rtn = PassShootResult.fail;
            }
            receiver.resetProceduralTask();
            return rtn;
        }
        return PassShootResult.Executing;
    }




    private static class PassTask extends ProceduralTask {
        private final Ally passer;
        private final PUAG.AllyPassNode passNode;
        private final FieldSubscriber<Boolean> canceller;
        public PassTask(PUAG.AllyPassNode passerNode, FieldSubscriber<Boolean> canceller) {
            this.canceller = canceller;
            this.passer = passerNode.getBot();
            this.passNode = passerNode;
        }

        @Override
        public Boolean call() throws Exception {
            Vec2D targetPoint = passNode.getPassPoint();
            double targetDir = passNode.getAngle();
            
            while(!passer.isPosArrived(targetPoint) || !passer.isDirAimed(targetDir)) {
                if(cancelConditions()) return false;
                passer.curveTo(targetPoint, targetDir);
                delay(3);
            }
            passer.kick(passNode.getKickVec());
            delay(500);
            passer.stop();
            return true;
        }
        
        boolean cancelConditions() {
            if(isCancelled()) return true;
            if(canceller.getMsg()) {
                canceller.forceSetMsg(false);
                return true;
            }
            return false;
        }
        
    }

    private static class ReceiveTask extends ProceduralTask {
        private final Ally receiver;
        private final PUAG.AllyRecepNode recepNode;
        private final Ball ball;

        private final FieldSubscriber<Boolean> canceller;
        public ReceiveTask(PUAG.AllyRecepNode recepNode, Ball ball, FieldSubscriber<Boolean> canceller) {
            this.receiver = recepNode.getBot();
            this.recepNode = recepNode;
            this.canceller = canceller;
            this.ball = ball;
        }

        @Override
        public Boolean call() throws Exception {
            Vec2D targetPoint = recepNode.getReceptionPoint();
            double targetDir = recepNode.getAngle();

            while(!receiver.isPosArrived(targetPoint) || !receiver.isDirAimed(targetDir)) {
                if(cancelConditions()) return false;
                receiver.curveTo(targetPoint, targetDir);
                delay(3);
            }

            while(!receiver.isHoldingBall()) {
                if(cancelConditions()) return false;
                receiver.dynamicIntercept(ball, targetDir);
                delay(3);
            }
            receiver.stop();
            return true;
        }

        boolean cancelConditions() {
            if(isCancelled()) return true;
            if(canceller.getMsg()) {
                canceller.forceSetMsg(false);
                return true;
            }
            return false;
        }
    }




}
