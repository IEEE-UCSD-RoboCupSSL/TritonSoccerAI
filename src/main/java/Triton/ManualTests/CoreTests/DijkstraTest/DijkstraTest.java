package Triton.ManualTests.CoreTests.DijkstraTest;

import Triton.Config.Config;
import Triton.CoreModules.AI.TritonProbDijkstra.ComputableImpl.MockCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.*;
import Triton.CoreModules.AI.TritonProbDijkstra.PDG;
import Triton.CoreModules.AI.TritonProbDijkstra.TritonDijkstra;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TestUtil.TestUtil;
import Triton.ManualTests.TritonTestable;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.SoccerObjects;
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


        PDG.AllyPassNode allyPassNode = new PDG.AllyPassNode(startAlly);
        ArrayList<PDG.Node> allyRecepNodes = new ArrayList<>();

        for (int i = 1; i < fielders.size(); i++) {
            allyRecepNodes.add(new PDG.AllyRecepNode(fielders.get(i)));
        }

        PDG.GoalNode goalNode = new PDG.GoalNode();

        PDG PDG = new PDG(allyPassNode, goalNode, allyRecepNodes);

        PDG.Node n0 = allyPassNode;
        PDG.Node n1 = allyRecepNodes.get(0);
        PDG.Node n2 = allyRecepNodes.get(1);
        PDG.Node n3 = allyRecepNodes.get(2);
        PDG.Node n4 = allyRecepNodes.get(3);
        PDG.Node n5 = goalNode;

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
        ArrayList<PDG.Node> maxProbPath = optimalPath.getMaxProbPath();
        System.out.printf("Return optimal path: [%s]\n", optimalPath.pathString());

        PDG.Node node = maxProbPath.get(0);
        PDG.Node node1 = maxProbPath.get(1);
        PDG.Node node2 = maxProbPath.get(2);
        PDG.Node node3 = maxProbPath.get(3);

        boolean b = TestUtil.testReferenceEq("Test if start node is pass node", PDG.AllyPassNode.class, node.getClass());
        boolean b1 = TestUtil.testReferenceEq("Test if node1 is recep node", PDG.AllyRecepNode.class, node1.getClass());
        boolean b2 = TestUtil.testReferenceEq("Test if node2 is recep node", PDG.AllyRecepNode.class, node2.getClass());
        boolean b3 = TestUtil.testReferenceEq("Test if node3 is goal node", PDG.GoalNode.class, node3.getClass());

        if(!(b && b1 && b2 && b3)){
            return false;
        }

        PDG.AllyPassNode passNode = (PDG.AllyPassNode) node;
        PDG.AllyRecepNode recepNode1 = (PDG.AllyRecepNode) node1;
        PDG.AllyRecepNode recepNode2 = (PDG.AllyRecepNode) node2;
        PDG.GoalNode endGoalNode = (PDG.GoalNode) node3;

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
        ArrayList<PDG.Node> maxProbPath1 = optimalPath1.getMaxProbPath();
        PDG.Node node11 = maxProbPath1.get(0);
        PDG.Node node12 = maxProbPath1.get(1);

        boolean b11 = TestUtil.testReferenceEq("Test if node11 is pass node", PDG.AllyPassNode.class, node11.getClass());
        boolean b12 = TestUtil.testReferenceEq("Test if Node12 is goal node", PDG.GoalNode.class, node12.getClass());

        if(! (b11 && b12)){
            return false;
        }

        PDG.AllyPassNode passNode1 = (PDG.AllyPassNode) node11;
        PDG.GoalNode endGoalNode1 = (PDG.GoalNode) node12;

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
        ArrayList<PDG.Node> maxProbPath2 = optimalPath2.getMaxProbPath();
        TestUtil.testIntEq("Test if returned path has 6 nodes", 6, maxProbPath2.size());

        PDG.Node node20 = maxProbPath2.get(0);
        PDG.Node node21 = maxProbPath2.get(1);
        PDG.Node node22 = maxProbPath2.get(2);
        PDG.Node node23 = maxProbPath2.get(3);
        PDG.Node node24 = maxProbPath2.get(4);
        PDG.Node node25 = maxProbPath2.get(5);

        boolean b20 = TestUtil.testReferenceEq("Test if node20 is pass node", PDG.AllyPassNode.class, node20.getClass());
        boolean b21 = TestUtil.testReferenceEq("Test if Node21 is recep node", PDG.AllyRecepNode.class, node21.getClass());
        boolean b22 = TestUtil.testReferenceEq("Test if Node22 is recep node", PDG.AllyRecepNode.class, node22.getClass());
        boolean b23 = TestUtil.testReferenceEq("Test if Node23 is recep node", PDG.AllyRecepNode.class, node23.getClass());
        boolean b24 = TestUtil.testReferenceEq("Test if Node24 is recep node", PDG.AllyRecepNode.class, node24.getClass());
        boolean b25 = TestUtil.testReferenceEq("Test if Node25 is goal node", PDG.GoalNode.class, node25.getClass());

        if(!(b20 && b21 && b22 && b23 && b24 && b25)){
            return false;
        }

        PDG.AllyPassNode passNode20 = (PDG.AllyPassNode) node20;
        PDG.AllyRecepNode recepNode21 = (PDG.AllyRecepNode) node21;
        PDG.AllyRecepNode recepNode22 = (PDG.AllyRecepNode) node22;
        PDG.AllyRecepNode recepNode23 = (PDG.AllyRecepNode) node23;
        PDG.AllyRecepNode recepNode24 = (PDG.AllyRecepNode) node24;
        PDG.GoalNode goalNode25 = (PDG.GoalNode) node25;

        Vec2D passPoint20 = passNode20.getPassPoint();
        Vec2D kickVec20 = passNode20.getKickVec();
        Vec2D receptionPoint21 = recepNode21.getReceptionPoint();
        Vec2D receptionPoint22 = recepNode22.getReceptionPoint();
        Vec2D receptionPoint23 = recepNode23.getReceptionPoint();
        double angle23 = recepNode23.getAngle();
        Vec2D receptionPoint24 = recepNode24.getReceptionPoint();
        Vec2D goalCenter25 = goalNode25.getGoalCenter();

        TestUtil.testVec2dEq("Test if start node pass point is correct", new Vec2D(0.3, 0.4), passPoint20, 0.001);
        TestUtil.testVec2dEq("Test if start node kick vec is correct", new Vec2D(222.22, 333.33), kickVec20, 0.001);
        TestUtil.testVec2dEq("Test if node21 has correct recep point", new Vec2D(200, 100), receptionPoint21, 0.001);
        TestUtil.testVec2dEq("Test if node22 has correct recep point", new Vec2D(100, 400), receptionPoint22, 0.001);
        TestUtil.testVec2dEq("Test if node23 has correct recep point", new Vec2D(400, 300), receptionPoint23, 0.001);
        TestUtil.testDoubleEq("Test if node23 has correct angle", 42.5, angle23, 0.001);
        TestUtil.testVec2dEq("Test if node24 has correct recep point", new Vec2D(300, 500), receptionPoint24, 0.001);
        TestUtil.testVec2dEq("Test if end node has correct goal center", new Vec2D(0, 4500), goalCenter25, 0.001);

        double totalProbabilityProduct = optimalPath2.getTotalProbabilityProduct();
        TestUtil.testDoubleEq("Test if optimalPath2 has correct total prob", 0.95099, totalProbabilityProduct, 0.001);

        TestUtil.enterKeyToContinue();

        return true;

    }
}
