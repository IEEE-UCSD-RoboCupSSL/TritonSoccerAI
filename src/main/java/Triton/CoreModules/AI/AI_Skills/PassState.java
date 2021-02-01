package Triton.CoreModules.AI.AI_Skills;

public enum PassState {
    PENDING, // when pass & receive method is not in use
    PASSER_HOLDS_BALL,
    PASSER_IN_POSITION,
    RECEIVER_IN_POSITION,
    PASSED, // means ball is kicked
    RECEIVE_SUCCESS,
    FAILED
}
