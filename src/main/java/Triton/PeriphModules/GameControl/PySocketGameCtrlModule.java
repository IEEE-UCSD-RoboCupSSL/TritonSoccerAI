package Triton.PeriphModules.GameControl;

import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.PeriphModules.GameControl.GameStates.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class PySocketGameCtrlModule extends GameCtrlModule {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;

    private boolean isFirstRun = true;
    public PySocketGameCtrlModule(int port) {
        super("pysocket game controller");

        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (isFirstRun) {
            super.subscribe();
            isFirstRun = false;
        }

        System.out.println();
        String gsStr = null;
        try {
            gsStr = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] gsSplit = gsStr.split(" ");

        GameState gameState;
        switch (gsSplit[0]) {
            case "HALT" -> {
                System.out.println(">>>HALT<<<");
                gameState = new HaltGameState();
            }
            case "STOP" -> {
                System.out.println(">>>STOP<<<");
                gameState = new StopGameState();
            }
            case "NORMAL_START" -> {
                System.err.println(">>>GC: NORMAL_START<<<");
                gameState = new NormalStartGameState();
            }
            case "FORCE_START" -> {
                System.err.println(">>>GC: FORCE_START<<<");
                gameState = new ForceStartGameState();
            }
            case "PREPARE_KICKOFF_YELLOW" -> {
                System.err.println(">>>GC: PREPARE_KICKOFF_YELLOW<<<");
                gameState = new PrepareKickoffGameState(Team.YELLOW);
            }
            case "PREPARE_KICKOFF_BLUE" -> {
                System.err.println(">>>GC: PREPARE_KICKOFF_BLUE<<<");
                gameState = new PrepareKickoffGameState(Team.BLUE);
            }
            case "PREPARE_PENALTY_YELLOW" -> {
                System.err.println(">>>GC: PREPARE_PENALTY_YELLOW<<<");
                gameState = new PreparePenaltyGameState(Team.YELLOW);
            }
            case "PREPARE_PENALTY_BLUE" -> {
                System.err.println(">>>GC: PREPARE_PENALTY_BLUE<<<");
                gameState = new PreparePenaltyGameState(Team.BLUE);
            }
            case "DIRECT_FREE_YELLOW" -> {
                System.err.println(">>>GC: DIRECT_FREE_YELLOW<<<");
                gameState = new PrepareDirectFreeGameState(Team.YELLOW);
            }
            case "DIRECT_FREE_BLUE" -> {
                System.err.println(">>>GC: DIRECT_FREE_BLUE<<<");
                gameState = new PrepareDirectFreeGameState(Team.BLUE);
            }
            case "INDIRECT_FREE_YELLOW" -> {
                System.err.println(">>>GC: INDIRECT_FREE_YELLOW<<<");
                gameState = new PrepareIndirectFreeGameState(Team.YELLOW);
            }
            case "INDIRECT_FREE_BLUE" -> {
                System.err.println(">>>GC: INDIRECT_FREE_BLUE<<<");
                gameState = new PrepareIndirectFreeGameState(Team.BLUE);
            }
            case "TIMEOUT_YELLOW" -> {
                System.err.println(">>>GC: TIMEOUT_YELLOW<<<");
                gameState = new TimeoutGameState(Team.YELLOW);
            }
            case "TIMEOUT_BLUE" -> {
                System.err.println(">>>GC: TIMEOUT_BLUE<<<");
                gameState = new TimeoutGameState(Team.BLUE);
            }
            case "BALL_PLACEMENT_BLUE" -> {
                System.err.println(">>>GC: BALL_PLACEMENT_BLUE<<<");
                gameState = new BallPlacementGameState(Team.BLUE, new Vec2D(Double.parseDouble(gsSplit[1]), Double.parseDouble(gsSplit[2])));
            }
            case "BALL_PLACEMENT_YELLOW" -> {
                System.err.println(">>>GC: BALL_PLACEMENT_YELLOW<<<");
                gameState = new BallPlacementGameState(Team.YELLOW, new Vec2D(Double.parseDouble(gsSplit[1]), Double.parseDouble(gsSplit[2])));
            }
            default -> {
                System.out.println(">>>UNKNOWN<<<");
                gameState = new UnknownGameState();
            }
        }

        gsPub.publish(gameState);
    }
}
