package com.symmetrylabs.slstudio.comments;// //----------------------------------------------------------------------------------------------------------------------------------
// int         NumApcRows=4, NumApcCols=8;

// // public class Pick {
// //     public int  NumPicks, Default   ,   
// //             CurRow  , CurCol    ,
// //             StartRow, EndRow    ;
// //     String  tag     , Desc[]    ;

// //     public Pick (String label, int _Def, int _Num,  int nStart, String d[]) {
// //         NumPicks    = _Num;     Default = _Def; 
// //         StartRow    = nStart;   EndRow  = StartRow + floor((NumPicks-1) / NumApcCols);
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
//     com.symmetrylabs.slstudio.model.Strip   s;
//     int     dir, ci;        // dir -- 1 or -1.
//                             // ci  -- color index

//     dVertex(com.symmetrylabs.slstudio.model.Strip _s, LXPoint _p)  { s = _s; ci  = _p.index; }
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
//     float   dist2    (com.symmetrylabs.slstudio.model.Strip s1, int pos1, com.symmetrylabs.slstudio.model.Strip s2, int pos2)   {   return pointDist(s1.points.get(pos1), s2.points.get(pos2)); }
//     float   pd2      (LXPoint p1, float x, float y, float z)        {   return dist(p1.x,p1.y,p1.z,x,y,z); }
//     boolean sameSame (com.symmetrylabs.slstudio.model.Strip s1, com.symmetrylabs.slstudio.model.Strip s2)                       {   return max(dist2(s1, 0, s2, 0), dist2(s1,14, s2,14)) < 5 ;  }   // same strut, same direction
//     boolean sameOpp  (com.symmetrylabs.slstudio.model.Strip s1, com.symmetrylabs.slstudio.model.Strip s2)                       {   return max(dist2(s1, 0, s2,14), dist2(s1,14, s2,0 )) < 5 ;  }   // same strut, opp direction
//     boolean sameBar  (com.symmetrylabs.slstudio.model.Strip s1, com.symmetrylabs.slstudio.model.Strip s2)                       {   return sameSame(s1,s2) || sameOpp(s1,s2);                   }   // 2 strips on same strut


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

//     dVertex v0(com.symmetrylabs.slstudio.model.Strip s) { return (dVertex)s.obj1; }
//     dVertex v1(com.symmetrylabs.slstudio.model.Strip s) { return (dVertex)s.obj2; }

//     dPixel getClosest(PVector p) {
//         dVertex v = null; int pos=0; float d = 2500;

//         for (com.symmetrylabs.slstudio.model.Strip s : model.strips) {
//             float nd = pd2(s.points.get(0),p.x,p.y,p.z); if (nd < d) { v=v0(s); d=nd; pos=0; }
//             if (nd > 30) continue;
//             for (int k=1; k<=14; k++) {
//                 nd = pd2(s.points.get(k),p.x,p.y,p.z); if (nd < d) { v =v0(s); d=nd; pos=k; }
//             }
//         }
//         return random(2) < 1 ? new dPixel(v,pos) : new dPixel(v.opp,14-pos);
//     }

//     public dLattice() {
//         lattice=this;

//         for (com.symmetrylabs.slstudio.model.Strip s  : model.strips) {
//             dVertex vrtx0 = new dVertex(s,s.points.get(0 )); s.obj1=vrtx0;
//             dVertex vrtx1 = new dVertex(s,s.points.get(14)); s.obj2=vrtx1;
//             vrtx0.setOpp(vrtx1); vrtx1.setOpp(vrtx0);
//         }

//         for (com.symmetrylabs.slstudio.model.Strip s1 : model.strips) { for (com.symmetrylabs.slstudio.model.Strip s2 : model.strips) {
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
