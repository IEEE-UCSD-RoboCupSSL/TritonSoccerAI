package triton.coreModules.ai.dijkstra;

import triton.config.globalVariblesAndConstants.GvcGeometry;
import triton.coreModules.ai.dijkstra.exceptions.*;
import triton.coreModules.robot.ally.Ally;
import triton.misc.math.linearAlgebra.Vec2D;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
public class Pdg { //Probability Undirected Acyclic Graph

    private final Node startNode;
    private final Node endNode;
    private final HashMap<Node, Integer> nodeToIndexMap = new HashMap<>();
    private final HashMap<Node, Set<Node>> nodeNeighborSetMap = new HashMap<>();
    private final Edge[][] adjMatrix;

    public Pdg(Node startNode, Node endNode, List<Node> middleNodes) throws NodesNotUniqueException {

        if(!testNodeUnique(startNode, endNode, middleNodes)){
            throw new NodesNotUniqueException(startNode, endNode, middleNodes);
        }
        // construct graph
        this.startNode = startNode;
        this.endNode = endNode;
        int index = 0;

        nodeToIndexMap.put(startNode, index);
//        System.out.printf("[PUAG1] map %s to index %d\n", startNode, index);
        index++;

        for (Node middleNode : middleNodes) {
            nodeToIndexMap.put(middleNode, index);
//            System.out.printf("[PUAG1] map %s to index %d\n", middleNode, index);
            index++;
        }

        nodeToIndexMap.put(endNode, index);
//        System.out.printf("[PUAG1] map %s to index %d\n", endNode, index);

        adjMatrix = new Edge[nodeToIndexMap.size()][nodeToIndexMap.size()];

        for (Node node1 : nodeToIndexMap.keySet()) {
            HashSet<Node> neighborSet = new HashSet<>();
            nodeNeighborSetMap.put(node1, neighborSet);
            neighborSet.addAll(nodeToIndexMap.keySet());
        }

        for (int i = 0; i < nodeToIndexMap.size(); i++) {
            for (int j = 0; j < nodeToIndexMap.size(); j++) {
                adjMatrix[i][j] = new Edge();
            }
        }
    }

    public int getIndexOfNode(Node node) throws GraphIOException {
        Integer integer = nodeToIndexMap.get(node);
        if(integer == null){
            throw new NonExistentNodeException(node);
        }

        if(integer >= getNumNodes()){
            throw new InvalidNodeIndexException(nodeToIndexMap, node);
        }

        return integer;
    }

    private boolean testNodeUnique(Node startNode, Node endNode, List<Node> middleNodes){
        HashSet<Node> nodes = new HashSet<>();

        nodes.add(startNode);
        nodes.add(endNode);
        nodes.addAll(middleNodes);

        return nodes.size() >= middleNodes.size() + 2;
    }

    public Set<Node> getNodeSet() {
        return nodeToIndexMap.keySet();
    }

    public int getNumNodes() {
        return nodeToIndexMap.size();
    }

    public List<Node> getAdjacentNodes(Node node) {
        if(nodeNeighborSetMap.containsKey(node)) {
            return new ArrayList<>(nodeNeighborSetMap.get(node));
        } else {
            return null;
        }
    }

    public Edge getEdge(Node node1, Node node2) throws GraphIOException {
        if (node1.equals(node2)){
            throw new NoSuchEdgeException(node1, node2);
        }

        boolean isNode2NeighborOfNode1 = getAdjacentNodes(node1).contains(node2);
        boolean isNode1NeighborOfNode2 = getAdjacentNodes(node2).contains(node1);

        boolean areNeighbors = isNode1NeighborOfNode2 && isNode2NeighborOfNode1;

        assert isNode1NeighborOfNode2 == isNode2NeighborOfNode1;

        int indexOfNode1 = getIndexOfNode(node1);
        int indexOfNode2 = getIndexOfNode(node2);

        if (areNeighbors) {
            return adjMatrix[indexOfNode1][indexOfNode2];
        } else {
            throw new NoSuchEdgeException(node1, node2);
        }

    }

    public void setEdgeProb(Node node1, Node node2, double prob) throws GraphIOException {
        getEdge(node1, node2).setProb(prob);
    }


    public abstract static class Node {
        @Nullable private final Ally bot;
        private final int relatedRobotId;

        public Vec2D getPos(){
            if(bot == null){
                return GvcGeometry.GOAL_CENTER_FOE;
            } else {
                return bot.getPos();
            }
        }

        public Node(@Nullable Ally bot) {
            this.bot = bot;
            if(bot == null){
                relatedRobotId = -1;
            } else {
                relatedRobotId = bot.getID();
            }
        }

        public String getNodeBotIdString(){
            if (bot == null){
                return "Goal";
            }

            return Integer.toString(bot.getID());
        }

        public @Nullable Ally getBot() {
            return bot;
        }

        public int getRelatedRobotId() {
            return relatedRobotId;
        }

        @Override
        public int hashCode() {
            if(bot == null){
////                System.out.println("[hashCode] bot is null");
                return super.hashCode();
            }
////            System.out.println("[hashCode] bot is not null");
            return bot.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(bot == null || obj == null){
////                System.out.println("[equals] either bot or obj is null");
                return super.equals(obj);
            }

            if(!(obj instanceof Node)){
////                System.out.println("[equals] obj is no instanceof Node");
                return super.equals(obj);
            }

            Ally otherBot = ((Node) obj).getBot();

            if(otherBot == null){
                return super.equals(obj);
            }

            int id1 = this.bot.getID();
            int id2 = otherBot.getID();

////            System.out.printf("[equals] comparing if bot %d is equal to bot %d\n", id1, id2);
            return id1 == id2;
        }
    }

    public static class AllyNode extends Node {

        public AllyNode(Ally bot) {
            super(bot);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class AllyRecepNode extends AllyNode {
        private Vec2D receptionPoint;
        private double angle;

        public AllyRecepNode(Ally bot) {
            super(bot);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class AllyPassNode extends AllyNode {
        private Vec2D passPoint;
        private double angle;
        private Vec2D kickVec;

        public AllyPassNode(Ally bot) {
            super(bot);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class GoalNode extends Node {
        private Vec2D goalCenter = GvcGeometry.GOAL_CENTER_FOE;

        public GoalNode() {
            super(null);
        }

        @Override
        public int hashCode() {
            return goalCenter.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof GoalNode)){
                return false;
            }

            return this.goalCenter.equals(((GoalNode) obj).goalCenter);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Edge {
        private double prob;
    }

}
