public class MultiRKPattern01 extends MultiCubeMapPattern {

  List<CompoundParameter> rXList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> rYList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> rZList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> amtList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> speedList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> dspList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> nDspList = new ArrayList<CompoundParameter>();

  static final int faceRes = 200;

  public MultiRKPattern01(LX lx) {
    super(lx, Subpattern.class, faceRes);

    for (int i = 0; i < model.suns.size(); ++i) {
      CompoundParameter rX = new CompoundParameter("rX"+i, 0, -PI, PI);
      CompoundParameter rY = new CompoundParameter("rY"+i, 0, -PI, PI);
      CompoundParameter rZ = new CompoundParameter("rZ"+i, 0, -PI, PI);
      CompoundParameter amt = new CompoundParameter("amt"+i, 20, 1, 25);
      CompoundParameter speed = new CompoundParameter("speed"+i, PI, -TWO_PI*2, TWO_PI*2);
      CompoundParameter dsp = new CompoundParameter("dsp"+i, HALF_PI, 0, PI);
      CompoundParameter nDsp = new CompoundParameter("nDsp"+i, 1, .125, 2.5);

      rXList.add(rX);
      rYList.add(rY);
      rZList.add(rZ);
      amtList.add(amt);
      speedList.add(speed);
      dspList.add(dsp);
      nDspList.add(nDsp);

      addParameter(rX);
      addParameter(rY);
      addParameter(rZ);
      addParameter(amt);
      addParameter(speed);
      addParameter(dsp);
      addParameter(nDsp);
    }
  }

  private class Subpattern extends MultiCubeMapPattern.Subpattern {
    int ringRes = 40, ringAmt = 20, pRingAmt = 20;
    float l1 = 600, l2 = 600, l3 = 600;
    float gTheta, gThetaSpacing, gWeightScalar;
    float rotX, rotXT, rotY, rotYT, rotZ, rotZT, dspmt, dspmtT, nDspmt, nDspmtT, thetaSpeed, thetaSpeedT;
    ArrayList <Ring> testRings;

    Subpattern() {
      super();

      testRings = new ArrayList<Ring>();
      for (int i=0; i<ringAmt; i++) {
        Ring testRing = new Ring(ringRes, l1, l2, l3);
        testRings.add(testRing);
      }
    }

    @Override
      void run(double deltaMs, PGraphics pg) {
      rotXT = rXList.get(sunIndex).getValuef();
      rotYT = rYList.get(sunIndex).getValuef();
      rotZT = rZList.get(sunIndex).getValuef();
      ringAmt = round(amtList.get(sunIndex).getValuef());
      thetaSpeedT = speedList.get(sunIndex).getValuef();
      dspmtT = dspList.get(sunIndex).getValuef();
      nDspmtT = nDspList.get(sunIndex).getValuef();

      rotX = lerp(rotX, rotXT, .1);
      rotY = lerp(rotY, rotYT, .1);
      rotZ = lerp(rotZ, rotZT, .1);

      thetaSpeed = lerp(thetaSpeed, thetaSpeedT, .1);
      dspmt = lerp(dspmt, dspmtT, .1);
      nDspmt = lerp(nDspmt, nDspmtT, .01);

      replenish();

      gTheta += thetaSpeed/720;
      if (gTheta>PI) gTheta -= PI;
      else if (gTheta<0) gTheta += PI;
      gThetaSpacing = lerp(gThetaSpacing, PI/testRings.size(), .1);
      gWeightScalar = map(ringAmt, 1, 25, 2.5, 1);

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
          vts[i] = new Vtx(this.theta, initPhi, this.l1, this.l2, this.l3);
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

      PVector pos;
      float thetaBase, thetaOfst, thetaOfstRange, theta, phi;
      float l1, l2, l3;

      Vtx(float theta, float phi, float l1, float l2, float l3) {
        pos = new PVector();
        this.theta = thetaBase = theta;
        this.phi = phi;
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
      }

      void update(float thetaBase) {
        this.thetaBase = thetaBase;

        thetaOfstRange = sin(theta)*dspmt;
        thetaOfst = (noise(sin(phi)*cos(phi)*nDspmt+frameCount*.005, this.thetaBase*nDspmt*.25-frameCount*.005)-.5)*thetaOfstRange;
        theta = this.thetaBase + thetaOfst;
        pos.set(
          sin(theta)*cos(phi)*.5*l1, 
          cos(theta)*.5*l2, 
          sin(theta)*sin(phi)*.5*l3
          );
      }
    }
  }
}

