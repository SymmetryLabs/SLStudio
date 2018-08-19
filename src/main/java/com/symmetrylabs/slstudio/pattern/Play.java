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


public class Play extends DPat
{
    public class rAngle {
        float     prvA, dstA, c;
        float     prvR, dstR, r;
        float     _cos, _sin, x, y;
        public float     fixAngle    (float a, float b) { return a<b ?
            (MathUtils.abs(a-b) > MathUtils.abs(a+2*PConstants.PI-b) ? a : a+2*PConstants.PI) :
            (MathUtils.abs(a-b) > MathUtils.abs(a-2*PConstants.PI-b) ? a : a-2*PConstants.PI)    ; }
        public float    getX(float r)    {    return mCtr.x + _cos*r; }
        public float    getY(float r)    {    return mCtr.y + _sin*r; }
        public void    move()             {    c         = interp(t,prvA,dstA);
            r         = interp(t,prvR,dstR);
            _cos     = MathUtils.cos(c);     _sin     = MathUtils.sin(c);
            x         = getX(r);     y         = getY(r);        }
        public void    set()             {    prvA     = dstA;     dstA     = random(2*PConstants.PI);     prvA = fixAngle(prvA, dstA);
            prvR     = dstR;     dstR     = random(mCtr.y);                                    }
    }

    CompoundParameter     pAmp, pRadius, pBounce, tempoMod;
    DiscreteParameter            pTimePattern, pTempoMult, pShape;

    ArrayList<rWave> waves = new ArrayList<rWave>(10);

    int        nBeats    =      0;
    float     t,amp,rad,bnc,zTheta=0;

    rAngle    a1         = new rAngle(), a2             = new rAngle(),
        a3         = new rAngle(), a4             = new rAngle();
    PVector    cPrev     = new PVector(), cRand        = new PVector(),
        cMid     = new PVector(),
        theta     = new PVector(), tSin        = new PVector(),
        tCos    = new PVector(), cMidNorm     = new PVector();
    float    LastBeat=3, LastMeasure=3;
    int        curRandTempo = 1, curRandTPat = 1;

    public Play(LX lx) {
        super(lx);
        pRadius        = addParam("Rad"     , .1f      );
        pBounce        = addParam("Bnc"    , .2f    );
        pAmp          = addParam("Amp"     , .2f    );
        tempoMod    = addParam("tempo", 1, .1f, 4);
        pTempoMult     = new DiscreteParameter ("TMult"        , new String[] {"1x", "2x", "4x", "8x", "16x", "Rand"    }    );
        pTimePattern= new DiscreteParameter ("TPat"        , new String[] {"Bnce", "Sin", "Roll", "Quant", "Accel", "Dcel", "Slide", "Rand"}    );
        pShape         = new DiscreteParameter ("Shape"        , new String[] {"Line", "Tap", "V", "RndV",
            "Prmd", "Wings", "W2", "Clock",
            "Trngle", "Quad", "Sphr", "Cone",
            "Noise", "Wave", "?", "?"}                         );
        pTimePattern.setValue(1);
//        addSingleParameterUIRow(pTempoMult);
//        addSingleParameterUIRow(pTimePattern);
//        addSingleParameterUIRow(pShape);
        addParameter(pTempoMult);
        addParameter(pTimePattern);
        addParameter(pShape);
    }

    public class rWave {
        float v0, a0, x0, t,damp,a;
        boolean bDone=false;
        final float len=8;
        rWave(float _x0, float _a0, float _v0, float _damp) { x0=_x0*len; a0=_a0; v0=_v0; t=0; damp = _damp; }
        public void move(double deltaMs) {
            t += deltaMs*.001f;
            if (t>4) bDone=true;
        }
        public float val(float _x) {
            _x*=len;
            float dist = t*v0 - MathUtils.abs(_x-x0);
            if (dist<0) { a=1; return 0; }
            a  = a0*exp(-dist*damp) * exp(-MathUtils.abs(_x-x0)/(.2f*len)); // * max(0,1-t/dur)
            return    -a*MathUtils.sin(dist);
        }
    }

    public void onReset()  { zTheta=0; super.onReset(); }
    public void onActive() {
        zTheta=0;
        while (lx.tempo.bpm() > 40) lx.tempo.setBpm(lx.tempo.bpm()/2);
    }
//
    int KeyPressed = -1;
//    public boolean noteOn(LXMidiNote note) {
//        int row = note.getPitch(), col = note.getChannel();
//        if (row == 57) {KeyPressed = col; return true; }
//        return super.noteOn(note);
//    }

