package triton.coreModules.ai.skills;


import triton.config.oldConfigs.ObjectConfig;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.misc.math.coordinates.PerspectiveConverter;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;

import static triton.misc.math.coordinates.PerspectiveConverter.normAng;

public class Dodging extends Skills {

    protected final RobotList<Ally> fielders;
    protected final RobotList<Foe> foes;
    protected final Ball ball;

    private double clearance = 500; // mm
    private double clearanceAngle = 90; // degree

    protected BasicEstimator basicEstimator;

    public Dodging(RobotList<Ally> fielders, RobotList<Foe> foes,
                   Ball ball, BasicEstimator basicEstimator) {
        this.fielders = fielders;
        this.foes = foes;
        this.ball = ball;
        this.basicEstimator = basicEstimator;
    }


    public boolean dodge(Ally holder, Vec2D holdBallStartPos) {
        if(!holder.isHoldingBall()) {
            return false;
        }
        Vec2D holderPos = holder.getPos();

//       if(holderPos.sub(holdBallStartPos).mag() < ObjectConfig.EXCESSIVE_DRIBBLING_DIST) {
            ArrayList<Vec2D> dangerFoePos = new ArrayList<>();
            for(Foe foe : foes) {
                Vec2D foePos = foe.getPos();
                if(foePos.sub(holderPos).mag() < clearance) {
                    dangerFoePos.add(foePos);
                }
            }
            if(dangerFoePos.size() <= 0) {
                holder.stop();
            } else {
                Vec2D nearestFoePos = basicEstimator.getNearestFoeToBall().getPos();
                Vec2D dodgingVec = holderPos.sub(nearestFoePos).normalized();
                Vec2D dodgingPos = nearestFoePos.add(dodgingVec.scale(clearance));

                double refHolderToFoe = 999; // 999 is an impossible value for angle, it has special usage here
                double holderDir = holder.getDir();
                double[] angleRange = new double[]{normAng(holderDir - clearanceAngle / 2),
                                                    normAng(holderDir + clearanceAngle / 2)};
                for(Vec2D dangerPos : dangerFoePos) {
                    double holderToFoeAngle = dangerPos.sub(holderPos).toPlayerAngle();
                    if(angleBetween(holderToFoeAngle, angleRange)) {
                        if(angDiff(holderToFoeAngle, holderDir) < angDiff(refHolderToFoe, holderDir)) {
                            refHolderToFoe = holderToFoeAngle;
                        }
                    }
                }

                double excessDribPercent = dodgingPos.sub(holdBallStartPos).mag() / ObjectConfig.EXCESSIVE_DRIBBLING_DIST;
                double clockwise = dodgingVec.toPlayerAngle() < 0 ? -1.0 : 1.0;
                dodgingPos = dodgingPos.add(dodgingVec.rotate(-90 * clockwise).scale(excessDribPercent * clearance * 2));
                dodgingPos = dodgingPos.sub(dodgingVec);
                if(dodgingPos.sub(holdBallStartPos).mag() > ObjectConfig.EXCESSIVE_DRIBBLING_DIST * 0.75) {
                    dodgingPos = dodgingPos.sub(holdBallStartPos).normalized().rotate(-90 * clockwise)
                            .scale(ObjectConfig.EXCESSIVE_DRIBBLING_DIST / 2);
                }

                double dodgingAngle;
                if(refHolderToFoe == 999) { // no need to rotate
                    dodgingAngle = holderDir;
                } else {
                    if (PerspectiveConverter.calcAngDiff(refHolderToFoe, holderDir) > 0) {
                        dodgingAngle = normAng(refHolderToFoe - (clearanceAngle / 2) );
                    } else {
                        dodgingAngle = normAng(refHolderToFoe + (clearanceAngle / 2));
                    }
                }

//                System.out.println(dodgingPos + " " + dodgingAngle);

                if(holder.isDirAimed(dodgingAngle)) {
                    holder.curveTo(dodgingPos);
                } else {
                    holder.dribRotate(ball, dodgingAngle);
                }
            }
//        } else {
//             holder.stop();
//             holder.kick(new Vec2D(0.1, ObjectConfig.MAX_KICK_VEL));
//        }
        return true;
    }

//    public boolean dodgeTo(Ally holder, Vec2D pos) {
//        return false;
//    }



    private static double angDiff(double a1, double a2) {
        return Math.abs(PerspectiveConverter.calcAngDiff(a1, a2));
    }
    private static boolean angleBetween(double angle, double[] angleRange) {
        double totalDiff = angDiff(angleRange[0], angleRange[1]);
        return angDiff(angle, angleRange[0]) < totalDiff && angDiff(angle, angleRange[1]) < totalDiff;
    }




}
