package com.symmetrylabs.patterns;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.midi.*;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.transform.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.NoiseUtils;
import com.symmetrylabs.util.MathUtils;

// int         NumApcRows=4, NumApcCols=8;
// // public class Pick {
// //     public int  NumPicks, Default   ,
// //             CurRow  , CurCol    ,
// //             StartRow, EndRow    ;
// //     String  tag     , Desc[]    ;

// //     public Pick (String label, int _Def, int _Num,  int nStart, String d[]) {
// //         NumPicks    = _Num;     Default = _Def;
// //         StartRow    = nStart;   EndRow  = StartRow + (float)Math.floor((NumPicks-1) / NumApcCols);
// //         tag         = label;    Desc    = d;
// //         reset();
// //     }

// //     int     Cur()           { return (CurRow-StartRow)*NumApcCols + CurCol;                 }
// //     String  CurDesc()       { return Desc[Cur()]; }
// //     void    reset()         { CurCol = Default % NumApcCols; CurRow = StartRow + Default / NumApcCols; }

// //     boolean set(int r, int c)   {
// //         if (!btwn(r,StartRow,EndRow) || !btwn(c,0,NumApcCols-1) ||
// //             !btwn((r-StartRow)*NumApcCols + c,0,NumPicks-1))    return false;
// //         CurRow=r; CurCol=c;                                     return true;
// //     }
// // }
public class DPat extends LXPattern {

        public static class NDat {
                float   xz, yz, zz, hue, speed, angle, den;
                float   xoff,yoff,zoff;
                float   sinAngle, cosAngle;
                boolean isActive;
                NDat          () { isActive=false; }
                boolean Active() { return isActive; }
                void    set     (float _hue, float _xz, float _yz, float _zz, float _den, float _speed, float _angle) {
                        isActive = true;
                        hue=_hue; xz=_xz; yz=_yz; zz =_zz; den=_den; speed=_speed; angle=_angle;
                        xoff = MathUtils.random(100e3f); yoff = MathUtils.random(100e3f); zoff = MathUtils.random(100e3f);

                }
        }

        public static class DBool {
                boolean def, b;
                String  tag;
                int     row, col;
                void    reset() { b = def; }
                boolean set (int r, int c, boolean val) { if (r != row || c != col) return false; b = val; return true; }
                boolean toggle(int r, int c) { if (r != row || c != col) return false; b = !b; return true; }
                DBool(String _tag, boolean _def, int _row, int _col) {
                        def = _def; b = _def; tag = _tag; row = _row; col = _col;
                }
        }

        public static final double DEG_TO_RAD = Math.PI / 180d;
        public static final double RAD_TO_DEG = 180d / Math.PI;

        //List<Pick>   picks  = new ArrayList<Pick>  ();
        List<DBool>  bools  = new ArrayList<DBool> ();
        LXVector pTrans = new LXVector(0, 0, 0);
        LXVector mMax, mCtr, mHalf;

        LXMidiOutput  APCOut;
        LXMidiOutput MidiFighterTwisterOut;
        int         nMaxRow     = 53;
        float       LastJog = -1;
        float[]     xWaveNz, yWaveNz;
        int         nPoint  , nPoints;
        LXVector     xyzJog = new LXVector(0, 0, 0), modmin;

        float           NoiseMove   = MathUtils.random(10000);
        CompoundParameter  pSpark, pWave, pRotX, pRotY, pRotZ, pSpin, pTransX, pTransY;
        BooleanParameter            pXsym, pYsym, pRsym, pXdup, pXtrip, pJog, pGrey;

