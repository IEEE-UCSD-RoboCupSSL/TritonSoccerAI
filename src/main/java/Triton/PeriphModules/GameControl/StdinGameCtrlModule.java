package Triton.PeriphModules.GameControl;

import Triton.CoreModules.AI.GameStates;

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
            GameStates gs;

            switch (gsStr) {
                case "halt" -> {
                    System.out.println(">>>HALT<<<");
                    gs = GameStates.HALT;
                }
                case "stop" -> {
                    System.out.println(">>>STOP<<<");
                    gs = GameStates.STOP;
                }
                case "running" -> {
                    System.out.println(">>>RUNNING<<<");
                    gs = GameStates.RUNNING;
                }
                case "freekick" -> {
                    System.out.println(">>>FREE_KICK<<<");
                    gs = GameStates.FREE_KICK;
                }
                case "kickoff" -> {
                    System.out.println(">>>KICKOFF<<<");
                    gs = GameStates.KICKOFF;
                }
                case "penalty" -> {
                    System.out.println(">>>PENALTY<<<");
                    gs = GameStates.PENALTY;
                }
                case "timeout" -> {
                    System.out.println(">>>TIMEOUT<<<");
                    gs = GameStates.TIMEOUT;
                }
                case "ballplacement" -> {
                    System.out.println(">>>BALL_PLACEMENT<<<");
                    gs = GameStates.BALL_PLACEMENT;
                }
                default -> {
                    System.out.println(">>>UNKNOWN<<<");
                    gs = GameStates.UNKNOWN;
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
