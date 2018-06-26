package com.symmetrylabs.slstudio.pattern.base;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.util.NoiseUtils;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PImage;
import processing.core.PVector;

import static processing.core.PConstants.ADD;

import java.util.ArrayList;
import java.util.SplittableRandom;

import static com.symmetrylabs.util.MathUtils.*;
import static processing.core.PVector.angleBetween;
//import static sun.tools.java.Constants.ADD;

public abstract class APat extends SLPattern<SLModel>

{
//    ArrayList<Pick> picks = new ArrayList<Pick> ();
    ArrayList<DPat.DBool> bools = new ArrayList<DPat.DBool> ();

    PVector mMax, mCtr, mHalf;

    LXMidiOutput APCOut;
    LXMidiOutput TwisterOut;
    int nMaxRow = 53;
    float LastJog = -1;
    float[] xWaveNz, yWaveNz;
    int nPoint , nPoints;
    PVector xyzJog = new PVector(), modmin;
    public SplittableRandom splittableRandom = new SplittableRandom();

    float NoiseMove = random(10000);
    CompoundParameter pSpark, pWave, pRotX, pRotY, pRotZ, pSpin,pSpinX, pSpinY,  pTransX, pTransY;
    DPat.DBool pXsym, pYsym, pRsym, pXdup, pXtrip, pJog, pGrey;

    float lxh () { return palette.getHuef(); }
    int c1c (float a) { return round(100*constrain(a,0,1)); }
    float interpWv(float i, float[] vals) { return interp(i-floor(i), vals[floor(i)], vals[ceil(i)]); }
    void setNorm (PVector vec) { vec.set(vec.x/mMax.x, vec.y/mMax.y, vec.z/mMax.z); }
    void setRand (PVector vec) { vec.set(random(mMax.x), random(mMax.y), random(mMax.z)); }
    void setVec (PVector vec, LXPoint p) { vec.set(p.x, p.y, p.z); }
    void interpolate(float i, PVector a, PVector b) { a.set(interp(i,a.x,b.x), interp(i,a.y,b.y), interp(i,a.z,b.z)); }
    void StartRun(double deltaMs) { }
    float val (CompoundParameter p) { return p.getValuef(); }
    int CalcPoint(PVector p) { return lx.hsb(0,0,0); }
    int blend3(int c1, int c2, int c3) { return PImage.blendColor(c1, PImage.blendColor(c2,c3,ADD),ADD); }

    void rotateZ (PVector p, PVector o, float nSin, float nCos) { p.set( nCos*(p.x-o.x) - nSin*(p.y-o.y) + o.x , nSin*(p.x-o.x) + nCos*(p.y-o.y) + o.y,p.z); }
    void rotateX (PVector p, PVector o, float nSin, float nCos) { p.set(p.x,nCos*(p.y-o.y) - nSin*(p.z-o.z) + o.y , nSin*(p.y-o.y) + nCos*(p.z-o.z) + o.z ); }
    void rotateY (PVector p, PVector o, float nSin, float nCos) { p.set( nSin*(p.z-o.z) + nCos*(p.x-o.x) + o.x,p.y, nCos*(p.z-o.z) - nSin*(p.x-o.x) + o.z ); }

    CompoundParameter addParam(String label, double value) { CompoundParameter p = new CompoundParameter(label, value); addParameter(p); return p; }



    protected boolean btwn(int a, int b, int c) {
        return a >= b && a <= c;
    }

    protected boolean btwn(double a, double b, double c) {
        return a >= b && a <= c;
    }

    protected float interp(float a, float b, float c) {
        return (1 - a) * b + a * c;
    }

    protected float randctr(float a) {
        return (float) (splittableRandom.nextDouble((double) a) - a * 0.5f);
    }


