import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.awt.Color; 
import java.util.Arrays; 
import java.util.Collections; 
import java.util.List; 
import java.util.stream.Collectors; 
import java.util.stream.Stream; 
import java.util.function.Function; 
import java.util.function.IntFunction; 
import java.net.InetAddress; 
import java.util.Map; 
import java.net.SocketException; 
import heronarts.lx.modulator.*; 
import heronarts.p3lx.ui.studio.device.*; 

import com.google.gson.annotations.*; 
import com.google.gson.*; 
import com.google.gson.internal.*; 
import com.google.gson.internal.bind.*; 
import com.google.gson.internal.bind.util.*; 
import com.google.gson.reflect.*; 
import com.google.gson.stream.*; 
import heronarts.lx.*; 
import heronarts.lx.audio.*; 
import heronarts.lx.blend.*; 
import heronarts.lx.color.*; 
import heronarts.lx.effect.*; 
import heronarts.lx.midi.*; 
import heronarts.lx.midi.remote.*; 
import heronarts.lx.model.*; 
import heronarts.lx.modulator.*; 
import heronarts.lx.osc.*; 
import heronarts.lx.output.*; 
import heronarts.lx.parameter.*; 
import heronarts.lx.pattern.*; 
import heronarts.lx.transform.*; 
import heronarts.lx.transition.*; 
import heronarts.p3lx.*; 
import heronarts.p3lx.font.*; 
import heronarts.p3lx.pattern.*; 
import heronarts.p3lx.ui.*; 
import heronarts.p3lx.ui.component.*; 
import heronarts.p3lx.ui.control.*; 
import heronarts.p3lx.ui.studio.*; 
import heronarts.p3lx.ui.studio.device.*; 
import heronarts.p3lx.ui.studio.global.*; 
import heronarts.p3lx.ui.studio.midi.*; 
import heronarts.p3lx.ui.studio.mixer.*; 
import heronarts.p3lx.ui.studio.modulation.*; 
import heronarts.p3lx.ui.studio.project.*; 
import heronarts.p3lx.video.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class LXStudioCubes extends PApplet {

/**
 * EnvelopLX
 *
 * An interactive, sound-reactive lighting control system for the
 * Envelop spatial audio platform.
 *
 *  https://github.com/EnvelopSound/EnvelopLX
 *  http://www.studioCubes.us/
 *
 * Copyright 2017- Mark C. Slee
 */

enum Environment {
    MIDWAY,
    SATELLITE
}

// Change this line if you want a different configuration!
Environment environment = Environment.MIDWAY;  
LXStudio lx;
StudioCubes studioCubes;
EnvelopModel venue;

public void setup() {
    long setupStart = System.nanoTime();
    // LX.logInitTiming();
    

    venue = getModel();
    lx = new LXStudio(this, venue) {
        @Override
        protected void initialize(LXStudio lx, LXStudio.UI ui) {
            studioCubes = new StudioCubes(lx);
            lx.engine.registerComponent("studioCubes", studioCubes);
            lx.engine.addLoopTask(studioCubes);
                            
            // Output drivers
            try {
                lx.engine.output.gammaCorrection.setValue(1);
                lx.engine.output.enabled.setValue(false);
                lx.addOutput(getOutput(lx));
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
                
            // OSC drivers
            try {
                lx.engine.osc.receiver(3344).addListener(new EnvelopOscControlListener(lx));
                lx.engine.osc.receiver(3355).addListener(new EnvelopOscSourceListener());
                lx.engine.osc.receiver(3366).addListener(new EnvelopOscMeterListener());
            } catch (SocketException sx) {
                throw new RuntimeException(sx);
            }
                
            lx.registerPatterns(new Class[]{
                heronarts.p3lx.pattern.SolidColorPattern.class,
                IteratorTestPattern.class
            });
            lx.registerEffects(new Class[]{
                FlashEffect.class,
                BlurEffect.class,
                DesaturationEffect.class
            });
        
            ui.theme.setPrimaryColor(0xff008ba0);
            ui.theme.setSecondaryColor(0xff00a08b);
            ui.theme.setAttentionColor(0xffa00044);
            ui.theme.setFocusColor(0xff0094aa);
            ui.theme.setControlBorderColor(0xff292929);
        }
        
        @Override
        protected void onUIReady(LXStudio lx, LXStudio.UI ui) {
            ui.leftPane.audio.setVisible(true);
            ui.main.addComponent(getUIVenue());
            ui.main.addComponent(new UISoundObjects());
            ui.main.setPhi(PI/32).setMinRadius(2*FEET).setMaxRadius(48*FEET);
            new UIEnvelopSource(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 2);
            new UIEnvelopDecode(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 3);
        }
    };
    long setupFinish = System.nanoTime();
    println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms"); 
}

public void draw() {
    background(lx.ui.theme.getDarkBackgroundColor());
}

static class StudioCubes extends LXRunnable {
    
    public final Source source = new Source();
    public final Decode decode = new Decode();
    
    public StudioCubes(LX lx) {
        super(lx);
        addSubcomponent(source);
        addSubcomponent(decode);
        source.start();
        decode.start();
        start();
    }
    
    @Override
    public void run(double deltaMs) {
        source.loop(deltaMs);
        decode.loop(deltaMs);
    }
    
    private final static String KEY_SOURCE = "source";
    private final static String KEY_DECODE = "decode";
    
    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.add(KEY_SOURCE, LXSerializable.Utils.toObject(lx, this.source));
        obj.add(KEY_DECODE, LXSerializable.Utils.toObject(lx, this.decode));
    }
    
    @Override
    public void load(LX lx, JsonObject obj) {
        if (obj.has(KEY_SOURCE)) {
            this.source.load(lx, obj.getAsJsonObject(KEY_SOURCE));
        }
        if (obj.has(KEY_DECODE)) {
            this.decode.load(lx, obj.getAsJsonObject(KEY_DECODE));
        }
        super.load(lx, obj);
    }
    
    abstract class Meter extends LXRunnable {

        private final double[] targets;
        
        public final BoundedParameter gain = (BoundedParameter)
            new BoundedParameter("Gain", 0, -24, 24).setUnits(LXParameter.Units.DECIBELS);
        
        public final BoundedParameter range = (BoundedParameter)
            new BoundedParameter("Range", 24, 6, 96).setUnits(LXParameter.Units.DECIBELS);
            
        public final BoundedParameter attack = (BoundedParameter)
            new BoundedParameter("Attack", 25, 0, 50).setUnits(LXParameter.Units.MILLISECONDS);
            
        public final BoundedParameter release = (BoundedParameter)
            new BoundedParameter("Release", 50, 0, 500).setUnits(LXParameter.Units.MILLISECONDS);
        
        protected Meter(int numChannels) {
            targets = new double[numChannels];
            addParameter(gain);
            addParameter(range);
            addParameter(attack);
            addParameter(release);
        }
        
        public void run(double deltaMs) {
            BoundedParameter[] channels = getChannels();
            for (int i = 0; i < channels.length; ++i) {
                double target = this.targets[i];
                double value = channels[i].getValue();
                double gain = (target >= value) ? Math.exp(-deltaMs / attack.getValue()) : Math.exp(-deltaMs / release.getValue());
                channels[i].setValue(target + gain * (value - target));
            }
        }
        
        public void setLevels(OscMessage message) {
            double gainValue = this.gain.getValue();
            double rangeValue = this.range.getValue();
            for (int i = 0; i < this.targets.length; ++i) {
                targets[i] = constrain((float) (1 + (message.getFloat() + gainValue) / rangeValue), 0, 1);
            }
        }
        
        protected abstract BoundedParameter[] getChannels();
    }
    
    class Source extends Meter {
        public static final int NUM_CHANNELS = 16;
        
        class Channel extends BoundedParameter {
            
            public final int index;
            public boolean active;
            public final PVector xyz = new PVector();
            
            float tx;
            float ty;
            float tz;
            
            Channel(int i) {
                super("Source-" + (i+1));
                this.index = i+1;
                this.active = false;
            }
        }
        
        public final Channel[] channels = new Channel[NUM_CHANNELS];
        
        Source() {
            super(NUM_CHANNELS);
            for (int i = 0; i < channels.length; ++i) {
                channels[i] = new Channel(i);
                addParameter(channels[i]);
            }
        }
        
        public BoundedParameter[] getChannels() {
            return this.channels;
        }
    }
    
    class Decode extends Meter {
        
        public static final int NUM_CHANNELS = 8;
        public final BoundedParameter[] channels = new BoundedParameter[NUM_CHANNELS];
        
        Decode() {
            super(NUM_CHANNELS);
            for (int i = 0; i < channels.length; ++i) {
                channels[i] = new BoundedParameter("Source-" + (i+1));
                addParameter(channels[i]);
            }
        }
        
        public BoundedParameter[] getChannels() {
            return this.channels;
        }
    }
}


public static class Strobe extends LXEffect {
    
    public enum Waveshape {
        TRI,
        SIN,
        SQUARE,
        UP,
        DOWN
    };
    
    public final EnumParameter<Waveshape> mode = new EnumParameter<Waveshape>("Shape", Waveshape.TRI);
    
    public final CompoundParameter frequency = (CompoundParameter)
        new CompoundParameter("Freq", 1, .05f, 10).setUnits(LXParameter.Units.HERTZ);  
    
    private final SawLFO basis = new SawLFO(1, 0, new FunctionalParameter() {
        public double getValue() {
            return 1000 / frequency.getValue();
    }});
                
    public Strobe(LX lx) {
        super(lx);
        addParameter(mode);
        addParameter(frequency);
        startModulator(basis);
    }
    
    @Override
    protected void onEnable() {
        basis.setBasis(0).start();
    }
    
    private LXWaveshape getWaveshape() {
        switch (this.mode.getEnum()) {
        case SIN: return LXWaveshape.SIN;
        case TRI: return LXWaveshape.TRI;
        case UP: return LXWaveshape.UP;
        case DOWN: return LXWaveshape.DOWN;
        case SQUARE: return LXWaveshape.SQUARE;
        }
        return LXWaveshape.SIN;
    }
    
    private final float[] hsb = new float[3];
    
    @Override
    public void run(double deltaMs, double amount) {
        float amt = this.enabledDamped.getValuef();
        if (amt > 0) {
            float strobef = basis.getValuef();
            strobef = (float) getWaveshape().compute(strobef);
            strobef = lerp(1, strobef, amt);
            if (strobef < 1) {
                if (strobef == 0) {
                    for (int i = 0; i < colors.length; ++i) {
                        colors[i] = LXColor.BLACK;
                    }
                } else {
                    for (int i = 0; i < colors.length; ++i) {
                        LXColor.RGBtoHSB(colors[i], hsb);
                        hsb[2] *= strobef;
                        colors[i] = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                    }
                }
            }
        }
    }
}

public class LSD extends LXEffect {
    
    public final BoundedParameter scale = new BoundedParameter("Scale", 10, 5, 40);
    public final BoundedParameter speed = new BoundedParameter("Speed", 4, 1f, 6);
    public final BoundedParameter range = new BoundedParameter("Range", 1, .7f, 2);
    
    public LSD(LX lx) {
        super(lx);
        addParameter(scale);
        addParameter(speed);
        addParameter(range);
        this.enabledDampingAttack.setValue(500);
        this.enabledDampingRelease.setValue(500);
    }
    
    final float[] hsb = new float[3];

    private float accum = 0;
    private int equalCount = 0;
    private float sign = 1;
    
    @Override
    public void run(double deltaMs, double amount) {
        float newAccum = (float) (accum + sign * deltaMs * speed.getValuef() / 4000.f);
        if (newAccum == accum) {
            if (++equalCount >= 5) {
                equalCount = 0;
                sign = -sign;
                newAccum = accum + sign*.01f;
            }
        }
        accum = newAccum;
        float sf = scale.getValuef() / 1000.f;
        float rf = range.getValuef();
        for (LXPoint p :  model.points) {
            LXColor.RGBtoHSB(colors[p.index], hsb);
            float h = rf * noise(sf*p.x, sf*p.y, sf*p.z + accum);
            int c2 = LX.hsb(h * 360, 100, hsb[2]*100);
            if (amount < 1) {
                colors[p.index] = LXColor.lerp(colors[p.index], c2, amount);
            } else {
                colors[p.index] = c2;
            }
        }
    }
}








final static float INCHES = 1;
final static float FEET = 12*INCHES;
    /**
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\  
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 *
 * Contains the model definitions for the cube structures.
 */



/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */
public static class Model extends LXModel {

    public final List<Tower> towers;
    public final List<Cube> cubes;
    public final List<Face> faces;
    public final List<Strip> strips;
    public final Map<String, Cube> cubeTable;
    private final Cube[] _cubes;

    public Model(List<Tower> towers, Cube[] cubeArr) {
        super(new Fixture(cubeArr));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        _cubes = cubeArr;

        // Make unmodifiable accessors to the model data
        List<Tower> towerList = new ArrayList<Tower>();
        List<Cube> cubeList = new ArrayList<Cube>();
        List<Face> faceList = new ArrayList<Face>();
        List<Strip> stripList = new ArrayList<Strip>();
        Map<String, Cube> _cubeTable = new HashMap<String, Cube>();
        
        for (Tower tower : towers) {
            towerList.add(tower);
            for (Cube cube : tower.cubes) {
                if (cube != null) {
                    _cubeTable.put(cube.id, cube);
                    cubeList.add(cube);
                    for (Face face : cube.faces) {
                        faceList.add(face);
                        for (Strip strip : face.strips) {
                            stripList.add(strip);
                        }
                    }
                }
            }
        }

        this.towers    = Collections.unmodifiableList(towerList);
        this.cubes     = Collections.unmodifiableList(cubeList);
        this.faces     = Collections.unmodifiableList(faceList);
        this.strips    = Collections.unmodifiableList(stripList);
        this.cubeTable = Collections.unmodifiableMap (_cubeTable);
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(Cube[] cubeArr) {
            for (Cube cube : cubeArr) { 
                if (cube != null) { 
                    for (LXPoint point : cube.points) { 
                        this.points.add(point); 
                    } 
                } 
            } 
        }
    }

    /**
     * TODO(mcslee): figure out better solution
     * 
     * @param index
     * @return
     */
    public Cube getCubeByRawIndex(int index) {
        return _cubes[index];
    }
    
    public Cube getCubeById(String id) {
        return this.cubeTable.get(id);
    }
}

/**
 * Model of a set of cubes stacked in a tower
 */
public static class Tower extends LXModel {
    
    /**
     * Tower id
     */
    public final String id;
    
    /**
     * Immutable list of cubes
     */
    public final List<Cube> cubes;

    /**
     * Immutable list of faces
     */
    public final List<Face> faces;

    /**
         * Immutable list of strips
         */
    public final List<Strip> strips;

    /**
     * Constructs a tower model from these cubes
     * 
     * @param cubes Array of cubes
     */
    public Tower(String id, List<Cube> cubes) {
        super(cubes.toArray(new Cube[] {}));
        this.id   = id;

        List<Cube>  cubeList  = new ArrayList<Cube>();
        List<Face>  faceList  = new ArrayList<Face>();
        List<Strip> stripList = new ArrayList<Strip>();

        for (Cube cube : cubes) {
            cubeList.add(cube);
            for (Face face : cube.faces) {
                faceList.add(face);
                for (Strip strip : face.strips) {
                    stripList.add(strip);
                }
            }
        }
        this.cubes = Collections.unmodifiableList(cubeList);
        this.faces = Collections.unmodifiableList(faceList);
        this.strips = Collections.unmodifiableList(stripList);
    }
}

/**
 * Model of a single cube, which has an orientation and position on the
 * car. The position is specified in x,y,z coordinates with rotation. The
 * x axis is left->right, y is bottom->top, and z is front->back.
 * 
 * A cube's x,y,z position is specified as the left, bottom, front corner.
 * 
 * Dimensions are all specified in real-world inches.
 */
public static class Cube extends LXModel {

    public enum Type {

        //            Edge     |  LEDs   |  LEDs
        //            Length   |  Per    |  Per
        //            Inches   |  Meter  |  Edge
        SMALL         (12,        72,       15),
        MEDIUM        (18,        60,       23),
        LARGE         (24,        30,       15),
        LARGE_DOUBLE  (24,        60,       30);
        

        public final float EDGE_WIDTH;
        public final float EDGE_HEIGHT;

        public final int POINTS_PER_STRIP;
        public final int POINTS_PER_CUBE;
        public final int POINTS_PER_FACE;

        public final int LEDS_PER_METER;

        public final Face.Metrics FACE_METRICS;

        private Type(float edgeLength, int ledsPerMeter, int ledsPerStrip) {
            this.EDGE_WIDTH = this.EDGE_HEIGHT = edgeLength;

            this.POINTS_PER_STRIP = ledsPerStrip;
            this.POINTS_PER_CUBE = STRIPS_PER_CUBE*POINTS_PER_STRIP;
            this.POINTS_PER_FACE = Face.STRIPS_PER_FACE*POINTS_PER_STRIP;

            this.LEDS_PER_METER = ledsPerMeter;

            this.FACE_METRICS = new Face.Metrics(
                new Strip.Metrics(this.EDGE_WIDTH, POINTS_PER_STRIP, ledsPerMeter), 
                new Strip.Metrics(this.EDGE_HEIGHT, POINTS_PER_STRIP, ledsPerMeter)
            );
        }

    };

    public static final Type CUBE_TYPE_WITH_MOST_PIXELS = Type.LARGE_DOUBLE;

    public final static int FACES_PER_CUBE = 4; 

    public final static int STRIPS_PER_CUBE = FACES_PER_CUBE*Face.STRIPS_PER_FACE;

    public final static float CHANNEL_WIDTH = 1.5f;

    public final Type type;

    public final String id;

    /**
     * Immutable list of all cube faces
     */
    public final List<Face> faces;

    /**
     * Immutable list of all strips
     */
    public final List<Strip> strips;

    /**
     * Front left corner x coordinate 
     */
    public final float x;

    /**
     * Front left corner y coordinate 
     */
    public final float y;

    /**
     * Front left corner z coordinate 
     */
    public final float z;

    /**
     * Rotation about the x-axis 
     */
    public final float rx;

    /**
     * Rotation about the y-axis 
     */
    public final float ry;

    /**
     * Rotation about the z-axis 
     */
    public final float rz;

    public Cube(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t, Type type) {
        super(new Fixture(x, y, z, rx, ry, rz, t, type));
        Fixture fixture = (Fixture) this.fixtures.get(0);
        this.type     = type;
        this.id       = id;

        while (rx < 0) rx += 360;
        while (ry < 0) ry += 360;
        while (rz < 0) rz += 360;
        rx = rx % 360;
        ry = ry % 360;
        rz = rz % 360;

        this.x = x; 
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;

        this.faces = Collections.unmodifiableList(fixture.faces);
        this.strips = Collections.unmodifiableList(fixture.strips);
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Face> faces = new ArrayList<Face>();
        private final List<Strip> strips = new ArrayList<Strip>();

        private Fixture(float x, float y, float z, float rx, float ry, float rz, LXTransform t, Cube.Type type) {
            // LXTransform t = new LXTransform();
            t.push();
            t.translate(x, y, z);
            t.translate(type.EDGE_WIDTH/2, type.EDGE_HEIGHT/2, type.EDGE_WIDTH/2);
            t.rotateX(rx * PI / 180.f);
            t.rotateY(ry * PI / 180.f);
            t.rotateZ(rz * PI / 180.f);
            t.translate(-type.EDGE_WIDTH/2, -type.EDGE_HEIGHT/2, -type.EDGE_WIDTH/2);

            for (int i = 0; i < FACES_PER_CUBE; i++) {
                Face face = new Face(type.FACE_METRICS, (ry + 90*i) % 360, t);
                this.faces.add(face);
                for (Strip s : face.strips) {
                    this.strips.add(s);
                }
                for (LXPoint p : face.points) {
                    this.points.add(p);
                }
                t.translate(type.EDGE_WIDTH, 0, 0);
                t.rotateY(HALF_PI);
            }
            t.pop();
        }
    }
}

/**
 * A face is a component of a cube. It is comprised of four strips forming
 * the lights on this side of a cube. A whole cube is formed by four faces.
 */
public static class Face extends LXModel {

    public final static int STRIPS_PER_FACE = 3;

    public static class Metrics {
        final Strip.Metrics horizontal;
        final Strip.Metrics vertical;

        public Metrics(Strip.Metrics horizontal, Strip.Metrics vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
    }

    /**
     * Immutable list of strips
     */
    public final List<Strip> strips;

    /**
     * Rotation of the face about the y-axis
     */
    public final float ry;

    Face(Metrics metrics, float ry, LXTransform transform) {
        super(new Fixture(metrics, ry, transform));
        Fixture fixture = (Fixture) this.fixtures.get(0);
        this.ry = ry;
        this.strips = Collections.unmodifiableList(fixture.strips);
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Strip> strips = new ArrayList<Strip>();

        private Fixture(Metrics metrics, float ry, LXTransform transform) {
            transform.push();
            transform.translate(0, metrics.vertical.length, 0);
            for (int i = 0; i < STRIPS_PER_FACE; i++) {
                boolean isHorizontal = (i % 2 == 0);
                Strip.Metrics stripMetrics = isHorizontal ? metrics.horizontal : metrics.vertical;
                Strip strip = new Strip(stripMetrics, ry, transform, isHorizontal);
                this.strips.add(strip);
                transform.translate(isHorizontal ? metrics.horizontal.length : metrics.vertical.length, 0, 0);
                transform.rotateZ(HALF_PI);
                for (LXPoint p : strip.points) {
                    this.points.add(p);
                }
            }
            transform.pop();
        }
    }
}

/**
 * A strip is a linear run of points along a single edge of one cube.
 */
public static class Strip extends LXModel {

    public static final float INCHES_PER_METER = 39.3701f;

    public static class Metrics {

        public final float length;
        public final int numPoints;
        public final int ledsPerMeter;

        public final float POINT_SPACING;

        public Metrics(float length, int numPoints, int ledsPerMeter) {
            this.length = length;
            this.numPoints = numPoints;
            this.ledsPerMeter = ledsPerMeter;
            this.POINT_SPACING = INCHES_PER_METER / ledsPerMeter;
        }

        public Metrics(int numPoints, float spacing) {
            this.length = numPoints * spacing;
            this.numPoints = numPoints;
            this.ledsPerMeter = (int)floor((INCHES_PER_METER / this.length) * numPoints);
            this.POINT_SPACING = spacing;
        }
    }

    public final Metrics metrics;

    /**
     * Whether this is a horizontal strip
     */
    public final boolean isHorizontal;

    /**
     * Rotation about the y axis
     */
    public final float ry;

    public Object obj1 = null, obj2 = null;

    Strip(Metrics metrics, float ry, List<LXPoint> points, boolean isHorizontal) {
        super(points);
        this.isHorizontal = isHorizontal;
        this.metrics = metrics;   
        this.ry = ry;
    }

    Strip(Metrics metrics, float ry, LXTransform transform, boolean isHorizontal) {
        super(new Fixture(metrics, ry, transform));
        this.metrics = metrics;
        this.isHorizontal = isHorizontal;
        this.ry = ry;
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(Metrics metrics, float ry, LXTransform transform) {
            float offset = (metrics.length - (metrics.numPoints - 1) * metrics.POINT_SPACING) / 2.f;
            transform.push();
            transform.translate(offset, -Cube.CHANNEL_WIDTH/2.f, 0);
            for (int i = 0; i < metrics.numPoints; i++) {
                LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
                this.points.add(point);
                transform.translate(metrics.POINT_SPACING, 0, 0);
            }
            transform.pop();
        }
    }
}


public EnvelopModel getModel() {
    switch (environment) {
    case SATELLITE: return new Satellite();
    case MIDWAY: return new Midway();
    }
    return null;
}

static abstract class EnvelopModel extends LXModel {
        
    static abstract class Config {
        
        static class Rail {
            public final PVector position;
            public final int numPoints;
            public final float pointSpacing;
            
            Rail(PVector position, int numPoints, float pointSpacing) {
                this.position = position;
                this.numPoints = numPoints;
                this.pointSpacing = pointSpacing;
            }
        }
        
        public abstract PVector[] getColumns();
        public abstract float[] getArcs();
        public abstract Rail[] getRails();
    }
    
    public final List<Column> columns;
    public final List<Arc> arcs;
    public final List<Rail> rails;
    
    protected EnvelopModel(Config config) {
        super(new Fixture(config));
        Fixture f = (Fixture) fixtures.get(0);
        columns = Collections.unmodifiableList(Arrays.asList(f.columns));
        final Arc[] arcs = new Arc[columns.size() * config.getArcs().length];
        final Rail[] rails = new Rail[columns.size() * config.getRails().length];
        int a = 0;
        int r = 0;
        for (Column column : columns) {
            for (Arc arc : column.arcs) {
                arcs[a++] = arc;
            }
            for (Rail rail : column.rails) {
                rails[r++] = rail;
            }
        }
        this.arcs = Collections.unmodifiableList(Arrays.asList(arcs));
        this.rails = Collections.unmodifiableList(Arrays.asList(rails));
    }
    
    private static class Fixture extends LXAbstractFixture {
        
        final Column[] columns;
        
        Fixture(Config config) {
            columns = new Column[config.getColumns().length];
            LXTransform transform = new LXTransform();
            int ci = 0;
            for (PVector pv : config.getColumns()) {
                transform.push();
                transform.translate(pv.x, 0, pv.y);
                float theta = atan2(pv.y, pv.x) - HALF_PI;
                transform.rotateY(theta);
                addPoints(columns[ci] = new Column(config, ci, transform, theta));
                transform.pop();
                ++ci;
            }
        }
    }
}

static class Midway extends EnvelopModel {
    
    final static float WIDTH = 20*FEET + 10.25f*INCHES;
    final static float DEPTH = 41*FEET + 6*INCHES;
    
    final static float INNER_OFFSET_X = WIDTH/2.f - 1*FEET - 8.75f*INCHES;
    final static float OUTER_OFFSET_X = WIDTH/2.f - 5*FEET - 1.75f*INCHES;
    final static float INNER_OFFSET_Z = -DEPTH/2.f + 15*FEET + 10.75f*INCHES;
    final static float OUTER_OFFSET_Z = -DEPTH/2.f + 7*FEET + 8*INCHES;
    
    final static float SUB_OFFSET_X = 36*INCHES;
    final static float SUB_OFFSET_Z = 20*INCHES;
    
    final static EnvelopModel.Config CONFIG = new EnvelopModel.Config() {
        public PVector[] getColumns() {
            return COLUMN_POSITIONS;
        }
        
        public float[] getArcs() {
            return ARC_POSITIONS;
        }
        
        public EnvelopModel.Config.Rail[] getRails() {
            return RAILS;
        }
    };
    
    final static int NUM_POINTS = 109;
    final static float POINT_SPACING = 1.31233596f*INCHES;
    
    final static EnvelopModel.Config.Rail[] RAILS = {
        new EnvelopModel.Config.Rail(new PVector(-1, 0, 0), NUM_POINTS, POINT_SPACING),
        new EnvelopModel.Config.Rail(new PVector(1, 0, 0), NUM_POINTS, POINT_SPACING)
    };
    
    final static float[] ARC_POSITIONS = { 1/3.f, 2/3.f };
    
    final static PVector[] COLUMN_POSITIONS = {
        new PVector(-OUTER_OFFSET_X, -OUTER_OFFSET_Z, 101),
        new PVector(-INNER_OFFSET_X, -INNER_OFFSET_Z, 102),
        new PVector(-INNER_OFFSET_X,  INNER_OFFSET_Z, 103),
        new PVector(-OUTER_OFFSET_X,  OUTER_OFFSET_Z, 104),
        new PVector( OUTER_OFFSET_X,  OUTER_OFFSET_Z, 105),
        new PVector( INNER_OFFSET_X,  INNER_OFFSET_Z, 106),
        new PVector( INNER_OFFSET_X, -INNER_OFFSET_Z, 107),
        new PVector( OUTER_OFFSET_X, -OUTER_OFFSET_Z, 108)
    };
        
    final static PVector[] SUB_POSITIONS = {
        COLUMN_POSITIONS[0].copy().add(-SUB_OFFSET_X, -SUB_OFFSET_Z),
        COLUMN_POSITIONS[3].copy().add(-SUB_OFFSET_X, SUB_OFFSET_Z),
        COLUMN_POSITIONS[4].copy().add(SUB_OFFSET_X, SUB_OFFSET_Z),
        COLUMN_POSITIONS[7].copy().add(SUB_OFFSET_X, -SUB_OFFSET_Z),
    };
    
    Midway() {
        super(CONFIG);
    }
}

static class Satellite extends EnvelopModel {
    
    final static float EDGE_LENGTH = 12*FEET;
    final static float HALF_EDGE_LENGTH = EDGE_LENGTH / 2;
    final static float INCIRCLE_RADIUS = HALF_EDGE_LENGTH + EDGE_LENGTH / sqrt(2);
    
    final static PVector[] PLATFORM_POSITIONS = {
        new PVector(-HALF_EDGE_LENGTH,  INCIRCLE_RADIUS, 101),
        new PVector(-INCIRCLE_RADIUS,  HALF_EDGE_LENGTH, 102),
        new PVector(-INCIRCLE_RADIUS, -HALF_EDGE_LENGTH, 103),
        new PVector(-HALF_EDGE_LENGTH, -INCIRCLE_RADIUS, 104),
        new PVector( HALF_EDGE_LENGTH, -INCIRCLE_RADIUS, 105),
        new PVector( INCIRCLE_RADIUS, -HALF_EDGE_LENGTH, 106),
        new PVector( INCIRCLE_RADIUS,  HALF_EDGE_LENGTH, 107),
        new PVector( HALF_EDGE_LENGTH,  INCIRCLE_RADIUS, 108)
    };
    
    final static PVector[] COLUMN_POSITIONS;
    static {
        float ratio = (INCIRCLE_RADIUS - Column.RADIUS - 6*INCHES) / INCIRCLE_RADIUS;
        COLUMN_POSITIONS = new PVector[PLATFORM_POSITIONS.length];
        for (int i = 0; i < PLATFORM_POSITIONS.length; ++i) {
            COLUMN_POSITIONS[i] = PLATFORM_POSITIONS[i].copy().mult(ratio);
        }
    };
    
    final static float POINT_SPACING = 1.31233596f*INCHES;
    
    final static EnvelopModel.Config.Rail[] RAILS = {
        new EnvelopModel.Config.Rail(new PVector(-1, 0, 0), 108, POINT_SPACING),
        new EnvelopModel.Config.Rail(new PVector(0, 0, 1), 100, POINT_SPACING),
        new EnvelopModel.Config.Rail(new PVector(1, 0, 0), 108, POINT_SPACING)
    };
    
    final static float[] ARC_POSITIONS = { };
    
    final static EnvelopModel.Config CONFIG = new EnvelopModel.Config() {
        public PVector[] getColumns() {
            return COLUMN_POSITIONS;
        }
        
        public float[] getArcs() {
            return ARC_POSITIONS;
        }
        
        public EnvelopModel.Config.Rail[] getRails() {
            return RAILS;
        }
    };
    
    Satellite() {
        super(CONFIG);
    }
}


static class Column extends LXModel {
    
    final static float SPEAKER_ANGLE = 22.f/180.f*PI;
    
    final static float HEIGHT = Rail.HEIGHT;
    final static float RADIUS = 20*INCHES;
    
    final int index;
    final float theta;
    
    final List<Arc> arcs;
    final List<Rail> rails;
    
    Column(EnvelopModel.Config config, int index, LXTransform transform, float theta) {
        super(new Fixture(config, transform));
        this.index = index;
        this.theta = theta;
        Fixture f = (Fixture) fixtures.get(0);
        arcs = Collections.unmodifiableList(Arrays.asList(f.arcs));
        rails = Collections.unmodifiableList(Arrays.asList(f.rails));
    }
    
    private static class Fixture extends LXAbstractFixture {
        final Arc[] arcs;
        final Rail[] rails;
        
        Fixture(EnvelopModel.Config config, LXTransform transform) {
            
            // Transform begins on the floor at center of column
            transform.push();
            
            // Rails
            this.rails = new Rail[config.getRails().length];
            for (int i = 0; i < config.getRails().length; ++i) {
                EnvelopModel.Config.Rail rail = config.getRails()[i]; 
                transform.translate(RADIUS * rail.position.x, 0, RADIUS * rail.position.z);
                addPoints(rails[i] = new Rail(rail, transform));
                transform.translate(-RADIUS * rail.position.x, 0, -RADIUS * rail.position.z);
            }
            
            // Arcs
            this.arcs = new Arc[config.getArcs().length];
            for (int i = 0; i < config.getArcs().length; ++i) {
                float y = config.getArcs()[i] * HEIGHT;
                transform.translate(0, y, 0);      
                addPoints(arcs[i] = new Arc(transform));
                transform.translate(0, -y, 0);
            }
            
            transform.pop();
        }
    }
}

static class Rail extends LXModel {
    
    final static int LEFT = 0;
    final static int RIGHT = 1;
    
    final static float HEIGHT = 12*FEET;
    
    Rail(EnvelopModel.Config.Rail rail, LXTransform transform) {
        super(new Fixture(rail, transform));
    }
    
    private static class Fixture extends LXAbstractFixture {
        Fixture(EnvelopModel.Config.Rail rail, LXTransform transform) {
            transform.push();
            transform.translate(0, rail.pointSpacing / 2.f, 0);
            for (int i = 0; i < rail.numPoints; ++i) {
                addPoint(new LXPoint(transform));
                transform.translate(0, rail.pointSpacing, 0);
            }
            transform.pop();
        }
    }
}

static class Arc extends LXModel {
    
    final static float RADIUS = Column.RADIUS;
    
    final static int BOTTOM = 0;
    final static int TOP = 1;
    
    final static int NUM_POINTS = 34;
    final static float POINT_ANGLE = PI / NUM_POINTS;
    
    Arc(LXTransform transform) {
        super(new Fixture(transform));
    }
    
    private static class Fixture extends LXAbstractFixture {
        Fixture(LXTransform transform) {
            transform.push();
            transform.rotateY(POINT_ANGLE / 2.f);
            for (int i = 0; i < NUM_POINTS; ++i) {
                transform.translate(-RADIUS, 0, 0);
                addPoint(new LXPoint(transform));
                transform.translate(RADIUS, 0, 0);
                transform.rotateY(POINT_ANGLE);
            }
            transform.pop();
        }
    }
}



class EnvelopOscMeterListener implements LXOscListener {
    public void oscMessage(OscMessage message) {
        if (message.matches("/server/dsp/meter/input")) {
            studioCubes.source.setLevels(message);
        } else if (message.matches("/server/dsp/meter/decoded")) {
            studioCubes.decode.setLevels(message);
        } else {
            println(message);
        }
    }
}

class EnvelopOscSourceListener implements LXOscListener {
        
    public void oscMessage(OscMessage message) {
        String[] parts = message.getAddressPattern().toString().split("/");
        if (parts.length == 4) {
            if (parts[1].equals("source")) {
                try {
                    int index = Integer.parseInt(parts[2]) - 1;
                    if (index >= 0 && index < studioCubes.source.channels.length) {
                        StudioCubes.Source.Channel channel = studioCubes.source.channels[index];
                        if (parts[3].equals("active")) {
                            channel.active = message.getFloat() > 0;
                        } else if (parts[3].equals("xyz")) {
                            float rx = message.getFloat();
                            float ry = message.getFloat();
                            float rz = message.getFloat();
                            channel.xyz.set(rx, ry, rz);
                            channel.tx = venue.cx + rx * venue.xRange/2;
                            channel.ty = venue.cy + rz * venue.yRange/2;
                            channel.tz = venue.cz + ry * venue.zRange/2;
                        }
                    } else {
                        println("Invalid source channel message: " + message);
                    }
                } catch (NumberFormatException nfx) {}
            }
        }
    }
}

static class EnvelopOscControlListener implements LXOscListener {
    
    private final LX lx;
    
    private final Map<InetAddress, EnvelopOscClient> clients =
        new HashMap<InetAddress, EnvelopOscClient>();
    
    EnvelopOscControlListener(LX lx) {
        this.lx = lx;    
    }
    
    public void oscMessage(OscMessage message) {
        println("[" + message.getSource() + "] " + message);
        EnvelopOscClient client = clients.get(message.getSource());
        if (client == null) {
            if (message.matches("/lx/register/studioCubes")) {
                try {
                    clients.put(message.getSource(), new EnvelopOscClient(lx, message.getSource()));
                } catch (SocketException sx) {
                    sx.printStackTrace();
                }
            }
        } else {
            client.oscMessage(message);
        }
    }
}
    
static class EnvelopOscClient implements LXOscListener {
    
    private final static int NUM_KNOBS = 12;
    private final KnobListener[] knobs = new KnobListener[NUM_KNOBS]; 

    private final LX lx; 
    private final LXOscEngine.Transmitter transmitter;

    EnvelopOscClient(LX lx, InetAddress source) throws SocketException {
        this.lx = lx;
        this.transmitter = lx.engine.osc.transmitter(source, 3434);
        
        setupListeners();
        register();
    }

    private class KnobListener implements LXParameterListener {
        
        private final int index;
        private BoundedParameter parameter;
        
        private KnobListener(int index) {
            this.index = index;
        }
        
        public void register(BoundedParameter parameter) {
            if (this.parameter != null) {
                this.parameter.removeListener(this);
            }
            this.parameter = parameter;
            if (this.parameter != null){
                this.parameter.addListener(this);
            }
        }
        
        public void set(double value) {
            if (this.parameter != null) {
                this.parameter.setNormalized(value);
            }
        }
        
        public void onParameterChanged(LXParameter p) {
            sendKnobs(); 
        }
    }
    
    private void setupListeners() {
        // Build knob listener objects
        for (int i = 0; i < NUM_KNOBS; ++i) {
            this.knobs[i] = new KnobListener(i);
        }

        // Register to listen for pattern changes 
        lx.engine.getChannel(0).addListener(new LXChannel.AbstractListener() {
            public void patternDidChange(LXChannel channel, LXPattern pattern) {
                int i = 0;
                for (LXPattern p : channel.getPatterns()) {
                    if (p == pattern) {
                        sendMessage("/lx/pattern/active", i);
                        break;
                    }
                    ++i;
                }
                registerKnobs(pattern);
                sendKnobs();
            }
        });
    }
    
    public void oscMessage(OscMessage message) {
        if (message.matches("/lx/register/studioCubes")) {
            register();
        } else if (message.matches("/lx/pattern/index")) {
            lx.goIndex(message.get().toInt());
        } else if (message.matches("/lx/pattern/parameter")) {
            // TODO(mcslee): sanitize input
            knobs[message.getInt()].set(message.getDouble());
        } else if (message.matches("/lx/tempo/bpm")) {
            lx.tempo.setBpm(message.getDouble());
            println("Set bpm to: " + lx.tempo.bpm());
        } else if (message.matches("/lx/tempo/tap")) {
            lx.tempo.trigger(false);
        }
    }
    
    public void register() {
        sendMessage("/lx/register");
        sendPatterns();
        // registerKnobs(lx.engine.getActivePattern());
        sendKnobs();
    }
    
    public void registerKnobs(LXPattern pattern) {
        int i = 0;
        for (LXParameter parameter : pattern.getParameters()) {
            if (i > NUM_KNOBS) {
                break;
            }
            if (parameter instanceof BoundedParameter) {
                knobs[i++].register((BoundedParameter) parameter);
            }
        }
        while (i < NUM_KNOBS) {
            knobs[i++].register(null);
        }
    }
    
    public void sendKnobs() {
        OscMessage parameter = new OscMessage("/lx/pattern/parameter/values");
        for (KnobListener knob : this.knobs) {
            if (knob.parameter != null) {
                parameter.add(knob.parameter.getNormalizedf());
            } else {
                parameter.add(-1);
            }
        }
        sendPacket(parameter);
    }
    
    private void sendPatterns() {
        //OscMessage message = new OscMessage("/lx/pattern/list");
        //LXPattern activePattern = lx.engine.getPattern();
        //int active = 0, i = 0;
        //for (LXPattern pattern : lx.getPatterns()) {
        //  message.add(pattern.getName());
        //  if (pattern == activePattern) {
        //    active = i;
        //  }
        //  ++i;
        //}
        //sendPacket(message);    
        //sendMessage("/lx/pattern/active", active);
    }
    
    private void sendMessage(String addressPattern) {
        sendPacket(new OscMessage(addressPattern));
    }
    
    private void sendMessage(String addressPattern, int value) {
        sendPacket(new OscMessage(addressPattern).add(value));
    }
    
    private void sendPacket(OscPacket packet) {
        try {
            this.transmitter.send(packet);
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }
}


public LXOutput getOutput(LX lx) throws IOException {
    switch (environment) {
        case MIDWAY: return new MidwayOutput(lx);
        case SATELLITE: return new SatelliteOutput(lx);
    }
    return null;
}
        
class MidwayOutput extends LXDatagramOutput {
    MidwayOutput(LX lx) throws IOException {
        super(lx);
        int columnIp = 101;
        for (Column column : venue.columns) {
            addDatagram(new DDPDatagram(column).setAddress("10.0.0." + (columnIp++)));
        }    
    }
}

class SatelliteOutput extends LXDatagramOutput {
    SatelliteOutput(LX lx) throws IOException {
        super(lx);
        int universe = 0;
        int columnIp = 1;
        for (Column column : venue.columns) {
            for (Rail rail : column.rails) {
                // Top to bottom
                int[] indices = new int[rail.size];
                for (int i = 0; i < indices.length; i++) {
                    indices[indices.length-1-i] = rail.points.get(i).index;
                }
                addDatagram(new ArtNetDatagram(indices, universe++).setAddress("192.168.0." + columnIp));
            }
            ++columnIp;
        }
    }
}



public static class Test extends LXPattern {
    
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

public static class Palette extends LXPattern {
    public Palette(LX lx) {
        super(lx);
    }
    
    public void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = palette.getColor(p);
        }
    }
}

public static class MidiFlash extends LXPattern {
    
    private final LinearEnvelope brt = new LinearEnvelope("Brt", 100, 0, 1000);  
    
    public MidiFlash(LX lx) {
        super(lx);
        addModulator(brt.setValue(0));
    }
    
    @Override
    public void noteOnReceived(MidiNoteOn note) {
        brt.setValue(note.getVelocity() / 127.f * 100).start();
    }
    
    public void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = palette.getColor(p, brt.getValuef());
        }
    }
}

