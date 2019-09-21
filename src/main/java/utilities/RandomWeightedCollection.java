package utilities;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomWeightedCollection<E> {

    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    @Getter(AccessLevel.PUBLIC) private double total;

    public RandomWeightedCollection(Random random) {

        this.random = random;
        this.total = 0;
    }

    public RandomWeightedCollection<E> add(double weight, E result) {
        if (weight <= 0) return this;

        total += weight;
        map.put(total, result);

        return this;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}