/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimuBot;

/**
 *
 * @author kevinzhou
 */
public class Runner {
    
    private static final String VISION_MULTICAST_ADDR = "224.5.23.3";
    private static final int VISION_PORT = 10002; 

    
    public static void main(String args[]) {
        
        //Connection connection1 = new Connection(); // Where is this used?
        VisionConnection vision = new VisionConnection(VISION_MULTICAST_ADDR, VISION_PORT);
        FieldMonitor fieldMonitor = new FieldMonitor(vision, new FieldConfiguration());
        vision.addObserver(fieldMonitor);

        for(int i = 0; i < 3; i++) { 
            vision.collectData(4);
            System.out.println(fieldMonitor.detection.toString());
        }
        
        /*
        connection1.setIp("127.0.0.1");
        connection1.setPort(20011);
        connection1.setId(0);
        connection1.setWheel1(0);
        connection1.setWheel2(0);
        connection1.setWheel3(0);
        connection1.setWheel4(0);
        connection1.setVelX(3);
        connection1.setVelY(0);
        connection1.setVelZ(0);
        connection1.setWheelSpeed(false);
        connection1.setKickspeedZ(0);
        connection1.setTeamYellow(true);
        connection1.setKickspeedX(0);
        connection1.setSpinner(false);
        connection1.send();*/
        /*
        Connection connection2 = new Connection();
        
        connection2.setIp("127.0.0.1");
        connection2.setPort(20011);
        connection2.setId(1);
        connection2.setWheel1(0);
        connection2.setWheel2(0);
        connection2.setWheel3(0);
        connection2.setWheel4(0);
        connection2.setVelX(3);
        connection2.setVelY(0);
        connection2.setVelZ(0);
        connection2.setWheelSpeed(false);
        connection2.setKickspeedZ(0);
        connection2.setTeamYellow(false);
        connection2.setKickspeedX(0);
        connection2.setSpinner(false);
        connection2.send();*/
        
        //System.out.println("******** Connection 1 ********");
        
        
        //System.out.println("******** Connection 2 ********");
        //connection2.receive();


        
    }
}
