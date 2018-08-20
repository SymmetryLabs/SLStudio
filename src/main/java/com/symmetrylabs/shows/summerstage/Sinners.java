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
    float n = 0;

    CompoundParameter hue = new CompoundParameter("hue", 0.07);
    CompoundParameter hvar = new CompoundParameter("hvar", 0.07);
    CompoundParameter sat = new CompoundParameter("sat", 0.9);
    CompoundParameter speed = new CompoundParameter("speed", 7, 30);

    public Sinners(LX lx) {
        super(lx);

        for (int i = 0; i < model.points.length; i++) {
            offsets.add(MathUtils.random(1));
            multipliers.add(MathUtils.random(1));
            bases.add(MathUtils.random(1));
        }

        addParameter(hvar);
        addParameter(hue);
        addParameter(sat);
        addParameter(speed);
    }

    public void run(double deltaMs) {
        List<CubesModel.Cube> cubes = model.getCubes();

        for (int i = 0; i < cubes.size(); i++) {
            CubesModel.Cube cube = cubes.get(i);
            float pos = (float) i / (float) cubes.size();
            float s = sin(multipliers.get(i) * n + offsets.get(i));
            s = (s + 1.0f) / 2.0f;
            float c = bases.get(i) + NoiseUtils.noise(pos + n / 100.0f) / 2;
            float huevar = hvar.getValuef();
            float actual_c = (hue.getValuef()) + (c * huevar);

            setColor(cube, LXColor.hsb(actual_c * 360, sat.getValuef() * 100, s * 100));
        }

        n += speed.getValuef() / 1000.0 * deltaMs;
    }
}