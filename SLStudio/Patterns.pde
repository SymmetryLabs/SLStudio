import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static processing.core.PApplet.map;

public class Ball extends DPat {

  CompoundParameter xPos = new CompoundParameter("xPos", model.cx, model.xMin, model.xMax);
  CompoundParameter yPos = new CompoundParameter("yPos", model.cy, model.yMin, model.yMax);
  CompoundParameter zPos = new CompoundParameter("zPos", model.cz, model.zMin, model.zMax);

  CompoundParameter size = new CompoundParameter("size", model.xRange*0.1, model.xRange*0.01, model.xRange*0.5);

  public Ball(LX lx) {
    super(lx);
    addParameter(xPos);
    addParameter(yPos);
    addParameter(zPos);
    addParameter(size);
  }

  color CalcPoint(PVector p) {
    if (LXUtils.distance(p.x, p.y, xPos.getValuef(), yPos.getValuef()) < size.getValuef()) {
      return lx.hsb(lxh(), 100, 100);
    } else {
      return LXColor.BLACK;
    }
  }
}

public class Noise extends DPat {
  int       CurAnim, iSymm;
  int       XSym=1,YSym=2,RadSym=3;
  float       zTime , zTheta=0, zSin, zCos, rtime, ttime;
  CompoundParameter  pSpeed , pDensity, pSharp;
  DiscreteParameter     pChoose, pSymm;
  int       _ND = 4;
  NDat      N[] = new NDat[_ND];

  public Noise(LX lx) {
    super(lx);
    pSpeed = new CompoundParameter("Fast", .55, -2, 2);
    addParameter(pSpeed);
    pDensity  = addParam("Dens"    , .3);
    pSharp    = addParam("Shrp"    ,  0);
    pSymm     = new DiscreteParameter("Symm" , new String[] {"None", "X", "Y", "Rad"} );
    pChoose   = new DiscreteParameter("Anim", new String[] {"Drip", "Cloud", "Rain", "Fire", "Mach", "Spark","VWav", "Wave"}  );
    pChoose.setValue(6);
    addParameter(pSymm);
    addParameter(pChoose);
    //addNonKnobParameter(pSymm);
    //addNonKnobParameter(pChoose);
      //addSingleParameterUIRow(pChoose);
      //addSingleParameterUIRow(pSymm);
    for (int i=0; i<_ND; i++) N[i] = new NDat();
  }

  void onActive() { zTime = random(500); zTheta=0; rtime = 0; ttime = 0; }

  void StartRun(double deltaMs) {
    zTime   += deltaMs*(1*val(pSpeed)-.50) * .002;
    zTheta  += deltaMs*(spin()-.5)*.01  ;
    rtime += deltaMs;
    iSymm  = pSymm.getValuei();
    zSin  = sin(zTheta);
    zCos  = cos(zTheta);

    if (pChoose.getValuei() != CurAnim) {
      CurAnim = pChoose.getValuei(); ttime = rtime;
      pSpin   .reset(); zTheta    = 0;
      pDensity  .reset(); pSpeed    .reset();
      for (int i=0; i<_ND; i++) { N[i].isActive = false; }

      switch(CurAnim) {
      //               hue xz  yz  zz den mph angle
      case 0: N[0].set(0  ,75 ,75 ,150,45 ,3  ,0  );
              N[1].set(20, 25, 50, 50, 25, 1, 0 );
                    N[2].set(80, 25, 50, 50, 15, 2, 0 );
                    pSharp.setValue(1 );   break;  // drip
      case 1: N[0].set(0  ,100,100,200,45 ,3  ,180); pSharp.setValue(0 ); break;  // clouds
      case 2: N[0].set(0  ,2  ,400,2  ,20 ,3  ,0  ); pSharp.setValue(.5); break;  // rain
      case 3: N[0].set(40 ,100,100,200,10 ,1  ,180);
          N[1].set(0  ,100,100,200,10 ,5  ,180); pSharp.setValue(0 ); break;  // fire 1
      case 4: N[0].set(0  ,40 ,40 ,40 ,15 ,2.5,180);
          N[1].set(20 ,40 ,40 ,40 ,15 ,4  ,0  );
          N[2].set(40 ,40 ,40 ,40 ,15 ,2  ,90 );
                    N[3].set(60 ,40 ,40 ,40 ,15 ,3  ,-90); pSharp.setValue(.5); break; // machine
      case 5: N[0].set(0  ,400,100,2  ,15 ,3  ,90 );
          N[1].set(20 ,400,100,2  ,15 ,2.5,0  );
          N[2].set(40 ,100,100,2  ,15 ,2  ,180);
          N[3].set(60 ,100,100,2  ,15 ,1.5,270); pSharp.setValue(.5); break; // spark
      }
    }

    for (int i=0; i<_ND; i++) if (N[i].Active()) {
      N[i].sinAngle = sin(radians(N[i].angle));
      N[i].cosAngle = cos(radians(N[i].angle));
    }
  }

  color CalcPoint(PVector p) {
    color c = 0;
    rotateZ(p, mCtr, zSin, zCos);
        //rotateY(p, mCtr, ySin, yCos);
        //rotateX(p, mCtr, xSin, xCos);
    if (CurAnim == 6 || CurAnim == 7) {
      setNorm(p);
      return lx.hsb(lxh(),100, 100 * (
              constrain(1-50*(1-val(pDensity))*abs(p.y-sin(zTime*10  + p.x*(300))*.5 - .5),0,1) +
      (CurAnim == 7 ? constrain(1-50*(1-val(pDensity))*abs(p.x-sin(zTime*10  + p.y*(300))*.5 - .5),0,1) : 0))
      );
    }

    if (iSymm == XSym && p.x > mMax.x/2) p.x = mMax.x-p.x;
    if (iSymm == YSym && p.y > mMax.y/2) p.y = mMax.y-p.y;

    for (int i=0;i<_ND; i++) if (N[i].Active()) {
      NDat  n     = N[i];
      float zx    = zTime * n.speed * n.sinAngle,
          zy    = zTime * n.speed * n.cosAngle;

      float b     = (iSymm==RadSym ? (zTime*n.speed+n.xoff-p.dist(mCtr)/n.xz)
                     : noise(p.x/n.xz+zx+n.xoff,p.y/n.yz+zy+n.yoff,p.z/n.zz+n.zoff))
              *1.8;

      b +=  n.den/100 -.4 + val(pDensity) -1;
    c =   PImage.blendColor(c,lx.hsb(lxh()+n.hue,100,c1c(b)),ADD);
    }
    return c;
  }
}

public class SoundParticles extends LXPattern   {
    private final int LIMINAL_KEY = 46;
    private final int MAX_VELOCITY = 100;
    private boolean debug = false;
    private boolean doUpdate= false;
    //public  VerletPhysics physics;
    public LXProjection spinProjection;
    public  LXProjection scaleProjection;
    //private LeapMotion leap;
    public GraphicMeter eq = null;
    public  CompoundParameter spark = new CompoundParameter("Spark", 0);
    public  CompoundParameter magnitude = new CompoundParameter("Mag", 0.1, 1);
    public  CompoundParameter scale = new CompoundParameter("scale", 1, .8, 1.2);
    public  CompoundParameter spin  = new CompoundParameter("Spin", .5 , 0, 1);
    public  CompoundParameter sizeV = new CompoundParameter("Size", .33, 0, 1);
    public  CompoundParameter speed = new CompoundParameter("Speed", 16, 0, 500);
    public  CompoundParameter colorWheel = new CompoundParameter("Color", 0, 0, 360);
    public  CompoundParameter wobble = new CompoundParameter("wobble", 1, 0, 10);
    public  CompoundParameter radius = new CompoundParameter("radius", 700, 0, 1500);
    private ArrayList<Particle> particles = new ArrayList<Particle>();
    public  ArrayList<SinLFO> xPos = new ArrayList<SinLFO>();
    public  ArrayList<SinLFO> yPos = new ArrayList<SinLFO>();
    public  ArrayList<SinLFO> zPos = new ArrayList<SinLFO>();
    public  ArrayList<SinLFO> wobbleX = new ArrayList<SinLFO>();
    public  ArrayList<SinLFO> wobbleY = new ArrayList<SinLFO>();
    public  ArrayList<SinLFO> wobbleZ = new ArrayList<SinLFO>();
    public  PVector startVelocity = new PVector();
    private PVector modelCenter = new PVector();
    public  SawLFO angle = new SawLFO(0, TWO_PI, 1000);
    private float[] randomFloat = new float[model.points.length];
    private float[] freqBuckets;
    // private float lastParticleBirth = millis();
    // private float lastTime = millis();
    // private float lastTransmitEQ = millis();
    private int prints = 0;
    private float sparkX = 0.;
    private float sparkY = 0.;
    private float sparkZ = 0.;
    private float midiToHz(int key )  {
          return (float) (440 * pow(2, (key - 69) / 12));
          }
    private float midiToAngle(int key )   {
           return (2 * PI / 24) * key;
          }
    private float randctr (float a) { return random(a) - a*.5; }

