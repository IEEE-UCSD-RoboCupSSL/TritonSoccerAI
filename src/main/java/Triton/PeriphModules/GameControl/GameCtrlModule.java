package Triton.PeriphModules.GameControl;

import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.GameControl.GameStates.GameState;
import Triton.PeriphModules.GameControl.GameStates.UnknownGameState;

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
