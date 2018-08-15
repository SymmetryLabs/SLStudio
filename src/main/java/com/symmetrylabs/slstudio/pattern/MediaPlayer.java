package com.symmetrylabs.slstudio.pattern;

import com.jogamp.common.net.Uri;
import com.jogamp.opengl.util.av.GLMediaPlayer;
import com.jogamp.opengl.util.av.GLMediaPlayerFactory;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;

import java.net.URISyntaxException;

public class MediaPlayer extends SLPattern<SLModel> {
    GLMediaPlayer mediaPlayer;

    public MediaPlayer(LX lx) {
        super(lx);
        mediaPlayer = GLMediaPlayerFactory.createDefault();

        Uri mediaUri = null;
        try {
            mediaUri = Uri.create("file", "", "/Users/willh/Downloads/LED-Cube-Test_Arig-USC-v1.mov", "");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mediaPlayer.initStream(
            mediaUri,
            GLMediaPlayer.STREAM_ID_AUTO,
            GLMediaPlayer.STREAM_ID_NONE,
            GLMediaPlayer.TEXTURE_COUNT_DEFAULT);
    }

    @Override
    public void run(double elapsedMs) {
    }
}
