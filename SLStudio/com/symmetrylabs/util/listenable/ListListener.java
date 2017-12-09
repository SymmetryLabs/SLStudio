package com.symmetrylabs.util.listenable;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public static interface ListListener<E> {
    public void itemAdded(int index, E element);

    public void itemRemoved(int index, E element);
}