public class EnvelopDecode extends LXPattern {
    
    public final BoundedParameter fade = new BoundedParameter("Fade", 1*FEET, 0.001f, 4*FEET); 
    
    public EnvelopDecode(LX lx) {
        super(lx);
        addParameter(fade);
    }
    
    public void run(double deltaMs) {
        float fv = fade.getValuef();
        float falloff = 100 / fv;
        for (Column column : venue.columns) {
            float level = studioCubes.decode.channels[column.index].getValuef() * (model.yRange / 2.f);
            for (LXPoint p : column.points) {
                float yn = abs(p.y - model.cy);
                float b = falloff * (level - yn);
                colors[p.index] = palette.getColor(p, constrain(b, 0, 100));
            }
        }
    }
}

public class SoundObjects extends LXPattern implements UIPattern {
    
    public final BoundedParameter size = new BoundedParameter("Base", 4*FEET, 0, 24*FEET);
    public final BoundedParameter response = new BoundedParameter("Level", 0, 1*FEET, 24*FEET);
    
    public SoundObjects(LX lx) {
        super(lx);
        for (StudioCubes.Source.Channel object : studioCubes.source.channels) {
            addLayer(new Layer(lx, object));
        }
        addParameter(size);
        addParameter(response);
    }
    
    public void buildControlUI(UI ui, UIPatternControl container) {
        int i = 0;
        for (LXLayer layer : getLayers()) {
            new UIButton((i % 4)*33, (i/4)*22, 28, 18).setLabel(Integer.toString(i+1)).setParameter(((Layer)layer).active).addToContainer(container);
            ++i;
        }
        int knobSpacing = UIKnob.WIDTH + 4;
        new UIKnob(0, 92).setParameter(size).addToContainer(container);
        new UIKnob(knobSpacing, 92).setParameter(response).addToContainer(container);

        container.setContentWidth(3*knobSpacing - 4);
    }
    
