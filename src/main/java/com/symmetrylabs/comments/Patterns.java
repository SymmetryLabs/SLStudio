package com.symmetrylabs.comments;// public class TraktorPlay extends com.symmetrylabs.util.dan.DPat //implements P3LX.KeyEventHandler
// {
//   private LXAudioInput audioInput = lx.engine.audio.getInput();
//   private GraphicMeter eq = new GraphicMeter(audioInput);

//   private boolean rhythmOn = false; 
//   private boolean leapOn = false; 
//   //private LeapMotion leap; 
//   PVector position = new PVector(); 

//   // void keyEvent(KeyEvent keyEvent) {
//   //   switch (keyEvent.getKey()) {
//   //    case 'r': rhythmOn = !rhythmOn; print(rhythmOn); 
//   //    case 'O':   onReset();  println("reset"); break; 
//   //    case 'L':  leapOn = !leapOn;   println(leapOn); break; 
//   //   }
//   // }

//   public class rAngle {
//     float   prvA, dstA, c;
//     float   prvR, dstR, r;    
//     float   _cos, _sin, x, y;
//     float   fixAngle  (float a, float b) { return a<b ?
//                     (abs(a-b) > abs(a+2*PI-b) ? a : a+2*PI) :
//                     (abs(a-b) > abs(a-2*PI-b) ? a : a-2*PI) ; }
//     float getX(float r) { return mCtr.x + _cos*r; }
//     float getY(float r) { return mCtr.y + _sin*r; }
//     void  move()      { c     = interp(t,prvA,dstA); 
//                   r     = interp(t,prvR,dstR);
//                   _cos  = cos(c);   _sin  = sin(c);
//                                  x    = getX(r);  y     = getY(r);    }   
//     void  set()       { prvA  = dstA;   dstA  = random(2*PI);   prvA = fixAngle(prvA, dstA);
//                   prvR  = dstR;   dstR  = random(mCtr.y);                 }
//   }
//   final int FRAME_WIDTH = 60;
//   int currentdeltaMs; 

//   CompoundParameter speed = new CompoundParameter("SPD", 0.4);
//   Click stepTimer = new Click("Step", speed.getValuef()*30);
//   private float[] bass = new float[FRAME_WIDTH];
//   private float[] treble = new float[FRAME_WIDTH];

//   private int index = 0;
//    int pcounter = 0;

//   CompoundParameter  pAmp, pRadius, pBounce;
//   DiscreteParameter      pTimePattern, pTempoMult, pShape;

//   ArrayList<rWave> waves = new ArrayList<rWave>(10);

//   int   nBeats  =   0;
//   float   t,amp,rad,bnc,zTheta=0, yTheta=0, xTheta=0;

//   rAngle  a1    = new rAngle(), a2      = new rAngle(),
//       a3    = new rAngle(), a4      = new rAngle();
//   PVector cPrev   = new PVector(), cRand    = new PVector(),
//       cMid  = new PVector(), V      = new PVector(),
//       theta   = new PVector(), thetaX = new PVector(),
//       thetaY = new PVector(), 
//       tSin   = new PVector(), tSinX = new PVector(), tSinY = new PVector(), 
//       tCos  = new PVector(), tCosX = new PVector(), tCosY = new PVector(), 
//       cMidNorm   = new PVector(),
//       Pn    = new PVector(), clockRadius= new PVector();
//   float LastBeat=3, LastMeasure=3;
//   int   curRandTempo = 1, curRandTPat = 1;

//   TraktorPlay(P3LX lx, PApplet parent) {
//     super(lx);

//     eq.slope.setValue(6);
//     eq.gain.setValue(6);
//     eq.range.setValue(36);
//     eq.release.setValue(640);
//     addParameter(eq.gain);
//     addParameter(eq.range);
//     addParameter(eq.attack);
//     addParameter(eq.release);
//     addParameter(eq.slope);
//     addModulator(eq).start();

//    // lx.addKeyEventHandler(parent);
//     //leap= new LeapMotion(parent).withGestures(); 
//     pRadius   = addParam("Rad"  , 1.4  , 0, 4  );
//     pBounce   = addParam("Bnc"  , .0  );
//     pAmp      = addParam("Amp"  , .2  );

