package triton.periphModules.vision;

import triton.misc.modulePubSubSystem.Module;

import java.io.IOException;
import java.net.SocketTimeoutException;

public abstract class VisionModule implements Module {
    public void run() {
        try {
            //System.out.println("prepare to update");
            update();
            //System.out.println("received");
        } catch (SocketTimeoutException e) {
            System.err.println("SSL Vision Multicast Timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive a single packet, and publish it to proper subscribers
     */
    protected abstract void update() throws IOException;
}
