package Triton.PeriphModules.GameControl.GameStates;

public class GameState {
    private GameStateName name;

    public GameState (GameStateName name) {
        this.name = name;
    }

    public GameStateName getName() {
        return name;
    }
}
