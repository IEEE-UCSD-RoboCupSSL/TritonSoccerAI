package triton.manualTests.periphMiscTests.periphTests;

import triton.config.Config;
import triton.coreModules.ai.dijkstra.exceptions.UnknownPdgNodeException;
import triton.coreModules.ai.dijkstra.Pdg;
import triton.coreModules.ai.dijkstra.TritonDijkstra;
import triton.coreModules.robot.ally.Ally;
import triton.manualTests.testUtil.TestUtil;
import triton.manualTests.TritonTestable;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class CustomPriorityQueueTest implements TritonTestable {

    @Override
    public boolean test(Config config) {
        boolean isSuccess = true;

        TritonDijkstra.AttackPathInfo attackPathInfo = new TritonDijkstra.AttackPathInfo();
        ArrayList<Pdg.Node> path = new ArrayList<>();
        path.add(new Pdg.AllyPassNode(new Ally(config, 0)));
        path.add(new Pdg.AllyRecepNode(new Ally(config, 1)));
        path.add(new Pdg.AllyRecepNode(new Ally(config, 2)));

        attackPathInfo.setMaxProbPath(path);
        attackPathInfo.setTotalProbabilityProduct(0.5);

        TritonDijkstra.AttackPathInfo attackPathInfo1 = null;
        try {
            attackPathInfo1 = attackPathInfo.replicatePath();
        } catch (UnknownPdgNodeException e) {
            e.printStackTrace();
        }
        attackPathInfo1.appendAndUpdate(new Pdg.AllyRecepNode(new Ally(config, 3)), 0.2);

        TritonDijkstra.AttackPathInfo attackPathInfo3 = new TritonDijkstra.AttackPathInfo();
        ArrayList<Pdg.Node> path1 = new ArrayList<>();
        path1.add(new Pdg.AllyPassNode(new Ally(config, 0)));
        path1.add(new Pdg.AllyRecepNode(new Ally(config, 1)));
        path1.add(new Pdg.AllyRecepNode(new Ally(config, 2)));

        attackPathInfo.setMaxProbPath(path1);
        attackPathInfo.setTotalProbabilityProduct(0.095);


        TritonDijkstra.AttackPathInfo attackPathInfo2 = null;
        try {
            attackPathInfo2 = attackPathInfo.replicatePath();
        } catch (UnknownPdgNodeException e) {
            e.printStackTrace();
        }
        attackPathInfo2.appendAndUpdate(new Pdg.AllyRecepNode(new Ally(config, 4)), 0.9);



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
