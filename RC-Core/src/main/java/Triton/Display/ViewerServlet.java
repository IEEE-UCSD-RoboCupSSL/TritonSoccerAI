package Triton.Display;

import Triton.Detection.DetectionData;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;

import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final int INTERVAL = 10; // time interval for sending data
    public static final double SCALE = 1 / 7.5;
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final int ROBOT_RADIUS = 12;
    public static final int ROBOT_COUNT = 6;
    
	private static double convert_x(double x) {
		return x * SCALE + WINDOW_WIDTH / 2;
	}
	
	private static double convert_y(double y) {
		return - y * SCALE + WINDOW_HEIGHT / 2;
	}

	private static String robotJson(Vec2D loc, double ori) {
		String json = "{";
		int x = (int) (convert_x(loc.x - ROBOT_RADIUS));
		int y = (int) (convert_y(loc.y - ROBOT_RADIUS));
		json += "\"x\":\"" + x + "px\"," + "\"y\":\"" + y + "px\"," + 
				"\"o\":\"" + String.format("%.3f", ori) + "rad\"}";
		return json;
	}

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        DetectionData data;
        PrintWriter out = null;
        String json;

        while(true) {
            try {
                json = "{";
                data = DetectionData.get();
                
                Vec2D  loc;
                double ori;

                for(int i = 0; i < ROBOT_COUNT; i++) {
                    loc = data.getRobotPos(Team.YELLOW, i);
                    ori = data.getRobotOrient(Team.YELLOW, i);
                    json += "\"y" + i + "\":" + robotJson(loc, ori) + ",";
                    loc = data.getRobotPos(Team.BLUE, i);
                    ori = data.getRobotOrient(Team.BLUE, i);
                    json += "\"b" + i + "\":" + robotJson(loc, ori) + ",";
                }
                json = json.substring(0, json.length() - 1) + "}";

            }  catch (NullPointerException | IndexOutOfBoundsException e) {
                continue;
            }

            out = response.getWriter();
            out.write("data: " + json + "\n\n");
            out.flush();
            
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}