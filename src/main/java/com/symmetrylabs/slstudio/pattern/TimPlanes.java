package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
//import com.symmetrylabs.slstudio.pattern.base.SLPattern;
//import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
//import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import processing.core.PImage;

import static com.symmetrylabs.util.MathConstants.PI;
//import static com.symmetrylabs.util.MathUtils.abs;
//import static com.symmetrylabs.util.MathUtils.cos;
//import static com.symmetrylabs.util.MathUtils.sin;
import static java.lang.Math.pow;

import static processing.core.PConstants.ADD;

//import static sun.tools.java.Constants.ADD;

public class TimPlanes extends LXPattern {//SLPattern<StripsModel<Strip>>  {
    private CompoundParameter wobbleParameter = new CompoundParameter("Wob", 0.166);
    private CompoundParameter wobbleSpreadParameter = new CompoundParameter("WSpr", 0.25);
    private CompoundParameter wobbleSpeedParameter = new CompoundParameter("WSpd", 0.375);
    private CompoundParameter wobbleOffsetParameter = new CompoundParameter("WOff", 0);
    private CompoundParameter derezParameter = new CompoundParameter("Drez", 0.5, 0, .98);
    private CompoundParameter thicknessParameter = new CompoundParameter("Thick", 0.4);
    private CompoundParameter ySpreadParameter = new CompoundParameter("ySpr", 0.2);
    private CompoundParameter hueParameter = new CompoundParameter("Hue", 0.75);
    private CompoundParameter hueSpreadParameter = new CompoundParameter("HSpr", 0.68);

    private BooleanParameter splitHue = new BooleanParameter("splitHue");

    final float centerX, centerY, centerZ;
    float phase;

    class Plane {
        LXVector center;
        Rotation rotation;
        float hue;

        Plane(LXVector center, Rotation rotation, float hue) {
            this.center = center;
            this.rotation = rotation;
            this.hue = hue;
        }
    }

    public TimPlanes(LX lx) {
        super(lx);
        centerX = (model.xMin + model.xMax) / 2;
        centerY = (model.yMin + model.yMax) / 2;
        centerZ = (model.zMin + model.zMax) / 2;
        phase = 0;
        addParameter(splitHue);
        addParameter(wobbleParameter);
        addParameter(wobbleSpreadParameter);
        addParameter(wobbleSpeedParameter);
//    addParameter(wobbleOffsetParameter);
        addParameter(derezParameter);
        addParameter(thicknessParameter);
        addParameter(ySpreadParameter);
        addParameter(hueParameter);
        addParameter(hueSpreadParameter);
    }

    int beat = 0;
    float prevRamp = 0;
    float[] wobbleSpeeds = { 1.0f/8, 1.0f/4, 1.0f/2, 1.0f };

    public void run(double deltaMs) {

        float ramp = (float)lx.tempo.ramp();
        if (ramp < prevRamp) {
            beat = (beat + 1) % 32;
        }
        prevRamp = ramp;

        float wobbleSpeed = wobbleSpeeds[MathUtils.floor(wobbleSpeedParameter.getValuef() * wobbleSpeeds.length * 0.9999f)];

        phase = (((beat + ramp) * wobbleSpeed + wobbleOffsetParameter.getValuef()) % 1) * 2 * PI;

        float ySpread = ySpreadParameter.getValuef() * 50;
        float wobble = wobbleParameter.getValuef() * PI;
        float wobbleSpread = wobbleSpreadParameter.getValuef() * PI;
        float hue = hueParameter.getValuef() * 360;
        float hueSpread0 = (hueSpreadParameter.getValuef() - 0.5f) * 360;
        float hueSpread1 = hueSpread0;
        if (splitHue.getValueb()){
            hueSpread1 = - hueSpread0;
        }

        float saturation = 10f + 60.0f * ((float) pow(ramp, 0.25f));

        float derez = derezParameter.getValuef();

        Plane[] planes = {
            new Plane(
                new LXVector(centerX, centerY + ySpread, centerZ),
                new Rotation(((float) wobble) - ((float) wobbleSpread), ((float) phase), 0f),
                hue + 360f - hueSpread0),
            new Plane(
                new LXVector(centerX, centerY, centerZ),
                new Rotation(wobble, phase, 0),
                hue),
            new Plane(
                new LXVector(centerX, centerY - ySpread, centerZ),
                new Rotation(wobble + wobbleSpread, phase, 0),
                hue + 360 + hueSpread1)
        };

        float thickness = (thicknessParameter.getValuef() * 25 + 1);

        LXVector normalizedPoint = new LXVector(0, 0, 0);

        for (LXVector p : getVectors()) {
            if (MathUtils.random(1.0f) < derez) {
                continue;
            }

            int c = 0;

            for (Plane plane : planes) {
                normalizedPoint.x = p.x - plane.center.x;
                normalizedPoint.y = p.y - plane.center.y;
                normalizedPoint.z = p.z - plane.center.z;

                float v = plane.rotation.rotatedY(normalizedPoint);
                float d = MathUtils.abs(v);

                final int planeColor;
                if (d <= thickness) {
                    planeColor = lx.hsb(plane.hue, saturation, 100);
                } else if (d <= thickness * 2) {
                    float value = 1 - ((d - thickness) / thickness);
                    planeColor = lx.hsb(plane.hue, saturation, value * 100);
                } else {
                    planeColor = 0;
                }

                if (planeColor != 0) {
                    if (c == 0) {
                        c = planeColor;
                    } else {
                        c = PImage.blendColor(c, planeColor, ADD);
                    }
                }
            }

            colors[p.index] = c;
        }
    }


    class Rotation {
        private float a, b, c, d, e, f, g, h, i;

        Rotation(float yaw, float pitch, float roll) {
            float cosYaw = MathUtils.cos(yaw);
            float sinYaw = MathUtils.sin(yaw);
            float cosPitch = MathUtils.cos(pitch);
            float sinPitch = MathUtils.sin(pitch);
            float cosRoll = MathUtils.cos(roll);
            float sinRoll = MathUtils.sin(roll);

            a = cosYaw * cosPitch;
            b = cosYaw * sinPitch * sinRoll - sinYaw * cosRoll;
            c = cosYaw * sinPitch * cosRoll + sinYaw * sinRoll;
            d = sinYaw * cosPitch;
            e = sinYaw * sinPitch * sinRoll + cosYaw * cosRoll;
            f = sinYaw * sinPitch * cosRoll - cosYaw * sinRoll;
            g = -1 * sinPitch;
            h = cosPitch * sinRoll;
            i = cosPitch * cosRoll;
        }

        LXVector rotated(LXVector v) {
            return new LXVector(
                rotatedX(v),
                rotatedY(v),
                rotatedZ(v));

        }

        float rotatedX(LXVector v) {
            return a * v.x + b * v.y + c * v.z;
        }

        float rotatedY(LXVector v) {
            return d * v.x + e * v.y + f * v.z;
        }

        float rotatedZ(LXVector v) {
            return g * v.x + h * v.y + i * v.z;
        }
    }

}