//     pTempoMult  = new DiscreteParameter ("TMult" , new String[] {"1x", "2x", "4x", "8x", "16x", "Rand" } );
//     pTimePattern= new DiscreteParameter ("TPat" , new String[] {"Bnce", "Sin", "Roll", "Quan", "Acc", "Decc", "Slide", "Rand"} );
//     pShape    = new DiscreteParameter ("Shape" , new String[] {"Line", "Tap", "V", "RndV",
//                                   "Prmd", "Wings", "W2", "Clock",
//                                   "Trngle", "Quad", "Sphr", "Cone",
//                                   "com.symmetrylabs.pattern.Noise", "Wave", "?", "?"}            );
//     //addSingleParameterUIRow(pTempoMult);
//     //addSingleParameterUIRow(pTimePattern);

//     //addNonKnobParameter(pTempoMult);
//     //addNonKnobParameter(pTimePattern);
//     //addNonKnobParameter(pShape);

//     //addSingleParameterUIRow(pShape);
//     for (int i = 0; i < FRAME_WIDTH; ++i) {
//       bass[i] = 0;
//       treble[i] = 0;
//     }
//     addModulator(stepTimer).start();
//   }

//   public class rWave {
//     float v0, a0, x0, t,damp,a;
//     boolean bDone=false;
//     final float len=8;
//     rWave(float _x0, float _a0, float _v0, float _damp) { x0=_x0*len; a0=_a0; v0=_v0; t=0; damp = _damp; }
//     void move(double deltaMs) {
//       t += deltaMs*.001;
//       if (t>4) bDone=true;
//     }
//     float val(float _x) {
//       _x*=len;
//       float dist = t*v0 - abs(_x-x0);
//       if (dist<0) { a=1; return 0; }
//       a  = a0*exp(-dist*damp) * exp(-abs(_x-x0)/(.2*len)); // * max(0,1-t/dur)
//       return  -a*sin(dist);
//     }
//   }

//   // public LXParameter getRhythmDisabledParameter() {
//   //   return new FunctionalParameter() {
//   //     public double getValue() {
//   //       return lx.tempo.ramp(); 
//   //     }
//   //   };
//   // }

//   void onReset()  { zTheta=0; xTheta=0; yTheta=0;  super.onReset(); }
//   void onActive() { 
//     zTheta=0; xTheta=0; yTheta=0; 
//     while (lx.tempo.bpm() > 40) lx.tempo.setBpm(lx.tempo.bpm()/2);
//     // if (eq  == null) {
//     //   eq = new GraphicEQ(lx.audioInput());
//     //   eq.slope.setValue(6);
//     //   eq.gain.setValue(6);
//     //   eq.range.setValue(36);
//     //   eq.release.setValue(640);
//     //   addParameter(eq.gain);
//     //   addParameter(eq.range);
//     //   addParameter(eq.attack);
//     //   addParameter(eq.release);
//     //   addParameter(eq.slope);
//     //   addModulator(eq).start();
//     // }
//   }

//   int KeyPressed = -1;
//   // boolean noteOn(LXMidiNote note  ) {
//   //   int row = note.getPitch(), col = note.getChannel();
//   //   if (row == 57) {KeyPressed = col; return true; }
//   //   return super.noteOn(note);
//   // }
//    int counter = 0;

//   public  void StartRun(double deltaMs) {

//       float hx = 0, hy = 0, hz = 0, pitch= 0, roll= 0, yaw=0;  
//      // if (leapEnabled.isOn()) {
//      //  for (Hand hand : leap.getHands()) {
//      //     position= hand.getStabilizedPosition();
//      //     hx += position.x;
//      //     hy += position.y;
//      //     hz += position.z;
//      //    // PVector dynamics = hand.getDynamics();
//      //     pitch = hand.getPitch(); 
//      //     roll = hand.getRoll(); 
//      //     yaw = hand.getYaw(); 
//      //     }
//      //   }    
//     //dynamics.x

