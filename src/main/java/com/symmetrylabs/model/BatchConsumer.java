package com.symmetrylabs.model;


public interface BatchConsumer {
    public void accept(int start, int end);
}
