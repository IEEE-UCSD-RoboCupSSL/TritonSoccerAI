package triton.coreModules.ai.skills;

import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.dijkstra.Pdg;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.proceduralSkills.dependency.ProceduralTask;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.modulePubSubSystem.FieldPubSubPair;

import java.util.concurrent.ExecutionException;

import static triton.Util.delay;

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

    public CoordinatedPass(Pdg.AllyPassNode passerNode, Pdg.AllyRecepNode receiverNode,
                           Ball ball, BasicEstimator estimator) {
        this.basicEstimator = estimator;
        this.passer = passerNode.getBot();
        this.receiver = receiverNode.getBot();
        this.ball = ball;
        passTask = new PassTask(passerNode, ball);
        receiveTask = new ReceiveTask(receiverNode, ball);
    }

    /* return null if it's in the middle of execution; return true if successfully compl*/
    public PassShootResult execute() throws ExecutionException, InterruptedException {
        if(basicEstimator.getBallHolder() instanceof Foe) {
            passer.cancelProceduralTask();
            receiver.cancelProceduralTask();
        }
        
        if(!passer.isProcedureCompleted()){
            passer.executeProceduralTask(passTask);
        } else {
            if(passer.isProcedureCancelled()) {
                cancelResetTasks();
                return PassShootResult.fail;
            }
            if(passer.getProcedureReturnStatus()) {
                passer.stop();
            } else {
                cancelResetTasks();
                return PassShootResult.fail;
            }
            passer.resetProceduralTask();
        }

        if(!receiver.isProcedureCompleted()) {
            receiver.executeProceduralTask(receiveTask);
        } else {
            if(receiver.isProcedureCancelled()) {
                cancelResetTasks();
                return PassShootResult.fail;
            }
            PassShootResult rtn;
            if(receiver.getProcedureReturnStatus()){
                rtn = PassShootResult.success;
            } else {
                rtn = PassShootResult.fail;
            }
            cancelResetTasks();
            return rtn;
        }
        return PassShootResult.Executing;
    }

    public void cancelResetTasks() {
        passer.stop();
        receiver.stop();
        passer.cancelProceduralTask();
        receiver.cancelProceduralTask();
        while(!passer.isProcedureCancelled()) delay(3);
        while(!receiver.isProcedureCancelled()) delay(3);
        receiver.resetProceduralTask();
        passer.resetProceduralTask();
    }

    private static class PassTask extends ProceduralTask {
        private final Ally passer;
        private final Pdg.AllyPassNode passNode;
        public final Ball ball;
        public PassTask(Pdg.AllyPassNode passerNode, Ball ball) {
            this.passer = passerNode.getBot();
            this.passNode = passerNode;
            this.ball = ball;
        }

        @Override
        public Boolean call() throws Exception {
            Vec2D targetPoint = passNode.getPassPoint();
            double targetDir = passNode.getAngle();

            while(!passer.isHoldingBall()) {
                if(isCancelled()) return false;
                passer.getBall(ball);
            }

            while(!passer.isPosArrived(targetPoint) || !passer.isDirAimed(targetDir)) {
                if(isCancelled()) return false;
                if(!passer.isHoldingBall()) return false;
                passer.curveTo(targetPoint, targetDir);
                delay(3);
            }
            passer.kick(passNode.getKickVec());
            delay(500);
            passer.stop();
            return true;
        }
    }

    private static class ReceiveTask extends ProceduralTask {
        private final Ally receiver;
        private final Pdg.AllyRecepNode recepNode;
        private final Ball ball;

        public ReceiveTask(Pdg.AllyRecepNode recepNode, Ball ball) {
            this.receiver = recepNode.getBot();
            this.recepNode = recepNode;
            this.ball = ball;
        }

        @Override
        public Boolean call() throws Exception {
            Vec2D targetPoint = recepNode.getReceptionPoint();
            double targetDir = recepNode.getAngle();

            while(!receiver.isPosArrived(targetPoint) || !receiver.isDirAimed(targetDir)) {
                if(isCancelled()) return false;
                receiver.curveTo(targetPoint, targetDir);
                delay(3);
            }

//            long t0 = System.currentTimeMillis();
//            while(System.currentTimeMillis() - t0 < 1000) {
//                if(isCancelled()) return false;
//                receiver.stop();
//                if(receiver.isHoldingBall()) break;
//            }

            while(!receiver.isHoldingBall()) {
                if(isCancelled()) return false;
                receiver.dynamicIntercept(ball, targetDir);
                delay(3);
            }
            receiver.stop();
            return true;
        }

    }

}
