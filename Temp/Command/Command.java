package Triton.Command;

import Triton.WorldSim.WorldSim;

public abstract class Command {

    public static WorldSim world;

    protected boolean executed = false;

    public abstract void execute();

    public static void setWorld(WorldSim world) {
        Command.world = world;
    }
}