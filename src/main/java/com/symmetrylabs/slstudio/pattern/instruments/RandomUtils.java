package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.Random;

import heronarts.lx.transform.LXVector;

public class RandomUtils {
    static Random random = new Random();

    public static LXVector randomXyDisc() {
        while (true) {
            float x = random.nextFloat() * 2 - 1;
            float y = random.nextFloat() * 2 - 1;
            if (x*x + y*y < 1) {
                return new LXVector(x, y, 0);
            }
        }
    }

    public static LXVector randomSphere() {
        while (true) {
            float x = random.nextFloat() * 2 - 1;
            float y = random.nextFloat() * 2 - 1;
            float z = random.nextFloat() * 2 - 1;
            if (x*x + y*y + z*z < 1) {
                return new LXVector(x, y, z);
            }
        }
    }

    public static double randomVariation() {
        return random.nextDouble() * 2 - 1;
    }
}
