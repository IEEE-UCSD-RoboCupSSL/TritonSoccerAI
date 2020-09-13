package Triton.Command;

import java.util.HashMap;
import Triton.Detection.Team;
import Triton.Config.ObjectConfig;
import Triton.DesignPattern.*;

public class CommandData extends AbstractData {

    private HashMap<Team, HashMap<Integer, MoveToCommand>> moveToCommands;

    public CommandData() {
        super("Command");
        moveToCommands = new HashMap<Team, HashMap<Integer, MoveToCommand>>();
        moveToCommands.put(Team.BLUE, new HashMap<Integer, MoveToCommand>());
        moveToCommands.put(Team.YELLOW, new HashMap<Integer, MoveToCommand>());
    }

    public void add(Command command) {
        lock.writeLock().lock();
        try {
            if (command instanceof MoveToCommand) {
                MoveToCommand moveToCommand = (MoveToCommand) command;
                moveToCommands.get(moveToCommand.getTeam()).put(moveToCommand.getID(), moveToCommand);
            } else if (command instanceof SwitchCommand) {
                ((SwitchCommand) command).execute();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void executeAll() {
        lock.readLock().lock();
        try {
            for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
                MoveToCommand m = moveToCommands.get(Team.BLUE).get(i);
                if (m != null) m.execute();
                m = moveToCommands.get(Team.YELLOW).get(i);
                if (m != null) m.execute();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public static CommandData get() {
        return (CommandData) MsgChannel.get("Command");
    }
}