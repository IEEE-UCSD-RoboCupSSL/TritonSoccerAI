package triton.coreModules.ai.dijkstra.computableImpl;

import triton.coreModules.ai.dijkstra.computables.DijkCompute;
import triton.coreModules.ai.dijkstra.exceptions.NonExistentNodeException;
import triton.coreModules.ai.dijkstra.Pdg;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.RobotList;
import triton.coreModules.robot.RobotSnapshot;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.RWLockee;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
@Setter
public class MockCompute implements DijkCompute {
    private HashMap<Pdg.Node, Integer> nodeToIndexMap;
    private Pdg.GoalNode goalNode;
    private final double[][] probMatrix;
    private final double[][] angleMatrix;
    private final Vec2D[][] kickVecMatrix;
    private final Vec2D[][] passPointMatrix;
    private final Vec2D[][] recepPointMatrix;


    private Pdg graph;

    public MockCompute(Pdg graph) {
        this.graph = graph;
        nodeToIndexMap = graph.getNodeToIndexMap();
        for (Pdg.Node node : graph.getNodeSet()) {
            if(node.getClass() == Pdg.GoalNode.class){
                this.goalNode = (Pdg.GoalNode)node;
            }
        }

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

    public int getIndexOfNode(Pdg.Node n) throws NonExistentNodeException {
        Integer integer = nodeToIndexMap.get(n);
        if(integer == null){
            throw new NonExistentNodeException(n);
        }
        return integer;
    }

    public boolean setProb(Pdg.Node n1, Pdg.Node n2, double prob) {
        try {
            probMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = prob;
            probMatrix[getIndexOfNode(n2)][getIndexOfNode(n1)] = prob;
        } catch (IndexOutOfBoundsException | NonExistentNodeException e){
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

    public void setAngle(Pdg.Node n1, Pdg.Node n2, double angle) throws NonExistentNodeException {
        int indexOfNode1 = getIndexOfNode(n1);
        int indexOfNode2 = getIndexOfNode(n2);

        angleMatrix[indexOfNode1][indexOfNode2] = angle;
        angleMatrix[indexOfNode2][indexOfNode1] = angle;
    }

    public void setKickVec(Pdg.Node n1, Pdg.Node n2, Vec2D kickVec) {
        try {
            kickVecMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = kickVec;
            kickVecMatrix[getIndexOfNode(n2)][getIndexOfNode(n1)] = kickVec;
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }

    }

    public void setPasspoint(Pdg.Node n1, Pdg.Node n2, Vec2D passpoint) {
        try {
            passPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = passpoint;
            passPointMatrix[getIndexOfNode(n2)][getIndexOfNode(n1)] = passpoint;
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }

    }

    public void setRecepPoint(Pdg.Node n1, Pdg.Node n2, Vec2D recepPoint) {
        try {
            recepPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)] = recepPoint;
            recepPointMatrix[getIndexOfNode(n2)][getIndexOfNode(n1)] = recepPoint;
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }

    }

    public void mock(RobotList<Ally> fielders){
        Ally startAlly = fielders.get(0);

        Pdg.AllyPassNode allyPassNode = new Pdg.AllyPassNode(startAlly);
        ArrayList<Pdg.Node> allyRecepNodes = new ArrayList<>();

        for (int i = 1; i < fielders.size(); i++) {
            allyRecepNodes.add(new Pdg.AllyRecepNode(fielders.get(i)));
        }

        Pdg.GoalNode goalNode = new Pdg.GoalNode();

        Pdg.Node n0 = allyPassNode;
        Pdg.Node n1 = allyRecepNodes.get(0);
        Pdg.Node n2 = allyRecepNodes.get(1);
        Pdg.Node n3 = allyRecepNodes.get(2);
        Pdg.Node n4 = allyRecepNodes.get(3);
        Pdg.GoalNode n5 = goalNode;


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


        try {
            this.setAngle(n0, n1, 30               );
            this.setAngle(n0, n2, bot2.getDir() - bot0.getDir()               );
            this.setAngle(n0, n3, bot3.getDir() - bot0.getDir()               );
            this.setAngle(n0, n4, bot4.getDir() - bot0.getDir()               );
            this.setAngle(n0, n5, n5.getGoalCenter().toAngle() - bot0.getDir());
            this.setAngle(n1, n2, 0              );
            this.setAngle(n1, n3, 0             );
            this.setAngle(n1, n4, bot4.getDir() - bot1.getDir()               );
            this.setAngle(n1, n5, n5.getGoalCenter().toAngle() - bot1.getDir());
            this.setAngle(n2, n3, bot3.getDir() - bot2.getDir()               );
            this.setAngle(n2, n4, bot4.getDir() - bot2.getDir()               );
            this.setAngle(n2, n5, n5.getGoalCenter().toAngle() - bot2.getDir());
            this.setAngle(n3, n4, bot4.getDir() - bot3.getDir()               );
            this.setAngle(n3, n5, n5.getGoalCenter().toAngle() - bot3.getDir());
            this.setAngle(n4, n5, n5.getGoalCenter().toAngle() - bot4.getDir());
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }

        this.setKickVec(n0, n1, new Vec2D(2, 1));
        this.setKickVec(n0, n2, new Vec2D(2, 1));
        this.setKickVec(n0, n3, new Vec2D(2, 1));
        this.setKickVec(n0, n4, new Vec2D(2, 1));
        this.setKickVec(n0, n5, new Vec2D(2, 1));
        this.setKickVec(n1, n2, new Vec2D(2, 1));
        this.setKickVec(n1, n3, new Vec2D(2, 1));
        this.setKickVec(n1, n4, new Vec2D(2, 1));
        this.setKickVec(n1, n5, new Vec2D(2, 1));
        this.setKickVec(n2, n3, new Vec2D(2, 1));
        this.setKickVec(n2, n4, new Vec2D(2, 1));
        this.setKickVec(n2, n5, new Vec2D(2, 1));
        this.setKickVec(n3, n4, new Vec2D(2, 1));
        this.setKickVec(n3, n5, new Vec2D(2, 1));
        this.setKickVec(n4, n5, new Vec2D(2, 1));

        this.setPasspoint(n0, n1, n0.getBot().getPos());
        this.setPasspoint(n0, n2, n0.getBot().getPos());
        this.setPasspoint(n0, n3, n0.getBot().getPos());
        this.setPasspoint(n0, n4, n0.getBot().getPos());
        this.setPasspoint(n0, n5, n0.getBot().getPos());
        this.setPasspoint(n1, n2, n1.getBot().getPos());
        this.setPasspoint(n1, n3, n1.getBot().getPos());
        this.setPasspoint(n1, n4, n1.getBot().getPos());
        this.setPasspoint(n1, n5, n1.getBot().getPos());
        this.setPasspoint(n2, n3, n2.getBot().getPos());
        this.setPasspoint(n2, n4, n2.getBot().getPos());
        this.setPasspoint(n2, n5, n2.getBot().getPos());
        this.setPasspoint(n3, n4, n3.getBot().getPos());
        this.setPasspoint(n3, n5, n3.getBot().getPos());
        this.setPasspoint(n4, n5, n4.getBot().getPos());

        this.setRecepPoint(n0, n1, new Vec2D(-499, -999));
        this.setRecepPoint(n0, n2, bot2.getPos().sub(bot0.getPos())     .add(500, 1000).scale(0.1));
        this.setRecepPoint(n0, n3, bot3.getPos().sub(bot0.getPos())     .add(1000, 500).scale(0.1));
        this.setRecepPoint(n0, n4, bot4.getPos().sub(bot0.getPos())     .add(500, 1000).scale(0.1));
        this.setRecepPoint(n0, n5, n5.getGoalCenter().sub(bot0.getPos()).add(1000, 500).scale(0.1));
        this.setRecepPoint(n1, n2, new Vec2D(0, 500));
        this.setRecepPoint(n1, n3, bot3.getPos().sub(bot1.getPos())     .add(1000, 500).scale(0.1));
        this.setRecepPoint(n1, n4, bot4.getPos().sub(bot1.getPos())     .add(500, 1000).scale(0.1));
        this.setRecepPoint(n1, n5, n5.getGoalCenter().sub(bot1.getPos()).add(1000, 500).scale(0.1));
        this.setRecepPoint(n2, n3, new Vec2D(0, 3000));
        this.setRecepPoint(n2, n4, bot4.getPos().sub(bot2.getPos())     .add(1000, 500).scale(0.1));
        this.setRecepPoint(n2, n5, n5.getGoalCenter().sub(bot2.getPos()).add(500, 1000).scale(0.1));
        this.setRecepPoint(n3, n4, bot4.getPos().sub(bot3.getPos())     .add(1000, 500).scale(0.1));
        this.setRecepPoint(n3, n5, n5.getGoalCenter().sub(bot3.getPos()).add(500, 1000).scale(0.1));
        this.setRecepPoint(n4, n5, n5.getGoalCenter().sub(bot4.getPos()).add(1000, 500).scale(0.1));
    }

    @Override
    public double computeProb(Pdg.Node n1, Pdg.Node n2) {
        try {
            return probMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double computeGoalProb(Pdg.Node n) {
        return 1.0;
    }

    @Override
    public double computeAngle(Pdg.Node n1, Pdg.Node n2) {
        try {
            return angleMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double computeGoalAngle(Pdg.Node n) {
        try {
            return angleMatrix[getIndexOfNode(n)][getIndexOfNode(goalNode)];
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Vec2D computeKickVec(Pdg.Node n1, Pdg.Node n2) {

        try {
            return kickVecMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Vec2D computeGoalKickVec(Pdg.Node node) {
        try {
            return passPointMatrix[getIndexOfNode(node)][getIndexOfNode(this.goalNode)];
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Vec2D computePassPoint(Pdg.Node n1, Pdg.Node n2) {

        try {
            return passPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Vec2D computePassPoint(Pdg.Node node) {
        return null;
    }

    @Override
    public Vec2D computeGoalPassPoint(Pdg.Node node) {
        try {
            return passPointMatrix[getIndexOfNode(node)][getIndexOfNode(this.goalNode)];
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Vec2D computeRecepPoint(Pdg.Node n1, Pdg.Node n2) {
        try {
            return recepPointMatrix[getIndexOfNode(n1)][getIndexOfNode(n2)];
        } catch (NonExistentNodeException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Vec2D computeGoalCenter() {
        return null;
    }

    @Override
    public void setSnapShots(ArrayList<RobotSnapshot> allySnaps, ArrayList<RobotSnapshot> foeSnaps, RWLockee<Vec2D> ballSnap) {

    }
}
