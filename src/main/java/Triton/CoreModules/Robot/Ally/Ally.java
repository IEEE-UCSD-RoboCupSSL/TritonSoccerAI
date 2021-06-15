package Triton.CoreModules.Robot.Ally;

import Proto.RemoteAPI;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.Config.OldConfigs.ObjectConfig;
import Triton.CoreModules.AI.PathFinder.JumpPointSearch.JPSPathFinder;
import Triton.CoreModules.AI.PathFinder.PathFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.KeeperSkills.Keep;
import Triton.CoreModules.Robot.MotionMode;
import Triton.CoreModules.Robot.MotionState;
import Triton.CoreModules.Robot.ProceduralSkills.Dependency.AsyncProcedure;
import Triton.CoreModules.Robot.ProceduralSkills.Dependency.ProceduralTask;
import Triton.CoreModules.Robot.Ally.AdvancedSkills.*;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotSockets.RobotConnection;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.*;
import Triton.PeriphModules.Detection.BallData;
import Triton.PeriphModules.Detection.RobotData;
import Triton.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.FIELD_LENGTH;
import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.FIELD_WIDTH;
import static Triton.Config.OldConfigs.ObjectConfig.*;
import static Triton.CoreModules.Robot.MotionMode.*;
import static Triton.CoreModules.Robot.MotionState.*;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.calcAngDiff;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

/* TL;DR, Instead, read RobotSkills Interface for a cleaner view !!!!!!! */
public class Ally extends Robot implements AllySkills {
    /*** internal pub sub ***/
    private final Publisher<MotionState> statePub;
    private final RobotConnection conn;
    /*** external pub sub ***/
    private final ArrayList<Subscriber<RobotData>> yellowRobotSubs;
    private final ArrayList<Subscriber<RobotData>> blueRobotSubs;
    private final Subscriber<BallData> ballSub;
    private final Subscriber<Boolean> isDribbledSub;
    private final Publisher<RemoteAPI.CommandData> commandsPub;
    private final Subscriber<MotionState> stateSub;
    private final Publisher<Vec2D> pointPub, kickVelPub, holdBallPosPub;
    private final Subscriber<Vec2D> pointSub, kickVelSub, holdBallPosSub;
    private final Publisher<Double> angPub;
    private final Subscriber<Double> angSub;

    protected ExecutorService threadPool;
    private PathFinder pathFinder;

    private boolean prevHoldBallStatus = false;
    private boolean isFirstRun = true;

    private AsyncProcedure asyncProcedure = null;
    private Config config;

    public Ally(Config config, int id) {
        super(config.myTeam, id);
        this.config = config;
        this.threadPool = App.threadPool;

        asyncProcedure = new AsyncProcedure(this.threadPool);

        conn = new RobotConnection(config, id);

        blueRobotSubs = new ArrayList<>();
        yellowRobotSubs = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            blueRobotSubs.add(new FieldSubscriber<>("From:DetectionModule", Team.BLUE.name() + i));
            yellowRobotSubs.add(new FieldSubscriber<>("From:DetectionModule", Team.YELLOW.name() + i));
        }
        ballSub = new FieldSubscriber<>("From:DetectionModule", "Ball");

        statePub = new FieldPublisher<>("From:Ally", "State " + id, MOVE_TVRV);
        pointPub = new FieldPublisher<>("From:Ally", "Point " + id, new Vec2D(0, 0));
        angPub = new FieldPublisher<>("From:Ally", "Ang " + id, 0.0);
        kickVelPub = new FieldPublisher<>("From:Ally", "KickVel " + id, new Vec2D(0, 0));
        holdBallPosPub = new FieldPublisher<>("From:Ally", "HoldBallPos " + id, null);

        stateSub = new FieldSubscriber<>("From:Ally", "State " + id);
        pointSub = new FieldSubscriber<>("From:Ally", "Point " + id);
        angSub = new FieldSubscriber<>("From:Ally", "Ang " + id);
        kickVelSub = new FieldSubscriber<>("From:Ally", "KickVel " + id);
        holdBallPosSub = new FieldSubscriber<>("From:Ally", "HoldBallPos " + id);

        isDribbledSub = new FieldSubscriber<>("From:RobotTCPConnection", "Drib " + id);
        commandsPub = new MQPublisher<>("From:Ally", "Commands " + id);