    public void StartRun(double deltaMs) {
        t     = lx.tempo.rampf()*tempoMod.getValuef();
        amp = pAmp        .getValuef();
        rad    = pRadius    .getValuef();
        bnc    = pBounce    .getValuef();
        zTheta    += deltaMs*(val(pSpin)-.5f)*.01f;

        theta    .set(val(pRotX)*PConstants.PI*2, val(pRotY)*PConstants.PI*2, val(pRotZ)*PConstants.PI*2 + zTheta);
        tSin    .set(MathUtils.sin(theta.x), MathUtils.sin(theta.y), MathUtils.sin(theta.z));
        tCos    .set(MathUtils.cos(theta.x), MathUtils.cos(theta.y), MathUtils.cos(theta.z));

        if (t<LastMeasure) {
            if (random(3) < 1) { curRandTempo = PApplet.parseInt(random(4)); if (curRandTempo == 3) curRandTempo = PApplet.parseInt(random(4));    }
            if (random(3) < 1) { curRandTPat  = pShape.getValuei()> 6 ? 2+PApplet.parseInt(random(5)) : PApplet.parseInt(random(7));                     }
        } LastMeasure = t;

        int nTempo = pTempoMult     .getValuei(); if (nTempo == 5) nTempo = curRandTempo;
        int nTPat  = pTimePattern.getValuei(); if (nTPat  == 7) nTPat  = curRandTPat ;

        switch (nTempo) {
            case 0:     t = t;                                break;
            case 1:     t = (t*2.f )%1.f;                        break;
            case 2:     t = (t*4.f )%1.f;                        break;
            case 3:     t = (t*8.f )%1.f;                        break;
            case 4:     t = (t*16.f)%1.f;                        break;
        }

        int i=0; while (i< waves.size()) {
            rWave w = waves.get(i);
            w.move(deltaMs); if (w.bDone) waves.remove(i); else i++;
        }

        if ((t<LastBeat && pShape.getValuei()!=14) || KeyPressed>-1) {
            waves.add(new rWave(
                KeyPressed>-1 ? map(KeyPressed,0,7,0,1) : random(1),        // location
                bnc*10,            // bounciness
                7,                // velocity
                2*(1-amp)));    // dampiness
            KeyPressed=-1;
            if (waves.size() > 5) waves.remove(0);
        }

        if (t<LastBeat) {
            cPrev.set(cRand); setRand(cRand);
            a1.set(); a2.set(); a3.set(); a4.set();
        } LastBeat = t;

        switch (nTPat) {
            case 0:     t = MathUtils.sin(PConstants.PI*t);                            break;    // bounce
            case 1:     t = norm(MathUtils.sin(2*PConstants.PI*(t+PConstants.PI/2)),-1,1);        break;    // sin
            case 2:     t = t;                                     break;    // roll
            case 3:     t = MathUtils.constrain(PApplet.parseInt(t*8)/7.f,0,1);            break;    // quant
            case 4:     t = t*t*t;                                break;    // accel
            case 5:     t = MathUtils.sin(PConstants.PI*t*.5f);                        break;    // deccel
            case 6:     t = .5f*(1-MathUtils.cos(PConstants.PI*t));                    break;    // slide
        }

        cMid.set        (cPrev);    interpolate(t,cMid,cRand);
        cMidNorm.set    (cMid);        setNorm(cMidNorm);
        a1.move(); a2.move(); a3.move(); a4.move();
    }

