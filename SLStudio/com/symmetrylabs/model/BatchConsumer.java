package com.symmetrylabs.model;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public static interface BatchConsumer {
    public void accept(int start, int end);
}
