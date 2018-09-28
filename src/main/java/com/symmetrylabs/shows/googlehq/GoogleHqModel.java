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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import processing.core.PGraphics;

public class GoogleHqModel extends SLModel implements MarkerSource {
    private static final String SOURCE_FILE = "shows/googlehq/hybycozo_led.obj";

    private ReadableObj model;
    private HashSet<Edge> edges;

    protected GoogleHqModel(List<LXPoint> points, ReadableObj model, HashSet<Edge> edges) {
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

        HashSet<Edge> edges = new HashSet<>();
        int NF = model.getNumFaces();
        for (int fi = 0; fi < NF; fi++) {
            ObjFace f = model.getFace(fi);
            int NV = f.getNumVertices();
            for (int vi = 0; vi < NV; vi++) {
                Edge e = new Edge(f.getVertexIndex(vi), f.getVertexIndex((vi + 1) % NV));
                edges.add(e);
            }
        }
        int NV = model.getNumVertices();
        ArrayList<LXPoint> points = new ArrayList<>(NV);
        for (int vi = 0; vi < NV; vi++) {
            FloatTuple v = model.getVertex(vi);
            points.add(new LXPoint(v.getX(), v.getY(), v.getZ()));
        }
        System.out.println(String.format("loaded model with %d edges", edges.size()));
        return new GoogleHqModel(points, model, edges);
    }

    private static class Edge {
        int v1;
        int v2;

        public Edge(int v1, int v2) {
            this.v1 = Math.min(v1, v2);
            this.v2 = Math.max(v1, v2);
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
        markers.add(new ObjEdgeMarker());
        return markers;
    }
}
