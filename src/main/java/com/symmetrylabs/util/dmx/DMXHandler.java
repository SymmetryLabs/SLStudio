package com.symmetrylabs.util.dmx;

public interface DMXHandler {

    default void onDMXStreamChanged(DMXStream stream) {}
    default void onDMXDataReceived(DMXStream stream) {}

}
