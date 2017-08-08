package com.symmetrylabs.patterns;

import java.util.List;
import java.util.ArrayList;

import processing.core.PVector;
import processing.event.KeyEvent;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.audio.*;
import heronarts.lx.midi.*;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.transform.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;

public class SoundParticles extends LXPattern {
    private final int LIMINAL_KEY = 46;
    private final int MAX_VELOCITY = 100;
    private final float TWO_PI = 2 * (float)Math.PI;

    private boolean debug = false;
    private boolean doUpdate= false;
    //public  VerletPhysics physics;
    public  LXProjection spinProjection;
    public  LXProjection scaleProjection;
    //private LeapMotion leap;
    public  GraphicMeter eq = null;
    public  CompoundParameter spark = new CompoundParameter("Spark", 0);
    public  CompoundParameter magnitude = new CompoundParameter("Mag", 0.1, 1);
    public  CompoundParameter scale = new CompoundParameter("scale", 1, .8, 1.2);
    public  CompoundParameter spin  = new CompoundParameter("Spin", .5 , 0, 1);
    public  CompoundParameter sizeV = new CompoundParameter("Size", .33, 0, 1);
    public  CompoundParameter speed = new CompoundParameter("Speed", 16, 0, 500);
    public  CompoundParameter colorWheel = new CompoundParameter("Color", 0, 0, 360);
    public  CompoundParameter wobble = new CompoundParameter("wobble", 1, 0, 10);
    public  CompoundParameter radius = new CompoundParameter("radius", 700, 0, 1500);
    private List<Particle> particles = new ArrayList<Particle>();
    public  List<SinLFO> xPos = new ArrayList<SinLFO>();
    public  List<SinLFO> yPos = new ArrayList<SinLFO>();
    public  List<SinLFO> zPos = new ArrayList<SinLFO>();
    public  List<SinLFO> wobbleX = new ArrayList<SinLFO>();
    public  List<SinLFO> wobbleY = new ArrayList<SinLFO>();
    public  List<SinLFO> wobbleZ = new ArrayList<SinLFO>();
    public  PVector startVelocity = new PVector();
    private PVector modelCenter = new PVector();
    public  SawLFO angle = new SawLFO(0, TWO_PI, 1000);
    private float[] randomFloat = new float[model.points.length];
    private float[] freqBuckets;
    // private float lastParticleBirth = millis();
    // private float lastTime = millis();
    // private float lastTransmitEQ = millis();
    private int prints = 0;
    private float sparkX = 0f;
    private float sparkY = 0f;
    private float sparkZ = 0f;
    private float midiToHz(int key )  {
        return (float) (440 * (float)Math.pow(2, (key - 69) / 12));
    }
    private float midiToAngle(int key )   {
        return (2 * (float)Math.PI / 24) * key;
    }
    private float randctr (float a) { return MathUtils.random(a) - a * .5f; }

    private final long startMillis = System.currentTimeMillis();
    private long millis() {
        return System.currentTimeMillis() - startMillis;
    }

    List<MidiNoteStamp> lfoNotes = new ArrayList<MidiNoteStamp>();
    MidiNote[] particleNotes = new MidiNote[128];

        class MidiNoteStamp {
            MidiNote note;
            float timestamp;
                MidiNoteStamp(MidiNote _note) {
                    note =_note;
                    timestamp = 0.001f * millis();
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
                    life = 1;
                    intensity = 1;
                    falloff = 1;
                    hue = 220f;
                    i = particles.size();
                    float rand = MathUtils.randomGaussian();
                    float rand2 = MathUtils.randomGaussian();
                    SinLFO x = new SinLFO(-rand*20, rand2*20,1500+ 500*rand2);
                    addModulator(x).trigger();
                    xPos.add(x);
                    SinLFO y = new SinLFO(-rand2*20, rand*20, 1500 + 500*rand2);
                    addModulator(y).trigger();
                    yPos.add(y);

                 // yPos.add(new SinLFO(0,model.yMax/10, 10000));
                    //zPos.add(new SinLFO(-model.zMax/10,model.zMax/10, 1000));

                    wobbleX.add(new SinLFO(6000, 1000, 2000));


                 //  if (MathUtils.random(0,1)<.5){
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
                    life = 1;
                    intensity = 1;
                    falloff = 1;
                    hue = 220f;
                }
                public  boolean isActive() {
                     if (Math.abs(this.position.dist(modelCenter)) >= radius.getValuef()) {
                        if (millis() % 100 < 5 ) {
                        System.out.println("particle distance to center:  " + Math.abs(this.position.dist(modelCenter)));
                        System.out.println("particle distance:  " +  distance);
                        };
                        //  System.out.println("position" + this.position + "modelCenter:  " +   modelCenter);
                        //  System.out.println("particle inactive");
                            return false;
                            }
                        else
                            //System.out.println("position" + this.position + "modelCenter:  " +   modelCenter);
                            //System.out.println("particle active");
                            return true;
                        }

                public void respawn() {
                    //this.position.set(modelCenter.mult(MathUtils.random(.5,1.2)));
                    this.position.set(model.cx, model.cy, model.cz);
                 // this.velocity.set(0,0,0);
                    this.hue=120 + MathUtils.randomGaussian()*30;
                }

                public int calcPoint(LXPoint p ) {
                    return lx.hsb(0,0,0);
                }

