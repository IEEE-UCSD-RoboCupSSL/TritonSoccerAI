package Triton.ManualTests.CoreTests.DijkstraTest;

import Triton.Config.Config;
import Triton.CoreModules.AI.TritonProbDijkstra.ComputableImpl.MockCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.AI.TritonProbDijkstra.TritonDijkstra;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TestUtil.TestUtil;
import Triton.ManualTests.TritonTestable;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.SoccerObjects;
import lombok.Data;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;

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
    public boolean test(Config config) {
        RobotList<Ally> fielders = so.fielders;
        Ally startAlly = fielders.get(0);


        PUAG.AllyPassNode allyPassNode = new PUAG.AllyPassNode(startAlly);
        ArrayList<PUAG.Node> allyRecepNodes = new ArrayList<>();

        for (int i = 1; i < fielders.size(); i++) {
            allyRecepNodes.add(new PUAG.AllyRecepNode(fielders.get(i)));
        }

        PUAG.GoalNode goalNode = new PUAG.GoalNode();

        PUAG puag = new PUAG(allyPassNode, goalNode, allyRecepNodes);

        PUAG.Node n0 = allyPassNode;
        PUAG.Node n1 = allyRecepNodes.get(0);
        PUAG.Node n2 = allyRecepNodes.get(1);
        PUAG.Node n3 = allyRecepNodes.get(2);
        PUAG.Node n4 = allyRecepNodes.get(3);
        PUAG.Node n5 = goalNode;

        MockCompute mockCompute = new MockCompute(puag);
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

        mockCompute.setPasspoint(n0, n1, new Vec2D(0.3, 0.4));
        mockCompute.setPasspoint(n0, n2, new Vec2D(0.3, 0.4));
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


        TritonDijkstra tritonDijkstra = new TritonDijkstra(puag, mockCompute);
        TritonDijkstra.AttackPathInfo optimalPath = tritonDijkstra.compute();
        double actual = optimalPath.getTotalProbabilityProduct();

        TestUtil.testDoublesEq("Test if correct total prob is returned 0", 0.605625, actual, 0.01);

        mockCompute.setProb(n0, n5, 0.99);
        TritonDijkstra tritonDijkstra1 = new TritonDijkstra(puag, mockCompute);
        TritonDijkstra.AttackPathInfo optimalPath1 = tritonDijkstra1.compute();
        double actual1 = optimalPath1.getTotalProbabilityProduct();

        TestUtil.testDoublesEq("Test if correct total prob is returned 1", 0.99, actual1, 0.001);

        mockCompute.setAllProb(0.2);
        mockCompute.setProb(n0, n2, 0.99);
        mockCompute.setProb(n2, n1, 0.99);
        mockCompute.setProb(n1, n4, 0.99);
        mockCompute.setProb(n4, n3, 0.99);
        mockCompute.setProb(n3, n5, 0.99);



        TestUtil.enterKeyToContinue();

        return true;

    }
}
