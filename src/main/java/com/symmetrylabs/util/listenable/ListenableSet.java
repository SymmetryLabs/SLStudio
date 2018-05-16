package com.symmetrylabs.util.listenable;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/** A set that can be monitored for item addition or item removal. */
public class ListenableSet<E> implements Collection<E> {
    public final Set<E> set = new HashSet<E>();
    private final List<SetListener<E>> listeners = new ArrayList<SetListener<E>>();

    public int size() {
        return set.size();
    }
    public boolean isEmpty() {
        return set.isEmpty();
    }
    public boolean contains(Object element) {
        return set.contains(element);
    }
    public boolean containsAll(@NotNull Collection elements) {
        return set.containsAll(elements);
    }
    public @NotNull Object[] toArray() { return set.toArray(); }
    public @NotNull <T> T[] toArray(@NotNull T[] array) { return set.toArray(array); }

    public boolean add(E element) {
        if (set.add(element)) {
            for (SetListener<E> listener : listeners) {
                listener.onItemAdded(element);
            }
            return true;
        }
        return false;
    }

    public boolean addAll(@NotNull Collection<? extends E> elements) {
        boolean addedAny = false;
        for (E element : elements) {
            addedAny = addedAny || add(element);
        }
        return addedAny;
    }

    public boolean remove(Object element) {
        if (set.remove(element)) {
            for (SetListener<E> listener : listeners) {
                listener.onItemRemoved((E) element);
            }
            return true;
        }
        return false;
    }

    public boolean removeAll(@NotNull Collection<?> elements) {
        boolean removedAny = false;
        for (Object element : elements) {
            removedAny = removedAny || remove(element);
        }
        return removedAny;
    }

    public boolean retainAll(@NotNull Collection<?> elementsToKeep) {
        List<E> toRemove = new ArrayList<>();
        for (E element : set) {
            if (!elementsToKeep.contains(element)) {
                toRemove.add(element);
            }
        }
        for (E element : toRemove) {
            remove(element);
        }
        return toRemove.size() > 0;
    }

    public void clear() {
        List<E> toRemove = new ArrayList<>(set);
        for (E element : toRemove) {
            remove(element);
        }
    }

    public final void addListener(SetListener<E> listener) {
        listeners.add(listener);
    }

    public final void addListenerWithInit(SetListener<E> listener) {
        listeners.add(listener);
        for (E element : set) {
            listener.onItemAdded(element);
        }
    }

    public final void removeListener(SetListener<E> listener) {
        listeners.remove(listener);
    }

    @NotNull
    public Iterator<E> iterator() {
        // There's no way to implement iterator().remove() in such a way that
        // the listeners are notified, so prevent it from being called.
        return new Iterator<E>() {
            Iterator<E> it = set.iterator();
            public boolean hasNext() { return it.hasNext(); }
            public E next() { return it.next(); }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
