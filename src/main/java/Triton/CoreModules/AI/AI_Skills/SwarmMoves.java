package Triton.CoreModules.AI.AI_Skills;

import Triton.Config.ObjectConfig;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Coordinates.Vec2D;
import Triton.Misc.Geometry.Line2D;

import java.util.ArrayList;
import java.util.Vector;

public class SwarmMoves extends Skills{

    /* Use optimal paths for each robots from the input botList
     * to go to each ones' nearest locations from the input locList.
     *
     * Size of the the botList doesn't have to be numRobots for sometimes
     * we only want some of the robots to go to certain locations while
     * leaving others controlled by other methods.
     *
     * This method isn't meant for goalKeeper to use though
     *
     * @contract: botList.size() == LocList.size()
     */
    public static void optPathsTo(RobotList<Ally> botList, ArrayList<Vec2D> locList) {
        if(botList.size() > ObjectConfig.ROBOT_COUNT - 1 || botList.size() != locList.size()) {
            System.out.println("Inputs have invalid size(s)");
        }



    }

    public static void lineUp(RobotList<Ally> botList, Line2D line) {
        if(botList.size() > ObjectConfig.ROBOT_COUNT - 1) {
            System.out.println("botList has invalid size");
        }

    }

    // To-do: make Curve2D
    public static void CurveUp(RobotList<Ally> botList /*...*/) {
        if(botList.size() > ObjectConfig.ROBOT_COUNT - 1) {
            System.out.println("botList has invalid size");
        }

    }

}
