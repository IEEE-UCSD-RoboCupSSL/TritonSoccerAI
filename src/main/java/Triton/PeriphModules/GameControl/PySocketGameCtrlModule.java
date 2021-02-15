package Triton.PeriphModules.GameControl;

import Triton.CoreModules.AI.GameStates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class PySocketGameCtrlModule extends GameCtrlModule {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;

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
        super.subscribe();

        while (true) {
            System.out.println();
            String gsStr = null;
            try {
                gsStr = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
