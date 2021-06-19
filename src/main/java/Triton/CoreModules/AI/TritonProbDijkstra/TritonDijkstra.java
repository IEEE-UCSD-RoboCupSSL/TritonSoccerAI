package Triton.CoreModules.AI.TritonProbDijkstra;

import Triton.CoreModules.Robot.Ally.KickType;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class TritonDijkstra {
    private PUAG graph;
    public TritonDijkstra(PUAG graph){
        this.graph = graph;
    }

    public AttackPathInfo compute() {
        /* To-do */
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



    public static class AttackPathInfo {
        public ArrayList<PUAG.Node> maxProbPath;
        public double TotalProbabilityProduct = 1.0;
        public ArrayList<ReceptionPoint> receptionPoints;
    }

    public static class ReceptionPoint {
        public Vec2D receptionPoint;
        public double angle;
        public KickType kickType;
    }




}
