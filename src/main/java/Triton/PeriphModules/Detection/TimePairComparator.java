package Triton.PeriphModules.Detection;

import org.javatuples.Pair;

import java.util.Comparator;

class TimePairComparator<T> implements Comparator<Pair<T, Double>> {
    @Override
    public int compare(Pair<T, Double> o1, Pair<T, Double> o2) {
        return Double.compare(o1.getValue1(), o2.getValue1());
    }
}
