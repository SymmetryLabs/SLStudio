package com.symmetrylabs.slstudio.pattern;

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

import static processing.core.PApplet.*;
import static java.lang.Math.*;


public class TraktorPlayNeue extends DPat
{
    private boolean rhythmOn = false;
    private boolean leapOn = false;
    PVector position = new PVector();
    CompoundParameter gainf = new CompoundParameter("gain", .5, 0, 1);

//    void keyEvent(KeyEvent keyEvent) {
//
//        switch (keyEvent.getKey()) {
//
//            case 'r': rhythmOn = !rhythmOn; print(rhythmOn);
//            case 'O':   onReset();  println("reset"); break;
//            case 'L':  leapOn = !leapOn;   println(leapOn); break;
//
//        }
//
//
//    }

    public class rAngle {
        float   prvA, dstA, c;
        float   prvR, dstR, r;
        float   _cos, _sin, x, y;
        float   fixAngle  (float a, float b) { return a<b ?
            (MathUtils.abs(a-b) > MathUtils.abs(a+2*PConstants.PI-b) ? a : a+2*PConstants.PI) :
            (MathUtils.abs(a-b) > MathUtils.abs(a-2*PConstants.PI-b) ? a : a-2*PConstants.PI) ; }
        float getX(float r) { return mCtr.x + _cos*r; }
        float getY(float r) { return mCtr.y + _sin*r; }
        void  move()      { c     = interp(t,prvA,dstA);
            r     = interp(t,prvR,dstR);
            _cos  = cos(c);   _sin  = sin(c);
            x    = getX(r);  y     = getY(r);    }
        void  set()       { prvA  = dstA;   dstA  = random(2*PConstants.PI);   prvA = fixAngle(prvA, dstA);
            prvR  = dstR;   dstR  = random(mCtr.y);                 }
    }
    final int FRAME_WIDTH = 60;
    int currentdeltaMs;

    CompoundParameter speed = new CompoundParameter("SPD", 0.4);
    Click stepTimer = new Click("Step", speed.getValuef()*30);
    private float[] bass = new float[FRAME_WIDTH];
    private float[] treble = new float[FRAME_WIDTH];

    private int index = 0;
    int pcounter = 0;
    private GraphicMeter eq;
//    private GraphicEQ eq = null;

    CompoundParameter  pAmp, pRadius, pBounce;
    DiscreteParameter      pTimePattern, pTempoMult, pShape;

    ArrayList<rWave> waves = new ArrayList<rWave>(10);

    int   nBeats  =   0;
    float   t,amp,rad,bnc,zTheta=0, yTheta=0, xTheta=0;

    rAngle  a1    = new rAngle(), a2      = new rAngle(),
        a3    = new rAngle(), a4      = new rAngle();
    PVector cPrev   = new PVector(), cRand    = new PVector(),
        cMid  = new PVector(),
        theta   = new PVector(), thetaX = new PVector(),
        thetaY = new PVector(),
        tSin   = new PVector(), tSinX = new PVector(), tSinY = new PVector(),
        tCos  = new PVector(), tCosX = new PVector(), tCosY = new PVector(),
        cMidNorm   = new PVector();
    float LastBeat=3, LastMeasure=3;
    int   curRandTempo = 1, curRandTPat = 1;

    public TraktorPlayNeue(LX lx) {
        super(lx);
        // lx.addKeyEventHandler(parent);
        pRadius   = addParam("Rad"  , 1.4  , 0, 4  );
        pBounce   = addParam("Bnc"  , .0  );
        pAmp      = addParam("Amp"  , .2  );

        pTempoMult  = new DiscreteParameter ("TMult" , new String[] {"1x", "2x", "4x", "8x", "16x", "Rand" } );
        pTimePattern= new DiscreteParameter ("TPat" , new String[] {"Bnce", "Sin", "Roll", "Quan", "Acc", "Decc", "Slide", "Rand"} );
        pShape    = new DiscreteParameter ("Shape" , new String[] {"Line", "Tap", "V", "RndV",
            "Prmd", "Wings", "W2", "Clock",
            "Trngle", "Quad", "Sphr", "Cone",
            "Noise", "Wave", "?", "?"}            );
//        addSingleParameterUIRow(pTempoMult);
//        addSingleParameterUIRow(pTimePattern);

        addParameter(pTempoMult);
        addParameter(pTimePattern);
        addParameter(pShape);
        addParameter(gainf);

//        addSingleParameterUIRow(pShape);
        for (int i = 0; i < FRAME_WIDTH; ++i) {
            bass[i] = 0;
            treble[i] = 0;
        }
        addModulator(stepTimer).start();
    }

