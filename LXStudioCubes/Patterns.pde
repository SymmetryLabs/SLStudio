import heronarts.lx.modulator.*;
import heronarts.p3lx.ui.studio.device.*;

public class SoundParticles extends SLPattern   {
    private final int LIMINAL_KEY = 46; 
    private final int MAX_VELOCITY = 100; 
    private boolean debug = false; 
    private boolean doUpdate= false;   
    //public  VerletPhysics physics; 
    public  LXProjection spinProjection; 
    public  LXProjection scaleProjection; 
    //private LeapMotion leap; 
    public  GraphicMeter eq = null;
    public  BoundedParameter spark = new BoundedParameter("Spark", 0); 
    public  BoundedParameter magnitude = new BoundedParameter("Mag", 0.1, 1);
    public  BoundedParameter scale = new BoundedParameter("scale", 1, .8, 1.2); 
    public  BoundedParameter spin  = new BoundedParameter("Spin", .5 , 0, 1); 
    public  BoundedParameter sizeV = new BoundedParameter("Size", .33, 0, 1); 
    public  BoundedParameter speed = new BoundedParameter("Speed", 16, 0, 500); 
    public  BoundedParameter colorWheel = new BoundedParameter("Color", 0, 0, 360); 
    public  BoundedParameter wobble = new BoundedParameter("wobble", 1, 0, 10); 
    public  BoundedParameter radius = new BoundedParameter("radius", 700, 0, 1500); 
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
    private float[] randomFloat = new float[model.points.size()];
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
              println("particle distance to center:  " +   abs(this.position.dist(modelCenter)));
              println("particle distance:  " +  distance); 
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
      for (int i = 0 ; i < model.points.size(); i++) {
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

    public void run(double deltaMs) {
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
     for (Particle p : particles) {
      p.run(deltaMs); 
     }
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


public static class Test extends SLPattern {
  
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

public static class Palette extends SLPattern {
  public Palette(LX lx) {
    super(lx);
  }
  
  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = palette.getColor(p);
    }
  }
}

public static class CubeEQ extends SLPattern {

  private GraphicMeter eq = null;
  private LXAudioInput audioInput;

  private final BoundedParameter edge = new BoundedParameter("EDGE", 0.5);
  private final BoundedParameter clr = new BoundedParameter("CLR", 0.1, 0, .5);
  private final BoundedParameter blockiness = new BoundedParameter("BLK", 0.5);

  public CubeEQ(LX lx) {
    super(lx);
    audioInput = lx.engine.audio.getInput();
    eq = new GraphicMeter(audioInput);
    // addParameter(eq.gain);
    // addParameter(eq.range);
    // addParameter(eq.attack);
    // addParameter(eq.release);
    // addParameter(eq.slope);
    addParameter(edge);
    addParameter(clr);
    addParameter(blockiness);
    addModulator(eq).start();
  }

  // void onActive() {
  //   if (eq == null) {
  //     audioInput = lx.engine.audio.getInput();
  //     eq = new GraphicMeter(audioInput);
  //     eq.range.setValue(48);
  //     eq.release.setValue(800);
  //     addParameter(eq.gain);
  //     addParameter(eq.range);
  //     addParameter(eq.attack);
  //     addParameter(eq.release);
  //     addParameter(eq.slope);
  //     addParameter(edge);
  //     addParameter(clr);
  //     addParameter(blockiness);
  //     addModulator(eq).start();
  //   }
  // }

  public void run(double deltaMs) {
    float edgeConst = 2 + 30*edge.getValuef();
    float clrConst = 1.1 + clr.getValuef();

    for (LXPoint p : model.points) {
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
  }
}

public static class Swarm extends SLPattern {
  
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
    for (Strip strip : model.strips) {
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
      ++s;
    }
  }
}

public static class SpaceTime extends SLPattern {

  SinLFO pos = new SinLFO(0, 1, 3000);
  SinLFO rate = new SinLFO(1000, 9000, 13000);
  SinLFO falloff = new SinLFO(10, 70, 5000);
  float angle = 0;

  BoundedParameter rateParameter = new BoundedParameter("RATE", 0.5);
  BoundedParameter sizeParameter = new BoundedParameter("SIZE", 0.5);

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
    float sVal1 = model.strips.size() * (0.5 + 0.5*sin(angle));
    float sVal2 = model.strips.size() * (0.5 + 0.5*cos(angle));

    float pVal = pos.getValuef();
    float fVal = falloff.getValuef();

    int s = 0;
    for (Strip strip : model.strips) {
      int i = 0;
      for (LXPoint p : strip.points) {
        colors[p.index] = lx.hsb(
          palette.getHuef() + 360 - p.x*.2 + p.y * .3,
          constrain(.4 * min(abs(s - sVal1), abs(s - sVal2)), 20, 100),
          max(0, 100 - fVal*abs(i - pVal*(strip.metrics.numPoints - 1)))
        );
        ++i;
      }
      ++s;
    }
  }
}

public static class ShiftingPlane extends SLPattern {