    ArrayList<MidiNoteStamp> lfoNotes = new ArrayList<MidiNoteStamp>();
    MidiNote[] particleNotes = new MidiNote[128];

      class MidiNoteStamp {
        MidiNote note;
        float timestamp;
          MidiNoteStamp(MidiNote _note) {
          note =_note;
          timestamp = millis()* .001;
          }
        }
       class Particle  {
        //VerletParticle verletParticle;
        PVector position= new PVector();
        PVector velocity= new PVector();
        PVector distance = new PVector();
        PVector modDist = new PVector();
        float hue;
        float life;
        float intensity;
        float falloff;
        int i = 0;
          Particle(PVector pos, PVector vel) {
           // verletParticle= new VerletParticle(pos.x, pos.y, pos.z);
            position.set(pos);
            velocity.set(vel);
            life=1;
            intensity=1;
            falloff=1;
            hue =220.;
            i = particles.size();
            float rand = randomGaussian();
            float rand2 = randomGaussian();
            SinLFO x = new SinLFO(-rand*20, rand2*20,1500+ 500*rand2);
            addModulator(x).trigger();
            xPos.add(x);
            SinLFO y = new SinLFO(-rand2*20, rand*20, 1500 + 500*rand2);
            addModulator(y).trigger();
            yPos.add(y);

           // yPos.add(new SinLFO(0,model.yMax/10, 10000));
            //zPos.add(new SinLFO(-model.zMax/10,model.zMax/10, 1000));

            wobbleX.add(new SinLFO(6000, 1000, 2000));


           //  if (random(0,1)<.5){
           //  xPos.get(i).trigger();
           // // xPos.get(i).setPeriod(wobbleX.get(i)).trigger();
           //  yPos.get(i).trigger();
           //  }
           //  else {
           //  xPos.get(i).trigger();
           //  //yPos.get(i).setPeriod(wobbleX.get(i)).trigger();
           //  xPos.get(i).trigger();
           //  }

           // zPos.get(i).trigger();
          }
          Particle(PVector pos, PVector vel, MidiNote note) {
           // verletParticle= new VerletParticle(pos.x, pos.y, pos.z);
            position.set(pos);
            velocity.set(vel);
            life=1;
            intensity=1;
            falloff=1;
            this.hue=220.;
          }
          public  boolean isActive() {
             if (abs(this.position.dist(modelCenter)) >= radius.getValuef()) {
              if( millis() %100 < 5 ) {
              //println("particle distance to center:  " +   abs(this.position.dist(modelCenter)));
              //println("particle distance:  " +  distance);
              };
              //  println("position" + this.position + "modelCenter:  " +   modelCenter);
              //  println("particle inactive");
                return false;
                }
              else
                //println("position" + this.position + "modelCenter:  " +   modelCenter);
                //println("particle active");
                return true;
              }

          public void respawn() {
            //this.position.set(modelCenter.mult(random(.5,1.2)));
            this.position.set(model.cx, model.cy, model.cz);
           // this.velocity.set(0,0,0);
            this.hue=120 + randomGaussian()*30;
          }

          public  color calcPoint(LXPoint p ) {

              return lx.hsb(0,0,0);
          }
          public  void run(double deltaMs) {
            if (!this.isActive()){
            respawn();
            }

            float spinNow = spin.getValuef();
            float sparkf = spark.getValuef();
            float clock = 0.001 * millis();

            if (spinNow != 0){
                if (spinNow > 0) {angle.setRange((double)0.0, (double)TWO_PI, 1000-spinNow);}
                else if (spinNow < 0) {angle.setRange(angle.getValuef(), angle.getValuef() - TWO_PI, (double) 1000 - spinNow);}

             spinProjection
             .center()
             .rotate((spinNow - .5) / 100 , 0,0,1)
             .translate(model.cx, model.cy, model.cz);
            //  .scale(scale.getValuef(),scale.getValuef(),scale.getValuef());
             }

            float move = ((float)deltaMs/ 1000)*speed.getValuef();
            PVector distance = PVector.mult(velocity, move);
            position.add(distance);
            //modDist.set(PVector.random3D().setMag(10));
             modDist.set(xPos.get(i).getValuef()/2, yPos.get(i).getValuef()/2);

            //modDist.set(sin((float)deltaMs/1000)*2,sin((float)deltaMs/1000 + PI/4)*2);
            position.add(modDist);

            // for (MidiNoteStamp noteStamp : lfoNotes) {
            //         MidiNote note = noteStamp.note;
            //        int key = noteStamp.note.getPitch();
            //        float lfoAge = clock - noteStamp.timestamp;
            //        float hz = midiToHz(key);
            //        float lfoAngle = midiToAngle(key);

            //        float wobbleAmp = (float) Math.pow(note.getVelocity() / MAX_VELOCITY, 2.0);

            //        wobbleAmp/= (1 + lfoAge);

            //       // position.add(wobbleAmp*wobble.getValuef()*cos(clock*lfoAngle), wobbleAmp*wobble.getValuef()*sin(clock*lfoAngle), 0);
            // }

                float size = sizeV.getValuef();
                float avgBass = eq.getAveragef(0,4);
                float avgMid = eq.getAveragef(6,6);
                float avgTreble= eq.getAveragef(12,6);

                float hueShift = 10;
                hueShift = hueShift * avgTreble*10;


              //for (LXPoint p : model.points) {
                int i = 0;
              for (LXVector p : spinProjection) {
                 float randomX = randomFloat[i];
                 //float randomY = randctr(20);
                 //float randomZ = randctr(20);
                 float sparkle = randomX*sparkf;
              // asin(p.y-position.y/ dist(p.x, p.y,position.x, position.y));

                //debugFloat("hue", hue, 100);

               // float b =0;
               // float thetaP = atan2((p.y - position.y), (p.x - position.x));  //too slow
               // float b = 100 - (pow(p.x-(position.x),2) + pow(p.y - (position.y), 2) + pow(p.z - (position.z), 2))/((10+6*avgBass)*size);
                float b = 100 - (pow(p.x-(position.x + sparkle),2) + pow(p.y - (position.y + sparkle), 2) + pow(p.z - (position.z+ randomX*sparkle), 2))/((10+6*avgBass)*size);

                if (b >0){
                  blendColor(p.index, lx.hsb(this.hue+hueShift, map(1-avgTreble, 0,1, 0, 100), b), LXColor.Blend.ADD);
                }
               i++;
            }
           position.sub(modDist);
          }
        }
    public  SoundParticles(LX lx) {
      super(lx);
      for (int i = 0 ; i < model.points.length; i++) {
        randomFloat[i]=randomGaussian()*10;
      }
      //physics=new VerletPhysics();
      //physics.addBehavior(new GravityBehavior(new Vec3D(0,0,0.5)));
      //physics.setWorldBounds(new AABB(new Vec3D(model.cx, model.cy, model.cz),model.xMax));
      spinProjection = new LXProjection(model);
      scaleProjection = new LXProjection(model);
     // leap= new LeapMotion(parent).withGestures();
     addParameter(spark);
      addParameter(magnitude);
      addParameter(sizeV);
      //addParameter(scale);
      addParameter(speed);
      addParameter(spin);
      addParameter(colorWheel);
      addParameter(wobble);
      addParameter(radius);
      addModulator(angle).trigger();
      //addModulator(xPos).trigger();
      //addModulator(yPos).trigger();
      //addModulator(zPos).trigger();

      modelCenter.set(model.cx, model.cy, model.cz);

      // println("modelCenter = " + modelCenter);
      // println("model.cx:  " + model.cx + "model.cy:  " + model.cy + "model.cz:  " + model.cz);

      }

