package com.symmetrylabs.pattern.raven;

import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.p3lx.P3LX;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PApplet.*;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.TRIANGLES;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class RKPattern03 extends P3CubeMapPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    BooleanParameter audioLink = new BooleanParameter("audioLink", false);
    CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
    CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
    CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
    BooleanParameter avg = new BooleanParameter("avg split");
    BooleanParameter tri = new BooleanParameter("show tri", true);
    BooleanParameter edge = new BooleanParameter("show edge", false);
    CompoundParameter speed = new CompoundParameter("speed", .01, 0, .05);
    CompoundParameter fragment = new CompoundParameter("fragment", .5, 0, 1);

    Vtx[][] rootVts;
    int iCSCols = 5, iCSRows = 4;
    float gF, thetaF, phiF, fragMid;
    float[] pEQBands = new float[16];
    ArrayList<Fct> fctList;
    boolean audioLinked, avgSplit, pAvgSplit, showTri, showEdge;
    PVector rotDir;

    float rotX, rotXT, rotY, rotYT, rotZ, rotZT, gFIncre, gFIncreT;

    public RKPattern03(LX lx) {

        super(
            (P3LX) lx,
            new PVector(lx.model.cx, lx.model.cy, lx.model.cz),
            new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange),
            200
        );

        gF = random(100);
        thetaF = random(100);
        phiF = random(100);
        rotDir = PVector.random3D();

        initRootVts();

        fctList = new ArrayList<Fct>();
        initFctList();

        //avgSplitUp();
        randomSplitUp(100);

        addParameter(audioLink);
        addParameter(rX);
        addParameter(rY);
        addParameter(rZ);
        addParameter(avg);
        addParameter(tri);
        addParameter(edge);
        addParameter(speed);
        addParameter(fragment);

        addModulator(eq).start();
    }

    void run(double deltaMs, PGraphics pg) {

        updateParameters();

        for (int i = 0; i < fctList.size(); i++) {
            Fct fct = fctList.get(i);
            fct.update();
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
        showTri = tri.getValueb();
        showEdge = edge.getValueb();

        avgSplit = avg.getValueb();
        if (pAvgSplit != avgSplit) {
            if (avgSplit) {
                resetFctList();
                avgSplitUp();
            } else {
                resetFctList();
                randomSplitUp(100);
            }
            pAvgSplit = avgSplit;
        }

        if (!audioLinked) {
            rotXT = rX.getValuef();
            rotYT = rY.getValuef();
            rotZT = rZ.getValuef();
            gFIncreT = speed.getValuef();
            fragMid = fragment.getValuef();
        } else {
            float totalMag = 0;
            for (int i = 0; i < pEQBands.length; i++) {
                totalMag += eq.getBandf(i);
            }

            rotXT += rotDir.x * (eq.getBandf(0) - pEQBands[0]) * 2;
            rotYT += rotDir.z * (eq.getBandf(7) - pEQBands[7]) * 2;
            rotZT += rotDir.y * eq.getBandf(14) * .1;
            gFIncreT = map(sq(totalMag), 0, 256, 0, .3);
            fragMid = map(totalMag, 0, 16, 1, 0);

            for (int i = 0; i < pEQBands.length; i++) {
                pEQBands[i] = eq.getBandf(i);
            }
        }

        rotX = lerp(rotX, rotXT, .25);
        rotY = lerp(rotY, rotYT, .25);
        rotZ = lerp(rotZ, rotZT, .25);
        gFIncre = lerp(gFIncre, gFIncreT, .25);
        gF += gFIncre;
    }

    void updateCubeMaps() {
        updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
        updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
        updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
        updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
        updateCubeMap(pgD, 0, 0, -.001, 0, -1, 0, 0, 1, 0);
        updateCubeMap(pgU, 0, 0, -.001, 0, 1, 0, 0, 1, 0);
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

    void drawScene(PGraphics pg) {
        pg.beginShape(TRIANGLES);
        pg.noStroke();
        for (int i = fctList.size() - 1; i > -1; i--) {
            Fct fct = fctList.get(i);
            if (!fct.hasChildren) fct.displayTri(pg);
        }
        pg.endShape();
        pg.stroke(255);
        for (int i = fctList.size() - 1; i > -1; i--) {
            Fct fct = fctList.get(i);
            if (!fct.hasChildren) fct.displayEdge(pg);
        }
    }

    void initFctList() {

        for (int j = 0; j < rootVts[1].length; j++) {
            int k = (j + 1) % rootVts[1].length;
            Fct fct = new Fct(0, rootVts[0][0], rootVts[1][j], rootVts[1][k]);
            fctList.add(fct);
        }

        for (int j = 0; j < rootVts[2].length; j++) {
            int k = (j + 1) % rootVts[2].length;
            Fct fct = new Fct(0, rootVts[3][0], rootVts[2][j], rootVts[2][k]);
            fctList.add(fct);
        }

        for (int j = 0; j < rootVts[1].length; j++) {
            int k = (j + 1) % rootVts[1].length;
            Fct fct1 = new Fct(0, rootVts[1][j], rootVts[1][k], rootVts[2][j]);
            fctList.add(fct1);
            Fct fct2 = new Fct(0, rootVts[2][j], rootVts[1][k], rootVts[2][k]);
            fctList.add(fct2);
        }
    }

    void resetFctList() {
        for (int i = fctList.size() - 1; i > -1; i--) {
            fctList.remove(i);
        }
        initFctList();
    }

    void avgSplitUp() {
        for (int j = 0; j < 2; j++) {
            for (int i = fctList.size() - 1; i > -1; i--) {
                Fct fct = fctList.get(i);
                if (!fct.hasChildren && fct.lv < 3) fct.splitUp();
            }
        }
    }

    void randomSplitUp(int times) {
        for (int i = 0; i < times; ) {
            int randInt = floor(random(fctList.size()));
            Fct randFct = fctList.get(randInt);
            if (randFct.hasChildren || randFct.lv > 3) {
                continue;
            } else {
                randFct.splitUp();
                i++;
            }
        }
    }

    class Fct {

        int lv;
        Vtx v1, v2, v3;
        float brt, fragRatio = 1, fragRatioT;
        float itpS, itpST, itpE, itpET, weight;
        PVector ctr, nrml;
        boolean hasChildren;
        int itvr, passedItvr;

        Fct(int lv, Vtx v1, Vtx v2, Vtx v3) {
            this.lv = lv;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;

            ctr = new PVector();

            weight = map(constrain(lv, 0, 4), 0, 4, 2.5, 1);

            itvr = round(random(60, 120));

            itpST = 1;
            itpET = 1;
        }

        void splitUp() {
            hasChildren = true;
            Vtx v4 = new Vtx(lv + 1, v1, v2);
            Vtx v5 = new Vtx(lv + 1, v2, v3);
            Vtx v6 = new Vtx(lv + 1, v3, v1);

            Fct cFct1 = new Fct(lv + 1, v1, v4, v6);
            Fct cFct2 = new Fct(lv + 1, v4, v2, v5);
            Fct cFct3 = new Fct(lv + 1, v6, v5, v3);
            Fct cFct4 = new Fct(lv + 1, v5, v4, v6);

            fctList.add(cFct1);
            fctList.add(cFct2);
            fctList.add(cFct3);
            fctList.add(cFct4);
        }

        void update() {
            v1.update();
            v2.update();
            v3.update();
            ctr.set(
                (v1.pos.x + v2.pos.x + v3.pos.x) / 3,
                (v1.pos.y + v2.pos.y + v3.pos.y) / 3,
                (v1.pos.z + v2.pos.z + v3.pos.z) / 3
            );

            nrml = PVector.sub(v1.pos, v2.pos).cross(PVector.sub(v3.pos, v2.pos));
            brt = map(abs(HALF_PI - PVector.angleBetween(nrml, ctr)), 0, HALF_PI, 0, 255);

            if (showTri) fragRatioT = constrain((noise(ctr.x * .005 + frameCount * .01,
                ctr.y * .005 - frameCount * .01,
                ctr.z * .005 + frameCount * .01
            ) - .5) * 2.5 + fragMid, 0, 1);
            else fragRatioT = 1;
            fragRatio = lerp(fragRatio, fragRatioT, .1);

            if (abs(itpST - itpS) < .005) itpS = itpST;
            else itpS = lerp(itpS, itpST, .25);
            if (abs(itpET - itpE) < .005) itpE = itpET;
            else itpE = lerp(itpE, itpET, .125);

            if (passedItvr < itvr) {
                passedItvr++;
            } else {
                passedItvr = 0;
                if (showEdge) {
                    itpS = 0;
                    itpE = 0;
                    itpST = 1;
                    itpET = 1;
                }
            }
        }

        void displayTri(PGraphics pg) {
            pg.fill(brt);
            pg.vertex(lerp(v1.pos.x, ctr.x, fragRatio), lerp(v1.pos.y, ctr.y, fragRatio), lerp(v1.pos.z, ctr.z, fragRatio));
            pg.vertex(lerp(v2.pos.x, ctr.x, fragRatio), lerp(v2.pos.y, ctr.y, fragRatio), lerp(v2.pos.z, ctr.z, fragRatio));
            pg.vertex(lerp(v3.pos.x, ctr.x, fragRatio), lerp(v3.pos.y, ctr.y, fragRatio), lerp(v3.pos.z, ctr.z, fragRatio));
        }

        void displayEdge(PGraphics pg) {
            pg.strokeWeight(weight);
            drawLine(pg, v1, v2, itpS, itpE);
            drawLine(pg, v2, v3, itpS, itpE);
            drawLine(pg, v3, v1, itpS, itpE);
        }

        void drawLine(PGraphics pg, Vtx v1, Vtx v2, float itpS, float itpE) {
            pg.line(
                lerp(v1.pos.x, v2.pos.x, itpS),
                lerp(v1.pos.y, v2.pos.y, itpS),
                lerp(v1.pos.z, v2.pos.z, itpS),
                lerp(v1.pos.x, v2.pos.x, itpE),
                lerp(v1.pos.y, v2.pos.y, itpE),
                lerp(v1.pos.z, v2.pos.z, itpE)
            );
        }
    }

    void initRootVts() {

        rootVts = new Vtx[iCSRows][];
        rootVts[0] = new Vtx[1];
        rootVts[1] = new Vtx[iCSCols];
        rootVts[2] = new Vtx[iCSCols];
        rootVts[3] = new Vtx[1];

        for (int i = 0; i < rootVts.length; i++) {
            for (int j = 0; j < rootVts[i].length; j++) {
                float phi = (j + i * .5) * TWO_PI /float(rootVts[i].length);
                float theta;//=i*PI/float(rootVts.length-1);
                if (i == 0) theta = 0;
                else if (i == 1) theta = HALF_PI - atan(.5);
                else if (i == 2) theta = HALF_PI + atan(.5);
                else theta = PI;
                float r = 300;

                rootVts[i][j] = new Vtx(0, theta, phi, r);
            }
        }
    }

    class Vtx {

        PVector pos, tgt, ofst;
        float theta, phi, r;
        int lv;

        Vtx(int lv, Vtx v1, Vtx v2) {

            this.lv = lv;

            if (abs(v1.phi - v2.phi) > PI) {
                this.phi = lerp(v1.phi, v2.phi + TWO_PI, .5);
            } else {
                this.phi = lerp(v1.phi, v2.phi, .5);
            }
            this.theta = lerp(v1.theta, v2.theta, .5);

            if (v1.theta == 0 || v1.theta == PI) this.phi = v2.phi;
            if (v2.theta == 0 || v2.theta == PI) this.phi = v1.phi;

            this.r = lerp(v1.r, v2.r, .5);

            tgt = new PVector(sin(theta) * cos(phi) * r, cos(theta) * r, sin(theta) * sin(phi) * r);
            pos = new PVector(lerp(v1.pos.x, v2.pos.x, .5), lerp(v1.pos.y, v2.pos.y, .5), lerp(v1.pos.z, v2.pos.z, .5));
            ofst = new PVector();
        }

        Vtx(int lv, float theta, float phi, float r) {

            this.lv = lv;
            this.theta = theta;
            this.phi = phi;
            this.r = r;

            tgt = new PVector(sin(theta) * cos(phi) * r, cos(theta) * r, sin(theta) * sin(phi) * r);
            pos = new PVector(tgt.x, tgt.y, tgt.z);
            ofst = new PVector();
        }

        void update() {
            updatePos();
        }

        void updatePos() {
            float thetaOfst = (noise(gF + tgt.x * .0025 + thetaF,
                -gF + tgt.y * .0025 + thetaF,
                gF + tgt.z * .0025 + thetaF
            ) - .5) * TWO_PI * 2;
            float phiOfst =
                (noise(-gF + tgt.x * .0025 + phiF, gF + tgt.y * .0025 + phiF, -gF + tgt.z * .0025 + phiF) - .5) * TWO_PI * 2;
            float rOfst = noise(tgt.x * .005 + gF, tgt.y * .005 + gF, tgt.z * .005 + gF) * r * .25;
            ofst.set(sin(thetaOfst) * cos(phiOfst) * rOfst, cos(thetaOfst) * rOfst, sin(thetaOfst) * sin(phiOfst) * rOfst);

            float rDsp = (noise(sin(theta + thetaF) * sin(phi), cos(theta) * cos(phi + phiF)) - .5) * r;
            tgt.set(sin(theta) * cos(phi) * (rDsp + r), cos(theta) * (rDsp + r), sin(theta) * sin(phi) * (rDsp + r));

            pos.set(
                lerp(pos.x, (tgt.x + ofst.x), .125),
                lerp(pos.y, (tgt.y + ofst.y), .125),
                lerp(pos.z, (tgt.z + ofst.z), .125)
            );
        }

        void display() {
            vertex(pos.x, pos.y, pos.z);
        }
    }
}
