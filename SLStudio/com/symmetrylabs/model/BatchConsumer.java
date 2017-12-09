package com.symmetrylabs.model;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface BatchConsumer {
    public void accept(int start, int end);
}