//     // if (leapEnabled.isOn())  {t = map(hy, 50, 500, 0, 1); }
//     // else { t = lx.tempo.rampf(); } 

//     t = lx.tempo.rampf();

//     amp = pAmp    .getValuef();
//     rad = pRadius .getValuef();
//     bnc = pBounce .getValuef();

//     //experimental leap 
//     // if (leapEnabled.isOn() && val(pSpin) != .5) {
//     // zTheta += deltaMs* (map(pitch, -45, 45,-.5, .5)*.01 ); 
//     // }
//     PVector translation = new PVector(position.x*10, position.y*10, 0); 
//     super.pTrans = translation; 

//      //if (!leapEnabled.isOn()){ zTheta  += deltaMs*(val(pSpin)-.5)*.01; }  
//     //experimental 
//     xTheta  += deltaMs*(val(pSpin)-.5)*.01;
//     yTheta  += deltaMs*(val(pSpin)-.5)*.01;

//     theta.set(val(pRotX)*PI*2, val(pRotY)*PI*2, val(pRotZ)*PI*2 + zTheta);   //Z-theta
//     tSin.set(sin(theta.x), sin(theta.y), sin(theta.z));
//     tCos.set(cos(theta.x), cos(theta.y), cos(theta.z));

//     thetaX.set(val(pRotX)*PI*2, val(pRotY)*PI*2, val(pRotZ)*PI*2 + xTheta);   //X-theta
//     tSinX.set(sin(thetaX.x), sin(thetaX.y), sin(thetaX.z));
//     tCosX.set(cos(thetaX.x), cos(thetaX.y), cos(thetaX.z));

//     thetaY.set(val(pRotX)*PI*2, val(pRotY)*PI*2, val(pRotZ)*PI*2 + yTheta);   //Y-theta
//     tSinY.set(sin(thetaY.x), sin(thetaY.y), sin(thetaY.z));
//     tCosY.set(cos(thetaY.x), cos(thetaY.y), cos(thetaY.z));

//     if (t<LastMeasure) {
//       if (random(3) < 1) { curRandTempo = int(random(4)); if (curRandTempo == 3) curRandTempo = int(random(4)); }
//       if (random(3) < 1) { curRandTPat  = pShape.getValuei() > 6 ? 2+int(random(5)) : int(random(7));           }
//     } LastMeasure = t;

//     int nTempo = pTempoMult.getValuei(); if (nTempo == 5) nTempo = curRandTempo;
//     int nTPat  = pTimePattern.getValuei(); if (nTPat  == 7) nTPat  = curRandTPat ;

//     switch (nTempo) {
//       case 0:   t = t;                break;
//       case 1:   t = (t*2. )%1.;           break;
//       case 2:   t = (t*4. )%1.;           break;
//       case 3:   t = (t*8. )%1.;           break;
//       case 4:   t = (t*16.)%1.;           break;
//     }

//     int i=0; while (i< waves.size()) {
//       rWave w = waves.get(i);
//       w.move(deltaMs); if (w.bDone) waves.remove(i); else i++;
//     }

//     if ((t<LastBeat && pShape.getValuei()!=14) || KeyPressed>-1) {
//       waves.add(new rWave(
//             KeyPressed>-1 ? map(KeyPressed,0,7,0,1) : random(1),    // location
//             bnc*10,     // bounciness
//             7,        // velocity
//             2*(1-amp)));  // dampiness
//       KeyPressed=-1;
//       if (waves.size() > 5) waves.remove(0);
//     }

//     if (t<LastBeat) {
//       cPrev.set(cRand); setRand(cRand);
//       a1.set(); a2.set(); a3.set(); a4.set();
//     } LastBeat = t;

//     switch (nTPat) {
//       case 0:   t = sin(PI*t);              break;  // bounce
//       case 1:   t = norm(sin(2*PI*(t+PI/2)),-1,1);    break;  // sin
//       case 2:   t = t;                  break;  // roll
//       case 3:   t = constrain(int(t*8)/7.,0,1);     break;  // quant
//       case 4:   t = t*t*t;                break;  // accel
//       case 5:   t = sin(PI*t*.5);           break;  // deccel
//       case 6:   t = .5*(1-cos(PI*t));         break;  // slide
//     }

