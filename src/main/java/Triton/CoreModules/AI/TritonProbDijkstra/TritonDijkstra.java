package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.CoreModules.AI.TritonProbDijkstra.Computables.DijkCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.InvalidDijkstraGraphException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NoDijkComputeInjectionException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.UnknownPuagNodeException;
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
    private final PUAG graph;
    private DijkCompute dijkComp;

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
    public TritonDijkstra(PUAG graph, DijkCompute dijkComp) throws InvalidDijkstraGraphException {
        this.graph = graph;
        this.dijkComp = dijkComp;

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
    public TritonDijkstra(PUAG graph) throws InvalidDijkstraGraphException {
        this.graph = graph;

        if (graph.getEndNode() == null || graph.getStartNode() == null) {
            throw new InvalidDijkstraGraphException();
        }
    }

    private void updateNode(PUAG.Node secondTailNode, PUAG.Node tailNode) {
        if (tailNode.getClass() == PUAG.AllyPassNode.class) {
            PUAG.AllyPassNode tailAllyPassNode = (PUAG.AllyPassNode) tailNode;

            graph.setEdgeProb(secondTailNode, tailNode, dijkComp.computeProb(secondTailNode, tailNode));
            tailAllyPassNode.setPassPoint(dijkComp.computePasspoint(secondTailNode, tailNode));
            tailAllyPassNode.setAngle(dijkComp.computeAngle(secondTailNode, tailNode));
            tailAllyPassNode.setKickVec(dijkComp.computeKickVec(secondTailNode, tailNode));
        } else if (tailNode.getClass() == PUAG.AllyRecepNode.class) {
            PUAG.AllyRecepNode tailAllyRecepNode = (PUAG.AllyRecepNode) tailNode;

            graph.setEdgeProb(secondTailNode, tailNode, dijkComp.computeProb(secondTailNode, tailNode));
            tailAllyRecepNode.setAngle(dijkComp.computeAngle(secondTailNode, tailNode));
            tailAllyRecepNode.setReceptionPoint(dijkComp.computeRecepPoint(secondTailNode, tailNode));
        } else if (tailNode.getClass() == PUAG.GoalNode.class) {
            PUAG.GoalNode tailGoalNode = (PUAG.GoalNode) tailNode;
            tailGoalNode.setGoalCenter(dijkComp.computeGoalCenter(tailGoalNode));
        } else {
            throw new UnknownPuagNodeException(tailNode);
        }
    }

    /**
     * Starts the computation. Requires the injection of an `DijkComp` object either by setter or constructor.
     *
     * @return An computed optimal path.
     * @throws UnknownPuagNodeException        Exception thrown if any node in the graph is not an known Node type.
     * @throws NoDijkComputeInjectionException Exception thrown if no `DijkComp` object is injected ever.
     */
    public AttackPathInfo compute() throws UnknownPuagNodeException, NoDijkComputeInjectionException {
        if (dijkComp == null) {
            throw new NoDijkComputeInjectionException();
        }

        PriorityQueue<AttackPathInfo> frontier = new PriorityQueue<>();
        HashSet<PUAG.Node> explored = new HashSet<>();

        PUAG.Node startNode = graph.getStartNode();

        AttackPathInfo attackPathInfo = new AttackPathInfo();
        attackPathInfo.appendAndUpdate(startNode, 1.0);

//        System.out.println("[compute1 ] Initialized new path: " + attackPathInfo);

        frontier.add(attackPathInfo);
//        System.out.println("[compute2 ] Added path: [" + attackPathInfo.pathString() + "] to frontier with P = " + attackPathInfo.getTotalProbabilityProduct());

        while (!frontier.isEmpty()) {
            AttackPathInfo currPath = frontier.poll();  // Get the path with the highest prob so far
//            System.out.println("[compute2.1 ] Popped path: [" + currPath.pathString() + "] out of frontier with P = " + currPath.getTotalProbabilityProduct());

            PUAG.Node tailNode = currPath.getTailNode();    // Get the tail node of the returned path

            if (tailNode != null) {
                if (tailNode.equals(graph.getEndNode())) {
                    PUAG.Node secondTailNode = currPath.getSecondTailNode();
                    updateNode(secondTailNode, tailNode);
                    if(secondTailNode == null) {
//                        System.out.println("[compute3 ] Second tail returned null.");
                    }
//                    System.out.printf("[compute4 ] Updating node %s - %s \n", secondTailNode.getNodeBotIdString(), tailNode.getNodeBotIdString());

                    currPath.appendAndUpdate(tailNode, 1.0);
//                    System.out.printf("[compute5 ] Append and update node %s with prob endNode\n", tailNode.getNodeBotIdString());

                    return currPath;
                }

                List<PUAG.Node> adjacentNodes = graph.getAdjacentNodes(tailNode);    // Get reachable neighbors from the tail
//                System.out.printf("[compute6 ] Neighbors of %s are: [%s]\n", tailNode.getNodeBotIdString(), neighborsString(adjacentNodes));

                for (PUAG.Node adjacentNode : adjacentNodes) {

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
        private ArrayList<PUAG.Node> maxProbPath = new ArrayList<>();
        private HashSet<PUAG.Node> consideredNodes = new HashSet<>();
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

        public void appendAndUpdate(PUAG.Node node, Double prob) {
            maxProbPath.add(node);
            consideredNodes.add(node);
            totalProbabilityProduct *= prob;
        }

        /**
         * @return NULL if the path is empty
         */
        public @Nullable PUAG.Node getTailNode() {
            if (maxProbPath.isEmpty()) {
                return null;
            }
            return maxProbPath.get(maxProbPath.size() - 1);
        }

        public @Nullable PUAG.Node getSecondTailNode() {
            if (maxProbPath.isEmpty()) {
                return null;
            }

            return maxProbPath.get(maxProbPath.size() - 2);
        }

        public boolean isNodeInPath(PUAG.Node n){
            return consideredNodes.contains(n);
        }

        public AttackPathInfo replicatePath() {
            AttackPathInfo attackPathInfo = new AttackPathInfo();
            attackPathInfo.getMaxProbPath().addAll(this.maxProbPath);
            attackPathInfo.setTotalProbabilityProduct(this.totalProbabilityProduct);

            return attackPathInfo;
        }

        public String pathString() {
            StringBuilder stringBuilder = new StringBuilder();
            PUAG.Node node = maxProbPath.get(0);

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

    public static String neighborsString(List<PUAG.Node> neighbors){
        StringBuilder stringBuilder = new StringBuilder();

        PUAG.Node node = neighbors.get(0);

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
