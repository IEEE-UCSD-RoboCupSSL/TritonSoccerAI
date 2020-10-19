package Triton.Computation.ThetaStar;

import java.util.ArrayList;

import Triton.Detection.RobotData;
import Triton.Shape.Vec2D;

public class Path {
	private Grid grid;
	private ArrayList<Node> markedNodes;
	private RobotData robot;

	public Path(Grid grid, RobotData robot) {
		this.grid = grid;
		this.robot = robot;
	}

	public RobotData getRobot() {
		return robot;
	}

	// Add a path into the path records of a robot
	public void addPath(ArrayList<Node> path) {
		for (Node n : path) {
			if (!markedNodes.contains(n)) {
				markedNodes.add(n);
			}
			n.setWalkable(robot, false); // mark the node along the path
		}
	}

	// Clear the path records
	public void reset() {
		for (Node n : markedNodes) {
			n.setWalkable(robot, true);
		}
		markedNodes.clear();
	}

	public ArrayList<Node> getPaths(){
		return markedNodes;
	}
	 
}