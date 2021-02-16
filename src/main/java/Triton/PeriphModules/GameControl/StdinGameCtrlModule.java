package Triton.PeriphModules.GameControl;

import Triton.PeriphModules.GameControl.GameStates.*;

import java.util.Scanner;

public class StdinGameCtrlModule extends GameCtrlModule {
    private final Scanner scanner;

    public StdinGameCtrlModule(Scanner scanner) {
        super("stdin game controller");
        this.scanner = scanner;
    }

    @Override
    public void run() {
        super.subscribe();

        while (true) {
            System.out.println();
            System.out.println(">>> Enter new game state to update AI, Available game states are:");
            System.out.println("    [halt, stop, running, freekick, kickoff, penalty, timeout, ballplacement]");

            String gsStr = scanner.nextLine();
            GameState gs;

            switch (gsStr) {
                case "halt" -> {
                    System.out.println(">>>HALT<<<");
                    gs = new HaltGameState();
                }
                case "stop" -> {
                    System.out.println(">>>STOP<<<");
                    gs = new StopGameState();
                }
                case "running" -> {
                    System.out.println(">>>RUNNING<<<");
                    gs = new RunningGameState();
                }
                case "freekick" -> {
                    System.out.println(">>>FREE_KICK<<<");
                    gs = new FreeKickGameState();
                }
                case "kickoff" -> {
                    System.out.println(">>>KICKOFF<<<");
                    gs = new KickoffGameState();
                }
                case "penalty" -> {
                    System.out.println(">>>PENALTY<<<");
                    gs = new PenaltyGameState();
                }
                case "timeout" -> {
                    System.out.println(">>>TIMEOUT<<<");
                    gs = new TimeoutGameState();
                }
                case "ballplacement" -> {
                    System.out.println(">>>BALL_PLACEMENT<<<");
                    gs = new BallPlacementGameState();
                }
                default -> {
                    System.out.println(">>>UNKNOWN<<<");
                    gs = new UnknownGameState();
                }
            }

            gsPub.publish(gs);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
