/*
 * 
 * 　　┏┓　　　┏┓+ +
 * 　┏┛┻━━━┛┻┓ + +
 * 　┃　　　　　　　┃ 　
 * 　┃　　　━　　　┃ ++ + + +
 *  ████━████ ┃+
 * 　┃　　　　　　　┃ +
 * 　┃　　　┻　　　┃
 * 　┃　　　　　　　┃ + +
 * 　┗━┓　　　┏━┛
 * 　　　┃　　　┃　　　　　　　　　　　
 * 　　　┃　　　┃ + + + +
 * 　　　┃　　　┃
 * 　　　┃　　　┃ +  神兽保佑
 * 　　　┃　　　┃    代码无bug　　
 * 　　　┃　　　┃　　+　　　　　　　　　
 * 　　　┃　 　　┗━━━┓ + +
 * 　　　┃ 　　　　　　　┣┓
 * 　　　┃ 　　　　　　　┏┛
 * 　　　┗┓┓┏━┳┓┏┛ + + + +
 * 　　　　┃┫┫　┃┫┫
 * 　　　　┗┻┛　┗┻┛+ + + +
 * 
 */

/*
 * @Author: Neil Min, Cecilia Hong
 * @Date: 2020-10-10 15:44:31
 * @LastEditTime: 2020-10-10 08:37:41
 * @Description: Path record for a particular robot
 * @FilePath: /SimuBot/RC-Core/src/main/java/Triton/Computation/AStar/Path.java
 */

 
package Triton.Computation.AStar;

import java.util.ArrayList;

import Triton.Shape.Vec2D;

public class Path {
	private Grid grid;
	private ArrayList<Node> markedNodes;

	public Path(double worldSizeX, double worldSizeY) {
		grid = new Grid(worldSizeX, worldSizeY);
		markedNodes = new ArrayList<>();
	}

	// Add a path into the path records of a robot
	public void addPath(ArrayList<Node> path) {
		for (Node n : path) {
			if (!markedNodes.contains(n)) {
				markedNodes.add(n);
			}
			n.setWalkable(false); // mark the node along the path
		}
	}

	// Clear the path records
	public void reset() {
		for (Node n : markedNodes) {
			n.setWalkable(true);
		}
		markedNodes.clear();
	}

	public ArrayList<Node> getPaths(){
		return markedNodes;
	}
	 
}