//     //begin com.symmetrylabs.pattern.Traktor Code

//    //to-do  make this a parameter, as well as speed a parameter, after HeronLX has been modified to allow more knobs
//     // in-progress:  trying to average the lost FFT data in the case that stepTimer has not been called. Currently all that data is being ignored
//     float avgBass = eq.getAveragef(0,4);
//     float avgTreble = eq.getAveragef(eq.numBands-7, 7); 

//     if (stepTimer.click())
//     {
//       float rawBass = eq.getAveragef(0, 4);
//       float rawTreble = eq.getAveragef(eq.numBands-7, 7);
//      index = (index + 1) % FRAME_WIDTH;
//      bass[index] = rawBass * rawBass * rawBass * rawBass;
//      treble[index] = rawTreble * rawTreble;
//     }
//     else return;

//     cMid.set    (cPrev);  interpolate(t,cMid,cRand);
//     cMidNorm.set  (cMid);   setNorm(cMidNorm);
//     a1.move(); a2.move(); a3.move(); a4.move();
//    currentdeltaMs = (int) (deltaMs);
//   }

//   color CalcPoint(PVector Px) {
//     if (theta.x != 0) rotateX(Px, mCtr, tSin.x, tCos.x);
//     if (theta.y != 0) rotateY(Px, mCtr, tSin.y, tCos.y);
//     if (theta.z != 0) rotateZ(Px, mCtr, tSin.z, tCos.z);

//     Pn.set(Px); setNorm(Pn);

//     float mp  = min(Pn.x, Pn.z);
//     float yt  = map(t,0,1,.5-bnc/2,.5+bnc/2);
//     float traktortime = map(t,0,1, 1-bnc, 1+bnc);
//     float r,d;  

//     color ctemp = 0; 

//     switch (pShape.getValuei()) {
//     case 0:   int j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
//               int pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
//               ctemp = lx.hsb(
//               360 + palette.getHuef() + .8*abs(Px.x-model.cx),
//               constrain(10000/(1+abs(Px.y- model.cy)) ,0, 100), 
//               constrain(9 * (bass[pos]*model.cy*traktortime - .25*abs(Px.y - model.cy*traktortime)*rad), 0, 50)
//               );
//               ctemp = PImage.blendColor(ctemp, lx.hsb(
//               400 + palette.getHuef() + .5*abs(Px.x-model.cx),
//               constrain(10000/(1+abs(Px.y- model.cy)) ,0, 100),
//              constrain(5 * (treble[pos]*.6*model.cy*traktortime - abs(Px.y - model.cy*traktortime)*rad), 0, 50)), ADD);
//               V.set(Pn.x, yt                , Pn.z);              break;  // bouncing line
//     case 1:   j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
//               pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
//               ctemp = lx.hsb(
//               360 + palette.getHuef() + .8*abs(Px.x-model.cx),
//               100,
//               constrain(9 * (bass[pos]*model.cy*yt - abs(Px.y - model.cy*map(cos(PI*t * Pn.x),-1,1,0,2)+ 5 )*rad ), 0, 100)
//               );
//               ctemp = PImage.blendColor(ctemp, lx.hsb(
//               400 + palette.getHuef() + .5*abs(Px.x-model.cx),
//              60,
//              constrain(5 * (treble[pos]*.6*model.cy*yt - abs(Px.y - model.cy*map(cos(PI*t * Pn.x),-1,1,0,2) )*rad), 0, 100)), ADD);

