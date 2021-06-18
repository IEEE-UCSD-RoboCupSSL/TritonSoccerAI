package Triton.CoreModules.AI.TritonProbDijkstra;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class TritonDijkstra {
    private HashMap<Integer, ArrayList<Double>> adjList;
    private AttackPathInfo attackPathInfo;

    public TritonDijkstra(HashMap<Integer, ArrayList<Double>> adjList){

    }

    public AttackPathInfo calculate(){
        return new AttackPathInfo();
    }

    @Data
    static class pathInfo implements Comparable<Double> {
        ArrayList<Integer> currNodes;
        Double currProbInverse;

        @Override
        public int compareTo(@NotNull Double aDouble) {
            return 0;
        }
    }


}
