package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.CoreModules.AI.TritonProbDijkstra.Computables.DijkCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.GraphIOException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.InvalidDijkstraGraphException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NoDijkComputeInjectionException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.UnknownPdgNodeException;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;
import Triton.SoccerObjects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * A dijkstra algorithm that computes the optimal route for ball passing. The probability for
 * passing between any two nodes is computed on the go.
 * Some details on the graph. Currently, we assume that our graph is fully connected with
 * a max of 6 nodes (1 start node, 4 middle node, and 1 goal node). For a graph like this
 * there are 6 chooses 2 = 15 number of edges - O(|V|^2). So the time complexity of the current
 * implementation is (Java PQ Implementation uses a binary queue): O(|V|^2 log|V|)
 */
@Getter
@Setter
public class TritonDijkstra {
    private final PDG graph;
    private DijkCompute dijkComp;
    RobotList<Ally> fielders;
    RobotList<Foe> foes;
    Ball ball;


    /**
     * The preferred constructor. If the `TritonDijkstra` object is built using this constructor
     * then it requires no further call to setter to inject `dijkComp`.
     *
     * @param graph    The graph to be worked on
     * @param dijkComp A `DijkComp` object using the same references to the nodes stored in `graph`.
     *                 This object defines how the data like probability and angle should be computed
     *                 given two nodes.
     * @throws InvalidDijkstraGraphException The graph must have an end node and an start node
     *                                       * in the context of our program.
     */
    public TritonDijkstra(PDG graph, DijkCompute dijkComp, RobotList<Ally> fielders, RobotList<Foe> foes,
                          Ball ball) throws InvalidDijkstraGraphException {
        this.graph = graph;
        this.dijkComp = dijkComp;
        this.fielders = fielders;
        this.foes = foes;
        this.ball = ball;

        if (graph.getEndNode() == null || graph.getStartNode() == null) {
            throw new InvalidDijkstraGraphException();
        }
    }

    public TritonDijkstra(PDG graph, DijkCompute dijkComp, SoccerObjects so) throws InvalidDijkstraGraphException {
        this.graph = graph;
        this.dijkComp = dijkComp;
        this.fielders = so.fielders;
        this.foes = so.foes;
        this.ball = so.ball;

        if (graph.getEndNode() == null || graph.getStartNode() == null) {
            throw new InvalidDijkstraGraphException();
        }
    }


    /**
     * A `DijkComp` object needs to be injected later using setter if using this constructor.
     *
     * @param graph The graph to be worked on
     * @throws InvalidDijkstraGraphException The graph must have an end node and an start node
     *                                       in the context of our program.
     */
    public TritonDijkstra(PDG graph, RobotList<Ally> fielders, RobotList<Foe> foes,
                          Ball ball) throws InvalidDijkstraGraphException {
        this.graph = graph;
        this.fielders = fielders;
        this.foes = foes;
        this.ball = ball;

        if (graph.getEndNode() == null || graph.getStartNode() == null) {
            throw new InvalidDijkstraGraphException();
        }
    }

    public static ArrayList<RobotSnapshot> buildFielderSnaps(RobotList<Ally> fielders){
        ArrayList<RobotSnapshot> robotSnapshots = new ArrayList<>();

        for (Ally fielder : fielders) {
            robotSnapshots.add(new RobotSnapshot(fielder));
        }

        return robotSnapshots;
    }

    public static ArrayList<RobotSnapshot> buildFoeSnaps( RobotList<Foe> foes){
        ArrayList<RobotSnapshot> robotSnapshots = new ArrayList<>();

        for (Foe foe : foes) {
            robotSnapshots.add(new RobotSnapshot(foe));
        }

        return robotSnapshots;
    }

    public static RWLockee<Vec2D> buildBallSnap(Ball ball){
        return new RWLockee<Vec2D>(ball.getPos());
    }

    private void updateNode(PDG.Node node1, PDG.Node node2) throws GraphIOException {

        if (node2.getClass() == PDG.GoalNode.class) {
            graph.setEdgeProb(node1, node2, dijkComp.computeGoalProb(node1));
        } else {
            graph.setEdgeProb(node1, node2, dijkComp.computeProb(node1, node2));
        }

        if (node1.getClass() == PDG.AllyPassNode.class) {
            PDG.AllyPassNode node1PassNode = (PDG.AllyPassNode) node1;
            if (node2.getClass() == PDG.GoalNode.class){
                node1PassNode.setPassPoint(dijkComp.computeGoalPassPoint(node1));
                node1PassNode.setAngle(dijkComp.computeGoalAngle(node1));
                node1PassNode.setKickVec(dijkComp.computeGoalKickVec(node1));
            }else {
                node1PassNode.setPassPoint(dijkComp.computePassPoint(node1, node2));
                node1PassNode.setAngle(dijkComp.computeAngle(node1, node2));
                node1PassNode.setKickVec(dijkComp.computeKickVec(node1, node2));
            }
        }

        if (node2.getClass() == PDG.AllyRecepNode.class) {
            PDG.AllyRecepNode node2RecepNode = (PDG.AllyRecepNode) node2;
            node2RecepNode.setReceptionPoint(dijkComp.computeRecepPoint(node1, node2));
            node2RecepNode.setAngle(dijkComp.computeAngle(node1, node2));
        }


    }

