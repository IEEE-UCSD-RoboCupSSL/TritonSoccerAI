package triton.misc.designPattern.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class Observer implements Object {

    protected List<Subject> subjects = new ArrayList<>();

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