    public boolean noteOn(MidiNote note)  {

        if (note.getPitch() < LIMINAL_KEY ){
            lfoNotes.add(new MidiNoteStamp(note));
            return false;
          }

        float angle = map(note.getPitch(), 30, 50,  0, TWO_PI);
        float velocity = map(note.getVelocity(), 0, 127, 0, 1);                                           ;
        particles.add( new Particle(modelCenter.add(new PVector(random(-model.xMax/4,model.xMax/4), random(-model.yMax/4, model.yMax/4), 0)), new PVector( cos(angle)*velocity, sin(angle)*velocity, 0)));


        return false;
    }

    public synchronized void keyEvent(KeyEvent keyEvent){
    if (!(keyEvent.getAction() == KeyEvent.PRESS)) {return;}
      char key = keyEvent.getKey();
      switch (key) {
          case 'P': particles.add(new Particle(new PVector(random(.5*model.cx, 3*model.cx/2), random(.5*model.cy,3*model.cy/2), model.cz),
                    new PVector(random(-1,1), random(-1,1), random(-1,1))));
                    println("number of active particles:  "  +  particles.size());
                    break;

          case 'O':
                   break;
          case 'S':
                    break;


        }
    }

    private void debugFloat(String name, float num, float interval) {
      if (prints > 500) return;
      if (prints < 500) { // && ((lastTime-millis()) >= interval)){
      println(name + num);
      }
      prints++;
      //lastTime += millis();
    }

    public void sendEQ(GraphicMeter eq) {
      // OscMessage message = new OscMessage("/eq");
      // message.add(eq.getAveragef(0,4));
      // message.add(eq.getAveragef(8,6));
      // oscEngine.sendOscMessage(message);
      }

    public void onParameterChanged(LXParameter p ) {
      if (p == wobble){
        for (SinLFO x : xPos){
          x.setRangeFromHereTo(p.getValuef()*100 + randomGaussian()*10);
        }
        for (SinLFO y : yPos){
          y.setRangeFromHereTo(p.getValuef()*100 + randomGaussian()*10);
        }
        return;
       }
      if (p == colorWheel) {
        float val = p.getValuef();
        // OscMessage message = new OscMessage("/midi/cc");
        // message.add(0);
        // message.add(2);
        // message.add((int)map(val, 0, 1, 0, 127));
        // oscEngine.sendOscMessage(message);
        }
        return;
      }


    PVector randomVector() { return new PVector(random(-model.xMax/4, model.xMax/4), random(-model.yMax/4, model.yMax/4), 0);}



    public void onActive()  {
      if (eq == null ) {
        eq = new GraphicMeter(lx.engine.audio.getInput());
        eq.slope.setValue(6);
        eq.range.setValue(36);
        eq.attack.setValue(10);
        eq.release.setValue(640);
        eq.gain.setValue(.3);
        //addParameter(eq.gain);
        // addParameter(eq.range);
        // addParameter(eq.attack);
        // addParameter(eq.release);
        //addParameter(eq.slope);
        addModulator(eq).start();
        freqBuckets = new float[eq.numBands];
      }
     // uiDebugText.setText("Press P to add Particles", "S to add springs");

      for (int i=0; i<100; i++){
     // particles.add(new Particle(PVector.random3D().setMag(model.xMax/2).add(modelCenter), PVector.random3D().setMag(.1)));
      particles.add(new Particle(modelCenter.add(randomVector().setMag(50.)), PVector.random3D()));

      }
    }

    public void run(final double deltaMs) {
     setColors(0);
     if (doUpdate) {
     // physics.update();
      }
     if (eq != null) {
        // if ( millis() - lastTransmitEQ > 150 ) {
        //   //sendEQ(eq);
        //   lastTransmitEQ = millis();
       // }
      }

     sparkX = randctr(20);
     sparkY = randctr(20);
     sparkZ = randctr(20);

     // TODO Threadding: the particles don't like being parallelized
      particles.stream().forEach(new Consumer<Particle>() {
        @Override
        public void accept(final Particle p) {
          p.run(deltaMs);
        }
      });

    // for (Iterator<Particle> iter = particles.iterator(); iter.hasNext();) {

    //     Particle p = iter.next();
    //     if (!p.isActive()) {
    //      p.respawn();
    //      println("particle respawned");
    //     //iter.remove();
    //    // println("particle removed: ");
    //     } else {
    //     p.run(deltaMs);
    //     }
    //   }
    }



 }

public class StripPlay extends SLPattern {
  private final int NUM_OSC = 300;
  private final int MAX_PERIOD = 20000;
  private CompoundParameter brightParameter = new CompoundParameter("bright", 96, 70, 100);
  private CompoundParameter hueSpread = new CompoundParameter("hueSpread", 1);
  private CompoundParameter speed = new CompoundParameter("speed", .5);
  private CompoundParameter xSpeed = new CompoundParameter("xSpeed", 2000, 500, MAX_PERIOD);
  private CompoundParameter ySpeed = new CompoundParameter("ySpeed", 1600, 500, MAX_PERIOD);
  private CompoundParameter zSpeed = new CompoundParameter("zSpeed", 1000, 500, MAX_PERIOD);
  private DiscreteParameter numOsc = new DiscreteParameter("Strips", 179, 1, NUM_OSC);


  SinLFO[] fX = new SinLFO[NUM_OSC]; //new SinLFO(0, model.xMax, 5000);
  SinLFO[] fY = new SinLFO[NUM_OSC]; //new SinLFO(0, model.yMax, 4000);
  SinLFO[] fZ = new SinLFO[NUM_OSC]; //new SinLFO(0, model.yMax, 3000);
  SinLFO[] sat = new SinLFO[NUM_OSC];
  float[] colorOffset = new float[NUM_OSC];

  public StripPlay(LX lx) {
    super(lx);
    addParameter(brightParameter);
    addParameter(numOsc);
    addParameter(hueSpread);
    addParameter(speed);
    addParameter(xSpeed);
    addParameter(ySpeed);
    addParameter(zSpeed);

      for (int i=0;i<NUM_OSC;i++) {
        fX[i] = new SinLFO(model.xMin, model.xMax, random(2000,MAX_PERIOD));
        fY[i] = new SinLFO(model.yMin, model.yMax, random(2000,MAX_PERIOD));
        fZ[i] = new SinLFO(model.zMin, model.zMax, random(2000,MAX_PERIOD));
        sat[i] = new SinLFO(80, 100, random(2000,5000));
        addModulator(fX[i]).trigger();
        addModulator(fY[i]).trigger();
        addModulator(fZ[i]).trigger();
        colorOffset[i]= sin(random(-PI, PI))*40;
      }
  }

