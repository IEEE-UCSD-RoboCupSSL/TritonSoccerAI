package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.Swarm;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Matrix.Mat2D;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;

public class DefendPlanA extends Tactics {

    private final double guardGoalGap = 300; // mm
    protected final BasicEstimator basicEstimator;
    protected final PassEstimator passEstimator;

    public DefendPlanA(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        super(fielders, keeper, foes, ball);
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passEstimator = new PassEstimator(fielders, keeper, foes, ball);
    }

    @Override
    public boolean exec() {
        // should be invoked within a loop
        // invoking contract: ball is hold by an opponent bot

        Line2D middleLine = new Line2D(new Vec2D(0, 0), new Vec2D(0, 0)); // To-do:

        RobotList<Foe> attackingFoes = new RobotList<>();
        for (Foe foe : foes) {
            if (foe.getPos().y < middleLine.p1.y) {
                attackingFoes.add(foe);
            }
        }

        RobotList<Ally> guardFoeFielders = new RobotList<>();
        RobotList<Ally> guardGoalFielders = new RobotList<>();

        for (Foe foe : attackingFoes) {
            Ally nearestFielder = fielders.get(0);
            for (Ally fielder : fielders) {
                if (foe.getPos().sub(fielder.getPos()).mag()
                        < foe.getPos().sub(nearestFielder.getPos()).mag()) {
                    nearestFielder = fielder;
                }
            }
            guardFoeFielders.add(nearestFielder);
        }

        for (Ally fielder : fielders) {
            if (!guardFoeFielders.contains(fielder)) {
                guardGoalFielders.add(fielder);
            }
        }

        /* Delegate some of fielders to Hug-Guard Attacking Foes */
        ArrayList<Vec2D> attackingFoePos = new ArrayList<>();
        for (Foe foe : attackingFoes) {
            attackingFoePos.add(foe.getPos());
        }
        new Swarm(guardFoeFielders).groupTo(attackingFoePos, ball.getPos());


        /* Delegate the rest of fielders to lineup in the midpoint of foe shoot line*/
        Robot holder = basicEstimator.getBallHolder();
        if (holder == null) {
            return false;
        }
        Line2D foeShootLine = new Line2D(holder.getPos(), keeper.getPos());
        Vec2D foeShootLineMidPoint = foeShootLine.midpoint();
        Vec2D foeShootVec = keeper.getPos().sub(holder.getPos());
        Vec2D defenseVec = Mat2D.rotation(90).mult(foeShootVec);
        Line2D defenseLine = new Line2D(foeShootLineMidPoint, foeShootLineMidPoint.add(defenseVec));
        new Swarm(guardGoalFielders).lineUp(guardGoalFielders, defenseLine, guardGoalGap, foeShootLineMidPoint);


        return true;
    }

}
