package Triton.VirtualBot;

import Triton.Misc.Math.Matrix.Vec2D;

public class VirtualBotCmds {
    private int id;
    private float velX, velY, velAng;

    public VirtualBotCmds(int id) {
        this.id = id;
        velX = 0;
        velY = 0;
        velAng = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
