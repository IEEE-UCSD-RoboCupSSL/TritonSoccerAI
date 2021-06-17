package Triton.PeriphModules.GameControl;

import Triton.CoreModules.Robot.Team;
import Triton.PeriphModules.GameControl.GameStates.*;

import java.util.Scanner;

public class StdinGameCtrlModule extends GameCtrlModule {
    private final Scanner scanner;

    private boolean isFirstRun = true;

    public StdinGameCtrlModule(Scanner scanner) {
        super("stdin game controller");
        this.scanner = scanner;
    }

    @Override
    public void run() {
        if (isFirstRun) {
            super.subscribe();
            isFirstRun = false;
        }

        System.out.println();
        System.out.println(">>> Enter new game state to update AI, Available game states are:");
        System.out.println("    [halt, stop, running, freekick, kickoff, penalty, timeout, ballplacement]");

        String gsStr = scanner.nextLine();
        GameState gameState;

        switch (gsStr) {
            case "HALT" -> {
                System.err.println(">>>GC: HALT<<<");
                gameState = new HaltGameState();
            }
            case "STOP" -> {
                System.err.println(">>>GC: STOP<<<");
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
                gameState = new BallPlacementGameState(Team.BLUE);
            }
            case "BALL_PLACEMENT_YELLOW" -> {
                System.err.println(">>>GC: BALL_PLACEMENT_YELLOW<<<");
                gameState = new BallPlacementGameState(Team.YELLOW);
            }
            default -> {
                System.err.println(">>>GC: UNKNOWN<<<");
                gameState = new UnknownGameState();
            }
        }

        gsPub.publish(gameState);
    }
}
