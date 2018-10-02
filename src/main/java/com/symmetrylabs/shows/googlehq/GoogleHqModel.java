package com.symmetrylabs.shows.googlehq;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ReadableObj;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import processing.core.PGraphics;

public class GoogleHqModel extends SLModel implements MarkerSource {
    private static final String SOURCE_FILE = "shows/googlehq/hybycozo_curve.obj";

    private ReadableObj model;
    private Collection<Edge> edges;

    protected GoogleHqModel(List<LXPoint> points, ReadableObj model, Collection<Edge> edges) {
        super(points);
        this.model = model;
        this.edges = edges;
    }

    public static GoogleHqModel load() {
        ReadableObj model;
        try {
            InputStream in = new FileInputStream(SOURCE_FILE);
            model = ObjReader.read(in);
        } catch (IOException e) {
            System.err.println("could not read googlehq model file:");
            e.printStackTrace();
            return null;
        }

        ArrayList<LXPoint> points = new ArrayList<>(model.getNumVertices());
        for (int vi = 0; vi < model.getNumVertices(); vi++) {
            FloatTuple v = model.getVertex(vi);
            points.add(new LXPoint(v.getX(), v.getY(), -v.getZ()));
        }
        return new GoogleHqModel(points, model, null);
    }

    private static class Edge {
        int v1;
        int v2;
        LXVector p1;
        LXVector p2;
        LXVector dir;

        public Edge(int v1, int v2, ReadableObj obj) {
            this.v1 = Math.min(v1, v2);
            this.v2 = Math.max(v1, v2);
            FloatTuple ft1 = obj.getVertex(this.v1);
            FloatTuple ft2 = obj.getVertex(this.v2);
            p1 = new LXVector(ft1.getX(), ft1.getY(), ft1.getZ());
            p2 = new LXVector(ft2.getX(), ft2.getY(), ft2.getZ());
            dir = p1.copy().mult(-1).add(p2);
            dir.normalize();
        }

        public double subtendedAngle(Edge other) {
            /* absolute value because we don't care about which way the edges
                 face (it is 100% arbitrary) */
            return Math.acos(Math.abs(dir.dot(other.dir)));
        }

        public boolean sharesVertex(Edge other) {
            return v1 == other.v1 || v2 == other.v1 || v1 == other.v2 || v2 == other.v2;
        }

        @Override
        public int hashCode() {
            /* v1 and v2 are unlikely to have anything in their top 16 bits,
                 so it's probably safe to mix them just based on their bottom
                 word, but just to be safe, mix in v1's high bits as well. */
            return (((v1 & 0x0000FFFF) << 16) | ((v1 & 0xFFFF0000) >> 16)) ^ v2;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Edge)) {
                return false;
            }
            Edge o = (Edge) other;
            return o.v1 == v1 && o.v2 == v2;
        }

        @Override
        public String toString() {
            return String.format("%d/%d", v1, v2);
        }
    }

    public class ObjEdgeMarker implements Marker {
        @Override
        public void draw(PGraphics pg) {
            pg.strokeWeight(1);
            pg.stroke(0xFF, 0xFF, 0x00);
            pg.noFill();
            for (Edge e : edges) {
                FloatTuple v1 = model.getVertex(e.v1);
                FloatTuple v2 = model.getVertex(e.v2);
                pg.line(v1.getX(), v1.getY(), v1.getZ(), v2.getX(), v2.getY(), v2.getZ());
            }
        }
    }

    @Override
    public Collection<Marker> getMarkers() {
        ArrayList<Marker> markers = new ArrayList<>();
        if (edges != null) {
            markers.add(new ObjEdgeMarker());
        }
        return markers;
    }
}