  final BoundedParameter hueShift = new BoundedParameter("hShift", 0.5, 0, 1);

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

//   public void buildControlUI(UI ui, UIPatternControl container) {
//     int i = 0;
//     for (LXLayer layer : getLayers()) {
//       new UIButton((i % 4)*33, (i/4)*22, 28, 18).setLabel(Integer.toString(i+1)).setParameter(((Layer)layer).active).addToContainer(container);
//       ++i;
//     }
//     int knobSpacing = UIKnob.WIDTH + 4;
//     new UIKnob(0, 92).setParameter(size).addToContainer(container);
//     new UIKnob(knobSpacing, 92).setParameter(response).addToContainer(container);

//     container.setContentWidth(3*knobSpacing - 4);
//   }
  
//   class Layer extends LXLayer {
    
//     private final StudioCubes.Source.Channel object;
//     private final BooleanParameter active = new BooleanParameter("Active", true); 
    
//     Layer(LX lx, StudioCubes.Source.Channel object) {
//       super(lx);
//       this.object = object;
//       addParameter(active);
//     }
    
//     public void run(double deltaMs) {
//       if (!this.active.isOn()) {
//         return;
//       }
//       if (object.active) {
//         float falloff = 100 / (size.getValuef() + response.getValuef() * object.getValuef());
//         for (LXPoint p : model.points) {
//           float dist = dist(p.x, p.y, p.z, object.tx, object.ty, object.tz);
//           float b = 100 - dist*falloff;
//           if (b > 0) {
//             addColor(p.index, palette.getColor(p,  b));
//           }
//         }
//       }
//     }
//   }
  
//   public void run(double deltaMs) {
//     setColors(LXColor.BLACK);
//   }
// }

// public class Bouncing extends SLPattern {
  
//   public CompoundParameter gravity = new CompoundParameter("Gravity", -200, -100, -400);
//   public CompoundParameter size = new CompoundParameter("Length", 2*FEET, 1*FEET, 4*FEET);
//   public CompoundParameter amp = new CompoundParameter("Height", model.yRange, 1*FEET, model.yRange);
  
//   public Bouncing(LX lx) {
//     super(lx);
//     // for (Column column : model.columns) {
//     //   addLayer(new Bouncer(lx, column));
//     // }
//     addParameter(gravity);
//     addParameter(size);
//     addParameter(amp);
//   }
  
//   class Bouncer extends LXLayer {
    
//     private final Column column;
//     private final Accelerator position;
    
//     Bouncer(LX lx, Column column) {
//       super(lx);
//       this.column = column;
//       this.position = new Accelerator(column.yMax, 0, gravity);
//       startModulator(position);
//     }
    
//     public void run(double deltaMs) {
//       if (position.getValue() < 0) {
//         position.setValue(-position.getValue());
//         position.setVelocity(sqrt(abs(2 * (amp.getValuef() - random(0, 2*FEET)) * gravity.getValuef()))); 
//       }
//       float h = palette.getHuef();
//       float falloff = 100. / size.getValuef();
//       //for (Rail rail : column.rails) {
//         for (LXPoint p : model.points) {
//           float b = 100 - falloff * abs(p.y - position.getValuef());
//           if (b > 0) {
//             addColor(p.index, palette.getColor(p, b));
//           }
//         }
//       //}
//     }
//   }
    
//   public void run(double deltaMs) {
//     setColors(LXColor.BLACK);
//   }
// }

// public class Movers extends SLPattern {
  
//   private CompoundParameter period = new CompoundParameter("Speed", 150000, 200000, 50000); 
  
//   public Movers(LX lx) {  
//     super(lx);
//     addParameter(period);
//     for (int i = 0; i < 15; ++i) {
//       addLayer(new Mover(lx));
//     }
//   }
  
//   class Mover extends LXLayer {
//     final TriangleLFO pos = new TriangleLFO(0, lx.total, period);
    
//     Mover(LX lx) {
//       super(lx);
//       startModulator(pos.randomBasis());
//     }
    
//     public void run(double deltaMs) {
//       for (LXPoint p : model.points) {
//         float b = 100 - 3*abs(p.index - pos.getValuef());
//         if (b > 0) {
//           addColor(p.index, palette.getColor(p, b));
//         }
//       }
//     }
//   }
  
//   public void run(double deltaMs) {
//     setColors(LXColor.BLACK);
//   }
// }

public class Noise extends SLPattern {
  
