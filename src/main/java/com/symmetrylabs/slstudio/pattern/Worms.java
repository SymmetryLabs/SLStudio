package com.symmetrylabs.slstudio.pattern;

import java.lang.Math;
import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.Click;
import heronarts.lx.transform.LXVector;
import heronarts.lx.color.LXColor;
import heronarts.lx.LXUtils;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;

enum CursorState {
    RUNNING,
    STOPPING,
    STOPPED
}

public class Worms extends SLPattern {
    private final float STRIPS_PER_SECOND = 10;
    private final float TRAIL_TIME = 3000;
    private final float Z_MID_LATE = 82.0f;
    private final int NUM_CURSORS = 50;

    public dLattice lattice;
    private final List<Runnable> latticeInitedCallbacks = new ArrayList<Runnable>();
    private final ArrayList<dCursor> cur  = new ArrayList<dCursor>(30);
    private final Click moveChase = new Click(1000);
    private final LXVector middle;
    //private GraphicEQ eq = null;
    private float nConfusion;

    private CompoundParameter pBeat      = new CompoundParameter("BEAT",  0);
    private CompoundParameter pSpeed     = new CompoundParameter("FAST", .2);
    private CompoundParameter pBlur      = new CompoundParameter("BLUR", .3);
    private CompoundParameter pWorms     = new CompoundParameter("WRMS", .3);
    private CompoundParameter pConfusion = new CompoundParameter("CONF", .1);
    //private CompoundParameter pEQ        = new CompoundParameter("EQ"  ,  0);
    private CompoundParameter pSpawn     = new CompoundParameter("DIR" ,  0);
    private CompoundParameter pColor     = new CompoundParameter("CLR" , .1);

    public Worms(LX lx) {
        super(lx);
        addModulator(moveChase).start();
        addParameter(pBeat);
        addParameter(pSpeed);
        addParameter(pBlur);
        addParameter(pWorms);
        //addParameter(pEQ);
        addParameter(pConfusion);
        addParameter(pSpawn);
        addParameter(pColor);

        middle = new LXVector(1.5f * model.cx, 1.5f * model.cy, 71.f);
    }

    private boolean isLatticeInited() {
        return lattice != null;
    }

    private int animNum() {
        return (int)Math.floor(pSpawn.getValuef() * (4 - 0.01f));
    }

    private float randX() {
        return (float)LXUtils.random(0, model.xMax - model.xMin) + model.xMin;
    }

    private float randY() {
        return (float)LXUtils.random(0, model.yMax - model.yMin) + model.yMin;
    }

    private LXVector randEdge() {
        return (LXUtils.random(0, 2) < 1) ?
            new LXVector(((float)LXUtils.random(0, 2) < 1) ? model.xMin : model.xMax, randY(), Z_MID_LATE) :
            new LXVector(randX(), (float)LXUtils.random(0, 2) < 1 ? model.yMin:model.yMax, Z_MID_LATE);
    }

    public void onParameterChange(LXParameter parameter) {
        onParameterChanged(parameter);
        nConfusion = 1 - pConfusion.getValuef();

        for (int i = 0; i < NUM_CURSORS; i++) {
            if (parameter == pSpawn) {
                reset(cur.get(i));
            }
            cur.get(i).destSpeed = nConfusion;
        }
    }

    private float getClr() {
        return palette.getHuef() + (float)LXUtils.random(0, pColor.getValuef() * 300);
    }

