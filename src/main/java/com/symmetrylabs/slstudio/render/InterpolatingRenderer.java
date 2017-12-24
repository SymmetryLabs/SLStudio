package com.symmetrylabs.slstudio.render;

import java.util.Arrays;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.util.MathUtils;
import com.symmetrylabs.slstudio.util.TripleBuffer;

public class InterpolatingRenderer extends TripleBufferedRenderer {
    private RenderFrame frameA, frameB;

    public InterpolatingRenderer(LXFixture fixture, int[] colors, com.symmetrylabs.slstudio.render.Renderable renderable) {
        super(fixture, colors, renderable);

        frameA = tripleBuffer.getSnapshotBuffer();
        frameB = frameA;
    }

    @Override
    public void run(double deltaMs) {
        // need this here for now, can't call super.run()
        synchronized (this) {
            runLoopStarted = true;
        }

        RenderFrame frame = tripleBuffer.takeSnapshot();

        if (frame.renderEndNanos != frameB.renderEndNanos) {
            frameA = frameB;
            frameB = frame.copy();
        }

        double f = (System.nanoTime() - frameB.renderEndNanos) / (double)(frameB.renderEndNanos - frameA.renderEndNanos);
        //System.out.println(f);
        //System.out.println(frameA);
        //System.out.println(frameB);

        final double fFinal = f > 1 ? 1 : f < 0 ? 0 : f;
        Arrays.parallelSetAll(colors, i -> LXColor.lerp(frameA.buffer[i], frameB.buffer[i], fFinal));
    }
}