    public class rWave {
        float v0, a0, x0, t,damp,a;
        boolean bDone=false;
        final float len=8;
        rWave(float _x0, float _a0, float _v0, float _damp) { x0=_x0*len; a0=_a0; v0=_v0; t=0; damp = _damp; }
        void move(double deltaMs) {
            t += deltaMs*.001;
            if (t>4) bDone=true;
        }
        float val(float _x) {
            _x*=len;
            float dist = t*v0 - MathUtils.abs(_x-x0);
            if (dist<0) { a=1; return 0; }
            a  = a0*exp(-dist*damp) * exp(-MathUtils.abs(_x-x0)/(.2f*len)); // * max(0,1-t/dur)
            return  -a*sin(dist);
        }
    }

    // public LXParameter getRhythmDisabledParameter() {
    //   return new FunctionalParameter() {
    //     public double getValue() {
    //       return lx.tempo.ramp();
    //     }
    //   };
    // }

    public void onReset()  { zTheta=0; xTheta=0; yTheta=0;  super.onReset(); }
    public void onActive() {
        zTheta=0; xTheta=0; yTheta=0;
        while (lx.tempo.bpm() > 40) lx.tempo.setBpm(lx.tempo.bpm()/2);
        if (eq  == null) {
            eq = new GraphicMeter(lx.engine.audio.getInput());

            eq.slope.setValue(6);
            eq.gain.setValue(6);
            eq.range.setValue(36);
            eq.release.setValue(640);

//            addParameter(eq.gain);
//            addParameter(eq.range);
//            addParameter(eq.attack);
//            addParameter(eq.release);
//            addParameter(eq.slope);

            addModulator(eq).start();
        }
    }

    int fromKey(int pitch) {
        int k = (pitch - 36) % 16;
        int x = k % 4;
        int y = k / 4;
        return ((3 - y) * 4) + x;
    }

    int KeyPressed = -1;
    @Override
    public void noteOnReceived(MidiNoteOn note) {
        int key = fromKey(note.getPitch());
        if (key > 7) {
            key -= 7;
            KeyPressed = key;
        }
//        int row = note.getPitch(), col = note.getChannel();
//        if (row == 57) {KeyPressed = col; return; }
    }
    int counter = 0;