    private void reset(dCursor c) {
        switch(animNum()) {
            case 0: // middle to edges
                c.clr = lx.hsb(getClr(), 100, 100);
                c.setDest(lattice.getClosest(randEdge()).v, nConfusion);
                c.setCur (lattice.getClosest(middle));
                break;

            case 1: // top to bottom
                c.clr = lx.hsb(getClr(), 100, 100);
                float xLin = randX();
                c.setDest(lattice.getClosest(new LXVector(xLin, 0, Z_MID_LATE)).v, nConfusion);
                c.setCur (lattice.getClosest(new LXVector(xLin, model.yMax, Z_MID_LATE)));
                break;

            case 2: // chase a point around
                c.clr = lx.hsb(getClr(), 100, 100);
                break;

            case 3: // sideways
                boolean bLeft = LXUtils.random(0, 2) < 1;
                c.clr = lx.hsb(getClr() + ((float)LXUtils.random(0, 120)), 100, 100);
                float yLin = randX();
                c.setDest(lattice.getClosest(new LXVector(bLeft ? 0 : model.xMax, yLin, Z_MID_LATE)).v, nConfusion);
                c.setCur (lattice.getClosest(new LXVector(bLeft ? model.xMax : 0, yLin, Z_MID_LATE)));
                break;
        }

        if (pBlur.getValuef() == 1 && (float)LXUtils.random(0, 2) < 1) {
            c.clr = lx.hsb(0, 0, 0);
        }
    }

    private void setNewDest() {
        if (animNum() != 2) return;

        LXVector dest = new LXVector(randX(), randY(), Z_MID_LATE);
        for (int i = 0; i < NUM_CURSORS; i++) {
            cur.get(i).setDest(lattice.getClosest(dest).v, nConfusion);
            cur.get(i).clr = lx.hsb(getClr()+75,100,100); // chase a point around
        }
    }

    public void run(double deltaMs) {
        if (!isLatticeInited()) return;
        if (deltaMs > 100) return;

        if (moveChase.click()) {
            setNewDest();
        }

        float fBass = 0;
        float fTreble = 0;

        // if (pEQ.getValuef() > 0) {
        //   fBass = eq.getAveragef(0, 4);
        //   fTreble = eq.getAveragef(eq.numBands - 7, 7);
        // }

        if (pBlur.getValuef() < 1) { // trails
            for (int i = 0, s = model.points.length; i < s; i++) {
                int c = colors[i];
                float b = LXColor.b(c);

                if (b > 0) {
                    colors[i] = lx.hsb(
                        LXColor.h(c),
                        LXColor.s(c),
                        (float)LXUtils.constrain((b - 100 * deltaMs/(pBlur.getValuef() * TRAIL_TIME)), 0, 100));
                }
            }
        }

        //int nWorms = Math.floor(pWorms.getValuef() * NUM_CURSORS * map(pEQ.getValuef(), 0, 1, 1, LXUtils.constrain(2 * fBass, 0, 1)));
        int nWorms = (int)Math.floor(pWorms.getValuef() * NUM_CURSORS);

        for (int i = 0; i < nWorms; i++) {
            dCursor c = cur.get(i);
            int nLeft = (int)(Math.floor((float)deltaMs*0.001*STRIPS_PER_SECOND * 65536 * (5 * pSpeed.getValuef())));
            nLeft *= (1 - lx.tempo.rampf() * pBeat.getValuef());

            while (nLeft > 0) {
                nLeft = c.draw(nLeft, this);

                if (!c.isDone()) {
                    continue;
                }

                c.onDone();

                if (c.atDest()) {
                    reset(c);
                }
            }
        }
    }

    public void onActive() {
    //   if (eq == null) {
    //     eq = new GraphicEQ(lx.audioInput());
    //     eq.slope.setValue(6);
    //     eq.gain.setValue(12);
    //     eq.range.setValue(36);
    //     eq.release.setValue(640);
    //     addModulator(eq).start();
    //   }

        initLattice(new Runnable() {
            public void run() {
                for (int i = 0; i < NUM_CURSORS; i++) {
                    dCursor c = new dCursor();
                    reset(c);
                    cur.add(c);
                }

                //onParameterChange(pEQ);
                setNewDest();
            }
        });
    }

