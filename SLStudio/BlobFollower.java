package com.symmetrylabs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import processing.core.PVector;
import net.jafama.FastMath;

public class BlobFollower implements MarkerSource {
    BlobTracker tracker;
    List<Follower> followers;
    public float fadeInSec = 2;
    public float fadeOutSec = 2;
    public float fadedLevel = 1/250f;
    public float approachSec = 0.1f;
    public float maxSpeed = 360; // in/s
    public float maxAccel = 1800; // in/s^2
    public float maxRange = 48; // in

    public BlobFollower(BlobTracker tracker) {
        this.tracker = tracker;
        this.followers = new ArrayList<Follower>();
    }

    public void advance(float deltaSec) {
        List<BlobTracker.Blob> blobs = tracker.getBlobs();

        for (BlobTracker.Blob b : blobs) {
            if (findClosestFollower(b.pos, maxRange) == null) {
                followers.add(new Follower(b.pos));
            }
        }
        List<Follower> expired = new ArrayList<Follower>();
        for (Follower f : followers) {
            f.advance(deltaSec, findClosestBlob(f.pos, blobs, maxRange));
            if (f.expired) expired.add(f);
        }
        synchronized (followers) {
            followers.removeAll(expired);
        }
    }

    public List<Follower> getFollowers() {
        System.out.println("followers: " + followers.size());
        return followers;
    }

    public class Follower {
        public PVector pos;
        public PVector vel;
        public float value;
        public float ageSec;
        boolean expired;

        Follower(PVector pos) {
            this.pos = pos;
            this.vel = new PVector(0, 0, 0);
            this.value = 0;
            this.ageSec = 0;
        }

        void advance(float deltaSec, BlobTracker.Blob blob) {
            ageSec += deltaSec;
            if (blob == null) {
                if (value < fadedLevel) {
                    expired = true;
                } else {
                    value *= (float) FastMath.pow(fadedLevel, deltaSec / fadeOutSec);
                }
            } else {
                value += deltaSec / fadeInSec;
                if (value > 1) value = 1;

                PVector targetVel = PVector.div(PVector.sub(blob.pos, pos), approachSec);
                PVector targetAcc = PVector.div(PVector.sub(targetVel, vel), approachSec);
                float accel = targetAcc.mag();
                if (accel > maxAccel) {
                    targetAcc.div(accel/maxAccel);
                }
                vel.add(PVector.mult(targetAcc, deltaSec));
                float speed = vel.mag();
                if (speed > maxSpeed) {
                    vel.div(speed/maxSpeed);
                }
                pos.add(PVector.mult(vel, deltaSec));
            }
        }
    }

    public BlobTracker.Blob findClosestBlob(PVector p, List<BlobTracker.Blob> blobs, float range) {
        BlobTracker.Blob closest = null;
        float minDist = 0;
        for (BlobTracker.Blob b : blobs) {
            float dist = PVector.sub(p, b.pos).mag();
            if (dist < range && (closest == null || dist < minDist)) {
                closest = b;
                minDist = dist;
            }
        }
        return closest;
    }

    public Follower findClosestFollower(PVector p, float range) {
        Follower closest = null;
        float minDist = 0;
        for (Follower f : followers) {
            float dist = PVector.sub(p, f.pos).mag();
            if (dist < range && (closest == null || dist < minDist)) {
                closest = f;
                minDist = dist;
            }
        }
        return closest;
    }

    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        synchronized (followers) {
            for (Follower f : followers) {
                markers.add(new OctahedronWithArrow(f.pos, f.value*24, 0x0000ff, f.vel, 0xffff00));
                //markers.add(new Octahedron(f.pos, 6, 0x00ff00));
            }
        }
        return markers;
    }
}
