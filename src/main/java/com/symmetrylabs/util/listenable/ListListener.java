package com.symmetrylabs.util.listenable;


public interface ListListener<E> {
    public void itemAdded(int index, E element);

    public void itemRemoved(int index, E element);
}
