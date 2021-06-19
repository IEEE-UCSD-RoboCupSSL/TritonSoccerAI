package Triton.CoreModules.AI.AI_Tactics;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.Dodging;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.AI.ReceptionPoint;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.AI.TritonProbDijkstra.TritonDijkstra;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
    private ArrayList<Ally> attackers; // order matters, so using an arraylist
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
        while(currState != States.Exit) {
            switch (currState) {
                case Start -> {
                    ballHolder = basicEstimator.getBallHolder();
                    if (!(ballHolder instanceof Ally)) {
                        currState = States.Exit;
                    } else {
                        RobotList<Ally> restFielders = fielders.copy();
                        restFielders.remove((Ally) holder);
                        ArrayList<PUAG.Node> middleNodes = new ArrayList<>();
                        for (Ally bot : restFielders) {
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
                            if (node instanceof PUAG.AllyNode) {
                                attackers.add(((PUAG.AllyNode) node).getBot());
                            }
                        }
                        decoys = fielders.copy();
                        for (Ally attacker : attackers) {
                            decoys.remove(attacker);
                        }
                        runDecoyBackGndTasks();
                        if (tdksOutput.getTotalProbabilityProduct() > toPassThreshold) {
                            currState = States.ExecutePassPath;
                        } else {
                            currState = States.SDB;
                            SDB_t0 = System.currentTimeMillis();
                        }
                    }
                }
                case SDB -> { // SDB: Standby/Dodging/BackPass
                    if (System.currentTimeMillis() > SDB_delay) {
                        currState = States.Start;
                    }
                    /* Execute SDB */
                    // To-do
                }
                case ExecutePassPath -> {

                    ArrayList<ReceptionPoint> receptionPoints = tdksOutput.getReceptionPoints();
                    if(tdksOutput.getMaxProbPath().get(1) instanceof PUAG.GoalNode) {
                        /* Shoot Goal */
                    } else {
                        /* Pass to Next */
                        CoordinatedPass.PassShootResult passResult = CoordinatedPass.PassShootResult.Executing;

                        /* The first reception point is treated as the pass point the passer needs to go to */
                        CoordinatedPass cp = new CoordinatedPass(attackers.get(0), attackers.get(1),
                                                                    receptionPoints.get(0), receptionPoints.get(1), ball);
                        try {
                            while (passResult == CoordinatedPass.PassShootResult.Executing) {
                                passResult = cp.execute();
                                for (int i = 2; i < receptionPoints.size(); i++) {
                                    ReceptionPoint rp = receptionPoints.get(i);
                                    attackers.get(i).curveTo(rp.getPoint(), rp.getAngle());
                                }
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        switch (passResult) {
                            case success -> currState = States.Start;
                            case fail -> currState = States.Exit;
                        }
                    }
                }
            }
        }

        /* Exit State */
        currState = States.Start;
        return false;
    }

    private void runDecoyBackGndTasks() {

    }
}
