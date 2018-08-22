package com.symmetrylabs.layouts.streetlamp;

import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.StreamingACNDatagram;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;

public class StreetLampLayout implements Layout {

    public SLModel buildModel() {
        List<LXPoint> points = new ArrayList<>();

        points.add(new LXPoint(0, 0, 0));
        points.add(new LXPoint(0, 0, 0));
        points.add(new LXPoint(0, 0, 0));

        return new SLModel(points);
    }

    public void setupLx(SLStudioLX lx) {
        LXDatagramOutput output = new LXDatagramOutput(lx);

        for (LXPoint point : lx.model.getPoints()) {
            output.addDatagram(new RGBWACNDatagram(lx.model));
        }

        lx.addOutput(output);
    }

    static class RGBWACNDatagram extends StreamingACNDatagram {
        protected RGBWACNDatagram copyPoints(int[] colors, int[] pointIndices, int offset) {
            int i = offset;
            int[] byteOffset = BYTE_ORDERING[this.byteOrder.ordinal()];

            for (int index : pointIndices) {
                int color = (index >= 0) ? colors[index] : 0;
                float saturation = LXColor.s(color) / 100;
                float brightness = LXColor.b(color) / 100;

                byte red = (byte)(((color >> 16) & 0xff) * saturation);
                byte green = (byte)(((color >> 8) & 0xff) * saturation);
                byte blue = (byte)((color & 0xff) * saturation);
                byte white = (byte)((1 - saturation) * brightness * 255);

                this.buffer[i + byteOffset[0]] = red;
                this.buffer[i + byteOffset[1]] = green;
                this.buffer[i + byteOffset[2]] = blue;
                this.buffer[i + 3] = white;

                i += 4;
            }

            return this;
        }
    }
}