        float lxh()                           { return palette.getHuef(); }
        int   c1c(float a)                    { return Math.round(100 * MathUtils.constrain(a,0,1)); }
        float interpWv(float i, float[] vals) { return interp(i - (float)Math.floor(i), vals[(int)Math.floor(i)], vals[(int)Math.ceil(i)]); }
        void  setNorm(LXVector vec)           { vec.set(vec.x/mMax.x, vec.y/mMax.y, vec.z/mMax.z); }
        void  setRand(LXVector vec)           { vec.set(MathUtils.random(mMax.x), MathUtils.random(mMax.y), MathUtils.random(mMax.z)); }
        void  setVec(LXVector vec, LXPoint p) { vec.set(p.x, p.y, p.z); }
        void  interpolate(float i, LXVector a, LXVector b)  { a.set(interp(i,a.x,b.x), interp(i,a.y,b.y), interp(i,a.z,b.z)); }
        void  StartRun(double deltaMs)        { }
        float val(CompoundParameter p)        { return p.getValuef(); }
        int   CalcPoint(LXVector p)           { return lx.hsb(0,0,0); }
        int   blend3(int c1, int c2, int c3)  { return LXColor.blend(c1, LXColor.blend(c2, c3, LXColor.Blend.ADD), LXColor.Blend.ADD); }

        void  rotateZ(LXVector p, LXVector o, float nSin, float nCos) { p.set(    nCos*(p.x-o.x) - nSin*(p.y-o.y) + o.x    , nSin*(p.x-o.x) + nCos*(p.y-o.y) + o.y,p.z); }
        void  rotateX(LXVector p, LXVector o, float nSin, float nCos) { p.set(p.x,nCos*(p.y-o.y) - nSin*(p.z-o.z) + o.y    , nSin*(p.y-o.y) + nCos*(p.z-o.z) + o.z    ); }
        void  rotateY(LXVector p, LXVector o, float nSin, float nCos) { p.set(    nSin*(p.z-o.z) + nCos*(p.x-o.x) + o.x,p.y, nCos*(p.z-o.z) - nSin*(p.x-o.x) + o.z    ); }

        CompoundParameter  addParam(String label, double value)    { CompoundParameter p = new CompoundParameter(label, value); addParameter(p); return p; }
        CompoundParameter  addParam(String label, double value, double min, double max)  { CompoundParameter p2 = new CompoundParameter(label, value, min, max); addParameter(p2); return p2; }
        LXVector     vT1 = new LXVector(0, 0, 0), vT2 = new LXVector(0, 0, 0);
        float       calcCone (LXVector v1, LXVector v2, LXVector c)    {   vT1.set(v1); vT2.set(v2); subVecs(vT1, c); subVecs(vT2, c);
                                                                                                                                        return (float)(RAD_TO_DEG * LXVector.angleBetween(vT1,vT2)); }

        // Pick        addPick(String name, int def, int _max, String[] desc) {
        //     Pick P      = new Pick(name, def, _max+1, nMaxRow, desc);
        //     nMaxRow     = P.EndRow + 1;
        //     picks.add(P);
        //     return P;
        // }

        // float interp(float v1, float v2, float amt) {
        //     return MathUtils.lerp(v1, v2, amt);
        // }

        boolean btwn    (int        a,int    b,int      c)      { return a >= b && a <= c;  }
        boolean btwn    (double     a,double b,double   c)      { return a >= b && a <= c;  }
        float   interp  (float a, float b, float c) { return (1-a)*b + a*c; }
        float   randctr (float a) { return MathUtils.random(0, a) - a * .5f; }
        float   min4     (float a, float b, float c, float d) { return Math.min(Math.min(a,b),Math.min(c,d));   }
        float   pointDist(LXPoint p1, LXPoint p2) { return MathUtils.dist(p1.x,p1.y,p1.z,p2.x,p2.y,p2.z);     }
        float   xyDist   (LXPoint p1, LXPoint p2) { return MathUtils.dist(p1.x,p1.y,p2.x,p2.y);               }
        float   distToSeg(float x, float y, float x1, float y1, float x2, float y2) {
                float A             = x - x1, B = y - y1, C = x2 - x1, D = y2 - y1;
                float dot           = A * C + B * D, len_sq = C * C + D * D;
                float xx, yy,param  = dot / len_sq;

                if (param < 0 || (x1 == x2 && y1 == y2)) {  xx = x1; yy = y1; }
                else if (param > 1) {                       xx = x2; yy = y2; }
                else {                                      xx = x1 + param * C;
                                                                                                        yy = y1 + param * D; }
                float dx = x - xx, dy = y - yy;
                return (float)Math.sqrt(dx * dx + dy * dy);
        }

