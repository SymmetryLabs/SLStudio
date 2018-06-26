package com.symmetrylabs.slstudio.ping;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.BlobTracker;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import java.util.List;

import static processing.core.PApplet.println;


public class BlobViewer extends SLPattern<SLModel> {
    DiscreteParameter mode = new DiscreteParameter("mode", new String[]{"planes", "spheres"});
    CompoundParameter tolerance = new CompoundParameter("tolerance", 2, 0, 8); // in
    CompoundParameter radius = new CompoundParameter("radius", 12, 0, 240); // in
    CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
    CompoundParameter oscMergeRadius = new CompoundParameter("bMrgRad", 30, 0, 100);  // blob merge radius (in)
    CompoundParameter oscMaxSpeed = new CompoundParameter("bMaxSpd", 240, 0, 1000);  // max blob speed (in/s)
    CompoundParameter oscMaxDeltaSec = new CompoundParameter("bMaxDt", 0.5, 0, 1);
        // max interval to calculate blob velocities (s)

    private BlobTracker blobTracker;

    public BlobViewer(LX lx) {
        super(lx);
        addParameter(mode);
        addParameter(tolerance);
        addParameter(radius);
        addParameter(y);
        addParameter(oscMergeRadius);
        addParameter(oscMaxSpeed);
        blobTracker = BlobTracker.getInstance(lx);
    }

    void updateBlobTrackerParameters() {
        blobTracker.setBlobY(y.getValuef());
        blobTracker.setMergeRadius(oscMergeRadius.getValuef());
        blobTracker.setMaxSpeed(oscMaxSpeed.getValuef());
        blobTracker.setMaxDeltaSec(oscMaxDeltaSec.getValuef());
    }

    public void run(double deltaMs) {
        updateBlobTrackerParameters();
        List<BlobTracker.Blob> blobs = blobTracker.getBlobs();
        int[] highlightColors = {0xffff0000, 0xff00ff00, 0xff0000ff};
        float tol = tolerance.getValuef();
        float rad = radius.getValuef();
        boolean sphereMode = mode.getOption().equals("spheres");

        println("blobs: " + blobs.size());
        for (LXVector p : getVectorList()) {
            PVector pv = new PVector(p.x, p.y, p.z);
            int c = 0;
            for (int b = 0; b < blobs.size(); b++) {
                PVector pos = blobs.get(b).pos;
                boolean hit = sphereMode ?
                    (PVector.sub(pv, pos).mag() < rad) :
                    (Math.abs(p.x - pos.x) < tol || Math.abs(p.z - pos.z) < tol);
                if (hit) {
                    c = c | highlightColors[b % highlightColors.length];
                }
            }
            colors[p.index] = c;
        }
    }
}
