package Triton.ManualTests.PeriphMiscTests;

import Proto.SslVisionDetection;
import Triton.App;
import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.ManualTests.TritonTestable;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.MQSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.PeriphModules.Vision.ERForceVisionModule;
import Triton.Util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static Triton.Util.delay;


public class ErForceSimVisionModuleTest implements TritonTestable {
    public boolean test(Config config) {
        ERForceVisionModule erForceSimVisionModule = new ERForceVisionModule(config);
        App.threadPool.scheduleAtFixedRate(
                erForceSimVisionModule,
            0, Util.toPeriod(GvcModuleFreqs.VISION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        Subscriber<SslVisionDetection.SSL_DetectionFrame> visionSub =
                new MQSubscriber<SslVisionDetection.SSL_DetectionFrame>("From:ERForceVisionModule", "Detection");

        try {
            visionSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
//
//        long time = System.currentTimeMillis();
//        Vec2D pos = new Vec2D(0, 0);
        while (true) {
            SslVisionDetection.SSL_DetectionFrame frame = visionSub.getMsg();
            if(frame != null) {
                if (frame.getBallsCount() > 0) {
                    SslVisionDetection.SSL_DetectionBall detection = frame.getBalls(0);
                    Vec2D audienceBallPos = new Vec2D(detection.getX(), detection.getY());
//                    Vec2D currPos = PerspectiveConverter.audienceToPlayer(audienceBallPos);
                    System.out.println("Ball: " + audienceBallPos);

//                System.out.println((System.currentTimeMillis() - time) + " " + currPos.sub(pos));
//                time = System.currentTimeMillis();
//                pos = currPos;
                }
                Vec2D bbot0 = new Vec2D(frame.getRobotsBlue(0).getX(), frame.getRobotsBlue(0).getY());
                System.out.println("BLUE Bot0: " + bbot0);
            }
            delay(3);
        }
    }
}
