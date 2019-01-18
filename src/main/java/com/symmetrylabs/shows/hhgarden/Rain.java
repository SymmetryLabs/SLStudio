package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.hhgarden.FlowerModel.Direction;
import com.symmetrylabs.shows.hhgarden.FlowerModel.FlowerPoint;
import com.symmetrylabs.shows.hhgarden.FlowerModel.Group;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Rain extends FlowerPattern {
    private final CompoundParameter rateParam = new CompoundParameter("rate", 0.8, 0, 1);
    private final CompoundParameter decayParam = new CompoundParameter("decay", 0.99, 0.9, 1);

    double coeffs[];
    Random rand = new Random();

    public Rain(LX lx) {
        super(lx);
        addParameter(rateParam);
        addParameter(decayParam);
        coeffs = new double[model.getFlowers().size()];
    }

    @Override
    public void run(double elapsedMs) {
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] *= decayParam.getValuef();
        }
        double rate = rateParam.getValue();
        for (int i = 0; i < 1000 && rand.nextDouble() < rate; i++) {
            coeffs[rand.nextInt(coeffs.length)] = 1;
        }
        List<FlowerModel> flowers = model.getFlowers();
        for (int i = 0; i < flowers.size(); i++) {
            int flowerColor = LXColor.gray(100.f * coeffs[i]);
            for (LXPoint p : flowers.get(i).points) {
                colors[p.index] = flowerColor;
            }
        }
    }
}
