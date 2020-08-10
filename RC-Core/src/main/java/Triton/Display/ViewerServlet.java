package Triton.Display;

import java.util.HashMap;

import Triton.Geometry.Field;
import Triton.Geometry.GeometryData;
import Triton.Detection.DetectionData;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;
import Triton.Shape.Line2D;

import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Proto.MessagesRobocupSslGeometry.SSL_FieldCicularArc;

public class ViewerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final int INTERVAL = 10; // time interval for sending data
    public static final double SCALE = 1 / 7.5;
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 800;
    public static final int ROBOT_RADIUS = 12;
    public static final int BALL_RADIUS = 3;
    public static final int ROBOT_COUNT = 6;
    
	private static double convert_x(double x) {
		return x * SCALE + WINDOW_WIDTH / 2;
	}
	
	private static double convert_y(double y) {
		return - y * SCALE + WINDOW_HEIGHT / 2;
    }
    
    private static String lineJson(Line2D line) {
        String json = "{";
        long x1 = Math.round(convert_x(line.p1.x));
        long y1 = Math.round(convert_y(line.p1.y));
        long x2 = Math.round(convert_x(line.p2.x));
        long y2 = Math.round(convert_y(line.p2.y));
        json +=  "\"x1\":" + x1 + ",\"x2\":" + x2 + 
                ",\"y1\":" + y1 + ",\"y2\":" + y2 + "}";
        return json;
    }

    private static String arcJson(SSL_FieldCicularArc arc) {
        String json = "{";
        long x = Math.round(convert_x(arc.getCenter().getX()));
        long y = Math.round(convert_y(arc.getCenter().getY()));
        long r = Math.round(arc.getRadius() * SCALE);
        double a1 = arc.getA1();
        double a2 = arc.getA2();
        json +=  "\"x\":" + x + ",\"y\":" + y + ",\"r\":" + r + 
                 ",\"a1\":" + String.format("%.3f", a1) + 
                 ",\"a2\":" + String.format("%.3f", a2) + "}";

        return json;
    }

	private static String robotJson(Vec2D loc, double ori) {
		String json = "{";
		long x = Math.round(convert_x(loc.x)) - ROBOT_RADIUS;
		long y = Math.round(convert_y(loc.y)) - ROBOT_RADIUS;
		json += "\"x\":\"" + x + "px\"," + "\"y\":\"" + y + "px\"," + 
				"\"o\":\"" + String.format("%.3f", -ori) + "rad\"}";
		return json;
    }
    
    private static String ballJson(Vec2D loc) {
		String json = "{";
		long x = Math.round(convert_x(loc.x)) - BALL_RADIUS;
        long y = Math.round(convert_y(loc.y)) - BALL_RADIUS;
        json += "\"x\":\"" + x + "px\"," + "\"y\":\"" + y + "px\"}";
        return json;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        DetectionData detection;
        Field field;
        PrintWriter out = null;
        String json;
        boolean geoSent = false;

        while(true) {
            try {
                json = "{";
                if (!geoSent) {
                    field = GeometryData.get().getField();
                    
                    json += "\"lines\": {";
                    for(HashMap.Entry<String, Line2D> entry : field.lineSegments.entrySet()) {
                        String name = entry.getKey();
                        Line2D line = entry.getValue();
                        if(name.equals("CenterLine")) continue;
                        json += "\"" + name + "\":" + lineJson(line) + ",";
                    }
                    json = json.substring(0, json.length() - 1) + "}";
                    json += ", \"arcs\": {";
                    for(SSL_FieldCicularArc arc : field.arcList) {
                        json += "\"" + arc.getName() + "\":" + arcJson(arc) + ",";
                    }
                    json = json.substring(0, json.length() - 1) + "}}";
                    geoSent = true;
                } else {
                    detection = DetectionData.get();
                    Vec2D  loc;
                    double ori;
    
                    for(int i = 0; i < ROBOT_COUNT; i++) {
                        loc = detection.getRobotPos(Team.YELLOW, i);
                        ori = detection.getRobotOrient(Team.YELLOW, i);
                        json += "\"y" + i + "\":" + robotJson(loc, ori) + ",";
                        loc = detection.getRobotPos(Team.BLUE, i);
                        ori = detection.getRobotOrient(Team.BLUE, i);
                        json += "\"b" + i + "\":" + robotJson(loc, ori) + ",";
                    }
                    json += "\"ball\":" + ballJson(detection.getBallPos()) + "}";
                }

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