    class Layer extends LXLayer {
        
        private final StudioCubes.Source.Channel object;
        private final BooleanParameter active = new BooleanParameter("Active", true); 
        
        Layer(LX lx, StudioCubes.Source.Channel object) {
            super(lx);
            this.object = object;
            addParameter(active);
        }
        
        public void run(double deltaMs) {
            if (!this.active.isOn()) {
                return;
            }
            if (object.active) {
                float falloff = 100 / (size.getValuef() + response.getValuef() * object.getValuef());
                for (LXPoint p : model.points) {
                    float dist = dist(p.x, p.y, p.z, object.tx, object.ty, object.tz);
                    float b = 100 - dist*falloff;
                    if (b > 0) {
                        addColor(p.index, palette.getColor(p,  b));
                    }
                }
            }
        }
    }
    
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
    }
}

public class Bouncing extends LXPattern {
    
    public CompoundParameter gravity = new CompoundParameter("Gravity", -200, -100, -400);
    public CompoundParameter size = new CompoundParameter("Length", 2*FEET, 1*FEET, 4*FEET);
    public CompoundParameter amp = new CompoundParameter("Height", model.yRange, 1*FEET, model.yRange);
    
    public Bouncing(LX lx) {
        super(lx);
        for (Column column : venue.columns) {
            addLayer(new Bouncer(lx, column));
        }
        addParameter(gravity);
        addParameter(size);
        addParameter(amp);
    }
    