//                V.set(Pn.x, map(cos(PI*t * Pn.x),-1,1,0,1)  , Pn.z);              break;  // top tap
//     case 2:    j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
//                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
//               ctemp = lx.hsb(
//               360 + palette.getHuef() + .8*abs(Px.x-model.cx),
//               100,
//               constrain(9 * (bass[pos]*model.cy*(bnc*map(Pn.x<.5?Pn.x:1-Pn.x,0,.5 ,0,t-.5)+1) - abs(Px.y - model.cy*(bnc*map(Pn.x<.5?Pn.x:1-Pn.x,0,.5 ,0,t-.5)+1))*rad ), 0, 100)
//               );
//               ctemp = PImage.blendColor(ctemp, lx.hsb(
//               400 + palette.getHuef() + .5*abs(Px.x-model.cx),
//             60,
//              constrain(5 * (treble[pos]*.6*model.cy*(bnc*map(Pn.x<.5?Pn.x:1-Pn.x,0,.5 ,0,t-.5)+ 1) - abs(Px.y - model.cy*(bnc*map(Pn.x<.5?Pn.x:1-Pn.x,0,.5 ,0,t-.5)+1))*rad ), 0, 100)), ADD);
//              V.set(Pn.x, bnc*map(Pn.x<.5?Pn.x:1-Pn.x,0,.5 ,0,t-.5)+.5, Pn.z);        break;  // V shape
//     case 3:  j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
//               pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
//               float ptime = Pn.x < cMidNorm.x ? map(Pn.x,0,cMidNorm.x, 1,2*yt) :
//                         map(Pn.x,cMidNorm.x,1, 2*yt,1); 
//               ctemp = lx.hsb(
//               360 + palette.getHuef() + .8*abs(Px.x-model.cx),
//               100,
//               constrain(9 * (bass[pos]*model.cy*ptime - abs(Px.y - model.cy*ptime )), 0, 100)
//               );
//               ctemp = PImage.blendColor(ctemp, lx.hsb(
//               400 + palette.getHuef() + .5*abs(Px.x-model.cx),
//              60,
//              constrain(5 * (treble[pos]*.6*model.cy*ptime - abs(Px.y - model.cy*ptime)), 0, 100)), ADD);
//              V.set(Pn.x, Pn.x < cMidNorm.x ? map(Pn.x,0,cMidNorm.x, .5,yt) :
//                         map(Pn.x,cMidNorm.x,1, yt,.5), Pn.z);     
//                  break;  //  Random V shape

//     case 4:    ptime = (Pn.x < cMidNorm.x ?   map(Pn.x,0,cMidNorm.x, .5,2*yt) :
//                             map(Pn.x,cMidNorm.x,1, 2*yt,.5)) +
//               .5*(Pn.z < cMidNorm.z ?   map(Pn.z,0,cMidNorm.z, .5,2*yt) :
//                             map(Pn.z,cMidNorm.z,1, 2*yt,.5));   
//               j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
//               pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
//               ctemp = lx.hsb(
//               360 + palette.getHuef() + .8*abs(Px.x-model.cx),
//               100,
//               constrain(9 * (bass[pos]*model.cy*ptime - abs(Px.y - model.cy*ptime)), 0, 100)
//               );
//               ctemp = PImage.blendColor(ctemp, lx.hsb(
//               400 + palette.getHuef() + .5*abs(Px.x-model.cx),
//             60,
//              constrain(5 * (treble[pos]*.6*model.cy*ptime - abs(Px.y - model.cy*ptime)), 0, 100)), ADD);

//                 //irrelevant code for Play, just keeping here in case I end up wanting to use later. 
//              V.set(Pn.x, .5*(Pn.x < cMidNorm.x ?   map(Pn.x,0,cMidNorm.x, .5,yt) :
//                             map(Pn.x,cMidNorm.x,1, yt,.5)) +
//               .5*(Pn.z < cMidNorm.z ?   map(Pn.z,0,cMidNorm.z, .5,yt) :
//                             map(Pn.z,cMidNorm.z,1, yt,.5)), Pn.z);    break;  //  Random Pyramid shape

//     case 5:   j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
//               pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
//               ptime = bnc*map((Pn.x-.5)*(Pn.x-.5),0,.25,0,t-.5)+1;
//               ctemp = lx.hsb(
//               360 + palette.getHuef() + .8*abs(Px.x-model.cx),
//               100,
//               constrain(9 * (bass[pos]*model.cy*ptime - abs(Px.y - model.cy*ptime)), 0, 100)
//               );
//               ctemp = PImage.blendColor(ctemp, lx.hsb(
//               400 + palette.getHuef() + .5*abs(Px.x-model.cx),
//             60,
//              constrain(5 * (treble[pos]*.6*model.cy*ptime - abs(Px.y - model.cy*ptime)), 0, 100)), ADD);