  public void onParameterChanged(LXParameter parameter) {
    if (parameter == xSpeed) {
      for (int i = 0; i < NUM_OSC; i++) {
        fX[i].setPeriod(MAX_PERIOD + 1 - xSpeed.getValue());
      }
    } else if (parameter == ySpeed) {
      for (int i = 0; i < NUM_OSC; i++) {
        fY[i].setPeriod(MAX_PERIOD + 1  - ySpeed.getValue());
      }
    } else if (parameter == zSpeed) {
      for (int i = 0; i < NUM_OSC; i++) {
        fZ[i].setPeriod(MAX_PERIOD + 1  - zSpeed.getValue());
      }
    }  else if (parameter == hueSpread) {
      for (int i = 0; i < NUM_OSC; i++) {
        colorOffset[i] = colorOffset[i]*hueSpread.getValuef();
      }
    }
  }

  public void run(double deltaMs) {
    setColors(#000000);
    float[] bright = new float[model.points.length];
    for (Strip strip : model.strips) {
      LXPoint centerPoint = strip.points[8];
      for (int i=0;i<numOsc.getValue();i++) {
        float avgdist = dist(centerPoint.x,centerPoint.y,centerPoint.z,fX[i].getValuef(),fY[i].getValuef(),fZ[i].getValuef());
        boolean on = avgdist<30;
        if (on) {
          float hv = palette.getHuef()+colorOffset[i];
          float br = max(0,100-avgdist*2*(100 - brightParameter.getValuef()));
          int colr = lx.hsb(hv, sat[i].getValuef(), br);
          for (LXPoint p : strip.points) {
            if (br>bright[p.index]) {
              //colors[p.index] = lx.hsb(hv,sat[i].getValuef(),br);
              addColor(p.index, colr);
              bright[p.index] = br;
            }
          }
        }
      }
    }
  }
}

public class Pong extends DPat {
  SinLFO x,y,z,dx,dy,dz;
  float cRad; CompoundParameter pSize;
  DiscreteParameter   pChoose;
  PVector v = new PVector(), vMir =  new PVector();

  public Pong(LX lx) {
    super(lx);
    cRad = mMax.x/10;
    addModulator(dx = new SinLFO(6000,  500, 30000  )).trigger();
    addModulator(dy = new SinLFO(3000,  500, 22472  )).trigger();
    addModulator(dz = new SinLFO(1000,  500, 18420  )).trigger();
    addModulator(x  = new SinLFO(cRad, mMax.x - cRad, 0)).trigger();  x.setPeriod(dx);
    addModulator(y  = new SinLFO(cRad, mMax.y - cRad, 0)).trigger();  y.setPeriod(dy);
    addModulator(z  = new SinLFO(cRad, mMax.z - cRad, 0)).trigger();  z.setPeriod(dz);
      pSize = addParam  ("Size"     , 0.4 );
      pChoose = new DiscreteParameter("Anim", new String[] {"Pong", "Ball", "Cone"} );
      pChoose.setValue(2);
      addParameter(pChoose);
      //addNonKnobParameter(pChoose);
      //addSingleParameterUIRow(pChoose);
    removeParameter(pRotX);
    removeParameter(pRotY);
    removeParameter(pRotZ);
    removeParameter(pRotX);
    removeParameter(pSpin);
  }

  void    StartRun(double deltaMs)  { cRad = mMax.x*val(pSize)/6; }
  color CalcPoint(PVector p)      {
    v.set(x.getValuef(), y.getValuef(), z.getValuef());
    v.z=0;p.z=0;// ignore z dimension
    switch(pChoose.getValuei()) {
    case 0: vMir.set(mMax); vMir.sub(p);
        return lx.hsb(lxh(),100,c1c(1 - min(v.dist(p), v.dist(vMir))*.5/cRad));   // balls
    case 1: return lx.hsb(lxh(),100,c1c(1 - v.dist(p)*.5/cRad));              // ball
    case 2: vMir.set(mMax.x/2,0,mMax.z/2);
        return lx.hsb(lxh(),100,c1c(1 - calcCone(p,v,vMir) * max(.02,.45-val(pSize))));   // spot
    }
    return lx.hsb(0,0,0);
  }
}

public class BassPod extends LXPattern {

  private LXAudioInput audioInput = lx.engine.audio.getInput();
  private GraphicMeter eq = new GraphicMeter(audioInput);

  private final CompoundParameter clr = new CompoundParameter("CLR", 0.5);

  public BassPod(LX lx) {
    super(lx);
    addParameter(clr);
    // addParameter(eq.gain);
    // addParameter(eq.range);
    // addParameter(eq.attack);
    // addParameter(eq.release);
    // addParameter(eq.slope);
    addModulator(eq).start();
  }

  void onActive() {
    eq.range.setValue(36);
    eq.release.setValue(300);
    eq.gain.setValue(-6);
    eq.slope.setValue(6);
  }

  public void run(double deltaMs) {
    final float bassLevel = eq.getAveragef(0, 5);
    final float satBase = bassLevel*480*clr.getValuef();

    Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
      @Override
      public void accept(final LXPoint p) {
        int avgIndex = (int) constrain(1 + abs(p.x-model.cx)/(model.cx)*(eq.numBands-5), 0, eq.numBands-5);
        float value = 0;
        for (int i = avgIndex; i < avgIndex + 5; ++i) {
          value += eq.getBandf(i);
        }
        value /= 5.;

        float b = constrain(8 * (value*model.yMax - abs(p.y-model.yMax/2.)), 0, 100);
        colors[p.index] = lx.hsb(
          palette.getHuef() + abs(p.y - model.cy) + abs(p.x - model.cx),
          constrain(satBase - .6*dist(p.x, p.y, model.cx, model.cy), 0, 100),
          b
        );
      }
    });
  }
}

public class CubeEQ extends LXPattern {

  private LXAudioInput audioInput = lx.engine.audio.getInput();
  private GraphicMeter eq = new GraphicMeter(audioInput);

  private final CompoundParameter edge = new CompoundParameter("EDGE", 0.5);
  private final CompoundParameter clr = new CompoundParameter("CLR", 0.1, 0, .5);
  private final CompoundParameter blockiness = new CompoundParameter("BLK", 0.5);

  public CubeEQ(LX lx) {
    super(lx);
    // addParameter(eq.range);
    // addParameter(eq.attack);
    // addParameter(eq.release);
    // addParameter(eq.slope);
    addParameter(edge);
    addParameter(clr);
    addParameter(blockiness);
    addModulator(eq).start();
  }

  void onActive() {
    eq.range.setValue(48);
    eq.release.setValue(300);
  }

  public void run(double deltaMs) {
    final float edgeConst = 2 + 30*edge.getValuef();
    final float clrConst = 1.1 + clr.getValuef();

    Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
      @Override
      public void accept(final LXPoint p) {
        float avgIndex = constrain(2 + p.x / model.xMax * (eq.numBands-4), 0, eq.numBands-4);
        int avgFloor = (int) avgIndex;

        float leftVal = eq.getBandf(avgFloor);
        float rightVal = eq.getBandf(avgFloor+1);
        float smoothValue = lerp(leftVal, rightVal, avgIndex-avgFloor);

        float chunkyValue = (
          eq.getBandf(avgFloor/4*4) +
            eq.getBandf(avgFloor/4*4 + 1) +
            eq.getBandf(avgFloor/4*4 + 2) +
            eq.getBandf(avgFloor/4*4 + 3)
        ) / 4.;

        float value = lerp(smoothValue, chunkyValue, blockiness.getValuef());

        float b = constrain(edgeConst * (value*model.yMax - p.y), 0, 100);
        colors[p.index] = lx.hsb(
          480 + palette.getHuef() - min(clrConst*p.y, 120),
          100,
          b
        );
      }
    });
  }
}

public class Swarm extends SLPattern {

  SawLFO offset = new SawLFO(0, 1, 1000);
  SinLFO rate = new SinLFO(350, 1200, 63000);
  SinLFO falloff = new SinLFO(15, 50, 17000);
  SinLFO fX = new SinLFO(model.xMin, model.xMax, 19000);
  SinLFO fY = new SinLFO(model.yMin, model.yMax, 11000);
  SinLFO hOffX = new SinLFO(model.xMin, model.xMax, 13000);

