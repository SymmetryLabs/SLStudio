package com.symmetrylabs.util;

import java.util.Random;

/**
 * Perlin noise
 */
public class NoiseHelper {

    static final int PERLIN_YWRAPB = 4;
    static final int PERLIN_YWRAP = 1 << PERLIN_YWRAPB;
    static final int PERLIN_ZWRAPB = 8;
    static final int PERLIN_ZWRAP = 1 << PERLIN_ZWRAPB;
    static final int PERLIN_SIZE = 4095;

    static final double DEG_TO_RAD = Math.PI / 180;

    static final protected float sinLUT[];
    static final protected float cosLUT[];
    static final protected float SINCOS_PRECISION = 0.5f;
    static final protected int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);
    static {
        sinLUT = new float[SINCOS_LENGTH];
        cosLUT = new float[SINCOS_LENGTH];
        for (int i = 0; i < SINCOS_LENGTH; i++) {
            sinLUT[i] = (float)Math.sin(i * DEG_TO_RAD * SINCOS_PRECISION);
            cosLUT[i] = (float)Math.cos(i * DEG_TO_RAD * SINCOS_PRECISION);
        }
    }

    int perlin_octaves = 4; // default to medium smooth
    float perlin_amp_falloff = 0.5f; // 50% reduction/octave

    // [toxi 031112]
    // new vars needed due to recent change of cos table in PGraphics
    int perlin_TWOPI, perlin_PI;
    float[] perlin_cosTable;
    float[] perlin;

    Random perlinRandom;

    public NoiseHelper() {
        perlinRandom = new Random();
        perlin = new float[PERLIN_SIZE + 1];
        for (int i = 0; i < PERLIN_SIZE + 1; i++) {
            perlin[i] = perlinRandom.nextFloat(); //(float)Math.random();
        }
        // [toxi 031112]
        // noise broke due to recent change of cos table in PGraphics
        // this will take care of it
        perlin_cosTable = cosLUT;
        perlin_TWOPI = perlin_PI = SINCOS_LENGTH;
        perlin_PI >>= 1;
    }

    public float noise(float x) {
        // is this legit? it's a dumb way to do it (but repair it later)
        return noise(x, 0f, 0f);
    }

    public float noise(float x, float y) {
        return noise(x, y, 0f);
    }

    public float noise(float x, float y, float z) {

        if (x<0) x=-x;
        if (y<0) y=-y;
        if (z<0) z=-z;

        int xi=(int)x, yi=(int)y, zi=(int)z;
        float xf = x - xi;
        float yf = y - yi;
        float zf = z - zi;
        float rxf, ryf;

        float r=0;
        float ampl=0.5f;

        float n1,n2,n3;

        for (int i=0; i<perlin_octaves; i++) {
            int of=xi+(yi<<PERLIN_YWRAPB)+(zi<<PERLIN_ZWRAPB);

            rxf=noise_fsc(xf);
            ryf=noise_fsc(yf);

            n1  = perlin[of&PERLIN_SIZE];
            n1 += rxf*(perlin[(of+1)&PERLIN_SIZE]-n1);
            n2  = perlin[(of+PERLIN_YWRAP)&PERLIN_SIZE];
            n2 += rxf*(perlin[(of+PERLIN_YWRAP+1)&PERLIN_SIZE]-n2);
            n1 += ryf*(n2-n1);

            of += PERLIN_ZWRAP;
            n2  = perlin[of&PERLIN_SIZE];
            n2 += rxf*(perlin[(of+1)&PERLIN_SIZE]-n2);
            n3  = perlin[(of+PERLIN_YWRAP)&PERLIN_SIZE];
            n3 += rxf*(perlin[(of+PERLIN_YWRAP+1)&PERLIN_SIZE]-n3);
            n2 += ryf*(n3-n2);

            n1 += noise_fsc(zf)*(n2-n1);

            r += n1*ampl;
            ampl *= perlin_amp_falloff;
            xi<<=1; xf*=2;
            yi<<=1; yf*=2;
            zi<<=1; zf*=2;

            if (xf>=1.0f) { xi++; xf--; }
            if (yf>=1.0f) { yi++; yf--; }
            if (zf>=1.0f) { zi++; zf--; }
        }
        return r;
    }

    private float noise_fsc(float i) {
        // using bagel's cosine table instead
        return 0.5f*(1.0f-perlin_cosTable[(int)(i*perlin_PI)%perlin_TWOPI]);
    }

    public void noiseDetail(int lod) {
        if (lod>0) perlin_octaves=lod;
    }

    public void noiseDetail(int lod, float falloff) {
        if (lod>0) perlin_octaves=lod;
        if (falloff>0) perlin_amp_falloff=falloff;
    }

    public void noiseSeed(long seed) {
        if (perlinRandom == null) perlinRandom = new Random();
        perlinRandom.setSeed(seed);
        // force table reset after changing the random number seed [0122]
        perlin = null;
    }
}