        LXVector subVecs(LXVector a, LXVector b) {
            return a.add(-b.x, -b.y, -b.z);
        }

/* Pre PatternControls UI
    boolean noteOn(LXMidiNote note) {
        if (handleNote(note)) {
                updateLights();
                return true;
        } else {
                return false;
        }
        }

        boolean handleNote(LXMidiNote note) {
                int row = note.getPitch(), col = note.getChannel();
                for (int i=0; i<picks.size(); i++) if (picks.get(i).set(row, col))          { presetManager.dirty(this); return true; }
                for (int i=0; i<bools.size(); i++) if (bools.get(i).toggle(row, col))   { presetManager.dirty(this); return true; }
                println("row: " + row + "  col:   " + col);
                return false;
        }

        void        onInactive()            { uiDebugText.setText(""); }
*/


    // boolean noteOn(LXMidiNote note) {return false;}

    // boolean handleNote(LXMidiNote note) {return false;}

        @Override
        public void onInactive() {}

        protected void onReset() {
                // for (int i=0; i<bools .size(); i++) bools.get(i).reset();
                // for (int i=0; i<picks .size(); i++) picks.get(i).reset();
                //presetManager.dirty(this);
        //  updateLights(); now handled by patternControl UI
        }

        DPat(LX lx) {
                super(lx);

                pSpark      =   addParam("Sprk",  0);
                pWave       =   addParam("Wave",  0);
                pTransX     =   addParam("TrnX", .5);
                pTransY     =   addParam("TrnY", .5);
                pRotX       =   addParam("RotX", .5);
                pRotY       =   addParam("RotY", .5);
                pRotZ       =   addParam("RotZ", .5);
                pSpin       =   addParam("Spin", .5);


                pXsym = new BooleanParameter("X-SYM");
                pYsym = new BooleanParameter("Y-SYM");
                pRsym = new BooleanParameter("R-SYM");
                pXdup = new BooleanParameter("X-DUP");
                pJog = new BooleanParameter("JOG");
                pGrey = new BooleanParameter("GREY");

                // addNonKnobParameter(pXsym);
                // addNonKnobParameter(pYsym);
                // addNonKnobParameter(pRsym);
                // addNonKnobParameter(pXdup);
                // addNonKnobParameter(pJog);
                // addNonKnobParameter(pGrey);

                nPoints     =   model.points.length;


                //addMultipleParameterUIRow("Bools",pXsym,pYsym,pRsym,pXdup,pJog,pGrey);

                modmin      =   new LXVector(model.xMin, model.yMin, model.zMin);
                mMax        =   new LXVector(model.xMax, model.yMax, model.zMax); subVecs(mMax, modmin);
                mCtr        =   new LXVector(0, 0, 0); mCtr.set(mMax); mCtr.mult(.5f);
                mHalf       =   new LXVector(.5f, .5f, .5f);
                xWaveNz     =   new float[(int)Math.ceil(mMax.y) + 1];
                yWaveNz     =   new float[(int)Math.ceil(mMax.x) + 1];
                //println (model.xMin + " " + model.yMin + " " +  model.zMin);
                //println (model.xMax + " " + model.yMax + " " +  model.zMax);
            //for (MidiOutputDevice o: RWMidi.getOutputDevices()) { if (o.toString().contains("APC")) { APCOut = o.createOutput(); break;}}
        }

        float spin() {
            float raw = val(pSpin);
            if (raw <= 0.45) {
                return raw + 0.05f;
            }
            else if (raw >= 0.55) {
                return raw - 0.05f;
            }
            return 0.5f;
        }

        // void setAPCOutput(LXMidiOutput output) {
        //   APCOut = output;
        // }

        // void setMidiFighterTwisterOutput(LXMidiOutput output) {
        //     MidiFighterTwisterOut = output;
        // }

