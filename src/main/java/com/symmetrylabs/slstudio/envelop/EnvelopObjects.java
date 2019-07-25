package com.symmetrylabs.slstudio.envelop;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.ui.v2.GdxGraphicsAdapter;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import com.symmetrylabs.util.OctahedronWithArrow;
import com.symmetrylabs.util.TextMarker;
import heronarts.lx.LX;
import heronarts.lx.LXLayer;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.color.LXColor;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.symmetrylabs.util.DistanceConstants.FEET;
import static com.symmetrylabs.util.MathUtils.dist;

public class EnvelopObjects extends LXPattern implements MarkerSource {

    public final CompoundParameter size = new CompoundParameter("Base", 4*FEET, 0, 24*FEET);
    public final BoundedParameter response = new BoundedParameter("Level", 0, 1*FEET, 24*FEET);
    public final CompoundParameter spread = new CompoundParameter("Spread", 1, 1, .2);

    private final Envelop envelop;

    public EnvelopObjects(LX lx) {
        super(lx);

        envelop = Envelop.getInstance(lx);

        addParameter("size", this.size);
        addParameter("response", this.response);
        addParameter("spread", this.spread);

        for (Envelop.Source.Channel object : envelop.source.channels) {
            Layer layer = new Layer(lx, object);
            addLayer(layer);
            addParameter("source-" + object.index, layer.active);
        }
    }

    @Override public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        float size = 10;
        PVector pos = new PVector();
        int i = 0;
        int c = 0x80008040;

        for (Envelop.Source.Channel object : envelop.source.channels) {
            PVector wut = new PVector(object.tx, object.ty, object.tz);
            PVector wutPointer = new PVector(object.tx, object.ty, object.tz);
            markers.add(new OctahedronWithArrow(new PVector(object.tx, object.ty, object.tz), 10, LXColor.GREEN, wutPointer, LXColor.BLUE));
        }

//        markers.add(new TextMarker(pos, size, LXColor.WHITE, "yo world"));
        return markers;

    }

//    @Override
//    public void drawTextMarkers(GdxGraphicsAdapter g) {
//        final Quaternion rot = new Quaternion();
//        final Vector3 scale = new Vector3(3.8f, 1.2f, 1.2f);
//        float x = 0;
//        float y = 0;
//        float z = 0;
//        for (Envelop.Source.Channel object : envelop.source.channels) {
//            x = object.tx;
//            y = object.ty;
//            z = object.tz;
//            break;
//        }
//            g.textBatch.setTransformMatrix(new Matrix4(
//                new Vector3(
//                    x,
//                    y,
//                    z),
//                rot, scale));
//            g.font.draw(g.textBatch, "hello world", 0, 0, 100, 20, false);
//    }
//
//    @Override
//    public boolean drawLineMarkers(GdxGraphicsAdapter g) {
//        // mark ourselves as prefering the new direct-draw API
//        return true;
//    }


    class Layer extends LXLayer implements MarkerSource {

        private final Envelop.Source.Channel object;
        private final BooleanParameter active;

        private final MutableParameter tx = new MutableParameter();
        private final MutableParameter ty = new MutableParameter();
        private final MutableParameter tz = new MutableParameter();
        private final DampedParameter x = new DampedParameter(this.tx, 50*FEET);
        private final DampedParameter y = new DampedParameter(this.ty, 50*FEET);
        private final DampedParameter z = new DampedParameter(this.tz, 50*FEET);

        @Override public Collection<Marker> getMarkers() {
            List<Marker> markers = new ArrayList<>();
            float size = 10;
            PVector pos = new PVector();
            int i = 0;
            int c = 0x80008040;
            markers.add(new TextMarker(pos, size, LXColor.WHITE, "yo world"));
            return markers;
        }

        @Override
        public void drawTextMarkers(GdxGraphicsAdapter g) {
            final Quaternion rot = new Quaternion();
            final Vector3 scale = new Vector3(3.8f, 1.2f, 1.2f);
            g.textBatch.setTransformMatrix(new Matrix4(
                new Vector3(
                    0,
                    0,
                    0),
                rot, scale));
            g.font.draw(g.textBatch, "INSIDE LAYER", 0, 0, 100, 20, false);
        }

        @Override
        public boolean drawLineMarkers(GdxGraphicsAdapter g) {
            // mark ourselves as prefering the new direct-draw API
            return true;
        }

        Layer(LX lx, Envelop.Source.Channel object) {
            super(lx);

            this.object = object;

            active = new BooleanParameter("Source " + object.index, false);

            startModulator(this.x);
            startModulator(this.y);
            startModulator(this.z);
        }

        public void run(double deltaMs) {
            if (!this.active.isOn()) {
                return;
            }
            this.tx.setValue(object.tx);
            this.ty.setValue(object.ty);
            this.tz.setValue(object.tz);
            if (object.active) {
                float x = this.x.getValuef();
                float y = this.y.getValuef();
                float z = this.z.getValuef();
                float spreadf = spread.getValuef();
                float falloff = 100 / (size.getValuef() + response.getValuef() * object.getValuef());
                for (LXPoint p : model.getPoints()) {
                    float dist = dist(p.x * spreadf, p.y, p.z * spreadf, x * spreadf, y, z * spreadf);
                    float b = 100 - dist*falloff;
                    if (b > 0) {
                        addColor(p.index, LXColor.gray(b));
                    }
                }
            }
        }
    }

    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
    }
}