    public int CalcPoint(PVector Px) {
        if (theta.x != 0) rotateX(Px, mCtr, tSin.x, tCos.x);
        if (theta.y != 0) rotateY(Px, mCtr, tSin.y, tCos.y);
        if (theta.z != 0) rotateZ(Px, mCtr, tSin.z, tCos.z);

        PVector Pn = getNorm(Px);
        PVector V = new PVector();

        float mp    = MathUtils.min(Pn.x, Pn.z);
        float yt     = map(t,0,1,.5f-bnc/2,.5f+bnc/2);
        float r,d;

        switch (pShape.getValuei()) {
            case 0:        V.set(Pn.x, yt                                 , Pn.z);                             break;    // bouncing line
            case 1:        V.set(Pn.x, map(MathUtils.cos(PConstants.PI*t * Pn.x),-1,1,0,1)  , Pn.z);                             break;    // top tap
            case 2:        V.set(Pn.x, bnc*map(Pn.x<.5f?Pn.x:1-Pn.x,0,.5f ,0,t-.5f)+.5f, Pn.z);                break;    // V shape
            case 3:        V.set(Pn.x, Pn.x < cMidNorm.x ? map(Pn.x,0,cMidNorm.x, .5f,yt) :
                map(Pn.x,cMidNorm.x,1, yt,.5f), Pn.z);                  break;    //  Random V shape

            case 4:        V.set(Pn.x,    .5f*(Pn.x < cMidNorm.x ?     map(Pn.x,0,cMidNorm.x, .5f,yt) :
                map(Pn.x,cMidNorm.x,1, yt,.5f)) +
                .5f*(Pn.z < cMidNorm.z ?     map(Pn.z,0,cMidNorm.z, .5f,yt) :
                    map(Pn.z,cMidNorm.z,1, yt,.5f)), Pn.z);         break;    //  Random Pyramid shape

            case 5:        V.set(Pn.x, bnc*map((Pn.x-.5f)*(Pn.x-.5f),0,.25f,0,t-.5f)+.5f, Pn.z);                break;    // wings
            case 6:        V.set(Pn.x, bnc*map((mp  -.5f)*(mp  -.5f),0,.25f,0,t-.5f)+.5f, Pn.z);                break;    // wings

            case 7:        d = MathUtils.min(
                distToSeg(Px.x, Px.y, a1.getX(70),a1.getY(70), mCtr.x, mCtr.y),
                distToSeg(Px.x, Px.y, a2.getX(40),a2.getY(40), mCtr.x, mCtr.y));
                d = constrain(30*(rad*40-d),0,100);
                return lx.hsb(lxh(),100, d); // clock

            case 8:        r = amp*200 * map(bnc,0,1,1,MathUtils.sin(PConstants.PI*t));
                d = min(
                    distToSeg(Px.x, Px.y, a1.getX(r),a1.getY(r), a2.getX(r),a2.getY(r)),
                    distToSeg(Px.x, Px.y, a2.getX(r),a2.getY(r), a3.getX(r),a3.getY(r)),
                    distToSeg(Px.x, Px.y, a3.getX(r),a3.getY(r), a1.getX(r),a1.getY(r))                // triangle
                );
                d = constrain(30*(rad*40-d),0,100);
                return lx.hsb(lxh(),100, d); // clock

            case 9:        r = amp*200 * map(bnc,0,1,1,MathUtils.sin(PConstants.PI*t));
                d = MathUtils.min(
                    distToSeg(Px.x, Px.y, a1.getX(r),a1.getY(r), a2.getX(r),a2.getY(r)),
                    MathUtils.min(distToSeg(Px.x, Px.y, a2.getX(r),a2.getY(r), a3.getX(r),a3.getY(r)),
                        MathUtils.min(distToSeg(Px.x, Px.y, a3.getX(r),a3.getY(r), a4.getX(r),a4.getY(r)),
                    distToSeg(Px.x, Px.y, a4.getX(r),a4.getY(r), a1.getX(r),a1.getY(r))))                // quad
                );
                d = constrain(30*(rad*40-d),0,100);
                return lx.hsb(lxh(),100, d); // clock

            case 10:
                r = map(bnc,0,1,a1.r,amp*200*MathUtils.sin(PConstants.PI*t));
                return lx.hsb(lxh(),100,c1c(.9f+2*rad - dist(Px.x,Px.y,a1.getX(r),a1.getY(r))*.03f) );        // sphere

            case 11:
                Px.z=mCtr.z; cMid.z=mCtr.z;
                return lx.hsb(lxh(),100,c1c(1 - calcCone(Px,cMid,mCtr) * 0.02f > .5f?1:0));                  // cone

            case 12:    return lx.hsb(lxh() + NoiseUtils.noise(Pn.x,Pn.y,Pn.z + (NoiseMove+50000)/1000.f)*200,
                85,c1c(Pn.y < NoiseUtils.noise(Pn.x + NoiseMove/2000.f,Pn.z)*(1+amp)-amp/2.f-.1f ? 1 : 0));    // noise

            case 13:
            case 14:    float y=0; for (rWave w : waves) y += .5f*w.val(Pn.x);    // wave
                V.set(Pn.x, .7f+y, Pn.z);
                break;

            default:    return lx.hsb(0,0,0);
        }
        return lx.hsb(lxh(), 100, c1c(1 - V.dist(Pn)/rad));
    }
}


