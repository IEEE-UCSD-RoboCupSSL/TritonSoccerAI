package Triton.Geometry;

import Triton.DesignPattern.*;
import java.util.List;
import Proto.MessagesRobocupSslGeometry.SSL_GeometryCameraCalibration;

public class GeometryData extends AbstractData {

    public List<SSL_GeometryCameraCalibration> cameras;
    public Field field = new Field();

    public float getCameraQ0(int cameraID) {
        lock.readLock().lock();
        try {
            return this.cameras.get(cameraID).getQ0();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Field getField() {
        lock.readLock().lock();
        try {
            return field;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setField(Field field) {
        lock.writeLock().lock();
        try {
            this.field = field;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<SSL_GeometryCameraCalibration> getCameras() {
        lock.readLock().lock();
        try {
            return cameras;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setCameras(List<SSL_GeometryCameraCalibration> cameras) {
        lock.writeLock().lock();
        try {
            this.cameras = cameras;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public static GeometryData get() {
        return (GeometryData) MsgChannel.get("Geometry");
    }

    public static void publish(GeometryData data) {
        MsgChannel.publish("Geometry", data);
    }
}