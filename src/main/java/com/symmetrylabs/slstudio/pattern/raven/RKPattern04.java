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

import static com.symmetrylabs.util.NoiseUtils.noise;
import static com.symmetrylabs.util.MathUtils.random;
import static processing.core.PApplet.HALF_PI;
import static processing.core.PApplet.TWO_PI;
import static processing.core.PApplet.*;
import static processing.core.PConstants.HSB;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.TRIANGLES;


public class RKPattern04 extends P3CubeMapPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    BooleanParameter audioLink = new BooleanParameter("audioLink", false);
    CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
    CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
    CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
    BooleanParameter clrBg = new BooleanParameter("clear bg");
    CompoundParameter speed = new CompoundParameter("speed", .02, 0, .1);
    CompoundParameter h1 = new CompoundParameter("hue1", 0, 0, 255);
    CompoundParameter h2 = new CompoundParameter("hue2", 85, 0, 255);
    CompoundParameter h3 = new CompoundParameter("hue3", 170, 0, 255);

    float rotX, rotXT, rotY, rotYT, rotZ, rotZT, gFIncre, gFIncreT;
    boolean clearBg;

    Vtx[] rootVts;
    Mesh rootMesh;

    int mxLv = 2;
    float rF, rdnsF, gF;
    PVector lgt1, lgt2, lgt3;
    float hue1, hue1T, hue2, hue2T, hue3, hue3T;
    int c1, c2, c3;
    PVector rotDir;

    boolean audioLinked;

    float[] pEQBands = new float[16];
    float totalBandMag, avgBandMag;

    public RKPattern04(LX lx) {

        super(
            (P3LX) lx,
            new PVector(lx.model.cx, lx.model.cy, lx.model.cz),
            new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange),
            200
        );
        initRootVts();
        initRootMesh();

        lgt1 = new PVector(600, 600, 600);
        lgt2 = new PVector(600, -600, -600);
        lgt3 = new PVector(-600, -600, 600);
        hue1 = hue1T = 0;
        hue2 = hue2T = 85;
        hue3 = hue3T = 170;

        rotDir = PVector.random3D();

        addParameter(audioLink);
        addParameter(rX);
        addParameter(rY);
        addParameter(rZ);
        addParameter(clrBg);
        addParameter(speed);
        addParameter(h1);
        addParameter(h2);
        addParameter(h3);

        addModulator(eq).start();
    }

    public void run(double deltaMs, PGraphics pg) {

        updateParameters();

        updateRootVts();
        updateMesh();

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
            gFIncreT = speed.getValuef();
        } else {
            totalBandMag = 0;
            for (int i = 0; i < pEQBands.length; i++) {
                totalBandMag += eq.getBandf(i);
            }
            avgBandMag = totalBandMag / 16;

            rotXT += rotDir.x * (eq.getBandf(0) - pEQBands[0]) * 2;
            rotYT += rotDir.z * (eq.getBandf(7) - pEQBands[7]) * 2;
            rotZT += rotDir.y * eq.getBandf(14) * .1f;
            gFIncreT = map(sq(totalBandMag), 0, 256, 0, .25f);

            for (int i = 0; i < pEQBands.length; i++) {
                pEQBands[i] = eq.getBandf(i);
            }
        }

        hue1 = h1.getValuef();
        hue2 = h2.getValuef();
        hue3 = h3.getValuef();
        clearBg = clrBg.getValueb();
        rotX = lerp(rotX, rotXT, .25f);
        rotY = lerp(rotY, rotYT, .25f);
        rotZ = lerp(rotZ, rotZT, .25f);
        gFIncre = lerp(gFIncre, gFIncreT, .25f);
        hue1 = lerp(hue1, hue1T, .1f);
        hue2 = lerp(hue2, hue2T, .1f);
        hue3 = lerp(hue3, hue3T, .1f);

        SLStudio.applet.colorMode(HSB);
        c1 = SLStudio.applet.color(hue1, 255, 255);
        c2 = SLStudio.applet.color(hue2, 255, 255);
        c3 = SLStudio.applet.color(hue3, 255, 255);
    }

    void updateCubeMaps() {
        updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
        updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
        updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
        updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
        updateCubeMap(pgD, 0, 0, -.001f, 0, -1, 0, 0, 1, 0);
        updateCubeMap(pgU, 0, 0, -.001f, 0, 1, 0, 0, 1, 0);
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
        if (clearBg) pg.background(0);
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
        rootMesh.display(pg);
        pg.endShape();
    }

    void initRootMesh() {
        gF = random(100);
        rootMesh = new Mesh(15, 0);
    }

    void updateMesh() {

        rootMesh.update(rootVts, gF);
        gF += gFIncre;
    }

    class Mesh {

        Mesh[] children;

        int idx, lv;

        float r1, r2, r3;
        PVector nrml;

        Vtx[] outers, mids, struts, strutMids, sStruts;

        Mesh(int idx, int lv) {

            this.idx = idx;
            this.lv = lv;

            outers = new Vtx[3];
            mids = new Vtx[3];
            struts = new Vtx[3];
            strutMids = new Vtx[3];
            sStruts = new Vtx[3];

            for (int i = 0; i < 3; i++) {
                outers[i] = new Vtx();
                mids[i] = new Vtx();
                struts[i] = new Vtx();
                strutMids[i] = new Vtx();
                sStruts[i] = new Vtx();
            }

            if (lv < mxLv) {
                lv++;
                children = new Mesh[16];
                for (int i = 0; i < children.length; i++) {
                    children[i] = new Mesh(i, lv);
                }
            }
        }

        void update(Vtx[] outers, float f) {
            this.outers = outers;
            for (int i = 0; i < mids.length; i++) {
                float itp = (noise(f) - .5f) / ((float)(lv) + 1)+.5f;
                mids[i].interpolate(outers[i], outers[(i + 1) % outers.length], itp);
                f += gFIncre;
            }
            for (int i = 0; i < struts.length; i++) {
                float itp = (noise(f) - .5f) / ((float)(lv) + 1)+.8f;
                struts[i].protrude(mids[i], outers[(i + 2) % outers.length], outers[(i + 1) % outers.length], itp, lv);
                f += gFIncre;
            }
            for (int i = 0; i < strutMids.length; i++) {
                float itp = (noise(f) - .5f) / ((float)(lv) + 1)+.5f;
                strutMids[i].interpolate(struts[i], struts[(i + 1) % struts.length], itp);
                f += gFIncre;
            }
            for (int i = 0; i < sStruts.length; i++) {
                float itp = (noise(f) - .5f) / ((float)(lv) + 1)+.5f;
                sStruts[i].protrude(strutMids[i], outers[(i + 1) % outers.length], struts[i], itp, lv);
                f += gFIncre;
            }

            if (lv < mxLv) {
                f += gFIncre;
                children[15].update(struts, f);
                for (int i = 0; i < 3; i++) {
                    int j = (i + 1) % 3;

                    Vtx[] g1 = {
                        outers[j], sStruts[i], mids[i]
                    };
                    children[i * 5].update(g1, f);
                    f += gFIncre;

                    Vtx[] g2 = {
                        sStruts[i], struts[i], mids[i]
                    };
                    children[i * 5 + 1].update(g2, f);
                    f += gFIncre;

                    Vtx[] g3 = {
                        sStruts[i], struts[i], struts[j]
                    };
                    children[i * 5 + 2].update(g3, f);
                    f += gFIncre;

                    Vtx[] g4 = {
                        sStruts[i], struts[j], mids[j]
                    };
                    children[i * 5 + 3].update(g4, f);
                    f += gFIncre;

                    Vtx[] g5 = {
                        outers[j], sStruts[i], mids[j]
                    };
                    children[i * 5 + 4].update(g5, f);
                    f += gFIncre;
                }
            }
        }

        void display(PGraphics pg) {
            if (lv == mxLv) {
                for (int i = 0; i < 3; i++) {
                    int j = (i + 1) % 3;
                    drawTri(outers[j], sStruts[i], mids[i], pg);
                    drawTri(sStruts[i], struts[i], mids[i], pg);
                    drawTri(sStruts[i], struts[i], struts[j], pg);
                    drawTri(sStruts[i], struts[j], mids[j], pg);
                    drawTri(outers[j], sStruts[i], mids[j], pg);
                }
            }

            if (lv < mxLv) {
                for (int i = 0; i < children.length; i++) {
                    children[i].display(pg);
                }
            }
        }

        void drawTri(Vtx v1, Vtx v2, Vtx v3, PGraphics pg) {

            nrml = PVector.sub(v1.pos, v2.pos).cross(PVector.sub(v3.pos, v2.pos));
            r1 = map(abs(HALF_PI - PVector.angleBetween(nrml, lgt1)), 0, HALF_PI, 0, 1.5f);
            r2 = map(abs(HALF_PI - PVector.angleBetween(nrml, lgt2)), 0, HALF_PI, 0, 1.5f);
            r3 = map(abs(HALF_PI - PVector.angleBetween(nrml, lgt3)), 0, HALF_PI, 0, 1.5f);
            pg.fill(
                constrain(r1 * SLStudio.applet.red(c1) + r2 * SLStudio.applet.red(c2) + r3 * SLStudio.applet.red(c3), 0, 255),
                constrain(r1 * SLStudio.applet.green(c1) + r2 * SLStudio.applet.green(c2) + r3 * SLStudio.applet.green(c3), 0, 255),
                constrain(r1 * SLStudio.applet.blue(c1) + r2 * SLStudio.applet.blue(c2) + r3 * SLStudio.applet.blue(c3), 0, 255)
            );

            pg.vertex(v1.pos.x, v1.pos.y, v1.pos.z);
            pg.vertex(v2.pos.x, v2.pos.y, v2.pos.z);
            pg.vertex(v3.pos.x, v3.pos.y, v3.pos.z);
        }
    }

    void initRootVts() {

        rF = random(100);
        rdnsF = random(100);

        rootVts = new Vtx[3];
        for (int i = 0; i < rootVts.length; i++) {
            rootVts[i] = new Vtx();
        }
    }

    void updateRootVts() {
        for (int i = 0; i < rootVts.length; i++) {
            float r = 1200 + (noise(rF + i * .1f) - 0.5f) * 1200;
            float rdns = i * (TWO_PI / rootVts.length) + (noise(rdnsF + i * .1f) - .5f) * PI / 3;
            float x = r * cos(rdns);
            float y = 300;
            float z = r * sin(rdns);
            rootVts[i].setPos(x, y, z);
        }
        rF += gFIncre;
        rdnsF += gFIncre;
    }

    class Vtx {

        PVector pos;

        Vtx() {
            pos = new PVector();
        }

        void setPos(float x, float y, float z) {
            pos.set(x, y, z);
        }

        void interpolate(Vtx v1, Vtx v2, float itp) {
            setPos(
                lerp(v2.pos.x, v1.pos.x, itp),
                lerp(v2.pos.y, v1.pos.y, itp),
                lerp(v2.pos.z, v1.pos.z, itp)
            );
        }

        void protrude(Vtx mid, Vtx midOp, Vtx outer, float itp, int lv) {
            PVector strut0 = new PVector(
                lerp(midOp.pos.x, mid.pos.x, itp),
                lerp(midOp.pos.y, mid.pos.y, itp),
                lerp(midOp.pos.z, mid.pos.z, itp)
            );
            PVector m = new PVector(
                lerp(midOp.pos.x, mid.pos.x, .5f),
                lerp(midOp.pos.y, mid.pos.y, .5f),
                lerp(midOp.pos.z, mid.pos.z, .5f)
            );

            float d1 = dist(midOp.pos.x, midOp.pos.y, midOp.pos.z, mid.pos.x, mid.pos.y, mid.pos.z);
            float d2 = dist(strut0.x, strut0.y, strut0.z, mid.pos.x, mid.pos.y, mid.pos.z) -
                dist(m.x, m.y, m.z, mid.pos.x, mid.pos.y, mid.pos.z);

            float mag = sqrt(sq(d1 * .5f) - sq(d2)) / ((float)(lv) / 3 + 1);

            PVector ofst = PVector.sub(outer.pos, mid.pos).cross(PVector.sub(midOp.pos, mid.pos));
            ofst.normalize();
            ofst.mult(mag);

            setPos(strut0.x + ofst.x, strut0.y + ofst.y, strut0.z + ofst.z);
        }
    }
}
