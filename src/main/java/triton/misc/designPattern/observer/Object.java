package triton.misc.designPattern.observer;

public interface Object {

    void observe(Subject subject);

    void disregard(Subject subject);

    void update(Subject subject);
}