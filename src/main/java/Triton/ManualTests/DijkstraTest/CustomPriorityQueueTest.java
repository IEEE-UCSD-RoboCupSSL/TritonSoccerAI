package Triton.ManualTests.DijkstraTest;

import Triton.App;
import Triton.Config.Config;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.AI.TritonProbDijkstra.TritonDijkstra;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.ManualTests.TritonTestable;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class CustomPriorityQueueTest implements TritonTestable {
    private final PriorityQueue<TritonDijkstra.AttackPathInfo> pq = new PriorityQueue<>();

    @Override
    public boolean test(Config config) {
        boolean isSuccess = false;

        TritonDijkstra.AttackPathInfo attackPathInfo = new TritonDijkstra.AttackPathInfo();
        ArrayList<PUAG.Node> path = new ArrayList<>();
        path.add(new PUAG.AllyHolderNode(new Ally(config, 0)));
        path.add(new PUAG.AllyNode(new Ally(config, 1)));
        path.add(new PUAG.AllyNode(new Ally(config, 2)));

        attackPathInfo.setMaxProbPath(path);
        attackPathInfo.setTotalProbabilityProduct(0.5);

        TritonDijkstra.AttackPathInfo attackPathInfo1 = attackPathInfo.replicatePath();
        attackPathInfo1.appendAndUpdate(new PUAG.AllyNode(new Ally(config, 3)), 0.2);

        TritonDijkstra.AttackPathInfo attackPathInfo3 = new TritonDijkstra.AttackPathInfo();
        ArrayList<PUAG.Node> path1 = new ArrayList<>();
        path1.add(new PUAG.AllyHolderNode(new Ally(config, 0)));
        path1.add(new PUAG.AllyNode(new Ally(config, 1)));
        path1.add(new PUAG.AllyNode(new Ally(config, 2)));

        attackPathInfo.setMaxProbPath(path1);
        attackPathInfo.setTotalProbabilityProduct(0.11);

        TritonDijkstra.AttackPathInfo attackPathInfo2 = attackPathInfo.replicatePath();
        attackPathInfo2.appendAndUpdate(new PUAG.AllyNode(new Ally(config, 4)), 0.9);



        PriorityQueue<TritonDijkstra.AttackPathInfo> attackPathInfos = new PriorityQueue<>();
        attackPathInfos.add(attackPathInfo1);
        attackPathInfos.add(attackPathInfo2);

        if(attackPathInfos.poll() == attackPathInfo1){
            isSuccess = true;
        }

        attackPathInfos.add(attackPathInfo);

        if(attackPathInfos.poll() == attackPathInfo){
            isSuccess = true;
        }

        if (isSuccess){
            System.out.println("Test PASSED");
        }else{
            System.out.println("Test FAILED");
        }

        App.enterKeyToContinue();

        return isSuccess;
    }
}