//               V.set(Pn.x, bnc*map((Pn.x-.5)*(Pn.x-.5),0,.25,0,t-.5)+.5, Pn.z);        break;  // wings
//     case 6:    j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
//                pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
//                ptime = bnc*map((mp  -.5)*(mp  -.5),0,.25,0,t-.5)+1;
//                ctemp=lx.hsb(360 + palette.getHuef() + .8*abs(Px.x-model.cx),
//               100,
//               constrain(9 * (bass[pos]*model.cy*ptime - abs(Px.y - model.cy*ptime)), 0, 100)
//               );
//               ctemp = PImage.blendColor(ctemp, lx.hsb(
//               400 + palette.getHuef() + .5*abs(Px.x-model.cx),
//             60,
//              constrain(5 * (treble[pos]*.6*model.cy*ptime - abs(Px.y - model.cy*ptime)), 0, 100)), ADD);

//               V.set(Pn.x, bnc*map((mp  -.5)*(mp  -.5),0,.25,0,t-.5)+.5, Pn.z);        break;  // wings

//     case 7:  

//               int theta = (int) constrain( (model.xMax - Px.x) / (model.xMax * FRAME_WIDTH), 0, FRAME_WIDTH-1 );
//                pos = (index + FRAME_WIDTH - theta) % FRAME_WIDTH;

//               // ctemp = lx.hsb(
//               // 360 + palette.getHuef() + .8*abs(Px.x-clockRadius.x),
//               // 100,
//               // constrain(9 * (bass[pos]*model.cy*traktortime - abs(Px.y - clockRadius.y*traktortime)/rad), 0, 100)
//               // );

//               // ctemp = PImage.blendColor(ctemp, lx.hsb(
//               // 400 + palette.getHuef() + .5*abs(Px.x-clockRadius.x),
//               // 60,
//               // constrain(5 * (treble[pos]*.6*clockRadius.y*traktortime - abs(Px.y - clockRadius.y*traktortime)/rad), 0, 100)), ADD);


//             // return PImage.blendColor(lx.hsb(lxh(),100, d), ctemp, ADD); // clock
//             return #000000; 

//     case 8:   r = amp*200 * map(bnc,0,1,1,sin(PI*t));
//             d = min(
//             distToSeg(Px.x, Px.y, a1.getX(r),a1.getY(r), a2.getX(r),a2.getY(r)),
//             distToSeg(Px.x, Px.y, a2.getX(r),a2.getY(r), a3.getX(r),a3.getY(r)),
//             distToSeg(Px.x, Px.y, a3.getX(r),a3.getY(r), a1.getX(r),a1.getY(r))       // triangle
//             );
//           d = constrain(30*(rad*40-d),0,100);
//           return lx.hsb(lxh(),100, d); // clock

//      case 9:  j= (int) constrain((model.xMax - Px.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH-1);
//               pos = (index + FRAME_WIDTH - j) % FRAME_WIDTH;
//               ctemp = lx.hsb(
//               360 + palette.getHuef() + .8*abs(Px.x-model.cx),
//               100,
//               constrain(9 * (bass[pos]*model.cy*yt - abs(Px.y - model.cy*yt+ 5)), 0, 100)
//               );
//               ctemp = PImage.blendColor(ctemp, lx.hsb(
//               400 + palette.getHuef() + .5*abs(Px.x-model.cx),
//              60,
//              constrain(5 * (treble[pos]*.6*model.cy*yt - abs(Px.y - model.cy*yt)), 0, 100)), ADD);
//              return ctemp;
//      //  r = amp*200 * map(bnc,0,1,1,sin(PI*t));
//     //       d = min(
//     //         distToSeg(Px.x, Px.y, a1.getX(r),a1.getY(r), a2.getX(r),a2.getY(r)),
//     //         distToSeg(Px.x, Px.y, a2.getX(r),a2.getY(r), a3.getX(r),a3.getY(r)),
//     //         distToSeg(Px.x, Px.y, a3.getX(r),a3.getY(r), a4.getX(r),a4.getY(r)),
//     //         distToSeg(Px.x, Px.y, a4.getX(r),a4.getY(r), a1.getX(r),a1.getY(r))       // quad
//     //       );
//     //       d = constrain(30*(rad*40-d),0,100);
//     //       return lx.hsb(lxh(),100, d); // quad

