package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.slstudio.pattern.base.APat;
import com.symmetrylabs.util.MathUtils;

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


public class SineSphere extends APat {
    float modelrad = sqrt((model.xMax)*(model.xMax) + (model.yMax)*(model.yMax) + (model.zMax)*(model.zMax));
    private CompoundParameter yrotspeed = new CompoundParameter("yspeed", 5800, 1, 10000);
    private CompoundParameter yrot2speed = new CompoundParameter("y2speed", 4500, 1, 15000);
    private CompoundParameter yrot3speed = new CompoundParameter("y3speed", 1400, 1, 15000);
    private CompoundParameter vibrationrate = new CompoundParameter("vib", 3000, 1, 10000);
    private CompoundParameter speed = new CompoundParameter("nspeed", .00001f, .01f);
    private SawLFO yrot = new SawLFO(0, TWO_PI, yrotspeed);
    private SawLFO yrot2 = new SawLFO(0, -TWO_PI, yrot2speed);
    private SawLFO yrot3 = new SawLFO(0, -TWO_PI, yrot3speed);
    public CompoundParameter huespread = new CompoundParameter("HueSpread", 0, 180);
    public CompoundParameter widthparameter= new CompoundParameter("Width", 2.7f, 1, 10);
    public CompoundParameter vibration_magnitude = new CompoundParameter("Vmag", 20, 2, modelrad/2);
    public CompoundParameter scale = new CompoundParameter("Scale", 1, .1f, 5);
    private int pitch = 0;
    private int channel = 0;
    private int velocity = 0;
    private int cur = 0;
    public final LXProjection sinespin;
    public final LXProjection sinespin2;
    public final LXProjection sinespin3;

    Pick Galaxy, STime;

    public CompoundParameter rotationx = new CompoundParameter("rotx", 0, 0, 1 );
    public CompoundParameter rotationy = new CompoundParameter("roty", 1, 0, 1);
    public CompoundParameter rotationz = new CompoundParameter("rotz", 0, 0, 1);

    public final PVector P = new PVector();

    class Sphery {
        private float f1xcenter, f1ycenter, f1zcenter, f2xcenter , f2ycenter, f2zcenter; //second three are for an ellipse with two foci
        private PVector velocity, accel, center;
        private SinLFO vibration;
        private SinLFO surfacewave;
        private SinLFO xbounce;
        public SinLFO ybounce;
        private SinLFO zbounce;
        float vibration_magnitude, vperiod, radius, vibration_min, vibration_max, noiseTime;

        //public CompoundParameter huespread;
        public CompoundParameter bouncerate;
        public CompoundParameter bounceamp;
        public CompoundParameter vibrationrate;
        public final PVector circlecenter;

        public Sphery(LX lx, PVector center, float radius, float vibration_magnitude , float vperiod)
        {
            // super(lx, this);
            this.f1xcenter = center.x;
            this.f1ycenter = center.y;
            this.f1zcenter = center.z;
            this.center = center;
            this.radius = radius;
            this.circlecenter= new PVector(f1xcenter,f1ycenter,f1zcenter);

            this.vibration_magnitude = vibration_magnitude;

            this.vperiod = vperiod;
            //addParameter(bounceamp = new CompoundParameter("Amp", .5));
            //addParameter(bouncerate = new CompoundParameter("Rate", .5)); //ybounce.modulateDurationBy(bouncerate);
            //addParameter(vibrationrate = new CompoundParameter("vibration", 1000, 10000));
            //addParameter(widthparameter = new CompoundParameter("Width", .2));
            //addModulator(xbounce = new SinLFO(model.xMax/3, 2*model.yMax/3, 2000)).trigger();
            addModulator(ybounce= new SinLFO(model.yMax/3, 2*model.yMax/3, 240000)).trigger(); //bounce.modulateDurationBy

            //addModulator(bounceamp); //ybounce.setMagnitude(bouncerate);
            addModulator( vibration = new SinLFO( this.radius - vibration_magnitude , this.radius + vibration_magnitude, vperiod)).trigger(); //vibration.setPeriod(240000/lx.tempo.bpm());


        }

        // public Sphery(float f1xcenter, float f1ycenter, float f1zcenter, float vibration_magnitude, float vperiod)
        // {
        // this.f1xcenter = f1xcenter;
        // this.f1ycenter = f1ycenter;
        // this.f1zcenter = f1zcenter;
        // this.vibration_magnitude = vibration_magnitude;
        // this.vperiod = vperiod;
        // addModulator(ybounce= new SinLFO(model.yMax/3, 2*model.yMax/3, 240000)).trigger(); //bounce.modulateDurationBy
        // addModulator( vibration = new SinLFO( modelrad/10 - vibration_magnitude , modelrad/10 + vibration_magnitude, vperiod)).trigger(); //vibration.setPeriod(240000/lx.tempo.bpm());

        // }

        //for an ellipse
        // public Sphery(float f1xcenter, float f1ycenter, float f1zcenter, float f2xcenter, float f2ycenter, float f2zcenter,
        // float vibration_min, float vibration_max, float vperiod)

