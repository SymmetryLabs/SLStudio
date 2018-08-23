package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.pattern.VideoPlayer;
import heronarts.lx.LX;

public class HoldingOntoYouPlayer extends VideoPlayer {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    public HoldingOntoYouPlayer(LX lx) {
        super(lx);

        removeParameter(chooseFileParam);
        removeParameter(captureParam);

        mediaUrl = "../12_HoldingOntoYou01-HD_V2.mov";
        mediaOptions = null;
        cropTop = 1080 / 2;
        cropLeft = 0;
        cropRight = 0;
        cropBottom = 0;

        playParam.setValue(false);
    }

    @Override
    public long getStartTimeMs() {
        return 0L;
    }
}