    class Bouncer extends LXLayer {
        
        private final Column column;
        private final Accelerator position;
        
        Bouncer(LX lx, Column column) {
            super(lx);
            this.column = column;
            this.position = new Accelerator(column.yMax, 0, gravity);
            startModulator(position);
        }
        
        public void run(double deltaMs) {
            if (position.getValue() < 0) {
                position.setValue(-position.getValue());
                position.setVelocity(sqrt(abs(2 * (amp.getValuef() - random(0, 2*FEET)) * gravity.getValuef()))); 
            }
            float h = palette.getHuef();
            float falloff = 100.f / size.getValuef();
            for (Rail rail : column.rails) {
                for (LXPoint p : rail.points) {
                    float b = 100 - falloff * abs(p.y - position.getValuef());
                    if (b > 0) {
                        addColor(p.index, palette.getColor(p, b));
                    }
                }
            }
        }
    }
        
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
    }
}

public class Movers extends LXPattern {
    
    private CompoundParameter period = new CompoundParameter("Speed", 150000, 200000, 50000); 
    
    public Movers(LX lx) {  
        super(lx);
        addParameter(period);
        for (int i = 0; i < 15; ++i) {
            addLayer(new Mover(lx));
        }
    }
    
    class Mover extends LXLayer {
        final TriangleLFO pos = new TriangleLFO(0, lx.total, period);
        
