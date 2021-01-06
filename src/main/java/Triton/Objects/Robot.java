package Triton.Objects;

import Triton.Dependencies.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Modules.Detection.RobotData;
import Triton.Dependencies.Team;

public abstract class Robot implements Module {
    protected Team team;
    protected int ID;

    private final FieldSubscriber<RobotData> dataSub;

    public Robot(Team team, int ID) {
        this.team = team;
        this.ID = ID;

        dataSub = new FieldSubscriber<>("detection", team.name() + ID);
    }

    protected void subscribe() {
        try {
            dataSub.subscribe();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public RobotData getData() {
        return dataSub.getMsg();
    }

    public int timeToPoint() {
        return 0;
    }

    @Override
    public void run() {
        try {
            subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Team getTeam() {
        return team;
    }

    public int getID() {
        return ID;
    }
}