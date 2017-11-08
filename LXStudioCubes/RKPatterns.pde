public class RKPattern01 extends P3CubeMapPattern {

  int ringRes = 40;
  float l1 = 600, l2 = 600, l3 = 300;
  Ring [] testRings;

  public RKPattern01(LX lx) {

    super((P3LX) lx, new PVector(55*12 + 8*12, 4*12, 2*12 + 0.3*8*12), new PVector(16*12, 16*12, 16*12*0.3), 100);
    testRings = new Ring[20];
    for (int i=0; i<testRings.length; i++) {
      float initTheta = i*PI/testRings.length;
      testRings[i] = new Ring(ringRes, initTheta, l1, l2, l3);
    }
  }

  void run(double deltaMs, PGraphics pg) {

    for (int i=0; i<testRings.length; i++) {
      testRings[i].update();
    }
    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, 100);
    pg.image(pgR, 200, 100);
    pg.image(pgD, 100, 0);
    pg.image(pgU, 100, 200);
    pg.image(pgF, 100, 100);
    pg.image(pgB, 300, 100);
    pg.endDraw();
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
    pg.beginDraw();
    pg.background(0);
    pg.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    pg.frustum(-10, 10, -10, 10, 10, 1000);
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
    for (int i=0; i<testRings.length; i++) {
      testRings[i].display(pg);
    }
  }

  class Ring {

    int amt;
    float l1, l2, l3, theta, weight;
    Vtx [] vts;

    Ring(int amt, float theta, float l1, float l2, float l3) {

      this.amt = amt;
      this.l1 = l1;
      this.l2 = l2;
      this.l3 = l3;
      this.theta = theta;

      vts = new Vtx[this.amt];
      for (int i=0; i<vts.length; i++) {
        float initPhi = i*TWO_PI/amt;
        vts[i] = new Vtx(this.theta, initPhi, this.l1, this.l2, this.l3);
      }
    }

    void update() {
      theta += PI/720;
      if (theta>PI) theta -= PI;

      weight = sin(theta)*4;

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
        pg.curveVertex(vts[i].pos.x, vts[i].pos.y, vts[i].pos.z);
      }
      pg.curveVertex(vts[0].pos.x, vts[0].pos.y, vts[0].pos.z);
      pg.curveVertex(vts[1].pos.x, vts[1].pos.y, vts[1].pos.z);
      pg.curveVertex(vts[2].pos.x, vts[2].pos.y, vts[2].pos.z);
      pg.endShape();
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

      thetaOfstRange = sin(theta)*HALF_PI;
      thetaOfst = (noise(phi+frameCount*.005, cos(this.thetaBase)-frameCount*.005)-.5)*thetaOfstRange;
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

  ArrayList <Arc> arcs;
  float l1 = 600, l2 = 600, l3 = 300;
  int switchCount;

  public RKPattern02(LX lx) {

    super((P3LX) lx, new PVector(55*12 + 8*12, 4*12, 2*12 + 0.3*8*12), new PVector(16*12, 16*12, 16*12*0.3), 100);

    arcs = new ArrayList<Arc>();
    Arc parentArc = new Arc(0, 16, 0, PI, 0, TWO_PI);
    arcs.add(parentArc);

    for (int i=0; i<8; i++) {
      for (int j=arcs.size()-1; j>-1; j--) {
        Arc arc = arcs.get(j);
        if (!arc.hasChildren) arc.splitUp();
      }
    }
  }

  void run(double deltaMs, PGraphics pg) {

    if (frameCount%120==0) {
      switchCount = (switchCount+1)%3;
      if (switchCount == 0) {
        for (int i=arcs.size()-1; i>-1; i--) {
          Arc arc = arcs.get(i);
          if (arc.hasChildren) arc.reset();
        }
      } else {
        for (int i=arcs.size()-1; i>-1; i--) {
          Arc arc = arcs.get(i);
          if (arc.hasChildren) arc.randomize();
        }
      }
    }

    for (int i=0; i<arcs.size(); i++) {
      Arc arc = arcs.get(i);
      if (arc.hasChildren) arc.update();
    }
    updateCubeMaps();

    pg.beginDraw();
    pg.background(0);
    pg.image(pgL, 0, 100);
    pg.image(pgR, 200, 100);
    pg.image(pgD, 100, 0);
    pg.image(pgU, 100, 200);
    pg.image(pgF, 100, 100);
    pg.image(pgB, 300, 100);
    pg.endDraw();
  }

  void updateCubeMap(PGraphics pg, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
    pg.beginDraw();
    pg.background(0);
    pg.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    pg.frustum(-10, 10, -10, 10, 10, 1000);
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
    pg.rotateX(HALF_PI);
    pg.noFill();
    pg.stroke(255);
    pg.strokeWeight(2);
    for (int i=0; i<arcs.size(); i++) {
      Arc arc = arcs.get(i);
      if (arc.hasChildren) arc.display(pg);
    }
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

  class Arc {

    Arc cArc1, cArc2;
    boolean hasChildren;
    int segAmt, lv;

    PVector [] vts;

    float splitRatio, splitRatioT;
    float sTheta, eTheta, sPhi, ePhi;
    float l1 = 600, l2 = 600, l3 = 300;

    Arc(int lv, int segAmt, float sTheta, float eTheta, float sPhi, float ePhi) {

      this.lv = lv;
      this.segAmt = segAmt;
      this.sTheta = sTheta;
      this.eTheta = eTheta;
      this.sPhi = sPhi;
      this.ePhi = ePhi;
    }

    void update() {
      splitRatio = lerp(splitRatio, splitRatioT, .125);
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
    }

    void randomize() {
      splitRatioT = random(1);
    }

    void reset() {
      splitRatioT = .5;
    }

    void splitUp() {

      hasChildren = true;
      splitRatio = .5;
      splitRatioT = random(1);

      vts = new PVector[segAmt];

      if (lv%2==0) {
        cArc1 = new Arc(lv+1, segAmt, sTheta, lerp(sTheta, eTheta, splitRatio), sPhi, ePhi);
        cArc2 = new Arc(lv+1, segAmt, lerp(sTheta, eTheta, splitRatio), eTheta, sPhi, ePhi);
        arcs.add(cArc1);
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
        cArc1 = new Arc(lv+1, segAmt, sTheta, eTheta, sPhi, lerp(sPhi, ePhi, splitRatio));
        cArc2 = new Arc(lv+1, segAmt, sTheta, eTheta, lerp(sPhi, ePhi, splitRatio), ePhi);
        arcs.add(cArc1);
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

    void display(PGraphics pg) {
      pg.beginShape();
      for (int i=0; i<segAmt; i++) {
        pg.vertex(vts[i].x, vts[i].y, vts[i].z);
      }
      pg.endShape();
    }
  }
}