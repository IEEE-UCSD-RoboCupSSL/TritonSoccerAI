package triton.coreModules.ai.tactics;

import triton.config.Config;
import triton.coreModules.ai.skills.Swarm;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.estimators.PassInfo;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.Robot;
import triton.coreModules.robot.RobotList;
import triton.misc.math.geometry.Line2D;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

public class DefendPlanA extends Tactics {

    private double foeBlockOffset;
    private final double GUARD_GOAL_GAP = 300; // mm
    protected final BasicEstimator basicEstimator;
    protected final PassInfo passInfo;
    private final Config config;

    public DefendPlanA(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball, double foeBlockOffset, Config config) {
        super(fielders, keeper, foes, ball);
        this.config = config;
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passInfo = new PassInfo();
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
            ArrayList<Double> attackingFoeAng = new ArrayList<>();
            for (Foe foe : attackingFoes) {
                Vec2D foePos = foe.getPos();
                Vec2D ballPos = ball.getPos();
                Vec2D foeToBallVec = ballPos.sub(foePos).normalized();
                attackingFoeAng.add(foeToBallVec.toPlayerAngle());
                attackingFoePos.add(foe.getPos().add(foeToBallVec.scale(foeBlockOffset)));
            }

            new Swarm(guardFoeFielders, config).groupTo(attackingFoePos, attackingFoeAng, ball.getPos());

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
            new Swarm(guardGoalFielders, config).lineUp(guardGoalFielders, defenseVec, GUARD_GOAL_GAP, holderShootLineMidPoint);
            return true;
        } else {
            ArrayList<Vec2D> attackingFoePos = new ArrayList<>();
            ArrayList<Double> attackingFoeAng = new ArrayList<>();

            for (Foe foe : foes) {
                if (foe.getID() == RobotList.getFoeKeeperID())
                    continue;

                Vec2D foePos = foe.getPos();
                Vec2D ballPos = ball.getPos();
                Vec2D foeToBallVec = ballPos.sub(foePos).normalized();
                attackingFoeAng.add(foeToBallVec.toPlayerAngle());
                attackingFoePos.add(foe.getPos().add(foeToBallVec.scale(foeBlockOffset)));
            }

            new Swarm(fielders, config).groupTo(attackingFoePos, attackingFoeAng, ball.getPos());
            return true;
        }
    }
}