    /**
     * Starts the computation. Requires the injection of an `DijkComp` object either by setter or constructor.
     *
     * @return An computed optimal path.
     * @throws UnknownPdgNodeException        Exception thrown if any node in the graph is not an known Node type.
     * @throws NoDijkComputeInjectionException Exception thrown if no `DijkComp` object is injected ever.
     */
    public AttackPathInfo compute() throws GraphIOException, NoDijkComputeInjectionException {
        if (dijkComp == null) {
            throw new NoDijkComputeInjectionException();
        }

        PriorityQueue<AttackPathInfo> frontier = new PriorityQueue<>();
        HashSet<PDG.Node> explored = new HashSet<>();

        PDG.Node startNode = graph.getStartNode();

        AttackPathInfo attackPathInfo = new AttackPathInfo();
        attackPathInfo.appendAndUpdate(startNode, 1.0);

//        System.out.println("[compute1 ] Initialized new path: " + attackPathInfo);

        frontier.add(attackPathInfo);
//        System.out.println("[compute2 ] Added path: [" + attackPathInfo.pathString() + "] to frontier with P = " + attackPathInfo.getTotalProbabilityProduct());

        while (!frontier.isEmpty()) {
            AttackPathInfo currPath = frontier.poll();  // Get the path with the highest prob so far
//            System.out.println("[compute2.1 ] Popped path: [" + currPath.pathString() + "] out of frontier with P = " + currPath.getTotalProbabilityProduct());

            PDG.Node tailNode = currPath.getTailNode();    // Get the tail node of the returned path

            if (tailNode != null) {
                if (tailNode.equals(graph.getEndNode())) {
                    PDG.Node secondTailNode = currPath.getSecondTailNode();

                    if(secondTailNode == null) {
//                        System.out.println("[compute3 ] Second tail returned null.");
                    }
                    assert secondTailNode != null;
                    updateNode(secondTailNode, tailNode);

//                    System.out.printf("[compute4 ] Updating node %s - tail: %s \n", secondTailNode.getNodeBotIdString(), tailNode.getNodeBotIdString());
//                    System.out.printf("[compute5 ] Append and update node %s with prob endNode\n", tailNode.getNodeBotIdString());


                    return currPath;
                }

                List<PDG.Node> adjacentNodes = graph.getAdjacentNodes(tailNode);    // Get reachable neighbors from the tail
//                System.out.printf("[compute6 ] Neighbors of %s are: [%s]\n", tailNode.getNodeBotIdString(), neighborsString(adjacentNodes));

                for (PDG.Node adjacentNode : adjacentNodes) {

                    if (explored.contains(adjacentNode)) {
                        continue;
                    }

                    if (currPath.isNodeInPath(adjacentNode)){
                        continue;
                    }

                    updateNode(tailNode, adjacentNode);
//                    System.out.printf("[compute7 ] Updating node %s - %s \n", currPath.getTailNode().getNodeBotIdString(), adjacentNode.getNodeBotIdString());

                    AttackPathInfo newAttackPath = currPath.replicatePath();
//                    System.out.println("[compute7.1] Replicating path");


                    double prob = dijkComp.computeProb(tailNode, adjacentNode);
                    newAttackPath.appendAndUpdate(adjacentNode, prob);
//                    System.out.printf("[compute8 ] Append and update node %s with prob %f\n", adjacentNode.getNodeBotIdString(), prob);

//                    System.out.printf("[compute8.1] The resulting path is [%s] with P = %f \n", newAttackPath.pathString(), newAttackPath.getTotalProbabilityProduct());


                    Optional<AttackPathInfo> first = frontier.stream().filter((a) -> a.equals(newAttackPath)).findFirst();
                    if (first.isPresent()) { // Check if the neighbor was already considered
                        AttackPathInfo prevPath = first.get();
//                        System.out.printf("[compute9 ] Found existing path in frontier: [%s] with P = %f \n", prevPath.pathString(), prevPath.getTotalProbabilityProduct());

                        double prevProb = prevPath.getTotalProbabilityProduct();
                        double currProb = currPath.getTotalProbabilityProduct() * prob;

                        if (prevProb < currProb) {    // If the neighbor was already considered and the curr path
                            // is better, replace prev path with curr path
                            frontier.remove(prevPath);
                            frontier.add(newAttackPath);
//                            System.out.println("[compute10] Replaced path: " + newAttackPath.pathString() + " to frontier");

                        }
                    } else {
                        frontier.add(newAttackPath);
//                        System.out.println("[compute11] Added path: [" + newAttackPath.pathString() + "] to frontier with P = " + newAttackPath.getTotalProbabilityProduct());
                    }
                }
                explored.add(tailNode);
            }
        }

        return null;
    }

