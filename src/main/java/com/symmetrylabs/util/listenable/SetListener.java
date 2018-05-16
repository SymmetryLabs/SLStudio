package com.symmetrylabs.util.listenable;

public interface SetListener<E> {
    void onItemAdded(E element);
    void onItemRemoved(E element);
}
