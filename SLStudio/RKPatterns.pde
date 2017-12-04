public class RKPattern01 extends P3CubeMapPattern {

  private LXAudioInput audioInput = lx.engine.audio.getInput();
  private GraphicMeter eq = new GraphicMeter(audioInput);

  BooleanParameter audioLink = new BooleanParameter("audioLink", false);
  CompoundParameter rX = new CompoundParameter("rX", 0, -PI, PI);
  CompoundParameter rY = new CompoundParameter("rY", 0, -PI, PI);
  CompoundParameter rZ = new CompoundParameter("rZ", 0, -PI, PI);
  CompoundParameter amt = new CompoundParameter("amt", 20, 1, 25);
  CompoundParameter speed = new CompoundParameter("speed", PI, -TWO_PI*2, TWO_PI*2);
  CompoundParameter dsp = new CompoundParameter("dsp", HALF_PI, 0, PI);
  CompoundParameter nDsp = new CompoundParameter("nDsp", 1, .125, 2.5);

  int faceRes = 200;

  int ringRes = 40, ringAmt = 20, pRingAmt = 20;
  float l1 = 600, l2 = 600, l3 = 600;
  float gTheta, gThetaSpacing, gWeightScalar;
  float rotX, rotXT, rotY, rotYT, rotZ, rotZT, dspmt, dspmtT, nDspmt, nDspmtT, thetaSpeed, thetaSpeedT;
  ArrayList <Ring> testRings;
  PVector rotDir;

  boolean audioLinked;

  float [] pEQBands = new float[16];
  float totalBandMag, avgBandMag;

  public RKPattern01(LX lx) {

    super((P3LX) lx, new PVector(lx.model.cx, lx.model.cy, lx.model.cz), new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange), 200);

    rotDir = PVector.random3D();

    testRings = new ArrayList<Ring>();
    for (int i=0; i<ringAmt; i++) {
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

  void run(double deltaMs, PGraphics pg) {

    updateParameters();
    replenish();

    for (int i=0; i<testRings.size(); i++) {
      Ring testRing = testRings.get(i);
      testRing.update(i);
    }
    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, faceRes);
    pg.image(pgR, faceRes*2, faceRes);
    pg.image(pgD, faceRes, 0);
    pg.image(pgU, faceRes, faceRes*2);
    pg.image(pgF, faceRes, faceRes);
    pg.image(pgB, faceRes*3, faceRes);
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
      for (int i=0; i<pEQBands.length; i++) {
        totalBandMag += eq.getBandf(i);
      }
      avgBandMag = totalBandMag/16;

      rotXT += rotDir.x*(eq.getBandf(0)-pEQBands[0])*2;
      rotYT += rotDir.z*(eq.getBandf(7)-pEQBands[7])*2;
      rotZT += rotDir.y*eq.getBandf(14)*.1;
      thetaSpeedT = map(sq(totalBandMag), 0, 256, 0, TWO_PI*12);
      dspmtT = 0;
      nDspmtT = 0;

      for (int i=0; i<pEQBands.length; i++) {
        pEQBands[i] = eq.getBandf(i);
      }
    }
    ringAmt = round(amt.getValuef());

    rotX = lerp(rotX, rotXT, .25);
    rotY = lerp(rotY, rotYT, .25);
    rotZ = lerp(rotZ, rotZT, .25);

    thetaSpeed = lerp(thetaSpeed, thetaSpeedT, .25);
    dspmt = lerp(dspmt, dspmtT, .1);
    nDspmt = lerp(nDspmt, nDspmtT, .01);

    gTheta += thetaSpeed/720;
    if (gTheta>PI) gTheta -= PI;
    else if (gTheta<0) gTheta += PI;
    gThetaSpacing = lerp(gThetaSpacing, PI/testRings.size(), .1);
    gWeightScalar = map(ringAmt, 1, 25, 4, 1);
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
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
    updateCubeMap(pgD, 0, 0, -.001, 0, -1, 0, 0, 1, 0);
    updateCubeMap(pgU, 0, 0, -.001, 0, 1, 0, 0, 1, 0);
  }

  void drawScene(PGraphics pg) {
    for (int i=0; i<testRings.size(); i++) {
      Ring testRing = testRings.get(i);
      testRing.display(pg);
    }
  }

  void replenish() {
    if (pRingAmt != ringAmt) {
      if (ringAmt>pRingAmt) {
        for (int i=0; i<ringAmt-pRingAmt; i++) {
          Ring testRing = new Ring(ringRes, l1, l2, l3);
          testRings.add(0, testRing);
        }
      }
      if (ringAmt<pRingAmt) {
        for (int i=0; i<pRingAmt-ringAmt; i++) {
          int j = testRings.size()-1-i;
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
    Vtx [] vts;

    Ring(int amt, float l1, float l2, float l3) {

      this.amt = amt;
      this.l1 = l1;
      this.l2 = l2;
      this.l3 = l3;

      theta = 0;

      vts = new Vtx[this.amt];
      for (int i=0; i<vts.length; i++) {
        float initPhi = i*TWO_PI/amt;
        vts[i] = new Vtx(i, this.theta, initPhi, this.l1, this.l2, this.l3);
      }
    }

    void update(int idx) {
      theta = gTheta+idx*gThetaSpacing;
      if (theta>PI) theta -= PI;
      else if (theta<0) theta += PI;

      weight = sin(theta)*4*gWeightScalar;

      for (int i=0; i<vts.length; i++) {
        vts[i].update(theta);
      }
    }

    void display(PGraphics pg) {
      pg.noFill();
      pg.stroke(255);
      pg.strokeWeight(weight);
      pg.beginShape();
      for (int i=0; i<vts.length; i++) {
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
        thetaOfstRange = sin(theta)*dspmt;
        thetaOfst = (noise(sin(phi)*cos(phi)*nDspmt+frameCount*.005, this.thetaBase*nDspmt*.25-frameCount*.005)-.5)*thetaOfstRange;
      } else {
        thetaOfstRange = sin(theta);
        int bandIdx = floor(map(idx, 0, ringRes, 0, 16));
        thetaOfst = avgBandMag - eq.getBandf(bandIdx);
      }
      theta = this.thetaBase + thetaOfst;
      pos.set(
        sin(theta)*cos(phi)*.5*l1, 
        cos(theta)*.5*l2, 
        sin(theta)*sin(phi)*.5*l3
        );
    }
  }
}

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

  int faceRes = 200;
  ArrayList <Arc> arcs;
  float f, fT, fTIncre;
  float l1 = 600, l2 = 600, l3 = 600;
  float rotX, rotXT, rotY, rotYT, rotZ, rotZT, dspmt, dspmtT;
  float gWeight = 2, gWeightT = 2;
  boolean autoNoise, showEdge;
  int switchCount;
  PVector rotDir;

  boolean audioLinked;

  float [] pEQBands = new float[16];
  float totalBandMag, avgBandMag;

  public RKPattern02(LX lx) {

    super((P3LX) lx, new PVector(lx.model.cx, lx.model.cy, lx.model.cz), new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange), 200);

    arcs = new ArrayList<Arc>();
    Arc parentArc = new Arc(0, 0, 12, 0, PI, 0, TWO_PI);
    arcs.add(parentArc);

    for (int i=0; i<8; i++) {
      for (int j=arcs.size()-1; j>-1; j--) {
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

  void run(double deltaMs, PGraphics pg) {

    updateParameters();

    if (frameCount%90==0 && !autoNoise && !audioLinked) {
      switchCount = (switchCount+1)%3;
      if (switchCount == 0) {
        resetArcs();
        for (int j=0; j<8; j++) {
          for (int i=arcs.size()-1; i>-1; i--) {
            Arc arc = arcs.get(i);
            if (!arc.hasChildren) arc.splitUp();
          }
        }
        for (int i=arcs.size()-1; i>-1; i--) {
          Arc arc = arcs.get(i);
          arc.reset();
        }
      } else {
        resetArcs();
        for (int j=0; j<500; j++) {
          Arc arc = arcs.get(floor(random(arcs.size())));
          if (!arc.hasChildren) arc.splitUp();
        }
        for (int i=arcs.size()-1; i>-1; i--) {
          Arc arc = arcs.get(i);
          arc.randomize();
        }
      }
    }

    for (int i=0; i<arcs.size(); i++) {
      Arc arc = arcs.get(i);
      if (autoNoise || audioLinked) {
        float scalar = map(arc.lv, 0, 8, dspmt, dspmt*1.5);
        arc.splitRatio = .5+(noise(arc.idx*.1+f)-.5)*scalar;
        arc.fragRatioS = (noise(arc.idx*.1+f*2+2.46)-.5)*1.2+.5;
        arc.fragRatioE = (noise(arc.idx*.1+f*2+11.27)-.5)*1.2+.5;
      }
      arc.update();
    }
    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, faceRes);
    pg.image(pgR, faceRes*2, faceRes);
    pg.image(pgD, faceRes, 0);
    pg.image(pgU, faceRes, faceRes*2);
    pg.image(pgF, faceRes, faceRes);
    pg.image(pgB, faceRes*3, faceRes);
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
      for (int i=0; i<pEQBands.length; i++) {
        totalBandMag += eq.getBandf(i);
      }
      avgBandMag = totalBandMag/16;

      rotXT += rotDir.x*(eq.getBandf(0)-pEQBands[0])*2;
      rotYT += rotDir.z*(eq.getBandf(7)-pEQBands[7])*2;
      rotZT += rotDir.y*eq.getBandf(14)*.1;

      fTIncre = map(sq(totalBandMag), 0, 256, 0, 3);
      dspmtT = map(sq(totalBandMag), 0, 256, 0, 3);

      for (int i=0; i<pEQBands.length; i++) {
        pEQBands[i] = eq.getBandf(i);
      }
    }

    rotX = lerp(rotX, rotXT, .25);
    rotY = lerp(rotY, rotYT, .25);
    rotZ = lerp(rotZ, rotZT, .25);
    dspmt = lerp(dspmt, dspmtT, .25);

    if (showEdge) gWeightT = 2;
    else gWeightT = 0;
    if (abs(gWeightT-gWeight)<.005) gWeight = gWeightT;
    else gWeight = lerp(gWeight, gWeightT, .1);

    fT += fTIncre*.1;
    f = lerp(f, fT, .25);
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
    pg.beginDraw();
    pg.background(0);
    pg.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    pg.frustum(-10, 10, -10, 10, 10, 1000);
    pg.rotateX(rotX+HALF_PI);
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
    updateCubeMap(pgD, 0, 0, -.001, 0, -1, 0, 0, 1, 0);
    updateCubeMap(pgU, 0, 0, -.001, 0, 1, 0, 0, 1, 0);
  }

  void drawScene(PGraphics pg) {
    for (int i=0; i<arcs.size(); i++) {
      Arc arc = arcs.get(i);
      if (arc.hasChildren) arc.displayEdge(pg);
      else arc.displayFragment(pg);
    }
    pg.noFill();
    pg.stroke(255);
    pg.strokeWeight(gWeight);
    pg.beginShape();
    for (int i=0; i<16; i++) {
      float phi = 0;
      float theta = i*PI/15;
      pg.vertex(
        sin(theta)*cos(phi)*l1*.5, 
        cos(theta)*l2*.5, 
        sin(theta)*sin(phi)*l3*.5
        );
    }
    pg.endShape();
  }

  void resetArcs() {
    for (int i=arcs.size()-1; i>-1; i--) {
      arcs.remove(i);
    }
    Arc parentArc = new Arc(0, 0, 12, 0, PI, 0, TWO_PI);
    arcs.add(parentArc);
  }

  class Arc {

    Arc cArc1, cArc2;
    boolean hasChildren;
    int segAmt, lv, idx, dir;

    PVector [] vts;
    PVector [][] fragVts;

    float splitRatio, splitRatioT, splitRatioStp;
    float sTheta, eTheta, sPhi, ePhi, sThetaFrg, eThetaFrg, sPhiFrg, ePhiFrg;
    float l1 = 600, l2 = 600, l3 = 600;
    float fragRatioS = 0, fragRatioST = .4, fragRatioE = 1, fragRatioET = .6, fragRatioSStp, fragRatioEStp;

    Arc(int lv, int idx, int segAmt, float sTheta, float eTheta, float sPhi, float ePhi) {

      this.lv = lv;
      this.idx = idx;
      this.segAmt = segAmt;
      this.sTheta = sTheta;
      this.eTheta = eTheta;
      this.sPhi = sPhi;
      this.ePhi = ePhi;

      splitRatioStp = random(.03125, .25);
      fragRatioSStp = random(.0625, .25);
      fragRatioEStp = fragRatioSStp;

      fragVts = new PVector[segAmt/2][segAmt/2];
      for (int i=0; i<segAmt/2; i++) {
        for (int j=0; j<segAmt/2; j++) {
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
        if (lv%2==0) {
          for (int i=0; i<segAmt; i++) {
            float phi = sPhi + i*(ePhi-sPhi)/(vts.length-1);
            float theta = lerp(sTheta, eTheta, splitRatio);

            vts[i].set(
              sin(theta)*cos(phi)*l1*.5, 
              cos(theta)*l2*.5, 
              sin(theta)*sin(phi)*l3*.5
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
          for (int i=0; i<segAmt; i++) {
            float phi = lerp(sPhi, ePhi, splitRatio);
            float theta = sTheta + i*(eTheta-sTheta)/(vts.length-1);

            vts[i].set(
              sin(theta)*cos(phi)*l1*.5, 
              cos(theta)*l2*.5, 
              sin(theta)*sin(phi)*l3*.5
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
        for (int i=0; i<segAmt/2; i++) {
          for (int j=0; j<segAmt/2; j++) {
            float theta = sThetaFrg + i*(eThetaFrg-sThetaFrg)/(segAmt/2-1);
            float phi = sPhiFrg + j*(ePhiFrg-sPhiFrg)/(segAmt/2-1);

            fragVts[i][j].set(
              sin(theta)*cos(phi)*l1*.5, 
              cos(theta)*l2*.5, 
              sin(theta)*sin(phi)*l3*.5
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
      fragRatioEStp = fragRatioSStp*.5;
    }

    void reset() {
      splitRatioT = .5;
      fragRatioS = .5;
      fragRatioST = 0;
      fragRatioE = .5;
      fragRatioET = 1;
      fragRatioEStp = fragRatioSStp;
    }

    void splitUp() {

      hasChildren = true;
      splitRatio = random(1);
      splitRatioT = .5;

      vts = new PVector[segAmt];

      if (lv%2==0) {
        cArc1 = new Arc(lv+1, arcs.size(), segAmt, sTheta, lerp(sTheta, eTheta, splitRatio), sPhi, ePhi);
        arcs.add(cArc1);
        cArc2 = new Arc(lv+1, arcs.size(), segAmt, lerp(sTheta, eTheta, splitRatio), eTheta, sPhi, ePhi);
        arcs.add(cArc2);

        for (int i=0; i<segAmt; i++) {
          float phi = sPhi + i*(ePhi-sPhi)/(vts.length-1);
          float theta = lerp(sTheta, eTheta, splitRatio);

          vts[i] = new PVector(
            sin(theta)*cos(phi)*l1*.5, 
            cos(theta)*l2*.5, 
            sin(theta)*sin(phi)*l3*.5
            );
        }
      } else {
        cArc1 = new Arc(lv+1, arcs.size(), segAmt, sTheta, eTheta, sPhi, lerp(sPhi, ePhi, splitRatio));
        arcs.add(cArc1);
        cArc2 = new Arc(lv+1, arcs.size(), segAmt, sTheta, eTheta, lerp(sPhi, ePhi, splitRatio), ePhi);
        arcs.add(cArc2);

        for (int i=0; i<segAmt; i++) {
          float phi = lerp(sPhi, ePhi, splitRatio);
          float theta = sTheta + i*(eTheta-sTheta)/(vts.length-1);

          vts[i] = new PVector(
            sin(theta)*cos(phi)*l1*.5, 
            cos(theta)*l2*.5, 
            sin(theta)*sin(phi)*l3*.5
            );
        }
      }
    }

    void displayEdge(PGraphics pg) {
      pg.noFill();
      pg.stroke(255);
      pg.strokeWeight(gWeight);
      pg.beginShape();
      for (int i=0; i<segAmt; i++) {
        pg.vertex(vts[i].x, vts[i].y, vts[i].z);
      }
      pg.endShape();
    }

    void displayFragment(PGraphics pg) {
      pg.noStroke();
      pg.fill(255);
      pg.beginShape(TRIANGLES);
      for (int i=0; i<segAmt/2-1; i++) {
        for (int j=0; j<segAmt/2-1; j++) {
          pg.vertex(fragVts[i][j].x, fragVts[i][j].y, fragVts[i][j].z);
          pg.vertex(fragVts[i][j+1].x, fragVts[i][j+1].y, fragVts[i][j+1].z);
          pg.vertex(fragVts[i+1][j+1].x, fragVts[i+1][j+1].y, fragVts[i+1][j+1].z);
          pg.vertex(fragVts[i][j].x, fragVts[i][j].y, fragVts[i][j].z);
          pg.vertex(fragVts[i+1][j].x, fragVts[i+1][j].y, fragVts[i+1][j].z);
          pg.vertex(fragVts[i+1][j+1].x, fragVts[i+1][j+1].y, fragVts[i+1][j+1].z);
        }
      }
      pg.endShape(CLOSE);
    }
  }
}

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

  int faceRes = 200;

  Vtx [][] rootVts;
  int iCSCols = 5, iCSRows = 4;
  float gF, thetaF, phiF, fragMid;
  float [] pEQBands = new float[16];
  ArrayList <Fct> fctList;
  boolean audioLinked, avgSplit, pAvgSplit, showTri, showEdge;
  PVector rotDir;

  float rotX, rotXT, rotY, rotYT, rotZ, rotZT, gFIncre, gFIncreT;

  public RKPattern03(LX lx) {

    super((P3LX) lx, new PVector(lx.model.cx, lx.model.cy, lx.model.cz), new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange), 200);

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

    for (int i=0; i<fctList.size(); i++) {
      Fct fct = fctList.get(i);
      fct.update();
    }

    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, faceRes);
    pg.image(pgR, faceRes*2, faceRes);
    pg.image(pgD, faceRes, 0);
    pg.image(pgU, faceRes, faceRes*2);
    pg.image(pgF, faceRes, faceRes);
    pg.image(pgB, faceRes*3, faceRes);
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
      for (int i=0; i<pEQBands.length; i++) {
        totalMag += eq.getBandf(i);
      }

      rotXT += rotDir.x*(eq.getBandf(0)-pEQBands[0])*2;
      rotYT += rotDir.z*(eq.getBandf(7)-pEQBands[7])*2;
      rotZT += rotDir.y*eq.getBandf(14)*.1;
      gFIncreT = map(sq(totalMag), 0, 256, 0, .3);
      fragMid = map(totalMag, 0, 16, 1, 0);

      for (int i=0; i<pEQBands.length; i++) {
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

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
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
    for (int i=fctList.size()-1; i>-1; i--) {
      Fct fct = fctList.get(i);
      if (!fct.hasChildren) fct.displayTri(pg);
    }
    pg.endShape();
    pg.stroke(255);
    for (int i=fctList.size()-1; i>-1; i--) {
      Fct fct = fctList.get(i);
      if (!fct.hasChildren) fct.displayEdge(pg);
    }
  }

  void initFctList() {

    for (int j=0; j<rootVts[1].length; j++) {
      int k = (j+1)%rootVts[1].length;
      Fct fct = new Fct(0, rootVts[0][0], rootVts[1][j], rootVts[1][k]);
      fctList.add(fct);
    }

    for (int j=0; j<rootVts[2].length; j++) {
      int k = (j+1)%rootVts[2].length;
      Fct fct = new Fct(0, rootVts[3][0], rootVts[2][j], rootVts[2][k]);
      fctList.add(fct);
    }

    for (int j=0; j<rootVts[1].length; j++) {
      int k = (j+1)%rootVts[1].length;
      Fct fct1 = new Fct(0, rootVts[1][j], rootVts[1][k], rootVts[2][j]);
      fctList.add(fct1);
      Fct fct2 = new Fct(0, rootVts[2][j], rootVts[1][k], rootVts[2][k]);
      fctList.add(fct2);
    }
  }

  void resetFctList() {
    for (int i=fctList.size()-1; i>-1; i--) {
      fctList.remove(i);
    }
    initFctList();
  }

  void avgSplitUp() {
    for (int j=0; j<2; j++) {
      for (int i=fctList.size()-1; i>-1; i--) {
        Fct fct = fctList.get(i);
        if (!fct.hasChildren && fct.lv<3) fct.splitUp();
      }
    }
  }

  void randomSplitUp(int times) {
    for (int i=0; i<times; ) {
      int randInt = floor(random(fctList.size()));
      Fct randFct = fctList.get(randInt);
      if (randFct.hasChildren || randFct.lv>3) {
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
      Vtx v4 = new Vtx(lv+1, v1, v2);
      Vtx v5 = new Vtx(lv+1, v2, v3);
      Vtx v6 = new Vtx(lv+1, v3, v1);

      Fct cFct1 = new Fct(lv+1, v1, v4, v6);
      Fct cFct2 = new Fct(lv+1, v4, v2, v5);
      Fct cFct3 = new Fct(lv+1, v6, v5, v3);
      Fct cFct4 = new Fct(lv+1, v5, v4, v6);

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
        (v1.pos.x + v2.pos.x + v3.pos.x)/3, 
        (v1.pos.y + v2.pos.y + v3.pos.y)/3, 
        (v1.pos.z + v2.pos.z + v3.pos.z)/3
        );

      nrml = PVector.sub(v1.pos, v2.pos).cross(PVector.sub(v3.pos, v2.pos));
      brt = map(abs(HALF_PI-PVector.angleBetween(nrml, ctr)), 0, HALF_PI, 0, 255);

      if (showTri) fragRatioT = constrain((noise(ctr.x*.005+frameCount*.01, ctr.y*.005-frameCount*.01, ctr.z*.005+frameCount*.01)-.5)*2.5+fragMid, 0, 1);
      else fragRatioT = 1;
      fragRatio = lerp(fragRatio, fragRatioT, .1);

      if (abs(itpST-itpS)<.005) itpS = itpST;
      else itpS = lerp(itpS, itpST, .25);
      if (abs(itpET-itpE)<.005) itpE = itpET;
      else itpE = lerp(itpE, itpET, .125);

      if (passedItvr<itvr) {
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

    for (int i=0; i<rootVts.length; i++) {
      for (int j=0; j<rootVts[i].length; j++) {
        float phi = (j+i*.5)*TWO_PI/float(rootVts[i].length);
        float theta;//=i*PI/float(rootVts.length-1);
        if (i==0)theta = 0;
        else if (i==1)theta = HALF_PI-atan(.5);
        else if (i==2)theta = HALF_PI+atan(.5);
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

      if (abs(v1.phi-v2.phi)>PI) {
        this.phi = lerp(v1.phi, v2.phi+TWO_PI, .5);
      } else {
        this.phi = lerp(v1.phi, v2.phi, .5);
      }
      this.theta = lerp(v1.theta, v2.theta, .5);

      if (v1.theta == 0 || v1.theta == PI)this.phi = v2.phi;
      if (v2.theta == 0 || v2.theta == PI)this.phi = v1.phi;

      this.r = lerp(v1.r, v2.r, .5);

      tgt = new PVector(sin(theta)*cos(phi)*r, cos(theta)*r, sin(theta)*sin(phi)*r);
      pos = new PVector(lerp(v1.pos.x, v2.pos.x, .5), lerp(v1.pos.y, v2.pos.y, .5), lerp(v1.pos.z, v2.pos.z, .5));
      ofst = new PVector();
    }

    Vtx(int lv, float theta, float phi, float r) {

      this.lv = lv;
      this.theta = theta;
      this.phi = phi;
      this.r = r;

      tgt = new PVector(sin(theta)*cos(phi)*r, cos(theta)*r, sin(theta)*sin(phi)*r);
      pos = new PVector(tgt.x, tgt.y, tgt.z);
      ofst = new PVector();
    }

    void update() {
      updatePos();
    }

    void updatePos() {
      float thetaOfst = (noise(gF+tgt.x*.0025+thetaF, -gF+tgt.y*.0025+thetaF, gF+tgt.z*.0025+thetaF)-.5)*TWO_PI*2;
      float phiOfst = (noise(-gF+tgt.x*.0025+phiF, gF+tgt.y*.0025+phiF, -gF+tgt.z*.0025+phiF)-.5)*TWO_PI*2;
      float rOfst = noise(tgt.x*.005+gF, tgt.y*.005+gF, tgt.z*.005+gF)*r*.25;
      ofst.set(sin(thetaOfst)*cos(phiOfst)*rOfst, cos(thetaOfst)*rOfst, sin(thetaOfst)*sin(phiOfst)*rOfst);

      float rDsp = (noise(sin(theta+thetaF)*sin(phi), cos(theta)*cos(phi+phiF))-.5)*r;
      tgt.set(sin(theta)*cos(phi)*(rDsp+r), cos(theta)*(rDsp+r), sin(theta)*sin(phi)*(rDsp+r));

      pos.set(lerp(pos.x, (tgt.x+ofst.x), .125), lerp(pos.y, (tgt.y+ofst.y), .125), lerp(pos.z, (tgt.z+ofst.z), .125));
    }

    void display() {
      vertex(pos.x, pos.y, pos.z);
    }
  }
}


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

  int faceRes = 200;

  float rotX, rotXT, rotY, rotYT, rotZ, rotZT, gFIncre, gFIncreT;
  boolean clearBg;

  Vtx [] rootVts;
  Mesh rootMesh;

  int mxLv = 2;
  float rF, rdnsF, gF;
  PVector lgt1, lgt2, lgt3;
  float hue1, hue1T, hue2, hue2T, hue3, hue3T;
  color c1, c2, c3;
  PVector rotDir;

  boolean audioLinked;

  float [] pEQBands = new float[16];
  float totalBandMag, avgBandMag;

  public RKPattern04(LX lx) {

    super((P3LX) lx, new PVector(lx.model.cx, lx.model.cy, lx.model.cz), new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange), 200);
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

  void run(double deltaMs, PGraphics pg) {

    updateParameters();

    updateRootVts();
    updateMesh();

    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, faceRes);
    pg.image(pgR, faceRes*2, faceRes);
    pg.image(pgD, faceRes, 0);
    pg.image(pgU, faceRes, faceRes*2);
    pg.image(pgF, faceRes, faceRes);
    pg.image(pgB, faceRes*3, faceRes);
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
      for (int i=0; i<pEQBands.length; i++) {
        totalBandMag += eq.getBandf(i);
      }
      avgBandMag = totalBandMag/16;

      rotXT += rotDir.x*(eq.getBandf(0)-pEQBands[0])*2;
      rotYT += rotDir.z*(eq.getBandf(7)-pEQBands[7])*2;
      rotZT += rotDir.y*eq.getBandf(14)*.1;
      gFIncreT = map(sq(totalBandMag), 0, 256, 0, .25);

      for (int i=0; i<pEQBands.length; i++) {
        pEQBands[i] = eq.getBandf(i);
      }
    }

    hue1 = h1.getValuef();
    hue2 = h2.getValuef();
    hue3 = h3.getValuef();
    clearBg = clrBg.getValueb();
    rotX = lerp(rotX, rotXT, .25);
    rotY = lerp(rotY, rotYT, .25);
    rotZ = lerp(rotZ, rotZT, .25);
    gFIncre = lerp(gFIncre, gFIncreT, .25);
    hue1 = lerp(hue1, hue1T, .1);
    hue2 = lerp(hue2, hue2T, .1);
    hue3 = lerp(hue3, hue3T, .1);

    colorMode(HSB);
    c1 = color(hue1, 255, 255);
    c2 = color(hue2, 255, 255);
    c3 = color(hue3, 255, 255);
  }

  void updateCubeMaps() {
    updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
    updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
    updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
    updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
    updateCubeMap(pgD, 0, 0, -.001, 0, -1, 0, 0, 1, 0);
    updateCubeMap(pgU, 0, 0, -.001, 0, 1, 0, 0, 1, 0);
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
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

    Mesh [] children;

    int idx, lv;

    float r1, r2, r3;
    PVector nrml;

    Vtx [] outers, mids, struts, strutMids, sStruts;

    Mesh(int idx, int lv) {

      this.idx = idx;
      this.lv = lv;

      outers = new Vtx[3];
      mids = new Vtx[3];
      struts = new Vtx[3];
      strutMids = new Vtx[3];
      sStruts = new Vtx[3];

      for (int i=0; i<3; i++) {
        outers[i] = new Vtx();
        mids[i] = new Vtx();
        struts[i] = new Vtx();
        strutMids[i] = new Vtx();
        sStruts[i] = new Vtx();
      }

      if (lv<mxLv) {
        lv ++;
        children = new Mesh[16];
        for (int i=0; i<children.length; i++) {
          children[i] = new Mesh(i, lv);
        }
      }
    }

    void update(Vtx [] outers, float f) {
      this.outers = outers;
      for (int i=0; i<mids.length; i++) {
        float itp = (noise(f)-.5)/(float(lv)+1)+.5;
        mids[i].interpolate(outers[i], outers[(i+1)%outers.length], itp);
        f += gFIncre;
      }
      for (int i=0; i<struts.length; i++) {
        float itp = (noise(f)-.5)/(float(lv)+1)+.8;
        struts[i].protrude(mids[i], outers[(i+2)%outers.length], outers[(i+1)%outers.length], itp, lv);
        f += gFIncre;
      }
      for (int i=0; i<strutMids.length; i++) {
        float itp = (noise(f)-.5)/(float(lv)+1)+.5;
        strutMids[i].interpolate(struts[i], struts[(i+1)%struts.length], itp);
        f += gFIncre;
      }
      for (int i=0; i<sStruts.length; i++) {
        float itp = (noise(f)-.5)/(float(lv)+1)+.5;
        sStruts[i].protrude(strutMids[i], outers[(i+1)%outers.length], struts[i], itp, lv);
        f += gFIncre;
      }

      if (lv<mxLv) {
        f += gFIncre;
        children[15].update(struts, f);
        for (int i=0; i<3; i++) {
          int j = (i+1)%3;

          Vtx [] g1 = {
            outers[j], sStruts[i], mids[i]
          };
          children[i*5].update(g1, f);
          f += gFIncre;

          Vtx [] g2 = {
            sStruts[i], struts[i], mids[i]
          };
          children[i*5+1].update(g2, f);
          f += gFIncre;

          Vtx [] g3 = {
            sStruts[i], struts[i], struts[j]
          };
          children[i*5+2].update(g3, f);
          f += gFIncre;

          Vtx [] g4 = {
            sStruts[i], struts[j], mids[j]
          };
          children[i*5+3].update(g4, f);
          f += gFIncre;

          Vtx [] g5 = {
            outers[j], sStruts[i], mids[j]
          };
          children[i*5+4].update(g5, f);
          f += gFIncre;
        }
      }
    }

    void display(PGraphics pg) {
      if (lv == mxLv) {
        for (int i=0; i<3; i++) {
          int j = (i+1)%3;
          drawTri(outers[j], sStruts[i], mids[i], pg);
          drawTri(sStruts[i], struts[i], mids[i], pg);
          drawTri(sStruts[i], struts[i], struts[j], pg);
          drawTri(sStruts[i], struts[j], mids[j], pg);
          drawTri(outers[j], sStruts[i], mids[j], pg);
        }
      }

      if (lv<mxLv) {
        for (int i=0; i<children.length; i++) {
          children[i].display(pg);
        }
      }
    }

    void drawTri(Vtx v1, Vtx v2, Vtx v3, PGraphics pg) {

      nrml = PVector.sub(v1.pos, v2.pos).cross(PVector.sub(v3.pos, v2.pos));
      r1 = map(abs(HALF_PI-PVector.angleBetween(nrml, lgt1)), 0, HALF_PI, 0, 1.5);
      r2 = map(abs(HALF_PI-PVector.angleBetween(nrml, lgt2)), 0, HALF_PI, 0, 1.5);
      r3 = map(abs(HALF_PI-PVector.angleBetween(nrml, lgt3)), 0, HALF_PI, 0, 1.5);
      pg.fill(constrain(r1*red(c1)+r2*red(c2)+r3*red(c3), 0, 255), 
        constrain(r1*green(c1)+r2*green(c2)+r3*green(c3), 0, 255), 
        constrain(r1*blue(c1)+r2*blue(c2)+r3*blue(c3), 0, 255));

      pg.vertex(v1.pos.x, v1.pos.y, v1.pos.z);
      pg.vertex(v2.pos.x, v2.pos.y, v2.pos.z);
      pg.vertex(v3.pos.x, v3.pos.y, v3.pos.z);
    }
  }

  void initRootVts() {

    rF = random(100);
    rdnsF = random(100);

    rootVts = new Vtx[3];
    for (int i=0; i<rootVts.length; i++) {
      rootVts[i] = new Vtx();
    }
  }

  void updateRootVts() {
    for (int i=0; i<rootVts.length; i++) {
      float r = 1200+(noise(rF+i*.1)-0.5)*1200;
      float rdns = i*(TWO_PI/rootVts.length)+(noise(rdnsF+i*.1)-.5)*PI/3;
      float x = r*cos(rdns);
      float y = 300;
      float z = r*sin(rdns);
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
        lerp(midOp.pos.x, mid.pos.x, .5), 
        lerp(midOp.pos.y, mid.pos.y, .5), 
        lerp(midOp.pos.z, mid.pos.z, .5)
        );

      float d1 = dist(midOp.pos.x, midOp.pos.y, midOp.pos.z, mid.pos.x, mid.pos.y, mid.pos.z);
      float d2 = dist(strut0.x, strut0.y, strut0.z, mid.pos.x, mid.pos.y, mid.pos.z)-
        dist(m.x, m.y, m.z, mid.pos.x, mid.pos.y, mid.pos.z);

      float mag = sqrt(sq(d1*.5)-sq(d2))/(float(lv)/3+1);

      PVector ofst = PVector.sub(outer.pos, mid.pos).cross(PVector.sub(midOp.pos, mid.pos));
      ofst.normalize();
      ofst.mult(mag);

      setPos(strut0.x+ofst.x, strut0.y+ofst.y, strut0.z+ofst.z);
    }
  }
}

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

  int faceRes = 200;
  boolean clearBg;

  float rotX, rotXT, rotY, rotYT, rotZ, rotZT;
  PVector lgt1, lgt2, lgt3;
  float hue1, hue1T, hue2, hue2T, hue3, hue3T;
  float vOfstScalar, vOfstScalarT, vOfstSpeed, vOfstSpeedT;
  color c1, c2, c3;

  int mxLv = 2;
  float gF;
  float rF, rdnsF, dF;
  Mesh rootMesh1, rootMesh2;
  Vtx [] rootVts1, rootVts2;

  float [] thetaInit = {
    -atan(sqrt(2)), -atan(sqrt(2)), PI-atan(sqrt(2)), PI-atan(sqrt(2))
  };
  float [] phiInit = {
    HALF_PI*.5, HALF_PI*2.5, HALF_PI*1.5, HALF_PI*3.5
  };

  PVector rotDir;

  boolean audioLinked;

  float [] pEQBands = new float[16];
  float totalBandMag, avgBandMag;

  public RKPattern05(LX lx) {

    super((P3LX) lx, new PVector(lx.model.cx, lx.model.cy, lx.model.cz), new PVector(lx.model.xRange, lx.model.yRange, lx.model.zRange), 200);
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

  void run(double deltaMs, PGraphics pg) {

    updateParameters();

    updateRootVts();
    updateMesh();

    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, faceRes);
    pg.image(pgR, faceRes*2, faceRes);
    pg.image(pgD, faceRes, 0);
    pg.image(pgU, faceRes, faceRes*2);
    pg.image(pgF, faceRes, faceRes);
    pg.image(pgB, faceRes*3, faceRes);
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
      for (int i=0; i<pEQBands.length; i++) {
        totalBandMag += eq.getBandf(i);
      }
      avgBandMag = totalBandMag/16;

      rotXT += rotDir.x*(eq.getBandf(0)-pEQBands[0])*2;
      rotYT += rotDir.z*(eq.getBandf(7)-pEQBands[7])*2;
      rotZT += rotDir.y*eq.getBandf(14)*.1;
      vOfstSpeedT = map(sq(totalBandMag), 0, 256, 0, .02);
      vOfstScalarT = 5;//map(sq(totalBandMag), 0, 256, 0, 10);

      for (int i=0; i<pEQBands.length; i++) {
        pEQBands[i] = eq.getBandf(i);
      }
    }
    
    clearBg = clrBg.getValueb();
    hue1 = h1.getValuef();
    hue2 = h2.getValuef();
    hue3 = h3.getValuef();
    
    rotX = lerp(rotX, rotXT, .25);
    rotY = lerp(rotY, rotYT, .25);
    rotZ = lerp(rotZ, rotZT, .25);
    vOfstScalar = lerp(vOfstScalar, vOfstScalarT, .25);
    vOfstSpeed = lerp(vOfstSpeed, vOfstSpeedT, .25);
    hue1 = lerp(hue1, hue1T, .1);
    hue2 = lerp(hue2, hue2T, .1);
    hue3 = lerp(hue3, hue3T, .1);

    colorMode(HSB);
    c1 = color(hue1, 255, 255);
    c2 = color(hue2, 255, 255);
    c3 = color(hue3, 255, 255);
  }

  void updateCubeMaps() {
    updateCubeMap(pgF, 0, 0, 0, 0, 0, 1, 0, 1, 0);
    updateCubeMap(pgL, 0, 0, 0, 1, 0, 0, 0, 1, 0);
    updateCubeMap(pgR, 0, 0, 0, -1, 0, 0, 0, 1, 0);
    updateCubeMap(pgB, 0, 0, 0, 0, 0, -1, 0, 1, 0);
    updateCubeMap(pgD, 0, 0, -.001, 0, -1, 0, 0, 1, 0);
    updateCubeMap(pgU, 0, 0, -.001, 0, 1, 0, 0, 1, 0);
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
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
    rootMesh2.update(rootVts2, gF+247.121);
    gF += .005;
  }

  class Mesh {

    Mesh [] children;

    int idx, lv;

    float r1, r2, r3;
    PVector nrml;

    Vtx [] outers, mids, struts, strutMids, sStruts;

    Mesh(int idx, int lv) {

      this.idx = idx;
      this.lv = lv;

      outers = new Vtx[3];
      mids = new Vtx[3];
      struts = new Vtx[3];
      strutMids = new Vtx[3];
      sStruts = new Vtx[3];

      for (int i=0; i<3; i++) {
        outers[i] = new Vtx();
        mids[i] = new Vtx();
        struts[i] = new Vtx();
        strutMids[i] = new Vtx();
        sStruts[i] = new Vtx();
      }

      if (lv<mxLv) {
        lv ++;
        children = new Mesh[16];
        for (int i=0; i<children.length; i++) {
          children[i] = new Mesh(i, lv);
        }
      }
    }

    void update(Vtx [] outers, float f) {
      this.outers = outers;
      for (int i=0; i<mids.length; i++) {
        float itp = (noise(f)-.5)/4+.5;
        mids[i].interpolate(outers[i], outers[(i+1)%outers.length], itp, idx, lv);
        f += .1;
      }
      for (int i=0; i<struts.length; i++) {
        float itp = (noise(f)-.5)/4+.8;
        struts[i].interpolate(mids[i], outers[(i+2)%outers.length], itp, idx, lv);
        f += .1;
      }
      for (int i=0; i<strutMids.length; i++) {
        float itp = (noise(f)-.5)/4+.5;
        strutMids[i].interpolate(struts[i], struts[(i+1)%struts.length], itp, idx, lv);
        f += .1;
      }
      for (int i=0; i<sStruts.length; i++) {
        float itp = (noise(f)-.5)/4+.5;
        sStruts[i].interpolate(strutMids[i], outers[(i+1)%outers.length], itp, idx, lv);
        f += .1;
      }

      if (lv<mxLv) {
        f += .1;
        children[15].update(struts, f);
        for (int i=0; i<3; i++) {
          int j = (i+1)%3;

          Vtx [] g1 = {
            outers[j], sStruts[i], mids[i]
          };
          children[i*5].update(g1, f);
          f += .1;

          Vtx [] g2 = {
            sStruts[i], struts[i], mids[i]
          };
          children[i*5+1].update(g2, f);
          f += .1;

          Vtx [] g3 = {
            sStruts[i], struts[i], struts[j]
          };
          children[i*5+2].update(g3, f);
          f += .1;

          Vtx [] g4 = {
            sStruts[i], struts[j], mids[j]
          };
          children[i*5+3].update(g4, f);
          f += .1;

          Vtx [] g5 = {
            outers[j], sStruts[i], mids[j]
          };
          children[i*5+4].update(g5, f);
          f += .1;
        }
      }
    }

    void display(PGraphics pg) {
      if (lv == mxLv) {

        pg.beginShape(TRIANGLES);
        for (int i=0; i<3; i++) {
          int j = (i+1)%3;
          drawTri(outers[j], sStruts[i], mids[i], pg);
          drawTri(sStruts[i], struts[i], mids[i], pg);
          drawTri(sStruts[i], struts[i], struts[j], pg);
          drawTri(sStruts[i], struts[j], mids[j], pg);
          drawTri(outers[j], sStruts[i], mids[j], pg);
        }
        pg.endShape();
      }

      if (lv<mxLv) {
        for (int i=0; i<children.length; i++) {
          children[i].display(pg);
        }
      }
    }

    void drawTri(Vtx v1, Vtx v2, Vtx v3, PGraphics pg) {

      nrml = PVector.sub(v1.pos, v2.pos).cross(PVector.sub(v3.pos, v2.pos));
      r1 = map(abs(HALF_PI-PVector.angleBetween(nrml, lgt1)), 0, HALF_PI, 0, 1.5);
      r2 = map(abs(HALF_PI-PVector.angleBetween(nrml, lgt2)), 0, HALF_PI, 0, 1.5);
      r3 = map(abs(HALF_PI-PVector.angleBetween(nrml, lgt3)), 0, HALF_PI, 0, 1.5);
      pg.fill(constrain(r1*red(c1)+r2*red(c2)+r3*red(c3), 0, 255), 
        constrain(r1*green(c1)+r2*green(c2)+r3*green(c3), 0, 255), 
        constrain(r1*blue(c1)+r2*blue(c2)+r3*blue(c3), 0, 255));

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
    for (int i=0; i<3; i++) {
      rootVts1[i] = new Vtx();
      rootVts2[i] = new Vtx();
    }
  }

  void updateRootVts() {
    for (int i=0; i<3; i++) {
      float r = 1200;
      float theta = thetaInit[i]+(noise(dF+i*.1)-.5)*PI/3;
      float phi = phiInit[i]+(noise(dF+i*.1)-.5)*PI/3;
      float rdns = i*(TWO_PI/rootVts1.length);//+(noise(rdnsF+i*.1)-.5)*PI/3;
      float x = r*cos(rdns);
      float y = r*sin(rdns);
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

      //float ofst = (noise(theta ,phi, frameCount*.2)-.5)*140*float(idx)/(lv+1);
      float ofst = (noise(theta*vOfstScalar, phi*vOfstScalar, frameCount*vOfstSpeed)-.5)*100*float(idx)/(lv+1);

      setPos(
        lerp(v2.pos.x, v1.pos.x, itp)+ofst*sin(theta)*cos(phi), 
        lerp(v2.pos.y, v1.pos.y, itp)+ofst*sin(theta)*sin(phi), 
        lerp(v2.pos.z, v1.pos.z, itp)+ofst*cos(theta), 
        itpTheta, itpPhi
        );
    }
  }
}