        //Pre patternControls UI
        // void updateLights() {     if (APCOut == null ) return;
        //     for (int i = 0; i < NumApcRows; ++i)
        //         for (int j = 0; j < 8; ++j)         APCOut.sendNoteOn(j, 53+i,  0);
        //     for (int i=0; i<picks .size(); i++)     APCOut.sendNoteOn(picks.get(i).CurCol, picks.get(i).CurRow, 3);
        //     for (int i=0; i<bools .size(); i++)     if (bools.get(i).b)     APCOut.sendNoteOn   (bools.get(i).col, bools.get(i).row, 1);
        //                                             else                    APCOut.sendNoteOff  (bools.get(i).col, bools.get(i).row, 0);
        // }

        void updateLights() {}

        @Override
        public void run(double deltaMs) {
                if (deltaMs > 100) return;

                /* pre patternControls UI
                    if (this == midiEngine.getFocusedPattern()) {
                        String Text1="", Text2="";
                        for (int i=0; i<bools.size(); i++) if (bools.get(i).b) Text1 += " " + bools.get(i).tag       + "   ";
                        for (int i=0; i<picks.size(); i++) Text1 += picks.get(i).tag + ": " + picks.get(i).CurDesc() + "   ";
                        //uiDebugText.setText(Text1, Text2);
                }*/

                NoiseMove = (float)((NoiseMove + deltaMs) % 1e7);
                StartRun(deltaMs);
                LXVector P       = new LXVector(0, 0, 0), tP = new LXVector(0, 0, 0), pSave = new LXVector(0, 0, 0);
                pTrans.set(val(pTransX)*200-100, val(pTransY)*100-50,0);
                nPoint  = 0;

                if (pJog.getValueb()) {
                        float tRamp = (lx.tempo.rampf() % .25f);
                        if (tRamp < LastJog) xyzJog.set(randctr(mMax.x * .2f), randctr(mMax.y * .2f), randctr(mMax.z * .2f));
                        LastJog = tRamp;
                }

                // precalculate this stuff
                float wvAmp = val(pWave), sprk = val(pSpark);
                if (wvAmp > 0) {
                        for (int i = 0; i < (int)Math.ceil(mMax.x) + 1; i++)
                                yWaveNz[i] = wvAmp * (NoiseUtils.noise(i / (mMax.x * .3f) - (2e3f + NoiseMove) / 1500f) - .5f) * (mMax.y / 2f);

                        for (int i = 0; i < (int)Math.ceil(mMax.y) + 1; i++)
                                xWaveNz[i] = wvAmp * (NoiseUtils.noise(i / (mMax.y * .3f) - (1e3f + NoiseMove) / 1500f) - .5f) * (mMax.x / 2f);
                }

                for (LXPoint p : model.points) { nPoint++;
                        setVec(P,p);
                        subVecs(P, modmin);
                        subVecs(P, pTrans);
                        if (sprk  > 0) {P.y += sprk*randctr(50); P.x += sprk*randctr(50); P.z += sprk*randctr(50); }
                        if (wvAmp > 0)  P.y += interpWv(p.x-modmin.x, yWaveNz);
                        if (wvAmp > 0)  P.x += interpWv(p.y-modmin.y, xWaveNz);
                        if (pJog.getValueb())       P.add(xyzJog);


                        int cNew, cOld = colors[p.index];
                                                        { tP.set(P);                                    cNew = CalcPoint(tP);                           }
                        if (pXsym.getValueb())  { tP.set(mMax.x-P.x,P.y,P.z);                   cNew = LXColor.blend(cNew, CalcPoint(tP), LXColor.Blend.ADD); }
                        if (pYsym.getValueb())  { tP.set(P.x,mMax.y-P.y,P.z);                   cNew = LXColor.blend(cNew, CalcPoint(tP), LXColor.Blend.ADD); }
                        if (pRsym.getValueb())  { tP.set(mMax.x-P.x,mMax.y-P.y,mMax.z-P.z);     cNew = LXColor.blend(cNew, CalcPoint(tP), LXColor.Blend.ADD); }
                        if (pXdup.getValueb())  { tP.set((P.x+mMax.x*.5f)%mMax.x,P.y,P.z);       cNew = LXColor.blend(cNew, CalcPoint(tP), LXColor.Blend.ADD); }
                        if (pGrey.getValueb())  { cNew = lx.hsb(0, 0, LXColor.b(cNew)); }
                        colors[p.index] = cNew;
                }
        }
}
// //----------------------------------------------------------------------------------------------------------------------------------
// class dTurn {
//     dVertex v;
//     int pos0, pos1;
//     dTurn(int _pos0, dVertex _v, int _pos1) { v = _v; pos0 = _pos0; pos1 = _pos1; }
// }

