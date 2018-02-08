package com.symmetrylabs.util.listenable;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class ListenableList<E> implements Collection<E> {

    public final List<E> list = new ArrayList<E>();
    private final List<ListListener<E>> listeners = new ArrayList<ListListener<E>>();

    public int size() {
        return list.size();
    }
    public boolean isEmpty() {
        return list.isEmpty();
    }
    public boolean contains(Object o) {
        return list.contains(o);
    }
    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }
    public E get(int index) {
        return list.get(index);
    }
    public int indexOf(Object o) {
        return list.indexOf(o);
    }
    public Object[] toArray() { return list.toArray(); }
    public <E> E[] toArray(E[] array) { return list.toArray(array); }

    public void add(int index, E element) {
        list.add(index, element);
        for (ListListener<E> listener : listeners) {
            listener.itemAdded(index, element);
        }
    }

    public boolean add(E element) {
        list.add(element);
        for (ListListener<E> listener : listeners) {
            listener.itemAdded(list.size() - 1, element);
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        for (E element : c) {
            add(element);
        }
        return true;
    }

    public E remove(int index) {
        E element = list.remove(index);
        for (ListListener<E> listener : listeners) {
            listener.itemRemoved(index, element);
        }
        return element;
    }

    public boolean remove(Object o) {
        int index = list.indexOf(o);
        if (index != -1) {
            remove(index);
            return true;
        }
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        boolean removedAny = false;
        for (Object element : c) {
            removedAny = removedAny || remove(element);
        }
        return removedAny;
    }

    public boolean retainAll(Collection<?> c) {
        List<E> toRemove = new ArrayList<>();
        for (E element : list) {
            if (!c.contains(element)) {
                toRemove.add(element);
            }
        }
        for (E element : toRemove) {
            remove(element);
        }
        return toRemove.size() > 0;
    }

    public void clear() {
        while (!isEmpty()) {
            remove(0);
        }
    }

    public final ListenableList<E> addListener(ListListener<E> listener) {
        listeners.add(listener);
        return this;
    }

    public final ListenableList<E> addListenerWithInit(ListListener<E> listener) {
        listeners.add(listener);
        int index = 0;
        for (E element : list) {
            listener.itemAdded(index++, element);
        }
        return this;
    }

    public final ListenableList<E> removeListener(ListListener<E> listener) {
        listeners.remove(listener);
        return this;
    }

    public Iterator<E> iterator() {
        return list.iterator();
    }

}
