package com.symmetrylabs.shows.streetlamp;

import java.util.List;
import java.util.ArrayList;
import java.net.SocketException;
import java.net.UnknownHostException;

import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.StreamingACNDatagram2;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;

public class StreetLampLayout implements Show {

    public SLModel buildModel() {
        List<LXPoint> points = new ArrayList<>();

        points.add(new LXPoint(0, 0, 100));
        points.add(new LXPoint(-100, 0, 0));
        points.add(new LXPoint(100, 0, 0));

        return new SLModel(points);
    }

    public void setupLx(SLStudioLX lx) {
        try {
            LXDatagramOutput output = new LXDatagramOutput(lx);

            output.addDatagram(new RGBWACNDatagram(lx.model).setAddress("10.200.1.16"));

            lx.addOutput(output);
        }
        catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    static class RGBWACNDatagram extends StreamingACNDatagram2 {
        public RGBWACNDatagram(LXFixture fixture) {
            super(1, fixture.getPoints().size() * 4);
        }

        protected RGBWACNDatagram copyPoints(int[] colors, int[] pointIndices, int offset) {
            int i = offset;

            for (int color : colors) {
                if (i >= buffer.length)
                    break;

                float saturation = LXColor.s(color) / 100;
                float brightness = LXColor.b(color) / 100;

                byte red = (byte)(((color >> 16) & 0xff) * saturation);
                byte green = (byte)(((color >> 8) & 0xff) * saturation);
                byte blue = (byte)((color & 0xff) * saturation);
                byte white = (byte)((1 - saturation) * brightness * 255);

                buffer[i] = green;
                buffer[i + 1] = blue;
                buffer[i + 2] = white;
                buffer[i + 3] = red;

                i += 4;
            }

            return this;
        }
    }
}
