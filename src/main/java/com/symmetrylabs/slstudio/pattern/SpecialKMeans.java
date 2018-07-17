package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LXComponent;
import heronarts.lx.LXPattern;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;
import heronarts.lx.transform.LXVector;

public class SpecialKMeans extends LXPattern {
    int[] hues;
    KMeans kmeans;
    int[] current;
    int[] next;

    public SpecialKMeans(LX lx) {
        super(lx);

        int n = 30;
        hues = new int[n];
        kmeans = new KMeans(n);

        float lo = 250;
        float hi = 360;

        for (int i = 0; i < n; i++) {
            float p = (float)i / (float)n;
            hues[i] = LXColor.hsb(MathUtils.lerp(lo, hi, p), 100, 100);
        }

        current = new int[model.points.length];
        next = new int[model.points.length];

        for (int i = 0; i < model.points.length; i++) {
            current[i] = LXColor.BLACK;
            next[i] = hues[kmeans.assignments[i]];
        }
    }


    private class KMeans {
        int nCentroids;
        int nPoints;
        public LXVector[] centroids;
        public LXVector[] points;

        public int[] assignments;

        KMeans(int nCentroids) {
            nPoints = model.points.length;
            this.nCentroids = nCentroids;

            centroids = new LXVector[nCentroids];
            assignments = new int[nPoints];
            points = new LXVector[nPoints];


            for (int i = 0; i < nPoints; i++) {
                assignments[i] = -1;
                points[i] = new LXVector(model.points[i]);
            }
            pickRandomCentroids();
            step();
        }

        void pickRandomCentroids() {
            for (int i = 0; i < nCentroids; i++) {
                int j;
                do {
                    j = (int)MathUtils.random(nPoints);
                } while (assignments[j] != -1);
                assignments[j] = i;
                centroids[i] = points[j];
            }
        }

        void assignmentStep() {
            for (int i = 0; i < nPoints; i++) {
                float minDist = Float.POSITIVE_INFINITY;
                int minIndex = -1;
                for (int j = 0; j < nCentroids; j++) {
                    float dist = centroids[j].dist(points[i]);
                    if (dist < minDist) {
                        minDist = dist;
                        minIndex = j;
                    }
                }
                assignments[i] = minIndex;
            }
        }

        void moveCentroidsStep() {
            LXVector[] newCentroids = new LXVector[nCentroids];
            for (int i = 0; i < nCentroids; i++) {
                newCentroids[i] = new LXVector(0, 0, 0);
            }
            int counts[] = new int[nCentroids];

            for (int i = 0; i < nPoints; i++) {
                int a = assignments[i];
                newCentroids[a].add(points[i]);
                counts[a]++;
            }

            for (int i = 0; i < nCentroids; i++) {
                if (counts[i] > 0) {
                    newCentroids[i].div(counts[i]);
                }
                newCentroids[i].add(MathUtils.random(0, 3), MathUtils.random(0, 3), MathUtils.random(0, 3));
            }
            centroids = newCentroids;
        }

        public void step() {
            assignmentStep();
            moveCentroidsStep();
        }


    }

    float sinceLast = 0;

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);


        sinceLast += deltaMs;
        if (sinceLast > 100) {
            kmeans.step();
            sinceLast = 0;
            current = next;
            for (int i = 0; i < model.points.length; i++) {
                next[i] = hues[kmeans.assignments[i]];
            }
        }

        float a = (float)deltaMs / (float)100;
        for (int i = 0; i < model.points.length; i++) {
            LXPoint p = model.points[i];
            colors[p.index] = LXColor.lerp(current[i], next[i], a);
        }
        polyBuffer.markModified(SRGB8);
    }
}
