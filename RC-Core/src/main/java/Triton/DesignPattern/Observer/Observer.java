package Triton.DesignPattern.Observer;

import java.util.List;
import java.util.ArrayList;

public abstract class Observer implements Object {

    protected List<Subject> subjects = new ArrayList<Subject>();

    @Override
    public void observe(Subject subject) {
        subjects.add(subject);
        subject.attach(this);
    }

    @Override
    public void disregard(Subject subject) {
        subjects.remove(subject);
        subject.detach(this);
    }

    @Override
    public abstract void update(Subject subject);
}