package com.symmetrylabs.slstudio.pattern;

import processing.core.PImage;
import processing.core.PVector;
import static processing.core.PConstants.ADD;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.util.NoiseUtils;
import com.symmetrylabs.util.MathUtils;

public class Noise extends DPat {
    private int currModeIndex, iSymm;
    public final CompoundParameter speed = new CompoundParameter("Speed", 0.55, -2, 2);
    public final CompoundParameter density = new CompoundParameter("Dens", 0.6);
    public final CompoundParameter sharp = new CompoundParameter("Sharp", 0);
    public final DiscreteParameter mode = new DiscreteParameter("Anim", new String[]{"Drip", "Cloud", "Rain", "Fire", "Mach", "Spark", "VWav", "Wave"});
    public final DiscreteParameter symm = new DiscreteParameter("Symm", new String[]{"None", "X", "Y", "Rad"});

    private int xSym = 1, ySym = 2, radSym = 3;
    private float zTime, zTheta = 0, zSin, zCos, rtime, ttime;
    private int NUM_NDAT = 4;
    private NDat n[] = new NDat[NUM_NDAT];

    public Noise(LX lx) {
        super(lx);
        addParameter(speed);
        addParameter(symm);
        addParameter(mode);
        addParameter(density);
        mode.setValue(5);

        for (int i = 0; i < NUM_NDAT; i++) {
            this.n[i] = new NDat();
        }
    }

    public void onActive() {
        zTime = random(500);
        zTheta = 0;
        rtime = 0;
        ttime = 0;
    }

    @Override
    protected void StartRun(double deltaMs) {
        zTime += deltaMs * (1 * val(speed) - 0.50f) * 0.002f;
        zTheta += deltaMs * (spin() - 0.5f) * 0.01f;
        rtime += deltaMs;
        iSymm = symm.getValuei();
        zSin = MathUtils.sin(zTheta);
        zCos = MathUtils.cos(zTheta);

        if (mode.getValuei() != currModeIndex) {
            currModeIndex = mode.getValuei();
            ttime = rtime;
            pSpin.reset();
            zTheta = 0;
            density.reset();
            speed.reset();

            for (int i = 0; i < n.length; i++) {
                n[i].isActive = false;
            }

            switch (currModeIndex) {
                // hue xz  yz  zz den mph angle

                case 0: // drip
                    n[0].set(0, 75, 75, 150, 45, 3, 0);
                    n[1].set(20, 25, 50, 50, 25, 1, 0);
                    n[2].set(80, 25, 50, 50, 15, 2, 0);
                    sharp.setValue(1);
                    break;

                case 1: // clouds
                    n[0].set(0, 100, 100, 200, 45, 3, 180);
                    sharp.setValue(0);
                    break;

                case 2: // rain
                    n[0].set(0, 2, 400, 2, 20, 3, 0);
                    sharp.setValue(.5);
                    break;

                case 3: // fire
                    n[0].set(40, 100, 100, 200, 10, 1, 180);
                    n[1].set(0, 100, 100, 200, 10, 5, 180);
                    sharp.setValue(0);
                    break;

                case 4: // machine
                    n[0].set(0, 40, 40, 40, 15, 2.5f, 180);
                    n[1].set(20, 40, 40, 40, 15, 4, 0);
                    n[2].set(40, 40, 40, 40, 15, 2, 90);
                    n[3].set(60, 40, 40, 40, 15, 3, -90);
                    sharp.setValue(.5);
                    break;

                case 5: // spark
                    n[0].set(0, 400, 100, 2, 15, 3, 90);
                    n[1].set(20, 400, 100, 2, 15, 2.5f, 0);
                    n[2].set(40, 100, 100, 2, 15, 2, 180);
                    n[3].set(60, 100, 100, 2, 15, 1.5f, 270);
                    sharp.setValue(0.5);
                    break;
            }
        }

        for (int i = 0; i < n.length; i++) {
            if (n[i].Active()) {
                n[i].sinAngle = MathUtils.sin(MathUtils.radians(n[i].angle));
                n[i].cosAngle = MathUtils.cos(MathUtils.radians(n[i].angle));
            }
        }
    }

    @Override
    public int CalcPoint(PVector p) {
        int col = 0;
        rotateZ(p, mCtr, zSin, zCos);
        //rotateY(p, mCtr, ySin, yCos);
        //rotateX(p, mCtr, xSin, xCos);
        if (currModeIndex == 6 || currModeIndex == 7) {
            setNorm(p);

            float bri = MathUtils.constrain(1 - 50 * (1 - val(density)) * MathUtils.abs(p.y - MathUtils.sin(zTime * 10 + p.x * (300)) * .5f - .5f), 0, 1);

            if (currModeIndex == 7) {
                bri += MathUtils.constrain(1 - 50 * (1 - val(density)) * MathUtils.abs(p.x - MathUtils.sin(zTime * 10 + p.y * (300)) * .5f - .5f), 0, 1);
            }

            return lx.hsb(lxh(), 100, 100 * bri);
        }

        if (iSymm == xSym && p.x > mMax.x / 2) p.x = mMax.x - p.x;
        if (iSymm == ySym && p.y > mMax.y / 2) p.y = mMax.y - p.y;

        for (int i = 0; i < n.length; i++)
            if (n[i].Active()) {
                NDat nDat = n[i];
                float zx = zTime * nDat.speed * nDat.sinAngle;
                float zy = zTime * nDat.speed * nDat.cosAngle;

                float bri = (iSymm == radSym ? (zTime * nDat.speed + nDat.xoff - p.dist(mCtr) / nDat.xz)
                    : NoiseUtils.noise(p.x / nDat.xz + zx + nDat.xoff, p.y / nDat.yz + zy + nDat.yoff, p.z / nDat.zz + nDat.zoff)) * 1.8f;

                bri += nDat.den / 100 - 0.4 + val(density) - 1;
                col = PImage.blendColor(col, lx.hsb(lxh() + nDat.hue, 100, c1c(bri)), ADD);
            }

        return col;
    }
}
