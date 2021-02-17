package Triton.CoreModules.AI.Estimators;


import Triton.Config.AIConfig;
import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.PassState;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Matrix.Vec2D;


/* provide misc estimations methods to give AI
 * situation awareness of everything happening
 * on the game field
 * */
public class BasicEstimator {
    private final RobotList<Ally> fielders;
    private final RobotList<Foe> foes;
    private final Ball ball;
    private final Ally keeper;

    public BasicEstimator(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        this.foes = foes;
        this.ball = ball;
        this.keeper = keeper;
    }

    /*
     * under the premises a robot is holding the ball,
     * return the trajectory of its currently aiming
     */
    public Vec2D getAimTrajectory() {
        Robot bot = getBallHolder();
        if (bot == null) {
            return ball.getVel().normalized();
        }

        return new Vec2D(bot.getDir());
    }

    /*
     * return the reference of the robot currently holding/dribbling the ball,
     * if no robot is currently holding the ball, return null
     */
    public Robot getBallHolder() {
        Vec2D ballPos = ball.getPos();

        RobotList<Robot> bots = new RobotList<Robot>();
        bots.addAll(fielders);
        bots.addAll(foes);
        bots.add(keeper);

        for (Robot bot : bots) {
            Vec2D botPos = bot.getPos();
            double botAngle = bot.getDir();
            double dist = Vec2D.dist(ballPos, botPos);
            double ballFaceAngle = ballPos.sub(botPos).toPlayerAngle();
            double angleDiff = PerspectiveConverter.calcAngDiff(ballFaceAngle, botAngle);
            double absAngleDiff = Math.abs(angleDiff);

            if (dist <= AIConfig.BALL_HOLD_DIST_THRESH && absAngleDiff <= AIConfig.BALL_HOLD_ANGLE_THRESH) {
                return bot;
            }
        }

        return null;
    }

    /* Ball under our control means more than just holding the ball,
     * which includes the temporarily releasing the ball during pass,
     * */
    public boolean isBallUnderOurCtrl() {
        boolean rtn = false;

        Robot holder = getBallHolder();
        if (holder instanceof Ally) {
            rtn = true;
        }

        // Use getPassStates() in CoordinatedPass.java to deal with passing situation
        if (CoordinatedPass.getPassState() != PassState.FAILED
            && CoordinatedPass.getPassState() != PassState.PENDING) {
            // during some valid pass states while ball is traveling, no body holds the ball
            rtn = true;
        }

        return rtn;
    }

    /* if no one holds the ball, return true if t_ally_nearest_robot(ball_loc) < t_foe_nearest_robot(ball_loc)
     * if an opponent holds the ball, return false */
    public boolean isBallWithinOurReach() {
        // To-do
        return false;
    }


    public Ally getNearestFielderToBall() {
        Ally nearestFielder = null;
        for (Ally fielder : fielders) {
            if (nearestFielder == null ||
                    fielder.getPos().sub(ball.getPos()).mag() < nearestFielder.getPos().sub(ball.getPos()).mag()) {
                nearestFielder = fielder;
            }
        }
        return nearestFielder;
    }

    public Foe getNearestFoeToBall() {
        Foe nearestFoe = null;
        for (Foe foe : foes) {
            if (nearestFoe == null ||
                    foe.getPos().sub(ball.getPos()).mag() < nearestFoe.getPos().sub(ball.getPos()).mag()) {
                nearestFoe = foe;
            }
        }
        return nearestFoe;
    }


    public Robot getNearestBotToBall() {
        Robot nearestBot = null;
        for (Ally fielder : fielders) {
            if (nearestBot == null ||
                    fielder.getPos().sub(ball.getPos()).mag() < nearestBot.getPos().sub(ball.getPos()).mag()) {
                nearestBot = fielder;
            }
        }
        for (Foe foe : foes) {
            if (nearestBot == null ||
                    foe.getPos().sub(ball.getPos()).mag() < nearestBot.getPos().sub(ball.getPos()).mag()) {
                nearestBot = foe;
            }
        }
        return nearestBot;
    }


    public Robot getNearestBotToLine(Line2D line) {
        Robot nearestBot = null;
        for (Ally fielder : fielders) {
            if (nearestBot == null ||
                    fielder.getPos().distToLine(line) < nearestBot.getPos().distToLine(line)) {
                nearestBot = fielder;
            }
        }
        for (Foe foe : foes) {
            if (nearestBot == null ||
                    foe.getPos().distToLine(line) < nearestBot.getPos().distToLine(line)) {
                nearestBot = foe;
            }
        }
        return nearestBot;
    }

}
