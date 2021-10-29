package triton.periphModules.gameControl;

import proto.SslGcRefereeMessage;
import triton.config.Config;
import triton.coreModules.robot.Team;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.periphModules.gameControl.gameStates.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;

public class SSLGameCtrlModule extends GameCtrlModule {
    private final static int MAX_BUFFER_SIZE = 60000;

    //private MulticastSocket socket;
    private MulticastSocket socket;
    private DatagramPacket packet;

    private boolean isFirstRun = true;

    public SSLGameCtrlModule(Config config) {
        super("ssl game controller");
        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        int port = config.connConfig.gcConn.port;
        String ip = config.connConfig.gcConn.ipAddr;
        try {
            socket = new MulticastSocket(port); // this constructor will automatically enable reuse_addr
            socket.joinGroup(new InetSocketAddress(ip, port), triton.Util.getNetIf("eth1"));

            packet = new DatagramPacket(buffer, buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (isFirstRun) {
            super.subscribe();
            isFirstRun = false;
        }

        //System.out.println("????");
         try {
            socket.receive(packet);
            // System.out.println("!!!!");
            ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(),
                    packet.getOffset(), packet.getLength());

            SslGcRefereeMessage.Referee gcOutput = SslGcRefereeMessage.Referee.parseFrom(input);
            // System.out.println(gcOutput);
//            System.out.println(input);
//            System.err.println(gcOutput);
            parseGcOutput(gcOutput);
        } catch (SocketTimeoutException e) {
                System.out.println("SSL Game Controller Multicast Timeout");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void parseGcOutput(SslGcRefereeMessage.Referee gcOutput) {
        SslGcRefereeMessage.Referee.Command command = gcOutput.getCommand();
        GameState gameState;
        switch (command) {
            case HALT -> {
                gameState = new HaltGameState();
            }
            case STOP -> {
                gameState = new StopGameState();
            }
            case NORMAL_START -> {
                gameState = new NormalStartGameState();
            }
            case FORCE_START -> {
                gameState = new ForceStartGameState();
            }
            case PREPARE_KICKOFF_YELLOW -> {
                gameState = new PrepareKickoffGameState(Team.YELLOW);
            }
            case PREPARE_KICKOFF_BLUE -> {
                gameState = new PrepareKickoffGameState(Team.BLUE);
            }
            case PREPARE_PENALTY_YELLOW -> {
                gameState = new PreparePenaltyGameState(Team.YELLOW);
            }
            case PREPARE_PENALTY_BLUE -> {
                gameState = new PreparePenaltyGameState(Team.BLUE);
            }
            case DIRECT_FREE_YELLOW -> {
                gameState = new PrepareDirectFreeGameState(Team.YELLOW);
            }
            case DIRECT_FREE_BLUE -> {
                gameState = new PrepareDirectFreeGameState(Team.BLUE);
            }
            case INDIRECT_FREE_YELLOW -> {
                gameState = new PrepareIndirectFreeGameState(Team.YELLOW);
            }
            case INDIRECT_FREE_BLUE -> {
                gameState = new PrepareIndirectFreeGameState(Team.BLUE);
            }
            case TIMEOUT_YELLOW -> {
                gameState = new TimeoutGameState(Team.YELLOW);
            }
            case TIMEOUT_BLUE -> {
                gameState = new TimeoutGameState(Team.BLUE);
            }
            case BALL_PLACEMENT_BLUE -> {
                SslGcRefereeMessage.Referee.Point point = gcOutput.getDesignatedPosition();
                gameState = new BallPlacementGameState(Team.BLUE, new Vec2D(point.getX(), point.getY()));
            }
            case BALL_PLACEMENT_YELLOW -> {
                SslGcRefereeMessage.Referee.Point point = gcOutput.getDesignatedPosition();
                gameState = new BallPlacementGameState(Team.YELLOW, new Vec2D(point.getX(), point.getY()));
            }
            default -> {
                gameState = new UnknownGameState();
            }
        }
        gsPub.publish(gameState);
    }
}
