package com.symmetrylabs.slstudio.envelop;

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

import static com.symmetrylabs.util.DistanceConstants.FEET;
import static com.symmetrylabs.util.MathUtils.dist;

public class EnvelopObjects extends LXPattern {

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

    class Layer extends LXLayer {

        private final Envelop.Source.Channel object;
        private final BooleanParameter active;

        private final MutableParameter tx = new MutableParameter();
        private final MutableParameter ty = new MutableParameter();
        private final MutableParameter tz = new MutableParameter();
        private final DampedParameter x = new DampedParameter(this.tx, 50*FEET);
        private final DampedParameter y = new DampedParameter(this.ty, 50*FEET);
        private final DampedParameter z = new DampedParameter(this.tz, 50*FEET);

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
