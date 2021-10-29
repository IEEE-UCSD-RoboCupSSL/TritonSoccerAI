package triton.manualTests.periphMiscTests;

import proto.SslVisionDetection;
import triton.App;
import triton.config.Config;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.manualTests.TritonTestable;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.modulePubSubSystem.MQSubscriber;
import triton.misc.modulePubSubSystem.Subscriber;
import triton.periphModules.vision.ERForceVisionModule;
import triton.Util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static triton.Util.delay;


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
