package triton.periphModules.gameControl;

import triton.misc.modulePubSubSystem.FieldPublisher;
import triton.misc.modulePubSubSystem.FieldSubscriber;
import triton.misc.modulePubSubSystem.Module;
import triton.periphModules.gameControl.gameStates.GameState;
import triton.periphModules.gameControl.gameStates.UnknownGameState;

public abstract class GameCtrlModule implements Module {

    protected FieldPublisher<GameState> gsPub;
    protected FieldSubscriber<GameState> gsSub;

    protected GameCtrlModule(String gcName) {
        gsPub = new FieldPublisher<>("From:GameCtrlModule", "GameState " + gcName, new UnknownGameState());
        gsSub = new FieldSubscriber<>("From:GameCtrlModule", "GameState " + gcName);
        try {
            gsSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public abstract void run();

    protected void subscribe() {}

    public GameState getGameState() {
        if (gsSub.isSubscribed()) {
            return gsSub.getMsg();
        }
        return null;
    }


}
