package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.transform.LXVector;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.SeekableDemuxerTrack;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;

import java.io.File;
import java.io.IOException;

public class MediaPlayer extends SLPattern<SLModel> {
    private FrameGrab video;
    private SeekableDemuxerTrack track;
    private double time = 0;
    private double videoLengthMs = 0;

    public MediaPlayer(LX lx) {
        super(lx);

        File file = new File("C:/Users/willh/code/Symmetry/cube-arig-usc-v1.mp4");
        try {
            video = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
            System.out.println("loaded video");
        } catch (JCodecException | IOException e) {
            e.printStackTrace();
            video = null;
        }

        if (video != null) {
            track = video.getVideoTrack();
            videoLengthMs = track.getMeta().getTotalDuration() * 1000;

            /*
            try {
                video.seekToSecondSloppy(30.0);
                Picture nativeFrame = video.getNativeFrame();
                Transform xform = ColorUtil.getTransform(nativeFrame.getColor(), ColorSpace.RGB);
                picture = Picture.create(nativeFrame.getWidth(), nativeFrame.getHeight(), ColorSpace.RGB);
                xform.transform(nativeFrame, picture);
            } catch (IOException | JCodecException e) {
                e.printStackTrace();
            }
            */
        }
    }

    @Override
    public void run(double elapsedMs) {
        time += elapsedMs;
        while (time > videoLengthMs) {
            time -= videoLengthMs;
        }

        Picture nativeFrame;
        try {
            video.getVideoTrack().nextFrame();
            nativeFrame = video.getNativeFrame();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Transform xform = ColorUtil.getTransform(nativeFrame.getColor(), ColorSpace.RGB);
        Picture picture = Picture.create(nativeFrame.getWidth(), nativeFrame.getHeight(), ColorSpace.RGB);
        xform.transform(nativeFrame, picture);
        byte[] data = picture.getPlaneData(0);

        for (LXVector v : getVectors()) {
            int i = (int) v.y;
            int j = (int) v.x;
            int dataIdx = 3 * (picture.getWidth() * i + j);
            colors[v.index] = LXColor.rgb(
                data[dataIdx + 0] + 128,
                data[dataIdx + 1] + 128,
                data[dataIdx + 2] + 128);
        }
    }
}
