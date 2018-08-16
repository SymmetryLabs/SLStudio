package com.symmetrylabs.slstudio.pattern;

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
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import java.io.File;
import java.nio.IntBuffer;

public class MediaPlayer extends SLPattern<SLModel> {
    CompoundParameter scaleParam = new CompoundParameter("scale", 0.1, 1, 4);

    private int[] buf = null;
    private int width;

    private DirectMediaPlayerComponent mediaPlayerComponent;
    private DirectMediaPlayer mediaPlayer;

    public MediaPlayer(LX lx) {
        super(lx);

        addParameter(scaleParam);

        if (!new NativeDiscovery().discover()) {
            SLStudio.setWarning("MediaPlayer", "VLC not installed or not found");
            return;
        }

        mediaPlayerComponent = new DirectMediaPlayerComponent((w, h) -> {
            System.out.println(String.format("DMPC w=%d h=%d", w, h));
            return new RV32BufferFormat(w, h);
        }) {
            @Override
            public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
                Memory byteBuf = nativeBuffers[0];
                IntBuffer intBuf = byteBuf.getByteBuffer(0, byteBuf.size()).asIntBuffer();

                if (buf == null || buf.length != intBuf.limit()) {
                    System.out.println("MediaPlayer: realloc image buffer");
                    buf = new int[intBuf.limit()];
                }
                intBuf.get(buf);
                width = bufferFormat.getWidth();
            }
        };
        mediaPlayer = mediaPlayerComponent.getMediaPlayer();
        mediaPlayer.prepareMedia("../cube-arig-usc-v1.mp4");
    }

    @Override
    public void onActive() {
        mediaPlayer.setPosition(0);
        mediaPlayer.play();
    }

    @Override
    public void onInactive() {
        mediaPlayer.stop();
    }

    @Override
    public void run(double elapsedMs) {
        if (buf == null) {
            return;
        }

        float scale = scaleParam.getValuef();
        for (LXVector v : getVectors()) {
            int i = (int) (scale * v.y);
            int j = (int) (scale * v.x);
            int dataIdx = width * i + j;
            int vcolor = buf[dataIdx];

            colors[v.index] = LXColor.rgb(
                (vcolor >> 16) & 0xFF,
                (vcolor >>  8) & 0xFF,
                (vcolor      ) & 0xFF);
        }
    }
}