public class MultiRKPattern02 extends MultiCubeMapPattern {
  List<CompoundParameter> rXList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> rYList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> rZList = new ArrayList<CompoundParameter>();
  List<BooleanParameter> noiseList = new ArrayList<BooleanParameter>();
  List<CompoundParameter> speedList = new ArrayList<CompoundParameter>();
  List<CompoundParameter> dspList = new ArrayList<CompoundParameter>();
  List<BooleanParameter> edgeList = new ArrayList<BooleanParameter>();

  static final int faceRes = 200;

  public MultiRKPattern02(LX lx) {
    super(lx, Subpattern.class, faceRes);

    for (int i = 0; i < model.suns.size(); ++i) {
      CompoundParameter rX = new CompoundParameter("rX"+i, 0, -PI, PI);
      CompoundParameter rY = new CompoundParameter("rY"+i, 0, -PI, PI);
      CompoundParameter rZ = new CompoundParameter("rZ"+i, 0, -PI, PI);
      BooleanParameter noise = new BooleanParameter("noise"+i);
      CompoundParameter speed = new CompoundParameter("speed"+i, .1, 0, .5);
      CompoundParameter dsp = new CompoundParameter("dsp"+i, 1, 0, 2);
      BooleanParameter edge = new BooleanParameter("show edge"+i, true);

      rXList.add(rX);
      rYList.add(rY);
      rZList.add(rZ);
      noiseList.add(noise);
      speedList.add(speed);
      dspList.add(dsp);
      edgeList.add(edge);

      addParameter(rX);
      addParameter(rY);
      addParameter(rZ);
      addParameter(noise);
      addParameter(speed);
      addParameter(dsp);
      addParameter(edge);
    }
  }

  private class Subpattern extends MultiCubeMapPattern.Subpattern {
    ArrayList <Arc> arcs;
    float f, fT, fTIncre;
    float l1 = 600, l2 = 600, l3 = 600;
    float rotX, rotXT, rotY, rotYT, rotZ, rotZT, dspmt, dspmtT;
    float gWeight = 2, gWeightT = 2;
    boolean autoNoise, showEdge;
    int switchCount;

    Subpattern() {
      super();

      arcs = new ArrayList<Arc>();
      Arc parentArc = new Arc(0, 0, 12, 0, PI, 0, TWO_PI);
      arcs.add(parentArc);

      for (int i=0; i<8; i++) {
        for (int j=arcs.size()-1; j>-1; j--) {
          Arc arc = arcs.get(j);
          if (!arc.hasChildren) arc.splitUp();
        }
      }
    }

    @Override
      void run(double deltaMs, PGraphics pg) {

      rotXT = rXList.get(sunIndex).getValuef();
      rotYT = rYList.get(sunIndex).getValuef();
      rotZT = rZList.get(sunIndex).getValuef();
      autoNoise = noiseList.get(sunIndex).getValueb();
      dspmtT = dspList.get(sunIndex).getValuef();
      fTIncre = speedList.get(sunIndex).getValuef();
      showEdge = edgeList.get(sunIndex).getValueb();

      rotX = lerp(rotX, rotXT, .1);
      rotY = lerp(rotY, rotYT, .1);
      rotZ = lerp(rotZ, rotZT, .1);
      dspmt = lerp(dspmt, dspmtT, .1);

      if (showEdge) gWeightT = 2;
      else gWeightT = 0;
      if (abs(gWeightT-gWeight)<.005) gWeight = gWeightT;
      else gWeight = lerp(gWeight, gWeightT, .1);

      fT += fTIncre*.1;
      f = lerp(f, fT, .1);

      if (frameCount%90==0 && !autoNoise) {
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
        if (autoNoise) {
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
}