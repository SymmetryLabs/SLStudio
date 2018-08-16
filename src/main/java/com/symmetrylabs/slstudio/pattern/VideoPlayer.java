package com.symmetrylabs.slstudio.pattern;

import com.google.gson.JsonObject;
import com.sun.jna.Memory;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.swing.*;
import java.nio.IntBuffer;
import java.util.Deque;
import java.util.LinkedList;

public class VideoPlayer extends SLPattern<SLModel> {
    CompoundParameter shrinkParam = new CompoundParameter("shrink", 1, 0.1, 20);
    CompoundParameter yOffsetParam = new CompoundParameter("yoff", 0, 0, 1);
    BooleanParameter fitParam = new BooleanParameter("fit", false);
    BooleanParameter restartParam = new BooleanParameter("restart", false);

    /**
     * A guess at the amount of time it will take vlcj to start playing the
     * video. We skip the video forward by this amount to compensate for
     * the expected lag.
     */
    private static final long INITIAL_SKEW_GUESS_MS = 200;

    /**
     * Like INITIAL_SKEW_GUESS_MS, but for when we're restarting the video
     * when it's already playing instead of playing it from scratch. This
     * ends up taking a little longer that just starting the video from
     * the beginning.
     */
    private static final long RESTART_SKEW_GUESS_MS = 270;

    /**
     * Like INITIAL_SKEW_GUESS_MS, but for when the video loops
     * automatically.
     */
    private static final long LOOP_SKEW_GUESS_MS = 100;

    private int[] buf = null;
    private int width;
    private int height;
    private double time;
    private long skipOnNextFrame = 0;

    private Deque<Double> timeOffsets = new LinkedList<>();

    private DirectMediaPlayerComponent mediaPlayerComponent;
    private DirectMediaPlayer mediaPlayer;
    private String mediaFileName;

    public VideoPlayer(LX lx) {
        super(lx);

        addParameter(shrinkParam);
        addParameter(yOffsetParam);
        addParameter(fitParam);
        addParameter(restartParam);

        fitParam.setMode(BooleanParameter.Mode.MOMENTARY);
        restartParam.setMode(BooleanParameter.Mode.MOMENTARY);

        mediaPlayer = null;
        mediaPlayerComponent = null;
        mediaFileName = null;

        if (!new NativeDiscovery().discover()) {
            SLStudio.setWarning("VideoPlayer", "VLC not installed or not found");
            return;
        }

        mediaPlayerComponent = new DirectMediaPlayerComponent((w, h) -> {
            System.out.println(String.format("DMPC w=%d h=%d", w, h));
            width = w;
            height = h;
            setShrinkToFit();
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
    public void onParameterChanged(LXParameter p) {
        if (p == fitParam && fitParam.getValueb()) {
            setShrinkToFit();
        }
        if (p == restartParam && restartParam.getValueb()) {
            restartVideo();
        }
    }

    @Override
    public void onActive() {
        super.onActive();
        if (mediaFileName == null) {
            JFileChooser jfc = new JFileChooser();
            int res = jfc.showOpenDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                mediaFileName = jfc.getSelectedFile().getAbsolutePath();
            }
        }
        restartVideo();
    }

    @Override
    public void onInactive() {
        super.onInactive();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void setShrinkToFit() {
        float fitWidth = width / model.xRange;
        float fitHeight = height / model.yRange;
        float fit = Float.min(fitWidth, fitHeight);
        shrinkParam.setValue(fit);
    }

    private void restartVideo() {
        if (mediaPlayer == null)
            return;
        if (mediaFileName != null) {
            long skewGuess = 0;

            if (!mediaPlayer.isPlayable()) {
                mediaPlayer.prepareMedia(mediaFileName);
                mediaPlayer.mute(true);
                mediaPlayer.setRepeat(true);
                mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                    @Override
                    public void finished(MediaPlayer player) {
                        skipOnNextFrame = LOOP_SKEW_GUESS_MS;
                    }
                });
                skewGuess = INITIAL_SKEW_GUESS_MS;
            } else if (mediaPlayer.isPlaying()) {
                mediaPlayer.setPosition(0);
                skewGuess = RESTART_SKEW_GUESS_MS;
            } else {
                mediaPlayer.setPosition(0);
                skewGuess = INITIAL_SKEW_GUESS_MS;
            }

            mediaPlayer.play();
            mediaPlayer.skip(skewGuess);
            time = 0;
        }
    }

    private static final String KEY_VIDEO_FILE = "videoFile";

    @Override
    public void save(LX lx, JsonObject json) {
        super.save(lx, json);
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
    public String getCaption() {
        if (mediaPlayer == null) {
            return "VLC not available, video playback disabled";
        }

        double avgOffset = 0;
        synchronized (timeOffsets) {
            for (Double t : timeOffsets) {
                avgOffset += t;
            }
            avgOffset /= timeOffsets.size();
        }

        long ms = mediaPlayer.getTime();
        int s = (int) Math.floor(ms / 1000f);
        ms -= 1000f * s;
        int m = (int) Math.floor(s / 60f);
        s -= m * 60f;
        int h = (int) Math.floor((float) m / 60f);
        m -= h * 60f;
        return String.format(
            "video time: %02d:%02d:%02d.%03d average skew: %fms",
            h, m, s, ms, avgOffset);
    }

    @Override
    public void run(double elapsedMs) {
        if (buf == null) {
            return;
        }
        time += elapsedMs;
        if (time > mediaPlayer.getLength()) {
            time = 0;
        }

        if (skipOnNextFrame != 0) {
            mediaPlayer.skip(skipOnNextFrame);
            skipOnNextFrame = 0;
        }

        double delta = (double) mediaPlayer.getTime() - time;
        synchronized (timeOffsets) {
            timeOffsets.addFirst(delta);
            if (timeOffsets.size() > 1000) {
                timeOffsets.removeLast();
            }
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
