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
import static com.symmetrylabs.slstudio.util.Utils.random;
import static processing.core.PApplet.*;
import static processing.core.PConstants.HALF_PI;
import static processing.core.PConstants.PI;


public class RKPattern05 extends P3CubeMapPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    BooleanParameter audioLink = new BooleanParameter("audioLink", false);
    CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
    CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
    CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
    BooleanParameter clrBg = new BooleanParameter("clear bg");
    CompoundParameter speed = new CompoundParameter("speed", .05, .01, .2);
    CompoundParameter scalar = new CompoundParameter("scalar", 5, 1, 10);
    CompoundParameter h1 = new CompoundParameter("hue1", 0, 0, 255);
    CompoundParameter h2 = new CompoundParameter("hue2", 85, 0, 255);
    CompoundParameter h3 = new CompoundParameter("hue3", 170, 0, 255);

    boolean clearBg;

    float rotX, rotXT, rotY, rotYT, rotZ, rotZT;
    PVector lgt1, lgt2, lgt3;
    float hue1, hue1T, hue2, hue2T, hue3, hue3T;
    float vOfstScalar, vOfstScalarT, vOfstSpeed, vOfstSpeedT;
    int c1, c2, c3;

    int mxLv = 2;
    float gF;
    float rF, rdnsF, dF;
    Mesh rootMesh1, rootMesh2;
    Vtx[] rootVts1, rootVts2;

    float[] thetaInit = {
        -atan(sqrt(2)), -atan(sqrt(2)), PI - atan(sqrt(2)), PI - atan(sqrt(2))
    };
    float[] phiInit = {
        HALF_PI * .5f, HALF_PI * 2.5f, HALF_PI * 1.5f, HALF_PI * 3.5f
    };

    PVector rotDir;

    boolean audioLinked;

    float[] pEQBands = new float[16];
    float totalBandMag, avgBandMag;

    public RKPattern05(LX lx) {

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
        addParameter(scalar);
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
            vOfstSpeedT = speed.getValuef();
            vOfstScalarT = scalar.getValuef();
        } else {
            totalBandMag = 0;
            for (int i = 0; i < pEQBands.length; i++) {
                totalBandMag += eq.getBandf(i);
            }
            avgBandMag = totalBandMag / 16;

            rotXT += rotDir.x * (eq.getBandf(0) - pEQBands[0]) * 2;
            rotYT += rotDir.z * (eq.getBandf(7) - pEQBands[7]) * 2;
            rotZT += rotDir.y * eq.getBandf(14) * .1;
            vOfstSpeedT = map(sq(totalBandMag), 0, 256, 0, .02f);
            vOfstScalarT = 5;//map(sq(totalBandMag), 0, 256, 0, 10);

            for (int i = 0; i < pEQBands.length; i++) {
                pEQBands[i] = eq.getBandf(i);
            }
        }

        clearBg = clrBg.getValueb();
        hue1 = h1.getValuef();
        hue2 = h2.getValuef();
        hue3 = h3.getValuef();

        rotX = lerp(rotX, rotXT, .25f);
        rotY = lerp(rotY, rotYT, .25f);
        rotZ = lerp(rotZ, rotZT, .25f);
        vOfstScalar = lerp(vOfstScalar, vOfstScalarT, .25f);
        vOfstSpeed = lerp(vOfstSpeed, vOfstSpeedT, .25f);
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
        rootMesh1.display(pg);
        rootMesh2.display(pg);
        pg.endShape();
    }

    void initRootMesh() {
        gF = random(100);
        rootMesh1 = new Mesh(15, 0);
        rootMesh2 = new Mesh(15, 0);
    }

    void updateMesh() {

        rootMesh1.update(rootVts1, gF);
        rootMesh2.update(rootVts2, gF + 247.121f);
        gF += .005;
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
                float itp = (noise(f) - .5f) / 4 + .5f;
                mids[i].interpolate(outers[i], outers[(i + 1) % outers.length], itp, idx, lv);
                f += .1;
            }
            for (int i = 0; i < struts.length; i++) {
                float itp = (noise(f) - .5f) / 4 + .8f;
                struts[i].interpolate(mids[i], outers[(i + 2) % outers.length], itp, idx, lv);
                f += .1;
            }
            for (int i = 0; i < strutMids.length; i++) {
                float itp = (noise(f) - .5f) / 4 + .5f;
                strutMids[i].interpolate(struts[i], struts[(i + 1) % struts.length], itp, idx, lv);
                f += .1;
            }
            for (int i = 0; i < sStruts.length; i++) {
                float itp = (noise(f) - .5f) / 4 + .5f;
                sStruts[i].interpolate(strutMids[i], outers[(i + 1) % outers.length], itp, idx, lv);
                f += .1;
            }

            if (lv < mxLv) {
                f += .1;
                children[15].update(struts, f);
                for (int i = 0; i < 3; i++) {
                    int j = (i + 1) % 3;

                    Vtx[] g1 = {
                        outers[j], sStruts[i], mids[i]
                    };
                    children[i * 5].update(g1, f);
                    f += .1;

                    Vtx[] g2 = {
                        sStruts[i], struts[i], mids[i]
                    };
                    children[i * 5 + 1].update(g2, f);
                    f += .1;

                    Vtx[] g3 = {
                        sStruts[i], struts[i], struts[j]
                    };
                    children[i * 5 + 2].update(g3, f);
                    f += .1;

                    Vtx[] g4 = {
                        sStruts[i], struts[j], mids[j]
                    };
                    children[i * 5 + 3].update(g4, f);
                    f += .1;

                    Vtx[] g5 = {
                        outers[j], sStruts[i], mids[j]
                    };
                    children[i * 5 + 4].update(g5, f);
                    f += .1;
                }
            }
        }

        void display(PGraphics pg) {
            if (lv == mxLv) {

                pg.beginShape(TRIANGLES);
                for (int i = 0; i < 3; i++) {
                    int j = (i + 1) % 3;
                    drawTri(outers[j], sStruts[i], mids[i], pg);
                    drawTri(sStruts[i], struts[i], mids[i], pg);
                    drawTri(sStruts[i], struts[i], struts[j], pg);
                    drawTri(sStruts[i], struts[j], mids[j], pg);
                    drawTri(outers[j], sStruts[i], mids[j], pg);
                }
                pg.endShape();
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
        dF = random(100);
        rdnsF = random(100);

        rootVts1 = new Vtx[3];
        rootVts2 = new Vtx[3];
        for (int i = 0; i < 3; i++) {
            rootVts1[i] = new Vtx();
            rootVts2[i] = new Vtx();
        }
    }

    void updateRootVts() {
        for (int i = 0; i < 3; i++) {
            float r = 1200;
            float theta = thetaInit[i] + (noise(dF + i * .1f) - .5f) * PI / 3;
            float phi = phiInit[i] + (noise(dF + i * .1f) - .5f) * PI / 3;
            float rdns = i * (TWO_PI / rootVts1.length);//+(noise(rdnsF+i*.1)-.5)*PI/3;
            float x = r * cos(rdns);
            float y = r * sin(rdns);
            //if(j == 0) z = -400;
            //else z = 400;
            rootVts1[i].setPos(x, y, -200, theta, phi);
            rootVts2[i].setPos(y, x, 200, phi, theta);
        }
        rF += .005;
        rdnsF += .005;
    }

    class Vtx {

        PVector pos;
        float theta, phi;

        Vtx() {
            pos = new PVector();
        }

        void setPos(float x, float y, float z, float theta, float phi) {
            pos.set(x, y, z);
            this.theta = theta;
            this.phi = phi;
        }

        void interpolate(Vtx v1, Vtx v2, float itp, int idx, int lv) {

            float itpTheta = lerp(v2.theta, v1.theta, itp);
            float itpPhi = lerp(v2.phi, v1.phi, itp);

            //float ofst = (noise(theta ,phi, frameCount*.2)-.5)*140*(float)(idx)/(lv+1);
            float ofst = (noise(theta * vOfstScalar, phi * vOfstScalar, SLStudio.applet.frameCount * vOfstSpeed) - .5f) * 100 * (float)(idx) / (lv + 1);

            setPos(
                lerp(v2.pos.x, v1.pos.x, itp) + ofst * sin(theta) * cos(phi),
                lerp(v2.pos.y, v1.pos.y, itp) + ofst * sin(theta) * sin(phi),
                lerp(v2.pos.z, v1.pos.z, itp) + ofst * cos(theta),
                itpTheta, itpPhi
            );
        }
    }
}