  public Swarm(LX lx) {
    super(lx);

    addModulator(offset).trigger();
    addModulator(rate).trigger();
    addModulator(falloff).trigger();
    addModulator(fX).trigger();
    addModulator(fY).trigger();
    addModulator(hOffX).trigger();
    offset.setPeriod(rate);
  }

  float modDist(float v1, float v2, float mod) {
    v1 = v1 % mod;
    v2 = v2 % mod;
    if (v2 > v1) {
      return min(v2-v1, v1+mod-v2);
    }
    else {
      return min(v1-v2, v2+mod-v1);
    }
  }

  void run(double deltaMs) {
    float s = 0;
    model.strips.parallelStream().forEach(new Consumer<Strip>() {
      @Override
      public void accept(final Strip strip) {
        float s = model.strips.indexOf(strip);
        int i = 0;
        for (LXPoint p : strip.points) {
          float fV = max(-1, 1 - dist(p.x/2., p.y, fX.getValuef()/2., fY.getValuef()) / 64.);
          // println("fv: " + fV);
          colors[p.index] = lx.hsb(
            palette.getHuef() + 0.3 * abs(p.x - hOffX.getValuef()),
            constrain(80 + 40 * fV, 0, 100),
            constrain(100 -
              (30 - fV * falloff.getValuef()) * modDist(i + (s*63)%61, offset.getValuef() * strip.metrics.numPoints, strip.metrics.numPoints), 0, 100)
          );
          ++i;
        }
      }
    });
  }
}

public class SpaceTime extends SLPattern {

  SinLFO pos = new SinLFO(0, 1, 3000);
  SinLFO rate = new SinLFO(1000, 9000, 13000);
  SinLFO falloff = new SinLFO(10, 70, 5000);
  float angle = 0;

  CompoundParameter rateParameter = new CompoundParameter("RATE", 0.5);
  CompoundParameter sizeParameter = new CompoundParameter("SIZE", 0.5);

  public SpaceTime(LX lx) {
    super(lx);

    addModulator(  pos).trigger();
    addModulator(rate).trigger();
    addModulator(falloff).trigger();
    pos.setPeriod(rate);
    addParameter(rateParameter);
    addParameter(sizeParameter);
  }

  public void onParameterChanged(LXParameter parameter) {
    if (parameter == rateParameter) {
      rate.stop();
      rate.setValue(9000 - 8000*parameter.getValuef());
    }  else if (parameter == sizeParameter) {
      falloff.stop();
      falloff.setValue(70 - 60*parameter.getValuef());
    }
  }

  void run(double deltaMs) {
    angle += deltaMs * 0.0007;
    final float sVal1 = model.strips.size() * (0.5 + 0.5*sin(angle));
    final float sVal2 = model.strips.size() * (0.5 + 0.5*cos(angle));

    final float pVal = pos.getValuef();
    final float fVal = falloff.getValuef();

    model.strips.parallelStream().forEach(new Consumer<Strip>() {
      @Override
      public void accept(final Strip strip) {
        int s = model.strips.indexOf(strip);
        int i = 0;
        for (LXPoint p : strip.points) {
          colors[p.index] = lx.hsb(
            palette.getHuef() + 360 - p.x*.2 + p.y * .3,
            constrain(.4 * min(abs(s - sVal1), abs(s - sVal2)), 20, 100),
            max(0, 100 - fVal*abs(i - pVal*(strip.metrics.numPoints - 1)))
          );
          ++i;
        }
      }
    });
  }
}

public class Traktor extends LXPattern {

  private LXAudioInput audioInput = lx.engine.audio.getInput();
  private GraphicMeter eq = new GraphicMeter(audioInput);

  final int FRAME_WIDTH = 120;

  final CompoundParameter speed = new CompoundParameter("SPD", 0.5);
  final CompoundParameter hueSpread = new CompoundParameter("hueSpread", .4, 0, 1);
  final CompoundParameter trebleGain= new CompoundParameter("trebG", 1, 0, 10);
  final CompoundParameter bassGain = new CompoundParameter("bassG", 1, 0, 10);
  private float[] bass = new float[FRAME_WIDTH];
  private float[] treble = new float[FRAME_WIDTH];

  private int index = 0;

  public Traktor(LX lx) {
    super(lx);
    for (int i = 0; i < FRAME_WIDTH; ++i) {
      bass[i] = 0;
      treble[i] = 0;
    }
    addParameter(speed);
    addParameter(hueSpread);
    addParameter(bassGain);
    addParameter(trebleGain);
    addModulator(eq).start();
  }

  public void onActive() {
    eq.slope.setValue(6);
    eq.gain.setValue(12);
    eq.range.setValue(36);
    eq.release.setValue(200);
    // addParameter(eq.gain);
    // addParameter(eq.range);
    // addParameter(eq.attack);
    // addParameter(eq.release);
    // addParameter(eq.slope);
  }

  int counter = 0;

  public void run(double deltaMs) {

    int stepThresh = (int) (40 - 39*speed.getValuef());
    counter += deltaMs;
    if (counter < stepThresh) {
      return;
    }
    counter = counter % stepThresh;

    index = (index + 1) % FRAME_WIDTH;

    final float rawBass = eq.getAveragef(0, 4);
    final float rawTreble = eq.getAveragef(eq.numBands-7, 7);

    bass[index] = rawBass * rawBass * rawBass * rawBass;
    treble[index] = rawTreble * rawTreble;
    final float hueV = hueSpread.getValuef();
    final float bassG = bassGain.getValuef();
    final float trebG = trebleGain.getValuef();

    Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
      @Override
      public void accept(final LXPoint p) {
        int i = (int) constrain((model.xMax - p.x) / model.xMax * FRAME_WIDTH, 0, FRAME_WIDTH - 1);
        int pos = (index + FRAME_WIDTH - i) % FRAME_WIDTH;

        colors[p.index] = lx.hsb(
          360 + palette.getHuef() + .8 * hueV * abs(p.x - model.cx),
          100,
          constrain(9 * bassG * (bass[pos] * model.cy - abs(p.y - model.cy + 5)), 0, 100)
        );
        blendColor(p.index, lx.hsb(
          400 + palette.getHuef() + .5 * hueV * abs(p.x - model.cx),
          60,
          constrain(7 * trebG * (treble[pos] * .6 * model.cy - abs(p.y - model.cy)), 0, 100)

        ), LXColor.Blend.ADD);
      }
    });
  }
}

public class AskewPlanes extends DPat {

  CompoundParameter thickness = new CompoundParameter("thck", 0.2, 0.1, 0.9);
  float huev = 0;

  DiscreteParameter numPlanes = new DiscreteParameter("num" , new String[] {"3", "2", "1"});

  class Plane {
    private final SinLFO a;
    private final SinLFO b;
    private final SinLFO c;
    float av = 1;
    float bv = 1;
    float cv = 1;
    float denom = 0.1;

    Plane(int i) {
      addModulator(a = new SinLFO(-1, 1, 4000 + 1029*i)).trigger();
      addModulator(b = new SinLFO(-1, 1, 11000 - 1104*i)).trigger();
      addModulator(c = new SinLFO(-50, 50, 4000 + 1000*i * ((i % 2 == 0) ? 1 : -1))).trigger();
    }

    void run(double deltaMs) {
      av = a.getValuef();
      bv = b.getValuef();
      cv = c.getValuef();
      denom = sqrt(av*av + bv*bv);
    }
  }

  final Plane[] planes;
  final int NUM_PLANES = 3;

  public AskewPlanes(LX lx) {
    super(lx);
    addParameter(thickness);
    planes = new Plane[NUM_PLANES];
    for (int i = 0; i < planes.length; ++i) {
      planes[i] = new Plane(i);
    }
    pTransX.setValue(1);
    addParameter(numPlanes);
    removeParameter(pRotX);
    removeParameter(pRotY);
    removeParameter(pRotZ);
    removeParameter(pRotX);
    removeParameter(pSpin);
  }

