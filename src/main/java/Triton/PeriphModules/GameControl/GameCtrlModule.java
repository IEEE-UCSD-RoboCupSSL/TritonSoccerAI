package Triton.PeriphModules.GameControl;
import Triton.CoreModules.AI.GameStates;
import Triton.Misc.ModulePubSubSystem.FieldPublisher;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;

public abstract class GameCtrlModule implements Module {

    protected FieldPublisher<GameStates> gsPub;
    protected FieldSubscriber<GameStates> gsSub;

    protected GameCtrlModule(String gcName) {
        gsPub = new FieldPublisher<>("game state", gcName, GameStates.UNKNOWN);
        gsSub = new FieldSubscriber<>("game state", gcName);
    }


    public abstract void run();

    protected void subscribe() {
        try {
            gsSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GameStates getGameState() {
        if(gsSub.isSubscribed()) {
            return  gsSub.getMsg();
        }
        return null;
    }


}
