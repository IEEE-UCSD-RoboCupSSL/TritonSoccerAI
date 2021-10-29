package triton.manualTests.coreTests.dijkstraTest;

import triton.config.Config;
import triton.coreModules.ai.dijkstra.computableImpl.MockCompute;
import triton.coreModules.ai.dijkstra.exceptions.*;
import triton.coreModules.ai.dijkstra.Pdg;
import triton.coreModules.ai.dijkstra.TritonDijkstra;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.RobotList;
import triton.manualTests.testUtil.TestUtil;
import triton.manualTests.TritonTestable;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.SoccerObjects;
import lombok.Data;

import java.util.ArrayList;


/**
 *
 */
@Data
public class DijkstraTest implements TritonTestable {

    private SoccerObjects so;

    public DijkstraTest(SoccerObjects so) {
        this.so = so;
    }

    @Override
    public boolean test(Config config) throws GraphIOException, NoDijkComputeInjectionException {
        RobotList<Ally> fielders = so.fielders;
        Ally startAlly = fielders.get(0);


        Pdg.AllyPassNode allyPassNode = new Pdg.AllyPassNode(startAlly);
        ArrayList<Pdg.Node> allyRecepNodes = new ArrayList<>();

        for (int i = 1; i < fielders.size(); i++) {
            allyRecepNodes.add(new Pdg.AllyRecepNode(fielders.get(i)));
        }

        Pdg.GoalNode goalNode = new Pdg.GoalNode();

        Pdg PDG = new Pdg(allyPassNode, goalNode, allyRecepNodes);

        Pdg.Node n0 = allyPassNode;
        Pdg.Node n1 = allyRecepNodes.get(0);
        Pdg.Node n2 = allyRecepNodes.get(1);
        Pdg.Node n3 = allyRecepNodes.get(2);
        Pdg.Node n4 = allyRecepNodes.get(3);
        Pdg.Node n5 = goalNode;

        MockCompute mockCompute = new MockCompute(PDG);
        mockCompute.setProb(n0, n1, 0.95);
        mockCompute.setProb(n0, n2, 0.9);
        mockCompute.setProb(n0, n3, 0.8);
        mockCompute.setProb(n0, n4, 0.7);
        mockCompute.setProb(n0, n5, 0.111);
        mockCompute.setProb(n1, n2, 0.5);
        mockCompute.setProb(n1, n3, 0.85);
        mockCompute.setProb(n1, n4, 0.3);
        mockCompute.setProb(n1, n5, 0.2);
        mockCompute.setProb(n2, n3, 0.4);
        mockCompute.setProb(n2, n4, 0.8);
        mockCompute.setProb(n2, n5, 0.3);
        mockCompute.setProb(n3, n4, 0.4);
        mockCompute.setProb(n3, n5, 0.75);
        mockCompute.setProb(n4, n5, 0.7);

        mockCompute.setAngle(n0, n1, 0.95);
        mockCompute.setAngle(n0, n2, 0.9);
        mockCompute.setAngle(n0, n3, 0.8);
        mockCompute.setAngle(n0, n4, 0.7);
        mockCompute.setAngle(n0, n5, 0.1);
        mockCompute.setAngle(n1, n2, 0.5);
        mockCompute.setAngle(n1, n3, 0.85);
        mockCompute.setAngle(n1, n4, 0.3);
        mockCompute.setAngle(n1, n5, 0.2);
        mockCompute.setAngle(n2, n3, 0.4);
        mockCompute.setAngle(n2, n4, 0.8);
        mockCompute.setAngle(n2, n5, 0.3);
        mockCompute.setAngle(n3, n4, 0.4);
        mockCompute.setAngle(n3, n5, 0.75);
        mockCompute.setAngle(n4, n5, 0.7);

        mockCompute.setKickVec(n0, n1, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n0, n2, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n0, n3, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n0, n4, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n0, n5, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n1, n2, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n1, n3, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n1, n4, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n1, n5, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n2, n3, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n2, n4, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n2, n5, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n3, n4, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n3, n5, new Vec2D(0.3, 0.4));
        mockCompute.setKickVec(n4, n5, new Vec2D(0.3, 0.4));

        Vec2D oldPassPoint = new Vec2D(0.3, 0.4);
        mockCompute.setPasspoint(n0, n1, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n0, n2, oldPassPoint);
        mockCompute.setPasspoint(n0, n3, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n0, n4, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n0, n5, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n1, n2, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n1, n3, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n1, n4, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n1, n5, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n2, n3, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n2, n4, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n2, n5, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n3, n4, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n3, n5, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n4, n5, new Vec2D(0.3, 0.4));

        mockCompute.setRecepPoint(n0, n1, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n0, n2, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n0, n3, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n0, n4, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n0, n5, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n1, n2, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n1, n3, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n1, n4, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n1, n5, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n2, n3, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n2, n4, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n2, n5, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n3, n4, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n3, n5, new Vec2D(0.3, 0.4));
        mockCompute.setRecepPoint(n4, n5, new Vec2D(0.3, 0.4));

        TritonDijkstra tritonDijkstra = new TritonDijkstra(PDG, mockCompute, so);
        TritonDijkstra.AttackPathInfo optimalPath = tritonDijkstra.compute();
        double actual = optimalPath.getTotalProbabilityProduct();
        ArrayList<Pdg.Node> maxProbPath = optimalPath.getMaxProbPath();
        System.out.printf("Return optimal path: [%s]\n", optimalPath.pathString());

        Pdg.Node node = maxProbPath.get(0);
        Pdg.Node node1 = maxProbPath.get(1);
        Pdg.Node node2 = maxProbPath.get(2);
        Pdg.Node node3 = maxProbPath.get(3);

        boolean b = TestUtil.testReferenceEq("Test if start node is pass node", Pdg.AllyPassNode.class, node.getClass());
        boolean b1 = TestUtil.testReferenceEq("Test if node1 is recep node", Pdg.AllyRecepNode.class, node1.getClass());
        boolean b2 = TestUtil.testReferenceEq("Test if node2 is recep node", Pdg.AllyRecepNode.class, node2.getClass());
        boolean b3 = TestUtil.testReferenceEq("Test if node3 is goal node", Pdg.GoalNode.class, node3.getClass());

        if(!(b && b1 && b2 && b3)){
            return false;
        }

        Pdg.AllyPassNode passNode = (Pdg.AllyPassNode) node;
        Pdg.AllyRecepNode recepNode1 = (Pdg.AllyRecepNode) node1;
        Pdg.AllyRecepNode recepNode2 = (Pdg.AllyRecepNode) node2;
        Pdg.GoalNode endGoalNode = (Pdg.GoalNode) node3;

        Vec2D passPoint = passNode.getPassPoint();
        Vec2D receptionPoint1 = recepNode1.getReceptionPoint();
        Vec2D receptionPoint2 = recepNode2.getReceptionPoint();
        Vec2D goalCenter = endGoalNode.getGoalCenter();

        TestUtil.testVec2dEq("Test if start node has correct pass point", new Vec2D(0.3, 0.4), passPoint, 0.001);
        TestUtil.testVec2dEq("Test if node1 has correct recep point #1", new Vec2D(0.3, 0.4), receptionPoint1, 0.001);
        TestUtil.testVec2dEq("Test if node1 has correct recep point #2", new Vec2D(0.3, 0.4), receptionPoint2, 0.001);
        TestUtil.testVec2dEq("Test if node1 has correct goal center #3", new Vec2D(0, 4500), goalCenter, 0.001);
        TestUtil.testDoubleEq("Test if correct total prob is returned #0", 0.605625, actual, 0.01);

        mockCompute.setProb(n0, n5, 0.99);
        mockCompute.setPasspoint(n0, n5, new Vec2D(200, 300));
        TritonDijkstra tritonDijkstra1 = new TritonDijkstra(PDG, mockCompute, so);
        TritonDijkstra.AttackPathInfo optimalPath1 = tritonDijkstra1.compute();
        double actual1 = optimalPath1.getTotalProbabilityProduct();
        ArrayList<Pdg.Node> maxProbPath1 = optimalPath1.getMaxProbPath();
        Pdg.Node node11 = maxProbPath1.get(0);
        Pdg.Node node12 = maxProbPath1.get(1);

        boolean b11 = TestUtil.testReferenceEq("Test if node11 is pass node", Pdg.AllyPassNode.class, node11.getClass());
        boolean b12 = TestUtil.testReferenceEq("Test if Node12 is goal node", Pdg.GoalNode.class, node12.getClass());

        if(! (b11 && b12)){
            return false;
        }

        Pdg.AllyPassNode passNode1 = (Pdg.AllyPassNode) node11;
        Pdg.GoalNode endGoalNode1 = (Pdg.GoalNode) node12;

        TestUtil.testIntEq("Test if returned path has only 2 nodes", 2, maxProbPath1.size());
        TestUtil.testVec2dEq("Test if start node has correct pass point", new Vec2D(200, 300), passNode1.getPassPoint(), 0.001);
        TestUtil.testVec2dEq("Test if node1 has correct goal center #3", new Vec2D(0, 4500), endGoalNode1.getGoalCenter(), 0.001);
        TestUtil.testDoubleEq("Test if correct total prob is returned #1", 0.99, actual1, 0.001);

        mockCompute.setAllProb(0.2);
        mockCompute.setProb(n0, n2, 0.99);
        mockCompute.setKickVec(n0, n2, new Vec2D(222.22, 333.33));
        mockCompute.setProb(n2, n1, 0.99);
        mockCompute.setRecepPoint(n2, n1, new Vec2D(200, 100));
        mockCompute.setProb(n1, n4, 0.99);
        mockCompute.setRecepPoint(n1, n4, new Vec2D(100, 400));
        mockCompute.setProb(n4, n3, 0.99);
        mockCompute.setRecepPoint(n4, n3, new Vec2D(400, 300));
        mockCompute.setAngle(n4, n3, 42.5);
        mockCompute.setProb(n3, n5, 0.99);
        mockCompute.setRecepPoint(n3, n5, new Vec2D(300, 500));

        TritonDijkstra tritonDijkstra2 = new TritonDijkstra(PDG, mockCompute, so);
        TritonDijkstra.AttackPathInfo optimalPath2 = tritonDijkstra2.compute();
        ArrayList<Pdg.Node> maxProbPath2 = optimalPath2.getMaxProbPath();
        TestUtil.testIntEq("Test if returned path has 6 nodes", 6, maxProbPath2.size());

        Pdg.Node node20 = maxProbPath2.get(0);
        Pdg.Node node21 = maxProbPath2.get(1);
        Pdg.Node node22 = maxProbPath2.get(2);
        Pdg.Node node23 = maxProbPath2.get(3);
        Pdg.Node node24 = maxProbPath2.get(4);
        Pdg.Node node25 = maxProbPath2.get(5);

        boolean b20 = TestUtil.testReferenceEq("Test if node20 is pass node", Pdg.AllyPassNode.class, node20.getClass());
        boolean b21 = TestUtil.testReferenceEq("Test if Node21 is recep node", Pdg.AllyRecepNode.class, node21.getClass());
        boolean b22 = TestUtil.testReferenceEq("Test if Node22 is recep node", Pdg.AllyRecepNode.class, node22.getClass());
        boolean b23 = TestUtil.testReferenceEq("Test if Node23 is recep node", Pdg.AllyRecepNode.class, node23.getClass());
        boolean b24 = TestUtil.testReferenceEq("Test if Node24 is recep node", Pdg.AllyRecepNode.class, node24.getClass());
        boolean b25 = TestUtil.testReferenceEq("Test if Node25 is goal node", Pdg.GoalNode.class, node25.getClass());

        if(!(b20 && b21 && b22 && b23 && b24 && b25)){
            return false;
        }

        Pdg.AllyPassNode passNode20 = (Pdg.AllyPassNode) node20;
        Pdg.AllyRecepNode recepNode21 = (Pdg.AllyRecepNode) node21;
        Pdg.AllyRecepNode recepNode22 = (Pdg.AllyRecepNode) node22;
        Pdg.AllyRecepNode recepNode23 = (Pdg.AllyRecepNode) node23;
        Pdg.AllyRecepNode recepNode24 = (Pdg.AllyRecepNode) node24;
        Pdg.GoalNode goalNode25 = (Pdg.GoalNode) node25;

        Vec2D passPoint20 = passNode20.getPassPoint();
        Vec2D kickVec20 = passNode20.getKickVec();
        Vec2D receptionPoint21 = recepNode21.getReceptionPoint();
        Vec2D receptionPoint22 = recepNode22.getReceptionPoint();
        Vec2D receptionPoint23 = recepNode23.getReceptionPoint();
        double angle24 = recepNode24.getAngle();
        Vec2D receptionPoint24 = recepNode24.getReceptionPoint();
        Vec2D goalCenter25 = goalNode25.getGoalCenter();

        TestUtil.testVec2dEq("Test if start node pass point is correct", new Vec2D(0.3, 0.4), passPoint20, 0.001);
        TestUtil.testVec2dEq("Test if start node kick vec is correct", new Vec2D(222.22, 333.33), kickVec20, 0.001);
        TestUtil.testVec2dEq("Test if node21 has correct recep point", new Vec2D(0.3, 0.4), receptionPoint21, 0.001);
        TestUtil.testVec2dEq("Test if node22 has correct recep point", new Vec2D(200, 100), receptionPoint22, 0.001);
        TestUtil.testVec2dEq("Test if node23 has correct recep point", new Vec2D(100, 400), receptionPoint23, 0.001);
        TestUtil.testDoubleEq("Test if node24 has correct angle", 42.5, angle24, 0.001);
        TestUtil.testVec2dEq("Test if node24 has correct recep point", new Vec2D(400, 300), receptionPoint24, 0.001);
        TestUtil.testVec2dEq("Test if end node has correct goal center", new Vec2D(0, 4500), goalCenter25, 0.001);

        double totalProbabilityProduct = optimalPath2.getTotalProbabilityProduct();
        TestUtil.testDoubleEq("Test if optimalPath2 has correct total prob", 0.95099, totalProbabilityProduct, 0.001);

        TestUtil.enterKeyToContinue();

        return true;

    }
}
