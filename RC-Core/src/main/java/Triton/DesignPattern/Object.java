package Triton.DesignPattern;

public interface Object {
    
    public void observe(Subject subject);

    public void disregard(Subject subject);

    public abstract void update(Subject subject);
}