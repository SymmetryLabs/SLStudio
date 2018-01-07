package com.symmetrylabs.slstudio.model;


public interface BatchConsumer {
    public void accept(int start, int end);
}