//     case 10:
//           r = map(bnc,0,1,a1.r,amp*200*sin(PI*t));
//           return lx.hsb(lxh(),100,c1c(.9+2*rad - dist(Px.x,Px.y,a1.getX(r),a1.getY(r))*.03) );    // sphere

//     case 11:
//           Px.z=mCtr.z; cMid.z=mCtr.z;
//           return lx.hsb(lxh(),100,c1c(1 - calcCone(Px,cMid,mCtr) * 0.02 > .5?1:0));         // cone

//     case 12:  return lx.hsb(lxh() + noise(Pn.x,Pn.y,Pn.z + (NoiseMove+50000)/1000.)*200,
//             85,c1c(Pn.y < noise(Pn.x + NoiseMove/2000.,Pn.z)*(1+amp)-amp/2.-.1 ? 1 : 0)); // noise

//     case 13: 
//     case 14:  float y=0; for (rWave w : waves) y += .5*w.val(Pn.x); // wave
//           V.set(Pn.x, .7+y, Pn.z);
//           break;

//     default:  return lx.hsb(0,0,0);
//     }


//      return ctemp;

//     //return  PImage.blendColor(lx.hsb(lxh() , 100, c1c(1 - V.dist(Pn)/rad)),ctemp,  OVERLAY);  


//   }
// }

;

// requires it's own vector classes, but should prob refactor to use standard vector class
// public class Raindrops extends com.symmetrylabs.pattern.SLPattern {

//   CompoundParameter numRainDrops = new CompoundParameter("NUM", -40, -500, -20);
//   CompoundParameter size = new CompoundParameter("SIZE", 0.35, 0.1, 1.0);
//   CompoundParameter speedP = new CompoundParameter("SPD", -1000, -7000, -300);

//   Vector3 randomVector3() {
//     return new Vector3(
//         random(model.xMax - model.xMin) + model.xMin,
//         random(model.yMax - model.yMin) + model.yMin,
//         random(model.zMax - model.zMin) + model.zMin);
//   }

//   class Raindrop {
//     Vector3 p;
//     Vector3 v;
//     float radius;
//     float hue;
//     float speed;

//     Raindrop() {
//       this.radius = (model.yRange*.4)*size.getValuef();
//       this.p = new Vector3(
//               random(model.xMax - model.xMin) + model.xMin,
//               model.yMax + this.radius,
//               random(model.zMax - model.zMin) + model.zMin);
//       float velMagnitude = 120;
//       this.v = new Vector3(
//           0,
//           -3 * model.yMax,
//           0);
//       this.hue = random(15) + palette.getHuef();
//       this.speed = Math.abs(speedP.getValuef());
//     }

//     // returns TRUE when this should die
//     boolean age(double ms) {
//       p.add(v, (float) (ms / this.speed));
//       return this.p.y < (0 - this.radius);
//     }
//   }

//   private float leftoverMs = 0;
//   private float msPerRaindrop = 40;
//   private List<Raindrop> raindrops;

//   public Raindrops(LX lx) {
//     super(lx);
//     addParameter(numRainDrops);
//     addParameter(size);
//     addParameter(speedP);
//     raindrops = new LinkedList<Raindrop>();
//   }

//   public void run(double deltaMs) {
//     leftoverMs += deltaMs;
//     float msPerRaindrop = Math.abs(numRainDrops.getValuef());
//     while (leftoverMs > msPerRaindrop) {
//       leftoverMs -= msPerRaindrop;
//       raindrops.add(new Raindrop());
//     }