    @Getter
    @Setter
    @ToString
    public static class AttackPathInfo implements Comparable<AttackPathInfo> {
        private ArrayList<PDG.Node> maxProbPath = new ArrayList<>();
        private HashSet<PDG.Node> consideredNodes = new HashSet<>();
        private double totalProbabilityProduct = 1.0;

        @Override
        public int compareTo(@NotNull TritonDijkstra.AttackPathInfo anotherAttackPathInfo) {
            double p1Inverse;
            double p2Inverse;

            try {
                p1Inverse = 1 / this.totalProbabilityProduct;
            } catch (ArithmeticException e) {
                p1Inverse = Double.MAX_VALUE;
            }

            try {
                p2Inverse = 1 / anotherAttackPathInfo.totalProbabilityProduct;
            } catch (ArithmeticException e) {
                p2Inverse = Double.MAX_VALUE;
            }

            return Double.compare(p1Inverse, p2Inverse);
        }

        public void appendAndUpdate(PDG.Node node, Double prob) {
            maxProbPath.add(node);
            consideredNodes.add(node);
            totalProbabilityProduct *= prob;
        }

        /**
         * @return NULL if the path is empty
         */
        public @Nullable PDG.Node getTailNode() {
            if (maxProbPath.isEmpty()) {
                return null;
            }
            return maxProbPath.get(maxProbPath.size() - 1);
        }

        public @Nullable PDG.Node getSecondTailNode() {
            if (maxProbPath.isEmpty()) {
                return null;
            }

            return maxProbPath.get(maxProbPath.size() - 2);
        }

        public boolean isNodeInPath(PDG.Node n){
            return consideredNodes.contains(n);
        }

        public AttackPathInfo replicatePath() throws UnknownPdgNodeException {
            AttackPathInfo attackPathInfo = new AttackPathInfo();

            for (PDG.Node node : this.maxProbPath) {
                if(node.getClass() == PDG.AllyPassNode.class) {
                    PDG.AllyPassNode passNode = (PDG.AllyPassNode) node;
                    PDG.AllyPassNode newPassNode = new PDG.AllyPassNode(passNode.getBot());
                    newPassNode.setPassPoint(new Vec2D(passNode.getPassPoint()));
                    newPassNode.setAngle(passNode.getAngle());
                    newPassNode.setKickVec(new Vec2D(passNode.getKickVec()));
                    attackPathInfo.getMaxProbPath().add(newPassNode);
                    attackPathInfo.getConsideredNodes().add(newPassNode);
                } else if (node.getClass() == PDG.AllyRecepNode.class){
                    PDG.AllyRecepNode recepNode = (PDG.AllyRecepNode) node;
                    PDG.AllyRecepNode newRecepNode = new PDG.AllyRecepNode(recepNode.getBot());
                    newRecepNode.setReceptionPoint(new Vec2D(recepNode.getReceptionPoint()));
                    newRecepNode.setAngle(recepNode.getAngle());
                    attackPathInfo.getMaxProbPath().add(newRecepNode);
                    attackPathInfo.getConsideredNodes().add(newRecepNode);
                } else if (node.getClass() == PDG.GoalNode.class){
                    PDG.GoalNode goalNode = new PDG.GoalNode();
                    attackPathInfo.getMaxProbPath().add(goalNode);
                    attackPathInfo.getConsideredNodes().add(goalNode);
                } else {
                    throw new UnknownPdgNodeException(node);
                }
            }

            attackPathInfo.setTotalProbabilityProduct(this.totalProbabilityProduct);

            return attackPathInfo;
        }

        public String pathString() {
            StringBuilder stringBuilder = new StringBuilder();
            PDG.Node node = maxProbPath.get(0);

            if(node == null){
                return "";
            }

            stringBuilder.append(node.getNodeBotIdString());

            for (int i = 1; i < maxProbPath.size(); i++) {
                stringBuilder.append(" - ");
                stringBuilder.append(maxProbPath.get(i).getNodeBotIdString());
            }

            return stringBuilder.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AttackPathInfo that = (AttackPathInfo) o;
            return Objects.equals(getTailNode(), that.getTailNode());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTailNode());
        }
    }

    public static String neighborsString(List<PDG.Node> neighbors){
        StringBuilder stringBuilder = new StringBuilder();

        PDG.Node node = neighbors.get(0);

        if(node == null){
            return "[ ]";
        }

        stringBuilder.append("[");
        stringBuilder.append(node.getNodeBotIdString());


        for (int i = 1; i < neighbors.size(); i++) {
            stringBuilder.append(", ");
            stringBuilder.append(neighbors.get(i).getNodeBotIdString());
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