// class dVertex {
//     dVertex c0, c1, c2, c3,     // connections on the cube
//             opp, same;          // opp - same strip, opp direction
//                                 // same - same strut, diff strip, dir
//     dTurn   t0, t1, t2, t3;
//     Strip   s;
//     int     dir, ci;        // dir -- 1 or -1.
//                             // ci  -- color index

//     dVertex(Strip _s, LXPoint _p)  { s = _s; ci  = _p.index; }
//     LXPoint     getPoint(int i)      { return s.points.get(dir>0 ? i : 14-i);  }
//     void    setOpp(dVertex _opp) { opp = _opp; dir = (ci < opp.ci ? 1 : -1); }
// }
// //----------------------------------------------------------------------------------------------------------------------------------
// public class dPixel   { public dVertex v; public int pos; public dPixel(dVertex _v, int _pos) { v=_v; pos=_pos; } }
// public class dLattice {
//     void    addTurn  (dVertex v0, int pos0, dVertex v1, int pos1) { dTurn t = new dTurn(pos0, v1, pos1);
//                                                                     if (v0.t0 == null) { v0.t0=t; return; }
//                                                                     if (v0.t1 == null) { v0.t1=t; return; }
//                                                                     if (v0.t2 == null) { v0.t2=t; return; }
//                                                                     if (v0.t3 == null) { v0.t3=t; return; }
//                                                                 }
//     float   dist2    (Strip s1, int pos1, Strip s2, int pos2)   {   return pointDist(s1.points.get(pos1), s2.points.get(pos2)); }
//     float   pd2      (LXPoint p1, float x, float y, float z)        {   return MathUtils.dist(p1.x,p1.y,p1.z,x,y,z); }
//     boolean sameSame (Strip s1, Strip s2)                       {   return Math.max(dist2(s1, 0, s2, 0), dist2(s1,14, s2,14)) < 5 ;  }   // same strut, same direction
//     boolean sameOpp  (Strip s1, Strip s2)                       {   return Math.max(dist2(s1, 0, s2,14), dist2(s1,14, s2,0 )) < 5 ;  }   // same strut, opp direction
//     boolean sameBar  (Strip s1, Strip s2)                       {   return sameSame(s1,s2) || sameOpp(s1,s2);                   }   // 2 strips on same strut


//     void    addJoint (dVertex v1, dVertex v2) {
//         // should probably replace parallel but further with the new one
//         if (v1.c0 != null && sameBar(v2.s, v1.c0.s)) return;
//         if (v1.c1 != null && sameBar(v2.s, v1.c1.s)) return;
//         if (v1.c2 != null && sameBar(v2.s, v1.c2.s)) return;
//         if (v1.c3 != null && sameBar(v2.s, v1.c3.s)) return;

//         if      (v1.c0 == null) v1.c0 = v2;
//         else if (v1.c1 == null) v1.c1 = v2;
//         else if (v1.c2 == null) v1.c2 = v2;
//         else if (v1.c3 == null) v1.c3 = v2;
//     }

//     dVertex v0(Strip s) { return (dVertex)s.obj1; }
//     dVertex v1(Strip s) { return (dVertex)s.obj2; }

//     dPixel getClosest(LXVector p) {
//         dVertex v = null; int pos=0; float d = 2500;

