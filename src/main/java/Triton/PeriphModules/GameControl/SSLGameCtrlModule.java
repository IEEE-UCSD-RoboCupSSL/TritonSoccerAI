package Triton.PeriphModules.GameControl;

import Proto.SslGcApi;
import Proto.SslGcRefereeMessage;
import Proto.SslGcState;
import Triton.Config.Config;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.PeriphModules.GameControl.GameStates.*;
import Triton.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;

public class SSLGameCtrlModule extends GameCtrlModule {
    private final static int MAX_BUFFER_SIZE = 67108864;

    private DatagramSocket socket;
    private DatagramPacket packet;

    public SSLGameCtrlModule() {
        super("ssl game controller");

        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        NetworkInterface netIf = Util.getNetIf(Config.conn().getGcNetIf());
        socket = Util.mcSocket(Config.conn().getGcMcAddr(),
                Config.conn().getGcMcPort(),
                netIf);
        packet = new DatagramPacket(buffer, buffer.length);
    }

    @Override
    public void run() {
        while (true) {
            try {
                socket.receive(packet);
                ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(),
                        packet.getOffset(), packet.getLength());

                SslGcRefereeMessage.Referee gcOutput = SslGcRefereeMessage.Referee.parseFrom(input);
//                System.err.println(gcOutput);
                parseGcOutput(gcOutput);
            } catch (SocketTimeoutException e) {
//                System.err.println("SSL Game Controller Multicast Timeout");
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void parseGcOutput(SslGcRefereeMessage.Referee gcOutput) {
        SslGcRefereeMessage.Referee.Command command = gcOutput.getCommand();
        GameState gameState;
        switch (command) {
            case HALT -> {
                System.err.println(">>>HALT<<<");
                gameState = new HaltGameState();
            }
            case STOP -> {
                System.err.println(">>>STOP<<<");
                gameState = new StopGameState();
            }
            case NORMAL_START -> {
                System.err.println(">>>NORMAL_START<<<");
                gameState = new RunningGameState();
            }
            case FORCE_START -> {
                System.err.println(">>>FORCE_START<<<");
                gameState = new RunningGameState();
            }
            case PREPARE_KICKOFF_YELLOW -> {
                System.err.println(">>>PREPARE_KICKOFF_YELLOW<<<");
                gameState = new KickoffGameState(Team.YELLOW);
            }
            case PREPARE_KICKOFF_BLUE -> {
                System.err.println(">>>PREPARE_KICKOFF_BLUE<<<");
                gameState = new KickoffGameState(Team.BLUE);
            }
            case PREPARE_PENALTY_YELLOW -> {
                System.err.println(">>>PREPARE_PENALTY_YELLOW<<<");
                gameState = new PenaltyGameState(Team.YELLOW);
            }
            case PREPARE_PENALTY_BLUE -> {
                System.err.println(">>>PREPARE_PENALTY_BLUE<<<");
                gameState = new PenaltyGameState(Team.BLUE);
            }
            case DIRECT_FREE_YELLOW -> {
                System.err.println(">>>DIRECT_FREE_YELLOW<<<");
                gameState = new FreeKickGameState(Team.YELLOW);
            }
            case DIRECT_FREE_BLUE -> {
                System.err.println(">>>DIRECT_FREE_BLUE<<<");
                gameState = new FreeKickGameState(Team.BLUE);
            }
            case INDIRECT_FREE_YELLOW -> {
                System.err.println(">>>INDIRECT_FREE_YELLOW<<<");
                gameState = new FreeKickGameState(Team.YELLOW);
            }
            case INDIRECT_FREE_BLUE -> {
                System.err.println(">>>INDIRECT_FREE_BLUE<<<");
                gameState = new FreeKickGameState(Team.BLUE);
            }
            case TIMEOUT_YELLOW -> {
                System.err.println(">>>TIMEOUT_YELLOW<<<");
                gameState = new TimeoutGameState(Team.YELLOW);
            }
            case TIMEOUT_BLUE -> {
                System.err.println(">>>TIMEOUT_BLUE<<<");
                gameState = new TimeoutGameState(Team.BLUE);
            }
            case BALL_PLACEMENT_BLUE -> {
                System.err.println(">>>BALL_PLACEMENT_BLUE<<<");
                SslGcRefereeMessage.Referee.Point point = gcOutput.getDesignatedPosition();
                gameState = new BallPlacementGameState(Team.BLUE, new Vec2D(point.getX(), point.getY()));
            }
            case BALL_PLACEMENT_YELLOW -> {
                System.err.println(">>>BALL_PLACEMENT_YELLOW<<<");
                SslGcRefereeMessage.Referee.Point point = gcOutput.getDesignatedPosition();
                gameState = new BallPlacementGameState(Team.BLUE, new Vec2D(point.getX(), point.getY()));
            }
            default -> {
                System.err.println(">>>UNKNOWN<<<");
                gameState = new UnknownGameState();
            }
        }
        gsPub.publish(gameState);
    }
}
