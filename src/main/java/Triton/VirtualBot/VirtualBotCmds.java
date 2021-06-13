package Triton.VirtualBot;

import Triton.Misc.Math.Matrix.Vec2D;

public class VirtualBotCmds {
    private float velX, velY, velAng;

    public VirtualBotCmds() {
        velX = 0;
        velY = 0;
        velAng = 0;
    }

    public float getVelX() {
        return velX;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public float getVelY() {
        return velY;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public float getVelAng() {
        return velAng;
    }

    public void setVelAng(float velAng) {
        this.velAng = velAng;
    }

    @Override
    public String toString() {
        return "VirtualBotCmds{" +
                "velX=" + velX +
                ", velY=" + velY +
                ", velAng=" + velAng +
                '}';
    }
}
