package Triton.CoreModules.AI;

import Triton.CoreModules.AI.AI_Strategies.BasicPlay;
import Triton.CoreModules.AI.AI_Strategies.Strategies;
import Triton.CoreModules.AI.AI_Tactics.FillGapGetBall;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.GameControl.GameCtrlModule;


public class AI implements Module {
    private static final double KICK_DIST = 100;

    private final RobotList<Ally> fielders;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;


    private final Strategies strategyToPlay;
    private final GameCtrlModule gameCtrl;


    public AI(RobotList<Ally> fielders, Ally keeper,
              RobotList<Foe> foes, Ball ball, GameCtrlModule gameCtrl) {
        if (fielders == null || keeper == null || foes == null || ball == null || gameCtrl == null) {
            throw new NullPointerException();
        }

        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;
        this.gameCtrl = gameCtrl;

        strategyToPlay = new BasicPlay(fielders, keeper, foes, ball);
    }

    @Override
    public void run() {
        try {
            while (true) { // delay added
                GameStates currGameState = gameCtrl.getGameState();

                // Decision Tree
                switch (currGameState) {
                    case RUNNING -> {
                        tmpPlaceHolder(">>>RUNNING<<<");
                        strategyToPlay.play();
                    }
                    case FREE_KICK -> {
                        tmpPlaceHolder(">>>FREE_KICK<<<");

                        // To-do: problem
                        FillGapGetBall getball = new FillGapGetBall(fielders, keeper, foes, ball);
                        while(!getball.exec()) {
                            Thread.sleep(1);
                        }

                        fielders.stopAll();


                    }
                    case KICKOFF -> {
                        tmpPlaceHolder(">>>KICKOFF<<<");

                        Formation.getInstance().moveToFormation("kickoff-defense", fielders, keeper);

                    }
                    case PENALTY -> {
                        tmpPlaceHolder(">>>PENALTY<<<");
                    }
                    case TIMEOUT -> {
                        tmpPlaceHolder(">>>TIMEOUT<<<");
                        fielders.stopAll();
                        keeper.stop();
                    }
                    case HALT -> {
                        tmpPlaceHolder(">>>HALT<<<");
                        fielders.stopAll();
                        keeper.stop();
                    }
                    case STOP -> {
                        tmpPlaceHolder(">>>STOP<<<");
                        fielders.stopAll();
                        keeper.stop();
                    }
                    case BALL_PLACEMENT -> {
                        tmpPlaceHolder(">>>BALL_PLACEMENT<<<");
                        // to-do
                    }
                    default -> {
                        tmpPlaceHolder(">>>UNKNOWN<<<");
                    }
                }

                try { // avoid starving other threads
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tmpPlaceHolder(String s) {
    }
}


