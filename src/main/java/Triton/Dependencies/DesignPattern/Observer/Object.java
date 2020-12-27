package Triton.Dependencies.DesignPattern.Observer;

public interface Object {
    
    void observe(Subject subject);

    void disregard(Subject subject);

    void update(Subject subject);
}