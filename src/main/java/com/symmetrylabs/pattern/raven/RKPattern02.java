package com.symmetrylabs.pattern.raven;

import com.symmetrylabs.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.p3lx.P3LX;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static com.symmetrylabs.util.Utils.noise;
import static com.symmetrylabs.util.Utils.random;
import static processing.core.PApplet.*;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.TWO_PI;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RKPattern02 extends P3CubeMapPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    BooleanParameter audioLink = new BooleanParameter("audioLink", false);
    CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
    CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
    CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
    BooleanParameter noise = new BooleanParameter("noise");
    CompoundParameter speed = new CompoundParameter("speed", .1, 0, .5);
    CompoundParameter dsp = new CompoundParameter("dsp", 1, 0, 2);
    BooleanParameter edge = new BooleanParameter("show edge", true);

    ArrayList<Arc> arcs;
    float f, fT, fTIncre;
    float l1 = 600, l2 = 600, l3 = 600;
    float rotX, rotXT, rotY, rotYT, rotZ, rotZT, dspmt, dspmtT;
    float gWeight = 2, gWeightT = 2;
    boolean autoNoise, showEdge;
    int switchCount;
    PVector rotDir;

    boolean audioLinked;

    float[] pEQBands = new float[16];
    float totalBandMag, avgBandMag;

    public RKPattern02(LX lx) {

        super(
            (P3LX) lx,
            new PVector(lx.model.cx, lx.model.cy, lx.model.cz),
            new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange),
            200
        );

        arcs = new ArrayList<Arc>();
        Arc parentArc = new Arc(0, 0, 12, 0, PI, 0, TWO_PI);
        arcs.add(parentArc);

        for (int i = 0; i < 8; i++) {
            for (int j = arcs.size() - 1; j > -1; j--) {
                Arc arc = arcs.get(j);
                if (!arc.hasChildren) arc.splitUp();
            }
        }

        rotDir = PVector.random3D();

        addParameter(audioLink);
        addParameter(rX);
        addParameter(rY);
        addParameter(rZ);
        addParameter(noise);
        addParameter(speed);
        addParameter(dsp);
        addParameter(edge);

        addModulator(eq).start();
    }

    public void run(double deltaMs, PGraphics pg) {

        updateParameters();

        if (SLStudio.applet.frameCount % 90 == 0 && !autoNoise && !audioLinked) {
            switchCount = (switchCount + 1) % 3;
            if (switchCount == 0) {
                resetArcs();
                for (int j = 0; j < 8; j++) {
                    for (int i = arcs.size() - 1; i > -1; i--) {
                        Arc arc = arcs.get(i);
                        if (!arc.hasChildren) arc.splitUp();
                    }
                }
                for (int i = arcs.size() - 1; i > -1; i--) {
                    Arc arc = arcs.get(i);
                    arc.reset();
                }
            } else {
                resetArcs();
                for (int j = 0; j < 500; j++) {
                    Arc arc = arcs.get(floor(random(arcs.size())));
                    if (!arc.hasChildren) arc.splitUp();
                }
                for (int i = arcs.size() - 1; i > -1; i--) {
                    Arc arc = arcs.get(i);
                    arc.randomize();
                }
            }
        }

        for (int i = 0; i < arcs.size(); i++) {
            Arc arc = arcs.get(i);
            if (autoNoise || audioLinked) {
                float scalar = map(arc.lv, 0, 8, dspmt, dspmt * 1.5f);
                arc.splitRatio = .5f + (noise(arc.idx * .1f + f) - .5f) * scalar;
                arc.fragRatioS = (noise(arc.idx * .1f + f * 2 + 2.46f) - .5f) * 1.2f + .5f;
                arc.fragRatioE = (noise(arc.idx * .1f + f * 2 + 11.27f) - .5f) * 1.2f + .5f;
            }
            arc.update();
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
        showEdge = edge.getValueb();
        if (!audioLinked) {
            rotXT = rX.getValuef();
            rotYT = rY.getValuef();
            rotZT = rZ.getValuef();
            autoNoise = noise.getValueb();
            dspmtT = dsp.getValuef();
            fTIncre = speed.getValuef();
        } else {

            totalBandMag = 0;
            for (int i = 0; i < pEQBands.length; i++) {
                totalBandMag += eq.getBandf(i);
            }
            avgBandMag = totalBandMag / 16;

            rotXT += rotDir.x * (eq.getBandf(0) - pEQBands[0]) * 2;
            rotYT += rotDir.z * (eq.getBandf(7) - pEQBands[7]) * 2;
            rotZT += rotDir.y * eq.getBandf(14) * .1;

            fTIncre = map(sq(totalBandMag), 0, 256, 0, 3);
            dspmtT = map(sq(totalBandMag), 0, 256, 0, 3);

            for (int i = 0; i < pEQBands.length; i++) {
                pEQBands[i] = eq.getBandf(i);
            }
        }

        rotX = lerp(rotX, rotXT, .25f);
        rotY = lerp(rotY, rotYT, .25f);
        rotZ = lerp(rotZ, rotZT, .25f);
        dspmt = lerp(dspmt, dspmtT, .25f);

        if (showEdge) gWeightT = 2;
        else gWeightT = 0;
        if (abs(gWeightT - gWeight) < .005) gWeight = gWeightT;
        else gWeight = lerp(gWeight, gWeightT, .1f);

        fT += fTIncre * .1;
        f = lerp(f, fT, .25f);
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
        pg.rotateX(rotX + HALF_PI);
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
        for (int i = 0; i < arcs.size(); i++) {
            Arc arc = arcs.get(i);
            if (arc.hasChildren) arc.displayEdge(pg);
            else arc.displayFragment(pg);
        }
        pg.noFill();
        pg.stroke(255);
        pg.strokeWeight(gWeight);
        pg.beginShape();
        for (int i = 0; i < 16; i++) {
            float phi = 0;
            float theta = i * PI / 15;
            pg.vertex(
                sin(theta) * cos(phi) * l1 * .5f,
                cos(theta) * l2 * .5f,
                sin(theta) * sin(phi) * l3 * .5f
            );
        }
        pg.endShape();
    }

    void resetArcs() {
        for (int i = arcs.size() - 1; i > -1; i--) {
            arcs.remove(i);
        }
        Arc parentArc = new Arc(0, 0, 12, 0, PI, 0, TWO_PI);
        arcs.add(parentArc);
    }

    class Arc {

        Arc cArc1, cArc2;
        boolean hasChildren;
        int segAmt, lv, idx, dir;

        PVector[] vts;
        PVector[][] fragVts;

        float splitRatio, splitRatioT, splitRatioStp;
        float sTheta, eTheta, sPhi, ePhi, sThetaFrg, eThetaFrg, sPhiFrg, ePhiFrg;
        float l1 = 600, l2 = 600, l3 = 600;
        float fragRatioS = 0, fragRatioST = .4f, fragRatioE = 1, fragRatioET = .6f, fragRatioSStp, fragRatioEStp;

        Arc(int lv, int idx, int segAmt, float sTheta, float eTheta, float sPhi, float ePhi) {

            this.lv = lv;
            this.idx = idx;
            this.segAmt = segAmt;
            this.sTheta = sTheta;
            this.eTheta = eTheta;
            this.sPhi = sPhi;
            this.ePhi = ePhi;

            splitRatioStp = random(.03125f, .25f);
            fragRatioSStp = random(.0625f, .25f);
            fragRatioEStp = fragRatioSStp;

            fragVts = new PVector[segAmt / 2][segAmt / 2];
            for (int i = 0; i < segAmt / 2; i++) {
                for (int j = 0; j < segAmt / 2; j++) {
                    fragVts[i][j] = new PVector();
                }
            }

            dir = floor(random(4));
        }

        void update() {
            if (!autoNoise) {
                splitRatio = lerp(splitRatio, splitRatioT, splitRatioStp);
                fragRatioS = lerp(fragRatioS, fragRatioST, fragRatioSStp);
                fragRatioE = lerp(fragRatioE, fragRatioET, fragRatioEStp);
            }

            if (dir == 0) {
                sThetaFrg = sTheta;
                eThetaFrg = eTheta;
                sPhiFrg = lerp(sPhi, ePhi, fragRatioS);
                ePhiFrg = lerp(sPhi, ePhi, fragRatioE);
            } else if (dir == 1) {
                sThetaFrg = lerp(sTheta, eTheta, fragRatioS);
                eThetaFrg = lerp(sTheta, eTheta, fragRatioE);
                sPhiFrg = sPhi;
                ePhiFrg = ePhi;
            } else if (dir == 2) {
                sThetaFrg = sTheta;
                eThetaFrg = eTheta;
                sPhiFrg = lerp(ePhi, sPhi, fragRatioS);
                ePhiFrg = lerp(ePhi, sPhi, fragRatioE);
            } else {
                sThetaFrg = lerp(eTheta, sTheta, fragRatioS);
                eThetaFrg = lerp(eTheta, sTheta, fragRatioE);
                sPhiFrg = sPhi;
                ePhiFrg = ePhi;
            }

            if (hasChildren) {
                if (lv % 2 == 0) {
                    for (int i = 0; i < segAmt; i++) {
                        float phi = sPhi + i * (ePhi - sPhi) / (vts.length - 1);
                        float theta = lerp(sTheta, eTheta, splitRatio);

                        vts[i].set(
                            sin(theta) * cos(phi) * l1 * .5f,
                            cos(theta) * l2 * .5f,
                            sin(theta) * sin(phi) * l3 * .5f
                        );
                    }
                    cArc1.sTheta = sTheta;
                    cArc1.eTheta = lerp(sTheta, eTheta, splitRatio);
                    cArc2.sTheta = lerp(sTheta, eTheta, splitRatio);
                    cArc2.eTheta = eTheta;
                    cArc1.sPhi = sPhi;
                    cArc1.ePhi = ePhi;
                    cArc2.sPhi = sPhi;
                    cArc2.ePhi = ePhi;
                } else {
                    for (int i = 0; i < segAmt; i++) {
                        float phi = lerp(sPhi, ePhi, splitRatio);
                        float theta = sTheta + i * (eTheta - sTheta) / (vts.length - 1);

                        vts[i].set(
                            sin(theta) * cos(phi) * l1 * .5f,
                            cos(theta) * l2 * .5f,
                            sin(theta) * sin(phi) * l3 * .5f
                        );
                    }
                    cArc1.sTheta = sTheta;
                    cArc1.eTheta = eTheta;
                    cArc2.sTheta = sTheta;
                    cArc2.eTheta = eTheta;
                    cArc1.sPhi = sPhi;
                    cArc1.ePhi = lerp(sPhi, ePhi, splitRatio);
                    cArc2.sPhi = lerp(sPhi, ePhi, splitRatio);
                    cArc2.ePhi = ePhi;
                }
            } else {
                for (int i = 0; i < segAmt / 2; i++) {
                    for (int j = 0; j < segAmt / 2; j++) {
                        float theta = sThetaFrg + i * (eThetaFrg - sThetaFrg) / (segAmt / 2 - 1);
                        float phi = sPhiFrg + j * (ePhiFrg - sPhiFrg) / (segAmt / 2 - 1);

                        fragVts[i][j].set(
                            sin(theta) * cos(phi) * l1 * .5f,
                            cos(theta) * l2 * .5f,
                            sin(theta) * sin(phi) * l3 * .5f
                        );
                    }
                }
            }
        }

        void randomize() {
            splitRatioT = random(1);
            fragRatioS = 0;
            fragRatioST = 1;
            fragRatioE = 0;
            fragRatioET = 1;
            fragRatioEStp = fragRatioSStp * .5f;
        }

        void reset() {
            splitRatioT = .5f;
            fragRatioS = .5f;
            fragRatioST = 0;
            fragRatioE = .5f;
            fragRatioET = 1;
            fragRatioEStp = fragRatioSStp;
        }

        void splitUp() {

            hasChildren = true;
            splitRatio = random(1);
            splitRatioT = .5f;

            vts = new PVector[segAmt];

            if (lv % 2 == 0) {
                cArc1 = new Arc(lv + 1, arcs.size(), segAmt, sTheta, lerp(sTheta, eTheta, splitRatio), sPhi, ePhi);
                arcs.add(cArc1);
                cArc2 = new Arc(lv + 1, arcs.size(), segAmt, lerp(sTheta, eTheta, splitRatio), eTheta, sPhi, ePhi);
                arcs.add(cArc2);

                for (int i = 0; i < segAmt; i++) {
                    float phi = sPhi + i * (ePhi - sPhi) / (vts.length - 1);
                    float theta = lerp(sTheta, eTheta, splitRatio);

                    vts[i] = new PVector(
                        sin(theta) * cos(phi) * l1 * .5f,
                        cos(theta) * l2 * .5f,
                        sin(theta) * sin(phi) * l3 * .5f
                    );
                }
            } else {
                cArc1 = new Arc(lv + 1, arcs.size(), segAmt, sTheta, eTheta, sPhi, lerp(sPhi, ePhi, splitRatio));
                arcs.add(cArc1);
                cArc2 = new Arc(lv + 1, arcs.size(), segAmt, sTheta, eTheta, lerp(sPhi, ePhi, splitRatio), ePhi);
                arcs.add(cArc2);

                for (int i = 0; i < segAmt; i++) {
                    float phi = lerp(sPhi, ePhi, splitRatio);
                    float theta = sTheta + i * (eTheta - sTheta) / (vts.length - 1);

                    vts[i] = new PVector(
                        sin(theta) * cos(phi) * l1 * .5f,
                        cos(theta) * l2 * .5f,
                        sin(theta) * sin(phi) * l3 * .5f
                    );
                }
            }
        }

        void displayEdge(PGraphics pg) {
            pg.noFill();
            pg.stroke(255);
            pg.strokeWeight(gWeight);
            pg.beginShape();
            for (int i = 0; i < segAmt; i++) {
                pg.vertex(vts[i].x, vts[i].y, vts[i].z);
            }
            pg.endShape();
        }

        void displayFragment(PGraphics pg) {
            pg.noStroke();
            pg.fill(255);
            pg.beginShape(TRIANGLES);
            for (int i = 0; i < segAmt / 2 - 1; i++) {
                for (int j = 0; j < segAmt / 2 - 1; j++) {
                    pg.vertex(fragVts[i][j].x, fragVts[i][j].y, fragVts[i][j].z);
                    pg.vertex(fragVts[i][j + 1].x, fragVts[i][j + 1].y, fragVts[i][j + 1].z);
                    pg.vertex(fragVts[i + 1][j + 1].x, fragVts[i + 1][j + 1].y, fragVts[i + 1][j + 1].z);
                    pg.vertex(fragVts[i][j].x, fragVts[i][j].y, fragVts[i][j].z);
                    pg.vertex(fragVts[i + 1][j].x, fragVts[i + 1][j].y, fragVts[i + 1][j].z);
                    pg.vertex(fragVts[i + 1][j + 1].x, fragVts[i + 1][j + 1].y, fragVts[i + 1][j + 1].z);
                }
            }
            pg.endShape(CLOSE);
        }
    }
}
