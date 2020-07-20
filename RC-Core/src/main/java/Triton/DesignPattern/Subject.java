package Triton.DesignPattern;

import java.util.List;
import java.util.ArrayList;

public class Subject {

    protected List<Object> observers = new ArrayList<Object>();

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