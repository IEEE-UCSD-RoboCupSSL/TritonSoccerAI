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

            String[] gsSplit = gsStr.split(" ");

            GameState gs;
            switch (gsSplit[0]) {
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
                    if (gsSplit[1].equals("blue"))
                        gs = new FreeKickGameState(Team.BLUE);
                    else
                        gs = new FreeKickGameState(Team.YELLOW);
                }
                case "kickoff" -> {
                    System.out.println(">>>KICKOFF<<<");
                    gs = (gsSplit[1].equals("blue")) ? new KickoffGameState(Team.BLUE) : new KickoffGameState(Team.YELLOW);
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
                    Team team = (gsSplit[1].equals("blue")) ? Team.BLUE : Team.YELLOW;
                    Vec2D targetPos = new Vec2D(Double.parseDouble(gsSplit[2]), Double.parseDouble(gsSplit[3]));
                    gs = new BallPlacementGameState(team, targetPos);
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
