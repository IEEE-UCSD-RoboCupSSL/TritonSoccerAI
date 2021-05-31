package Triton.ManualTests.PeriphTests;

import Triton.Config.Config;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionBall;
import Triton.App;
import Triton.Config.GlobalVariblesAndConstants.GvcModuleFreqs;
import Triton.ManualTests.TritonTestable;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.MQSubscriber;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.PeriphModules.Vision.SSLVisionModule;
import Triton.Util;
import Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionFrame;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class SSLVisionModuleTest implements TritonTestable {
    public boolean test(Config config) {
        SSLVisionModule sslVisionModule = new SSLVisionModule(config);
        App.threadPool.scheduleAtFixedRate(
                sslVisionModule,
            0, Util.toPeriod(GvcModuleFreqs.GRSIM_VISION_MODULE_FREQ, TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        Subscriber<SSL_DetectionFrame> visionSub =
                new MQSubscriber<SSL_DetectionFrame>("vision", "detection");

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
        }
    }
}