  void StartRun(double deltaMs) {
    huev = palette.getHuef();

    // This is super fucking bizarre. But if this is a for loop, the framerate
    // tanks to like 30FPS, instead of 60. Call them manually and it works fine.
    // Doesn't make ANY sense... there must be some weird side effect going on
    // with the Processing internals perhaps?
//    for (Plane plane : planes) {
//      plane.run(deltaMs);
//    }
    planes[0].run(deltaMs);
    planes[1].run(deltaMs);
    planes[2].run(deltaMs);
  }

  color CalcPoint(PVector p) {
    //for (LXPoint p : model.points) {
      float d = MAX_FLOAT;

      int i = 0;
      for (Plane plane : planes) {
        if (i++ <= numPlanes.getValuei()-1) continue;
        if (plane.denom != 0) {
          d = min(d, abs(plane.av*(p.x-model.cx) + plane.bv*(p.y-model.cy) + plane.cv) / plane.denom);
        }
      }
      return lx.hsb(
        huev + abs(p.x-model.cx)*.3 + p.y*.8,
        max(0, 100 - .15*abs(p.x - model.cx)),
        constrain(700.*thickness.getValuef() - 10.*d, 0, 100)
      );
    //}
  }
}

public class ShiftingPlane extends LXPattern {

  final CompoundParameter hueShift = new CompoundParameter("hShift", 0.5, 0, 1);

  final SinLFO a = new SinLFO(-.2, .2, 5300);
  final SinLFO b = new SinLFO(1, -1, 13300);
  final SinLFO c = new SinLFO(-1.4, 1.4, 5700);
  final SinLFO d = new SinLFO(-10, 10, 9500);

  public ShiftingPlane(LX lx) {
    super(lx);
    addParameter(hueShift);
    addModulator(a).trigger();
    addModulator(b).trigger();
    addModulator(c).trigger();
    addModulator(d).trigger();
  }

  public void run(double deltaMs) {
    float hv = palette.getHuef();
    float av = a.getValuef();
    float bv = b.getValuef();
    float cv = c.getValuef();
    float dv = d.getValuef();
    float denom = sqrt(av*av + bv*bv + cv*cv);

    for (LXPoint p : model.points) {
      float d = abs(av*(p.x-model.cx) + bv*(p.y-model.cy) + cv*(p.z-model.cz) + dv) / denom;
      colors[p.index] = lx.hsb(
        hv + (abs(p.x-model.cx)*.6 + abs(p.y-model.cy)*.9 + abs(p.z - model.cz))*hueShift.getValuef(),
        constrain(110 - d*6, 0, 100),
        constrain(130 - 7*d, 0, 100)
      );
    }
  }
}

public class SunFlash extends SLPattern {
  private CompoundParameter rateParameter = new CompoundParameter("RATE", 0.125);
  private CompoundParameter attackParameter = new CompoundParameter("ATTK", 0.5);
  private CompoundParameter decayParameter = new CompoundParameter("DECAY", 0.5);
  private CompoundParameter hueVarianceParameter = new CompoundParameter("H.V.", 0.25);
  private CompoundParameter saturationParameter = new CompoundParameter("SAT", 0.5);

  class Flash {
    Sun c;
    float value;
    float hue;
    boolean hasPeaked;

    Flash() {
      c = model.suns.get(floor(random(model.suns.size())));
      hue = palette.getHuef() + (random(1) * 120 * hueVarianceParameter.getValuef());
      boolean infiniteAttack = (attackParameter.getValuef() > 0.999);
      hasPeaked = infiniteAttack;
      value = (infiniteAttack ? 1 : 0);
    }

    // returns TRUE if this should die
    boolean age(double ms) {
      if (!hasPeaked) {
        value = value + (float) (ms / 1000.0f * ((attackParameter.getValuef() + 0.01) * 5));
        if (value >= 1.0) {
          value = 1.0;
          hasPeaked = true;
        }
        return false;
      } else {
        value = value - (float) (ms / 1000.0f * ((decayParameter.getValuef() + 0.01) * 10));
        return value <= 0;
      }
    }
  }

  private float leftoverMs = 0;
  private List<Flash> flashes;

  public SunFlash(LX lx) {
    super(lx);
    addParameter(rateParameter);
    addParameter(attackParameter);
    addParameter(decayParameter);
    addParameter(hueVarianceParameter);
    addParameter(saturationParameter);
    flashes = new LinkedList<Flash>();
  }

  public void run(double deltaMs) {
    leftoverMs += deltaMs;
    float msPerFlash = 1000 / ((rateParameter.getValuef() + .01) * 100);
    while (leftoverMs > msPerFlash) {
      leftoverMs -= msPerFlash;
      flashes.add(new Flash());
    }

    for (LXPoint p : model.points) {
      colors[p.index] = 0;
    }

    flashes.parallelStream().forEach(new Consumer<Flash>() {
      @Override
      public void accept(final Flash flash) {
        color c = lx.hsb(flash.hue, saturationParameter.getValuef() * 100, (flash.value) * 100);
        for (LXPoint p : flash.c.points) {
          colors[p.index] = c;
        }
      }
    });

    Iterator<Flash> i = flashes.iterator();
    while (i.hasNext()) {
      Flash flash = i.next();
      boolean dead = flash.age(deltaMs);
      if (dead) {
        i.remove();
      }
    }
  }
}

public class Spheres extends LXPattern {
  private CompoundParameter hueParameter = new CompoundParameter("RAD", 1.0);
  private CompoundParameter periodParameter = new CompoundParameter("PERIOD", 4000.0, 200.0, 10000.0);
  private CompoundParameter hueVariance = new CompoundParameter("HueVar", 50, 0, 180);
  private final SawLFO lfo = new SawLFO(0, 1, 10000);
  private final SinLFO sinLfo = new SinLFO(0, 1, periodParameter);
  private final float centerX, centerY, centerZ;

  class Sphere {
    float x, y, z;
    float radius;
    float hue;
  }

  private final Sphere[] spheres;

  public Spheres(LX lx) {
    super(lx);
    addParameter(hueParameter);
    addParameter(periodParameter);
    addParameter(hueVariance);
    addModulator(lfo).trigger();
    addModulator(sinLfo).trigger();
    centerX = (model.xMax + model.xMin) / 2;
    centerY = (model.yMax + model.yMin) / 2;
    centerZ = (model.zMax + model.zMin) / 2;

    spheres = new Sphere[2];

    spheres[0] = new Sphere();
    spheres[0].x = model.xMin;
    spheres[0].y = centerY;
    spheres[0].z = centerZ;
    spheres[0].hue = palette.getHuef() - hueVariance.getValuef()/2;
    spheres[0].radius = 50;

    spheres[1] = new Sphere();
    spheres[1].x = model.xMax;
    spheres[1].y = centerY;
    spheres[1].z = centerZ;
    spheres[1].hue =  palette.getHuef() + hueVariance.getValuef()/2;
    spheres[1].radius = 50;
  }

  public void run(double deltaMs) {
    // Access the core master hue via this method call
    float hv = hueParameter.getValuef();
    float lfoValue = lfo.getValuef();
    float sinLfoValue = sinLfo.getValuef();

    spheres[0].hue = palette.getHuef() - hueVariance.getValuef()/2;
    spheres[1].hue = palette.getHuef() + hueVariance.getValuef()/2;

    spheres[0].x = model.xMin + sinLfoValue * model.xMax;
    spheres[1].x = model.xMax - sinLfoValue * model.xMax;

    spheres[0].radius = 100 * hueParameter.getValuef();
    spheres[1].radius = 100 * hueParameter.getValuef();

    Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
      @Override
      public void accept(final LXPoint p) {
        float value = 0;

        color c = lx.hsb(0, 0, 0);
        for (Sphere s : spheres) {
          float d = sqrt(pow(p.x - s.x, 2) + pow(p.y - s.y, 2) + pow(p.z - s.z, 2));
          float r = (s.radius); // * (sinLfoValue + 0.5));
          value = max(0, 1 - max(0, d - r) / 10);

          c = PImage.blendColor(c, lx.hsb(s.hue, 100, min(1, value) * 100), ADD);
        }

        colors[p.index] = c;
      }
    });
  }
}

