package Triton.CoreModules.AI.AI_Tactics;

import Triton.CoreModules.AI.AI_Skills.Swarm;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;

public class DefendPlanA extends Tactics {

    private double foeBlockOffset;
    private final double GUARD_GOAL_GAP = 300; // mm
    protected final BasicEstimator basicEstimator;
    protected final PassInfo passInfo;

    public DefendPlanA(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball, double foeBlockOffset) {
        super(fielders, keeper, foes, ball);
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passInfo = new PassInfo(fielders, foes, ball);
        this.foeBlockOffset = foeBlockOffset;
    }

    public void setFoeBlockOffset(double foeBlockOffset) {
        this.foeBlockOffset = foeBlockOffset;
    }

    @Override
    public boolean exec() {
        // should be invoked within a loop
        // invoking contract: ball is hold by an opponent bot

        RobotList<Foe> attackingFoes = new RobotList<>();
        for (Foe foe : foes) {
            if (foe.getPos().y < 0) {
                attackingFoes.add(foe);
            }
        }

        if (attackingFoes.size() > 0) {
            RobotList<Ally> guardFoeFielders = new RobotList<>();
            RobotList<Ally> guardGoalFielders = new RobotList<>();

            for (Foe foe : attackingFoes) {
                Ally nearestFielder = fielders.get(0);
                for (Ally fielder : fielders) {
                    if (!guardFoeFielders.contains(fielder) &&
                            foe.getPos().sub(fielder.getPos()).mag() < foe.getPos().sub(nearestFielder.getPos()).mag()) {
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
                Vec2D foePos = foe.getPos();
                Vec2D goalPos = new Vec2D(0, -4500);
                Vec2D foeToGoalVec = goalPos.sub(foePos).normalized();
                attackingFoePos.add(foe.getPos().add(foeToGoalVec.scale(foeBlockOffset)));
            }

            new Swarm(guardFoeFielders).groupTo(attackingFoePos, ball.getPos());

            /* Delegate the rest of fielders to lineup in the midpoint of foe shoot line*/
            Robot holder = basicEstimator.getBallHolder();
            if (holder == null) {
                return false;
            }

            Vec2D holderPos = holder.getPos();
            Vec2D holderFaceVec = new Vec2D(holder.getDir());

            double x;
            double y = -4500;
            if (Math.abs(holderFaceVec.y) <= 0.0001 || Math.abs(holderFaceVec.x) <= 0.0001) {
                x = holderPos.x;
            } else {
                double m = holderFaceVec.y / holderFaceVec.x;
                double b = holderPos.y - (holderPos.x * m);
                x = (y - b) / m;
            }
            Line2D holderShootLine = new Line2D(holderPos, new Vec2D(x, y));
            Vec2D holderShootLineMidPoint = holderShootLine.midpoint();
            Vec2D defenseVec = holderFaceVec.rotate(90);
            new Swarm(guardGoalFielders).lineUp(guardGoalFielders, defenseVec, GUARD_GOAL_GAP, holderShootLineMidPoint);
            return true;
        } else {
            ArrayList<Vec2D> attackingFoePos = new ArrayList<>();
            for (Foe foe : foes) {
                if (foe.getID() == RobotList.getFoeKeeperID())
                    continue;

                Vec2D foePos = foe.getPos();
                Vec2D goalPos = new Vec2D(0, -4500);
                Vec2D foeToGoalVec = goalPos.sub(foePos).normalized();
                attackingFoePos.add(foe.getPos().add(foeToGoalVec.scale(foeBlockOffset)));
            }

            new Swarm(fielders).groupTo(attackingFoePos, ball.getPos());
            return true;
        }
    }
}
