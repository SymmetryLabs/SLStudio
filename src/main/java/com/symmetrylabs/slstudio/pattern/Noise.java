package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.util.dan.DPat;
import com.symmetrylabs.slstudio.util.dan.NDat;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PImage;
import processing.core.PVector;

import static com.symmetrylabs.slstudio.util.Utils.noise;
import static processing.core.PApplet.*;


public class Noise extends DPat {
    int CurAnim, iSymm;
    int XSym = 1, YSym = 2, RadSym = 3;
    float zTime, zTheta = 0, zSin, zCos, rtime, ttime;
    CompoundParameter pSpeed, pDensity, pSharp;
    DiscreteParameter pChoose, pSymm;
    int _ND = 4;
    NDat N[] = new NDat[_ND];

    public Noise(LX lx) {
        super(lx);
        pSpeed = new CompoundParameter("Fast", .55, -2, 2);
        addParameter(pSpeed);
        pDensity = addParam("Dens", .3);
        pSharp = addParam("Shrp", 0);
        pSymm = new DiscreteParameter("Symm", new String[]{"None", "X", "Y", "Rad"});
        pChoose =
            new DiscreteParameter("Anim", new String[]{"Drip", "Cloud", "Rain", "Fire", "Mach", "Spark", "VWav", "Wave"});
        pChoose.setValue(6);
        addParameter(pSymm);
        addParameter(pChoose);
        //addNonKnobParameter(pSymm);
        //addNonKnobParameter(pChoose);
        //addSingleParameterUIRow(pChoose);
        //addSingleParameterUIRow(pSymm);
        for (int i = 0; i < _ND; i++) N[i] = new NDat();
    }

    public void onActive() {
        zTime = random(500);
        zTheta = 0;
        rtime = 0;
        ttime = 0;
    }

    @Override
    protected void StartRun(double deltaMs) {
        zTime += deltaMs * (1 * val(pSpeed) - .50f) * .002f;
        zTheta += deltaMs * (spin() - .5f) * .01f;
        rtime += deltaMs;
        iSymm = pSymm.getValuei();
        zSin = sin(zTheta);
        zCos = cos(zTheta);

        if (pChoose.getValuei() != CurAnim) {
            CurAnim = pChoose.getValuei();
            ttime = rtime;
            pSpin.reset();
            zTheta = 0;
            pDensity.reset();
            pSpeed.reset();
            for (int i = 0; i < _ND; i++) {
                N[i].isActive = false;
            }

            switch (CurAnim) {
                //               hue xz  yz  zz den mph angle
                case 0:
                    N[0].set(0, 75, 75, 150, 45, 3, 0);
                    N[1].set(20, 25, 50, 50, 25, 1, 0);
                    N[2].set(80, 25, 50, 50, 15, 2, 0);
                    pSharp.setValue(1);
                    break;  // drip
                case 1:
                    N[0].set(0, 100, 100, 200, 45, 3, 180);
                    pSharp.setValue(0);
                    break;  // clouds
                case 2:
                    N[0].set(0, 2, 400, 2, 20, 3, 0);
                    pSharp.setValue(.5);
                    break;  // rain
                case 3:
                    N[0].set(40, 100, 100, 200, 10, 1, 180);
                    N[1].set(0, 100, 100, 200, 10, 5, 180);
                    pSharp.setValue(0);
                    break;  // fire 1
                case 4:
                    N[0].set(0, 40, 40, 40, 15, 2.5f, 180);
                    N[1].set(20, 40, 40, 40, 15, 4, 0);
                    N[2].set(40, 40, 40, 40, 15, 2, 90);
                    N[3].set(60, 40, 40, 40, 15, 3, -90);
                    pSharp.setValue(.5);
                    break; // machine
                case 5:
                    N[0].set(0, 400, 100, 2, 15, 3, 90);
                    N[1].set(20, 400, 100, 2, 15, 2.5f, 0);
                    N[2].set(40, 100, 100, 2, 15, 2, 180);
                    N[3].set(60, 100, 100, 2, 15, 1.5f, 270);
                    pSharp.setValue(.5);
                    break; // spark
            }
        }

        for (int i = 0; i < _ND; i++)
            if (N[i].Active()) {
                N[i].sinAngle = sin(radians(N[i].angle));
                N[i].cosAngle = cos(radians(N[i].angle));
            }
    }

    @Override
    public int CalcPoint(PVector p) {
        int c = 0;
        rotateZ(p, mCtr, zSin, zCos);
        //rotateY(p, mCtr, ySin, yCos);
        //rotateX(p, mCtr, xSin, xCos);
        if (CurAnim == 6 || CurAnim == 7) {
            setNorm(p);
            return lx.hsb(lxh(), 100, 100 * (
                constrain(1 - 50 * (1 - val(pDensity)) * abs(p.y - sin(zTime * 10 + p.x * (300)) * .5f - .5f), 0, 1) +
                    (CurAnim == 7 ? constrain(1 - 50 * (1 - val(pDensity)) * abs(p.x - sin(zTime * 10 + p.y * (300)) * .5f - .5f),
                        0,
                        1) : 0))
            );
        }

        if (iSymm == XSym && p.x > mMax.x / 2) p.x = mMax.x - p.x;
        if (iSymm == YSym && p.y > mMax.y / 2) p.y = mMax.y - p.y;

        for (int i = 0; i < _ND; i++)
            if (N[i].Active()) {
                NDat n = N[i];
                float zx = zTime * n.speed * n.sinAngle,
                    zy = zTime * n.speed * n.cosAngle;

                float b = (iSymm == RadSym ? (zTime * n.speed + n.xoff - p.dist(mCtr) / n.xz)
                    : noise(p.x / n.xz + zx + n.xoff, p.y / n.yz + zy + n.yoff, p.z / n.zz + n.zoff))
                    * 1.8f;

                b += n.den / 100 - .4 + val(pDensity) - 1;
                c = PImage.blendColor(c, lx.hsb(lxh() + n.hue, 100, c1c(b)), ADD);
            }
        return c;
    }
}
