package com.symmetrylabs.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.LXModulatorComponent;
import heronarts.lx.model.LXPoint;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;

public class BlobTracker extends LXModulatorComponent implements LXOscListener {
    private static final int OSC_PORT = 4343;

    private float mergeRadius = 30f;  // inches
    private float maxSpeed = 240f;  // inches per second
    private float maxDeltaSec = 0.5f;  // don't track movement across large gaps in time
    private float blobY = 40f;  // inches off the ground
    private long lastMessageMillis = 0;

    private LX lx;
    private Map<String, List<Blob>> blobsBySource = new HashMap<String, List<Blob>>();
    private List<Blob> lastKnownBlobs = new ArrayList<Blob>();

    private static Map<LX, BlobTracker> instanceByLX = new HashMap<LX, BlobTracker>();
    public static synchronized BlobTracker getInstance(LX lx) {
        if (!instanceByLX.containsKey(lx)) {
            instanceByLX.put(lx, new BlobTracker(lx));
        }
        return instanceByLX.get(lx);
    }

    private BlobTracker(LX lx) {
        super(lx, "BlobTracker");
        this.lx = lx;
        try {
            lx.engine.osc.receiver(OSC_PORT).addListener(this);
        } catch (java.net.SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMergeRadius(float radius) {
        mergeRadius = radius;
    }

    public void setMaxSpeed(float speed) {
        maxSpeed = speed;
    }

    public void setMaxDeltaSec(float deltaSec) {
        maxDeltaSec = deltaSec;
    }

    public void setBlobY(float y) {
        blobY = y;
    }

    @Override
    public void oscMessage(OscMessage message) {
        int arg = 0;
        String sourceId = message.getString(arg++);
        long millis = message.getInt(arg++);
        float deltaSec = (float) (millis - lastMessageMillis) * 0.001f;

        List<Blob> newBlobs = new ArrayList<Blob>();
        int count = message.getInt(arg++);
        for (int i = 0; i < count; i++) {
            float x = message.getFloat(arg++);
            float y = message.getFloat(arg++);
            float size = message.getFloat(arg++);
            newBlobs.add(new Blob(new PVector(x, blobY, y), size));
        }
        blobsBySource.put(sourceId, newBlobs);
        
        List<Blob> allBlobs = new ArrayList<Blob>();
        for (String id : blobsBySource.keySet()) {
            allBlobs.addAll(blobsBySource.get(id));
        }
        mergeBlobs(allBlobs, mergeRadius);

        if (deltaSec < maxDeltaSec) {
            FixedWidthOctree<Blob> blobIndex = createBlobIndex(lastKnownBlobs);
            for (Blob b : allBlobs) {
                b.vel = estimateNewBlobVelocity(b, blobIndex, deltaSec, maxSpeed);
            }
        }

        lastMessageMillis = millis;
        lastKnownBlobs = allBlobs;

        String status = "[" + millis + " ms] " + allBlobs.size() + " blob" + (allBlobs.size() == 1 ? "" : "s") + ": ";
        for (Blob b : allBlobs) {
                status += b + ", ";
        }
        System.out.println(status.substring(0, status.length() - 2));
    }

    private FixedWidthOctree createBlobIndex(List<Blob> newBlobs) {
        try {
            FixedWidthOctree<Blob> index = new FixedWidthOctree<Blob>(lx.model.cx, lx.model.cy, lx.model.cz,
                    (float) Math.max(lx.model.xRange, Math.max(lx.model.yRange, lx.model.zRange)), 3);
            for (Blob blob : newBlobs) {
                index.insert(blob.pos.x, blob.pos.y, blob.pos.z, blob);
            }
            return index;
        } catch (Exception e) {
            System.err.println("Exception while building blob index: " + e.getMessage());
        }

        return null;
    }

    /** Modifies a list of blobs in place, merging blobs within mergeRadius. */
    private void mergeBlobs(List<Blob> blobs, float mergeRadius) {
        boolean mergeFound;
        do {
            mergeFound = false;
            search_for_merges:
            for (Blob b : blobs) {
                for (Blob other : blobs) {
                    if (b != other && PVector.sub(b.pos, other.pos).mag() < mergeRadius) {
                        blobs.remove(b);
                        blobs.remove(other);
                        blobs.add(new Blob(PVector.div(PVector.add(b.pos, other.pos), 2), b.size + other.size));
                        mergeFound = true;
                        break search_for_merges;
                    }
                }
            }
        } while (mergeFound);
    }

    /** Returns an estimate of the velocity of a blob, given a list of previous blobs. */
    private PVector estimateNewBlobVelocity(Blob newBlob, FixedWidthOctree<Blob> blobIndex, float deltaSec, float maxSpeed) {
        Blob closestBlob = findClosestBlob(new LXPoint(newBlob.pos.x, newBlob.pos.y, newBlob.pos.z), blobIndex);
        if (closestBlob == null)
            return new PVector(0, 0, 0);

        PVector vel = PVector.div(PVector.sub(newBlob.pos, closestBlob.pos), deltaSec);
        if (vel.mag() < maxSpeed)
            return vel;

        return new PVector(0, 0, 0);
    }

    /** Returns a copy of the current list of blobs. */
    public List<Blob> getBlobs() {
        List<Blob> result = new ArrayList<Blob>();
        for (Blob b : lastKnownBlobs) {
            result.add(new Blob(b.pos, b.vel, b.size));
        }
        return result;
    }

    public Blob findClosestBlob(LXPoint p, FixedWidthOctree<Blob> blobIndex) {
        try {
            return blobIndex.nearest(p.x, (float) p.y, (float) p.z);
        } catch (Exception e) {
            System.err.println("Exception while finding nearest blob: " + e.getMessage());
        }
        return null;
    }

    public class Blob {
        public PVector pos;
        public PVector vel;
        public float size;

        private Blob(PVector pos, PVector vel, float size) {
            this.pos = pos;
            this.vel = vel;
            this.size = size;
        }

        private Blob(PVector pos, float size) {
            this(pos, new PVector(0, 0, 0), size);
        }

        public String toString() {
            return String.format("pos %s vel %s size %.0f", pos.toString(), vel.toString(), size);
        }
    }
}