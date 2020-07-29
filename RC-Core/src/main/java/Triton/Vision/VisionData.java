package Triton.Vision;

import Triton.DesignPattern.*;

import Proto.MessagesRobocupSslDetection.*;
import Proto.MessagesRobocupSslGeometry.*;

public class VisionData extends AbstractData {

    private SSL_DetectionFrame detectFrame;
    private SSL_GeometryData   geoData;

    public VisionData() {}

    public VisionData(SSL_DetectionFrame detectFrame, SSL_GeometryData geoData) {
        lock.writeLock().lock();
        try {
            this.detectFrame = detectFrame;
            this.geoData = geoData;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setDetection(SSL_DetectionFrame detectFrame) {
        lock.writeLock().lock();
        try {
            this.detectFrame = detectFrame;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public SSL_DetectionFrame getDetection() {
        lock.readLock().lock();
        try {
            return this.detectFrame;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setGeometry(SSL_GeometryData geoData) {
        lock.writeLock().lock();
        try {
            this.geoData = geoData;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public SSL_GeometryData getGeometry() {
        lock.readLock().lock();
        try {
            return geoData;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static VisionData get() {
        return (VisionData) MsgChannel.get("Vision");
    }

    public static void publish(VisionData data) {
        MsgChannel.publish("Vision", data);
    }
}