    public  void StartRun(double deltaMs) {

        float hx = 0, hy = 0, hz = 0, pitch= 0, roll= 0, yaw=0;
//        if (leapEnabled.isOn()) {
//            for (Hand hand : leap.getHands()) {
//                position= hand.getStabilizedPosition();
//                hx += position.x;
//                hy += position.y;
//                hz += position.z;
//                // PVector dynamics = hand.getDynamics();
//                pitch = hand.getPitch();
//                roll = hand.getRoll();
//                yaw = hand.getYaw();
//            }
//        }
        //dynamics.x

        if (false)  {t = map(hy, 50, 500, 0, 1); }
        else { t = lx.tempo.rampf(); }

        amp = pAmp    .getValuef();
        rad = pRadius .getValuef();
        bnc = pBounce .getValuef();

        //experimental leap
//        if (leapEnabled.isOn() && val(pSpin) != .5) {
//            zTheta += deltaMs* (map(pitch, -45, 45,-.5, .5)*.01 );
//        }
        PVector translation = new PVector(position.x*10, position.y*10, 0);
        super.pTrans = translation;

        if (true){ zTheta  += deltaMs*(val(pSpin)-.5)*.01; }
        //experimental
        xTheta  += deltaMs*(val(pSpin)-.5)*.01;
        yTheta  += deltaMs*(val(pSpin)-.5)*.01;

        theta.set(val(pRotX)*PConstants.PI*2, val(pRotY)*PConstants.PI*2, val(pRotZ)*PConstants.PI*2 + zTheta);   //Z-theta
        tSin.set(sin(theta.x), sin(theta.y), sin(theta.z));
        tCos.set(cos(theta.x), cos(theta.y), cos(theta.z));

        thetaX.set(val(pRotX)*PConstants.PI*2, val(pRotY)*PConstants.PI*2, val(pRotZ)*PConstants.PI*2 + xTheta);   //X-theta
        tSinX.set(sin(thetaX.x), sin(thetaX.y), sin(thetaX.z));
        tCosX.set(cos(thetaX.x), cos(thetaX.y), cos(thetaX.z));

        thetaY.set(val(pRotX)*PConstants.PI*2, val(pRotY)*PConstants.PI*2, val(pRotZ)*PConstants.PI*2 + yTheta);   //Y-theta
        tSinY.set(sin(thetaY.x), sin(thetaY.y), sin(thetaY.z));
        tCosY.set(cos(thetaY.x), cos(thetaY.y), cos(thetaY.z));

        if (t<LastMeasure) {
            if (random(3) < 1) { curRandTempo = (int)(MathUtils.random(4)); if (curRandTempo == 3) curRandTempo = (int)(random(4)); }
            if (random(3) < 1) { curRandTPat  = pShape.getValuei() > 6 ? 2+(int)(MathUtils.random(5)) : (int)(MathUtils.random(7));           }
        } LastMeasure = t;

        int nTempo = pTempoMult.getValuei(); if (nTempo == 5) nTempo = curRandTempo;
        int nTPat  = pTimePattern.getValuei(); if (nTPat  == 7) nTPat  = curRandTPat ;

        switch (nTempo) {
            case 0:   t = t;                break;
            case 1:   t = (t*2.f )%1.f;           break;
            case 2:   t = (t*4.f )%1.f;           break;
            case 3:   t = (t*8.f )%1.f;           break;
            case 4:   t = (t*16.f)%1.f;           break;
        }

        int i=0; while (i< waves.size()) {
            rWave w = waves.get(i);
            w.move(deltaMs); if (w.bDone) waves.remove(i); else i++;
        }

        if ((t<LastBeat && pShape.getValuei()!=14) || KeyPressed>-1) {
            waves.add(new rWave(
                KeyPressed>-1 ? map(KeyPressed,0,7,0,1) : random(1),    // location
                bnc*10,     // bounciness
                7,        // velocity
                2*(1-amp)));  // dampiness
            KeyPressed=-1;
            if (waves.size() > 5) waves.remove(0);
        }

        if (t<LastBeat) {
            cPrev.set(cRand); setRand(cRand);
            a1.set(); a2.set(); a3.set(); a4.set();
        } LastBeat = t;

        switch (nTPat) {
            case 0:   t = sin(PConstants.PI*t);              break;  // bounce
            case 1:   t = norm(sin(2*PConstants.PI*(t+PConstants.PI/2)),-1,1);    break;  // sin
            case 2:   t = t;                  break;  // roll
            case 3:   t = constrain((int)(t*8)/7.f,0,1);     break;  // quant
            case 4:   t = t*t*t;                break;  // accel
            case 5:   t = 1.f*sin(PConstants.PI*t*.5f);           break;  // deccel
            case 6:   t = .5f*(1-cos(PConstants.PI*t));         break;  // slide
        }



        //begin Traktor Code

        //to-do  make this a parameter, as well as speed a parameter, after HeronLX has been modified to allow more knobs
        // in-progress:  trying to average the lost FFT data in the case that stepTimer has not been called. Currently all that data is being ignored
        float avgBass = eq.getAveragef(0,4);
        float avgTreble = eq.getAveragef(eq.numBands-7, 7);

        if (stepTimer.click())
        {
            float rawBass = eq.getAveragef(0, 4);
            float rawTreble = eq.getAveragef(eq.numBands-7, 7);
            index = (index + 1) % FRAME_WIDTH;
            bass[index] = gainf.getValuef()*rawBass * rawBass * rawBass * rawBass;
            treble[index] = gainf.getValuef()*rawTreble * rawTreble;
        }
        else return;



        cMid.set    (cPrev);  interpolate(t,cMid,cRand);
        cMidNorm.set  (cMid);   setNorm(cMidNorm);
        a1.move(); a2.move(); a3.move(); a4.move();
        currentdeltaMs = (int) (deltaMs);
    }

