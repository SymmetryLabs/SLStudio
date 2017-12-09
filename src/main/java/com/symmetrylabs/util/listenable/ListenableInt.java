package com.symmetrylabs.util.listenable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ListenableInt {

    private int value;

    private final List<IntListener> listeners = new ArrayList<IntListener>();

    public ListenableInt() {
        this(0);
    }

    public ListenableInt(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
        for (IntListener listener : listeners) {
            listener.onChange(value);
        }
    }

    public void increment() {
        set(get() + 1);
    }

    public void decrement() {
        set(get() - 1);
    }

    public String toString() {
        return Integer.toString(value);
    }

    public final ListenableInt addListener(IntListener listener) {
        listeners.add(listener);
        return this;
    }

    public final ListenableInt addListenerWithInit(IntListener listener) {
        listeners.add(listener);
        listener.onChange(value);
        return this;
    }

    public final ListenableInt removeListener(IntListener listener) {
        listeners.remove(listener);
        return this;
    }

}
