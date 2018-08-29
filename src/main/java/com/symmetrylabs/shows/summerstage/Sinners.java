package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.NoiseUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.transform.LXVector;

import java.util.List;
import java.util.LinkedList;
import processing.core.PImage;
import java.util.Iterator;

import java.lang.Math;
import static processing.core.PApplet.*;

import heronarts.lx.color.*;

import java.util.*;

import com.symmetrylabs.shows.summerstage.SummerStageShow;

public class Sinners extends SLPattern<CubesModel> {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    ArrayList<Float> offsets = new ArrayList();
    ArrayList<Float> multipliers = new ArrayList();
    ArrayList<Float> bases = new ArrayList();
    ArrayList<Integer> order = new ArrayList();

    float n = 0;

    CompoundParameter hue = new CompoundParameter("hue", 0.86);
    CompoundParameter hvar = new CompoundParameter("hvar", 0.18);
    CompoundParameter sat = new CompoundParameter("sat", 0.9);
    CompoundParameter speed = new CompoundParameter("speed", 7, 0, 30);
    CompoundParameter density = new CompoundParameter("density", 1.0);
    CompoundParameter densityFade = new CompoundParameter("dfade", 0.1);

    CompoundParameter minPeriod = new CompoundParameter("minPeriod", 0, 0, 1);
    CompoundParameter maxPeriod = new CompoundParameter("maxPeriod", 1, 0, 1);



    public Sinners(LX lx) {
        super(lx);

        for (int i = 0; i < model.getCubes().size(); i++) {
            offsets.add(MathUtils.random(1));
            multipliers.add(MathUtils.random(1));
            bases.add(MathUtils.random(1));
            order.add(i);
        }
        Collections.shuffle(order);

        addParameter(hvar);
        addParameter(hue);
        addParameter(sat);
        addParameter(speed);
        addParameter(density);
        addParameter(densityFade);
        addParameter(minPeriod);
        addParameter(maxPeriod);
    }

    public void run(double deltaMs) {
        List<CubesModel.Cube> cubes = model.getCubes();

        
        for (int i = 0; i < cubes.size(); i++) {
            CubesModel.Cube cube = cubes.get(i);

            int o = order.get(i);
            float oR = (float)o / (float)order.size();
            float falloff = 1.0f;
            float dens = density.getValuef();
            float maxDist = densityFade.getValuef();
            if (oR >= dens) {
                float dist = MathUtils.min(oR - dens, maxDist);
                falloff = MathUtils.map(dist, 0, maxDist, 1.0f, 0.0f);
            }

            float pos = (float) i / (float) cubes.size();
            float m = MathUtils.map(multipliers.get(i), 0, 1, minPeriod.getValuef(), maxPeriod.getValuef());
            float s = sin(m * n + offsets.get(i));
            s = (s + 1.0f) / 2.0f;
            float c = bases.get(i) + NoiseUtils.noise(pos + n / 100.0f) / 2;
            float huevar = hvar.getValuef();
            float actual_c = (hue.getValuef()) + (c * huevar);

            setColor(cube, LXColor.hsb(actual_c * 360, sat.getValuef() * 100, s * 100 * falloff));
        }

        n += speed.getValuef() / 1000.0 * deltaMs;
    }
}
