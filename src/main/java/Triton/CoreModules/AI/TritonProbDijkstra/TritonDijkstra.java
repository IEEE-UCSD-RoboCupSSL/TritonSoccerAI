package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.InvalidDijkstraGraphException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NoDijkComputeInjectionException;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.UnknownPuagNodeException;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class TritonDijkstra {
    private final PUAG graph;
    private DijkCompute dijkComp;

    public TritonDijkstra(PUAG graph, DijkCompute dijkComp) throws InvalidDijkstraGraphException {
        this.graph = graph;
        this.dijkComp = dijkComp;

        if(graph.getEndNode() == null || graph.getStartNode() == null){
            throw new InvalidDijkstraGraphException();
        }
    }

    public TritonDijkstra(PUAG graph) throws InvalidDijkstraGraphException {
        this.graph = graph;

        if(graph.getEndNode() == null || graph.getStartNode() == null){
            throw new InvalidDijkstraGraphException();
        }
    }

    public AttackPathInfo compute() throws UnknownPuagNodeException, NoDijkComputeInjectionException {
        if(dijkComp == null){
            throw new NoDijkComputeInjectionException();
        }

        PriorityQueue<AttackPathInfo> frontier = new PriorityQueue<>();
        HashSet<PUAG.Node> explored = new HashSet<>();

        PUAG.Node startNode = graph.getStartNode();

        AttackPathInfo attackPathInfo = new AttackPathInfo();
        attackPathInfo.appendAndUpdate(startNode, 1.0);

        frontier.add(attackPathInfo);

        while (!frontier.isEmpty()) {
            AttackPathInfo currPath = frontier.poll();  // Get the path with the highest prob so far
            PUAG.Node tailNode = currPath.getTailNode();    // Get the tail node of the returned path

            if (tailNode != null) {
                if(tailNode.equals(graph.getEndNode())){
                    if(tailNode.getClass() == PUAG.AllyPassNode.class) {
                        PUAG.AllyPassNode tailAllyPassNode = (PUAG.AllyPassNode)  tailNode;
                        PUAG.Node secondTailNode = currPath.getSecondTailNode();

                        tailAllyPassNode.setPassPoint(dijkComp.computePasspoint(secondTailNode, tailNode));
                        tailAllyPassNode.setAngle(dijkComp.computeAngle(secondTailNode, tailNode));
                        tailAllyPassNode.setKickVec(dijkComp.computeKickVec(secondTailNode, tailNode));
                    }
                    else if (tailNode.getClass() == PUAG.AllyRecepNode.class){
                        PUAG.AllyRecepNode tailAllyRecepNode = (PUAG.AllyRecepNode) tailNode;
                        PUAG.Node secondTailNode = currPath.getSecondTailNode();

                        tailAllyRecepNode.setAngle(dijkComp.computeAngle(secondTailNode, tailNode));
                        tailAllyRecepNode.setReceptionPoint(dijkComp.computeRecepPoint(secondTailNode, tailNode));
                    }
                    else if (tailNode.getClass() == PUAG.GoalNode.class){
                        PUAG.GoalNode tailGoalNode = (PUAG.GoalNode) tailNode;
                        tailGoalNode.setGoalCenter(dijkComp.computeGoalCenter(tailGoalNode));
                    } else {
                        throw new UnknownPuagNodeException(tailNode);
                    }

                    currPath.appendAndUpdate(graph.getEndNode(), 1.0);
                    return currPath;
                }

                List<PUAG.Node> adjacentNodes = graph.getAdjacentNodes(tailNode);    // Get reachable neighbors from the tail

                for (PUAG.Node adjacentNode : adjacentNodes) {

                    if(explored.contains(adjacentNode)){
                        continue;
                    }

                    double prob = 1;    // These fields are to be provided

                    AttackPathInfo newAttackPath = currPath.replicatePath();
                    newAttackPath.appendAndUpdate(adjacentNode, prob);

                    Optional<AttackPathInfo> first = frontier.stream().filter((a) -> a.equals(newAttackPath)).findFirst();
                    if (first.isPresent()){ // Check if the neighbor was already considered
                        AttackPathInfo prevPath = first.get();

                        double prevProb = prevPath.getTotalProbabilityProduct();
                        double currProb = currPath.getTotalProbabilityProduct() * prob;

                        if (prevProb > currProb) {    // If the neighbor was already considered and the curr path
                            // is better, replace prev path with curr path
                            frontier.remove(prevPath);
                            frontier.add(newAttackPath);
                        }
                    } else {
                        frontier.add(newAttackPath);
                    }
                }
                explored.add(tailNode);
            }
        }

        return null;
    }

    @Getter
    @Setter
    public static class AttackPathInfo implements Comparable<AttackPathInfo> {
        private ArrayList<PUAG.Node> maxProbPath = new ArrayList<>();
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

        public AttackPathInfo replicatePath() {
            AttackPathInfo attackPathInfo = new AttackPathInfo();
            attackPathInfo.getMaxProbPath().addAll(this.maxProbPath);
            attackPathInfo.setTotalProbabilityProduct(this.totalProbabilityProduct);

            return attackPathInfo;
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

}
