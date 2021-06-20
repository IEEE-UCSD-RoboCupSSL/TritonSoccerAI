package Triton.PeriphModules.Vision;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcGeometry;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.MQPublisher;
import Triton.Misc.ModulePubSubSystem.Publisher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;

import static Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslGeometry.*;

/**
 * Module to receive data from grSim and send to GeometryModule and Detection Module
 */
public class GrSimVisionModule_OldProto extends VisionModule {

    private final static int MAX_BUFFER_SIZE = 67108864;
    private final Publisher<SSL_DetectionFrame> visionPub;
    private MulticastSocket socket;
    private DatagramPacket packet;

    /**
     * Constructs a VisionModule listening on default ip and port inside ConnectionjsonConfig
     */
    public GrSimVisionModule_OldProto(Config config) {
        this(config.connConfig.sslVisionConn.ipAddr, config.connConfig.sslVisionConn.port);
    }

    /**
     * Constructs a VisionModule listening on specified ip and port
     *
     * @param ip   ip to receive from
     * @param port port to receive from
     */
    public GrSimVisionModule_OldProto(String ip, int port) {
        visionPub = new MQPublisher<>("From:GrSimVisionModule_OldProto", "Detection");

        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        try {
//            socket = Util.mcSocket(jsonConfig.conn().getGrsimMcAddr(),
//                    jsonConfig.conn().getGrsimMcPort());

              socket = new MulticastSocket(port); // this constructor will automatically enable reuse_addr
              socket.joinGroup(new InetSocketAddress(ip, port),
                        NetworkInterface.getByInetAddress(InetAddress.getByName(ip)));

              packet = new DatagramPacket(buffer, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Receive a single packet, and publish it to proper subscribers
     */
    @Override
    protected void update() throws IOException {
        socket.receive(packet);
        ByteArrayInputStream stream = new ByteArrayInputStream(packet.getData(),
                packet.getOffset(), packet.getLength());
        SSL_WrapperPacket SSLPacket =
                SSL_WrapperPacket.parseFrom(stream);

        if (!GvcGeometry.IS_GEO_INIT && SSLPacket.hasGeometry()) {
            processGeometry(SSLPacket.getGeometry());
        }

        if (SSLPacket.hasDetection()) {
            visionPub.publish(SSLPacket.getDetection());
        }
    }

    private void processGeometry(SSL_GeometryData geometryData) {
        SSL_GeometryFieldSize field = geometryData.getField();

        // Field Lines
        GvcGeometry.FIELD_LINES = new HashMap<>();
        for (SSL_FieldLineSegment sslLine : field.getFieldLinesList()) {
            Vec2D p1 = new Vec2D(sslLine.getP1().getX(), sslLine.getP1().getY());
            Vec2D p2 = new Vec2D(sslLine.getP2().getX(), sslLine.getP2().getY());
            Line2D line = new Line2D(p1, p2);
            GvcGeometry.FIELD_LINES.put(sslLine.getName(), line);

            switch (sslLine.getName()) {
                case "TopTouchLine" -> GvcGeometry.TOP_TOUCH_LINE = line;
                case "BottomTouchLine" -> GvcGeometry.BOTTOM_TOUCH_LINE = line;
                case "LeftGoalLine" -> GvcGeometry.LEFT_GOAL_LINE = line;
                case "RightGoalLine" -> GvcGeometry.RIGHT_GOAL_LINE = line;
                case "HalfwayLine" -> GvcGeometry.HALFWAY_LINE = line;
                case "CenterLine" -> GvcGeometry.CENTER_LINE = line;
                case "LeftPenaltyStretch" -> GvcGeometry.LEFT_PENALTY_STRETCH = line;
                case "RightPenaltyStretch" -> GvcGeometry.RIGHT_PENALTY_STRETCH = line;
                case "RightGoalTopLine" -> GvcGeometry.RIGHT_GOAL_TOP_LINE = line;
                case "RightGoalBottomLine" -> GvcGeometry.RIGHT_GOAL_BOTTOM_LINE = line;
                case "RightGoalDepthLine" -> GvcGeometry.RIGHT_GOAL_DEPTH_LINE = line;
                case "LeftGoalTopLine" -> GvcGeometry.LEFT_GOAL_TOP_LINE = line;
                case "LeftGoalBottomLine" -> GvcGeometry.LEFT_GOAL_BOTTOM_LINE = line;
                case "LeftGoalDepthLine" -> GvcGeometry.LEFT_GOAL_DEPTH_LINE = line;
                case "LeftFieldLeftPenaltyStretch" -> GvcGeometry.LEFT_FIELD_LEFT_PENALTY_STRETCH = line;
                case "LeftFieldRightPenaltyStretch" -> GvcGeometry.LEFT_FIELD_RIGHT_PENALTY_STRETCH = line;
                case "RightFieldLeftPenaltyStretch" -> GvcGeometry.RIGHT_FIELD_LEFT_PENALTY_STRETCH = line;
                case "RightFieldRightPenaltyStretch" -> GvcGeometry.RIGHT_FIELD_RIGHT_PENALTY_STRETCH = line;
            }
        }

        // Field Circle
        SSL_FieldCicularArc arc = field.getFieldArcs(0);
        GvcGeometry.FIELD_CIRCLE_CENTER = new Vec2D(arc.getCenter().getX(), arc.getCenter().getY());
        GvcGeometry.FIELD_CIRCLE_RADIUS = arc.getRadius();
        GvcGeometry.FIELD_CIRCLE = new Circle2D(GvcGeometry.FIELD_CIRCLE_CENTER, GvcGeometry.FIELD_CIRCLE_RADIUS);

        // Field Size
        GvcGeometry.FIELD_WIDTH = field.getFieldWidth();
        GvcGeometry.FIELD_LENGTH = field.getFieldLength();
        GvcGeometry.FIELD_BOTTOM_LEFT = new Vec2D(-GvcGeometry.FIELD_LENGTH / 2, -GvcGeometry.FIELD_WIDTH / 2);
        GvcGeometry.GOAL_LEFT = GvcGeometry.RIGHT_GOAL_DEPTH_LINE.p1.y;
        GvcGeometry.GOAL_RIGHT = GvcGeometry.RIGHT_GOAL_DEPTH_LINE.p2.y;
        GvcGeometry.GOAL_LENGTH = field.getGoalWidth();
        GvcGeometry.GOAL_DEPTH = field.getGoalDepth();
        GvcGeometry.FULL_FIELD_LENGTH = GvcGeometry.FIELD_LENGTH + 2 * GvcGeometry.GOAL_DEPTH;

        // Other
        GvcGeometry.GOAL_CENTER_TEAM = new Vec2D(0, -GvcGeometry.FIELD_LENGTH / 2);
        GvcGeometry.GOAL_CENTER_FOE = new Vec2D(0, GvcGeometry.FIELD_LENGTH / 2);
        GvcGeometry.PENALTY_STRETCH_WIDTH =
                GvcGeometry.LEFT_FIELD_LEFT_PENALTY_STRETCH.p1.y - GvcGeometry.LEFT_FIELD_RIGHT_PENALTY_STRETCH.p1.y;
        GvcGeometry.PENALTY_STRETCH_LEFT = -GvcGeometry.PENALTY_STRETCH_WIDTH / 2;
        GvcGeometry.PENALTY_STRETCH_RIGHT = GvcGeometry.PENALTY_STRETCH_WIDTH / 2;
        GvcGeometry.PENALTY_STRETCH_DEPTH =
                GvcGeometry.LEFT_FIELD_LEFT_PENALTY_STRETCH.p2.x - GvcGeometry.LEFT_FIELD_LEFT_PENALTY_STRETCH.p1.x;
        GvcGeometry.PENALTY_STRETCH_Y = -GvcGeometry.FIELD_LENGTH / 2 + GvcGeometry.PENALTY_STRETCH_DEPTH;

        GvcGeometry.IS_GEO_INIT = true;

//        printGeoFields();
    }

    private void printGeoFields() {
        System.out.println("FIELD_WIDTH: " + GvcGeometry.FIELD_WIDTH);
        System.out.println("FIELD_LENGTH: " + GvcGeometry.FIELD_LENGTH);
        System.out.println("FIELD_BOTTOM_LEFT: " + GvcGeometry.FIELD_BOTTOM_LEFT);
        System.out.println("GOAL_LEFT: " + GvcGeometry.GOAL_LEFT);
        System.out.println("GOAL_RIGHT: " + GvcGeometry.GOAL_RIGHT);
        System.out.println("GOAL_LENGTH: " + GvcGeometry.GOAL_LENGTH);
        System.out.println("GOAL_DEPTH: " + GvcGeometry.GOAL_DEPTH);
        System.out.println("FULL_FIELD_LENGTH: " + GvcGeometry.FULL_FIELD_LENGTH);

        System.out.println("FIELD_CIRCLE_CENTER: " + GvcGeometry.FIELD_CIRCLE_CENTER);
        System.out.println("FIELD_CIRCLE_RADIUS: " + GvcGeometry.FIELD_CIRCLE_RADIUS);
        System.out.println("FIELD_CIRCLE: " + GvcGeometry.FIELD_CIRCLE);

        for (String key : GvcGeometry.FIELD_LINES.keySet()) {
            System.out.println(key + ": " + GvcGeometry.FIELD_LINES.get(key));
        }
    }
}