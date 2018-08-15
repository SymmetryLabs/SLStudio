package com.symmetrylabs.slstudio.pattern;

import com.jogamp.common.net.Uri;
import com.jogamp.opengl.util.av.GLMediaPlayer;
import com.jogamp.opengl.util.av.GLMediaPlayerFactory;
import com.jogamp.opengl.util.texture.TextureSequence;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.p3lx.P3LX;
import processing.opengl.PJOGL;

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

        mediaPlayer.addEventListener(new GLMediaPlayer.GLMediaEventListener() {
            @Override
            public void attributesChanged(GLMediaPlayer glMediaPlayer, int i, long l) {
                if ((i & EVENT_CHANGE_EOS) != 0) {
                    System.out.println("end of stream");
                }
                if ((i & EVENT_CHANGE_ERR) != 0) {
                    System.out.println("stream error in GL player");
                }
                if ((i & EVENT_CHANGE_INIT) != 0) {
                    System.out.println("stream initialized");
                }
            }

            @Override
            public void newFrameAvailable(GLMediaPlayer glMediaPlayer, TextureSequence.TextureFrame textureFrame, long l) {
                System.out.println("new frame available");
            }
        });

        mediaPlayer.initStream(
            mediaUri,
            GLMediaPlayer.STREAM_ID_AUTO,
            GLMediaPlayer.STREAM_ID_NONE,
            GLMediaPlayer.TEXTURE_COUNT_DEFAULT);
        try {
            mediaPlayer.initGL(((PJOGL) ((P3LX) lx).applet.beginPGL()).gl);
        } catch (GLMediaPlayer.StreamException e) {
            System.out.println("couldn't initialize GL for media player:");
            e.printStackTrace();
        }
    }

    @Override
    public void run(double elapsedMs) {
    }
}