                public void run(double deltaMs) {
                    if (!isActive()) {
                        respawn();
                    }

                    float spinNow = spin.getValuef();
                    float sparkf = spark.getValuef();
                    float clock = 0.001f * millis();

                    if (spinNow != 0){
                            if (spinNow > 0) {angle.setRange(0.0, TWO_PI, 1000 - spinNow);}
                            else if (spinNow < 0) {angle.setRange(angle.getValuef(), angle.getValuef() - TWO_PI, 1000 - spinNow);}

                     spinProjection
                     .center()
                     .rotate((spinNow - .5f) / 100 , 0,0,1)
                     .translate(model.cx, model.cy, model.cz);
                    //  .scale(scale.getValuef(),scale.getValuef(),scale.getValuef());
                     }

                    float move = ((float)deltaMs/ 1000)*speed.getValuef();
                    PVector distance = PVector.mult(velocity, move);
                    position.add(distance);
                    //modDist.set(PVector.random3D().setMag(10));
                     modDist.set(xPos.get(i).getValuef()/2, yPos.get(i).getValuef()/2);

                    //modDist.set((float)Math.sin((float)deltaMs/1000)*2,(float)Math.sin((float)deltaMs/1000 + PI/4)*2);
                    position.add(modDist);

                    // for (MidiNoteStamp noteStamp : lfoNotes) {
                    //         MidiNote note = noteStamp.note;
                    //        int key = noteStamp.note.getPitch();
                    //        float lfoAge = clock - noteStamp.timestamp;
                    //        float hz = midiToHz(key);
                    //        float lfoAngle = midiToAngle(key);

                    //        float wobbleAmp = (float) Math.pow(note.getVelocity() / MAX_VELOCITY, 2.0);

                    //        wobbleAmp/= (1 + lfoAge);

                    //       // position.add(wobbleAmp*wobble.getValuef()*(float)Math.cos(clock*lfoAngle), wobbleAmp*wobble.getValuef()*(float)Math.sin(clock*lfoAngle), 0);
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
                            float b = 100 - ((float)Math.pow(p.x-(position.x + sparkle),2) + (float)Math.pow(p.y - (position.y + sparkle), 2) + (float)Math.pow(p.z - (position.z+ randomX*sparkle), 2))/((10+6*avgBass)*size);

                            if (b > 0) {
                                LXColor.blend(p.index, lx.hsb(this.hue+hueShift, MathUtils.map(1-avgTreble, 0,1, 0, 100), b), LXColor.Blend.ADD);
                            }
                         i++;
                    }
                 position.sub(modDist);
                }
            }
    public  SoundParticles(LX lx) {
        super(lx);
        for (int i = 0 ; i < model.points.length; i++) {
            randomFloat[i]=MathUtils.randomGaussian()*10;
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

        // System.out.println("modelCenter = " + modelCenter);
        // System.out.println("model.cx:  " + model.cx + "model.cy:  " + model.cy + "model.cz:  " + model.cz);

        }

    public boolean noteOn(MidiNote note)  {

            if (note.getPitch() < LIMINAL_KEY ){
                    lfoNotes.add(new MidiNoteStamp(note));
                    return false;
                }

            float angle = MathUtils.map(note.getPitch(), 30, 50,  0, TWO_PI);
            float velocity = MathUtils.map(note.getVelocity(), 0, 127, 0, 1);                                           ;
            particles.add(new Particle(
                modelCenter.add(new PVector(MathUtils.random(-model.xMax / 4, model.xMax / 4), MathUtils.random(-model.yMax / 4, model.yMax / 4), 0)),
                new PVector((float)Math.cos(angle) * velocity, (float)Math.sin(angle) * velocity, 0)
            ));


            return false;
    }

    public synchronized void keyEvent(KeyEvent keyEvent){
    if (!(keyEvent.getAction() == KeyEvent.PRESS)) {return;}
        char key = keyEvent.getKey();
        switch (key) {
                case 'P':
                    particles.add(new Particle(
                        new PVector(MathUtils.random(.5f * model.cx, 3 * model.cx / 2), MathUtils.random(.5f * model.cy, 3 * model.cy / 2), model.cz),
                        new PVector(MathUtils.random(-1, 1), MathUtils.random(-1, 1), MathUtils.random(-1, 1))
                    ));

                    System.out.println("number of active particles:  "  +  particles.size());
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
        System.out.println(name + num);
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
                x.setRangeFromHereTo(p.getValuef()*100 + MathUtils.randomGaussian()*10);
            }
            for (SinLFO y : yPos){
                y.setRangeFromHereTo(p.getValuef()*100 + MathUtils.randomGaussian()*10);
            }
            return;
        }

        if (p == colorWheel) {
            float val = p.getValuef();
            // OscMessage message = new OscMessage("/midi/cc");
            // message.add(0);
            // message.add(2);
            // message.add((int)MathUtils.map(val, 0, 1, 0, 127));
            // oscEngine.sendOscMessage(message);
        }
    }

    PVector randomVector() { return new PVector(MathUtils.random(-model.xMax/4, model.xMax/4), MathUtils.random(-model.yMax/4, model.yMax/4), 0);}



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
     // particles.add(new Particle(PVector.random3D().setMag(model.xMax/2).add(modelCenter), PVector.random3D().setMag(.1f)));
        particles.add(new Particle(modelCenter.add(randomVector().setMag(50.f)), PVector.random3D()));

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
    //      System.out.println("particle respawned");
    //     //iter.remove();
    //    // System.out.println("particle removed: ");
    //     } else {
    //     p.run(deltaMs);
    //     }
    //   }
    }
}
