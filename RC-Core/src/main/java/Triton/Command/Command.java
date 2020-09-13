package Triton.Command;

public abstract class Command {

    protected boolean executed = false;

    public abstract void execute();
}