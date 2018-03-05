package com.symmetrylabs.slstudio.util;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.nissan.NissanModel;
import com.symmetrylabs.slstudio.model.nissan.NissanWindow;
import heronarts.lx.LX;
import heronarts.lx.LXModulatorComponent;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlobTracker extends LXModulatorComponent implements LXOscListener, com.symmetrylabs.slstudio.util.MarkerSource {
    private static final int OSC_PORT = 4343;

    private float mergeRadius = 30f;  // inches
    private float maxSpeed = 360f;  // inches per second
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

    public void processThermal(OscMessage message){

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

    public void processTouch(OscMessage message){
        // is this for me?
        if (!message.getAddressPattern().toString().contains("blob")) return;
        if (!(message.getAddressPattern().toString().contains(":u") || message.getAddressPattern().toString().contains(":v") ) ) return;

        int arg = 0;

        String windowId = message.getString(arg++);

        int count = message.getInt(arg++);
//        long millis = message.getInt(arg++);
        long millis = System.currentTimeMillis() % 1000;
        float deltaSec = (float) (millis - lastMessageMillis) * 0.001f;

        NissanWindow window = ((NissanModel) lx.model).tryGetWindowById(windowId);
        if (window == null || window.uvTransform == null) return;

        LXTransform transform = new LXTransform(new LXMatrix(window.uvTransform.getMatrix()));

        List<Blob> newBlobs = new ArrayList<>();
        for (int i = 0; i < count/4; i++) { // each data elt has 4
            float x = message.getFloat(arg++);
            float y = message.getFloat(arg++);
            float size0 = message.getFloat(arg++);
            float size1 = message.getFloat(arg++);
            float size = size0*size1;
            transform.push();
            transform.translate(x, y);
            newBlobs.add(new Blob(new PVector(transform.x(), transform.y(), transform.z()), size));
            transform.pop();
        }
        blobsBySource.put(windowId, newBlobs);

        List<Blob> allBlobs = new ArrayList<>();
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

    @Override
    public void oscMessage(OscMessage message) {
        // call handlers
        this.processTouch(message);
        this.processThermal(message);
    }

    /**
     * Modifies a list of blobs in place, merging blobs within mergeRadius.
     */
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

    /**
     * Returns an estimate of the velocity of a blob, given a list of previous blobs.
     */
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

    /**
     * Returns a copy of the current list of blobs.
     */
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

    public Collection<com.symmetrylabs.slstudio.util.Marker> getMarkers() {
        List<com.symmetrylabs.slstudio.util.Marker> markers = new ArrayList<com.symmetrylabs.slstudio.util.Marker>();
        for (Blob blob : lastKnownBlobs) {
            markers.add(new com.symmetrylabs.slstudio.util.OctahedronWithArrow(blob.pos, 12, 0xff0080, blob.vel, 0x8000ff));
        }
        return markers;
    }

    public class Blob {
        public PVector pos;
        public PVector vel;
        public float size;

        String ID;

        private Blob(PVector pos, PVector vel, float size) {
            this.pos = pos;
            this.vel = vel;
            this.size = size;
            this.ID = "NOTINITIALIZED";
        }

        private Blob(PVector pos, PVector vel, float size, String ID) {
            this.pos = pos;
            this.vel = vel;
            this.size = size;
            this.ID = ID;
        }

        private Blob(PVector pos, float size, String ID) {
            this(pos, new PVector(0, 0, 0), size);
            this.ID = ID;
        }

        private Blob(PVector pos, float size) {
            this(pos, new PVector(0, 0, 0), size);
            this.ID = "NOTINITIALIZED";
        }

        public String toString() {
            return String.format("pos %s vel %s size %.0f", pos.toString(), vel.toString(), size);
        }
    }
}