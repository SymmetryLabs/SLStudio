package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.LXUtils;

import processing.core.PVector;

import com.symmetrylabs.slstudio.pattern.SLPattern;

import com.symmetrylabs.slstudio.util.NoiseUtils;

public class Warp extends SLPattern {

    private final int X_RES = 12;
    private final int Y_RES = 6;

    private int xOff = 0;
    private int yOff = 0;

    private final PVector[] spots = new PVector[X_RES * Y_RES];

    public Warp(LX lx) {
        super(lx);

        for (int i = 0; i < spots.length; i++) {
            float xPos = ((i % X_RES / (float)X_RES) * model.xRange) + model.xMin;
            float yPos = ((i / X_RES / (float)Y_RES) * model.yRange) + model.yMin;
            spots[i] = new PVector(xPos, yPos, 0);
        }
    }

    public void run(double deltaMs) {
        setColors(0);
        xOff += 0.01;
        yOff += 0.01;

        for (PVector spot : spots) {
            spot.add(
                NoiseUtils.noise((spot.x / model.xRange) + xOff),
                NoiseUtils.noise((spot.y / model.yRange) + yOff),
                0
            );
        }

        for (LXPoint p : model.points) {
            for (PVector spot : spots) {
                if (LXUtils.distance(p.x, p.y, spot.x, spot.y) < 5) {
                    colors[p.index] = lx.hsb(0, 100, 100);
                }
            }
        }
    }

}