package com.symmetrylabs.slstudio.pattern.raven;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.p3lx.P3LX;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static com.symmetrylabs.slstudio.util.Utils.noise;
import static processing.core.PApplet.CLOSE;
import static processing.core.PApplet.*;
import static processing.core.PConstants.HALF_PI;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;


public class RKPattern01 extends P3CubeMapPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    BooleanParameter audioLink = new BooleanParameter("audioLink", false);
    CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
    CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
    CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
    CompoundParameter amt = new CompoundParameter("amt", 20, 1, 25);
    CompoundParameter speed = new CompoundParameter("speed", PI, -TWO_PI * 2, TWO_PI * 2);
    CompoundParameter dsp = new CompoundParameter("dsp", HALF_PI, 0, PI);
    CompoundParameter nDsp = new CompoundParameter("nDsp", 1, .125, 2.5);

    int ringRes = 40, ringAmt = 20, pRingAmt = 20;
    float l1 = 600, l2 = 600, l3 = 600;
    float gTheta, gThetaSpacing, gWeightScalar;
    float rotX, rotXT, rotY, rotYT, rotZ, rotZT, dspmt, dspmtT, nDspmt, nDspmtT, thetaSpeed, thetaSpeedT;
    ArrayList<Ring> testRings;
    PVector rotDir;

    boolean audioLinked;

    float[] pEQBands = new float[16];
    float totalBandMag, avgBandMag;

    public RKPattern01(LX lx) {

        super(
            (P3LX) lx,
            new PVector(lx.model.cx, lx.model.cy, lx.model.cz),
            new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange),
            200
        );

        rotDir = PVector.random3D();

        testRings = new ArrayList<Ring>();
        for (int i = 0; i < ringAmt; i++) {
            Ring testRing = new Ring(ringRes, l1, l2, l3);
            testRings.add(testRing);
        }

        addParameter(audioLink);
        addParameter(rX);
        addParameter(rY);
        addParameter(rZ);
        addParameter(amt);
        addParameter(speed);
        addParameter(dsp);
        addParameter(nDsp);

        addModulator(eq).start();
    }

    public void run(double deltaMs, PGraphics pg) {

        updateParameters();
        replenish();

        for (int i = 0; i < testRings.size(); i++) {
            Ring testRing = testRings.get(i);
            testRing.update(i);
        }
        updateCubeMaps();

        pg.beginDraw();
        pg.background(0);
        pg.image(pgL, 0, faceRes);
        pg.image(pgR, faceRes * 2, faceRes);
        pg.image(pgD, faceRes, 0);
        pg.image(pgU, faceRes, faceRes * 2);
        pg.image(pgF, faceRes, faceRes);
        pg.image(pgB, faceRes * 3, faceRes);
        pg.endDraw();
    }

    void updateParameters() {

        audioLinked = audioLink.getValueb();
        if (!audioLinked) {
            rotXT = rX.getValuef();
            rotYT = rY.getValuef();
            rotZT = rZ.getValuef();
            thetaSpeedT = speed.getValuef();
            dspmtT = dsp.getValuef();
            nDspmtT = nDsp.getValuef();
        } else {
            totalBandMag = 0;
            for (int i = 0; i < pEQBands.length; i++) {
                totalBandMag += eq.getBandf(i);
            }
            avgBandMag = totalBandMag / 16;

            rotXT += rotDir.x * (eq.getBandf(0) - pEQBands[0]) * 2;
            rotYT += rotDir.z * (eq.getBandf(7) - pEQBands[7]) * 2;
            rotZT += rotDir.y * eq.getBandf(14) * .1;
            thetaSpeedT = map(sq(totalBandMag), 0, 256, 0, TWO_PI * 12);
            dspmtT = 0;
            nDspmtT = 0;

            for (int i = 0; i < pEQBands.length; i++) {
                pEQBands[i] = eq.getBandf(i);
            }
        }
        ringAmt = round(amt.getValuef());

        rotX = lerp(rotX, rotXT, .25f);
        rotY = lerp(rotY, rotYT, .25f);
        rotZ = lerp(rotZ, rotZT, .25f);

        thetaSpeed = lerp(thetaSpeed, thetaSpeedT, .25f);
        dspmt = lerp(dspmt, dspmtT, .1f);
        nDspmt = lerp(nDspmt, nDspmtT, .01f);

        gTheta += thetaSpeed / 720;
        if (gTheta > PI) gTheta -= PI;
        else if (gTheta < 0) gTheta += PI;
        gThetaSpacing = lerp(gThetaSpacing, PI / testRings.size(), .1f);
        gWeightScalar = map(ringAmt, 1, 25, 4, 1);
    }

    void updateCubeMap(
        PGraphics pg,
        float eyeX,
        float eyeY,
        float eyeZ,
        float centerX,
        float centerY,
        float centerZ,
        float upX,
        float upY,
        float upZ
    ) {
        pg.beginDraw();
        pg.background(0);
        pg.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        pg.frustum(-10, 10, -10, 10, 10, 1000);
        pg.rotateX(rotX);
        pg.rotateY(rotY);
        pg.rotateZ(rotZ);
        drawScene(pg);
        pg.endDraw();
    }

    void updateCubeMaps() {
        updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
        updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
        updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
        updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
        updateCubeMap(pgD, 0, 0, -.001f, 0, -1, 0, 0, 1, 0);
        updateCubeMap(pgU, 0, 0, -.001f, 0, 1, 0, 0, 1, 0);
    }

    void drawScene(PGraphics pg) {
        for (int i = 0; i < testRings.size(); i++) {
            Ring testRing = testRings.get(i);
            testRing.display(pg);
        }
    }

    void replenish() {
        if (pRingAmt != ringAmt) {
            if (ringAmt > pRingAmt) {
                for (int i = 0; i < ringAmt - pRingAmt; i++) {
                    Ring testRing = new Ring(ringRes, l1, l2, l3);
                    testRings.add(0, testRing);
                }
            }
            if (ringAmt < pRingAmt) {
                for (int i = 0; i < pRingAmt - ringAmt; i++) {
                    int j = testRings.size() - 1 - i;
                    j = constrain(j, 1, ringAmt);
                    testRings.remove(j);
                }
            }
        }
        pRingAmt = ringAmt;
    }

    class Ring {

        int amt;
        float l1, l2, l3, theta, weight;
        Vtx[] vts;

        Ring(int amt, float l1, float l2, float l3) {

            this.amt = amt;
            this.l1 = l1;
            this.l2 = l2;
            this.l3 = l3;

            theta = 0;

            vts = new Vtx[this.amt];
            for (int i = 0; i < vts.length; i++) {
                float initPhi = i * TWO_PI / amt;
                vts[i] = new Vtx(i, this.theta, initPhi, this.l1, this.l2, this.l3);
            }
        }

        void update(int idx) {
            theta = gTheta + idx * gThetaSpacing;
            if (theta > PI) theta -= PI;
            else if (theta < 0) theta += PI;

            weight = sin(theta) * 4 * gWeightScalar;

            for (int i = 0; i < vts.length; i++) {
                vts[i].update(theta);
            }
        }

        void display(PGraphics pg) {
            pg.noFill();
            pg.stroke(255);
            pg.strokeWeight(weight);
            pg.beginShape();
            for (int i = 0; i < vts.length; i++) {
                pg.vertex(vts[i].pos.x, vts[i].pos.y, vts[i].pos.z);
            }
            //pg.curveVertex(vts[0].pos.x, vts[0].pos.y, vts[0].pos.z);
            //pg.curveVertex(vts[1].pos.x, vts[1].pos.y, vts[1].pos.z);
            //pg.curveVertex(vts[2].pos.x, vts[2].pos.y, vts[2].pos.z);
            pg.endShape(CLOSE);
        }
    }

    class Vtx {

        int idx;
        PVector pos;
        float thetaBase, thetaOfst, thetaOfstRange, theta, phi;
        float l1, l2, l3;

        Vtx(int idx, float theta, float phi, float l1, float l2, float l3) {
            pos = new PVector();
            this.idx = idx;
            this.theta = thetaBase = theta;
            this.phi = phi;
            this.l1 = l1;
            this.l2 = l2;
            this.l3 = l3;
        }

        void update(float thetaBase) {
            this.thetaBase = thetaBase;

            if (!audioLinked) {
                thetaOfstRange = sin(theta) * dspmt;
                thetaOfst = (noise(
                    sin(phi) * cos(phi) * nDspmt + SLStudio.applet.frameCount * .005f,
                    this.thetaBase * nDspmt * .25f - SLStudio.applet.frameCount * .005f
                ) - .5f) * thetaOfstRange;
            } else {
                thetaOfstRange = sin(theta);
                int bandIdx = floor(map(idx, 0, ringRes, 0, 16));
                thetaOfst = avgBandMag - eq.getBandf(bandIdx);
            }
            theta = this.thetaBase + thetaOfst;
            pos.set(
                sin(theta) * cos(phi) * .5f * l1,
                cos(theta) * .5f * l2,
                sin(theta) * sin(phi) * .5f * l3
            );
        }
    }
}