public class Rings extends LXPattern {
  float dx, dy, dz;
  float angleParam, spacingParam;
  float dzParam, centerParam;

  CompoundParameter pDepth = new CompoundParameter("DEPTH", 0.6);
  CompoundParameter pBright = new CompoundParameter("BRT", 0.75);
  CompoundParameter pSaturation = new CompoundParameter("SAT", 0.5);

  CompoundParameter pSpeed1 = new CompoundParameter("SPD1", 0.2);
  CompoundParameter pSpeed2 = new CompoundParameter("SPD2", 0.4);
  CompoundParameter pScale = new CompoundParameter("SCALE", 0.15);

  public Rings(LX lx) {
    super(lx);
    addParameter(pDepth);
    addParameter(pBright);
    addParameter(pSaturation);

    addParameter(pSpeed1);
    addParameter(pSpeed2);
    addParameter(pScale);
  }

  public void run(double deltaMs) {

    final float xyspeed = pSpeed1.getValuef() * 0.01f;
    final float zspeed = pSpeed1.getValuef() * 0.08f;
    final float scale = pScale.getValuef() * 20.0f;
    final float br = pBright.getValuef() * 3.0f;
    final float gamma = 3.0f;
    final float depth = 1.0f - pDepth.getValuef();
    final float saturation = pSaturation.getValuef() * 100.0f;

    final float angleSpeed = pSpeed1.getValuef() * 0.002f;
    angleParam = (float)((angleParam + angleSpeed * deltaMs) % (2*(float)Math.PI));
    final float angle = (float)Math.sin(angleParam);

    spacingParam += (float)deltaMs * pSpeed2.getValuef() * 0.001f;
    dzParam += (float)deltaMs * 0.000014f;
    centerParam += (float)deltaMs * pSpeed2.getValuef() * 0.001f;

    final float spacing = noise(spacingParam) * 50.0f;

    dx += (float)Math.cos(angle) * xyspeed;
    dy += (float)Math.sin(angle) * xyspeed;
    dz += (float)(Math.pow(noise(dzParam), 1.8f) - 0.5f) * zspeed;

    final float centerx = map(noise(centerParam, 100.0f), 0.0f, 1.0f, -0.1f, 1.1f);
    final float centery = map(noise(centerParam, 200.0f), 0.0f, 1.0f, -0.1f, 1.1f);
    final float centerz = map(noise(centerParam, 300.0f), 0.0f, 1.0f, -0.1f, 1.1f);

    final float coordMin = (float)Math.min(model.xMin, (float)Math.min(model.yMin, model.zMin));
    final float coordMax = (float)Math.max(model.xMax, (float)Math.max(model.yMax, model.zMax));

    noiseDetail(4);

    Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
      @Override
      public void accept(final LXPoint p) {
        // Scale while preserving aspect ratio
        float x = map(p.x, coordMin, coordMax, 0.0f, 1.0f);
        float y = map(p.y, coordMin, coordMax, 0.0f, 1.0f);
        float z = map(p.z, coordMin, coordMax, 0.0f, 1.0f);

        float dist =
          (float) Math.sqrt(Math.pow((x - centerx), 2) + Math.pow((y - centery), 2) + Math.pow((z - centerz), 2));
        float pulse = (float) (Math.sin(dz + dist * spacing) - 0.3f) * 0.6f;

        float n = map(noise(
          dx + (x - centerx) * scale + centerx + pulse,
          dy + (y - centery) * scale + centery,
          dz + (z - centerz) * scale + centerz
        )
          - depth, 0.0f, 1.0f, 0.0f, 2.0f);

        float brightness = 100.0f * constrain((float) Math.pow(br * n, gamma), 0.0f, 1.0f);
        if (brightness == 0) {
          colors[p.index] = LXColor.BLACK;
          return;
        }

        float m = map(noise(
          dx + (x - centerx) * scale + centerx,
          dy + (y - centery) * scale + centery,
          dz + (z - centerz) * scale + centerz
          ),
          0.0f, 1.0f, 0.0f, 300.0f
        );

        colors[p.index] = lx.hsb(
          palette.getHuef() + m,
          saturation,
          brightness
        );
      }
    });

    noiseDetail(1);
  }
};

// requires it's own vector classes, but should prob refactor to use standard vector class
// public class Raindrops extends SLPattern {

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


public class ViolinWave extends LXPattern {

  private LXAudioInput audioInput = lx.engine.audio.getInput();
  private GraphicMeter eq = new GraphicMeter(audioInput);

  CompoundParameter level = new CompoundParameter("LVL", 0.45);
  CompoundParameter range = new CompoundParameter("RNG", 0.5);
  CompoundParameter edge = new CompoundParameter("EDG", 0.5);
  CompoundParameter release = new CompoundParameter("RLS", 0.5);
  CompoundParameter speed = new CompoundParameter("SPD", 0.5);
  CompoundParameter amp = new CompoundParameter("AMP", 0.25, 0, 3);
  CompoundParameter period = new CompoundParameter("WAVE", 0.5);
  CompoundParameter pSize = new CompoundParameter("PSIZE", 0.5);
  CompoundParameter pSpeed = new CompoundParameter("PSPD", 0.5);
  CompoundParameter pDensity = new CompoundParameter("PDENS", 0.25);

  LinearEnvelope dbValue = new LinearEnvelope(0, 0, 10);

  public ViolinWave(LX lx) {
    super(lx);
    addParameter(level);
    addParameter(edge);
    addParameter(range);
    addParameter(release);
    addParameter(speed);
    addParameter(amp);
    addParameter(period);
    addParameter(pSize);
    addParameter(pSpeed);
    addParameter(pDensity);
    addModulator(dbValue);
    addModulator(eq).start();
  }

  final List<Particle> particles = new ArrayList<Particle>();

  class Particle {

    LinearEnvelope x = new LinearEnvelope(0, 0, 0);
    LinearEnvelope y = new LinearEnvelope(0, 0, 0);

    Particle() {
      addModulator(x);
      addModulator(y);
    }

    Particle trigger(boolean direction) {
      float xInit = random(model.xMin, model.xMax);
      float time = 3000 - 2500*pSpeed.getValuef();
      x.setRange(xInit, xInit + random(-40, 40), time).trigger();
      y.setRange(model.cy + 10, direction ? model.yMax + 50 : model.yMin - 50, time).trigger();
      return this;
    }

    boolean isActive() {
      return x.isRunning() || y.isRunning();
    }

