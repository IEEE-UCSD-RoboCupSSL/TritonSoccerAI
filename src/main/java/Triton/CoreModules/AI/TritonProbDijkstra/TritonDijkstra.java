package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.CoreModules.AI.ReceptionPoint;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * `@Data`automatically adds getters, setters, constructors, etc. for all private final fields.
 */
@Data
public class TritonDijkstra {
    private final PUAG graph;

    public AttackPathInfo compute() {

        HashSet<PUAG.Node> consideredNodes = new HashSet<>();
        PriorityQueue<AttackPathInfo> frontier = new PriorityQueue<>();

        PUAG.Node startNode = graph.getStartNode();

        AttackPathInfo attackPathInfo = new AttackPathInfo();
        attackPathInfo.appendAndUpdate(startNode, new ReceptionPoint(null, 0, null, true), 1.0);

        frontier.add(attackPathInfo);

        while(!frontier.isEmpty()){
            PUAG.Node curr = frontier.poll().getTailNode();

            if(curr != null) {
                List<PUAG.Node> adjacentNodes = curr.getAdjacentNodes();

                for (PUAG.Node adjacentNode : adjacentNodes) {

                }
            }


        }

        return new AttackPathInfo();
    }

    @Data
    public static class AttackPathInfo implements Comparable<AttackPathInfo>{
        private ArrayList<PUAG.Node> maxProbPath = new ArrayList<>();
        private double totalProbabilityProduct = 1.0;
        private ArrayList<ReceptionPoint> receptionPoints = new ArrayList<>();

        @Override
        public int compareTo(@NotNull TritonDijkstra.AttackPathInfo anotherAttackPathInfo) {
            double p1Inverse;
            double p2Inverse;

            try {
                p1Inverse = 1 / this.totalProbabilityProduct;
            }catch (ArithmeticException e){
                p1Inverse = Double.MAX_VALUE;
            }

            try {
                p2Inverse = 1 / anotherAttackPathInfo.totalProbabilityProduct;
            }catch (ArithmeticException e){
                p2Inverse = Double.MAX_VALUE;
            }

            return Double.compare(p1Inverse, p2Inverse);
        }

        public void appendAndUpdate(PUAG.Node node, ReceptionPoint receptionPoint, Double prob){
            maxProbPath.add(node);
            receptionPoints.add(receptionPoint);
            totalProbabilityProduct *= prob;
        }

        /**
         *
         * @return NULL if the path is empty
         */
        public @Nullable PUAG.Node getTailNode(){
            if(maxProbPath.isEmpty()){
                return null;
            }
            return maxProbPath.get(maxProbPath.size() - 1);
        }
    }

}
