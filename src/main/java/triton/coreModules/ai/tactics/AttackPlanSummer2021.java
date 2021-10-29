package triton.coreModules.ai.tactics;

import triton.config.Config;
import triton.coreModules.ai.skills.CoordinatedPass;
import triton.coreModules.ai.skills.Dodging;
import triton.coreModules.ai.skills.Swarm;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.estimators.AttackSupportMapModule;
import triton.coreModules.ai.estimators.PassProbMapModule;
import triton.coreModules.ai.estimators.PassInfo;
import triton.coreModules.ai.dijkstra.computableImpl.Compute;
import triton.coreModules.ai.dijkstra.exceptions.*;
import triton.coreModules.ai.dijkstra.Pdg;
import triton.coreModules.ai.dijkstra.TritonDijkstra;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.Robot;
import triton.coreModules.robot.RobotList;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static triton.Util.delay;

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

    private Robot ballHolder;
    private RobotList<Ally> restFielders;
    private ArrayList<Pdg.Node> attackerNodes; // order matters, so using an arraylist
    private RobotList<Ally> decoys;
    private TritonDijkstra.AttackPathInfo tdksOutput;
    private Pdg graph;
    private final double toPassThreshold = 0.00;
    private Compute compute;

    private long SDB_t0;
    private long SDB_delay = 1000; // ms

    public States getCurrState() {
        return currState;
    }

    public void setCurrState(States currState){
        this.currState = currState;
    }

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

    private States currState = States.Start;


    @Override
    public boolean exec() {
        while(currState != States.Exit) {
            delay(2);
            switch (currState) {
                case Start -> {
                    System.out.println("[Attack2021] Entering state [Start]");
                    ballHolder = basicEstimator.getBallHolder();
                    if (!(ballHolder instanceof Ally)) {
                        currState = States.Exit;
                    } else {

////                        System.out.println("Before copy: " + fielders);
                        RobotList<Ally> restFielders = fielders.copy();
////                        System.out.println("Before remove: " + restFielders);

                        boolean remove = restFielders.remove((Ally) ballHolder);
////                        System.out.println("ballHolder is: " + ballHolder);
////                        System.out.println("After remove: " + restFielders);
                        ArrayList<Pdg.Node> middleNodes = new ArrayList<>();
                        for (Ally bot : restFielders) {
                            Pdg.AllyRecepNode recepNode = new Pdg.AllyRecepNode(bot);
                            middleNodes.add(recepNode);
////                            System.out.println("Adding " + recepNode.getNodeBotIdString());
                        }
                        try {
                            graph = new Pdg(new Pdg.AllyPassNode((Ally) ballHolder), new Pdg.GoalNode(), middleNodes);
                        } catch (NodesNotUniqueException e) {
                            e.printStackTrace();
                        }
                        currState = States.Dijkstra;
                    }
                }
                case Dijkstra -> {
                    System.out.println("[Attack2021] Entering state [Dijkstra]");

                    compute = new Compute(graph);
                    compute.setSnapShots(TritonDijkstra.buildFielderSnaps(fielders), TritonDijkstra.buildFoeSnaps(foes), TritonDijkstra.buildBallSnap(ball));
//                    MockCompute compute = new MockCompute(graph);
//                    compute.mock(fielders);

                    try {
                        tdksOutput = (new TritonDijkstra(graph, compute, fielders, foes, ball).compute());
                    } catch (GraphIOException | NoDijkComputeInjectionException e) {
                        e.printStackTrace();
                    }
                    currState = States.Preparation;
                }
                case Preparation -> {
                    System.out.println("[Attack2021] Entering state [Preparation]");
                    if(!basicEstimator.isAllyHavingTheBall()) {
                        currState = States.Exit;
                        return false;
                    } else {
                        decoys = new RobotList<>();
                        attackerNodes = tdksOutput.getMaxProbPath();
                        System.out.println("[Attack2021] returned optimal path: [" + tdksOutput.pathString() + "]");
                        decoys = fielders.copy();
                        for (Pdg.Node attackerNode : attackerNodes) {
                            if(attackerNode instanceof Pdg.AllyNode) {
                                decoys.remove(((Pdg.AllyNode) attackerNode).getBot());
                            }
                        }

                        ArrayList<Ally> decoysCopy = new ArrayList<>(decoys);
                        System.out.println("[Attack2021] decoy list: " + getDecoyListString(decoysCopy));
//                        runDecoyBackGndTasks(ball.getPos());

                        //
                        penetrate(decoys, 0);
                        if (tdksOutput.getTotalProbabilityProduct() > toPassThreshold) {
                            currState = States.ExecutePassPath;
                        } else {
                            currState = States.SDB;
                            SDB_t0 = System.currentTimeMillis();
                        }
                    }
                }
                case SDB -> { // SDB: Standby/Dodging/BackPass
                    System.out.println("[Attack2021] Entering state [SDB]");
                    if (System.currentTimeMillis() > SDB_delay) {
                        currState = States.Start;
                    }
                    /* Execute SDB */
                    fielders.stopAll();
                }
                case ExecutePassPath -> {
                    System.out.println("[Attack2021] Entering state [ExecutePassPath]");
                    System.out.println("\t[ExecutePassPath] Opt path: [" + attackerNodes + "] with P = " + tdksOutput.getTotalProbabilityProduct());
                    if(attackerNodes.get(1) instanceof Pdg.GoalNode) {
                        /* Shoot Goal */
                        System.out.println("\t[ExecutePassPath] shooooooooot");

                        Vec2D vec2D = compute.computeGoalKickVec(attackerNodes.get(1));
                        double v = compute.computeGoalAngle(attackerNodes.get(0));
                        Ally bot = attackerNodes.get(0).getBot();
                        assert bot != null;

                        while(!bot.isDirAimed(v)) {
                            bot.rotateTo(v);
                        }

                        bot.kick(vec2D);

                        currState = States.Exit;
                        return false;
                    } else {
                        if(attackerNodes.get(0) instanceof Pdg.AllyPassNode
                                && attackerNodes.get(1) instanceof Pdg.AllyRecepNode) {

                            System.out.println("\t[ExecutePassPath] Initiating coordinated pass");

                            /* Pass to Next */
                            CoordinatedPass.PassShootResult passResult = CoordinatedPass.PassShootResult.Executing;

                            Pdg.AllyPassNode node1 = (Pdg.AllyPassNode) attackerNodes.get(0);
                            Pdg.AllyRecepNode node2 = (Pdg.AllyRecepNode) attackerNodes.get(1);

//                            System.out.printf("[node1] passPoint: <%f, %f >, angle: %f, kickVec: <%f, %f> \n", node1.getPassPoint().x,
//                                    node1.getPassPoint().y, node1.getAngle(), node1.getKickVec().x, node1.getKickVec().y);
//                            System.out.printf("[node2]recepPoint: <%f, %f >, angle: %f \n", node2.getReceptionPoint().x,
//                                    node2.getReceptionPoint().y, node2.getAngle());

                            CoordinatedPass cp = new CoordinatedPass((Pdg.AllyPassNode) attackerNodes.get(0),
                                    (Pdg.AllyRecepNode) attackerNodes.get(1), ball, basicEstimator);

                            try {

                                while (passResult == CoordinatedPass.PassShootResult.Executing) {
//                                    System.out.println("\t[ExecutePassPath] Kick vec: " + ((PDG.AllyPassNode) attackerNodes.get(0)).getKickVec());
//                                    System.out.println("\t[ExecutePassPath] All nodes in attacker nodes: [" + attackerNodes + "]");
                                    for (int i = 2; i < attackerNodes.size(); i++) {
                                        if(attackerNodes.get(i) instanceof Pdg.AllyRecepNode) {
                                            Pdg.AllyRecepNode recepNode = ((Pdg.AllyRecepNode) attackerNodes.get(i));
                                            System.out.printf("\t[ExecutePassPath] bot %d Curving to reception point \n", recepNode.getBot().getID());
                                            recepNode.getBot().curveTo(recepNode.getReceptionPoint(), recepNode.getAngle());
                                        }
                                    }
                                    passResult = cp.execute();

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
//                            System.out.println("\\033[31mError in AttackPlanSummer2021\\033[0m");
                        }
                    }
                }
            }
        }

        System.out.println("[Attack2021] Exiting...");
        /* Exit State */
        currState = States.Start;
        return false;
    }

    private void penetrate(ArrayList<Ally> decoys, double disToFoe){
        ArrayList<Vec2D> topNPos = new ArrayList<>();
        topNPos.add(new Vec2D(-2000, 3000));
        topNPos.add(new Vec2D(2000, 3000));
        topNPos.add(new Vec2D(-2000, 2000));
        topNPos.add(new Vec2D(2000, 2000));
        topNPos.add(new Vec2D(-2000, 1000));
        topNPos.add(new Vec2D(2000, 1000));

        HashSet<Integer> occupied = new HashSet<>();
        Random random = new Random();

        for (Ally ally : decoys) {

            int index = random.nextInt(topNPos.size());

            while(occupied.contains(index)){
                index = random.nextInt(topNPos.size());
            }
            Vec2D loc = topNPos.get(index);
            occupied.add(index);
            ally.curveTo(loc);

        }
    }

    private void runDecoyBackGndTasks(Vec2D priorityAnchor) {
        if(decoys == null || decoys.size() == 0) {
            System.out.println("\t[Decoy] decoy list is null or empty");
            return;
        }

        ArrayList<Vec2D> gapPos = atkSupportMap.getTopNMaxPosWithClearance(decoys.size(), interAllyClearance);

        if(gapPos == null){
            System.out.println("\t[Decoy] gapPos is null");
            return;
        }

        ArrayList<Double> gapPosDir = new ArrayList<>();

        for (Vec2D pos : gapPos) {
            gapPosDir.add(ball.getPos().sub(pos).toPlayerAngle());
        }

        System.out.println("\t[Decoy] Decoy running...");
        boolean b = new Swarm(restFielders, config).groupTo(gapPos, gapPosDir, priorityAnchor);
    }



    public static String getDecoyListString(ArrayList<Ally> decoys){
        if (decoys == null || decoys.size() == 0){
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(decoys.get(0).getID());

        for (int i = 1; i < decoys.size(); i++) {
            stringBuilder.append(" - ");
            stringBuilder.append(decoys.get(i).getID());
        }

        return stringBuilder.toString();
    }
}