    private void initLattice(Runnable callback) {
        if (!isLatticeInited()) {
            //latticeInited = true;
            new Thread() {
                public void run() {
                    final dLattice l = new dLattice(((StripsModel)model));
                    SLStudio.applet.dispatcher.dispatchEngine(new Runnable() {
                        public void run() {
                            lattice = l;
                            for (Runnable callback : latticeInitedCallbacks) {
                                callback.run();
                            }
                            latticeInitedCallbacks.clear();
                        }
                    });
                }
            }.start();
        }
        if (callback != null) {
            if (isLatticeInited()) {
                callback.run();
            } else {
                latticeInitedCallbacks.add(callback);
            }
        }
    }
}

class dCursor {
    dVertex vCur;
    dVertex vNext;
    dVertex vDest;

    float destSpeed;

    int posStop;
    int pos;
    int posNext; // 0 - 65535

    int clr;

    float speed = 1;
    float targetSpeed = 1;
    CursorState state = CursorState.RUNNING;

    boolean isDone() {
        return pos == posStop;
    }

    boolean atDest() {
        LXPoint vCurP = vCur.getPoint(0);
        LXPoint vDestP1 = vDest.getPoint(0);
        LXPoint vDestP2 = vDest.getPoint(14);

        return vCur.s == vDest.s ||
            LXUtils.distance(vCurP.x, vCurP.y, vDestP1.x, vDestP1.y) < 12 ||
            LXUtils.distance(vCurP.x, vCurP.y, vDestP2.x, vDestP2.y) < 12;
    }

    void setCur(dVertex _v, int _p) {
        p2 = null;
        vCur = _v;
        pos = _p;
        pickNext();
    }

    void setCur(dPixel  _p) {
        setCur(_p.v, _p.pos);
    }

    void setNext(dVertex _v, int _p, int _s) {
        vNext = _v;
        posNext = _p << 12;
        posStop = _s << 12;
    }

    void setDest(dVertex _v, float _speed) {
        vDest = _v;
        destSpeed = _speed;
    }

    void onDone() {
        setCur(vNext, posNext);
        pickNext();
    }

    float minDist;
    int nTurns;
    boolean bRandEval;

    void evaluate(dVertex v, int p, int s) {
        if (v == null) return;
        ++nTurns;

        if (bRandEval) {
            if (LXUtils.random(0, nTurns) < 1) {
                setNext(v,p,s);
            }
            return;

        } else {
            LXPoint p1 = v.getPoint(14);
            LXPoint p2 = vDest.getPoint(0);
            float d = (float)LXUtils.distance(p1.x, p1.y, p2.x, p2.y);

            if (d <  minDist) {
                minDist = d;
                setNext(v,p,s);
            }
            if (d == minDist && LXUtils.random(0, 2) < 1) {
                minDist = d;
                setNext(v, p, s);
            }
        }
    }

    void evalTurn(dTurn t) {
        if (t == null || t.pos0<< 12 <= pos) return;
        evaluate(t.v, t.pos1, t.pos0);
        evaluate(t.v.opp, 16 - t.pos1, t.pos0);
    }

    void pickNext() {
        bRandEval = LXUtils.random(0, 0.05 + destSpeed) < 0.05;
        minDist = 500;
        nTurns = 0;

        if (vCur != null) {
            evaluate(vCur.c0, 0, 15);
            evaluate(vCur.c1, 0, 15);
            evaluate(vCur.c2, 0, 15);
            evaluate(vCur.c3, 0, 15);
            evalTurn(vCur.t0);
            evalTurn(vCur.t1);
            evalTurn(vCur.t2);
            evalTurn(vCur.t3);
        }
    }

    LXPoint p1, p2;
    int i2;

