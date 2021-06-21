package Triton.CoreModules.AI.TritonProbDijkstra.ComputableImpl;

import Triton.CoreModules.AI.TritonProbDijkstra.Computables.DijkCompute;
import Triton.CoreModules.AI.TritonProbDijkstra.Exceptions.NonExistentNodeException;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.RWLockee;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@Getter
@Setter
public class MockCompute implements DijkCompute {
    private HashMap<PUAG.Node, Integer> nodeToIndexMap;
    private final double[][] probMatrix;
    private final double[][] angleMatrix;
    private final Vec2D[][] kickVecMatrix;
    private final Vec2D[][] passPointMatrix;
    private final Vec2D[][] recepPointMatrix;


    private PUAG graph;

    public MockCompute(PUAG graph) {
        this.graph = graph;
        nodeToIndexMap = graph.getNodeToIndexMap();

        assert graph.getNumNodes() == nodeToIndexMap.size();

        int workingSize = nodeToIndexMap.size();

        probMatrix = new double[workingSize][workingSize];
        angleMatrix = new double[workingSize][workingSize];
        kickVecMatrix = new Vec2D[workingSize][workingSize];
        passPointMatrix = new Vec2D[workingSize][workingSize];
        recepPointMatrix = new Vec2D[workingSize][workingSize];

        for (int i = 0; i < workingSize; i++) {
            for (int j = 0; j < workingSize; j++) {
                kickVecMatrix[i][j] = new Vec2D(0, 0);
                passPointMatrix[i][j] = new Vec2D(0, 0);
                recepPointMatrix[i][j] = new Vec2D(0, 0);
            }
        }
    }

    public int getIndexOfNode(PUAG.Node n){
        Integer integer = nodeToIndexMap.get(n);
        if(integer == null){
            throw new NonExistentNodeException(n);
        }
        return integer;
    }

    public boolean setProb(PUAG.Node n1, PUAG.Node n2, double prob) {
        try {
            probMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = prob;
            probMatrix[getIndexOfNode(n2)][getIndexOfNode(n1)] = prob;
        } catch (IndexOutOfBoundsException e){
            return false;
        }
        return true;
    }

    public void setAllProb(double prob){
        for (int i = 0; i < probMatrix.length; i++) {
            for (int j = 0; j < probMatrix.length; j++) {
                probMatrix[i][j] = prob;
            }
        }
    }

    public void setAngle(PUAG.Node n1, PUAG.Node n2, double angle) {
        int indexOfNode1 = getIndexOfNode(n1);
        int indexOfNode2 = getIndexOfNode(n2);

        angleMatrix[indexOfNode1][indexOfNode2] = angle;
        angleMatrix[indexOfNode2][indexOfNode1] = angle;
    }

    public void setKickVec(PUAG.Node n1, PUAG.Node n2, Vec2D kickVec) {
        kickVecMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = kickVec;
        kickVecMatrix[getIndexOfNode(n2)][getIndexOfNode(n1)] = kickVec;

    }

    public void setPasspoint(PUAG.Node n1, PUAG.Node n2, Vec2D passpoint) {
        passPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = passpoint;
        passPointMatrix[getIndexOfNode(n2)][getIndexOfNode(n1)] = passpoint;
    }

    public void setRecepPoint(PUAG.Node n1, PUAG.Node n2, Vec2D recepPoint) {
        recepPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = recepPoint;
        recepPointMatrix[getIndexOfNode(n2)][getIndexOfNode(n1)] = recepPoint;
    }

