package com.symmetrylabs.util.dmx;

import java.util.Arrays;

public class DMXStream {

    public final int[] data = new int[512];

    private final int[] lastSentData = new int[data.length];
    private final int[] cleanData = new int[data.length];
    private final boolean[] externalIsDirty = new boolean[data.length];
    private boolean forceDirty;

    void onDataReceived(DMXDataSnapshot snapshot) {
        for (int i = 0; i < snapshot.length; i++) {
            if (externalIsDirty[i]) {
                if (snapshot.data[i] == data[i]) {
                    externalIsDirty[i] = false;
                }
            } else if (snapshot.data[i] != data[i]) {
                data[i] = snapshot.data[i];
            }
        }
    }

    void checkForDataChanges() {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != cleanData[i]) {
                externalIsDirty[i] = true;
            }
        }
    }

    void storeCleanData() {
        System.arraycopy(data, 0, cleanData, 0, data.length);
    }

    boolean isOutputDirty() {
        if (forceDirty) {
            return true;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] != lastSentData[i]) {
                return true;
            }
        }
        return false;
    }

    void markDataSent() {
        System.arraycopy(data, 0, lastSentData, 0, data.length);
        forceDirty = false;
    }

    public DMXDataSnapshot getDataSnapshot() {
        return new DMXDataSnapshot(Arrays.copyOf(data, data.length));
    }

    public void forceDirty() {
        forceDirty = true;
    }
}