    public int CalcPoint(PVector Px) {



        if (theta.x != 0) rotateX(Px, mCtr, tSin.x, tCos.x);
        if (theta.y != 0) rotateY(Px, mCtr, tSin.y, tCos.y);
        if (theta.z != 0) rotateZ(Px, mCtr, tSin.z, tCos.z);

        PVector Pn = getNorm(Px);
        PVector V = new PVector();

        float mp  = MathUtils.min(Pn.x, Pn.z);
        float yt  = MathUtils.map(t,0,1,.5f-bnc/2,.5f+bnc/2);
        float traktortime = map(t,0,1, 1-bnc, 1+bnc);
        float r,d;

        int ctemp = 0;


        switch (pShape.getValuei()) {
            case 0:   int j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
                int pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
                ctemp = lx.hsb(
                    360 + 0.0f + .8f*MathUtils.abs(Px.x-model.cx),
                    constrain(10000/(1+MathUtils.abs(Px.y- model.cy)) ,0, 100),
                    constrain(9 * (bass[pos]*model.cy*traktortime - .25f*MathUtils.abs(Px.y - model.cy*traktortime)*rad), 0, 50)
                );
                ctemp = PImage.blendColor(ctemp, lx.hsb(
                    400 + 0.0f + .5f*MathUtils.abs(Px.x-model.cx),
                    constrain(10000/(1+MathUtils.abs(Px.y- model.cy)) ,0, 100),
                    constrain(5 * (treble[pos]*.6f*model.cy*traktortime - MathUtils.abs(Px.y - model.cy*traktortime)*rad), 0, 50)), ADD);
                V.set(Pn.x, yt                , Pn.z);              break;  // bouncing line
            case 1:   j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
                ctemp = lx.hsb(
                    360 + 0.0f + .8f*MathUtils.abs(Px.x-model.cx),
                    100,
                    constrain(9 * (bass[pos]*model.cy*yt - MathUtils.abs(Px.y - model.cy*map(cos(PConstants.PI*t * Pn.x),-1,1,0,2)+ 5 )*rad ), 0, 100)
                );
                ctemp = PImage.blendColor(ctemp, lx.hsb(
                    400 + 0.0f + .5f*MathUtils.abs(Px.x-model.cx),
                    60,
                    constrain(5 * (treble[pos]*.6f*model.cy*yt - MathUtils.abs(Px.y - model.cy*map(cos(PConstants.PI*t * Pn.x),-1,1,0,2) )*rad), 0, 100)), ADD);

                V.set(Pn.x, map(cos(PConstants.PI*t * Pn.x),-1,1,0,1)  , Pn.z);              break;  // top tap
            case 2:    j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
                ctemp = lx.hsb(
                    360 + 0.0f + .8f*MathUtils.abs(Px.x-model.cx),
                    100,
                    constrain(9 * (bass[pos]*model.cy*(bnc*map(Pn.x<.5f?Pn.x:1-Pn.x,0,.5f ,0,t-.5f)+1) - MathUtils.abs(Px.y - model.cy*(bnc*map(Pn.x<.5f?Pn.x:1-Pn.x,0,.5f ,0,t-.5f)+1))*rad ), 0, 100)
                );
                ctemp = PImage.blendColor(ctemp, lx.hsb(
                    400 + 0.0f + .5f*MathUtils.abs(Px.x-model.cx),
                    60,
                    constrain(5 * (treble[pos]*.6f*model.cy*(bnc*map(Pn.x<.5f?Pn.x:1-Pn.x,0,.5f ,0,t-.5f)+ 1) - MathUtils.abs(Px.y - model.cy*(bnc*map(Pn.x<.5?Pn.x:1-Pn.x,0,.5f ,0,t-.5f)+1))*rad ), 0, 100)), ADD);
                V.set(Pn.x, bnc*map(Pn.x<.5f?Pn.x:1-Pn.x,0,.5f ,0,t-.5f)+.5f, Pn.z);        break;  // V shape
            case 3:  j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
                float ptime = Pn.x < cMidNorm.x ? map(Pn.x,0,cMidNorm.x, 1,2*yt) :
                    map(Pn.x,cMidNorm.x,1, 2*yt,1);
                ctemp = lx.hsb(
                    360 + 0.0f + .8f*MathUtils.abs(Px.x-model.cx),
                    100,
                    constrain(9 * (bass[pos]*model.cy*ptime - MathUtils.abs(Px.y - model.cy*ptime )), 0, 100)
                );
                ctemp = PImage.blendColor(ctemp, lx.hsb(
                    400 + 0.0f + .5f*MathUtils.abs(Px.x-model.cx),
                    60,
                    constrain(5 * (treble[pos]*.6f*model.cy*ptime - MathUtils.abs(Px.y - model.cy*ptime)), 0, 100)), ADD);
                V.set(Pn.x, Pn.x < cMidNorm.x ? map(Pn.x,0,cMidNorm.x, .5f,yt) :
                    map(Pn.x,cMidNorm.x,1, yt,.5f), Pn.z);
                break;  //  Random V shape

            case 4:    ptime = (Pn.x < cMidNorm.x ?   map(Pn.x,0,cMidNorm.x, .5f,2*yt) :
                map(Pn.x,cMidNorm.x,1, 2*yt,.5f)) +
                .5f*(Pn.z < cMidNorm.z ?   map(Pn.z,0,cMidNorm.z, .5f,2*yt) :
                    map(Pn.z,cMidNorm.z,1, 2*yt,.5f));
                j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
                ctemp = lx.hsb(
                    360 + 0.0f + .8f*MathUtils.abs(Px.x-model.cx),
                    100,
                    constrain(9 * (bass[pos]*model.cy*ptime - MathUtils.abs(Px.y - model.cy*ptime)), 0, 100)
                );
                ctemp = PImage.blendColor(ctemp, lx.hsb(
                    400 + 0.0f + .5f*MathUtils.abs(Px.x-model.cx),
                    60,
                    constrain(5 * (treble[pos]*.6f*model.cy*ptime - MathUtils.abs(Px.y - model.cy*ptime)), 0, 100)), ADD);

                //irrelevant code for Play, just keeping here in case I end up wanting to use later.
                V.set(Pn.x, .5f*(Pn.x < cMidNorm.x ?   map(Pn.x,0,cMidNorm.x, .5f,yt) :
                    map(Pn.x,cMidNorm.x,1, yt,.5f)) +
                    .5f*(Pn.z < cMidNorm.z ?   map(Pn.z,0,cMidNorm.z, .5f,yt) :
                        map(Pn.z,cMidNorm.z,1, yt,.5f)), Pn.z);    break;  //  Random Pyramid shape

            case 5:   j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
                ptime = bnc*map((Pn.x-.5f)*(Pn.x-.5f),0,.25f,0,t-.5f)+1;
                ctemp = lx.hsb(
                    360 + 0.0f + .8f*MathUtils.abs(Px.x-model.cx),
                    100,
                    constrain(9 * (bass[pos]*model.cy*ptime - MathUtils.abs(Px.y - model.cy*ptime)), 0, 100)
                );
                ctemp = PImage.blendColor(ctemp, lx.hsb(
                    400 + 0.0f + .5f*MathUtils.abs(Px.x-model.cx),
                    60,
                    constrain(5 * (treble[pos]*.6f*model.cy*ptime - MathUtils.abs(Px.y - model.cy*ptime)), 0, 100)), ADD);

                V.set(Pn.x, bnc*map((Pn.x-.5f)*(Pn.x-.5f),0,.25f,0,t-.5f)+.5f, Pn.z);        break;  // wings
            case 6:    j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
                ptime = bnc*map((mp  -.5f)*(mp  -.5f),0,.25f,0,t-.5f)+1;
                ctemp=lx.hsb(360 + 0.0f + .8f*MathUtils.abs(Px.x-model.cx),
                    100,
                    constrain(9 * (bass[pos]*model.cy*ptime - MathUtils.abs(Px.y - model.cy*ptime)), 0, 100)
                );
                ctemp = PImage.blendColor(ctemp, lx.hsb(
                    400 + 0.0f + .5f*MathUtils.abs(Px.x-model.cx),
                    60,
                    constrain(5 * (treble[pos]*.6f*model.cy*ptime - MathUtils.abs(Px.y - model.cy*ptime)), 0, 100)), ADD);

                V.set(Pn.x, bnc*map((mp  -.5f)*(mp  -.5f),0,.25f,0,t-.5f)+.5f, Pn.z);        break;  // wings

            case 7:

                int theta = (int) constrain( (model.xMax - Px.x) / (model.xMax * FRAME_WIDTH), 0, FRAME_WIDTH-1 );
                pos = (index + FRAME_WIDTH - theta) % FRAME_WIDTH;

                // ctemp = lx.hsb(
                // 360 + 0.0f + .8*MathUtils.abs(Px.x-clockRadius.x),
                // 100,
                // constrain(9 * (bass[pos]*model.cy*traktortime - MathUtils.abs(Px.y - clockRadius.y*traktortime)/rad), 0, 100)
                // );

                // ctemp = PImage.blendColor(ctemp, lx.hsb(
                // 400 + 0.0f + .5*MathUtils.abs(Px.x-clockRadius.x),
                // 60,
                // constrain(5 * (treble[pos]*.6*clockRadius.y*traktortime - MathUtils.abs(Px.y - clockRadius.y*traktortime)/rad), 0, 100)), ADD);



                // return PImage.blendColor(lx.hsb(lxh(),100, d), ctemp, ADD); // clock
                return LXColor.BLACK;

            case 8:   r = amp*200 * map(bnc,0,1,1,sin(PConstants.PI*t));
                d = min(
                    distToSeg(Px.x, Px.y, a1.getX(r),a1.getY(r), a2.getX(r),a2.getY(r)),
                    distToSeg(Px.x, Px.y, a2.getX(r),a2.getY(r), a3.getX(r),a3.getY(r)),
                    distToSeg(Px.x, Px.y, a3.getX(r),a3.getY(r), a1.getX(r),a1.getY(r))       // triangle
                );
                d = constrain(30*(rad*40-d),0,100);
                return lx.hsb(lxh(),100, d); // clock

            case 9:  j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
                ctemp = lx.hsb(
                    360 + 0.0f + .8f*MathUtils.abs(Px.x-model.cx),
                    100,
                    constrain(9 * (bass[pos]*model.cy*yt - MathUtils.abs(Px.y - model.cy*yt+ 5)), 0, 100)
                );
                ctemp = PImage.blendColor(ctemp, lx.hsb(
                    400 + 0.0f + .5f*MathUtils.abs(Px.x-model.cx),
                    60,
                    constrain(5 * (treble[pos]*.6f*model.cy*yt - MathUtils.abs(Px.y - model.cy*yt)), 0, 100)), ADD);
                return ctemp;
            //  r = amp*200 * map(bnc,0,1,1,sin(PI*t));
            //       d = min(
            //         distToSeg(Px.x, Px.y, a1.getX(r),a1.getY(r), a2.getX(r),a2.getY(r)),
            //         distToSeg(Px.x, Px.y, a2.getX(r),a2.getY(r), a3.getX(r),a3.getY(r)),
            //         distToSeg(Px.x, Px.y, a3.getX(r),a3.getY(r), a4.getX(r),a4.getY(r)),
            //         distToSeg(Px.x, Px.y, a4.getX(r),a4.getY(r), a1.getX(r),a1.getY(r))       // quad
            //       );
            //       d = constrain(30*(rad*40-d),0,100);
            //       return lx.hsb(lxh(),100, d); // quad

            case 10:
                r = map(bnc,0,1,a1.r,amp*200*sin(PConstants.PI*t));
                return lx.hsb(lxh(),100,c1c(.9f+2*rad - dist(Px.x,Px.y,a1.getX(r),a1.getY(r))*.03f) );    // sphere

            case 11:
                Px.z=mCtr.z; cMid.z=mCtr.z;
                return lx.hsb(lxh(),100,c1c(1 - calcCone(Px,cMid,mCtr) * 0.02 > .5?1:0));         // cone

            case 12:  return lx.hsb(lxh() + NoiseUtils.noise(Pn.x,Pn.y,Pn.z + (NoiseMove+50000)/1000.f)*200,
                85,c1c(Pn.y < NoiseUtils.noise(Pn.x + NoiseMove/2000.f,Pn.z)*(1+amp)-amp/2.f-.1f ? 1 : 0)); // noise

            case 13:
            case 14:  float y=0; for (rWave w : waves) y += .5*w.val(Pn.x); // wave
                V.set(Pn.x, .7f+y, Pn.z);
                break;

            default:  return lx.hsb(0,0,0);
        }


        return ctemp;

        //return  PImage.blendColor(lx.hsb(lxh() , 100, c1c(1 - V.dist(Pn)/rad)),ctemp,  OVERLAY);


    }
}
