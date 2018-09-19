package com.symmetrylabs.slstudio.pattern.base;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.NoiseUtils;
import com.symmetrylabs.util.Utils;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PApplet;
import processing.core.PVector;
import processing.core.PConstants;
import java.util.*;

import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.modulator.Click;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;

import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.util.NoiseUtils;
import com.symmetrylabs.util.MathUtils;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.NoiseUtils;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.SplittableRandom;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PConstants.ADD;

public abstract class APat extends SLPattern<SLModel> {
    public class Pick {
        public int     NumPicks, Default    ,
            CurRow    , CurCol    ,
            StartRow, EndRow    ;
        String  tag        , Desc[]    ;
        int NumApcCols = 10;

        public Pick    (String label, int _Def, int _Num,     int nStart, String d[])    {
            NumPicks     = _Num;     Default = _Def;
            StartRow     = nStart;    EndRow    = StartRow + MathUtils.floor((NumPicks-1) / NumApcCols);
            tag            = label;     Desc     = d;
            reset();
        }

        boolean btwn      (int         a,int      b,int         c)        { return a >= b && a <= c;     }
        boolean btwn      (double     a,double b,double     c)        { return a >= b && a <= c;     }

        public int        Cur()              { return (CurRow-StartRow)*NumApcCols + CurCol;                    }
        public String    CurDesc()         { return Desc[Cur()]; }
        public void    reset()         { CurCol = Default % NumApcCols; CurRow    = StartRow + Default / NumApcCols; }

        public boolean set(int r, int c)    {
            if (!btwn(r,StartRow,EndRow) || !btwn(c,0,NumApcCols-1) ||
                !btwn((r-StartRow)*NumApcCols + c,0,NumPicks-1))     return false;
            CurRow=r; CurCol=c;                                     return true;
        }
    }

    public class DBool {
        boolean def, b;
        String    tag;
        int        row, col;
        public void     reset() { b = def; }
        public boolean set    (int r, int c, boolean val) { if (r != row || c != col) return false; b = val; return true; }
        public boolean toggle(int r, int c) { if (r != row || c != col) return false; b = !b; return true; }
        DBool(String _tag, boolean _def, int _row, int _col) {
            def = _def; b = _def; tag = _tag; row = _row; col = _col;
        }
    }


    ArrayList<Pick> picks = new ArrayList<Pick> ();
    ArrayList<DBool> bools = new ArrayList<DBool> ();

    PVector mMax, mCtr, mHalf;

    LXMidiOutput APCOut;
    LXMidiOutput TwisterOut;
    int nMaxRow = 53;
    float LastJog = -1;
    float[] xWaveNz, yWaveNz;
    int nPoint , nPoints;
    PVector xyzJog = new PVector(), modmin;

    float NoiseMove = MathUtils.random(10000);
    CompoundParameter pSpark, pWave, pRotX, pRotY, pRotZ, pSpin,pSpinX, pSpinY,  pTransX, pTransY;
    DBool pXsym, pYsym, pRsym, pXdup, pXtrip, pJog, pGrey;

    public float lxh () { return lx.palette.getHuef(); }
    public int c1c (float a) { return MathUtils.round(100*MathUtils.constrain(a,0,1)); }
    public float interpWv(float i, float[] vals) { return MathUtils.lerp(i-MathUtils.floor(i), vals[MathUtils.floor(i)], vals[MathUtils.ceil(i)]); }
    public void setNorm (PVector vec) { vec.set(vec.x/mMax.x, vec.y/mMax.y, vec.z/mMax.z); }
    public void setRand (PVector vec) { vec.set(MathUtils.random(mMax.x), MathUtils.random(mMax.y), MathUtils.random(mMax.z)); }
    public void setVec (PVector vec, LXPoint p) { vec.set(p.x, p.y, p.z); }
    public void interpolate(float i, PVector a, PVector b) { a.set(MathUtils.lerp(i,a.x,b.x), MathUtils.lerp(i,a.y,b.y), MathUtils.lerp(i,a.z,b.z)); }
    public void StartRun(double deltaMs) { }
    public float val (CompoundParameter p) { return p.getValuef(); }
    public int CalcPoint(PVector p) { return lx.hsb(0,0,0); }
    public int blend3(int c1, int c2, int c3) { return PImage.blendColor(c1, PImage.blendColor(c2,c3,ADD),ADD); }

