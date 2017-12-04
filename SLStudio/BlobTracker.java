package com.symmetrylabs.util;

import heronarts.lx.LX;
import heronarts.lx.LXModulatorComponent;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlobTracker extends LXModulatorComponent implements LXOscListener, MarkerSource {
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
        if (!message.getAddressPattern().toString().equals("/blobs")) return;

        int arg = 0;
        String sourceId = message.getString(arg++);
        long millis = message.getInt(arg++);
        float deltaSec = (float) (millis - lastMessageMillis) * 0.001f;

        List<Blob> newBlobs = new ArrayList<Blob>();
        int count = message.getInt(arg++);
        for (int i = 0; i < count; i++) {
            String id = message.getString(arg++);
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
            for (Blob b : allBlobs) {
                b.vel = estimateNewBlobVelocity(b, lastKnownBlobs, deltaSec, maxSpeed);
            }
        }

        lastMessageMillis = millis;
        lastKnownBlobs = allBlobs;
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
    private PVector estimateNewBlobVelocity(Blob newBlob, List<Blob> prevBlobs, float deltaSec, float maxSpeed) {
        Blob closestBlob = findClosestBlob(newBlob.pos, prevBlobs);
        final float ACCEL_FACTOR = 0.2f;
        if (closestBlob != null) {
            PVector vel = PVector.div(PVector.sub(newBlob.pos, closestBlob.pos), deltaSec);
            if (vel.mag() < maxSpeed) {
                return PVector.add(PVector.mult(vel, ACCEL_FACTOR), PVector.mult(closestBlob.vel, 1 - ACCEL_FACTOR));
            }
        }
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

    public Blob findClosestBlob(PVector p, List<Blob> blobs) {
        Blob closest = null;
        float minDist = 0;
        for (Blob b : blobs) {
            float dist = PVector.sub(p, b.pos).mag();
            if (closest == null || dist < minDist) {
                closest = b;
                minDist = dist;
            }
        }
        return closest;
    }

    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        for (Blob blob : lastKnownBlobs) {
            markers.add(new OctahedronWithArrow(blob.pos, 12, 0xff0080, blob.vel, 0x8000ff));
        }
        return markers;
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