package Triton.VirtualBot;

import Proto.FirmwareAPI;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.Misc.ModulePubSubSystem.FieldPubSubPair;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class VirtualBot implements Module {
    private boolean isFirstRun = true;
    private VirtualMcuTopModule mcuTopModule;

    /* prepare the main pubsubs */
    private FieldPubSubPair<FirmwareAPI.FirmwareCommand> firmCmdPubSubPair =
                new FieldPubSubPair<>("[Pair]DefinedIn:VirtualBot", "FirmCmd",
                        FirmwareAPI.FirmwareCommand.newBuilder()
                                .setInit(false).setVx(0.0f).setVy(0.0f).setW(0.0f)
                                .setKx(0.0f).setKz(0.0f).setDribbler(false).build()
                );
    private FieldPubSubPair<FirmwareAPI.FirmwareData> firmDataPubSubPair =
            new FieldPubSubPair<>("[Pair]DefinedIn:VirtualBot", "FirmData",
                        FirmwareAPI.FirmwareData.newBuilder()
                            .setEncX(0.0f).setEncY(0.0f).setImuTheta(0.0f).setImuOmega(0.0f)
                            .setImuAx(0.0f).setImuAy(0.0f).setIsHoldingball(false).build()
                );

    public VirtualBot(Config config, int id) {
        mcuTopModule = new VirtualMcuTopModule(config, id, firmCmdPubSubPair.pub, firmDataPubSubPair.sub);
    }

    public boolean isConnectedToTritonBot() {
        return mcuTopModule.isConnectedToTritonBot();
    }


    private void setup() {
        ScheduledFuture<?> sendTCPFuture = App.threadPool.scheduleAtFixedRate(
                mcuTopModule
            ,0, Util.toPeriod(GvcModuleFreqs.VIRTUAL_MCU_TOP_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

    @Override
    public void run() {
        if(isFirstRun) {
            setup();
            isFirstRun = false;
        }


    }
}