    public void run(final double deltaMs) {
      if (!isActive()) {
        return;
      }

      final float pFalloff = (30 - 27*pSize.getValuef());

      Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
        @Override
        public void accept(final LXPoint p) {
          float b = 100 - pFalloff * (abs(p.x - x.getValuef()) + abs(p.y - y.getValuef()));
          if (b > 0) {
            blendColor(p.index, lx.hsb(
              palette.getHuef(), 20, b
            ), LXColor.Blend.ADD);
          }
        }
      });
    }
  }

  float[] centers = new float[30];
  double accum = 0;
  boolean rising = true;

  void fireParticle(boolean direction) {
    boolean gotOne = false;
    for (Particle p : particles) {
      if (!p.isActive()) {
       p.trigger(direction);
       return;
      }
    }
    particles.add(new Particle().trigger(direction));
  }

  final double LOG_10 = Math.log(10);

  public void run(double deltaMs) {
    accum += deltaMs / (1000. - 900.*speed.getValuef());
    for (int i = 0; i < centers.length; ++i) {
      centers[i] = model.cy + 30*amp.getValuef()*sin((float) (accum + (i-centers.length/2.)/(1. + 9.*period.getValuef())));
    }
    float zeroDBReference = pow(10, (50 - 190*level.getValuef())/20.);
    float dB = (float) (20*Math.log((eq.getSquaref()) / zeroDBReference) / LOG_10);
    if (dB > dbValue.getValuef()) {
      rising = true;
      dbValue.setRangeFromHereTo(dB, 10).trigger();
    } else {
      if (rising) {
        for (int j = 0; j < pDensity.getValuef()*3; ++j) {
          fireParticle(true);
          fireParticle(false);
        }
      }
      rising = false;
      dbValue.setRangeFromHereTo(max(dB, -96), 50 + 1000*release.getValuef()).trigger();
    }
    float edg = 1 + edge.getValuef() * 40;
    float rng = (78 - 64 * range.getValuef()) / (model.yMax - model.cy);
    float val = max(2, dbValue.getValuef());

    for (LXPoint p : model.points) {
      int ci = (int) lerp(0, centers.length-1, (p.x - model.xMin) / (model.xMax - model.xMin));
      float rFactor = 1.0 -  0.9 * abs(p.x - model.cx) / (model.xMax - model.cx);
      colors[p.index] = lx.hsb(
        palette.getHuef() + abs(p.x - model.cx),
        min(100, 20 + 8*abs(p.y - centers[ci])),
        constrain(edg*(val*rFactor - rng * abs(p.y-centers[ci])), 0, 100)
      );
    }

    for (Particle p : particles) {
      p.run(deltaMs);
    }
  }
}

public class CrossSections extends LXPattern {

  final SinLFO x = new SinLFO(model.xMin, model.xMax, 5000);
  final SinLFO y = new SinLFO(model.yMin, model.yMax, 6000);
  final SinLFO z = new SinLFO(model.zMin, model.zMax, 7000);

  final CompoundParameter xw = new CompoundParameter("XWID", 0.3);
  final CompoundParameter yw = new CompoundParameter("YWID", 0.3);
  final CompoundParameter zw = new CompoundParameter("ZWID", 0.3);
  final CompoundParameter xr = new CompoundParameter("XRAT", 0.7);
  final CompoundParameter yr = new CompoundParameter("YRAT", 0.6);
  final CompoundParameter zr = new CompoundParameter("ZRAT", 0.5);
  final CompoundParameter xl = new CompoundParameter("XLEV", 1);
  final CompoundParameter yl = new CompoundParameter("YLEV", 1);
  final CompoundParameter zl = new CompoundParameter("ZLEV", 0.5);

  public CrossSections(LX lx) {
    super(lx);
    addModulator(x).trigger();
    addModulator(y).trigger();
    addModulator(z).trigger();
    addParams();
  }

  protected void addParams() {
    addParameter(xr);
    addParameter(yr);
    addParameter(zr);
    addParameter(xw);
    addParameter(xl);
    addParameter(yl);
    addParameter(zl);
    addParameter(yw);
    addParameter(zw);
  }

  void onParameterChanged(LXParameter p) {
    if (p == xr) {
      x.setPeriod(10000 - 8800*p.getValuef());
    } else if (p == yr) {
      y.setPeriod(10000 - 9000*p.getValuef());
    } else if (p == zr) {
      z.setPeriod(10000 - 9000*p.getValuef());
    }
  }

  float xv, yv, zv;

  protected void updateXYZVals() {
    xv = x.getValuef();
    yv = y.getValuef();
    zv = z.getValuef();
  }

  public void run(double deltaMs) {
    updateXYZVals();

    float xlv = 100*xl.getValuef();
    float ylv = 100*yl.getValuef();
    float zlv = 100*zl.getValuef();

    float xwv = 100. / (10 + 40*xw.getValuef());
    float ywv = 100. / (10 + 40*yw.getValuef());
    float zwv = 100. / (10 + 40*zw.getValuef());

    for (LXPoint p : model.points) {
      color c = 0;
      c = PImage.blendColor(c, lx.hsb(
      palette.getHuef() + p.x/10 + p.y/3,
      constrain(140 - 1.1*abs(p.x - model.xMax/2.), 0, 100),
      max(0, xlv - xwv*abs(p.x - xv))
        ), ADD);
      c = PImage.blendColor(c, lx.hsb(
      palette.getHuef() + 80 + p.y/10,
      constrain(140 - 2.2*abs(p.y - model.yMax/2.), 0, 100),
      max(0, ylv - ywv*abs(p.y - yv))
        ), ADD);
      c = PImage.blendColor(c, lx.hsb(
      palette.getHuef() + 160 + p.z / 10 + p.y/2,
      constrain(140 - 2.2*abs(p.z - model.zMax/2.), 0, 100),
      max(0, zlv - zwv*abs(p.z - zv))
        ), ADD);
      colors[p.index] = c;
    }
  }
}

public class TelevisionStatic extends LXPattern {
  CompoundParameter brightParameter = new CompoundParameter("BRIGHT", 1.0);
  CompoundParameter saturationParameter = new CompoundParameter("SAT", 1.0);
  CompoundParameter hueParameter = new CompoundParameter("HUE", 1.0);
  SinLFO direction = new SinLFO(0, 10, 3000);

  public TelevisionStatic(LX lx) {
    super(lx);
    addModulator(direction).trigger();
    addParameter(brightParameter);
    addParameter(saturationParameter);
    addParameter(hueParameter);
  }

 void run(double deltaMs) {
    final boolean d = direction.getValuef() > 5.0;
     Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
       @Override
       public void accept(final LXPoint p) {
         colors[p.index] = lx.hsb(palette.getHuef() + random(hueParameter.getValuef() * 360), random(saturationParameter.getValuef() * 100), random(brightParameter.getValuef() * 100));
       }
     });
  }
}

// public class Noise extends SLPattern {

//   public final CompoundParameter scale = new CompoundParameter("Scale", 10, 5, 40);
//   public final CompoundParameter xSpeed = new CompoundParameter("XSpd", 0, -6, 6);
//   public final CompoundParameter ySpeed = new CompoundParameter("YSpd", 0, -6, 6);
//   public final CompoundParameter zSpeed = new CompoundParameter("ZSpd", 1, -6, 6);
//   public final CompoundParameter floor = new CompoundParameter("Floor", 0, -2, 2);
//   public final CompoundParameter range = new CompoundParameter("Range", 1, .2, 4);
//   public final CompoundParameter xOffset = new CompoundParameter("XOffs", 0, -1, 1);
//   public final CompoundParameter yOffset = new CompoundParameter("YOffs", 0, -1, 1);
//   public final CompoundParameter zOffset = new CompoundParameter("ZOffs", 0, -1, 1);

//   public Noise(LX lx) {
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

public class Test extends LXPattern {

  final CompoundParameter thing = new CompoundParameter("Thing", 0, model.yRange);
  final SinLFO lfo = new SinLFO("Stuff", 0, 1, 2000);

  public Test(LX lx) {
    super(lx);
    addParameter(thing);
    startModulator(lfo);
  }

  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = palette.getColor(max(0, 100 - 10*abs(p.y - thing.getValuef())));
    }
  }
}

public class Palette extends LXPattern {
  public Palette(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = palette.getColor(p);
    }
  }
}

public class Blank extends LXPattern {
  public Blank(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    setColors(LXColor.BLACK);
  }
}

/* Non-Patterns */
public static abstract class SLPattern extends LXPattern {
  public final SLModel model;

  public SLPattern(LX lx) {
    super(lx);
    this.model = (SLModel)lx.model;
  }

  protected <T extends LXParameter> T addParam(T param) {
    addParameter(param);
    return param;
  }

  protected BooleanParameter booleanParam(String name) {
    return addParam(new BooleanParameter(name));
  }

  protected BooleanParameter booleanParam(String name, boolean value) {
    return addParam(new BooleanParameter(name, value));
  }

  protected CompoundParameter compoundParam(String name, double value, double min, double max) {
    return addParam(new CompoundParameter(name, value, min, max));
  }
}