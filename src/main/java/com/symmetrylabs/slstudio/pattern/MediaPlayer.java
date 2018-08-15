package com.symmetrylabs.slstudio.pattern;

import com.sun.jna.Memory;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.transform.LXVector;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import java.io.File;
import java.nio.IntBuffer;

public class MediaPlayer extends SLPattern<SLModel> {
    private boolean enabled = false;
    private double time = 0;
    private double videoLengthMs = 0;

    /* simple double-buffer for thread-safe frame update */
    private int[] front = null;
    private int[] back = null;
    private int width;

    private DirectMediaPlayerComponent mediaPlayerComponent;

    public MediaPlayer(LX lx) {
        super(lx);

        File file = new File("C:/Users/willh/code/Symmetry/cube-arig-usc-v1.mp4");

        if (!new NativeDiscovery().discover()) {
            SLStudio.setWarning("MediaPlayer", "VLC not installed or not found");
            return;
        }
        enabled = true;

        mediaPlayerComponent = new DirectMediaPlayerComponent((w, h) -> {
            System.out.println(String.format("DMPC w=%d h=%d", w, h));
            return new RV32BufferFormat(w, h);
        }) {
            @Override
            public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
                Memory byteBuf = nativeBuffers[0];
                IntBuffer intBuf = byteBuf.getByteBuffer(0, byteBuf.size()).asIntBuffer();

                if (back == null || back.length != intBuf.limit()) {
                    System.out.println("MediaPlayer: realloc image buffer");
                    back = new int[intBuf.limit()];
                    front = back;
                }
                intBuf.get(back);
                width = bufferFormat.getWidth();
            }
        };
    }

    @Override
    public void run(double elapsedMs) {
        if (!enabled) return;

        if (!mediaPlayerComponent.getMediaPlayer().isPlaying()) {
            mediaPlayerComponent.getMediaPlayer().playMedia("../cube-arig-usc-v1.mp4");
            mediaPlayerComponent.getMediaPlayer().skip(30000);
        }

        time += elapsedMs;
        while (time > videoLengthMs) {
            time -= videoLengthMs;
        }

        if (front == null) return;

        for (LXVector v : getVectors()) {
            int i = (int) v.y;
            int j = (int) v.x;
            int dataIdx = 3 * (width * i + j);
            int vcolor = front[dataIdx];

            colors[v.index] = LXColor.rgb(
                vcolor & 0xFF,
                (vcolor >> 8) & 0xFF,
                (vcolor >> 16) & 0xFF);
        }
    }
}
