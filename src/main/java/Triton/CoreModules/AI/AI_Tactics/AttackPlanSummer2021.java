package Triton.CoreModules.AI.AI_Tactics;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Skills.Dodging;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.AI.TritonProbDijkstra.TritonDijkstra;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;

import java.util.ArrayList;

public class AttackPlanSummer2021 extends Tactics {

    private static final double SHOOT_THRESHOLD = 0.6;
    private static final double PASS_THRESHOLD = 0.45;

    protected Ally passer, receiver;
    protected Robot holder;
    protected final BasicEstimator basicEstimator;
    private PassInfo passInfo;
    private GapFinder gapFinder;
    private PassFinder passFinder;
    private Dodging dodging;
    final private double interAllyClearance = 600; // mm
    private Config config;

    private PUAG graph;
    private Robot ballHolder;
    private RobotList<Ally> restFielders;
    private RobotList<Ally> attackers; // Note: excluding the ball holder
    private RobotList<Ally> decoys;
    private TritonDijkstra.AttackPathInfo tdksOutput;
    private final double toPassThreshold = 0.5;

    private long SDB_t0;
    private long SDB_delay = 1000; // ms





    public AttackPlanSummer2021(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes,
                                Ball ball, GapFinder gapFinder, PassFinder passFinder, Config config) {
        super(fielders, keeper, foes, ball);
        this.config = config;

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passInfo = new PassInfo(fielders, foes, ball);

        this.gapFinder = gapFinder;
        this.passFinder = passFinder;
        this.dodging = new Dodging(fielders, foes, ball, basicEstimator);
    }

    public static enum PassShootResult {
        success,
        fail,
        goalShot
    }

    public static enum States {
        Start, // Construct PGUG
        Dijkstra, // Run Dijkstra
        Preparation, // Grouping ally bots & run background task
        SDB, // Standby/Dodging/BackPass
        ExecutePassPath, //Holder Pass & Rest go to ReceptionPoints
        Exit
    }

    private States currState = States.Dijkstra;

    @Override
    public boolean exec() {

        switch (currState) {
            case Exit -> {
                currState = States.Start;
                return false;
            }
            case Start -> {
                ballHolder = basicEstimator.getBallHolder();
                if (!(ballHolder instanceof Ally)) {
                    currState = States.Exit;
                } else {
                    RobotList<Ally> restFielders = fielders.copy();
                    restFielders.remove((Ally)holder);
                    ArrayList<PUAG.Node> middleNodes = new ArrayList<>();
                    for(Ally bot : restFielders) {
                        middleNodes.add(new PUAG.AllyNode(bot));
                    }
                    graph = new PUAG(new PUAG.AllyHolderNode((Ally) ballHolder),
                                     new PUAG.GoalNode(),
                                     middleNodes);
                    currState = States.Dijkstra;
                }
            }
            case Dijkstra -> {
                tdksOutput = (new TritonDijkstra(graph).compute());
                currState = States.Preparation;
            }
            case Preparation -> {
                if(!basicEstimator.isAllyHavingTheBall()) {
                    currState = States.Exit;
                } else {
                    attackers = new RobotList<>();
                    decoys = new RobotList<>();
                    for (PUAG.Node node : tdksOutput.getMaxProbPath()) {
                        if (node instanceof PUAG.AllyNode && !(node instanceof PUAG.AllyHolderNode)) {
                            attackers.add(((PUAG.AllyNode) node).bot);
                        }
                    }
                    decoys = fielders.copy();
                    decoys.remove((Ally) holder);
                    for (Ally attacker : attackers) {
                        decoys.remove(attacker);
                    }
                    runDecoyBackGndTasks();
                    if(tdksOutput.getTotalProbabilityProduct() > toPassThreshold) {
                        currState = States.ExecutePassPath;
                    } else {
                        currState = States.SDB;
                        SDB_t0 = System.currentTimeMillis();
                    }
                }
            }
            case SDB -> { // SDB: Standby/Dodging/BackPass
                if(System.currentTimeMillis() > SDB_delay) {
                    currState = States.Start;
                }
                /* Execute SDB */
                // To-do
            }
            case ExecutePassPath -> {
                PassShootResult result = PassShootResult.fail;
                /* Execute Pass/Shoot */
                // To-do
                // Don't forget to assign passResult
                switch (result) {
                    case success -> currState = States.Start;
                    case fail, goalShot -> currState = States.Exit;
                }
            }
        }

        return true;
    }

    private void runDecoyBackGndTasks() {

    }
}
