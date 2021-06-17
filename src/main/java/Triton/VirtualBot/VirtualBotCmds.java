package Triton.VirtualBot;

public class VirtualBotCmds {
    private float velX, velY, velAng, kickX, kickZ;
    private boolean spinner;

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

    public float getKickX() {
        return kickX;
    }

    public void setKickX(float kickX) {
        this.kickX = kickX;
    }

    public float getKickZ() {
        return kickZ;
    }

    public void setKickZ(float kickZ) {
        this.kickZ = kickZ;
    }

    public boolean getSpinner() {
        return spinner;
    }

    public void setSpinner(boolean spinner) {
        this.spinner = spinner;
    }

    @Override
    public String toString() {
        return "VirtualBotCmds{" +
                "velX=" + velX +
                ", velY=" + velY +
                ", velAng=" + velAng +
                ", kickX=" + kickX +
                ", kickZ=" + kickZ +
                ", spinner=" + spinner +
                '}';
    }
}