    PVector vT1 = new PVector(), vT2 = new PVector();
    float calcCone (PVector v1, PVector v2, PVector c) { vT1.set(v1); vT2.set(v2); vT1.sub(c); vT2.sub(c);
        return degrees(angleBetween(vT1,vT2)); }

//    Pick addPick(String name, int def, int _max, String[] desc) {
//        Pick P = new Pick(name, def, _max+1, nMaxRow, desc);
//        nMaxRow = P.EndRow + 1;
//        picks.add(P);
//        return P;
//    }

//    boolean noteOff(LXMidiNote note ) {
//        int row = note.getPitch(), col = note.getChannel();
//        for (int i=0; i<bools.size(); i++) if (bools.get(i).set(row, col, false)) { presetManager.dirty(this); return true; }
//        updateLights(); return false;
//    }
//
//    boolean noteOn(LXMidiNote note) {
//        int row = note.getPitch(), col = note.getChannel();
//        for (int i=0; i<picks.size(); i++) if (picks.get(i).set(row, col)) { presetManager.dirty(this); return true; }
//        for (int i=0; i<bools.size(); i++) if (bools.get(i).set(row, col, true)) { presetManager.dirty(this); return true; }
//        System.out.println("row: " + row + " col: " + col); return false;
//    }

//    void onInactive() { uiDebugText.setText(""); }
    void onReset() {
//        for (int i=0; i<bools .size(); i++) bools.get(i).reset();
//        for (int i=0; i<picks .size(); i++) picks.get(i).reset();
//        presetManager.dirty(this);
//        updateLights();
    }

    public APat(LX lx) {
        super(lx);



        nPoints = model.points.length;
        pXsym = new DPat.DBool("X-SYM", false, 48, 0); bools.add(pXsym );
        pYsym = new DPat.DBool("Y-SYM", false, 48, 1); bools.add(pYsym );
        pRsym = new DPat.DBool("R-SYM", false, 48, 2); bools.add(pRsym );
        pXdup = new DPat.DBool("X-DUP", false, 48, 3); bools.add(pXdup );
        pJog = new DPat.DBool("JOG" , false, 48, 4); bools.add(pJog );
        pGrey = new DPat.DBool("GREY" , false, 48, 5); bools.add(pGrey );

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
        xWaveNz = new float[ceil(mMax.y)+1];
        yWaveNz = new float[ceil(mMax.x)+1];

        //println (model.xMin + " " + model.yMin + " " + model.zMin);
        //println (model.xMax + " " + model.yMax + " " + model.zMax);
        //for (MidiOutputDevice o: RWMidi.getOutputDevices()) { if (o.toString().contains("APC")) { APCOut = o.createOutput(); break;}}
    }

    float spin() {
        float raw = val(pSpin);
        if (raw <= 0.45) {
            return raw + 0.05f;
        } else if (raw >= 0.55) {
            return raw - 0.05f;
        }
        return 0.5f;
    }

    void setAPCOutput(LXMidiOutput output) {
        APCOut = output;
    }

    void setMidiFighterTwisterOutput(LXMidiOutput output){
        TwisterOut = output;

    }
//    void updateLights() { if (APCOut == null) return;
//        for (int i = 0; i < NumApcRows; ++i)
//            for (int j = 0; j < 8; ++j) APCOut.sendNoteOn(j, 53+i, 0);
//        for (int i=0; i<picks .size(); i++) APCOut.sendNoteOn(picks.get(i).CurCol, picks.get(i).CurRow, 3);
//        for (int i=0; i<bools .size(); i++) if (bools.get(i).b) APCOut.sendNoteOn (bools.get(i).col, bools.get(i).row, 1);
//        else APCOut.sendNoteOff (bools.get(i).col, bools.get(i).row, 0);
//    }

    //void run(double deltaMs) {
    @Override
    public void run(double deltaMs, PolyBuffer.Space space) {

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
            for (int i=0; i<ceil(mMax.x)+1; i++)
                yWaveNz[i] = wvAmp * (NoiseUtils.noise((float)(i/(mMax.x*.3f)-(2e3+ NoiseMove)/1500f)) - .5f) * (mMax.y/2f);

            for (int i=0; i<ceil(mMax.y)+1; i++)
            //    xWaveNz[i] = wvAmp * ( NoiseUtils.noise((float) (i / (mMax.y * .3f) - (1e3 + NoiseMove) / 1500.)) - .5) * (mMax.x/2.);
            xWaveNz[i] = wvAmp * (NoiseUtils.noise((float)(i/(mMax.y*.3f)-(1e3+NoiseMove)/1500f)) - .5f) * (mMax.x/2f);
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


