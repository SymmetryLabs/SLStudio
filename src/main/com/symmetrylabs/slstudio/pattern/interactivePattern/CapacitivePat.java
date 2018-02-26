package com.symmetrylabs.slstudio.pattern.interactivePattern;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.nissan.NissanCar;
import com.symmetrylabs.slstudio.model.nissan.NissanWindow;
import com.symmetrylabs.slstudio.model.nissan.PanelPoint;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import com.symmetrylabs.slstudio.pattern.ping.SLPatternWithMarkers;
import com.symmetrylabs.slstudio.util.*;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

import java.util.*;

public class CapacitivePat extends SLPatternWithMarkers {
    CompoundParameter timeScale = new CompoundParameter("timeScale", 1, 0, 1);  // time scaling factor
    BooleanParameter oscFollowers = new BooleanParameter("atBlobs");
    BooleanParameter oscBlobs = new BooleanParameter("nearBlobs");
    BooleanParameter everywhere = new BooleanParameter("everywhere");
    BooleanParameter perSun = new BooleanParameter("perSun");
    CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);  // focus coordinates (in)
    CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
    CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
    CompoundParameter zScale = new CompoundParameter("zScale", 0, -6, 12);  // z scaling factor (dB)
    DiscreteParameter maxBirds = new DiscreteParameter("maxBirds", 8, 0, 100);

    CompoundParameter spnRad = new CompoundParameter("spnRad", 100, 0, 400);  // radius (in) within which to spawn birds
    CompoundParameter spnRate = new CompoundParameter("spnRate", 0.2, 0, 2);  // maximum spawn rate (birds/s)
    CompoundParameter spnVary = new CompoundParameter("spnVary", 0, 0, 1);
    // vary spawn rate according to focus speed (0 = don't vary, 1 = determine entirely by speed)
    CompoundParameter scatter = new CompoundParameter("scatter", 100, 0, 1000);  // initial velocity randomness (in/s)
    CompoundParameter spdMult = new CompoundParameter("spdMult", 1, 0, 2);  // (ratio) bird target speed / focus speed
    CompoundParameter maxSpd = new CompoundParameter("maxSpd", 10, 0, 100);  // max bird speed (in/s)
    CompoundParameter turnSec = new CompoundParameter("turnSec", 1, 0, 2);  // time (s) to complete 90% of a turn
    CompoundParameter fadeInSec = new CompoundParameter("fadeInSec", 0.5, 0, 2);  // time (s) to fade up to 100% intensity
    CompoundParameter fadeOutSec = new CompoundParameter("fadeOutSec", 1, 0, 2);// time (s) to fade down to 10% intensity

    CompoundParameter size = new CompoundParameter("size", 100, 0, 2000);  // render radius of each bird (in)
    CompoundParameter detail = new CompoundParameter("detail", 4, 0, 10);  // ripple spatial frequency (number of waves)
    CompoundParameter ripple = new CompoundParameter("ripple", 0, -10, 10);  // ripple movement (waves/s)
    DiscreteParameter palette = new DiscreteParameter("palette", ((SLStudioLX) lx).paletteLibrary.getNames());
    // selected colour palette
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, 0, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward start or stop
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

    PVector prevFocus = null;
    float numToSpawn = 0f;

    private BlobTracker blobTracker;
    private BlobFollower blobFollower;
    private ZigzagPalette pal = new ZigzagPalette();
    private long lastRun;

    public CapacitivePat(LX lx) {
        super(lx);

        blobTracker = BlobTracker.getInstance(lx);
        blobFollower = new BlobFollower(blobTracker);

        addParameter(oscFollowers);
        addParameter(oscBlobs);
        addParameter(everywhere);
        addParameter(perSun);

        addParameter(timeScale);
        addParameter(size);
        addParameter(detail);
        addParameter(ripple);

        addParameter(x);
        addParameter(y);
        addParameter(z);
        addParameter(zScale);

        addParameter(palette);
        addParameter(palShift);
        addParameter(palBias);
        addParameter(palCutoff);

        addParameter(palStart);
        addParameter(palStop);
        addParameter(spnRad);
        addParameter(maxBirds);

        addParameter(spnRate);
        addParameter(spnVary);
        addParameter(scatter);

        addParameter(spdMult);
        addParameter(maxSpd);
        addParameter(turnSec);
        addParameter(fadeInSec);
        addParameter(fadeOutSec);

    }



    private int scale(float x, int min, int range) {
        int scaled_value = min + (int)(x*range);
        return scaled_value;
    }

    public void run(double deltaMs) {
        advanceSimulation((float) deltaMs * 0.001f * timeScale.getValuef());
        blobFollower.advance((float) deltaMs * 0.001f);
//        render();
        lastRun = new Date().getTime();
    }

    void advanceSimulation(float deltaSec) {
        if (oscBlobs.isOn()) {

            List<BlobTracker.Blob> blobs = blobTracker.getBlobs();
            for (BlobTracker.Blob b : blobs) {
                setColors(0);
                for (NissanCar car : this.model.getCars()) {
                    for (NissanWindow window : car.getWindows()) {


                        String window_id = window.getId();
                        int stripIndex = SLStudio.applet.selectedStrip.getValuei();
                        List<Strip> strips = car.getStrips();
                        Strip strip = strips.get(stripIndex);

                        for (Strip stripx : strips) {
                            for (LXPoint p : stripx.points) {
                                if (((PanelPoint) p).getPanel_x() == scale(b.pos.x, window.min_x, window.range_x)) {
                                    colors[p.index] = LXColor.RED;
                                }
                                if (((PanelPoint) p).getPanel_y() == scale(b.pos.y, window.min_y, window.range_y)) {
                                    colors[p.index] = LXColor.GREEN;
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        if (lastRun + 1000 < new Date().getTime()) return markers; // hack to hide markers if inactive
        if (oscFollowers.isOn()) {
            markers.addAll(blobFollower.getMarkers());
        } else {
            if (oscBlobs.isOn()) {
                for (BlobTracker.Blob b : blobTracker.getBlobs()) {
                    markers.add(new CubeMarker(b.pos, spnRad.getValuef(), 0x00ff00));
                }
            } else if (everywhere.isOn()) {
                markers.add(new CubeMarker(
                    new PVector(model.cx, model.cy, model.cz),
                    new PVector(model.xRange / 2, model.yRange / 2, model.zRange / 2),
                    0x00ff00
                ));
            } else {
                markers.add(new CubeMarker(
                    new PVector(x.getValuef(), y.getValuef(), z.getValuef()),
                    spnRad.getValuef(),
                    0x00ff00
                ));
            }
        }
        return markers;
    }
}

