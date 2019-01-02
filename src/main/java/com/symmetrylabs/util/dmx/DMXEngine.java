package com.symmetrylabs.util.dmx;

import java.util.ArrayList;
import java.util.List;

public class DMXEngine {

    private final DMXStream stream = new DMXStream();

    private List<DMXHandler> handlers = new ArrayList<>();

    public void onDataReceived(DMXDataSnapshot data) {
        stream.onDataReceived(data);
        for (DMXHandler handler : handlers) {
            handler.onDMXDataReceived(stream);
        }
    }

    public void addHandler(DMXHandler handler) {
        handlers.add(handler);
        handler.onDMXStreamChanged(stream);
    }

    public void removeHandler(DMXHandler handler) {
        handlers.remove(handler);
        handler.onDMXStreamChanged(null);
    }

    public DMXStream getStream() {
        return stream;
    }

    public void forceDirty() {
        stream.forceDirty();
    }

    public boolean isOutputDirty() {
        return stream.isOutputDirty();
    }

    public void checkForDataChanges() {
        stream.checkForDataChanges();
    }

    public void storeCleanData() {
        stream.storeCleanData();
    }

    public void markDataSent() {
        stream.markDataSent();
    }

    public DMXDataSnapshot getDataSnapshot() {
        return stream.getDataSnapshot();
    }

}