//         for (Strip s : model.strips) {
//             float nd = pd2(s.points.get(0),p.x,p.y,p.z); if (nd < d) { v=v0(s); d=nd; pos=0; }
//             if (nd > 30) continue;
//             for (int k=1; k<=14; k++) {
//                 nd = pd2(s.points.get(k),p.x,p.y,p.z); if (nd < d) { v =v0(s); d=nd; pos=k; }
//             }
//         }
//         return MathUtils.random(2) < 1 ? new dPixel(v,pos) : new dPixel(v.opp,14-pos);
//     }

//     public dLattice() {
//         lattice=this;

//         for (Strip s  : model.strips) {
//             dVertex vrtx0 = new dVertex(s,s.points.get(0 )); s.obj1=vrtx0;
//             dVertex vrtx1 = new dVertex(s,s.points.get(14)); s.obj2=vrtx1;
//             vrtx0.setOpp(vrtx1); vrtx1.setOpp(vrtx0);
//         }

//         for (Strip s1 : model.strips) { for (Strip s2 : model.strips) {
//             if (s1.points.get(0).index < s2.points.get(0).index) continue;
//             int c=0;
//             if (sameSame(s1,s2))    {   v0(s1).same = v0(s2); v1(s1).same = v1(s2);
//                                         v0(s2).same = v0(s1); v1(s2).same = v1(s1); continue; } // parallel
//             if (sameOpp (s1,s2))    {   v0(s1).same = v1(s2); v1(s1).same = v0(s2);
//                                         v0(s2).same = v1(s1); v1(s2).same = v0(s1); continue; } // parallel
//             if (dist2(s1, 0, s2, 0) < 20) { c++; addJoint(v1(s1), v0(s2)); addJoint(v1(s2), v0(s1)); }
//             if (dist2(s1, 0, s2,14) < 20) { c++; addJoint(v1(s1), v1(s2)); addJoint(v0(s2), v0(s1)); }
//             if (dist2(s1,14, s2, 0) < 20) { c++; addJoint(v0(s1), v0(s2)); addJoint(v1(s2), v1(s1)); }
//             if (dist2(s1,14, s2,14) < 20) { c++; addJoint(v0(s1), v1(s2)); addJoint(v0(s2), v1(s1)); }
//             if (c>0) continue;

//             // Are they touching at all?
//             int pos1=0, pos2=0; float d = 500;

//             while (pos1 < 14 || pos2 < 14) {
//                 float oldD = d;
//                 if (pos1<14) { float d2 = dist2(s1, pos1+1, s2, pos2+0); if (d2 < d) { d=d2; pos1++; } }
//                 if (pos2<14) { float d2 = dist2(s1, pos1+0, s2, pos2+1); if (d2 < d) { d=d2; pos2++; } }
//                 if (d > 50  || oldD == d) break ;
//             }

//             if (d>5) continue;
//             addTurn(v0(s1), pos1, v0(s2), pos2); addTurn(v1(s1), 14-pos1, v0(s2), pos2);
//             addTurn(v0(s2), pos2, v0(s1), pos1); addTurn(v1(s2), 14-pos2, v0(s1), pos1);
//         }}
//     }
// }

// public dLattice lattice;

// boolean latticeInited;
// List<Runnable> latticeInitedCallbacks = new ArrayList<Runnable>();

// public void initLattice() {
//     initLattice(null);
// }

// public void initLattice(Runnable callback) {
//     if (!latticeInited) {
//         latticeInited = true;
//         new Thread() {
//             public void run() {
//                 final dLattice l = new dLattice();
//                 dispatcher.dispatchEngine(new Runnable() {
//                     public void run() {
//                         lattice = l;
//                         for (Runnable callback : latticeInitedCallbacks) {
//                             callback.run();
//                         }
//                         latticeInitedCallbacks.clear();
//                     }
//                 });
//             }
//         }.start();
//     }
//     if (callback != null) {
//         if (isLatticeInited()) {
//             callback.run();
//         } else {
//             latticeInitedCallbacks.add(callback);
//         }
//     }
// }

// public boolean isLatticeInited() {
//     return lattice != null;
// }
// //----------------------------------------------------------------------------------------------------------------------------------
