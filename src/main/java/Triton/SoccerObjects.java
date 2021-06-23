package Triton;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotFactory;
import Triton.CoreModules.Robot.RobotList;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static Triton.Util.delay;

public class SoccerObjects {
    public RobotList<Ally> allFielders;
    public RobotList<Ally> fielders;
    public Ally keeper;
    public RobotList<Foe> foes;
    public Ball ball;
    private final Config config;
    private ArrayList<ScheduledFuture<?>> futures;

    public SoccerObjects(Config config) {
        this.config = config;
        ball = new Ball();
        ball.subscribe();
        // instantiate robots
        fielders = RobotFactory.createAllyFielderBots(config);
        keeper = RobotFactory.createGoalKeeperBot(config);
        foes = RobotFactory.createFoeBotsForTracking(config);
        futures = new ArrayList<>();
    }

    public ArrayList<ScheduledFuture<?>> runModules() {

        ArrayList<ScheduledFuture<?>> allyFieldersFutures = null;
        ArrayList<ScheduledFuture<?>> foesFutures = null;
        ScheduledFuture<?> goalKeeperFuture = null;
        // our/ally robots == fielders + 1 goalkeeper
        if (fielders.connectAll() == config.numAllyRobots - 1 && keeper.connect()) {
            allyFieldersFutures = fielders.runAll();
            goalKeeperFuture = App.threadPool.scheduleAtFixedRate(
                    keeper,
                0, Util.toPeriod(GvcModuleFreqs.ROBOT_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        } else {
            System.out.println("Error Connecting to Robots (in App.java)");
        }
        // opponent/foe robots: foes = foeFielders + 1 foeGoalKeeper
        foesFutures = foes.runAll(); // submit all to threadPool

        if(allyFieldersFutures != null) {
            futures.addAll(allyFieldersFutures);
        }
        if(foesFutures != null) {
            futures.addAll(foesFutures);
        }
        if(goalKeeperFuture != null) {
            futures.add(goalKeeperFuture);
        }
        return futures;
    }
}