        // {
        // this.f1xcenter = f1xcenter;
        // this.f1ycenter = f1ycenter;
        // this.f1zcenter = f1zcenter;
        // this.f2xcenter = f2xcenter;
        // this.f2ycenter = f2ycenter;
        // this.f2zcenter = f2zcenter;
        // this.vibration_min = vibration_min;
        // this.vibration_max = vibration_max;
        // this.vperiod = vperiod;
        // //addModulator(xbounce = new SinLFO(model.xMax/3, 2*model.yMax/3, 2000)).trigger();
        // addModulator(ybounce).trigger();
        // addModulator( vibration = new SinLFO(vibration_min , vibration_max, lx.tempo.rampf())).trigger(); //vibration.modulateDurationBy(vx);
        // addParameter(widthparameter = new CompoundParameter("Width", .1));
        // //addParameter(huespread = new CompoundParameter("bonk", .2));

        // }

        public int c1c (float a) { return MathUtils.round(100*MathUtils.constrain(a,0,1)); }

        public void setVelocity(PVector vel) {this.velocity=vel;}
        public void setAcceleration(PVector acc) {this.accel= acc;}
        public void setVibrationPeriod(double period){
            // to-do: make this conditional upon time signature

            this.vibration.setPeriod(period);
        }

        public void setVibrationMagnitude(double mag){
            //to-do: make this optionally conditional upon decibel volume, frequency spectrum)
            this.vibration.setRange(-mag,mag);

        }


        public float distfromcirclecenter(PVector here)
        {
            return PVector.dist(here, this.circlecenter);
        }
        //void updatespherey(deltaMs, )

        public float quadrant(PVector q) {
            float qtheta = atan2( (q.x-f1xcenter) , (q.z - f1zcenter) );
            float qphi = acos( (q.z-f1zcenter)/(PVector.dist(q,circlecenter)) );


            return map(qtheta, -PConstants.PI/2, PConstants.PI/2, 200-huespread.getValuef(), 240+huespread.getValuef());
            //if (q.x > f1xcenter ) {return 140 ;}
            //else {return 250;}
        }

        public void run(double deltaMs) { velocity.mult((float)deltaMs); this.center.add(velocity);  this.noiseTime=(float)deltaMs*speed.getValuef();}

        public int spheryvalue (PVector p) {
            circlecenter.set(this.f1xcenter, this.f1ycenter, this.f1zcenter);
            //switch(sShpape.cur() ) {}

            float b = MathUtils.max(0, 100 - widthparameter.getValuef()*MathUtils.abs(p.dist(circlecenter)
                - vibration.getValuef()) );

            if (b <= 0) {
                return 0;
            }

            return lx.hsb(
                constrain(quadrant(p), 0, 360),
                // constrain(100*noise(quadrant(p)), 0, 100),
                100,
                b
            );
        }
        public int ellipsevalue(float px, float py, float pz , float f1xc, float f1yc, float f1zc, float f2xc, float f2yc, float f2zc)
        {
//switch(sShpape.cur() ) {}
            return lx.hsb(huespread.getValuef()*5*px, dist(model.xMax-px, model.yMax-py, model.zMax-pz, f1xc, f1yc, f1zc) ,
                MathUtils.max(0, 100 - 100*widthparameter.getValuef() *
                    MathUtils.abs( (dist(px, py, pz, f1xc, ybounce.getValuef(), f1zc) +
                        (MathUtils.dist(px, py , pz, f2xc, ybounce.getValuef(), f2zc) ) )/2
                        - 1.2f*vibration.getValuef() ) ) ) ;
        }


    }


//    public boolean noteOn(LXMidiNote note ) {
//        int row = note.getPitch(), col = note.getChannel();
//        // if (row == 57) {KeyPressed = col; return true; }
//        return super.noteOn(note);
//    }


// public boolean noteOn(Note note) {
// pitch= note.getPitch();
// velocity=note.getVelocity();
// channel=note.getChannel();
// return true;
// }

// public boolean gridPressed(int row, int col) {
// pitch = row; channel = col;
// cur = NumApcCols*(pitch-53)+col;
// //setState(row, col, 0 ? 1 : 0);
// return true;
// }

    //public grid
    final Sphery[] spherys;

