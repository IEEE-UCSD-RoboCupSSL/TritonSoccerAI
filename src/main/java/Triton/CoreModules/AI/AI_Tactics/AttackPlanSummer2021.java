package Triton.CoreModules.AI.AI_Tactics;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.Dodging;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.AttackSupportMapModule;
import Triton.CoreModules.AI.Estimators.PassProbMapModule;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.AI.TritonProbDijkstra.Computables.DijkCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.InvalidDijkstraGraphException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NoDijkComputeInjectionException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.UnknownPuagNodeException;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.AI.TritonProbDijkstra.TritonDijkstra;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static Triton.Util.delay;

public class AttackPlanSummer2021 extends Tactics {

    private static final double SHOOT_THRESHOLD = 0.6;
    private static final double PASS_THRESHOLD = 0.45;

    protected Ally passer, receiver;
    protected Robot holder;
    protected final BasicEstimator basicEstimator;
    private PassInfo passInfo;
    private AttackSupportMapModule atkSupportMap;
    private PassProbMapModule passProbMap;
    private Dodging dodging;
    final private double interAllyClearance = 600; // mm
    private Config config;

    private PUAG graph;
    private Robot ballHolder;
    private RobotList<Ally> restFielders;
    private ArrayList<PUAG.Node> attackerNodes; // order matters, so using an arraylist
    private RobotList<Ally> decoys;
    private TritonDijkstra.AttackPathInfo tdksOutput;
    private DijkCompute dijkCompute;
    private final double toPassThreshold = 0.5;

    private long SDB_t0;
    private long SDB_delay = 1000; // ms

    public AttackPlanSummer2021(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes,
                                Ball ball, AttackSupportMapModule atkSupportMap, PassProbMapModule passProbMap, Config config) {
        super(fielders, keeper, foes, ball);
        this.config = config;

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passInfo = new PassInfo();

        this.atkSupportMap = atkSupportMap;
        this.passProbMap = passProbMap;
        this.dodging = new Dodging(fielders, foes, ball, basicEstimator);
    }

    public enum States {
        Start, // Construct PGUG
        Dijkstra, // Run Dijkstra
        Preparation, // Grouping ally bots & run background task
        SDB, // Standby/Dodging/BackPass
        ExecutePassPath, //Holder Pass & Rest go to ReceptionPoints
        Exit
    }

    private States currState = States.Dijkstra;

    public void setDijkCompute(DijkCompute dijkCompute){
        this.dijkCompute = dijkCompute;
    }

    @Override
    public boolean exec() {
        while(currState != States.Exit) {
            delay(2);
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
                            middleNodes.add(new PUAG.AllyRecepNode(bot));
                        }
                        graph = new PUAG(new PUAG.AllyPassNode((Ally) ballHolder),
                                         new PUAG.GoalNode(),
                                         middleNodes);
                        currState = States.Dijkstra;
                    }
                }
                case Dijkstra -> {
                    tdksOutput = (new TritonDijkstra(graph, dijkCompute, fielders, foes, ball).compute());
                    currState = States.Preparation;
                }
                case Preparation -> {
                    if(!basicEstimator.isAllyHavingTheBall()) {
                        currState = States.Exit;
                    } else {
                        decoys = new RobotList<>();
                        attackerNodes = tdksOutput.getMaxProbPath();
                        decoys = fielders.copy();
                        for (PUAG.Node attackerNode : attackerNodes) {
                            if(attackerNode instanceof PUAG.AllyNode) {
                                decoys.remove(((PUAG.AllyNode) attackerNode).getBot());
                            }
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

                    if(attackerNodes.get(1) instanceof PUAG.GoalNode) {
                        /* Shoot Goal */
                    } else {
                        if(attackerNodes.get(0) instanceof PUAG.AllyPassNode
                                && attackerNodes.get(1) instanceof PUAG.AllyRecepNode) {

                            /* Pass to Next */
                            CoordinatedPass.PassShootResult passResult = CoordinatedPass.PassShootResult.Executing;
                            CoordinatedPass cp = new CoordinatedPass((PUAG.AllyPassNode) attackerNodes.get(0),
                                    (PUAG.AllyRecepNode) attackerNodes.get(1), ball, basicEstimator);
                            try {
                                while (passResult == CoordinatedPass.PassShootResult.Executing) {
                                    passResult = cp.execute();
                                    for (int i = 2; i < attackerNodes.size(); i++) {
                                        if(attackerNodes.get(i) instanceof PUAG.AllyRecepNode) {
                                            PUAG.AllyRecepNode recepNode = ((PUAG.AllyRecepNode) attackerNodes.get(i));
                                            recepNode.getBot().curveTo(recepNode.getReceptionPoint(), recepNode.getAngle());
                                        }
                                    }
                                    delay(3);
                                }
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            switch (passResult) {
                                case success -> currState = States.Start;
                                case fail -> currState = States.Exit;
                            }
                        } else {
                            System.out.println("\\033[31mError in AttackPlanSummer2021\\033[0m");
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