    public void rotateZ (PVector p, PVector o, float nSin, float nCos) { p.set( nCos*(p.x-o.x) - nSin*(p.y-o.y) + o.x , nSin*(p.x-o.x) + nCos*(p.y-o.y) + o.y,p.z); }
    public void rotateX (PVector p, PVector o, float nSin, float nCos) { p.set(p.x,nCos*(p.y-o.y) - nSin*(p.z-o.z) + o.y , nSin*(p.y-o.y) + nCos*(p.z-o.z) + o.z ); }
    public void rotateY (PVector p, PVector o, float nSin, float nCos) { p.set( nSin*(p.z-o.z) + nCos*(p.x-o.x) + o.x,p.y, nCos*(p.z-o.z) - nSin*(p.x-o.x) + o.z ); }

    public CompoundParameter addParam(String label, double value) { CompoundParameter p = new CompoundParameter(label, value); addParameter(p); return p; }

    PVector vT1 = new PVector(), vT2 = new PVector();
    public float calcCone (PVector v1, PVector v2, PVector c) { vT1.set(v1); vT2.set(v2); vT1.sub(c); vT2.sub(c);
        return MathUtils.degrees(PVector.angleBetween(vT1,vT2)); }

    public Pick addPick(String name, int def, int _max, String[] desc) {
        Pick P = new Pick(name, def, _max+1, nMaxRow, desc);
        nMaxRow = P.EndRow + 1;
        picks.add(P);
        return P;
    }

//    public boolean noteOff(LXMidiNote note ) {
//        int row = note.getPitch(), col = note.getChannel();
//        for (int i=0; i<bools.size(); i++) if (bools.get(i).set(row, col, false)) { presetManager.dirty(this); return true; }
//        updateLights(); return false;
//    }

//    public boolean noteOn(LXMidiNote note) {
//        int row = note.getPitch(), col = note.getChannel();
//        for (int i=0; i<picks.size(); i++) if (picks.get(i).set(row, col)) { presetManager.dirty(this); return true; }
//        for (int i=0; i<bools.size(); i++) if (bools.get(i).set(row, col, true)) { presetManager.dirty(this); return true; }
//        println("row: " + row + " col: " + col); return false;
//    }

//    public void onInactive() { uiDebugText.setText(""); }
    public void onReset() {
        for (int i=0; i<bools .size(); i++) bools.get(i).reset();
        for (int i=0; i<picks .size(); i++) picks.get(i).reset();
//        presetManager.dirty(this);
        updateLights();
    }

    public APat(LX lx) {
        super(lx);



        nPoints = model.points.length;
        pXsym = new DBool("X-SYM", false, 48, 0); bools.add(pXsym );
        pYsym = new DBool("Y-SYM", false, 48, 1); bools.add(pYsym );
        pRsym = new DBool("R-SYM", false, 48, 2); bools.add(pRsym );
        pXdup = new DBool("X-DUP", false, 48, 3); bools.add(pXdup );
        pJog = new DBool("JOG" , false, 48, 4); bools.add(pJog );
        pGrey = new DBool("GREY" , false, 48, 5); bools.add(pGrey );

        // addNonKnobParameter(pXsym);
        // addNonKnobParameter(pYsym);
        // addNonKnobParameter(pRsym);
        // addNonKnobParameter(pXdup);
        // addNonKnobParameter(pJog);
        // addNonKnobParameter(pGrey);

        modmin = new PVector(model.xMin, model.yMin, model.zMin);
        mMax = new PVector(model.xMax, model.yMax, model.zMax); mMax.sub(modmin);
        mCtr = new PVector(); mCtr.set(mMax); mCtr.mult(.5f);
        mHalf = new PVector(.5f,.5f,.5f);
        xWaveNz = new float[MathUtils.ceil(mMax.y)+1];
        yWaveNz = new float[MathUtils.ceil(mMax.x)+1];

        //println (model.xMin + " " + model.yMin + " " + model.zMin);
        //println (model.xMax + " " + model.yMax + " " + model.zMax);
        //for (MidiOutputDevice o: RWMidi.getOutputDevices()) { if (o.toString().contains("APC")) { APCOut = o.createOutput(); break;}}
    }

    public float spin() {
        float raw = val(pSpin);
        if (raw <= 0.45f) {
            return raw + 0.05f;
        } else if (raw >= 0.55f) {
            return raw - 0.05f;
        }
        return 0.5f;
    }

