package Triton.CoreModules.AI.TritonProbDijkstra;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class AttackPathInfo {
    private ArrayList<Integer> nodes;
    private double totalProb;
    private double[] receptionPoints;
}
