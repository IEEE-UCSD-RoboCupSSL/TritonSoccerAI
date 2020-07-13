package Triton.Detection;

import java.util.HashMap;
import Triton.Geometry.Point2D;
import Proto.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;

public class DetectionManager {

    public static final int ROBOT_COUNT = 6;

    public class TeamID {
        Team team;
        int id;

        public TeamID(Team team, int id) {
            this.team = team;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;

            if (!(o instanceof TeamID))
                return false;

            TeamID teamID = (TeamID) o;

            if (team == teamID.team && id == teamID.id)
                return true;

            return false;
        }
    }

    public HashMap<TeamID, Robot> robots;
    private int ballCount;
    private Ball ball;

    static TeamID[] YELLOW = new TeamID[ROBOT_COUNT];
    static TeamID[] BLUE   = new TeamID[ROBOT_COUNT];

    public DetectionManager() {
        robots = new HashMap<TeamID, Robot>();
        for (int i = 0; i < ROBOT_COUNT; i++) {
            YELLOW[i] = new TeamID(Team.YELLOW, i);
            BLUE[i]   = new TeamID(Team.BLUE, i);
            robots.put(YELLOW[i], new Robot(Team.YELLOW, i));
            robots.put(BLUE[i],   new Robot(Team.BLUE, i));
        }
        ball = new Ball();
    }

    public void update(SSL_DetectionFrame df) {
        double time = df.getTCapture();

        for (SSL_DetectionRobot r : df.getRobotsYellowList()) {
            getRobot(Team.YELLOW, r.getRobotId()).update(r, time);
        }
        for (SSL_DetectionRobot r : df.getRobotsBlueList()) {
            getRobot(Team.BLUE, r.getRobotId()).update(r, time);
        }
        ballCount = df.getBallsCount();
        if (ballCount > 0) {
            ball.update(df.getBalls(0), time);
        }
    }

    public int getBallCount() {
        return ballCount;
    }
    
    public Ball getBall() {
        return ball;
    }

    public Point2D getBallPos() {
        return ball.getPos();
    }

    public Robot getRobot(Team team, int ID) {
        if(team == Team.YELLOW) {
            return robots.get(YELLOW[ID]);
        } else {
            return robots.get(BLUE[ID]);
        }
    }

    public Point2D getRobotPos(Team team, int ID) {
        return getRobot(team, ID).getPos();
    }

    public double getRobotOrient(Team team, int ID) {
        return getRobot(team, ID).getOrient();
    }
}