    public void setAPCOutput(LXMidiOutput output) {
        APCOut = output;
    }

    public void setMidiFighterTwisterOutput(LXMidiOutput output){
        TwisterOut = output;

    }
    public void updateLights() { if (APCOut == null) return;
        for (int i = 0; i < 10; ++i)
            for (int j = 0; j < 8; ++j) APCOut.sendNoteOn(j, 53+i, 0);
        for (int i=0; i<picks .size(); i++) APCOut.sendNoteOn(picks.get(i).CurCol, picks.get(i).CurRow, 3);
        for (int i=0; i<bools .size(); i++) if (bools.get(i).b) APCOut.sendNoteOn (bools.get(i).col, bools.get(i).row, 1);
        else APCOut.sendNoteOff (bools.get(i).col, bools.get(i).row, 0);
    }

    protected float randctr(float a) {
        return ((MathUtils.random(1.0f) * 2) - 1) * a;
//        return (float) (splittableRandom.nextDouble((double) a) - a * 0.5f);
    }

    public void run(double deltaMs)
    {
        if (deltaMs > 100) return;

//        if (this == midiEngine.getFocusedPattern()) {
//            String Text1="", Text2="";
//            for (int i=0; i<bools.size(); i++) if (bools.get(i).b) Text1 += " " + bools.get(i).tag + " ";
//            for (int i=0; i<picks.size(); i++) Text1 += picks.get(i).tag + ": " + picks.get(i).CurDesc() + " ";
//            uiDebugText.setText(Text1, Text2);
//        }

        NoiseMove += deltaMs; NoiseMove = NoiseMove % 1e7f;
        StartRun (deltaMs);
        PVector P = new PVector(), tP = new PVector(), pSave = new PVector();
        PVector pTrans = new PVector(val(pTransX)*200-100, val(pTransY)*100-50,0);
        nPoint = 0;

        if (pJog.b) {
            float tRamp = (lx.tempo.rampf() % .25f);
            if (tRamp < LastJog) xyzJog.set(randctr(mMax.x*.2f), randctr(mMax.y*.2f), randctr(mMax.z*.2f));
            LastJog = tRamp;
        }

        // precalculate this stuff
        float wvAmp = val(pWave), sprk = val(pSpark);
        if (wvAmp > 0) {
            for (int i=0; i<MathUtils.ceil(mMax.x)+1; i++)
                yWaveNz[i] = wvAmp * (NoiseUtils.noise(i/(mMax.x*.3f)-(2e3f+NoiseMove)/1500.f) - .5f) * (mMax.y/2.f);

            for (int i=0; i<MathUtils.ceil(mMax.y)+1; i++)
                xWaveNz[i] = wvAmp * (NoiseUtils.noise(i/(mMax.y*.3f)-(1e3f+NoiseMove)/1500.f) - .5f) * (mMax.x/2.f);
        }

        for (LXPoint p : model.points) { nPoint++;
            setVec(P,p);
            P.sub(modmin);
            P.sub(pTrans);
            if (sprk > 0) {P.y += sprk*randctr(50); P.x += sprk*randctr(50); P.z += sprk*randctr(50); }
            if (wvAmp > 0) P.y += interpWv(p.x-modmin.x, yWaveNz);
            if (wvAmp > 0) P.x += interpWv(p.y-modmin.y, xWaveNz);
            if (pJog.b) P.add(xyzJog);


            int cNew, cOld = colors[p.index];
            { tP.set(P); cNew = CalcPoint(tP); }
            if (pXsym.b) { tP.set(mMax.x-P.x,P.y,P.z); cNew = PImage.blendColor(cNew, CalcPoint(tP), ADD); }
            if (pYsym.b) { tP.set(P.x,mMax.y-P.y,P.z); cNew = PImage.blendColor(cNew, CalcPoint(tP), ADD); }
            if (pRsym.b) { tP.set(mMax.x-P.x,mMax.y-P.y,mMax.z-P.z); cNew = PImage.blendColor(cNew, CalcPoint(tP), ADD); }
            if (pXdup.b) { tP.set((P.x+mMax.x*.5f)%mMax.x,P.y,P.z); cNew = PImage.blendColor(cNew, CalcPoint(tP), ADD); }
            if (pGrey.b) { cNew = lx.hsb(0, 0, LXColor.b(cNew)); }
            colors[p.index] = cNew;
        }
    }
}
