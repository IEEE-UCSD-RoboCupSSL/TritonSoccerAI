package Triton.CoreModules.AI.TritonProbDijkstra;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class TritonDijkstra {
    private final PUAG graph;
    private ProbCompute probComp;

    public TritonDijkstra(PUAG graph, ProbCompute probComp) {
        this.graph = graph;
        this.probComp = probComp;
    }

    public TritonDijkstra(PUAG graph){
        this.graph = graph;
    }

    public AttackPathInfo compute() {
        PriorityQueue<AttackPathInfo> frontier = new PriorityQueue<>();
        HashSet<PUAG.Node> explored = new HashSet<>();

        PUAG.Node startNode = graph.getStartNode();

        AttackPathInfo attackPathInfo = new AttackPathInfo();
        attackPathInfo.appendAndUpdate(startNode, 1.0);

        frontier.add(attackPathInfo);

        while (!frontier.isEmpty()) {
            AttackPathInfo currPath = frontier.poll();  // Get the path with the highest prob so far
            PUAG.Node tailNode = currPath.getTailNode();    // Get the tail node Of the returned path

            if (tailNode != null) {
                if(tailNode.equals(graph.getEndNode())){
                    double prob = 1;    // These fields are to be provided

                    currPath.appendAndUpdate(graph.getEndNode(), prob);
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
