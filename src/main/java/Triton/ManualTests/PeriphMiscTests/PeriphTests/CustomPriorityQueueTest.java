package Triton.ManualTests.PeriphMiscTests.PeriphTests;

import Triton.App;
import Triton.Config.Config;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.AI.TritonProbDijkstra.TritonDijkstra;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.ManualTests.TestUtil.TestUtil;
import Triton.ManualTests.TritonTestable;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class CustomPriorityQueueTest implements TritonTestable {

    @Override
    public boolean test(Config config) {
        boolean isSuccess = true;

        TritonDijkstra.AttackPathInfo attackPathInfo = new TritonDijkstra.AttackPathInfo();
        ArrayList<PUAG.Node> path = new ArrayList<>();
        path.add(new PUAG.AllyPassNode(new Ally(config, 0)));
        path.add(new PUAG.AllyRecepNode(new Ally(config, 1)));
        path.add(new PUAG.AllyRecepNode(new Ally(config, 2)));

        attackPathInfo.setMaxProbPath(path);
        attackPathInfo.setTotalProbabilityProduct(0.5);

        TritonDijkstra.AttackPathInfo attackPathInfo1 = attackPathInfo.replicatePath();
        attackPathInfo1.appendAndUpdate(new PUAG.AllyRecepNode(new Ally(config, 3)), 0.2);

        TritonDijkstra.AttackPathInfo attackPathInfo3 = new TritonDijkstra.AttackPathInfo();
        ArrayList<PUAG.Node> path1 = new ArrayList<>();
        path1.add(new PUAG.AllyPassNode(new Ally(config, 0)));
        path1.add(new PUAG.AllyRecepNode(new Ally(config, 1)));
        path1.add(new PUAG.AllyRecepNode(new Ally(config, 2)));

        attackPathInfo.setMaxProbPath(path1);
        attackPathInfo.setTotalProbabilityProduct(0.095);


        TritonDijkstra.AttackPathInfo attackPathInfo2 = attackPathInfo.replicatePath();
        attackPathInfo2.appendAndUpdate(new PUAG.AllyRecepNode(new Ally(config, 4)), 0.9);



        PriorityQueue<TritonDijkstra.AttackPathInfo> attackPathInfos = new PriorityQueue<>();
        attackPathInfos.add(attackPathInfo2);
        attackPathInfos.add(attackPathInfo1);

        isSuccess = (attackPathInfos.poll() == attackPathInfo1);


        attackPathInfos.add(attackPathInfo3);
        isSuccess &= (attackPathInfos.poll() == attackPathInfo3);


        attackPathInfos.add(attackPathInfo);

        isSuccess &= (attackPathInfos.poll() == attackPathInfo);


        if (isSuccess){
            System.out.println("Test PASSED");
        }else{
            System.out.println("Test FAILED");
        }

        TestUtil.enterKeyToContinue();

        return isSuccess;
    }
}
