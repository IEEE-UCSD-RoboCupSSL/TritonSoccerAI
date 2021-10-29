package triton.coreModules.robot.ally.advancedSkills;

import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;

public class VirtualAutoCap {


    public static void exec(Ally ally, Ball ball) {
        if(ally.isHoldingBall()) {
            ally.moveAt(new Vec2D(0, 8));
            ally.spinTo(ally.getDir());
        } else {
            Vec2D ballPos = ball.getPos();
            Vec2D botPos = ally.getPos();
            Vec2D targetPos = ballPos.sub(ballPos.sub(botPos)).normalized().scale(85);

            ally.moveTo(targetPos);
            ally.spinTo(ally.getDir());
        }

    }

//    public static void exec(Ally ally, Vec2D endPoint, double angle) {
//        double targetAngle = normAng(angle);
//
//        ally.spinTo(targetAngle);
//
//        ArrayList<Vec2D> path = ally.findPath(endPoint);
//        if (path != null && path.size() > 0) {
//            Vec2D nextNode;
//            if (path.size() == 1) nextNode = path.get(0);
//            else if (path.size() == 2) nextNode = path.get(1);
//            else nextNode = path.get(1);
//
//            if (path.size() <= 2) ally.moveTo(nextNode);
//            else ally.moveToNoSlowDown(nextNode);
//        } else {
//            ally.moveAt(new Vec2D(0, 0));
//        }
//    }
}