        conn.buildTcpConnection();
        conn.buildUDPStream();
    }

    public boolean connect() {
        boolean rtn = false;
        try {
            rtn = conn.getTCPConnection().connect();
        } catch (IOException e) {
            // System.out.printf("Robot %d TCP connection fails: %s\n", super.ID, e.getClass());
            e.printStackTrace();
        }
        return rtn;
    }

    public void reinit() {
        // To-do for physical robots
    }


    /*** Allow robot to run a parallel procedural task asynchronously ***/
    // To-do: lock all exe method when in procedural mode
    synchronized public void executeProceduralTask(ProceduralTask task) {
        asyncProcedure.execute(this, task);
    }

    synchronized public void resetProceduralTask() {
        asyncProcedure.reset();
    }

    synchronized public boolean isProcedureCompleted() {
        return asyncProcedure.isCompleted();
    }

    synchronized public boolean getProcedureReturnStatus() throws ExecutionException, InterruptedException {
        return asyncProcedure.getCompletionReturn();
    }

    synchronized public void cancelProceduralTask() {
        asyncProcedure.cancel();
    }

    synchronized public boolean isProcedureCancelled() {
        return asyncProcedure.isCancelled();
    }

    /*** primitive control methods ***/
    @Override
    synchronized public void autoCap() {
        statePub.publish(AUTO_CAPTURE);
    }

    @Override
    synchronized public void stop() {
        moveAt(new Vec2D(0, 0));
        spinAt(0);
    }

    /**
     * @param loc player perspective, millimeter
     */
    @Override
    synchronized public void moveTo(Vec2D loc) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD, MOVE_NSTDRD -> statePub.publish(MOVE_TDRD);
            default -> statePub.publish(MOVE_TDRV);
        }
        pointPub.publish(loc);
    }

    /**
     * @param vel player perspective, vector with unit as percentage from -100 to 100
     */
    @Override
    synchronized public void moveAt(Vec2D vel) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD, MOVE_NSTDRD -> statePub.publish(MOVE_TVRD);
            default -> statePub.publish(MOVE_TVRV);
        }
        pointPub.publish(vel);
    }

    /**
     * @param angle player perspective, degrees, starting from y-axis, positive is counter clockwise
     */
    @Override
    synchronized public void spinTo(double angle) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TDRV, MOVE_NSTDRD, MOVE_NSTDRV -> statePub.publish(MOVE_TDRD);
            default -> statePub.publish(MOVE_TVRD);
        }
        angPub.publish(angle);
    }

    /**
     * @param angVel unit is percentage from -100 to 100, positive is counter clockwise
     */
    @Override
    synchronized public void spinAt(double angVel) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TDRV, MOVE_NSTDRD, MOVE_NSTDRV -> statePub.publish(MOVE_TDRV);
            default -> statePub.publish(MOVE_TVRV);
        }
        angPub.publish(angVel);
    }

    // runs in the caller thread
    @Override
    synchronized public void kick(Vec2D kickVel) {
        double mag = kickVel.mag();
        if (mag >= MAX_KICK_VEL) {
            kickVel = kickVel.normalized().scale(MAX_KICK_VEL);
        }

        kickVelPub.publish(kickVel);

        threadPool.submit(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            kickVelPub.publish(new Vec2D(0, 0));
        });
    }

    /*** advanced control methods with path avoiding obstacles ***/

    // Note: (moveTo/At & spinTo/At] are mutually exclusive to advanced control methods
    @Override
    synchronized public void rotateTo(double angle) {
        RotateTo.exec(this, angle);
    }

    @Override
    synchronized public void strafeTo(Vec2D endPoint) {
        StrafeTo.exec(this, endPoint, getDir());
    }

    @Override
    synchronized public void strafeTo(Vec2D endPoint, double angle) {
        StrafeTo.exec(this, endPoint, angle);
    }

    @Override
    public void curveTo(Vec2D endPoint) {
        CurveTo.exec(this, endPoint, getDir());
    }

    @Override
    synchronized public void curveTo(Vec2D endPoint, double angle) {
        CurveTo.exec(this, endPoint, angle);
    }

    @Override
    synchronized public void fastCurveTo(Vec2D endPoint) {
        FastCurveTo.exec(this, endPoint, getDir());
    }

    @Override
    synchronized public void fastCurveTo(Vec2D endPoint, double endAngle) {
        FastCurveTo.exec(this, endPoint, endAngle);
    }

    @Override
    synchronized public void sprintFrontTo(Vec2D endPoint) {
        SprintFrontTo.exec(this, endPoint, getDir());
    }

    @Override
    synchronized public void sprintFrontTo(Vec2D endPoint, double endAngle) {
        SprintFrontTo.exec(this, endPoint, endAngle);
    }

    @Override
    synchronized public void sprintTo(Vec2D endPoint) {
        SprintTo.exec(this, endPoint);
    }

    @Override
    synchronized public void sprintTo(Vec2D endPoint, double endAngle) {
        SprintTo.exec(this, endPoint, endAngle);
    }

    /*** Soccer Skills methods ***/
    @Override
    synchronized public void getBall(Ball ball) {
        GetBall.exec(this, ball);
    }

    @Override
    synchronized public boolean dribRotate(Ball ball, double angle) {
        return DribRotate.exec(this, ball, angle);
    }

    @Override
    synchronized public boolean dribRotate(Ball ball, double angle, double offsetDist) {
        return DribRotate.exec(this, ball, angle, offsetDist);
    }

    @Override
    synchronized public void staticIntercept(Ball ball, Vec2D anchorPos) {
        StaticIntercept.exec(this, ball, anchorPos);
    }

    @Override
    synchronized public void dynamicIntercept(Ball ball, double faceDir) {
        DynamicIntercept.exec(this, ball, faceDir);
    }

    @Override
    synchronized public void keep(Ball ball, Vec2D aimTraj) {
        Keep.exec(this, ball, aimTraj);
    }

    @Override
    synchronized public void receive(Ball ball, Vec2D receivePos) {
        // To-do: devise new implementation or choose the better between the two intercept impl
        staticIntercept(ball, receivePos);
    }

    @Override
    synchronized public boolean isHoldingBall() {
        if (!isDribbledSub.isSubscribed()) {
            return false;
        }
        return isDribbledSub.getMsg();
    }

    @Override
    synchronized public Vec2D HoldBallPos() {
        return holdBallPosSub.getMsg();
    }


    @Override
    synchronized public boolean isPosArrived(Vec2D pos) {
        return isPosArrived(pos, POS_PRECISION);
    }

    @Override
    synchronized public boolean isPosArrived(Vec2D pos, double dist) {
        return pos.sub(getPos()).mag() < dist;
    }

    @Override
    synchronized public boolean isDirAimed(double angle) {
        return isDirAimed(angle, DIR_PRECISION);
    }

    @Override
    synchronized public boolean isDirAimed(double angle, double angleDiff) {
        return Math.abs(calcAngDiff(angle, getDir())) < angleDiff;
    }

    /**
     * Update the set path of the robot
     */
    public ArrayList<Vec2D> findPath(Vec2D endPoint) {
        pathFinder.setObstacles(getObstacles());
        return pathFinder.findPath(getPos(), endPoint);
    }

    /**
     * Returns an ArrayList of circles representing the obstacles for pathfinding
     *
     * @return an ArrayList of circles representing the obstacles for pathfinding
     */
    private ArrayList<Circle2D> getObstacles() {
        ArrayList<RobotData> blueRobots = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            blueRobots.add(blueRobotSubs.get(i).getMsg());
        }

        ArrayList<RobotData> yellowRobots = new ArrayList<>();
        for (int i = 0; i < config.numAllyRobots; i++) {
            yellowRobots.add(yellowRobotSubs.get(i).getMsg());
        }

        ArrayList<Circle2D> obstacles = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (team == Team.YELLOW && ID == i)
                continue;
            RobotData robot = yellowRobots.get(i);
            obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
        }
        for (int i = 0; i < 6; i++) {
            if (team == Team.BLUE && ID == i)
                continue;
            RobotData robot = blueRobots.get(i);
            obstacles.add(new Circle2D(robot.getPos(), ObjectConfig.ROBOT_RADIUS));
        }

        return obstacles;
    }

    public void moveToNoSlowDown(Vec2D loc) {
        switch (stateSub.getMsg()) {
            case MOVE_TDRD, MOVE_TVRD, MOVE_NSTDRD -> statePub.publish(MOVE_NSTDRD);
            default -> statePub.publish(MOVE_NSTDRV);
        }
        pointPub.publish(loc);
    }

    // Everything in run() runs in the Ally Thread
    @Override
    public void run() {
        if (isFirstRun) {
            try {
                subscribe();
                super.run();
                pathFinder = new JPSPathFinder(FIELD_WIDTH, FIELD_LENGTH, config);

                ScheduledFuture<?> sendTCPFuture = App.threadPool.scheduleAtFixedRate(conn.getTCPConnection().getSendTCP(),
                        0,
                        Util.toPeriod(GvcModuleFreqs.TCP_CONNECTION_SEND_FREQ, TimeUnit.NANOSECONDS),
                        TimeUnit.NANOSECONDS);

                ScheduledFuture<?> receiveTCPFuture = App.threadPool.scheduleAtFixedRate(conn.getTCPConnection().getReceiveTCP(),
                        0,
                        Util.toPeriod(GvcModuleFreqs.TCP_CONNECTION_RECEIVE_FREQ, TimeUnit.NANOSECONDS),
                        TimeUnit.NANOSECONDS);

                ScheduledFuture<?> UDPFuture = App.threadPool.scheduleAtFixedRate(conn.getUDPStream(),
                        0,
                        Util.toPeriod(GvcModuleFreqs.UDP_STREAM_SEND_FREQ, TimeUnit.NANOSECONDS),
                        TimeUnit.NANOSECONDS);


                conn.getTCPConnection().sendInit();
            } catch (Exception e) {
                e.printStackTrace();
            }

            isFirstRun = false;
        }

        RemoteAPI.CommandData command;
        MotionState state = stateSub.getMsg();

        switch (state) {
            case MOVE_TDRD -> command = createTDRDCmd();
            case MOVE_TDRV -> command = createTDRVCmd();
            case MOVE_TVRD -> command = createTVRDCmd();
            case MOVE_TVRV -> command = createTVRVCmd();
            case MOVE_NSTDRD -> command = createNSTDRDCmd();
            case MOVE_NSTDRV -> command = createNSTDRVCmd();
            case AUTO_CAPTURE -> command = createAutoCapCmd();
            default -> {
                command = createTVRVCmd();
            }
        }
        commandsPub.publish(command);

        boolean isHolding = isHoldingBall();
        if (isHolding != prevHoldBallStatus) {
            if (isHolding) {
                holdBallPosPub.publish(getPos());
            } else {
                holdBallPosPub.publish(null);
            }
        }
        prevHoldBallStatus = isHoldingBall();
    }

    @Override
    protected void subscribe() {
        super.subscribe();
        try {
            for (int i = 0; i < config.numAllyRobots; i++) {
                yellowRobotSubs.get(i).subscribe(1000);
                blueRobotSubs.get(i).subscribe(1000);
            }
            ballSub.subscribe(1000);

            isDribbledSub.subscribe(1000);
            stateSub.subscribe(1000);
            pointSub.subscribe(1000);
            angSub.subscribe(1000);
            kickVelSub.subscribe(1000);
            holdBallPosSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RemoteAPI.CommandData createTDRDCmd() {
        return createPrimitiveCmdBuilder(TDRD);
    }

    /*** TL;DR ***/

    private RemoteAPI.CommandData createPrimitiveCmdBuilder(MotionMode mode) {
        RemoteAPI.CommandData.Builder command = RemoteAPI.CommandData.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(false);
        command.setMode(mode.ordinal());

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        Vec2D point = pointSub.getMsg();
        motionSetPoint.setX(point.x);
        motionSetPoint.setY(point.y);
        double angle = normAng(angSub.getMsg());
        motionSetPoint.setZ(angle);
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }

    private RemoteAPI.CommandData createTDRVCmd() {
        return createPrimitiveCmdBuilder(TDRV);
    }

    private RemoteAPI.CommandData createTVRDCmd() {
        return createPrimitiveCmdBuilder(TVRD);
    }

    private RemoteAPI.CommandData createTVRVCmd() {
        return createPrimitiveCmdBuilder(TVRV);
    }

    private RemoteAPI.CommandData createNSTDRDCmd() {
        return createPrimitiveCmdBuilder(NSTDRD);
    }

    private RemoteAPI.CommandData createNSTDRVCmd() {
        return createPrimitiveCmdBuilder(NSTDRV);
    }

    private RemoteAPI.CommandData createAutoCapCmd() {
        RemoteAPI.CommandData.Builder command = RemoteAPI.CommandData.newBuilder();
        command.setIsWorldFrame(true);
        command.setEnableBallAutoCapture(true);
        command.setMode(TVRV.ordinal());

        RemoteAPI.Vec3D.Builder motionSetPoint = RemoteAPI.Vec3D.newBuilder();
        motionSetPoint.setX(0);
        motionSetPoint.setY(0);
        motionSetPoint.setZ(0);
        command.setMotionSetPoint(motionSetPoint);

        RemoteAPI.Vec2D.Builder kickerSetPoint = RemoteAPI.Vec2D.newBuilder();
        Vec2D kickVel = kickVelSub.getMsg();
        kickerSetPoint.setX(kickVel.x);
        kickerSetPoint.setY(kickVel.y);
        command.setKickerSetPoint(kickerSetPoint);

        return command.build();
    }

    synchronized public void displayPathFinder() {
        if (pathFinder instanceof JPSPathFinder) {
            ((JPSPathFinder) pathFinder).display();
        }
    }
}