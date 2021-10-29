package triton.manualTests.periphMiscTests.periphTests;

import triton.config.Config;
import triton.legacy.oldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionBall;
import triton.App;
import triton.config.globalVariblesAndConstants.GvcModuleFreqs;
import triton.manualTests.TritonTestable;
import triton.misc.math.coordinates.PerspectiveConverter;
import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.modulePubSubSystem.MQSubscriber;
import triton.misc.modulePubSubSystem.Subscriber;
import triton.periphModules.vision.GrSimVisionModule_OldProto;
import triton.Util;
import triton.legacy.oldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static triton.Util.delay;


public class GrSimVisionModuleTest_OldProto implements TritonTestable {
    public boolean test(Config config) {
        GrSimVisionModule_OldProto grSimVisionModuleOldProto = new GrSimVisionModule_OldProto(config);
        App.threadPool.scheduleAtFixedRate(
                grSimVisionModuleOldProto,
            0, Util.toPeriod(GvcModuleFreqs.VISION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        Subscriber<SSL_DetectionFrame> visionSub =
                new MQSubscriber<SSL_DetectionFrame>("From:GrSimVisionModule_OldProto", "Detection");

        try {
            visionSub.subscribe(1000);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        long time = System.currentTimeMillis();
        Vec2D pos = new Vec2D(0, 0);
        while (true) {
            SSL_DetectionFrame frame = visionSub.getMsg();
            if (frame.getBallsCount() > 0) {
                SSL_DetectionBall detection = frame.getBalls(0);
                Vec2D audienceBallPos = new Vec2D(detection.getX(), detection.getY());
                Vec2D currPos = PerspectiveConverter.audienceToPlayer(audienceBallPos);
                System.out.println(currPos);

//                System.out.println((System.currentTimeMillis() - time) + " " + currPos.sub(pos));
//                time = System.currentTimeMillis();
//                pos = currPos;
            }
            delay(3);
        }
    }
}
