package Triton.CoreModules.AI.AI_Skills;

import Triton.Config.ObjectConfig;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.Math.Geometry.Line2D;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Comparator;

public class Swarm extends Skills {

    private final RobotList<Ally> botList;
//    private static Publisher<Boolean> delayPub;
//    private static Subscriber<Boolean> delaySub;
//    private static int defaultUpdatePeriod = 100; // milliseconds
//
//    static { // static block only runs once during the first time this class is used
//        /* these steps take time, don't put them in constructors because constructor for
//        * this class is meant to repeatedly called whenever a new subset of robots
//        * are assigned as a swarm group */
//        delayPub = new FieldPublisher<>("Swarm", "DelayFinished", false);
//        delaySub = new FieldSubscriber<>("Swarm", "DelayFinished");
//        try {
//            delaySub.subscribe(1000);
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
//
//        App.threadPool.submit(new Runnable() {
//            @Override
//            public void run() {
//                if(delaySub.getMsg()) {
//                    delayPub.publish(false);
//                } else {
//                    delayPub.publish(true);
//                }
//
//                try {
//                    Thread.sleep(defaultUpdatePeriod);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }





    public Swarm(RobotList<Ally> botList) {
        this.botList = botList;
    }


    /*
     * Command a group of robots from the input botList
     * to go to the positions in posList with priority defined
     * by the priorityRefPoint, i.e. the positions closest to
     * priorityRefPoint will be the first position to assign a
     * nearest robot, which gives a simple-to-implement global measurement
     * for optimizing speed when selecting the robot-targetPosition pairs.
     *
     * The priorityRefPoint is usually the target location to which
     * the passer passes the ball.
     *
     *
     * Size of the the botList doesn't have to be numRobots for sometimes
     * we only want some of the robots to go to certain positions while
     * leaving others controlled by other methods.
     *
     * dirList corresponds to posList, not the robots, a robot can be selected
     * to go to a different (pos, dir) pair depending on whether it is the closest
     * bot to that position. (P.S. positions are sorted based on priorityRefPoint)
     *
     * @contract: botList.size() == posList.size() == dirList.size()
     * @return true when all bots from botList have arrived their corresponding positions
     * */
    public boolean groupTo(ArrayList<Vec2D> posList,
                                  ArrayList<Double> dirList, Vec2D priorityRefPoint) {
        if (botList.size() > ObjectConfig.ROBOT_COUNT - 1 || botList.size() != posList.size()
                || (dirList != null && posList.size() != dirList.size())) {
            System.out.println("Inputs have invalid size(s)");
        }

        /* sort posList(or posDirList) based on priorityRefPoint */
        ArrayList<Pair<Vec2D, Double>> posDirList;
        posDirList = new ArrayList<>();
        for(int i = 0; i < posList.size(); i++) {
            if(dirList != null) {
                posDirList.add(new Pair<>(posList.get(i), dirList.get(i)));
            }
            else {
                posDirList.add(new Pair<>(posList.get(i), null));
            }
        }
        posDirList.sort(new Comparator<>() { // sorts in ascending order
            @Override
            public int compare(Pair<Vec2D, Double> o1, Pair<Vec2D, Double> o2) { // rtn (neg_int, 0, pos_int) : (<, =, >)
                // priorityRefPoint is used as "effective final" here
                int a = (int) (o1.getValue0().sub(priorityRefPoint).mag());
                int b = (int) (o2.getValue0().sub(priorityRefPoint).mag());
                return a - b;
            }
        });
//        for(Pair<Vec2D, Double> posDir : posDirList) {
//            System.out.println(posDir.getValue0());
//        }

        @SuppressWarnings("unchecked")
        RobotList<Ally> bots = (RobotList<Ally>) botList.clone();

        boolean rtn = true;

        for (Pair<Vec2D, Double> posDir : posDirList) {
            Vec2D loc = posDir.getValue0();
            Double ang = posDir.getValue1();

            Ally nearestBot = null;
            double dist = Double.MAX_VALUE;

            /* find nearest bot */
            for (Ally bot : bots) {

                /* upgrade to using path distance instead of eucledian distance*/
                double newDist = loc.sub(bot.getPos()).mag();
                if (newDist < dist) {
                    dist = newDist;
                    nearestBot = bot;
                }
            }
            if (nearestBot != null) {
                bots.remove(nearestBot);

                // if(Swarm.delaySub.getMsg()) { // only update command after having delayed for a bit to avoid robotic parkinson behavior :)
                    /* command nearest bot to the corresponding location */
                if (ang != null) {
                    /* To-do: upgrade to using curveTo */
                    nearestBot.fastCurveTo(loc, ang);
                } else {
                    nearestBot.fastCurveTo(loc);
                    // System.out.println(nearestBot.getID() + " " + loc);
                }
                //}

                if (!nearestBot.isPosArrived(loc) ||
                        (ang != null && !nearestBot.isDirAimed(ang))) {
                    rtn = false;
                }
            }
        }

        return rtn;
    }

    public boolean groupTo(ArrayList<Vec2D> locList, Vec2D priorityRefPoint) {
        return groupTo(locList, null, priorityRefPoint);
    }

    public boolean groupTo(ArrayList<Vec2D> locList, ArrayList<Double> dirList) {
        return groupTo(locList, dirList, new Vec2D(0, 0));
    }

    /* default priorityRefPoint would be center (0, 0), i.e. robots tend to arrive at locations near the center first*/
    public boolean groupTo(ArrayList<Vec2D> locList) {
        return groupTo(locList, null, new Vec2D(0, 0));
    }




    public static void lineUp(RobotList<Ally> botList, Line2D line, double gap, Vec2D center) {
        if (botList.size() > ObjectConfig.ROBOT_COUNT - 1) {
            System.out.println("botList has invalid size");
        }


    }

    // To-do: make Curve2D
    public static void CurveUp(RobotList<Ally> botList /*...*/) {
        if (botList.size() > ObjectConfig.ROBOT_COUNT - 1) {
            System.out.println("botList has invalid size");
        }

    }

}
