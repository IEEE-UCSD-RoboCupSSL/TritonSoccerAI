package Triton.CoreModules.AI.AI_Skills;


import Triton.Config.ObjectConfig;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;

import static Triton.Config.ObjectConfig.MAX_KICK_VEL;

public class Dodging extends Skills {

    protected final RobotList<Ally> fielders;
    protected final RobotList<Foe> foes;
    protected final Ball ball;

    private double clearance = 360; // mm

    public Dodging(RobotList<Ally> fielders, RobotList<Foe> foes,
                   Ball ball) {
        this.fielders = fielders;
        this.foes = foes;
        this.ball = ball;
    }


    public boolean dodge(Ally holder, Vec2D holdBallStartPos) {
        if(!holder.isHoldingBall()) {
            return false;
        }
        Vec2D holderPos = holder.getPos();

        if(holderPos.sub(holdBallStartPos).mag() < clearance) {





        } else {
            holder.stop();
            holder.kick(new Vec2D(0.1, ObjectConfig.MAX_KICK_VEL));
        }
        return false;
    }

    public boolean dodgeTo(Ally holder, Vec2D pos) {
        return false;
    }

}