    public void mock(RobotList<Ally> fielders){
        Ally startAlly = fielders.get(0);

        PUAG.AllyPassNode allyPassNode = new PUAG.AllyPassNode(startAlly);
        ArrayList<PUAG.Node> allyRecepNodes = new ArrayList<>();

        for (int i = 1; i < fielders.size(); i++) {
            allyRecepNodes.add(new PUAG.AllyRecepNode(fielders.get(i)));
        }

        PUAG.GoalNode goalNode = new PUAG.GoalNode();

        PUAG.Node n0 = allyPassNode;
        PUAG.Node n1 = allyRecepNodes.get(0);
        PUAG.Node n2 = allyRecepNodes.get(1);
        PUAG.Node n3 = allyRecepNodes.get(2);
        PUAG.Node n4 = allyRecepNodes.get(3);
        PUAG.GoalNode n5 = goalNode;


        this.setProb(n0, n1, 0.95);
        this.setProb(n0, n2, 0.9);
        this.setProb(n0, n3, 0.8);
        this.setProb(n0, n4, 0.7);
        this.setProb(n0, n5, 0.111);
        this.setProb(n1, n2, 0.5);
        this.setProb(n1, n3, 0.85);
        this.setProb(n1, n4, 0.3);
        this.setProb(n1, n5, 0.2);
        this.setProb(n2, n3, 0.4);
        this.setProb(n2, n4, 0.8);
        this.setProb(n2, n5, 0.3);
        this.setProb(n3, n4, 0.4);
        this.setProb(n3, n5, 0.75);
        this.setProb(n4, n5, 0.7);

        Ally bot0 = n0.getBot();
        Ally bot1 = n1.getBot();
        Ally bot2 = n2.getBot();
        Ally bot3 = n3.getBot();
        Ally bot4 = n4.getBot();

        assert bot0 != null;
        assert bot1 != null;
        assert bot2 != null;
        assert bot3 != null;
        assert bot4 != null;


        this.setAngle(n0, n1, bot1.getDir() - bot0.getDir()               );
        this.setAngle(n0, n2, bot2.getDir() - bot0.getDir()               );
        this.setAngle(n0, n3, bot3.getDir() - bot0.getDir()               );
        this.setAngle(n0, n4, bot4.getDir() - bot0.getDir()               );
        this.setAngle(n0, n5, n5.getGoalCenter().toAngle() - bot0.getDir());
        this.setAngle(n1, n2, bot2.getDir() - bot1.getDir()               );
        this.setAngle(n1, n3, bot3.getDir() - bot1.getDir()               );
        this.setAngle(n1, n4, bot4.getDir() - bot1.getDir()               );
        this.setAngle(n1, n5, n5.getGoalCenter().toAngle() - bot1.getDir());
        this.setAngle(n2, n3, bot3.getDir() - bot2.getDir()               );
        this.setAngle(n2, n4, bot4.getDir() - bot2.getDir()               );
        this.setAngle(n2, n5, n5.getGoalCenter().toAngle() - bot2.getDir());
        this.setAngle(n3, n4, bot4.getDir() - bot3.getDir()               );
        this.setAngle(n3, n5, n5.getGoalCenter().toAngle() - bot3.getDir());
        this.setAngle(n4, n5, n5.getGoalCenter().toAngle() - bot4.getDir());

        this.setKickVec(n0, n1, bot1.getPos().sub(bot0.getPos())     );
        this.setKickVec(n0, n2, bot2.getPos().sub(bot0.getPos())     );
        this.setKickVec(n0, n3, bot3.getPos().sub(bot0.getPos())     );
        this.setKickVec(n0, n4, bot4.getPos().sub(bot0.getPos())     );
        this.setKickVec(n0, n5, n5.getGoalCenter().sub(bot0.getPos()));
        this.setKickVec(n1, n2, bot2.getPos().sub(bot1.getPos())     );
        this.setKickVec(n1, n3, bot3.getPos().sub(bot1.getPos())     );
        this.setKickVec(n1, n4, bot4.getPos().sub(bot1.getPos())     );
        this.setKickVec(n1, n5, n5.getGoalCenter().sub(bot1.getPos()));
        this.setKickVec(n2, n3, bot3.getPos().sub(bot2.getPos())     );
        this.setKickVec(n2, n4, bot4.getPos().sub(bot2.getPos())     );
        this.setKickVec(n2, n5, n5.getGoalCenter().sub(bot2.getPos()));
        this.setKickVec(n3, n4, bot4.getPos().sub(bot3.getPos())     );
        this.setKickVec(n3, n5, n5.getGoalCenter().sub(bot3.getPos()));
        this.setKickVec(n4, n5, n5.getGoalCenter().sub(bot4.getPos()));

        this.setPasspoint(n0, n1, bot1.getPos().sub(bot0.getPos())          );
        this.setPasspoint(n0, n2, bot2.getPos().sub(bot0.getPos())          );
        this.setPasspoint(n0, n3, bot3.getPos().sub(bot0.getPos())          );
        this.setPasspoint(n0, n4, bot4.getPos().sub(bot0.getPos())          );
        this.setPasspoint(n0, n5, n5.getGoalCenter().sub(bot0.getPos())     );
        this.setPasspoint(n1, n2, bot2.getPos().sub(bot1.getPos())          );
        this.setPasspoint(n1, n3, bot3.getPos().sub(bot1.getPos())          );
        this.setPasspoint(n1, n4, bot4.getPos().sub(bot1.getPos())          );
        this.setPasspoint(n1, n5, n5.getGoalCenter().sub(bot1.getPos())     );
        this.setPasspoint(n2, n3, bot3.getPos().sub(bot2.getPos())          );
        this.setPasspoint(n2, n4, bot4.getPos().sub(bot2.getPos())          );
        this.setPasspoint(n2, n5, n5.getGoalCenter().sub(bot2.getPos())     );
        this.setPasspoint(n3, n4, bot4.getPos().sub(bot3.getPos())          );
        this.setPasspoint(n3, n5, n5.getGoalCenter().sub(bot3.getPos())     );
        this.setPasspoint(n4, n5, n5.getGoalCenter().sub(bot4.getPos())     );

        this.setRecepPoint(n0, n1, bot1.getPos().sub(bot0.getPos())     .add(1000, 1000));
        this.setRecepPoint(n0, n2, bot2.getPos().sub(bot0.getPos())     .add(1000, 1000));
        this.setRecepPoint(n0, n3, bot3.getPos().sub(bot0.getPos())     .add(1000, 1000));
        this.setRecepPoint(n0, n4, bot4.getPos().sub(bot0.getPos())     .add(1000, 1000));
        this.setRecepPoint(n0, n5, n5.getGoalCenter().sub(bot0.getPos()).add(1000, 1000));
        this.setRecepPoint(n1, n2, bot2.getPos().sub(bot1.getPos())     .add(1000, 1000));
        this.setRecepPoint(n1, n3, bot3.getPos().sub(bot1.getPos())     .add(1000, 1000));
        this.setRecepPoint(n1, n4, bot4.getPos().sub(bot1.getPos())     .add(1000, 1000));
        this.setRecepPoint(n1, n5, n5.getGoalCenter().sub(bot1.getPos()).add(1000, 1000));
        this.setRecepPoint(n2, n3, bot3.getPos().sub(bot2.getPos())     .add(1000, 1000));
        this.setRecepPoint(n2, n4, bot4.getPos().sub(bot2.getPos())     .add(1000, 1000));
        this.setRecepPoint(n2, n5, n5.getGoalCenter().sub(bot2.getPos()).add(1000, 1000));
        this.setRecepPoint(n3, n4, bot4.getPos().sub(bot3.getPos())     .add(1000, 1000));
        this.setRecepPoint(n3, n5, n5.getGoalCenter().sub(bot3.getPos()).add(1000, 1000));
        this.setRecepPoint(n4, n5, n5.getGoalCenter().sub(bot4.getPos()).add(1000, 1000));
    }

    @Override
    public double computeProb(PUAG.Node n1, PUAG.Node n2) {
        return probMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public double computeGoalProb(PUAG.Node n) {
        return 1.0;
    }

    @Override
    public double computeAngle(PUAG.Node n1, PUAG.Node n2) {
        return angleMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public Vec2D computeKickVec(PUAG.Node n1, PUAG.Node n2) {
        return kickVecMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public Vec2D computePasspoint(PUAG.Node n1, PUAG.Node n2) {
        return passPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public Vec2D computeRecepPoint(PUAG.Node n1, PUAG.Node n2) {
        return recepPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
    }

    @Override
    public Vec2D computeGoalCenter(PUAG.Node n) {
        if(n.getClass() == PUAG.GoalNode.class) {
            return ((PUAG.GoalNode) n).getGoalCenter();
        }
        return null;
    }

    @Override
    public void setSnapShots(ArrayList<RobotSnapshot> allySnaps, ArrayList<RobotSnapshot> foeSnaps, RWLockee<Vec2D> ballSnap) {

    }
}