    int draw(int nAmount, SLPattern pat) {
        if (pat == null) return 0;
        if (vCur == null) return 0;

        int nFrom = (pos) >> 12;
        int nMv = Math.min(nAmount, posStop - pos);
        int nTo = Math.min(14,(pos + nMv) >> 12);
        dVertex v  = vCur;

        // if (dDebug) {
        //   p1 = v.getPoint(nFrom);
        //   float d = (p2 == null ? 0 : pointDist(p1, p2));
        //   if (d>5) {
        //     System.out.print("too wide! quitting: " + d);
        //     exit();
        //   }
        // }

        for (int i = nFrom; i <= nTo; i++) {
            pat.getColors()[v.ci + v.dir*i] = clr;
        }

        if (v.same != null) {
            for (int i = nFrom; i <= nTo; i++) {
                pat.getColors()[v.same.ci + v.same.dir*i] = clr;
            }
        }

        // if (dDebug) {
        //   p2 = v.getPoint(nTo); i2 = nTo;
        // }

        pos += nMv;
        return nAmount - nMv;
    }
}

class dVertex {
    // connections on the cube
    dVertex c0;
    dVertex c1;
    dVertex c2;
    dVertex c3;

    // opp - same strip, opp direction
    dVertex opp;
    dVertex same;

    // same - same strut, diff strip, dir
    dTurn t0;
    dTurn t1;
    dTurn t2;
    dTurn t3;

    Strip s;
    int dir, ci;

    dVertex(Strip _s, LXPoint _p) {
        s = _s;
        ci = _p.index;
    }

    LXPoint getPoint(int i) {
        return s.points[dir > 0 ? i : 14 - i];
    }

    void setOpp(dVertex _opp) {
        opp = _opp;
        dir = (ci < opp.ci ? 1 : -1);
    }
}

class dTurn {
    dVertex v;
    int pos0, pos1;

    dTurn(int _pos0, dVertex _v, int _pos1) {
        v = _v;
        pos0 = _pos0;
        pos1 = _pos1;
    }
}

class dPixel {
    public dVertex v;
    public int pos;

    public dPixel(dVertex _v, int _pos) {
        v = _v;
        pos = _pos;
    }
}

class dLattice {
    StripsModel model;

    void addTurn(dVertex v0, int pos0, dVertex v1, int pos1) {
        dTurn t = new dTurn(pos0, v1, pos1);
        if (v0.t0 == null) { v0.t0=t; return; }
        if (v0.t1 == null) { v0.t1=t; return; }
        if (v0.t2 == null) { v0.t2=t; return; }
        if (v0.t3 == null) { v0.t3=t; return; }
    }