        Mover(LX lx) {
            super(lx);
            startModulator(pos.randomBasis());
        }
        
        public void run(double deltaMs) {
            for (LXPoint p : model.points) {
                float b = 100 - 3*abs(p.index - pos.getValuef());
                if (b > 0) {
                    addColor(p.index, palette.getColor(p, b));
                }
            }
        }
    }
    
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);
    }
}

public class Noise extends LXPattern {
    
    public final CompoundParameter scale = new CompoundParameter("Scale", 10, 5, 40);
    public final CompoundParameter xSpeed = new CompoundParameter("XSpd", 0, -6, 6);
    public final CompoundParameter ySpeed = new CompoundParameter("YSpd", 0, -6, 6);
    public final CompoundParameter zSpeed = new CompoundParameter("ZSpd", 1, -6, 6);
    public final CompoundParameter floor = new CompoundParameter("Floor", 0, -2, 2);
    public final CompoundParameter range = new CompoundParameter("Range", 1, .2f, 4);
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
        
        public void accum(double deltaMs, float speed) {
            float newAccum = (float) (this.accum + this.sign * deltaMs * speed / 4000.f);
            if (newAccum == this.accum) {
                if (++this.equalCount >= 5) {
                    this.equalCount = 0;
                    this.sign = -sign;
                    newAccum = this.accum + sign*.01f;
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
        
        float sf = scale.getValuef() / 1000.f;
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
public UI3dComponent getUIVenue() {
    switch (environment) {
    case SATELLITE: return new UISatellite();
    case MIDWAY: return new UIMidway();
    }
    return null;
}

abstract class UIVenue extends UI3dComponent {

    final static float LOGO_SIZE = 100*INCHES;
    final PImage LOGO = loadImage("envelop-logo-clear.png");

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        pg.stroke(0xff000000);
        pg.fill(0xff202020);
        drawFloor(ui, pg);
        
        // Logo
        pg.noFill();
        pg.noStroke();
        pg.beginShape();
        pg.texture(LOGO);
        pg.textureMode(NORMAL);
        pg.vertex(-LOGO_SIZE, .1f, -LOGO_SIZE, 0, 1);
        pg.vertex(LOGO_SIZE, .1f, -LOGO_SIZE, 1, 1);
        pg.vertex(LOGO_SIZE, .1f, LOGO_SIZE, 1, 0);
        pg.vertex(-LOGO_SIZE, .1f, LOGO_SIZE, 0, 0);
        pg.endShape(CLOSE);
        
        // Speakers
        pg.fill(0xff000000);
        pg.stroke(0xff202020);
        for (Column column : venue.columns) {
            pg.translate(column.cx, 0, column.cz);
            pg.rotateY(-column.theta);
            pg.translate(0, 9*INCHES, 0);
            pg.rotateX(Column.SPEAKER_ANGLE);
            pg.box(21*INCHES, 16*INCHES, 15*INCHES);
            pg.rotateX(-Column.SPEAKER_ANGLE);
            pg.translate(0, 6*FEET-9*INCHES, 0);
            pg.box(21*INCHES, 16*INCHES, 15*INCHES);
            pg.translate(0, 11*FEET + 3*INCHES - 6*FEET, 0);
            pg.rotateX(-Column.SPEAKER_ANGLE);
            pg.box(21*INCHES, 16*INCHES, 15*INCHES);
            pg.rotateX(Column.SPEAKER_ANGLE);
            pg.rotateY(+column.theta);
            pg.translate(-column.cx, -11*FEET - 3*INCHES, -column.cz);
        }
    }
    
    protected abstract void drawFloor(UI ui, PGraphics pg);
}

class UISatellite extends UIVenue {
    public void drawFloor(UI ui, PGraphics pg) {
        pg.beginShape();
        for (PVector v : Satellite.PLATFORM_POSITIONS) {
            pg.vertex(v.x, 0, v.y);
        }
        pg.endShape(CLOSE);
        pg.beginShape(QUAD_STRIP);
        for (int vi = 0; vi <= Satellite.PLATFORM_POSITIONS.length; ++vi) {
            PVector v = Satellite.PLATFORM_POSITIONS[vi % Satellite.PLATFORM_POSITIONS.length];
            pg.vertex(v.x, 0, v.y);
            pg.vertex(v.x, -8*INCHES, v.y);
        }
        pg.endShape();
    }
}
    
class UIMidway extends UIVenue {
        
    @Override
    public void onDraw(UI ui, PGraphics pg) {
        super.onDraw(ui, pg);
        
        // Desk
        pg.translate(0, 20*INCHES, -Midway.DEPTH/2 + 18*INCHES);
        pg.box(6*FEET, 40*INCHES, 36*INCHES);
        pg.translate(0, -20*INCHES, Midway.DEPTH/2 - 18*INCHES);
        
        // Subwoofers
        for (PVector pv : Midway.SUB_POSITIONS) {
            pg.translate(pv.x, 10*INCHES, pv.y);
            pg.rotateY(-QUARTER_PI);
            pg.box(29*INCHES, 20*INCHES, 29*INCHES);
            pg.rotateY(QUARTER_PI);
            pg.translate(-pv.x, -10*INCHES, -pv.y);
        }
    }
    
    @Override
    protected void drawFloor(UI ui, PGraphics pg) {
        // Floor
        pg.translate(0, -4*INCHES, 0);
        pg.box(Midway.WIDTH, 8*INCHES, Midway.DEPTH);
        pg.translate(0, 4*INCHES, 0);
    }        
}

class UIEnvelopSource extends UICollapsibleSection {
    UIEnvelopSource(UI ui, float x, float y, float w) {
        super(ui, x, y, w, 124);
        setTitle("StudioCubes SOURCE");
        new UIEnvelopMeter(ui, studioCubes.source, 0, 0, getContentWidth(), 60).addToContainer(this);    
        UIAudio.addGainAndRange(this, 64, studioCubes.source.gain, studioCubes.source.range);
        UIAudio.addAttackAndRelease(this, 84, studioCubes.source.attack, studioCubes.source.release);
    }
}

class UIEnvelopDecode extends UICollapsibleSection {
    UIEnvelopDecode(UI ui, float x, float y, float w) {
        super(ui, x, y, w, 124);
        setTitle("STUDIOCUBES DECODE");
        new UIEnvelopMeter(ui, studioCubes.decode, 0, 0, getContentWidth(), 60).addToContainer(this);
        UIAudio.addGainAndRange(this, 64, studioCubes.decode.gain, studioCubes.decode.range);
        UIAudio.addAttackAndRelease(this, 84, studioCubes.decode.attack, studioCubes.decode.release);
    }
}

class UIEnvelopMeter extends UI2dComponent {
        
    private final StudioCubes.Meter meter;
    
    public UIEnvelopMeter(UI ui, StudioCubes.Meter meter, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.meter = meter;
        setBackgroundColor(ui.theme.getDarkBackgroundColor());
        setBorderColor(ui.theme.getControlBorderColor());
    }
    
    public void onDraw(UI ui, PGraphics pg) {
        BoundedParameter[] channels = this.meter.getChannels();
        float bandWidth = ((width-2) - (channels.length-1)) / channels.length;
        
        pg.noStroke();
        pg.fill(ui.theme.getPrimaryColor());
        int x = 1;
        for (int i = 0; i < channels.length; ++i) {
            int nextX = Math.round(1 + (bandWidth+1) * (i+1));
            float h = (this.height-2) * channels[i].getValuef(); 
            pg.rect(x, this.height-1-h, nextX-x-1, h);
            x = nextX;
        }
                
        // TODO(mcslee): do this properly, on a timer
        redraw();
    }
}

class UISoundObjects extends UI3dComponent {
    final PFont objectLabelFont; 

    UISoundObjects() {
        this.objectLabelFont = loadFont("Arial-Black-24.vlw");
    }
    
    public void onDraw(UI ui, PGraphics pg) {
        for (StudioCubes.Source.Channel channel : studioCubes.source.channels) {
            if (channel.active) {
                float tx = channel.tx;
                float ty = channel.ty;
                float tz = channel.tz;
                pg.directionalLight(40, 40, 40, .5f, -.4f, 1);
                pg.ambientLight(40, 40, 40);
                pg.translate(tx, ty, tz);
                pg.noStroke();
                pg.fill(0xff00ddff);
                pg.sphere(6*INCHES);
                pg.noLights();
                pg.scale(1, -1);
                pg.textAlign(CENTER, CENTER);
                pg.textFont(objectLabelFont);
                pg.textSize(4);
                pg.fill(0xff00ddff);
                pg.text(Integer.toString(channel.index), 0, -1*INCHES, -6.1f*INCHES);
                pg.scale(1, -1);
                pg.translate(-tx, -ty, -tz);
            }
        }    
    }
}
    public void settings() {  size(1280, 800, P3D); }
    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "LXStudioCubes" };
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}
