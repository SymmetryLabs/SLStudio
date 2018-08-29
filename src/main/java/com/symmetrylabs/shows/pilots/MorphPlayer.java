package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.pattern.VideoPlayer;
import heronarts.lx.LX;

public class MorphPlayer extends VideoPlayer {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    public MorphPlayer(LX lx) {
        super(lx);

        removeParameter(chooseFileParam);
        removeParameter(captureParam);

        mediaUrl = "../cube-arig-usc-v1.mp4";
        mediaOptions = null;
        cropTop = 20;
        cropLeft = 16;
        cropRight = 492;
        cropBottom = 483;

        playParam.setValue(false);
    }

    @Override
    public long getStartTimeMs() {
        return 0L;
    }
}
