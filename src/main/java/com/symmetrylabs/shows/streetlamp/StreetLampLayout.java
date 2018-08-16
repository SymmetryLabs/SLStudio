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

        return new SLModel(points);
    }

    public void setupLx(SLStudioLX lx) {
        LXDatagramOutput output = new LXDatagramOutput(lx);

        for (LXPoint point : lx.model.getPoints()) {
            output.addDatagram()
        }

        lx.addOutput(output);
    }
}
