//import static com.symmetrylabs.util.MathUtils.*;
//
//class dCursor {
//    dVertex vCur;
//    dVertex vNext;
//    dVertex vDest;
//
//    float destSpeed;
//
//    int posStop;
//    int pos;
//    int posNext; // 0 - 65535
//
//    int clr;
//
//    float speed = 1;
//    float targetSpeed = 1;
//    CursorState state = CursorState.RUNNING;
//
//    boolean isDone() {
//        return pos == posStop;
//    }
//
//    boolean atDest() {
//        LXPoint vCurP = vCur.getPoint(0);
//        LXPoint vDestP1 = vDest.getPoint(0);
//        LXPoint vDestP2 = vDest.getPoint(14);
//
//        return vCur.s == vDest.s ||
//            LXUtils.distance(vCurP.x, vCurP.y, vDestP1.x, vDestP1.y) < 12 ||
//            LXUtils.distance(vCurP.x, vCurP.y, vDestP2.x, vDestP2.y) < 12;
//    }
//
//    void setCur(dVertex _v, int _p) {
//        p2 = null;
//        vCur = _v;
//        pos = _p;
//        pickNext();
//    }
//
//    void setCur(dPixel  _p) {
//        setCur(_p.v, _p.pos);
//    }
//
//    void setNext(dVertex _v, int _p, int _s) {
//        vNext = _v;
//        posNext = _p << 12;
//        posStop = _s << 12;
//    }
//
//    void setDest(dVertex _v, float _speed) {
//        vDest = _v;
//        destSpeed = _speed;
//    }
//
//    void onDone() {
//        setCur(vNext, posNext);
//        pickNext();
//    }
//
//    float minDist;
//    int nTurns;
//    boolean bRandEval;
//
//    void evaluate(dVertex v, int p, int s) {
//        if (v == null) return;
//        ++nTurns;
//
//        if (bRandEval) {
//            if (LXUtils.random(0, nTurns) < 1) {
//                setNext(v,p,s);
//            }
//            return;
//
//        } else {
//            LXPoint p1 = v.getPoint(14);
//            LXPoint p2 = vDest.getPoint(0);
//            float d = (float)LXUtils.distance(p1.x, p1.y, p2.x, p2.y);
//
//            if (d <  minDist) {
//                minDist = d;
//                setNext(v,p,s);
//            }
//            if (d == minDist && LXUtils.random(0, 2) < 1) {
//                minDist = d;
//                setNext(v, p, s);
//            }
//        }
//    }
//
//    void evalTurn(dTurn t) {
//        if (t == null || t.pos0<< 12 <= pos) return;
//        evaluate(t.v, t.pos1, t.pos0);
//        evaluate(t.v.opp, 16 - t.pos1, t.pos0);
//    }
//
//    void pickNext() {
//        bRandEval = LXUtils.random(0, 0.05 + destSpeed) < 0.05;
//        minDist = 500;
//        nTurns = 0;
//
//        if (vCur != null) {
//            evaluate(vCur.c0, 0, 15);
//            evaluate(vCur.c1, 0, 15);
//            evaluate(vCur.c2, 0, 15);
//            evaluate(vCur.c3, 0, 15);
//            evalTurn(vCur.t0);
//            evalTurn(vCur.t1);
//            evalTurn(vCur.t2);
//            evalTurn(vCur.t3);
//        }
//    }
//
//    LXPoint p1, p2;
//    int i2;
//
//    int draw(int nAmount, SLPattern pat) {
//        if (pat == null) return 0;
//        if (vCur == null) return 0;
//
//        int nFrom = (pos) >> 12;
//        int nMv = Math.min(nAmount, posStop - pos);
//        int nTo = Math.min(14,(pos + nMv) >> 12);
//        dVertex v  = vCur;
//
//        // if (dDebug) {
//        //   p1 = v.getPoint(nFrom);
//        //   float d = (p2 == null ? 0 : pointDist(p1, p2));
//        //   if (d>5) {
//        //     System.out.print("too wide! quitting: " + d);
//        //     exit();
//        //   }
//        // }
//
//        for (int i = nFrom; i <= nTo; i++) {
//            pat.getColors()[v.ci + v.dir*i] = clr;
//        }
//
//        if (v.same != null) {
//            for (int i = nFrom; i <= nTo; i++) {
//                pat.getColors()[v.same.ci + v.same.dir*i] = clr;
//            }
//        }
//
//        // if (dDebug) {
//        //   p2 = v.getPoint(nTo); i2 = nTo;
//        // }
//
//        pos += nMv;
//        return nAmount - nMv;
//    }
//}
//
//class dVertex {
//    // connections on the cube
//    dVertex c0;
//    dVertex c1;
//    dVertex c2;
//    dVertex c3;
//
//    // opposite - same strip, opposite direction
//    dVertex opposite;
//    dVertex same;
//
//    // same - same strut, diff strip, dir
//    dTurn t0;
//    dTurn t1;
//    dTurn t2;
//    dTurn t3;
//
//    Strip strip;
//    int dir, ci;
//
//    dVertex(Strip strip, LXPoint p) {
//        this.strip = strip;
//        ci = p.index;
//    }
//
//    LXPoint getPoint(int i) {
//        return strip.points[dir > 0 ? i : 14 - i];
//    }
//
//    void setOpposite(dVertex opposite) {
//        this.opposite = opposite;
//        dir = (ci < opposite.ci ? 1 : -1);
//    }
//}
//
//class dTurn {
//    dVertex v;
//    int pos0, pos1;
//
//    dTurn(int _pos0, dVertex _v, int _pos1) {
//        v = _v;
//        pos0 = _pos0;
//        pos1 = _pos1;
//    }
//}
//
//class dPixel {
//    public dVertex v;
//    public int pos;
//
//    public dPixel(dVertex _v, int _pos) {
//        v = _v;
//        pos = _pos;
//    }
//}
//
//class dLattice {
//    StripsModel<Strip> model;
//
//    void addTurn(dVertex v0, int pos0, dVertex v1, int pos1) {
//        dTurn t = new dTurn(pos0, v1, pos1);
//        if (v0.t0 == null) { v0.t0=t; return; }
//        if (v0.t1 == null) { v0.t1=t; return; }
//        if (v0.t2 == null) { v0.t2=t; return; }
//        if (v0.t3 == null) { v0.t3=t; return; }
//    }
//
//    float dist2(Strip s1, int pos1, Strip s2, int pos2) {
//        LXPoint p1 = s1.points[pos1];
//        LXPoint p2 = s2.points[pos2];
//        return dist(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
//    }
//
//    float distToPoint(LXPoint p1, float x, float y, float z) {
//        return dist(p1.x, p1.y, p1.z, x, y, z);
//    }
//
//    // same strut, same direction
//    boolean sameSame(Strip s1, Strip s2) {
//        return Math.max(
//            dist2(s1, 0, s2, 0),
//            dist2(s1, 14, s2, 14)
//        ) < 5;
//    }
//
//    // same strut, opp direction
//    boolean sameOpp(Strip s1, Strip s2) {
//        return Math.max(dist2(s1, 0, s2, 14), dist2(s1, 14, s2, 0)) < 5;
//    }
//
//    // 2 strips on same strut
//    boolean onSameStrip(Strip s1, Strip s2) {
//        return sameSame(s1, s2) || sameOpp(s1, s2);
//    }
//
//    void addJoint(dVertex v1, dVertex v2) {
//        // should probably replace parallel but further with the new one
//        if (v1.c0 != null && onSameStrip(v2.strip, v1.c0.strip)) return;
//        if (v1.c1 != null && onSameStrip(v2.strip, v1.c1.strip)) return;
//        if (v1.c2 != null && onSameStrip(v2.strip, v1.c2.strip)) return;
//        if (v1.c3 != null && onSameStrip(v2.strip, v1.c3.strip)) return;
//
//        if      (v1.c0 == null) v1.c0 = v2;
//        else if (v1.c1 == null) v1.c1 = v2;
//        else if (v1.c2 == null) v1.c2 = v2;
//        else if (v1.c3 == null) v1.c3 = v2;
//    }
//
//    dVertex v0(Strip strip) {
//        return (dVertex)strip.obj1;
//    }
//
//    dVertex v1(Strip strip) {
//        return (dVertex)strip.obj2;
//    }
//
//    dPixel getClosest(LXVector p) {
//        dVertex v = null;
//        int pos = 0;
//        float d = 2500;
//
//        for (Strip strip : model.getStrips()) {
//            float nd = distToPoint(s.points[0], p.x, p.y, p.z);
//
//            if (nd < d) {
//                v = v0(strip);
//                d = nd;
//                pos = 0;
//            }
//
//            if (nd > 30) continue;
//
//            for (int k = 1; k <= 14; k++) {
//                nd = distToPoint(strip.points[k], p.x, p.y, p.z);
//
//                if (nd < d) {
//                    v = v0(strip);
//                    d = nd;
//                    pos = k;
//                }
//            }
//        }
//
//        return LXUtils.random(0, 2) < 1 ? new dPixel(v, pos) : new dPixel(v.opp, 14 - pos);
//    }
//
//    public dLattice(StripsModel<Strip> model) {
//        this.model = model;
//
//        List<Strip> strips = model.getStrips();
//
//        for (Strip strip : strips) {
//            dVertex vertex1 = new dVertex(strip, strip.points[0]);
//            strip.obj1 = vertex0;
//
//            dVertex vertex1 = new dVertex(strip, strip.points[14]);
//            strip.obj2 = vrtx1;
//
//            vrtx0.setOpposite(vrtx1);
//            vrtx1.setOpposite(vrtx0);
//        }
//
//        for (Strip strip1 : strips) {
//            for (Strip strip2 : strips) {
//                if (strip1.points[0].index < strip2.points[0].index) continue;
//                int c = 0;
//
//                // parallel
//                if (sameSame(strip1, strip2)) {
//                    v0(strip1).same = v0(strip2);
//                    v1(strip1).same = v1(strip2);
//                    v0(strip2).same = v0(strip1);
//                    v1(strip2).same = v1(strip1);
//                    continue;
//                }
//
//                // parallel
//                if (sameOpp(strip1, strip2)) {
//                    v0(strip1).same = v1(strip2);
//                    v1(strip1).same = v0(strip2);
//                    v0(strip2).same = v1(strip1);
//                    v1(strip2).same = v0(strip1);
//                    continue;
//                }
//
//                if (dist2(strip1, 0, strip2, 0) < 10) { c++; addJoint(v1(strip1), v0(strip2)); addJoint(v1(strip2), v0(strip1)); }
//                if (dist2(strip1, 0, strip2,14) < 10) { c++; addJoint(v1(strip1), v1(strip2)); addJoint(v0(strip2), v0(strip1)); }
//                if (dist2(strip1,14, strip2, 0) < 10) { c++; addJoint(v0(strip1), v0(strip2)); addJoint(v1(strip2), v1(strip1)); }
//                if (dist2(strip1,14, strip2,14) < 10) { c++; addJoint(v0(strip1), v1(strip2)); addJoint(v0(strip2), v1(strip1)); }
//                if (c > 0) continue;
//
//                // Are they touching at all?
//                int pos1 = 0, pos2 = 0; float d = 500;
//
//                while (pos1 < 14 || pos2 < 14) {
//                    float oldD = d;
//
//                    if (pos1 < 14) {
//                        float d2 = dist2(s1, pos1+1, strip2, pos2+0);
//                        if (d2 < d) {
//                            d = d2;
//                            pos1++;
//                        }
//                    }
//
//                    if (pos2 < 14) {
//                        float d2 = dist2(s1, pos1+0, strip2, pos2+1);
//                        if (d2 < d) {
//                            d = d2;
//                            pos2++;
//                        }
//                    }
//
//                    if (d > 50  || oldD == d) break;
//                }
//
//                if (d > 5) continue;
//                addTurn(v0(strip1), pos1, v0(strip2), pos2);
//                addTurn(v1(strip1), 14 - pos1, v0(strip2), pos2);
//                addTurn(v0(strip2), pos2, v0(strip1), pos1);
//                addTurn(v1(strip2), 14 - pos2, v0(strip1), pos1);
//            }
//        }
//    }
//}
