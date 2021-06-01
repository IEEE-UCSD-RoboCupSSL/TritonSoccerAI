package Triton.VirtualBot;

import Triton.App;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.CoreModules.Robot.Robot;
import Triton.Util;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class VirtualBotList extends ArrayList<VirtualBot> {

    public ArrayList<ScheduledFuture<?>> runAll() {
        ArrayList<ScheduledFuture<?>> botFutures = new ArrayList<>();
        for (VirtualBot bot : this) {
            ScheduledFuture<?> robotFuture = App.threadPool.scheduleAtFixedRate(
                        bot,
                    0, Util.toPeriod(GvcModuleFreqs.VIRTUAL_BOT_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            botFutures.add(robotFuture);
        }
        return botFutures;
    }

    public boolean areAllConnectedToTritonBots() {
        for(VirtualBot bot : this) {
            if(!bot.isConnectedToTritonBot()) return false;
        }
        return true;
    }


}