  public final CompoundParameter scale = new CompoundParameter("Scale", 10, 5, 40);
  public final CompoundParameter xSpeed = new CompoundParameter("XSpd", 0, -6, 6);
  public final CompoundParameter ySpeed = new CompoundParameter("YSpd", 0, -6, 6);
  public final CompoundParameter zSpeed = new CompoundParameter("ZSpd", 1, -6, 6);
  public final CompoundParameter floor = new CompoundParameter("Floor", 0, -2, 2);
  public final CompoundParameter range = new CompoundParameter("Range", 1, .2, 4);
  public final CompoundParameter xOffset = new CompoundParameter("XOffs", 0, -1, 1);
  public final CompoundParameter yOffset = new CompoundParameter("YOffs", 0, -1, 1);
  public final CompoundParameter zOffset = new CompoundParameter("ZOffs", 0, -1, 1);
  
  public Noise(LX lx) {
    super(lx);
    addParameter(scale);
    addParameter(floor);
    addParameter(range);
    addParameter(xSpeed);
    addParameter(ySpeed);
    addParameter(zSpeed);
    addParameter(xOffset);
    addParameter(yOffset);
    addParameter(zOffset);
  }
  
  private class Accum {
    private float accum = 0;
    private int equalCount = 0;
    private float sign = 1;
    
    void accum(double deltaMs, float speed) {
      float newAccum = (float) (this.accum + this.sign * deltaMs * speed / 4000.);
      if (newAccum == this.accum) {
        if (++this.equalCount >= 5) {
          this.equalCount = 0;
          this.sign = -sign;
          newAccum = this.accum + sign*.01;
        }
      }
      this.accum = newAccum;
    }
  };
  
  private final Accum xAccum = new Accum();
  private final Accum yAccum = new Accum();
  private final Accum zAccum = new Accum();
    
  @Override
  public void run(double deltaMs) {
    xAccum.accum(deltaMs, xSpeed.getValuef());
    yAccum.accum(deltaMs, ySpeed.getValuef());
    zAccum.accum(deltaMs, zSpeed.getValuef());
    
    float sf = scale.getValuef() / 1000.;
    float rf = range.getValuef();
    float ff = floor.getValuef();
    float xo = xOffset.getValuef();
    float yo = yOffset.getValuef();
    float zo = zOffset.getValuef();
    for (LXPoint p :  model.points) {
      float b = ff + rf * noise(sf*p.x + xo + xAccum.accum, sf*p.y + yo + yAccum.accum, sf*p.z + zo + zAccum.accum);
      colors[p.index] = palette.getColor(p, constrain(b*100, 0, 100));
    }
  }
}

public static abstract class SLPattern extends LXPattern {
  public final SLModel model;

  public SLPattern(LX lx) {
    super(lx);
    this.model = (SLModel)lx.model;
  }
}