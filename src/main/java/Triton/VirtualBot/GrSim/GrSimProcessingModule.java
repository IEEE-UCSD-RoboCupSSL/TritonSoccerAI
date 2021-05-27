package Triton.VirtualBot.GrSim;


/*
import Triton.Legacy.OldGrSimProtoSrcs.GrSimCommands;
import Proto.VFirmwareAPI;
import Triton.Config.OldConfigs.ObjectConfig;
import Triton.Misc.ModulePubSubSystem.*;
import Triton.Misc.ModulePubSubSystem.Module;

import java.util.ArrayList;

public class GrSimProcessingModule implements Module {
    private final ArrayList<Subscriber<VFirmwareAPI.VF_Commands>> vfirmCmdSubs;
    private final ArrayList<Publisher<GrSimCommands.grSim_Robot_Command>> grSimCmdPubs;

    private boolean isFirstRun = true;

    public GrSimProcessingModule() {
        vfirmCmdSubs = new ArrayList<Subscriber<VFirmwareAPI.VF_Commands>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            vfirmCmdSubs.add(new FieldSubscriber<VFirmwareAPI.VF_Commands>(
                    "vfirm", "cmd" + i));
        }

        grSimCmdPubs = new ArrayList<Publisher<GrSimCommands.grSim_Robot_Command>>();
        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            grSimCmdPubs.add(new FieldPublisher<GrSimCommands.grSim_Robot_Command>(
                    "grSim", "botCmd" + i,
                    GrSimCommands.grSim_Robot_Command.getDefaultInstance()));
        }
    }

    @Override
    public void run() {
        if (isFirstRun) {
            try {
                subscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
            isFirstRun = false;
        }

        for (int i = 0; i < ObjectConfig.ROBOT_COUNT; i++) {
            vfirmCmdSubs.get(i).getMsg();
            update(vfirmCmdSubs.get(i).getMsg(), i);
        }
    }

    private void subscribe() {
        try {
            for (Subscriber<VFirmwareAPI.VF_Commands> vfirmSub : vfirmCmdSubs) {
                vfirmSub.subscribe(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(VFirmwareAPI.VF_Commands vfirmCmd, int ID) {
        GrSimCommands.grSim_Robot_Command.Builder grSimRobotCommands = GrSimCommands.grSim_Robot_Command.newBuilder();

        // TODO Process vFirm commands into grSimRobotCommands

        grSimCmdPubs.get(ID).publish(grSimRobotCommands.build());
    }
}
*/