package com.symmetrylabs.slstudio.pattern;

import com.google.gson.JsonObject;
import com.sun.jna.Memory;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.swing.*;
import java.nio.IntBuffer;

public class VideoPlayer extends SLPattern<SLModel> {
    CompoundParameter shrinkParam = new CompoundParameter("shrink", 1, 0.1, 20);
    CompoundParameter yOffsetParam = new CompoundParameter("yoff", 0, 0, 1);

    private int[] buf = null;
    private int width;
    private int height;

    private DirectMediaPlayerComponent mediaPlayerComponent;
    private DirectMediaPlayer mediaPlayer;
    private String mediaFileName;

    public VideoPlayer(LX lx) {
        super(lx);

        addParameter(shrinkParam);
        addParameter(yOffsetParam);

        if (!new NativeDiscovery().discover()) {
            SLStudio.setWarning("VideoPlayer", "VLC not installed or not found");
            return;
        }

        mediaPlayerComponent = new DirectMediaPlayerComponent((w, h) -> {
            System.out.println(String.format("DMPC w=%d h=%d", w, h));
            float fitWidth = w / model.xRange;
            float fitHeight = h / model.yRange;
            float fit = Float.min(fitWidth, fitHeight);
            shrinkParam.setValue(fit);
            return new RV32BufferFormat(w, h);
        }) {
            @Override
            public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
                Memory byteBuf = nativeBuffers[0];
                IntBuffer intBuf = byteBuf.getByteBuffer(0, byteBuf.size()).asIntBuffer();

                width = bufferFormat.getWidth();
                height = bufferFormat.getHeight();

                if (buf == null || buf.length != intBuf.limit()) {
                    System.out.println("VideoPlayer: realloc image buffer");
                    buf = new int[intBuf.limit()];
                }
                intBuf.get(buf);
            }
        };
        mediaPlayer = mediaPlayerComponent.getMediaPlayer();
    }

    @Override
    public void onActive() {
        if (mediaFileName == null) {
            JFileChooser jfc = new JFileChooser();
            int res = jfc.showOpenDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                mediaFileName = jfc.getSelectedFile().getAbsolutePath();
            }
        }
        if (mediaFileName != null) {
            if (!mediaPlayer.isPlayable()) {
                mediaPlayer.playMedia(mediaFileName);
            } else {
                mediaPlayer.setPosition(0);
                mediaPlayer.play();
            }
        }
    }

    @Override
    public void onInactive() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private static final String KEY_VIDEO_FILE = "videoFile";

    @Override
    public void save(LX lx, JsonObject json) {
        super.load(lx, json);
        if (json != null && mediaFileName != null) {
            json.addProperty(KEY_VIDEO_FILE, mediaFileName);
        }
    }

    @Override
    public void load(LX lx, JsonObject json) {
        super.load(lx, json);
        if (json != null && json.has(KEY_VIDEO_FILE)) {
            mediaFileName = json.get(KEY_VIDEO_FILE).getAsString();
        }
    }

    @Override
    public void run(double elapsedMs) {
        if (buf == null) {
            return;
        }

        float shrink = shrinkParam.getValuef();
        for (LXVector v : getVectors()) {
            int i = (int) ((shrink * (model.yMax - v.y)) + yOffsetParam.getValue() * height);
            int j = (int) (shrink * (v.x - model.xMin));

            int color;
            if (i >= height || j >= width || i < 0 || j < 0) {
                color = LXColor.gray(0);
            } else {
                int vcolor = buf[width * i + j];
                color = LXColor.rgb(
                    (vcolor >> 16) & 0xFF,
                    (vcolor >> 8) & 0xFF,
                    vcolor & 0xFF);
            }
            colors[v.index] = color;
        }
    }
}