    public SineSphere(LX lx)
    {
        super(lx);
        sinespin = new LXProjection(model);
        sinespin2 = new LXProjection(model);
        sinespin3= new LXProjection(model);
        addParameter(huespread);
        addParameter(vibrationrate);
        addParameter(widthparameter);
        addParameter(rotationx);
        addParameter(rotationy);
        addParameter(rotationz);
        addParameter(yrotspeed);
        addParameter(yrot2speed);
        addParameter(yrot3speed);
        addParameter(vibration_magnitude);
        addParameter(scale);
        addModulator(yrot).trigger();
        addModulator(yrot2).trigger();
        addModulator(yrot3).trigger();
        //Galaxy = addPick("Galaxy", 1, 3, new String[] {"home", "vertical","single","aquarium"});
        STime =addPick("Time", 1, 4, new String[]{"half", "triplet", "beat", "2x", "3x" });

        spherys = new Sphery[] {
            new Sphery(lx, new PVector (model.xMax/4, model.yMax/2, model.zMax/2), modelrad/12, modelrad/25, 3000),
            new Sphery(lx, new PVector(.75f*model.xMax, model.yMax/2, model.zMax/2), modelrad/14, modelrad/28, 2000),
            new Sphery(lx, new PVector( model.cx, model.cy, model.cz), modelrad/8, modelrad/15, 2300),
            new Sphery(lx, new PVector( .7f*model.xMax, .65f*model.yMax, .5f*model.zMax), modelrad/11, modelrad/25, 3500),
            new Sphery(lx, new PVector( .75f*model.xMax, .8f*model.yMax, .7f*model.zMax), modelrad/12, modelrad/30, 2000)


        };
    }

// public void onParameterChanged(LXParameter parameter)
// {


// for (Sphery s : spherys) {
// if (s == null) continue;
// double bampv = s.bounceamp.getValue();
// double brv = s.bouncerate.getValue();
// double tempobounce = lx.tempo.bpm();
// if (parameter == s.bounceamp)
// {
// s.ybounce.setRange(bampv*model.yMax/3 , bampv*2*model.yMax/3, brv);
// }
// else if ( parameter == s.bouncerate )
// {
// s.ybounce.setDuration(120000./tempobounce);
// }
// }
// }

    public void run( double deltaMs) {
        float t = lx.tempo.rampf();
        float bpm = lx.tempo.bpmf();
        float scalevalue = scale.getValuef();
        int spherytime= STime.Cur();


        switch (spherytime) {

            case 0: t = map(.5f*t ,0,.5f, 0,1); bpm = .5f*bpm; break;

            case 1: t = t; bpm = bpm; break;

            case 2: t = map(2*t,0,2,0,1); bpm = 2*bpm; break;

            default: t= t; bpm = bpm;
        }

        //switch(sphery.colorscheme)

        for ( Sphery s: spherys){
            s.setVibrationPeriod(vibrationrate.getValuef());
            // s.setVibrationMagnitude(vibration_magnitude.getValuef());

        }


        sinespin.reset()
            // Translate so the center of the car is the origin, offset
            .center()
            .scale(scalevalue, scalevalue, scalevalue)
            // Rotate around the origin (now the center of the car) about an y-vector
            .rotate(yrot.getValuef(), rotationx.getValuef(), rotationy.getValuef() , rotationz.getValuef())
            .translate(model.cx, model.cy, model.cz);





        for (LXVector p: sinespin)
        {
            P.set(p.x, p.y, p.z);
            // PVector P = new PVector(p.x, p.y, p.z);
            int c = 0xff000000;
            c = blendIfColor(c, spherys[1].spheryvalue(P), ADD);
            c = blendIfColor(c, spherys[0].spheryvalue(P), ADD);
            c = blendIfColor(c, spherys[2].spheryvalue(P),ADD);


            colors[p.index] = c;


        }
        sinespin2.reset()
            .center()
            .scale(scalevalue,scalevalue,scalevalue)
            .rotate(yrot2.getValuef(), rotationx.getValuef(), rotationy.getValuef() , rotationz.getValuef())
            .translate(model.cx,model.cy,model.cz);

        for (LXVector p: sinespin2)
        { int c = 0;
            // PVector P = new PVector(p.x, p.y, p.z);
            P.set(p.x, p.y, p.z);
            c = blendIfColor(c, spherys[3].spheryvalue(P),ADD);

            colors[p.index] = blendIfColor(colors[p.index], c , ADD);

        }
        sinespin3.reset()
            .center()
            .scale(scalevalue,scalevalue,scalevalue)
            .rotate(yrot3.getValuef(),-1 + rotationx.getValuef(), rotationy.getValuef(), rotationz.getValuef())
            .translate(model.cx, model.cy, model.cz);
        for (LXVector p: sinespin3)
        { int c = 0;
            // PVector P = new PVector(p.x, p.y, p.z);
            P.set(p.x, p.y, p.z);
            c = blendIfColor(c, spherys[4].spheryvalue(P),ADD);

            colors[p.index] = blendIfColor(colors[p.index], c , ADD);

        }







    }

    public int blendIfColor(int c1, int c2, int mode) {
        if (c2 != 0) {
            return PImage.blendColor(c1, c2, mode);
        }
        return c1;
    }


    // color c = 0;
    // c = PImage.blendColor(c, spherys[3].ellipsevalue(Px.x, Px.y, Px.z, model.xMax/4, model.yMax/4, model.zMax/4, 3*model.xMax/4, 3*model.yMax/4, 3*model.zMax/4),ADD);
    // return c;
    // }
    // return lx.hsb(0,0,0);
    // // else if(spheremode ==2)
    // { color c = 0;
    // return lx.hsb(CalcCone( (xyz by = new xyz(0,spherys[2].ybounce.getValuef(),0) ), Px, mid) );

    // }


    // }

}
