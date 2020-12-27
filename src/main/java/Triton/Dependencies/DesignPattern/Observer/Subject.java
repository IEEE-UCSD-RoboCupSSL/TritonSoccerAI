package Triton.Dependencies.DesignPattern.Observer;

import java.util.ArrayList;
import java.util.List;

public class Subject {

    protected List<Object> observers = new ArrayList<>();

    protected void attach(Object observer) {
        observers.add(observer);
    }
    
    protected void detach(Object observer) {
        observers.remove(observer);
    }

    protected void notifyAllObservers() {
        for(Object o : observers) {
            o.update(this);
        }
    }
}