    float dist2(Strip s1, int pos1, Strip s2, int pos2) {
        LXPoint p1 = s1.points[pos1];
        LXPoint p2 = s2.points[pos2];
        return dist3d(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }

    float dist3d(float x1, float y1, float z1, float x2, float y2, float z2) {
        return (float)Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    float pd2 (LXPoint p1, float x, float y, float z) {
        return dist3d(p1.x, p1.y, p1.z, x, y, z);
    }

    // same strut, same direction
    boolean sameSame(Strip s1, Strip s2) {
        return Math.max(dist2(s1, 0, s2, 0), dist2(s1, 14, s2, 14)) < 5;
    }

    // same strut, opp direction
    boolean sameOpp(Strip s1, Strip s2) {
        return Math.max(dist2(s1, 0, s2, 14), dist2(s1, 14, s2, 0)) < 5;
    }

    // 2 strips on same strut
    boolean sameBar(Strip s1, Strip s2) {
        return sameSame(s1,s2) || sameOpp(s1,s2);
    }

    void addJoint(dVertex v1, dVertex v2) {
        // should probably replace parallel but further with the new one
        if (v1.c0 != null && sameBar(v2.s, v1.c0.s)) return;
        if (v1.c1 != null && sameBar(v2.s, v1.c1.s)) return;
        if (v1.c2 != null && sameBar(v2.s, v1.c2.s)) return;
        if (v1.c3 != null && sameBar(v2.s, v1.c3.s)) return;

        if      (v1.c0 == null) v1.c0 = v2;
        else if (v1.c1 == null) v1.c1 = v2;
        else if (v1.c2 == null) v1.c2 = v2;
        else if (v1.c3 == null) v1.c3 = v2;
    }

    dVertex v0(Strip s) { return (dVertex)s.obj1; }
    dVertex v1(Strip s) { return (dVertex)s.obj2; }

    dPixel getClosest(LXVector p) {
        dVertex v = null;
        int pos = 0;
        float d = 2500;

        List<Strip> strips = ((StripsModel)model).getStrips();
        for (Strip s : strips) {
            float nd = pd2(s.points[0], p.x, p.y, p.z);

            if (nd < d) { v=v0(s);
                d = nd;
                pos = 0;
            }

            if (nd > 30) continue;

            for (int k = 1; k <= 14; k++) {
                nd = pd2(s.points[k], p.x, p.y, p.z);

                if (nd < d) {
                    v = v0(s);
                    d = nd;
                    pos = k;
                }
            }
        }

        return LXUtils.random(0, 2) < 1 ? new dPixel(v, pos) : new dPixel(v.opp, 14 - pos);
    }

    public dLattice(StripsModel model) {
        this.model = model;

        List<Strip> strips = model.getStrips();

        for (Strip s : strips) {
            dVertex vrtx0 = new dVertex(s, s.points[0]);
            s.obj1 = vrtx0;

            dVertex vrtx1 = new dVertex(s, s.points[14]);
            s.obj2 = vrtx1;

            vrtx0.setOpp(vrtx1);
            vrtx1.setOpp(vrtx0);
        }

        for (Strip s1 : strips) {
            for (Strip s2 : strips) {
                if (s1.points[0].index < s2.points[0].index) continue;
                int c = 0;

                // parallel
                if (sameSame(s1,s2)) {
                    v0(s1).same = v0(s2);
                    v1(s1).same = v1(s2);
                    v0(s2).same = v0(s1);
                    v1(s2).same = v1(s1);
                    continue;
                }

                // parallel
                if (sameOpp (s1,s2)) {
                    v0(s1).same = v1(s2);
                    v1(s1).same = v0(s2);
                    v0(s2).same = v1(s1);
                    v1(s2).same = v0(s1);
                    continue;
                }

                if (dist2(s1, 0, s2, 0) < 20) { c++; addJoint(v1(s1), v0(s2)); addJoint(v1(s2), v0(s1)); }
                if (dist2(s1, 0, s2,14) < 20) { c++; addJoint(v1(s1), v1(s2)); addJoint(v0(s2), v0(s1)); }
                if (dist2(s1,14, s2, 0) < 20) { c++; addJoint(v0(s1), v0(s2)); addJoint(v1(s2), v1(s1)); }
                if (dist2(s1,14, s2,14) < 20) { c++; addJoint(v0(s1), v1(s2)); addJoint(v0(s2), v1(s1)); }
                if (c > 0) continue;

                // Are they touching at all?
                int pos1 = 0, pos2 = 0; float d = 500;

                while (pos1 < 14 || pos2 < 14) {
                    float oldD = d;

                    if (pos1 < 14) {
                        float d2 = dist2(s1, pos1+1, s2, pos2+0);
                        if (d2 < d) {
                            d = d2;
                            pos1++;
                        }
                    }

                    if (pos2 < 14) {
                        float d2 = dist2(s1, pos1+0, s2, pos2+1);
                        if (d2 < d) {
                            d = d2;
                            pos2++;
                        }
                    }

                    if (d > 50  || oldD == d) break;
                }

                if (d > 5) continue;
                addTurn(v0(s1), pos1, v0(s2), pos2);
                addTurn(v1(s1), 14 - pos1, v0(s2), pos2);
                addTurn(v0(s2), pos2, v0(s1), pos1);
                addTurn(v1(s2), 14 - pos2, v0(s1), pos1);
            }
        }
    }
}