//     for (LXPoint p : model.points) {
//       color c =
//         PImage.blendColor(
//           lx.hsb(210, 20, (float)Math.max(0, 1 - Math.pow((model.yMax - p.y) / 10, 2)) * 50),
//           lx.hsb(220, 60, (float)Math.max(0, 1 - Math.pow((p.y - model.yMin) / 10, 2)) * 100),
//           ADD);
//       for (Raindrop raindrop : raindrops) {
//         if (p.x >= (raindrop.p.x - raindrop.radius) && p.x <= (raindrop.p.x + raindrop.radius) &&
//             p.y >= (raindrop.p.y - raindrop.radius) && p.y <= (raindrop.p.y + raindrop.radius)) {
//           float d = raindrop.p.distanceTo(p) / raindrop.radius;
//   //      float value = (float)Math.max(0, 1 - Math.pow(Math.min(0, d - raindrop.radius) / 5, 2));
//           if (d < 1) {
//             c = PImage.blendColor(c, lx.hsb(raindrop.hue, 80, (float)Math.pow(1 - d, 0.01) * 100), ADD);
//           }
//         }
//       }
//       colors[p.index] = c;
//     }

//     Iterator<Raindrop> i = raindrops.iterator();
//     while (i.hasNext()) {
//       Raindrop raindrop = i.next();
//       boolean dead = raindrop.age(deltaMs);
//       if (dead) {
//         i.remove();
//       }
//     }
//   }
// }


// public class com.symmetrylabs.pattern.Noise extends com.symmetrylabs.pattern.SLPattern {

//   public final CompoundParameter scale = new CompoundParameter("Scale", 10, 5, 40);
//   public final CompoundParameter xSpeed = new CompoundParameter("XSpd", 0, -6, 6);
//   public final CompoundParameter ySpeed = new CompoundParameter("YSpd", 0, -6, 6);
//   public final CompoundParameter zSpeed = new CompoundParameter("ZSpd", 1, -6, 6);
//   public final CompoundParameter floor = new CompoundParameter("Floor", 0, -2, 2);
//   public final CompoundParameter range = new CompoundParameter("Range", 1, .2, 4);
//   public final CompoundParameter xOffset = new CompoundParameter("XOffs", 0, -1, 1);
//   public final CompoundParameter yOffset = new CompoundParameter("YOffs", 0, -1, 1);
//   public final CompoundParameter zOffset = new CompoundParameter("ZOffs", 0, -1, 1);

//   public com.symmetrylabs.pattern.Noise(LX lx) {
//     super(lx);
//     addParameter(scale);
//     addParameter(floor);
//     addParameter(range);
//     addParameter(xSpeed);
//     addParameter(ySpeed);
//     addParameter(zSpeed);
//     addParameter(xOffset);
//     addParameter(yOffset);
//     addParameter(zOffset);
//   }

//   private class Accum {
//     private float accum = 0;
//     private int equalCount = 0;
//     private float sign = 1;

//     void accum(double deltaMs, float speed) {
//       float newAccum = (float) (this.accum + this.sign * deltaMs * speed / 4000.);
//       if (newAccum == this.accum) {
//         if (++this.equalCount >= 5) {
//           this.equalCount = 0;
//           this.sign = -sign;
//           newAccum = this.accum + sign*.01;
//         }
//       }
//       this.accum = newAccum;
//     }
//   };

//   private final Accum xAccum = new Accum();
//   private final Accum yAccum = new Accum();
//   private final Accum zAccum = new Accum();

//   @Override
//   public void run(double deltaMs) {
//     xAccum.accum(deltaMs, xSpeed.getValuef());
//     yAccum.accum(deltaMs, ySpeed.getValuef());
//     zAccum.accum(deltaMs, zSpeed.getValuef());

//     float sf = scale.getValuef() / 1000.;
//     float rf = range.getValuef();
//     float ff = floor.getValuef();
//     float xo = xOffset.getValuef();
//     float yo = yOffset.getValuef();
//     float zo = zOffset.getValuef();
//     for (LXPoint p :  model.points) {
//       float b = ff + rf * noise(sf*p.x + xo + xAccum.accum, sf*p.y + yo + yAccum.accum, sf*p.z + zo + zAccum.accum);
//       colors[p.index] = palette.getColor(p, constrain(b*100, 0, 100));
//     }
//   }
// }
