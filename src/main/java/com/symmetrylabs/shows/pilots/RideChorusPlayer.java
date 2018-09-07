package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.pattern.VideoPlayer;
import heronarts.lx.LX;

public class RideChorusPlayer extends VideoPlayer {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    public RideChorusPlayer(LX lx) {
        super(lx);

        removeParameter(chooseFileParam);
        removeParameter(captureParam);

        mediaUrl = "../14_Ride_Output_01_V3.mov";
        mediaOptions = null;
        cropTop = 610;
        cropLeft = 32;
        cropRight = 355;
        cropBottom = 32;

        playParam.setValue(false);
    }

    @Override
    public long getStartTimeMs() {
        return 54_000L;
    }
}
