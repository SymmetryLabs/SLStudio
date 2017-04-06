import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import java.net.*; 
import java.lang.reflect.*; 
import java.awt.Color; 
import java.util.concurrent.ConcurrentLinkedQueue; 
import java.util.Arrays; 
import java.util.Collections; 
import java.util.List; 
import java.util.stream.Collectors; 
import java.util.stream.Stream; 
import java.util.function.Function; 
import java.util.function.IntFunction; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.atomic.AtomicBoolean; 
import java.util.regex.Pattern; 
import java.util.regex.Matcher; 
import heronarts.lx.modulator.*; 
import heronarts.p3lx.ui.studio.device.*; 
import java.io.*; 
import java.net.*; 
import java.util.Random; 
import java.util.zip.*; 
import processing.core.*; 
import processing.data.*; 
import java.util.regex.Pattern; 
import java.util.regex.Matcher; 
import java.lang.reflect.Field; 

import org.apache.commons.math3.*; 
import org.apache.commons.math3.genetics.*; 
import org.apache.commons.math3.optimization.*; 
import org.apache.commons.math3.optimization.fitting.*; 
import org.apache.commons.math3.optimization.general.*; 
import org.apache.commons.math3.optimization.direct.*; 
import org.apache.commons.math3.optimization.univariate.*; 
import org.apache.commons.math3.optimization.linear.*; 
import org.apache.commons.math3.analysis.*; 
import org.apache.commons.math3.analysis.differentiation.*; 
import org.apache.commons.math3.analysis.interpolation.*; 
import org.apache.commons.math3.analysis.solvers.*; 
import org.apache.commons.math3.analysis.integration.*; 
import org.apache.commons.math3.analysis.integration.gauss.*; 
import org.apache.commons.math3.analysis.function.*; 
import org.apache.commons.math3.analysis.polynomials.*; 
import org.apache.commons.math3.ode.events.*; 
import org.apache.commons.math3.ode.*; 
import org.apache.commons.math3.ode.sampling.*; 
import org.apache.commons.math3.ode.nonstiff.*; 
import org.apache.commons.math3.fitting.*; 
import org.apache.commons.math3.fitting.leastsquares.*; 
import org.apache.commons.math3.geometry.spherical.twod.*; 
import org.apache.commons.math3.geometry.spherical.oned.*; 
import org.apache.commons.math3.geometry.*; 
import org.apache.commons.math3.geometry.partitioning.*; 
import org.apache.commons.math3.geometry.partitioning.utilities.*; 
import org.apache.commons.math3.geometry.hull.*; 
import org.apache.commons.math3.geometry.enclosing.*; 
import org.apache.commons.math3.geometry.euclidean.twod.*; 
import org.apache.commons.math3.geometry.euclidean.twod.hull.*; 
import org.apache.commons.math3.geometry.euclidean.oned.*; 
import org.apache.commons.math3.geometry.euclidean.threed.*; 
import org.apache.commons.math3.exception.*; 
import org.apache.commons.math3.exception.util.*; 
import org.apache.commons.math3.fraction.*; 
import org.apache.commons.math3.random.*; 
import org.apache.commons.math3.distribution.*; 
import org.apache.commons.math3.distribution.fitting.*; 
import org.apache.commons.math3.optim.*; 
import org.apache.commons.math3.optim.nonlinear.vector.*; 
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.*; 
import org.apache.commons.math3.optim.nonlinear.scalar.*; 
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.*; 
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.*; 
import org.apache.commons.math3.optim.univariate.*; 
import org.apache.commons.math3.optim.linear.*; 
import org.apache.commons.math3.special.*; 
import org.apache.commons.math3.ml.neuralnet.*; 
import org.apache.commons.math3.ml.neuralnet.twod.*; 
import org.apache.commons.math3.ml.neuralnet.twod.util.*; 
import org.apache.commons.math3.ml.neuralnet.sofm.*; 
import org.apache.commons.math3.ml.neuralnet.sofm.util.*; 
import org.apache.commons.math3.ml.neuralnet.oned.*; 
import org.apache.commons.math3.ml.clustering.*; 
import org.apache.commons.math3.ml.clustering.evaluation.*; 
import org.apache.commons.math3.ml.distance.*; 
import org.apache.commons.math3.stat.ranking.*; 
import org.apache.commons.math3.stat.correlation.*; 
import org.apache.commons.math3.stat.*; 
import org.apache.commons.math3.stat.interval.*; 
import org.apache.commons.math3.stat.descriptive.*; 
import org.apache.commons.math3.stat.descriptive.moment.*; 
import org.apache.commons.math3.stat.descriptive.rank.*; 
import org.apache.commons.math3.stat.descriptive.summary.*; 
import org.apache.commons.math3.stat.regression.*; 
import org.apache.commons.math3.stat.clustering.*; 
import org.apache.commons.math3.stat.inference.*; 
import org.apache.commons.math3.filter.*; 
import org.apache.commons.math3.util.*; 
import org.apache.commons.math3.complex.*; 
import org.apache.commons.math3.dfp.*; 
import org.apache.commons.math3.linear.*; 
import org.apache.commons.math3.primes.*; 
import org.apache.commons.math3.transform.*; 
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




//import org.apache.commons.math3.util.FastMath;

public LXStudio lx;
//StudioCubes studioCubes;
public SLModel model;
public Dispatcher dispatcher;
public NetworkMonitor networkMonitor;
public CubeOutputTester cubeOutputTester;
public BroadcastPacketTester broadcastPacketTester;

public void setup() {
    long setupStart = System.nanoTime();
    // LX.logInitTiming();
    

    model = getModel();
    lx = new LXStudio(this, model) {
        @Override
        protected void initialize(LXStudio lx, LXStudio.UI ui) {
            //studioCubes = new StudioCubes(lx);
            //lx.engine.registerComponent("studioCubes", studioCubes);
            //lx.engine.addLoopTask(studioCubes);

            // Output drivers
            (dispatcher = new Dispatcher(lx)).start();
            (networkMonitor = new NetworkMonitor(lx)).start();
            setupGammaCorrection();
            buildOutputs(lx);
            cubeOutputTester = new CubeOutputTester(lx);
            broadcastPacketTester = new BroadcastPacketTester(lx);
            new CubeResetModule(lx);
            //logTime("Built Outputs");

            // try {
            //   lx.engine.output.gammaCorrection.setValue(1);
            //   lx.engine.output.enabled.setValue(false);
            //   lx.addOutput(getOutput(lx));
            // } catch (Exception x) {
            //   throw new RuntimeException(x);
            // }
                
            // OSC drivers
            // try {
            //   lx.engine.osc.receiver(3344).addListener(new EnvelopOscControlListener(lx));
            //   lx.engine.osc.receiver(3355).addListener(new EnvelopOscSourceListener());
            //   lx.engine.osc.receiver(3366).addListener(new EnvelopOscMeterListener());
            // } catch (SocketException sx) {
            //   throw new RuntimeException(sx);
            // }
                
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
            //ui.main.addComponent(getUIVenue());
            //ui.main.addComponent(new UISoundObjects());
            ui.main.setPhi(PI/32).setMinRadius(2*FEET).setMaxRadius(48*FEET);
            new UIOutputs(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 3);
            //new UIEnvelopSource(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 2);
            //new UIEnvelopDecode(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 3);
        }
    };
    long setupFinish = System.nanoTime();
    println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms"); 
}

public void draw() {
    background(lx.ui.theme.getDarkBackgroundColor());
}

// static class StudioCubes extends LXRunnable {
    
//   public final Source source = new Source();
//   public final Decode decode = new Decode();
    
//   public StudioCubes(LX lx) {
//     super(lx);
//     addSubcomponent(source);
//     addSubcomponent(decode);
//     source.start();
//     decode.start();
//     start();
//   }
    
//   @Override
//   public void run(double deltaMs) {
//     source.loop(deltaMs);
//     decode.loop(deltaMs);
//   }
    
//   private final static String KEY_SOURCE = "source";
//   private final static String KEY_DECODE = "decode";
    
//   @Override
//   public void save(LX lx, JsonObject obj) {
//     super.save(lx, obj);
//     obj.add(KEY_SOURCE, LXSerializable.Utils.toObject(lx, this.source));
//     obj.add(KEY_DECODE, LXSerializable.Utils.toObject(lx, this.decode));
//   }
    
//   @Override
//   public void load(LX lx, JsonObject obj) {
//     if (obj.has(KEY_SOURCE)) {
//       this.source.load(lx, obj.getAsJsonObject(KEY_SOURCE));
//     }
//     if (obj.has(KEY_DECODE)) {
//       this.decode.load(lx, obj.getAsJsonObject(KEY_DECODE));
//     }
//     super.load(lx, obj);
//   }
    
//   abstract class Meter extends LXRunnable {

//     private final double[] targets;
        
//     public final BoundedParameter gain = (BoundedParameter)
//       new BoundedParameter("Gain", 0, -24, 24).setUnits(LXParameter.Units.DECIBELS);
        
//     public final BoundedParameter range = (BoundedParameter)
//       new BoundedParameter("Range", 24, 6, 96).setUnits(LXParameter.Units.DECIBELS);
            
//     public final BoundedParameter attack = (BoundedParameter)
//       new BoundedParameter("Attack", 25, 0, 50).setUnits(LXParameter.Units.MILLISECONDS);
            
//     public final BoundedParameter release = (BoundedParameter)
//       new BoundedParameter("Release", 50, 0, 500).setUnits(LXParameter.Units.MILLISECONDS);
        
//     protected Meter(int numChannels) {
//       targets = new double[numChannels];
//       addParameter(gain);
//       addParameter(range);
//       addParameter(attack);
//       addParameter(release);
//     }
        
//     public void run(double deltaMs) {
//       BoundedParameter[] channels = getChannels();
//       for (int i = 0; i < channels.length; ++i) {
//         double target = this.targets[i];
//         double value = channels[i].getValue();
//         double gain = (target >= value) ? Math.exp(-deltaMs / attack.getValue()) : Math.exp(-deltaMs / release.getValue());
//         channels[i].setValue(target + gain * (value - target));
//       }
//     }
        
//     public void setLevels(OscMessage message) {
//       double gainValue = this.gain.getValue();
//       double rangeValue = this.range.getValue();
//       for (int i = 0; i < this.targets.length; ++i) {
//         targets[i] = constrain((float) (1 + (message.getFloat() + gainValue) / rangeValue), 0, 1);
//       }
//     }
        
//     protected abstract BoundedParameter[] getChannels();
//   }
    
//   class Source extends Meter {
//     public static final int NUM_CHANNELS = 16;
        
//     class Channel extends BoundedParameter {
            
//       public final int index;
//       public boolean active;
//       public final PVector xyz = new PVector();
            
//       float tx;
//       float ty;
//       float tz;
            
//       Channel(int i) {
//         super("Source-" + (i+1));
//         this.index = i+1;
//         this.active = false;
//       }
//     }
        
//     public final Channel[] channels = new Channel[NUM_CHANNELS];
        
//     Source() {
//       super(NUM_CHANNELS);
//       for (int i = 0; i < channels.length; ++i) {
//         channels[i] = new Channel(i);
//         addParameter(channels[i]);
//       }
//     }
        
//     public BoundedParameter[] getChannels() {
//       return this.channels;
//     }
//   }
    
//   class Decode extends Meter {
        
//     public static final int NUM_CHANNELS = 8;
//     public final BoundedParameter[] channels = new BoundedParameter[NUM_CHANNELS];
        
//     Decode() {
//       super(NUM_CHANNELS);
//       for (int i = 0; i < channels.length; ++i) {
//         channels[i] = new BoundedParameter("Source-" + (i+1));
//         addParameter(channels[i]);
//       }
//     }
        
//     public BoundedParameter[] getChannels() {
//       return this.channels;
//     }
//   }
// }


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


public class Dispatcher implements LXLoopTask {

    LX lx;

    private final DispatchQueue engineQueue = new DispatchQueue();
    private final DispatchQueue uiQueue = new DispatchQueue();

    Dispatcher(LX lx) {
        this.lx = lx;

        uiQueue.setThreadName(Thread.currentThread().getName());
        setEngineThreaded(true);
    }

    public void setEngineThreaded(boolean threaded) {
        engineQueue.setThreadName(threaded ? "LX Engine Thread" : Thread.currentThread().getName());
    }

    public void start() {
        lx.engine.addLoopTask(this);
        registerMethod("draw", this);
    }

    public void loop(double deltaMs) {
        engineQueue.executeAll();
    }

    public void draw() {
        uiQueue.executeAll();
    }

    public void dispatchEngine(Runnable runnable) {
        engineQueue.queue(runnable);
    }

    public void dispatchUi(Runnable runnable) {
        uiQueue.queue(runnable);
    }

    public DispatchQueue getEngineQueue() {
        return engineQueue;
    }

    public DispatchQueue getUiQueue() {
        return uiQueue;
    }

}

class DispatchQueue {

    private volatile String threadName;
    private final ConcurrentLinkedQueue<Runnable> queuedRunnables = new ConcurrentLinkedQueue<Runnable>();

    DispatchQueue() {
        this(null);
    }

    DispatchQueue(String threadName) {
        this.threadName = threadName;
    }

    public synchronized void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void executeAll() {
        Runnable runnable;
        while ((runnable = queuedRunnables.poll()) != null) {
            runnable.run();
        }
    }

    public void queue(Runnable runnable) {
        boolean shouldRunNow = false;
        synchronized (this) {
            if (threadName != null && Thread.currentThread().getName().equals(threadName)) {
                shouldRunNow = true;
            }
        }
        if (shouldRunNow) {
            runnable.run();
        } else {
            queuedRunnables.add(runnable);
        }
    }
}
public static class ListenableList<E> implements Iterable<E> {

    public final List<E> list = new ArrayList<E>();

    private final List<ListListener<E>> listeners = new ArrayList<ListListener<E>>();

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public E get(int index) {
        return list.get(index);
    }

    public void add(int index, E element) {
        list.add(index, element);
        for (ListListener<E> listener : listeners) {
            listener.itemAdded(index, element);
        }
    }

    public boolean add(E element) {
        list.add(element);
        for (ListListener<E> listener : listeners) {
            listener.itemAdded(list.size() - 1, element);
        }
        return true;
    }

    public E remove(int index) {
        E element = list.remove(index);
        for (ListListener<E> listener : listeners) {
            listener.itemRemoved(index, element);
        }
        return element;
    }

    public boolean remove(Object o) {
        int index = list.indexOf(o);
        if (index != -1) {
            remove(index);
            return true;
        }
        return false;
    }

    public void clear() {
        while (!isEmpty()) {
            remove(0);
        }
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public final ListenableList<E> addListener(ListListener<E> listener) {
        listeners.add(listener);
        return this;
    }

    public final ListenableList<E> addListenerWithInit(ListListener<E> listener) {
        listeners.add(listener);
        int index = 0;
        for (E element : list) {
            listener.itemAdded(index++, element);
        }
        return this;
    }

    public final ListenableList<E> removeListener(ListListener<E> listener) {
        listeners.remove(listener);
        return this;
    }

    public Iterator<E> iterator() {
        return list.iterator();
    }

}

public static interface ListListener<E> {
    public void itemAdded(int index, E element);
    public void itemRemoved(int index, E element);
}

public static abstract class AbstractListListener<E> implements ListListener<E> {
    public void itemAdded(int index, E element) {}
    public void itemRemoved(int index, E element) {}
}

public static class ListenableInt {

    private int value;

    private final List<IntListener> listeners = new ArrayList<IntListener>();

    public ListenableInt() {
        this(0);
    }

    public ListenableInt(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
        for (IntListener listener : listeners) {
            listener.onChange(value);
        }
    }

    public void increment() {
        set(get() + 1);
    }

    public void decrement() {
        set(get() - 1);
    }

    public String toString() {
        return Integer.toString(value);
    }

    public final ListenableInt addListener(IntListener listener) {
        listeners.add(listener);
        return this;
    }

    public final ListenableInt addListenerWithInit(IntListener listener) {
        listeners.add(listener);
        listener.onChange(value);
        return this;
    }

    public final ListenableInt removeListener(IntListener listener) {
        listeners.remove(listener);
        return this;
    }

}

public static interface IntListener {
    public void onChange(int value);
}
static final float globalOffsetX = 0;
static final float globalOffsetY = 0;
static final float globalOffsetZ = 0;

static final float globalRotationX = 0;
static final float globalRotationY = 0;
static final float globalRotationZ = 0;

static final float CUBE_WIDTH = 24;
static final float CUBE_HEIGHT = 24;
static final float CUBE_SPACING = 2;
static final float TOWER_RISER = 14;

static final TowerConfig[] TOWER_CONFIG = {

    new TowerConfig(35*0,           0, 0, 45, new String[] {
        "23", "0", "0", "0"
    }),
    new TowerConfig(35*1, TOWER_RISER, 0, 45, new String[] {
        "0", "0", "0", "0"
    }),
    new TowerConfig(35*2,           0, 0, 45, new String[] {
        "0", "0", "0", "0"
    }),
    new TowerConfig(35*3, TOWER_RISER, 0, 45, new String[] {
        "0", "0", "0", "0"
    }),
    new TowerConfig(35*4,           0, 0, 45, new String[] {
        "0", "0", "0", "0"
    }),
    new TowerConfig(35*5, TOWER_RISER, 0, 45, new String[] {
        "0", "0", "0", "0"
    }),
    new TowerConfig(35*6,           0, 0, 45, new String[] {
        "0", "0", "0", "0"
    }),

};

static class TowerConfig {

    final Cube.Type type;
    final float x;
    final float y;
    final float z;
    final float xRot;
    final float yRot;
    final float zRot;
    final String[] ids;
    final float[] yValues;

    TowerConfig(float x, float y, float z, String[] ids) {
        this(Cube.Type.LARGE, x, y, z, ids);
    }

    TowerConfig(float x, float y, float z, float yRot, String[] ids) {
        this(x, y, z, 0, yRot, 0, ids);
    }

    TowerConfig(Cube.Type type, float x, float y, float z, String[] ids) {
        this(type, x, y, z, 0, 0, 0, ids);
    }

    TowerConfig(Cube.Type type, float x, float y, float z, float yRot, String[] ids) {
        this(type, x, y, z, 0, yRot, 0, ids);
    }

    TowerConfig(float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
        this(Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
    }

    TowerConfig(Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
        this.ids = ids;

        this.yValues = new float[ids.length];
        for (int i = 0; i < ids.length; i++) {
            yValues[i] = y + i * (CUBE_HEIGHT + CUBE_SPACING);
        }
    }

}

Map<String, String> macToPhysid = new HashMap<String, String>();
Map<String, String> physidToMac = new HashMap<String, String>();

public SLModel buildModel() {

    byte[] bytes = loadBytes("physid_to_mac.json");
    if (bytes != null) {
        try {
            JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                macToPhysid.put(entry.getValue().getAsString(), entry.getKey());
                physidToMac.put(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    // Any global transforms
    LXTransform globalTransform = new LXTransform();
    globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
    globalTransform.rotateY(globalRotationY * PI / 180.f);
    globalTransform.rotateX(globalRotationX * PI / 180.f);
    globalTransform.rotateZ(globalRotationZ * PI / 180.f);

    /* Cubes ----------------------------------------------------------*/
    List<Tower> towers = new ArrayList<Tower>();
    List<Cube> allCubes = new ArrayList<Cube>();

    for (TowerConfig config : TOWER_CONFIG) {
        List<Cube> cubes = new ArrayList<Cube>();
        float x = config.x;
        float z = config.z;
        float xRot = config.xRot;
        float yRot = config.yRot;
        float zRot = config.zRot;
        Cube.Type type = config.type;

        for (int i = 0; i < config.ids.length; i++) {
            float y = config.yValues[i];
            Cube cube = new Cube(config.ids[i], x, y, z, xRot, yRot-90, zRot, globalTransform, type);
            cubes.add(cube);
            allCubes.add(cube);
        }
        towers.add(new Tower("", cubes));
    }
    /*-----------------------------------------------------------------*/

    Cube[] allCubesArr = new Cube[allCubes.size()];
    for (int i = 0; i < allCubesArr.length; i++) {
        allCubesArr[i] = allCubes.get(i);
    }

    return new SLModel(towers, allCubesArr);
}

public SLModel getModel() {
    return buildModel();
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

public static class SLModel extends LXModel {
    public final List<Tower> towers;
    public final List<Cube> cubes;
    public final List<Face> faces;
    public final List<Strip> strips;
    public final Map<String, Cube> cubeTable;
    private final Cube[] _cubes;

    public SLModel(List<Tower> towers, Cube[] cubeArr) {
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






static class NetworkManager {

    private static NetworkManager instance;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}

static class NetworkInfo {
    public static List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (nets != null) {
            while (nets.hasMoreElements()) {
                NetworkInterface iface = nets.nextElement();
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    InetAddress broadcast = addr.getBroadcast();
                    if (broadcast != null) {
                        addresses.add(broadcast);
                    }
                }
            }
        }
        return addresses;
    }
    public static List<InetAddress> getInetAddresses() {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (nets != null) {
            while (nets.hasMoreElements()) {
                NetworkInterface iface = nets.nextElement();
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    InetAddress inetAddress = addr.getAddress();
                    if (inetAddress != null) {
                        addresses.add(inetAddress);
                    }
                }
            }
        }
        return addresses;
    }
}

static class ControllerCommand {

    public final InetAddress addr;
    public final byte[] command;
    public final int responseSize;

    private ControllerCommandCallback callback;

    ControllerCommand(InetAddress addr, byte[] command) {
        this(addr, command, -1, null);
    }

    ControllerCommand(final InetAddress addr, final byte[] command,
            final int responseSize, final ControllerCommandCallback callback) {
        this.addr = addr;
        this.command = command;
        this.responseSize = responseSize;
        this.callback = callback;

        NetworkManager.getInstance().getExecutor().submit(new Runnable() {
            public void run() {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket();
                } catch (SocketException e) {
                    return;
                }
                try {
                    try {
                        socket.setSoTimeout(1000);
                    } catch (SocketException e) {
                        return;
                    }
                    try {
                        socket.send(new java.net.DatagramPacket(command, command.length,
                            new InetSocketAddress(addr, 7890)));
                    } catch (IOException e) {
                        return;
                    }

                    if (responseSize < 1 || callback == null) return;

                    do {
                        byte[] response = new byte[responseSize];
                        java.net.DatagramPacket packet = new java.net.DatagramPacket(response, response.length);
                        try {
                            socket.receive(packet);
                            if (callback != null) callback.onResponse(packet);
                        } catch (IOException e) {
                            break;
                        }
                    } while (!addr.isMulticastAddress());
                } finally {
                    socket.close();
                    if (callback != null) callback.onFinish();
                }
            }
        });
    }
}

public static interface ControllerCommandCallback {
    public void onResponse(java.net.DatagramPacket response);
    public void onFinish();
}

public static abstract class AbstractControllerCommandCallback implements ControllerCommandCallback {
    public void onResponse(java.net.DatagramPacket response) {}
    public void onFinish() {}
}

static class PingCommand extends ControllerCommand {
    PingCommand(InetAddress addr, final ControllerCommandCallback callback) {
        super(addr, new byte[] { (byte)136, 1 }, 1, new ControllerCommandCallback() {
            public void onResponse(java.net.DatagramPacket response) {
                if (callback != null && response.getLength() == 1 && response.getData()[0] == 1) {
                    callback.onResponse(response);
                }
            }
            public void onFinish() { if (callback != null) callback.onFinish(); }
        });
    }
}

public static class ResetCommand extends ControllerCommand {
    public ResetCommand(InetAddress addr) {
        super(addr, new byte[] { (byte)136, 2 });
    }
}

public static class VersionCommand extends ControllerCommand {
    public VersionCommand(InetAddress addr, final VersionCommandCallback callback) {
        super(addr, new byte[] { (byte)136, 3 }, 1, new ControllerCommandCallback() {
            public void onResponse(java.net.DatagramPacket response) {
                if (callback != null && response.getLength() == 1) {
                    callback.onResponse(response, response.getData()[0]);
                }
            }
            public void onFinish() { if (callback != null) callback.onFinish(); }
        });
    }
}

public static interface VersionCommandCallback {
    public void onResponse(java.net.DatagramPacket response, int version);
    public void onFinish();
}

public static abstract class AbstractVersionCommandCallback implements VersionCommandCallback {
    public void onResponse(java.net.DatagramPacket response, int version) {}
    public void onFinish() {}
}

public static class MacAddrCommand extends ControllerCommand {
    public MacAddrCommand(InetAddress addr, final MacAddrCommandCallback callback) {
        super(addr, new byte[] { (byte)136, 4 }, 6, new ControllerCommandCallback() {
            public void onResponse(java.net.DatagramPacket response) {
                if (callback != null && response.getLength() == 6) {
                    callback.onResponse(response, response.getData());
                }
            }
            public void onFinish() { if (callback != null) callback.onFinish(); }
        });
    }
}

public static interface MacAddrCommandCallback {
    public void onResponse(java.net.DatagramPacket response, byte[] macAddr);
    public void onFinish();
}

public static abstract class AbstractMacAddrCommandCallback implements MacAddrCommandCallback {
    public void onResponse(java.net.DatagramPacket response, byte[] macAddr) {}
    public void onFinish() {}
}

class NetworkMonitor {

    private final ControllerScan controllerScan = new ControllerScan();

    public final ListenableList<NetworkDevice> networkDevices = controllerScan.networkDevices;

    private final java.util.TimerTask scanTask = new ScanTask();
    private final java.util.Timer timer = new java.util.Timer();

    private boolean oldVersionWarningGiven = false;

    NetworkMonitor(LX lx) {
        networkDevices.addListener(new AbstractListListener<NetworkDevice>() {
            public void itemAdded(int index, final NetworkDevice result) {
                new VersionCommand(result.ipAddress, new VersionCommandCallback() {
                    public void onResponse(java.net.DatagramPacket response, final int version) {
                        dispatcher.dispatchEngine(new Runnable() {
                            public void run() {
                                result.version.set(version);
                            }
                        });
                    }
                    public void onFinish() {
                        dispatcher.dispatchEngine(new Runnable() {
                            public void run() {
                                if (!oldVersionWarningGiven) {
                                    for (NetworkDevice device : networkDevices) {
                                        if (device.version.get() != -1
                                                && (device.version.get() < result.version.get()
                                                        || device.version.get() > result.version.get())) {
                                            System.out.println("WARNING: One or more cubes have outdated firmware!");
                                            oldVersionWarningGiven = true;
                                            return;
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public void start() {
        timer.schedule(scanTask, 0, 500);
    }

    class ScanTask extends java.util.TimerTask {
        public void run() {
            controllerScan.scan();
        }
    }

}

public static class NetworkDevice {

    public final InetAddress ipAddress;
    public final byte[] macAddress;
    public final ListenableInt version = new ListenableInt(-1);

    public int connectionRetries = 0;

    public NetworkDevice(InetAddress ipAddress, byte[] macAddress) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }

}

public class ControllerScan {

    public final ListenableList<NetworkDevice> networkDevices = new ListenableList<NetworkDevice>();

    public void scan() {
        new Runnable() {
            final ListenableList<NetworkDevice> tmpNetworkDevices = new ListenableList<NetworkDevice>();
            int instances = 0;
            public void run() {
                for (InetAddress addr : NetworkInfo.getBroadcastAddresses()) {
                    instances++;
                    new MacAddrCommand(addr, new MacAddrCommandCallback() {
                        public void onResponse(java.net.DatagramPacket response, byte[] macAddr) {
                            final NetworkDevice networkDevice = new NetworkDevice(response.getAddress(), macAddr);
                            dispatcher.dispatchEngine(new Runnable() {
                                public void run() {
                                    tmpNetworkDevices.add(networkDevice);
                                }
                            });
                        }

                        public void onFinish() {
                            dispatcher.dispatchEngine(new Runnable() {
                                public void run() {
                                    if (--instances == 0) {
                                        for (int i = 0; i < networkDevices.size(); i++) {
                                            NetworkDevice device = networkDevices.get(i);
                                            boolean found = false;
                                            boolean removed = false;
                                            for (NetworkDevice tmpDevice : tmpNetworkDevices) {
                                                if (Arrays.equals(device.macAddress, tmpDevice.macAddress)
                                                        && device.ipAddress.equals(tmpDevice.ipAddress)) {
                                                    found = true;
                                                    device.connectionRetries = 0;
                                                    break;
                                                } else if (Arrays.equals(device.macAddress, tmpDevice.macAddress)
                                                        || device.ipAddress.equals(tmpDevice.ipAddress)) {
                                                    removed = true;
                                                    networkDevices.remove(i--);
                                                    break;
                                                }
                                            }
                                            if (!found && !removed) {
                                                if (++device.connectionRetries == 5) {
                                                    networkDevices.remove(i--);
                                                }
                                            }
                                        }
                                        for (NetworkDevice tmpDevice : tmpNetworkDevices) {
                                            boolean found = false;
                                            for (NetworkDevice device : networkDevices) {
                                                if (Arrays.equals(device.macAddress, tmpDevice.macAddress)) {
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (!found) {
                                                networkDevices.add(tmpDevice);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }.run();
    }
}

class CubeResetModule {
    final BooleanParameter enabled = new BooleanParameter("Cube reset enabled");
    CubeResetModule(LX lx) {
        //moduleRegistrar.modules.add(new Module("Reset all cubes", enabled, true));
        enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (enabled.isOn()) new CubeResetter().run();
            }
        });
    }
}

static class CubeResetter {
    public void run() {
        for (InetAddress addr : NetworkInfo.getBroadcastAddresses()) {
            new ResetCommand(addr);
        }
    }
}
// import java.net.InetAddress;
// import java.util.Map;

// class EnvelopOscMeterListener implements LXOscListener {
//   public void oscMessage(OscMessage message) {
//     if (message.matches("/server/dsp/meter/input")) {
//       studioCubes.source.setLevels(message);
//     } else if (message.matches("/server/dsp/meter/decoded")) {
//       studioCubes.decode.setLevels(message);
//     } else {
//       println(message);
//     }
//   }
// }

// class EnvelopOscSourceListener implements LXOscListener {
        
//   public void oscMessage(OscMessage message) {
//     String[] parts = message.getAddressPattern().toString().split("/");
//     if (parts.length == 4) {
//       if (parts[1].equals("source")) {
//         try {
//           int index = Integer.parseInt(parts[2]) - 1;
//           if (index >= 0 && index < studioCubes.source.channels.length) {
//             StudioCubes.Source.Channel channel = studioCubes.source.channels[index];
//             if (parts[3].equals("active")) {
//               channel.active = message.getFloat() > 0;
//             } else if (parts[3].equals("xyz")) {
//               float rx = message.getFloat();
//               float ry = message.getFloat();
//               float rz = message.getFloat();
//               channel.xyz.set(rx, ry, rz);
//               channel.tx = venue.cx + rx * venue.xRange/2;
//               channel.ty = venue.cy + rz * venue.yRange/2;
//               channel.tz = venue.cz + ry * venue.zRange/2;
//             }
//           } else {
//             println("Invalid source channel message: " + message);
//           }
//         } catch (NumberFormatException nfx) {}
//       }
//     }
//   }
// }

// static class EnvelopOscControlListener implements LXOscListener {
    
//   private final LX lx;
    
//   private final Map<InetAddress, EnvelopOscClient> clients =
//     new HashMap<InetAddress, EnvelopOscClient>();
    
//   EnvelopOscControlListener(LX lx) {
//     this.lx = lx;    
//   }
    
//   public void oscMessage(OscMessage message) {
//     println("[" + message.getSource() + "] " + message);
//     EnvelopOscClient client = clients.get(message.getSource());
//     if (client == null) {
//       if (message.matches("/lx/register/studioCubes")) {
//         try {
//           clients.put(message.getSource(), new EnvelopOscClient(lx, message.getSource()));
//         } catch (SocketException sx) {
//           sx.printStackTrace();
//         }
//       }
//     } else {
//       client.oscMessage(message);
//     }
//   }
// }
    
// static class EnvelopOscClient implements LXOscListener {
    
//   private final static int NUM_KNOBS = 12;
//   private final KnobListener[] knobs = new KnobListener[NUM_KNOBS]; 

//   private final LX lx; 
//   private final LXOscEngine.Transmitter transmitter;

//   EnvelopOscClient(LX lx, InetAddress source) throws SocketException {
//     this.lx = lx;
//     this.transmitter = lx.engine.osc.transmitter(source, 3434);
        
//     setupListeners();
//     register();
//   }

//   private class KnobListener implements LXParameterListener {
        
//     private final int index;
//     private BoundedParameter parameter;
        
//     private KnobListener(int index) {
//       this.index = index;
//     }
        
//     void register(BoundedParameter parameter) {
//       if (this.parameter != null) {
//         this.parameter.removeListener(this);
//       }
//       this.parameter = parameter;
//       if (this.parameter != null){
//         this.parameter.addListener(this);
//       }
//     }
        
//     void set(double value) {
//       if (this.parameter != null) {
//         this.parameter.setNormalized(value);
//       }
//     }
        
//     public void onParameterChanged(LXParameter p) {
//       sendKnobs(); 
//     }
//   }
    
//   private void setupListeners() {
//     // Build knob listener objects
//     for (int i = 0; i < NUM_KNOBS; ++i) {
//       this.knobs[i] = new KnobListener(i);
//     }

//     // Register to listen for pattern changes 
//     lx.engine.getChannel(0).addListener(new LXChannel.AbstractListener() {
//       public void patternDidChange(LXChannel channel, LXPattern pattern) {
//         int i = 0;
//         for (LXPattern p : channel.getPatterns()) {
//           if (p == pattern) {
//             sendMessage("/lx/pattern/active", i);
//             break;
//           }
//           ++i;
//         }
//         registerKnobs(pattern);
//         sendKnobs();
//       }
//     });
//   }
    
//   public void oscMessage(OscMessage message) {
//     if (message.matches("/lx/register/studioCubes")) {
//       register();
//     } else if (message.matches("/lx/pattern/index")) {
//       lx.goIndex(message.get().toInt());
//     } else if (message.matches("/lx/pattern/parameter")) {
//       // TODO(mcslee): sanitize input
//       knobs[message.getInt()].set(message.getDouble());
//     } else if (message.matches("/lx/tempo/bpm")) {
//       lx.tempo.setBpm(message.getDouble());
//       println("Set bpm to: " + lx.tempo.bpm());
//     } else if (message.matches("/lx/tempo/tap")) {
//       lx.tempo.trigger(false);
//     }
//   }
    
//   void register() {
//     sendMessage("/lx/register");
//     sendPatterns();
//     // registerKnobs(lx.engine.getActivePattern());
//     sendKnobs();
//   }
    
//   void registerKnobs(LXPattern pattern) {
//     int i = 0;
//     for (LXParameter parameter : pattern.getParameters()) {
//       if (i > NUM_KNOBS) {
//         break;
//       }
//       if (parameter instanceof BoundedParameter) {
//         knobs[i++].register((BoundedParameter) parameter);
//       }
//     }
//     while (i < NUM_KNOBS) {
//       knobs[i++].register(null);
//     }
//   }
    
//   void sendKnobs() {
//     OscMessage parameter = new OscMessage("/lx/pattern/parameter/values");
//     for (KnobListener knob : this.knobs) {
//       if (knob.parameter != null) {
//         parameter.add(knob.parameter.getNormalizedf());
//       } else {
//         parameter.add(-1);
//       }
//     }
//     sendPacket(parameter);
//   }
    
//   private void sendPatterns() {
//     //OscMessage message = new OscMessage("/lx/pattern/list");
//     //LXPattern activePattern = lx.engine.getPattern();
//     //int active = 0, i = 0;
//     //for (LXPattern pattern : lx.getPatterns()) {
//     //  message.add(pattern.getName());
//     //  if (pattern == activePattern) {
//     //    active = i;
//     //  }
//     //  ++i;
//     //}
//     //sendPacket(message);    
//     //sendMessage("/lx/pattern/active", active);
//   }
    
//   private void sendMessage(String addressPattern) {
//     sendPacket(new OscMessage(addressPattern));
//   }
    
//   private void sendMessage(String addressPattern, int value) {
//     sendPacket(new OscMessage(addressPattern).add(value));
//   }
    
//   private void sendPacket(OscPacket packet) {
//     try {
//       this.transmitter.send(packet);
//     } catch (IOException iox) {
//       iox.printStackTrace();
//     }
//   }
// }
//import org.timothyb89.lifx.net.BroadcastListener;
//import java.math.BigInteger;

    /*
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 */

public void buildOutputs(final LX lx) {
    networkMonitor.networkDevices.addListener(new ListListener<NetworkDevice>() {
        public void itemAdded(int index, NetworkDevice device) {
            String macAddr = NetworkUtils.macAddrToString(device.macAddress);
            String physid = macToPhysid.get(macAddr);
            if (physid == null) {
                physid = macAddr;
                println("WARNING: MAC address not in physid_to_mac.json: " + macAddr);
            }
            final SLController controller = new SLController(lx, device, physid);
            controllers.add(index, controller);
            dispatcher.dispatchEngine(new Runnable() {
                public void run() {
                    lx.addOutput(controller);
                }
            });
            //controller.enabled.setValue(false);
        }
        public void itemRemoved(int index, NetworkDevice device) {
            // final SLController controller = controllers.remove(index);
            // dispatcher.dispatchEngine(new Runnable() {
            //   public void run() {
            //     lx.removeOutput(controller);
            //   }
            // });
        }
    });

    lx.addOutput(new SLController(lx, "10.200.1.255"));
    
    //lx.addOutput(new LIFXOutput());
}

static final int redGamma[] = new int[256];
static final int greenGamma[] = new int[256];
static final int blueGamma[] = new int[256];

final float[][] gammaSet = {
    { 2, 2.1f, 2.8f },
    { 2, 2.2f, 2.8f },
};

final DiscreteParameter gammaSetIndex = new DiscreteParameter("GMA", gammaSet.length+1);
final BoundedParameter redGammaFactor = new BoundedParameter("RGMA", 2, 1, 4);
final BoundedParameter greenGammaFactor = new BoundedParameter("GGMA", 2.2f, 1, 4);
final BoundedParameter blueGammaFactor = new BoundedParameter("BGMA", 2.8f, 1, 4);

public void setupGammaCorrection() {
    final float redGammaOrig = redGammaFactor.getValuef();
    final float greenGammaOrig = greenGammaFactor.getValuef();
    final float blueGammaOrig = blueGammaFactor.getValuef();
    gammaSetIndex.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter parameter) {
            if (gammaSetIndex.getValuei() == 0) {
                redGammaFactor.reset(redGammaOrig);
                greenGammaFactor.reset(greenGammaOrig);
                blueGammaFactor.reset(blueGammaOrig);
            } else {
                redGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][0]);
                greenGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][1]);
                blueGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][2]);
            }
        }
    });
    redGammaFactor.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter parameter) {
            buildGammaCorrection(redGamma, parameter.getValuef());
        }
    });
    buildGammaCorrection(redGamma, redGammaFactor.getValuef());
    greenGammaFactor.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter parameter) {
            buildGammaCorrection(greenGamma, parameter.getValuef());
        }
    });
    buildGammaCorrection(greenGamma, greenGammaFactor.getValuef());
    blueGammaFactor.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter parameter) {
            buildGammaCorrection(blueGamma, parameter.getValuef());
        }
    });
    buildGammaCorrection(blueGamma, blueGammaFactor.getValuef());
}

public void buildGammaCorrection(int[] gammaTable, float gammaCorrection) {
    for (int i = 0; i < 256; i++) {
        gammaTable[i] = (int)(pow(1.0f * i / 255, gammaCorrection) * 255 + 0.5f);
    }
}



// public class LIFXOutput extends LXOutput {
//   DatagramSocket dsocket;

//   int packetSizeBytes = 49;
//   byte[] packetData;

//   public LIFXOutput() {
//     super(lx);
//     packetData = new byte[packetSizeBytes];
//   }

//   void onSend(int[] colors) {
//     // Create data socket connection if needed
//     if (dsocket == null) {
//       try {
//         dsocket = new DatagramSocket();
//         dsocket.connect(new InetSocketAddress("10.1.10.255", 56700));
//         //socket.setTcpNoDelay(true);
//         // output = socket.getOutputStream();
//       }
//       catch (ConnectException e) {  dispose();  }
//       catch (IOException      e) {  dispose();  }
//       if (dsocket == null) return;
//     }

//     for (LIFXBulb bulb : model.lifxBulbs)
//       sendPacket(bulb.macAddress, colors[bulb.point.index]);
//   }

//   void sendPacket(String macAddressString, int col) {
//     // format mac address
//     String[] stringBytes = macAddressString.split(":");
//     byte[] macAddress = new byte[stringBytes.length];

//     for (int x = 0; x < stringBytes.length; x++) {
//         BigInteger temp = new BigInteger(stringBytes[x], 16);
//         byte[] raw = temp.toByteArray();
//         macAddress[x] = raw[raw.length - 1];
//     }

//     // format colors
//     int hue = (int)(LXColor.h(col)/360 * 65535);
//     int sat = (int)(LXColor.s(col)/100 * 65535);
//     int bri = (int)(LXColor.b(col)/100 * 65535);

//     // build packet
//     packetData[0] = (byte)0x00;
//     packetData[1] = (byte)0x00;
//     packetData[2] = (byte)0x00;
//     packetData[3] = (byte)0x14;

//     // client id (source)
//     packetData[4] = (byte)0x00;
//     packetData[5] = (byte)0x00;
//     packetData[6] = (byte)0x00;
//     packetData[7] = (byte)0x00;

//     // frame address
//     packetData[8]  = macAddress[0];
//     packetData[9]  = macAddress[1];
//     packetData[10] = macAddress[2];
//     packetData[11] = macAddress[3];
//     packetData[12] = macAddress[4];
//     packetData[13] = macAddress[5];
//     packetData[14] = (byte)0x00;
//     packetData[15] = (byte)0x00;

//     // *reserved*
//     packetData[16] = (byte)0x00;
//     packetData[17] = (byte)0x00;
//     packetData[18] = (byte)0x00;
//     packetData[19] = (byte)0x00;
//     packetData[20] = (byte)0x00;
//     packetData[21] = (byte)0x00;

//     // response
//     packetData[22] = (byte)0x00;

//     // sequence number
//     packetData[23] = (byte)0x00;

//     // protocol header
//     packetData[24] = (byte)0x00;
//     packetData[25] = (byte)0x00;
//     packetData[26] = (byte)0x00;
//     packetData[27] = (byte)0x00;
//     packetData[28] = (byte)0x00;
//     packetData[29] = (byte)0x00;
//     packetData[30] = (byte)0x00;
//     packetData[31] = (byte)0x00;

//     // message type
//     packetData[32] = (byte)0x66;
//     packetData[33] = (byte)0x00;

//     // *reserved*
//     packetData[34] = (byte)0x00;
//     packetData[35] = (byte)0x00;

//     /* payload ------*/
//     // reserved
//     packetData[36] = (byte)0x00;

//     // hue
//     packetData[37] = (byte)hue;
//     packetData[38] = (byte)(hue >> 8);

//     // saturation
//     packetData[39] = (byte)sat;
//     packetData[40] = (byte)(sat >> 8);

//     // brightness
//     packetData[41] = (byte)bri;
//     packetData[42] = (byte)(bri >> 8);

//     // kelvin
//     packetData[43] = (byte)0xAC;
//     packetData[44] = (byte)0x0D;

//     // milliseconds
//     packetData[45] = (byte)0x00;
//     packetData[46] = (byte)0x00;
//     packetData[47] = (byte)0x00;
//     packetData[48] = (byte)0x00;

//     try {
//       dsocket.send(new java.net.DatagramPacket(packetData, packetSizeBytes));
//     } catch (Exception e) {dispose();
//   }
// }


    // List<Bulb> bulbs;

    // public LIFXOutput() throws IOException {
    //  super(lx);

    //  bulbs = new ArrayList<Bulb>();
    //  BroadcastListener listener = new BroadcastListener();
    //  listener.bus().register(this);
    //  listener.startListen();

    //  println(listener);
    //  enabled.setValue(true);
    // }

    // void onSend(int[] colors) {
    //  if (model.lifxBulbs.size() < 1) return;

    //  LIFXBulb bulbModel = model.lifxBulbs.get(0);
    //  //for (LIFXBulb bulb : model.lifxBulbs) {
    //    for (Bulb bulbOutput : this.bulbs) {
    //      //if (THEY_DONT_MATCH) continue; <- make them all light up for now
                
    //      color c = colors[bulbModel.point.index];
    //      int r = (int)LXColor.red(c);
    //      int g = (int)LXColor.green(c);
    //      int b = (int)LXColor.blue(c);
    //      try {
    //        bulbOutput.setColor(LIFXColor.fromRGB(r, g, b) , 1);
    //      } catch (IOException e) {}
    //      //break;
    //    }
    //  //}
    // }
    
    // public void gatewayFound(GatewayDiscoveredEvent ev) {
    //  Gateway g = ev.getGateway();
    //  g.bus().register(this);

    //  println("gatewayFound");
    //  try {
    //    g.connect(); // automatically discovers bulbs
    //  } catch (IOException ex) { }
    // }
    
    // public void bulbDiscovered(GatewayBulbDiscoveredEvent event) throws IOException {
    //  this.bulbs.add(event.getBulb());

    //  // register for bulb events
    //  event.getBulb().bus().register(this);

    //  println("bulb discovered");
        
    //  // send some packets
    //  // event.getBulb().turnOff();
    //  // event.getBulb().setColor(LIFXColor.fromRGB(0, 255, 0));
    // }

    // public void bulbUpdated(BulbStatusUpdatedEvent event) {
    //  //System.out.println("bulb updated");
    // }
    
//}


ListenableList<SLController> controllers = new ListenableList<SLController>();

class SLController extends LXOutput {
    Socket        socket;
    DatagramSocket dsocket;
    OutputStream    output;
    NetworkDevice networkDevice;
    String        cubeId;
    InetAddress   host;
    boolean       isBroadcast;

    // Trip had to change order for Cisco as workaround for rotation bug
    final int[]   STRIP_ORD      = new int[] { 
                                                                                         //RED
                                                                                         9, 10, 11, 
                                                                                         // GREEN 
                                                                                         0, 1, 2,   
                                                                                         // BLUE
                                                                                         3, 4, 5,
                                                                                         // WHITE
                                                                                         6, 7, 8 }; 
    // final int[]  STRIP_ORD      = new int[] { 6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5 };

    static final int HEADER_LENGTH = 4;
    static final int BYTES_PER_PIXEL = 3;

    final int numStrips = STRIP_ORD.length;
    int numPixels;
    int contentSizeBytes;
    int packetSizeBytes;
    byte[] packetData;

    SLController(LX lx, NetworkDevice device, String cubeId) {
        this(lx, device, device.ipAddress, cubeId, false);
    }

    SLController(LX lx, String _host, String _cubeId) {
        this(lx, _host, _cubeId, false);
    }

    SLController(LX lx, String _host) {
        this(lx, _host, "", true);
    }

    private SLController(LX lx, String host, String cubeId, boolean isBroadcast) {
        this(lx, null, NetworkUtils.ipAddrToInetAddr(host), cubeId, isBroadcast);
    }

    private SLController(LX lx, NetworkDevice networkDevice, InetAddress host, String cubeId, boolean isBroadcast) {
        super(lx);

        this.networkDevice = networkDevice;
        this.host = host;
        this.cubeId = cubeId;
        this.isBroadcast = isBroadcast;

        enabled.setValue(true);
    }

    public void initPacketData(int numPixels) {
        this.numPixels = numPixels;
        contentSizeBytes = BYTES_PER_PIXEL * numPixels;
        packetSizeBytes = HEADER_LENGTH + contentSizeBytes; // add header length
        packetData = new byte[packetSizeBytes];

        setHeader();
    }

    public void setHeader() {
        packetData[0] = 0;  // Channel
        packetData[1] = 0;  // Command (Set pixel colors)
        // indices 2,3 = high byte, low byte
        // 3 bytes * 180 pixels = 540 bytes = 0x021C
        packetData[2] = (byte)((contentSizeBytes >> 8) & 0xFF);
        packetData[3] = (byte)((contentSizeBytes >> 0) & 0xFF);
    }

    public void setPixel(int number, int c) {
        //println("number: "+number);
        int offset = 4 + number * 3;

        // Extract individual colors
            int r = c >> 16 & 0xFF;
            int g = c >> 8 & 0xFF;
            int b = c & 0xFF;

            // Repack gamma corrected colors
        packetData[offset + 0] = (byte) redGamma[r];
        packetData[offset + 1] = (byte) greenGamma[g];
        packetData[offset + 2] = (byte) blueGamma[b];
    }

    public void onSend(int[] colors) {
        if (isBroadcast != broadcastPacketTester.enabled.isOn()) return;

        // Create data socket connection if needed
        if (dsocket == null) {
            try {
                dsocket = new DatagramSocket();
                dsocket.connect(new InetSocketAddress(host, 7890));
                //socket.setTcpNoDelay(true);
                // output = socket.getOutputStream();
            }
            catch (ConnectException e) {  dispose();  }
            catch (IOException      e) {  dispose();  }
            if (dsocket == null) return;
        }

        // Find the Cube we're outputting to
        // If we're on broadcast, use cube 0 for all cubes, even
        // if that cube isn't modelled yet
        // Use the mac address to find the cube if we have it
        // Otherwise use the cube id
        Cube cube = null;
        if ((cubeOutputTester.enabled.isOn() || isBroadcast) && model.cubes.size() > 0) {
            cube = model.cubes.get(0);
        } else {
            for (Cube c : model.cubes) {
                if (c.id != null && c.id.equals(cubeId)) {
                    cube = c;
                    break;
                }
            }
        }

        // Initialize packet data base on cube type.
        // If we don't know the cube type, default to
        // using the cube type with the most pixels
        Cube.Type cubeType = cube != null ? cube.type : Cube.CUBE_TYPE_WITH_MOST_PIXELS;
        int numPixels = cubeType.POINTS_PER_CUBE;
        if (packetData == null || packetData.length != numPixels) {
            initPacketData(numPixels);
        }

        // Fill the datagram with pixel data
        // Fill with all black if we don't have cube data
        if (cube != null) {
            for (int stripNum = 0; stripNum < numStrips; stripNum++) {
                int stripId = STRIP_ORD[stripNum];
                Strip strip = cube.strips.get(stripId);

                for (int i = 0; i < strip.metrics.numPoints; i++) {
                    LXPoint point = strip.getPoints().get(i);
                    setPixel(stripNum * strip.metrics.numPoints + i, colors[point.index]);
                }
            }
        } else {
            for (int i = 0; i < numPixels; i++) {
                setPixel(i, LXColor.BLACK);
            }
        }

        // Send the cube data to the cube. yay!
        try { 
            //println("packetSizeBytes: "+packetSizeBytes);
            dsocket.send(new java.net.DatagramPacket(packetData,packetSizeBytes));} 
        catch (Exception e) {dispose();}
    }  

    public void dispose() {
        if (dsocket != null)  println("Disconnected from OPC server");
        println("Failed to connect to OPC server " + host);
        socket = null;
        dsocket = null;
    }
}
//---------------------------------------------------------------------------------------------

// Helper class for testing cube output
// When a cube is recognized on the network,
// if this option is enabled, the output classes will
// send the color data for cube 0 regardless of if the
// cube that just connected is modelled yet or not
class CubeOutputTester {
    final BooleanParameter enabled = new BooleanParameter("Test output enabled");
    CubeOutputTester(LX lx) {
        //moduleRegistrar.modules.add(new Module("Test output", enabled));
    }
}

class BroadcastPacketTester {
    final BooleanParameter enabled = new BooleanParameter("Broadcast packet enabled");
    BroadcastPacketTester(LX lx) {
        //moduleRegistrar.modules.add(new Module("Broadcast packet", enabled));
    }
}

//---------------------------------------------------------------------------------------------
// class UIOutput extends UIWindow {
//   UIOutput(float x, float y, float w, float h) {
//     super(lx.ui,"Output",x,y,w,h);
//     float yPos = UIWindow.TITLE_LABEL_HEIGHT - 2;
//     final SortedSet<Beagle> sortedBeagles = new TreeSet<Beagle>(new Comparator<Beagle>() {
//       int compare(Beagle o1, Beagle o2) {
//         try {
//           return Integer.parseInt(o1.cubeId) - Integer.parseInt(o2.cubeId);
//         } catch (NumberFormatException e) {
//           return o1.cubeId.compareTo(o2.cubeId);
//         }
//       }
//     });
//     final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
//     for (Beagle b : beagles) { sortedBeagles.add(b); }
//     for (Beagle b : sortedBeagles) { items.add(new BeagleItem(b)); }
//     final UIItemList outputList = new UIItemList(1, yPos, width-2, height-yPos-1);
//     outputList
//       .setItems     (items    )
//       .addToContainer   (this   );
//     beagles.addListener(new ListListener<Beagle>() {
//       void itemAdded(final int index, final Beagle b) {
//         dispatcher.dispatchUi(new Runnable() {
//           public void run() {
//             if (b.networkDevice != null) b.networkDevice.version.addListener(deviceVersionListener);
//             sortedBeagles.add(b);
//             items.clear();
//             for (Beagle b : sortedBeagles) { items.add(new BeagleItem(b)); }
//             outputList.setItems(items);
//             setTitle(items.size());
//             redraw();
//           }
//         });
//       }
//       void itemRemoved(final int index, final Beagle b) {
//         dispatcher.dispatchUi(new Runnable() {
//           public void run() {
//             if (b.networkDevice != null) b.networkDevice.version.removeListener(deviceVersionListener);
//             sortedBeagles.remove(b);
//             items.clear();
//             for (Beagle b : sortedBeagles) { items.add(new BeagleItem(b)); }
//             outputList.setItems(items);
//             setTitle(items.size());
//             redraw();
//           }
//         });
//       }
//     });
//     setTitle(items.size());

//     BooleanParameter allOutputsEnabled = new BooleanParameter("allOutputsEnabled", true);
//     UIButton outputsEnabledButton = new UIButton(87, 4, 50, 15);
//     outputsEnabledButton.setLabel("Connect").setParameter(allOutputsEnabled);
//     outputsEnabledButton.addToContainer(this);
//     allOutputsEnabled.addListener(new LXParameterListener() {
//       void onParameterChanged(LXParameter parameter) {
//         for (Beagle beagle : beagles) {
//           beagle.enabled.setValue(((BooleanParameter)parameter).isOn());
//         }
//       }
//     });
//   }

//   private final IntListener deviceVersionListener = new IntListener() {
//     public void onChange(int version) {
//       dispatcher.dispatchUi(new Runnable() {
//         public void run() {
//           redraw();
//         }
//       });
//     }
//   };

//   private void setTitle(int count) {
//     setTitle("Output (" + count + ")");
//   }

//   class BeagleItem extends UIItemList.AbstractItem {
//     final Beagle beagle;
//     BeagleItem(Beagle _beagle) {
//       this.beagle = _beagle;
//       beagle.enabled.addListener(new LXParameterListener() {
//         public void onParameterChanged(LXParameter parameter) { redraw(); }
//       });
//     }
//     String  getLabel  () {
//       if (beagle.networkDevice != null && beagle.networkDevice.version.get() != -1) {
//         return beagle.cubeId + " (v" + beagle.networkDevice.version + ")";
//       } else {
//         return beagle.cubeId;
//       }
//     }
//     boolean isSelected() { return beagle.enabled.isOn(); }
//     void onMousePressed(boolean hasFocus) { beagle.enabled.toggle(); }
//   }
// }
//---------------------------------------------------------------------------------------------



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

public static class CubeEQ extends LXPattern {

    private GraphicMeter eq = null;
    private LXAudioInput audioInput;

    private final BoundedParameter edge = new BoundedParameter("EDGE", 0.5f);
    private final BoundedParameter clr = new BoundedParameter("CLR", 0.1f, 0, .5f);
    private final BoundedParameter blockiness = new BoundedParameter("BLK", 0.5f);

    public CubeEQ(LX lx) {
        super(lx);
        audioInput = lx.engine.audio.getInput();
    }

    public void onActive() {
        if (eq == null) {
            eq = new GraphicMeter(audioInput);
            // eq.range.setValue(48);
            // eq.release.setValue(800);
            // addParameter(eq.gain);
            // addParameter(eq.range);
            // addParameter(eq.attack);
            // addParameter(eq.release);
            //addParameter(eq.slope);
            addParameter(edge);
            addParameter(clr);
            addParameter(blockiness);
            addModulator(eq).start();

            eq.gain.setValue(30);
        }
    }

    public void run(double deltaMs) {
        float edgeConst = 2 + 30*edge.getValuef();
        float clrConst = 1.1f + clr.getValuef();

        for (LXPoint p : model.points) {
            float avgIndex = constrain(2 + p.x / model.xMax * (eq.getNumBands()-4), 0, eq.getNumBands()-4);
            int avgFloor = (int) avgIndex;

            float leftVal = eq.getBandf(avgFloor);
            float rightVal = eq.getBandf(avgFloor+1);
            float smoothValue = lerp(leftVal, rightVal, avgIndex-avgFloor);
            
            float chunkyValue = (
                eq.getBandf(avgFloor/4*4) +
                eq.getBandf(avgFloor/4*4 + 1) +
                eq.getBandf(avgFloor/4*4 + 2) +
                eq.getBandf(avgFloor/4*4 + 3)
            ) / 4.f; 
            
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

// public static class Swarm extends LXPattern {
    
//   SawLFO offset = new SawLFO(0, 1, 1000);
//   SinLFO rate = new SinLFO(350, 1200, 63000);
//   SinLFO falloff = new SinLFO(15, 50, 17000);
//   SinLFO fX = new SinLFO(model.xMin, model.xMax, 19000);
//   SinLFO fY = new SinLFO(model.yMin, model.yMax, 11000);
//   SinLFO hOffX = new SinLFO(model.xMin, model.xMax, 13000);

//   public Swarm(LX lx) {
//     super(lx);
        
//     addModulator(offset).trigger();
//     addModulator(rate).trigger();
//     addModulator(falloff).trigger();
//     addModulator(fX).trigger();
//     addModulator(fY).trigger();
//     addModulator(hOffX).trigger();
//     offset.setPeriod(rate);
//   }

//   float modDist(float v1, float v2, float mod) {
//     v1 = v1 % mod;
//     v2 = v2 % mod;
//     if (v2 > v1) {
//       return min(v2-v1, v1+mod-v2);
//     } 
//     else {
//       return min(v1-v2, v2+mod-v1);
//     }
//   }

//   void run(double deltaMs) {
//     float s = 0;
//     for (Strip strip : model.strips) {
//       int i = 0;
//       for (LXPoint p : strip.points) {
//         float fV = max(-1, 1 - dist(p.x/2., p.y, fX.getValuef()/2., fY.getValuef()) / 64.);
//        // println("fv: " + fV); 
//         colors[p.index] = lx.hsb(
//         palette.getHuef() + 0.3 * abs(p.x - hOffX.getValuef()),
//         constrain(80 + 40 * fV, 0, 100), 
//         constrain(100 - 
//           (30 - fV * falloff.getValuef()) * modDist(i + (s*63)%61, offset.getValuef() * strip.metrics.numPoints, strip.metrics.numPoints), 0, 100)
//           );
//         ++i;
//       } 
//       ++s;
//     }
//   }
// }

// public static class SpaceTime extends LXPattern {

//   SinLFO pos = new SinLFO(0, 1, 3000);
//   SinLFO rate = new SinLFO(1000, 9000, 13000);
//   SinLFO falloff = new SinLFO(10, 70, 5000);
//   float angle = 0;

//   BoundedParameter rateParameter = new BoundedParameter("RATE", 0.5);
//   BoundedParameter sizeParameter = new BoundedParameter("SIZE", 0.5);

//   public SpaceTime(LX lx) {
//     super(lx);

//     addModulator(pos).trigger();
//     addModulator(rate).trigger();
//     addModulator(falloff).trigger();
//     pos.setPeriod(rate);
//     addParameter(rateParameter);
//     addParameter(sizeParameter);
//   }

//   public void onParameterChanged(LXParameter parameter) {
//     if (parameter == rateParameter) {
//       rate.stop();
//       rate.setValue(9000 - 8000*parameter.getValuef());
//     }  else if (parameter == sizeParameter) {
//       falloff.stop();
//       falloff.setValue(70 - 60*parameter.getValuef());
//     }
//   }

//   void run(double deltaMs) {
        // angle += deltaMs * 0.0007;
        // float sVal1 = model.strips.size() * (0.5 + 0.5*sin(angle));
        // float sVal2 = model.strips.size() * (0.5 + 0.5*cos(angle));

        // float pVal = pos.getValuef();
        // float fVal = falloff.getValuef();

        // int s = 0;
        // for (Strip strip : model.strips) {
        //   int i = 0;
        //   for (LXPoint p : strip.points) {
        //     colors[p.index] = lx.hsb(
        //       palette.getHuef() + 360 - p.x*.2 + p.y * .3,
        //       constrain(.4 * min(abs(s - sVal1), abs(s - sVal2)), 20, 100),
        //       max(0, 100 - fVal*abs(i - pVal*(strip.metrics.numPoints - 1)))
        //     );
        //     ++i;
        //   }
        //   ++s;
        // }
//   }
// }

public static class ShiftingPlane extends LXPattern {

    final BoundedParameter hueShift = new BoundedParameter("hShift", 0.5f, 0, 1);

    final SinLFO a = new SinLFO(-.2f, .2f, 5300);
    final SinLFO b = new SinLFO(1, -1, 13300);
    final SinLFO c = new SinLFO(-1.4f, 1.4f, 5700);
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
                hv + (abs(p.x-model.cx)*.6f + abs(p.y-model.cy)*.9f + abs(p.z - model.cz))*hueShift.getValuef(),
                constrain(110 - d*6, 0, 100),
                constrain(130 - 7*d, 0, 100)
            );
        }
    }
}

// public static class MidiFlash extends LXPattern {
    
//   private final LinearEnvelope brt = new LinearEnvelope("Brt", 100, 0, 1000);  
    
//   public MidiFlash(LX lx) {
//     super(lx);
//     addModulator(brt.setValue(0));
//   }
    
//   @Override
//   public void noteOnReceived(MidiNoteOn note) {
//     brt.setValue(note.getVelocity() / 127. * 100).start();
//   }
    
//   public void run(double deltaMs) {
//     for (LXPoint p : model.points) {
//       colors[p.index] = palette.getColor(p, brt.getValuef());
//     }
//   }
// }

// public class EnvelopDecode extends LXPattern {
    
//   public final BoundedParameter fade = new BoundedParameter("Fade", 1*FEET, 0.001, 4*FEET); 
    
//   public EnvelopDecode(LX lx) {
//     super(lx);
//     addParameter(fade);
//   }
    
//   public void run(double deltaMs) {
//     float fv = fade.getValuef();
//     float falloff = 100 / fv;
//     for (Column column : venue.columns) {
//       float level = studioCubes.decode.channels[column.index].getValuef() * (model.yRange / 2.);
//       for (LXPoint p : column.points) {
//         float yn = abs(p.y - model.cy);
//         float b = falloff * (level - yn);
//         colors[p.index] = palette.getColor(p, constrain(b, 0, 100));
//       }
//     }
//   }
// }

// public class SoundObjects extends LXPattern implements UIPattern {
    
//   public final BoundedParameter size = new BoundedParameter("Base", 4*FEET, 0, 24*FEET);
//   public final BoundedParameter response = new BoundedParameter("Level", 0, 1*FEET, 24*FEET);
    
//   public SoundObjects(LX lx) {
//     super(lx);
//     for (StudioCubes.Source.Channel object : studioCubes.source.channels) {
//       addLayer(new Layer(lx, object));
//     }
//     addParameter(size);
//     addParameter(response);
//   }
    
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

// public class Bouncing extends LXPattern {
    
//   public CompoundParameter gravity = new CompoundParameter("Gravity", -200, -100, -400);
//   public CompoundParameter size = new CompoundParameter("Length", 2*FEET, 1*FEET, 4*FEET);
//   public CompoundParameter amp = new CompoundParameter("Height", model.yRange, 1*FEET, model.yRange);
    
//   public Bouncing(LX lx) {
//     super(lx);
//     // for (Column column : venue.columns) {
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

// public class Movers extends LXPattern {
    
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








static final class Utils {

    // Processing functions
    // (duplicated here for easy access)

    static final float EPSILON = PConstants.EPSILON;
    static final float MAX_FLOAT = PConstants.MAX_FLOAT;
    static final float MIN_FLOAT = PConstants.MIN_FLOAT;
    static final int MAX_INT = PConstants.MAX_INT;
    static final int MIN_INT = PConstants.MIN_INT;

    // shapes
    static final int VERTEX = PConstants.VERTEX;
    static final int BEZIER_VERTEX = PConstants.BEZIER_VERTEX;
    static final int QUADRATIC_VERTEX = PConstants.QUADRATIC_VERTEX;
    static final int CURVE_VERTEX = PConstants.CURVE_VERTEX;
    static final int BREAK = PConstants.BREAK;

    // useful goodness
    static final float PI = PConstants.PI;
    static final float HALF_PI = PConstants.HALF_PI;
    static final float THIRD_PI = PConstants.THIRD_PI;
    static final float QUARTER_PI = PConstants.QUARTER_PI;
    static final float TWO_PI = PConstants.TWO_PI;
    static final float TAU = PConstants.TAU;

    static final float DEG_TO_RAD = PConstants.DEG_TO_RAD;
    static final float RAD_TO_DEG = PConstants.RAD_TO_DEG;

    static final String WHITESPACE = PConstants.WHITESPACE;

    // for colors and/or images
    static final int RGB   = PConstants.RGB;
    static final int ARGB  = PConstants.ARGB;
    static final int HSB   = PConstants.HSB;
    static final int ALPHA = PConstants.ALPHA;

    // image file types
    static final int TIFF  = PConstants.TIFF;
    static final int TARGA = PConstants.TARGA;
    static final int JPEG  = PConstants.JPEG;
    static final int GIF   = PConstants.GIF;

    // filter/convert types
    static final int BLUR      = PConstants.BLUR;
    static final int GRAY      = PConstants.GRAY;
    static final int INVERT    = PConstants.INVERT;
    static final int OPAQUE    = PConstants.OPAQUE;
    static final int POSTERIZE = PConstants.POSTERIZE;
    static final int THRESHOLD = PConstants.THRESHOLD;
    static final int ERODE     = PConstants.ERODE;
    static final int DILATE    = PConstants.DILATE;

    // blend mode keyword definitions
    static final int REPLACE    = PConstants.REPLACE;
    static final int BLEND      = PConstants.BLEND;
    static final int ADD        = PConstants.ADD;
    static final int SUBTRACT   = PConstants.SUBTRACT;
    static final int LIGHTEST   = PConstants.LIGHTEST;
    static final int DARKEST    = PConstants.DARKEST;
    static final int DIFFERENCE = PConstants.DIFFERENCE;
    static final int EXCLUSION  = PConstants.EXCLUSION;
    static final int MULTIPLY   = PConstants.MULTIPLY;
    static final int SCREEN     = PConstants.SCREEN;
    static final int OVERLAY    = PConstants.OVERLAY;
    static final int HARD_LIGHT = PConstants.HARD_LIGHT;
    static final int SOFT_LIGHT = PConstants.SOFT_LIGHT;
    static final int DODGE      = PConstants.DODGE;
    static final int BURN       = PConstants.BURN;

    // for messages
    static final int CHATTER   = PConstants.CHATTER;
    static final int COMPLAINT = PConstants.COMPLAINT;
    static final int PROBLEM   = PConstants.PROBLEM;

    // types of transformation matrices
    static final int PROJECTION = PConstants.PROJECTION;
    static final int MODELVIEW  = PConstants.MODELVIEW;

    // types of projection matrices
    static final int CUSTOM       = PConstants.CUSTOM;
    static final int ORTHOGRAPHIC = PConstants.ORTHOGRAPHIC;
    static final int PERSPECTIVE  = PConstants.PERSPECTIVE;

    // shapes
    static final int GROUP           = PConstants.GROUP;

    static final int POINT           = PConstants.POINT;
    static final int POINTS          = PConstants.POINTS;

    static final int LINE            = PConstants.LINE;
    static final int LINES           = PConstants.LINES;
    static final int LINE_STRIP      = PConstants.LINE_STRIP;
    static final int LINE_LOOP       = PConstants.LINE_LOOP;

    static final int TRIANGLE        = PConstants.TRIANGLE;
    static final int TRIANGLES       = PConstants.TRIANGLES;
    static final int TRIANGLE_STRIP  = PConstants.TRIANGLE_STRIP;
    static final int TRIANGLE_FAN    = PConstants.TRIANGLE_FAN;

    static final int QUAD            = PConstants.QUAD;
    static final int QUADS           = PConstants.QUADS;
    static final int QUAD_STRIP      = PConstants.QUAD_STRIP;

    static final int POLYGON         = PConstants.POLYGON;
    static final int PATH            = PConstants.PATH;

    static final int RECT            = PConstants.RECT;
    static final int ELLIPSE         = PConstants.ELLIPSE;
    static final int ARC             = PConstants.ARC;

    static final int SPHERE          = PConstants.SPHERE;
    static final int BOX             = PConstants.BOX;

    // shape closing modes
    static final int OPEN = PConstants.OPEN;
    static final int CLOSE = PConstants.CLOSE;

    // shape drawing modes
    static final int CORNER   = PConstants.CORNER;
    static final int CORNERS  = PConstants.CORNERS;
    static final int RADIUS   = PConstants.RADIUS;
    static final int CENTER   = PConstants.CENTER;
    static final int DIAMETER = PConstants.DIAMETER;

    // arc drawing modes
    static final int CHORD  = PConstants.CHORD;
    static final int PIE    = PConstants.PIE;

    // vertically alignment modes for text
    static final int BASELINE = PConstants.BASELINE;
    static final int TOP = PConstants.TOP;
    static final int BOTTOM = PConstants.BOTTOM;

    // uv texture orientation modes
    static final int NORMAL     = PConstants.NORMAL;
    static final int IMAGE      = PConstants.IMAGE;

    // texture wrapping modes
    static final int CLAMP = PConstants.CLAMP;
    static final int REPEAT = PConstants.REPEAT;

    // text placement modes
    static final int MODEL = PConstants.MODEL;
    static final int SHAPE = PConstants.SHAPE;

    // stroke modes
    static final int SQUARE   = PConstants.SQUARE;
    static final int ROUND    = PConstants.ROUND;
    static final int PROJECT  = PConstants.PROJECT;
    static final int MITER    = PConstants.MITER;
    static final int BEVEL    = PConstants.BEVEL;

    // lighting
    static final int AMBIENT = PConstants.AMBIENT;
    static final int DIRECTIONAL  = PConstants.DIRECTIONAL;
    static final int SPOT = PConstants.SPOT;

    // key constants
    static final char BACKSPACE = PConstants.BACKSPACE;
    static final char TAB       = PConstants.TAB;
    static final char ENTER     = PConstants.ENTER;
    static final char RETURN    = PConstants.RETURN;
    static final char ESC       = PConstants.ESC;
    static final char DELETE    = PConstants.DELETE;
    static final int CODED     = PConstants.CODED;

    static final int UP        = PConstants.UP;
    static final int DOWN      = PConstants.DOWN;
    static final int LEFT      = PConstants.LEFT;
    static final int RIGHT     = PConstants.RIGHT;

    static final int ALT       = PConstants.ALT;
    static final int CONTROL   = PConstants.CONTROL;
    static final int SHIFT     = PConstants.SHIFT;

    // orientations (only used on Android, ignored on desktop)
    static final int PORTRAIT = PConstants.PORTRAIT;
    static final int LANDSCAPE = PConstants.LANDSCAPE;
    static final int SPAN = PConstants.SPAN;

    // cursor types
    static final int ARROW = PConstants.ARROW;
    static final int CROSS = PConstants.CROSS;
    static final int HAND  = PConstants.HAND;
    static final int MOVE  = PConstants.MOVE;
    static final int TEXT  = PConstants.TEXT;
    static final int WAIT  = PConstants.WAIT;

    // hints
    static final int DISABLE_DEPTH_TEST         = PConstants.DISABLE_DEPTH_TEST;
    static final int ENABLE_DEPTH_TEST          = PConstants.ENABLE_DEPTH_TEST;
    static final int ENABLE_DEPTH_SORT          = PConstants.ENABLE_DEPTH_SORT;
    static final int DISABLE_DEPTH_SORT         = PConstants.DISABLE_DEPTH_SORT;
    static final int DISABLE_OPENGL_ERRORS      = PConstants.DISABLE_OPENGL_ERRORS;
    static final int ENABLE_OPENGL_ERRORS       = PConstants.ENABLE_OPENGL_ERRORS;
    static final int DISABLE_DEPTH_MASK         = PConstants.DISABLE_DEPTH_MASK;
    static final int ENABLE_DEPTH_MASK          = PConstants.ENABLE_DEPTH_MASK;
    static final int DISABLE_OPTIMIZED_STROKE   = PConstants.DISABLE_OPTIMIZED_STROKE;
    static final int ENABLE_OPTIMIZED_STROKE    = PConstants.ENABLE_OPTIMIZED_STROKE;
    static final int ENABLE_STROKE_PERSPECTIVE  = PConstants.ENABLE_STROKE_PERSPECTIVE;
    static final int DISABLE_STROKE_PERSPECTIVE = PConstants.DISABLE_STROKE_PERSPECTIVE;
    static final int DISABLE_TEXTURE_MIPMAPS    = PConstants.DISABLE_TEXTURE_MIPMAPS;
    static final int ENABLE_TEXTURE_MIPMAPS     = PConstants.ENABLE_TEXTURE_MIPMAPS;
    static final int ENABLE_STROKE_PURE         = PConstants.ENABLE_STROKE_PURE;
    static final int DISABLE_STROKE_PURE        = PConstants.DISABLE_STROKE_PURE;
    static final int ENABLE_BUFFER_READING      = PConstants.ENABLE_BUFFER_READING;
    static final int DISABLE_BUFFER_READING     = PConstants.DISABLE_BUFFER_READING;
    static final int DISABLE_KEY_REPEAT         = PConstants.DISABLE_KEY_REPEAT;
    static final int ENABLE_KEY_REPEAT          = PConstants.ENABLE_KEY_REPEAT;
    static final int DISABLE_ASYNC_SAVEFRAME    = PConstants.DISABLE_ASYNC_SAVEFRAME;
    static final int ENABLE_ASYNC_SAVEFRAME     = PConstants.ENABLE_ASYNC_SAVEFRAME;
    static final int HINT_COUNT                 = PConstants.HINT_COUNT;

    //////////////////////////////////////////////////////////////
    // getting the time
    public static final int second() { return PApplet.second(); }
    public static final int minute() { return PApplet.minute(); }
    public static final int hour() { return PApplet.hour(); }
    public static final int day() { return PApplet.day(); }
    public static final int month() { return PApplet.month(); }
    public static final int year() { return PApplet.year(); }

    //////////////////////////////////////////////////////////////
    // printing
    public static final void print(byte what) { PApplet.print(what); }
    public static final void print(boolean what) { PApplet.print(what); }
    public static final void print(char what) { PApplet.print(what); }
    public static final void print(int what) { PApplet.print(what); }
    public static final void print(long what) { PApplet.print(what); }
    public static final void print(float what) { PApplet.print(what); }
    public static final void print(double what) { PApplet.print(what); }
    public static final void print(String what) { PApplet.print(what); }
    public static final void print(Object... variables) { PApplet.print(variables); }
    public static final void println() { PApplet.println(); }
    public static final void println(byte what) { PApplet.println(what); }
    public static final void println(boolean what) { PApplet.println(what); }
    public static final void println(char what) { PApplet.println(what); }
    public static final void println(int what) { PApplet.println(what); }
    public static final void println(long what) { PApplet.println(what); }
    public static final void println(float what) { PApplet.println(what); }
    public static final void println(double what) { PApplet.println(what); }
    public static final void println(String what) { PApplet.println(what); }
    public static final void println(Object... variables) { PApplet.println(variables); }
    public static final void println(Object what) { PApplet.println(what); }
    public static final void printArray(Object what) { PApplet.println(what); }
    public static final void debug(String msg) { PApplet.debug(msg); }

    //////////////////////////////////////////////////////////////
    // MATH
    public static final float abs(float n) { return PApplet.abs(n); }
    public static final int abs(int n) { return PApplet.abs(n); }
    public static final float sq(float n) { return PApplet.sq(n); }
    public static final float sqrt(float n) { return PApplet.sqrt(n); }
    public static final float log(float n) { return PApplet.log(n); }
    public static final float exp(float n) { return PApplet.exp(n); }
    public static final float pow(float n, float e) { return PApplet.pow(n, e); }
    public static final int max(int a, int b) { return PApplet.max(a, b); }
    public static final float max(float a, float b) { return PApplet.max(a, b); }
    public static final int max(int a, int b, int c) { return PApplet.max(a, b, c); }
    public static final float max(float a, float b, float c) { return PApplet.max(a, b, c); }
    public static final int max(int[] list) { return PApplet.max(list); }
    public static final float max(float[] list) { return PApplet.max(list); }
    public static final int min(int a, int b) { return PApplet.min(a, b); }
    public static final float min(float a, float b) { return PApplet.min(a, b); }
    public static final int min(int a, int b, int c) { return PApplet.min(a, b, c); }
    public static final float min(float a, float b, float c) { return PApplet.min(a, b, c); }
    public static final int min(int[] list) { return PApplet.min(list); }
    public static final float min(float[] list) { return PApplet.min(list); }
    public static final int constrain(int amt, int low, int high) { return PApplet.constrain(amt, low, high); }
    public static final float constrain(float amt, float low, float high) { return PApplet.constrain(amt, low, high); }
    public static final float sin(float angle) { return PApplet.sin(angle); }
    public static final float cos(float angle) { return PApplet.cos(angle); }
    public static final float tan(float angle) { return PApplet.tan(angle); }
    public static final float asin(float value) { return PApplet.asin(value); }
    public static final float acos(float value) { return PApplet.acos(value); }
    public static final float atan(float value) { return PApplet.atan(value); }
    public static final float atan2(float y, float x) { return PApplet.atan2(y, x); }
    public static final float degrees(float radians) { return PApplet.degrees(radians); }
    public static final float radians(float degrees) { return PApplet.radians(degrees); }
    public static final int ceil(float n) { return PApplet.ceil(n); }
    public static final int floor(float n) { return PApplet.floor(n); }
    public static final int round(float n) { return PApplet.round(n); }
    public static final float mag(float a, float b) { return PApplet.mag(a, b); }
    public static final float mag(float a, float b, float c) { return PApplet.mag(a, b, c); }
    public static final float dist(float x1, float y1, float x2, float y2) { return PApplet.dist(x1, y1, x2, y2); }
    public static final float dist(float x1, float y1, float z1,
                                                                 float x2, float y2, float z2) { return PApplet.dist(x1, y1, z1, x2, y2, z2); }
    public static final float lerp(float start, float stop, float amt) { return PApplet.lerp(start, stop, amt); }
    public static final float norm(float value, float start, float stop) { return PApplet.norm(value, start, stop); }
    public static final float map(float value,
                                                                float start1, float stop1,
                                                                float start2, float stop2) { return PApplet.map(value, start1, stop1, start2, stop2); }

    //////////////////////////////////////////////////////////////
    // SORT
    public static final byte[] sort(byte list[]) { return PApplet.sort(list); }
    public static final byte[] sort(byte[] list, int count) { return PApplet.sort(list, count); }
    public static final char[] sort(char list[]) { return PApplet.sort(list); }
    public static final char[] sort(char[] list, int count) { return PApplet.sort(list, count); }
    public static final int[] sort(int list[]) { return PApplet.sort(list); }
    public static final int[] sort(int[] list, int count) { return PApplet.sort(list, count); }
    public static final float[] sort(float list[]) { return PApplet.sort(list); }
    public static final float[] sort(float[] list, int count) { return PApplet.sort(list, count); }
    public static final String[] sort(String list[]) { return PApplet.sort(list); }
    public static final String[] sort(String[] list, int count) { return PApplet.sort(list, count); }

    //////////////////////////////////////////////////////////////
    // ARRAY UTILITIES
    public static final void arrayCopy(Object src, int srcPosition,
                                                             Object dst, int dstPosition,
                                                             int length) { PApplet.arrayCopy(src, srcPosition, dst, dstPosition, length); }
    public static final void arrayCopy(Object src, Object dst, int length) { PApplet.arrayCopy(src, dst, length); }
    public static final void arrayCopy(Object src, Object dst) { PApplet.arrayCopy(src, dst); }
    public static final boolean[] expand(boolean list[]) { return PApplet.expand(list); }
    public static final boolean[] expand(boolean list[], int newSize) { return PApplet.expand(list, newSize); }
    public static final byte[] expand(byte list[]) { return PApplet.expand(list); }
    public static final byte[] expand(byte list[], int newSize) { return PApplet.expand(list, newSize); }
    public static final char[] expand(char list[]) { return PApplet.expand(list); }
    public static final char[] expand(char list[], int newSize) { return PApplet.expand(list, newSize); }
    public static final int[] expand(int list[]) { return PApplet.expand(list); }
    public static final int[] expand(int list[], int newSize) { return PApplet.expand(list, newSize); }
    public static final long[] expand(long list[]) { return PApplet.expand(list); }
    public static final long[] expand(long list[], int newSize) { return PApplet.expand(list, newSize); }
    public static final float[] expand(float list[]) { return PApplet.expand(list); }
    public static final float[] expand(float list[], int newSize) { return PApplet.expand(list, newSize); }
    public static final double[] expand(double list[]) { return PApplet.expand(list); }
    public static final double[] expand(double list[], int newSize) { return PApplet.expand(list, newSize); }
    public static final String[] expand(String list[]) { return PApplet.expand(list); }
    public static final String[] expand(String list[], int newSize) { return PApplet.expand(list, newSize); }
    public static final Object expand(Object array) { return PApplet.expand(array); }
    public static final Object expand(Object list, int newSize) { return PApplet.expand(list, newSize); }
    public static final byte[] append(byte array[], byte value) { return PApplet.append(array, value); }
    public static final char[] append(char array[], char value) { return PApplet.append(array, value); }
    public static final int[] append(int array[], int value) { return PApplet.append(array, value); }
    public static final float[] append(float array[], float value) { return PApplet.append(array, value); }
    public static final String[] append(String array[], String value) { return PApplet.append(array, value); }
    public static final Object append(Object array, Object value) { return PApplet.append(array, value); }
    public static final boolean[] shorten(boolean list[]) { return PApplet.shorten(list); }
    public static final byte[] shorten(byte list[]) { return PApplet.shorten(list); }
    public static final char[] shorten(char list[]) { return PApplet.shorten(list); }
    public static final int[] shorten(int list[]) { return PApplet.shorten(list); }
    public static final float[] shorten(float list[]) { return PApplet.shorten(list); }
    public static final String[] shorten(String list[]) { return PApplet.shorten(list); }
    public static final Object shorten(Object list) { return PApplet.shorten(list); }
    public static final boolean[] splice(boolean list[],
                                                                             boolean value, int index) { return PApplet.splice(list, value, index); }
    public static final boolean[] splice(boolean list[],
                                                                             boolean value[], int index) { return PApplet.splice(list, value, index); }
    public static final byte[] splice(byte list[],
                                                                        byte value, int index) { return PApplet.splice(list, value, index); }
    public static final byte[] splice(byte list[],
                                                                        byte value[], int index) { return PApplet.splice(list, value, index); }
    public static final char[] splice(char list[],
                                                                        char value, int index) { return PApplet.splice(list, value, index); }
    public static final char[] splice(char list[],
                                                                        char value[], int index) { return PApplet.splice(list, value, index); }
    public static final int[] splice(int list[],
                                                                     int value, int index) { return PApplet.splice(list, value, index); }
    public static final int[] splice(int list[],
                                                                     int value[], int index) { return PApplet.splice(list, value, index); }
    public static final float[] splice(float list[],
                                                                         float value, int index) { return PApplet.splice(list, value, index); }
    public static final float[] splice(float list[],
                                                                         float value[], int index) { return PApplet.splice(list, value, index); }
    public static final String[] splice(String list[],
                                                                            String value, int index) { return PApplet.splice(list, value, index); }
    public static final String[] splice(String list[],
                                                                            String value[], int index) { return PApplet.splice(list, value, index); }
    public static final Object splice(Object list, Object value, int index) { return PApplet.splice(list, value, index); }
    public static final boolean[] subset(boolean list[], int start) { return PApplet.subset(list, start); }
    public static final boolean[] subset(boolean list[], int start, int count) { return PApplet.subset(list, start, count); }
    public static final byte[] subset(byte list[], int start) { return PApplet.subset(list, start); }
    public static final byte[] subset(byte list[], int start, int count) { return PApplet.subset(list, start, count); }
    public static final char[] subset(char list[], int start) { return PApplet.subset(list, start); }
    public static final char[] subset(char list[], int start, int count) { return PApplet.subset(list, start, count); }
    public static final int[] subset(int list[], int start) { return PApplet.subset(list, start); }
    public static final int[] subset(int list[], int start, int count) { return PApplet.subset(list, start, count); }
    public static final float[] subset(float list[], int start) { return PApplet.subset(list, start); }
    public static final float[] subset(float list[], int start, int count) { return PApplet.subset(list, start, count); }
    public static final String[] subset(String list[], int start) { return PApplet.subset(list, start); }
    public static final String[] subset(String list[], int start, int count) { return PApplet.subset(list, start, count); }
    public static final Object subset(Object list, int start) { return PApplet.subset(list, start); }
    public static final Object subset(Object list, int start, int count) { return PApplet.subset(list, start, count); }
    public static final boolean[] concat(boolean a[], boolean b[]) { return PApplet.concat(a, b); }
    public static final byte[] concat(byte a[], byte b[]) { return PApplet.concat(a, b); }
    public static final char[] concat(char a[], char b[]) { return PApplet.concat(a, b); }
    public static final int[] concat(int a[], int b[]) { return PApplet.concat(a, b); }
    public static final float[] concat(float a[], float b[]) { return PApplet.concat(a, b); }
    public static final String[] concat(String a[], String b[]) { return PApplet.concat(a, b); }
    public static final Object concat(Object a, Object b) { return PApplet.concat(a, b); }
    public static final boolean[] reverse(boolean list[]) { return PApplet.reverse(list); }
    public static final byte[] reverse(byte list[]) { return PApplet.reverse(list); }
    public static final char[] reverse(char list[]) { return PApplet.reverse(list); }
    public static final int[] reverse(int list[]) { return PApplet.reverse(list); }
    public static final float[] reverse(float list[]) { return PApplet.reverse(list); }
    public static final String[] reverse(String list[]) { return PApplet.reverse(list); }
    public static final Object reverse(Object list) { return PApplet.reverse(list); }

    //////////////////////////////////////////////////////////////
    // STRINGS
    public static final String trim(String str) { return PApplet.trim(str); }
    public static final String[] trim(String[] array) { return PApplet.trim(array); }
    public static final String join(String[] list, char separator) { return PApplet.join(list, separator); }
    public static final String join(String[] list, String separator) { return PApplet.join(list, separator); }
    public static final String[] splitTokens(String value) { return PApplet.splitTokens(value); }
    public static final String[] splitTokens(String value, String delim) { return PApplet.splitTokens(value, delim); }
    public static final String[] split(String value, char delim) { return PApplet.split(value, delim); }
    public static final String[] split(String value, String delim) { return PApplet.split(value, delim); }
    public static final String[] match(String str, String regexp) { return PApplet.match(str, regexp); }
    public static final String[][] matchAll(String str, String regexp) { return PApplet.matchAll(str, regexp); }

    //////////////////////////////////////////////////////////////
    // CASTING FUNCTIONS
    public static final boolean parseBoolean(int what) { return PApplet.parseBoolean(what); }
    public static final boolean parseBoolean(String what) { return PApplet.parseBoolean(what); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final boolean[] parseBoolean(int what[]) { return PApplet.parseBoolean(what); }
    public static final boolean[] parseBoolean(String what[]) { return PApplet.parseBoolean(what); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final byte parseByte(boolean what) { return PApplet.parseByte(what); }
    public static final byte parseByte(char what) { return PApplet.parseByte(what); }
    public static final byte parseByte(int what) { return PApplet.parseByte(what); }
    public static final byte parseByte(float what) { return PApplet.parseByte(what); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final byte[] parseByte(boolean what[]) { return PApplet.parseByte(what); }
    public static final byte[] parseByte(char what[]) { return PApplet.parseByte(what); }
    public static final byte[] parseByte(int what[]) { return PApplet.parseByte(what); }
    public static final byte[] parseByte(float what[]) { return PApplet.parseByte(what); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final char parseChar(byte what) { return PApplet.parseChar(what); }
    public static final char parseChar(int what) { return PApplet.parseChar(what); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final char[] parseChar(byte what[]) { return PApplet.parseChar(what); }
    public static final char[] parseChar(int what[]) { return PApplet.parseChar(what); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final int parseInt(boolean what) { return PApplet.parseInt(what); }
    public static final int parseInt(byte what) { return PApplet.parseInt(what); }
    public static final int parseInt(char what) { return PApplet.parseInt(what); }
    public static final int parseInt(float what) { return PApplet.parseInt(what); }
    public static final int parseInt(String what) { return PApplet.parseInt(what); }
    public static final int parseInt(String what, int otherwise) { return PApplet.parseInt(what, otherwise); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final int[] parseInt(boolean what[]) { return PApplet.parseInt(what); }
    public static final int[] parseInt(byte what[]) { return PApplet.parseInt(what); }
    public static final int[] parseInt(char what[]) { return PApplet.parseInt(what); }
    public static final int[] parseInt(float what[]) { return PApplet.parseInt(what); }
    public static final int[] parseInt(String what[]) { return PApplet.parseInt(what); }
    public static final int[] parseInt(String what[], int missing) { return PApplet.parseInt(what, missing); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final float parseFloat(int what) { return PApplet.parseFloat(what); }
    public static final float parseFloat(String what) { return PApplet.parseFloat(what); }
    public static final float parseFloat(String what, float otherwise) { return PApplet.parseFloat(what, otherwise); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final float[] parseFloat(byte what[]) { return PApplet.parseFloat(what); }
    public static final float[] parseFloat(int what[]) { return PApplet.parseFloat(what); }
    public static final float[] parseFloat(String what[]) { return PApplet.parseFloat(what); }
    public static final float[] parseFloat(String what[], float missing) { return PApplet.parseFloat(what, missing); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final String str(boolean x) { return PApplet.str(x); }
    public static final String str(byte x) { return PApplet.str(x); }
    public static final String str(char x) { return PApplet.str(x); }
    public static final String str(int x) { return PApplet.str(x); }
    public static final String str(float x) { return PApplet.str(x); }
    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    public static final String[] str(boolean x[]) { return PApplet.str(x); }
    public static final String[] str(byte x[]) { return PApplet.str(x); }
    public static final String[] str(char x[]) { return PApplet.str(x); }
    public static final String[] str(float x[]) { return PApplet.str(x); }

    //////////////////////////////////////////////////////////////
    // INT NUMBER FORMATTING
    public static final String nf(float num) { return PApplet.nf(num); }
    public static final String[] nf(float[] num) { return PApplet.nf(num); }
    public static final String[] nf(int num[], int digits) { return PApplet.nf(num, digits); }
    public static final String nf(int num, int digits) { return PApplet.nf(num, digits); }
    public static final String[] nfc(int num[]) { return PApplet.nfc(num); }
    public static final String nfc(int num) { return PApplet.nfc(num); }
    public static final String nfs(int num, int digits) { return PApplet.nfs(num, digits); }
    public static final String[] nfs(int num[], int digits) { return PApplet.nfs(num, digits); }
    public static final String nfp(int num, int digits) { return PApplet.nfp(num, digits); }
    public static final String[] nfp(int num[], int digits) { return PApplet.nfp(num, digits); }

    //////////////////////////////////////////////////////////////
    // FLOAT NUMBER FORMATTING
    public static final String[] nf(float num[], int left, int right) { return PApplet.nf(num, left, right); }
    public static final String nf(float num, int left, int right) { return PApplet.nf(num, left, right); }
    public static final String[] nfc(float num[], int right) { return PApplet.nfc(num, right); }
    public static final String nfc(float num, int right) { return PApplet.nfc(num, right); }
    public static final String[] nfs(float num[], int left, int right) { return PApplet.nfs(num, left, right); }
    public static final String nfs(float num, int left, int right) { return PApplet.nfs(num, left, right); }
    public static final String[] nfp(float num[], int left, int right) { return PApplet.nfp(num, left, right); }
    public static final String nfp(float num, int left, int right) { return PApplet.nfp(num, left, right); }

    //////////////////////////////////////////////////////////////
    // HEX/BINARY CONVERSION
    public static final String hex(byte value) { return PApplet.hex(value); }
    public static final String hex(char value) { return PApplet.hex(value); }
    public static final String hex(int value) { return PApplet.hex(value); }
    public static final String hex(int value, int digits) { return PApplet.hex(value, digits); }
    public static final int unhex(String value) { return PApplet.unhex(value); }
    public static final String binary(byte value) { return PApplet.binary(value); }
    public static final String binary(char value) { return PApplet.binary(value); }
    public static final String binary(int value) { return PApplet.binary(value); }
    public static final String binary(int value, int digits) { return PApplet.binary(value, digits); }
    public static final int unbinary(String value) { return PApplet.unbinary(value); }

    //////////////////////////////////////////////////////////////
    // COLOR FUNCTIONS
    public static final int blendColor(int c1, int c2, int mode) { return PApplet.blendColor(c1, c2, mode); }
    public static final int lerpColor(int c1, int c2, float amt, int mode) { return PApplet.lerpColor(c1, c2, amt, mode); }

    // Processing stuff that needs to be converted to static

    static private final long millisOffset = System.currentTimeMillis();

    static String sketchPath;

    static public int millis() {
        return (int) (System.currentTimeMillis() - millisOffset);
    }

    public static Table loadTable(String filename, String options) {
        try {
            String optionStr = Table.extensionOptions(true, filename, options);
            String[] optionList = PApplet.trim(PApplet.split(optionStr, ','));

            Table dictionary = null;
            for (String opt : optionList) {
                if (opt.startsWith("dictionary=")) {
                    dictionary = loadTable(opt.substring(opt.indexOf('=') + 1), "tsv");
                    return dictionary.typedParse(createInput(filename), optionStr);
                }
            }
            InputStream input = createInput(filename);
            if (input == null) {
                System.err.println(filename + " does not exist or could not be read");
                return null;
            }
            return new Table(input, optionStr);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //////////////////////////////////////////////////////////////
    // FILE INPUT
    public static InputStream createInput(String filename) {
        InputStream input = createInputRaw(filename);
        final String lower = filename.toLowerCase();
        if ((input != null) &&
                (lower.endsWith(".gz") || lower.endsWith(".svgz"))) {
            try {
                return new GZIPInputStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return input;
    }

    public static InputStream createInputRaw(String filename) {
        if (filename == null) return null;

        if (sketchPath == null) {
            System.err.println("The sketch path is not set.");
            throw new RuntimeException("Files must be loaded inside setup() or after it has been called.");
        }

        if (filename.length() == 0) {
            // an error will be called by the parent function
            //System.err.println("The filename passed to openStream() was empty.");
            return null;
        }

        // First check whether this looks like a URL. This will prevent online
        // access logs from being spammed with GET /sketchfolder/http://blahblah
        if (filename.contains(":")) {  // at least smells like URL
            try {
                URL url = new URL(filename);
                URLConnection conn = url.openConnection();
                if (conn instanceof HttpURLConnection) {
                    HttpURLConnection httpConn = (HttpURLConnection) conn;
                    // Will not handle a protocol change (see below)
                    httpConn.setInstanceFollowRedirects(true);
                    int response = httpConn.getResponseCode();
                    // Normally will not follow HTTPS redirects from HTTP due to security concerns
                    // http://stackoverflow.com/questions/1884230/java-doesnt-follow-redirect-in-urlconnection/1884427
                    if (response >= 300 && response < 400) {
                        String newLocation = httpConn.getHeaderField("Location");
                        return createInputRaw(newLocation);
                    }
                    return conn.getInputStream();
                } else if (conn instanceof JarURLConnection) {
                    return url.openStream();
                }
            } catch (MalformedURLException mfue) {
                // not a url, that's fine

            } catch (FileNotFoundException fnfe) {
                // Added in 0119 b/c Java 1.5 throws FNFE when URL not available.
                // http://dev.processing.org/bugs/show_bug.cgi?id=403

            } catch (IOException e) {
                // changed for 0117, shouldn't be throwing exception
                e.printStackTrace();
                //System.err.println("Error downloading from URL " + filename);
                return null;
                //throw new RuntimeException("Error downloading from URL " + filename);
            }
        }

        InputStream stream = null;

        // Moved this earlier than the getResourceAsStream() checks, because
        // calling getResourceAsStream() on a directory lists its contents.
        // http://dev.processing.org/bugs/show_bug.cgi?id=716
        try {
            // First see if it's in a data folder. This may fail by throwing
            // a SecurityException. If so, this whole block will be skipped.
            File file = new File(dataPath(filename));
            if (!file.exists()) {
                // next see if it's just in the sketch folder
                file = sketchFile(filename);
            }

            if (file.isDirectory()) {
                return null;
            }
            if (file.exists()) {
                try {
                    // handle case sensitivity check
                    String filePath = file.getCanonicalPath();
                    String filenameActual = new File(filePath).getName();
                    // make sure there isn't a subfolder prepended to the name
                    String filenameShort = new File(filename).getName();
                    // if the actual filename is the same, but capitalized
                    // differently, warn the user.
                    //if (filenameActual.equalsIgnoreCase(filenameShort) &&
                    //!filenameActual.equals(filenameShort)) {
                    if (!filenameActual.equals(filenameShort)) {
                        throw new RuntimeException("This file is named " +
                                                                             filenameActual + " not " +
                                                                             filename + ". Rename the file " +
                                                                             "or change your code.");
                    }
                } catch (IOException e) { }
            }

            // if this file is ok, may as well just load it
            stream = new FileInputStream(file);
            if (stream != null) return stream;

            // have to break these out because a general Exception might
            // catch the RuntimeException being thrown above
        } catch (IOException ioe) {
        } catch (SecurityException se) { }

        // Using getClassLoader() prevents java from converting dots
        // to slashes or requiring a slash at the beginning.
        // (a slash as a prefix means that it'll load from the root of
        // the jar, rather than trying to dig into the package location)
        ClassLoader cl = Utils.class.getClassLoader();

        // by default, data files are exported to the root path of the jar.
        // (not the data folder) so check there first.
        stream = cl.getResourceAsStream("data/" + filename);
        if (stream != null) {
            String cn = stream.getClass().getName();
            // this is an irritation of sun's java plug-in, which will return
            // a non-null stream for an object that doesn't exist. like all good
            // things, this is probably introduced in java 1.5. awesome!
            // http://dev.processing.org/bugs/show_bug.cgi?id=359
            if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
                return stream;
            }
        }

        // When used with an online script, also need to check without the
        // data folder, in case it's not in a subfolder called 'data'.
        // http://dev.processing.org/bugs/show_bug.cgi?id=389
        stream = cl.getResourceAsStream(filename);
        if (stream != null) {
            String cn = stream.getClass().getName();
            if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
                return stream;
            }
        }

        try {
            // attempt to load from a local file, used when running as
            // an application, or as a signed applet
            try {  // first try to catch any security exceptions
                try {
                    stream = new FileInputStream(dataPath(filename));
                    if (stream != null) return stream;
                } catch (IOException e2) { }

                try {
                    stream = new FileInputStream(sketchPath(filename));
                    if (stream != null) return stream;
                } catch (Exception e) { }  // ignored

                try {
                    stream = new FileInputStream(filename);
                    if (stream != null) return stream;
                } catch (IOException e1) { }

            } catch (SecurityException se) { }  // online, whups

        } catch (Exception e) {
            //die(e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    //////////////////////////////////////////////////////////////

    public static String sketchPath() {
        return sketchPath;
    }

    public static String sketchPath(String where) {
        if (sketchPath() == null) {
            return where;
        }
        // isAbsolute() could throw an access exception, but so will writing
        // to the local disk using the sketch path, so this is safe here.
        // for 0120, added a try/catch anyways.
        try {
            if (new File(where).isAbsolute()) return where;
        } catch (Exception e) { }

        return sketchPath() + File.separator + where;
    }

    public static File sketchFile(String where) {
        return new File(sketchPath(where));
    }

    public static String savePath(String where) {
        if (where == null) return null;
        String filename = sketchPath(where);
        PApplet.createPath(filename);
        return filename;
    }

    public static File saveFile(String where) {
        return new File(savePath(where));
    }

    public static String dataPath(String where) {
        return dataFile(where).getAbsolutePath();
    }

    public static File dataFile(String where) {
        // isAbsolute() could throw an access exception, but so will writing
        // to the local disk using the sketch path, so this is safe here.
        File why = new File(where);
        if (why.isAbsolute()) return why;

        URL jarURL = Utils.class.getProtectionDomain().getCodeSource().getLocation();
        // Decode URL
        String jarPath;
        try {
            jarPath = jarURL.toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        if (jarPath.contains("Contents/Java/")) {
            File containingFolder = new File(jarPath).getParentFile();
            File dataFolder = new File(containingFolder, "data");
            return new File(dataFolder, where);
        }
        // Windows, Linux, or when not using a Mac OS X .app file
        File workingDirItem =
            new File(sketchPath + File.separator + "data" + File.separator + where);
//    if (workingDirItem.exists()) {
        return workingDirItem;
//    }
//    // In some cases, the current working directory won't be set properly.
    }


    //////////////////////////////////////////////////////////////
    // RANDOM NUMBERS

    static Random internalRandom;

    public static final float random(float high) {
        // avoid an infinite loop when 0 or NaN are passed in
        if (high == 0 || high != high) {
            return 0;
        }

        if (internalRandom == null) {
            internalRandom = new Random();
        }

        // for some reason (rounding error?) Math.random() * 3
        // can sometimes return '3' (once in ~30 million tries)
        // so a check was added to avoid the inclusion of 'howbig'
        float value = 0;
        do {
            value = internalRandom.nextFloat() * high;
        } while (value == high);
        return value;
    }

    public static final float randomGaussian() {
        if (internalRandom == null) {
            internalRandom = new Random();
        }
        return (float) internalRandom.nextGaussian();
    }

    public static final float random(float low, float high) {
        if (low >= high) return low;
        float diff = high - low;
        float value = 0;
        // because of rounding error, can't just add low, otherwise it may hit high
        // https://github.com/processing/processing/issues/4551
        do {
            value = random(diff) + low;
        } while (value == high);
        return value;
    }

    public static final void randomSeed(long seed) {
        if (internalRandom == null) {
            internalRandom = new Random();
        }
        internalRandom.setSeed(seed);
    }

    static final protected float sinLUT[];
    static final protected float cosLUT[];
    static final protected float SINCOS_PRECISION = 0.5f;
    static final protected int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);
    static {
        sinLUT = new float[SINCOS_LENGTH];
        cosLUT = new float[SINCOS_LENGTH];
        for (int i = 0; i < SINCOS_LENGTH; i++) {
            sinLUT[i] = (float) Math.sin(i * PConstants.DEG_TO_RAD * SINCOS_PRECISION);
            cosLUT[i] = (float) Math.cos(i * PConstants.DEG_TO_RAD * SINCOS_PRECISION);
        }
    }

    //////////////////////////////////////////////////////////////
    // PERLIN NOISE
    static final int PERLIN_YWRAPB = 4;
    static final int PERLIN_YWRAP = 1<<PERLIN_YWRAPB;
    static final int PERLIN_ZWRAPB = 8;
    static final int PERLIN_ZWRAP = 1<<PERLIN_ZWRAPB;
    static final int PERLIN_SIZE = 4095;

    static int perlin_octaves = 4; // default to medium smooth
    static float perlin_amp_falloff = 0.5f; // 50% reduction/octave

    // [toxi 031112]
    // new vars needed due to recent change of cos table in PGraphics
    static int perlin_TWOPI, perlin_PI;
    static float[] perlin_cosTable;
    static float[] perlin;

    static Random perlinRandom;

    public static float noise(float x) {
        // is this legit? it's a dumb way to do it (but repair it later)
        return noise(x, 0f, 0f);
    }

    public static float noise(float x, float y) {
        return noise(x, y, 0f);
    }

    public static float noise(float x, float y, float z) {
        if (perlin == null) {
            if (perlinRandom == null) {
                perlinRandom = new Random();
            }
            perlin = new float[PERLIN_SIZE + 1];
            for (int i = 0; i < PERLIN_SIZE + 1; i++) {
                perlin[i] = perlinRandom.nextFloat(); //(float)Math.random();
            }
            // [toxi 031112]
            // noise broke due to recent change of cos table in PGraphics
            // this will take care of it
            perlin_cosTable = cosLUT;
            perlin_TWOPI = perlin_PI = SINCOS_LENGTH;
            perlin_PI >>= 1;
        }

        if (x<0) x=-x;
        if (y<0) y=-y;
        if (z<0) z=-z;

        int xi=(int)x, yi=(int)y, zi=(int)z;
        float xf = x - xi;
        float yf = y - yi;
        float zf = z - zi;
        float rxf, ryf;

        float r=0;
        float ampl=0.5f;

        float n1,n2,n3;

        for (int i=0; i<perlin_octaves; i++) {
            int of=xi+(yi<<PERLIN_YWRAPB)+(zi<<PERLIN_ZWRAPB);

            rxf=noise_fsc(xf);
            ryf=noise_fsc(yf);

            n1  = perlin[of&PERLIN_SIZE];
            n1 += rxf*(perlin[(of+1)&PERLIN_SIZE]-n1);
            n2  = perlin[(of+PERLIN_YWRAP)&PERLIN_SIZE];
            n2 += rxf*(perlin[(of+PERLIN_YWRAP+1)&PERLIN_SIZE]-n2);
            n1 += ryf*(n2-n1);

            of += PERLIN_ZWRAP;
            n2  = perlin[of&PERLIN_SIZE];
            n2 += rxf*(perlin[(of+1)&PERLIN_SIZE]-n2);
            n3  = perlin[(of+PERLIN_YWRAP)&PERLIN_SIZE];
            n3 += rxf*(perlin[(of+PERLIN_YWRAP+1)&PERLIN_SIZE]-n3);
            n2 += ryf*(n3-n2);

            n1 += noise_fsc(zf)*(n2-n1);

            r += n1*ampl;
            ampl *= perlin_amp_falloff;
            xi<<=1; xf*=2;
            yi<<=1; yf*=2;
            zi<<=1; zf*=2;

            if (xf>=1.0f) { xi++; xf--; }
            if (yf>=1.0f) { yi++; yf--; }
            if (zf>=1.0f) { zi++; zf--; }
        }
        return r;
    }

    private static float noise_fsc(float i) {
        // using bagel's cosine table instead
        return 0.5f*(1.0f-perlin_cosTable[(int)(i*perlin_PI)%perlin_TWOPI]);
    }

    public static void noiseDetail(int lod) {
        if (lod>0) perlin_octaves=lod;
    }

    public static void noiseDetail(int lod, float falloff) {
        if (lod>0) perlin_octaves=lod;
        if (falloff>0) perlin_amp_falloff=falloff;
    }

    public static void noiseSeed(long seed) {
        if (perlinRandom == null) perlinRandom = new Random();
        perlinRandom.setSeed(seed);
        // force table reset after changing the random number seed [0122]
        perlin = null;
    }



    //////////////////////////////////////////////////////////////
    // SPLINE UTILITY FUNCTIONS (used by both Bezier and Catmull-Rom)
    protected static void splineForward(int segments, PMatrix3D matrix) {
        float f  = 1.0f / segments;
        float ff = f * f;
        float fff = ff * f;

        matrix.set(0,     0,    0, 1,
                             fff,   ff,   f, 0,
                             6*fff, 2*ff, 0, 0,
                             6*fff, 0,    0, 0);
    }



    //////////////////////////////////////////////////////////////
    // BEZIER

    protected static boolean bezierInited = false;
    public static int bezierDetail = 20;

    // used by both curve and bezier, so just init here
    protected static PMatrix3D bezierBasisMatrix =
        new PMatrix3D(-1,  3, -3,  1,
                                     3, -6,  3,  0,
                                    -3,  3,  0,  0,
                                     1,  0,  0,  0);

    //protected PMatrix3D bezierForwardMatrix;
    protected static PMatrix3D bezierDrawMatrix;

    public static float bezierPoint(float a, float b, float c, float d, float t) {
        float t1 = 1.0f - t;
        return a*t1*t1*t1 + 3*b*t*t1*t1 + 3*c*t*t*t1 + d*t*t*t;
    }

    public static float bezierTangent(float a, float b, float c, float d, float t) {
        return (3*t*t * (-a+3*b-3*c+d) +
                        6*t * (a-2*b+c) +
                        3 * (-a+b));
    }

    protected static void bezierInitCheck() {
        if (!bezierInited) {
            bezierInit();
        }
    }

    protected static void bezierInit() {
        // overkill to be broken out, but better parity with the curve stuff below
        bezierDetail(bezierDetail);
        bezierInited = true;
    }

    public static void bezierDetail(int detail) {
        bezierDetail = detail;

        if (bezierDrawMatrix == null) {
            bezierDrawMatrix = new PMatrix3D();
        }

        // setup matrix for forward differencing to speed up drawing
        splineForward(detail, bezierDrawMatrix);

        // multiply the basis and forward diff matrices together
        // saves much time since this needn't be done for each curve
        //mult_spline_matrix(bezierForwardMatrix, bezier_basis, bezierDrawMatrix, 4);
        //bezierDrawMatrix.set(bezierForwardMatrix);
        bezierDrawMatrix.apply(bezierBasisMatrix);
    }

    //////////////////////////////////////////////////////////////
    // CATMULL-ROM CURVE

    protected static boolean curveInited = false;
    public static int curveDetail = 20;
    public static float curveTightness = 0;
    // catmull-rom basis matrix, perhaps with optional s parameter
    protected static PMatrix3D curveBasisMatrix;
    protected static PMatrix3D curveDrawMatrix;

    protected static PMatrix3D bezierBasisInverse;
    protected static PMatrix3D curveToBezierMatrix;

    public static float curvePoint(float a, float b, float c, float d, float t) {
        curveInitCheck();

        float tt = t * t;
        float ttt = t * tt;
        PMatrix3D cb = curveBasisMatrix;

        // not optimized (and probably need not be)
        return (a * (ttt*cb.m00 + tt*cb.m10 + t*cb.m20 + cb.m30) +
                        b * (ttt*cb.m01 + tt*cb.m11 + t*cb.m21 + cb.m31) +
                        c * (ttt*cb.m02 + tt*cb.m12 + t*cb.m22 + cb.m32) +
                        d * (ttt*cb.m03 + tt*cb.m13 + t*cb.m23 + cb.m33));
    }

    public static float curveTangent(float a, float b, float c, float d, float t) {
        curveInitCheck();

        float tt3 = t * t * 3;
        float t2 = t * 2;
        PMatrix3D cb = curveBasisMatrix;

        // not optimized (and probably need not be)
        return (a * (tt3*cb.m00 + t2*cb.m10 + cb.m20) +
                        b * (tt3*cb.m01 + t2*cb.m11 + cb.m21) +
                        c * (tt3*cb.m02 + t2*cb.m12 + cb.m22) +
                        d * (tt3*cb.m03 + t2*cb.m13 + cb.m23) );
    }

    public static void curveDetail(int detail) {
        curveDetail = detail;
        curveInit();
    }

    public static void curveTightness(float tightness) {
        curveTightness = tightness;
        curveInit();
    }

    protected static void curveInitCheck() {
        if (!curveInited) {
            curveInit();
        }
    }

    protected static void curveInit() {
        // allocate only if/when used to save startup time
        if (curveDrawMatrix == null) {
            curveBasisMatrix = new PMatrix3D();
            curveDrawMatrix = new PMatrix3D();
            curveInited = true;
        }

        float s = curveTightness;
        curveBasisMatrix.set((s-1)/2f, (s+3)/2f,  (-3-s)/2f, (1-s)/2f,
                                                 (1-s),    (-5-s)/2f, (s+2),     (s-1)/2f,
                                                 (s-1)/2f, 0,         (1-s)/2f,  0,
                                                 0,        1,         0,         0);

        //setup_spline_forward(segments, curveForwardMatrix);
        splineForward(curveDetail, curveDrawMatrix);

        if (bezierBasisInverse == null) {
            bezierBasisInverse = bezierBasisMatrix.get();
            bezierBasisInverse.invert();
            curveToBezierMatrix = new PMatrix3D();
        }

        // TODO only needed for PGraphicsJava2D? if so, move it there
        // actually, it's generally useful for other renderers, so keep it
        // or hide the implementation elsewhere.
        curveToBezierMatrix.set(curveBasisMatrix);
        curveToBezierMatrix.preApply(bezierBasisInverse);

        // multiply the basis and forward diff matrices together
        // saves much time since this needn't be done for each curve
        curveDrawMatrix.apply(curveBasisMatrix);
    }

}
// UI3dComponent getUIVenue() {
//   switch (environment) {
//   case SATELLITE: return new UISatellite();
//   case MIDWAY: return new UIMidway();
//   }
//   return null;
// }

// abstract class UIVenue extends UI3dComponent {

//   final static float LOGO_SIZE = 100*INCHES;
//   final PImage LOGO = loadImage("envelop-logo-clear.png");

//   @Override
//   public void onDraw(UI ui, PGraphics pg) {
//     pg.stroke(#000000);
//     pg.fill(#202020);
//     drawFloor(ui, pg);
        
//     // Logo
//     pg.noFill();
//     pg.noStroke();
//     pg.beginShape();
//     pg.texture(LOGO);
//     pg.textureMode(NORMAL);
//     pg.vertex(-LOGO_SIZE, .1, -LOGO_SIZE, 0, 1);
//     pg.vertex(LOGO_SIZE, .1, -LOGO_SIZE, 1, 1);
//     pg.vertex(LOGO_SIZE, .1, LOGO_SIZE, 1, 0);
//     pg.vertex(-LOGO_SIZE, .1, LOGO_SIZE, 0, 0);
//     pg.endShape(CLOSE);
        
//     // Speakers
//     pg.fill(#000000);
//     pg.stroke(#202020);
//     for (Column column : venue.columns) {
//       pg.translate(column.cx, 0, column.cz);
//       pg.rotateY(-column.theta);
//       pg.translate(0, 9*INCHES, 0);
//       pg.rotateX(Column.SPEAKER_ANGLE);
//       pg.box(21*INCHES, 16*INCHES, 15*INCHES);
//       pg.rotateX(-Column.SPEAKER_ANGLE);
//       pg.translate(0, 6*FEET-9*INCHES, 0);
//       pg.box(21*INCHES, 16*INCHES, 15*INCHES);
//       pg.translate(0, 11*FEET + 3*INCHES - 6*FEET, 0);
//       pg.rotateX(-Column.SPEAKER_ANGLE);
//       pg.box(21*INCHES, 16*INCHES, 15*INCHES);
//       pg.rotateX(Column.SPEAKER_ANGLE);
//       pg.rotateY(+column.theta);
//       pg.translate(-column.cx, -11*FEET - 3*INCHES, -column.cz);
//     }
//   }
    
//   protected abstract void drawFloor(UI ui, PGraphics pg);
// }

// class UISatellite extends UIVenue {
//   public void drawFloor(UI ui, PGraphics pg) {
//     pg.beginShape();
//     for (PVector v : Satellite.PLATFORM_POSITIONS) {
//       pg.vertex(v.x, 0, v.y);
//     }
//     pg.endShape(CLOSE);
//     pg.beginShape(QUAD_STRIP);
//     for (int vi = 0; vi <= Satellite.PLATFORM_POSITIONS.length; ++vi) {
//       PVector v = Satellite.PLATFORM_POSITIONS[vi % Satellite.PLATFORM_POSITIONS.length];
//       pg.vertex(v.x, 0, v.y);
//       pg.vertex(v.x, -8*INCHES, v.y);
//     }
//     pg.endShape();
//   }
// }
    
// class UIMidway extends UIVenue {
        
//   @Override
//   public void onDraw(UI ui, PGraphics pg) {
//     super.onDraw(ui, pg);
        
//     // Desk
//     pg.translate(0, 20*INCHES, -Midway.DEPTH/2 + 18*INCHES);
//     pg.box(6*FEET, 40*INCHES, 36*INCHES);
//     pg.translate(0, -20*INCHES, Midway.DEPTH/2 - 18*INCHES);
        
//     // Subwoofers
//     for (PVector pv : Midway.SUB_POSITIONS) {
//       pg.translate(pv.x, 10*INCHES, pv.y);
//       pg.rotateY(-QUARTER_PI);
//       pg.box(29*INCHES, 20*INCHES, 29*INCHES);
//       pg.rotateY(QUARTER_PI);
//       pg.translate(-pv.x, -10*INCHES, -pv.y);
//     }
//   }
    
//   @Override
//   protected void drawFloor(UI ui, PGraphics pg) {
//     // Floor
//     pg.translate(0, -4*INCHES, 0);
//     pg.box(Midway.WIDTH, 8*INCHES, Midway.DEPTH);
//     pg.translate(0, 4*INCHES, 0);
//   }        
// }

class UIOutputs extends UICollapsibleSection {
        UIOutputs(UI ui, float x, float y, float w) {
                super(ui, x, y, w, 124);

                final SortedSet<SLController> sortedControllers = new TreeSet<SLController>(new Comparator<SLController>() {
                        public int compare(SLController o1, SLController o2) {
                                try {
                                        return Integer.parseInt(o1.cubeId) - Integer.parseInt(o2.cubeId);
                                } catch (NumberFormatException e) {
                                        return o1.cubeId.compareTo(o2.cubeId);
                                }
                        }
                });
                final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
                for (SLController b : controllers) { sortedControllers.add(b); }
                for (SLController b : sortedControllers) { items.add(new ControllerItem(b)); }
                final UIItemList outputList = new UIItemList(ui, 1, 0, w-11, 20);

                outputList.setItems(items).addToContainer(this);

                setTitle(items.size());

                controllers.addListener(new ListListener<SLController>() {
                    public void itemAdded(final int index, final SLController c) {
                        dispatcher.dispatchUi(new Runnable() {
                                public void run() {
                                        if (c.networkDevice != null) c.networkDevice.version.addListener(deviceVersionListener);
                                        sortedControllers.add(c);
                                        items.clear();
                                                for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                                        outputList.setItems(items);
                                        setTitle(items.size());
                                        redraw();
                                }
                        });
                    }
                    public void itemRemoved(final int index, final SLController c) {
                        dispatcher.dispatchUi(new Runnable() {
                                public void run() {
                                        if (c.networkDevice != null) c.networkDevice.version.removeListener(deviceVersionListener);
                                        sortedControllers.remove(c);
                                        items.clear();
                                                for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                                        outputList.setItems(items);
                                        setTitle(items.size());
                                        redraw();
                                }
                        });
                    }
                });

                addTopLevelComponent(new UIButton(4, 4, 12, 12) {
                    @Override
                    public void onToggle(boolean on) {
                        for (SLController c : controllers)
                                c.enabled.setValue(on);
                    }
                }.setBorderRounding(4));
        }

        private final IntListener deviceVersionListener = new IntListener() {
                public void onChange(int version) {
                        dispatcher.dispatchUi(new Runnable() {
                        public void run() {
                                        redraw();
                                }
                        });
                }
        };

        private void setTitle(int count) {
                setTitle("OUTPUT (" + count + ")");
                setTitleX(20);
        }

        class ControllerItem extends UIItemList.AbstractItem {
                final SLController controller;

                ControllerItem(SLController _controller) {
                    this.controller = _controller;
                    controller.enabled.addListener(new LXParameterListener() {
                        public void onParameterChanged(LXParameter parameter) { redraw(); }
                    });
                }

                public String getLabel() {
                        if (controller.networkDevice != null && controller.networkDevice.version.get() != -1) {
                                return controller.cubeId + " (v" + controller.networkDevice.version + ")";
                        } else {
                                return controller.cubeId;
                        }
                }

                public boolean isSelected() { 
                        return controller.enabled.isOn();
                }

                @Override
                public int getActiveColor(UI ui) {
                        return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
                }

                @Override
                public void onActivate() {
                        println("onActivate");
                        controller.enabled.toggle();
                }

                @Override
                public void onDeactivate() {
                        println("onDeactivate");
                        controller.enabled.setValue(false);
                }
        }
}

// class UIEnvelopSource extends UICollapsibleSection {
//   UIEnvelopSource(UI ui, float x, float y, float w) {
//     super(ui, x, y, w, 124);
//     setTitle("StudioCubes SOURCE");
//     new UIEnvelopMeter(ui, studioCubes.source, 0, 0, getContentWidth(), 60).addToContainer(this);    
//     UIAudio.addGainAndRange(this, 64, studioCubes.source.gain, studioCubes.source.range);
//     UIAudio.addAttackAndRelease(this, 84, studioCubes.source.attack, studioCubes.source.release);
//   }
// }

// class UIEnvelopDecode extends UICollapsibleSection {
//   UIEnvelopDecode(UI ui, float x, float y, float w) {
//     super(ui, x, y, w, 124);
//     setTitle("STUDIOCUBES DECODE");
//     new UIEnvelopMeter(ui, studioCubes.decode, 0, 0, getContentWidth(), 60).addToContainer(this);
//     UIAudio.addGainAndRange(this, 64, studioCubes.decode.gain, studioCubes.decode.range);
//     UIAudio.addAttackAndRelease(this, 84, studioCubes.decode.attack, studioCubes.decode.release);
//   }
// }

// class UIEnvelopMeter extends UI2dComponent {
        
//   private final StudioCubes.Meter meter;
    
//   public UIEnvelopMeter(UI ui, StudioCubes.Meter meter, float x, float y, float w, float h) {
//     super(x, y, w, h);
//     this.meter = meter;
//     setBackgroundColor(ui.theme.getDarkBackgroundColor());
//     setBorderColor(ui.theme.getControlBorderColor());
//   }
    
//   public void onDraw(UI ui, PGraphics pg) {
//     BoundedParameter[] channels = this.meter.getChannels();
//     float bandWidth = ((width-2) - (channels.length-1)) / channels.length;
        
//     pg.noStroke();
//     pg.fill(ui.theme.getPrimaryColor());
//     int x = 1;
//     for (int i = 0; i < channels.length; ++i) {
//       int nextX = Math.round(1 + (bandWidth+1) * (i+1));
//       float h = (this.height-2) * channels[i].getValuef(); 
//       pg.rect(x, this.height-1-h, nextX-x-1, h);
//       x = nextX;
//     }
                
//     // TODO(mcslee): do this properly, on a timer
//     redraw();
//   }
// }

// class UISoundObjects extends UI3dComponent {
//   final PFont objectLabelFont; 

//   UISoundObjects() {
//     this.objectLabelFont = loadFont("Arial-Black-24.vlw");
//   }
    
//   public void onDraw(UI ui, PGraphics pg) {
//     for (StudioCubes.Source.Channel channel : studioCubes.source.channels) {
//       if (channel.active) {
//         float tx = channel.tx;
//         float ty = channel.ty;
//         float tz = channel.tz;
//         pg.directionalLight(40, 40, 40, .5, -.4, 1);
//         pg.ambientLight(40, 40, 40);
//         pg.translate(tx, ty, tz);
//         pg.noStroke();
//         pg.fill(0xff00ddff);
//         pg.sphere(6*INCHES);
//         pg.noLights();
//         pg.scale(1, -1);
//         pg.textAlign(CENTER, CENTER);
//         pg.textFont(objectLabelFont);
//         pg.textSize(4);
//         pg.fill(#00ddff);
//         pg.text(Integer.toString(channel.index), 0, -1*INCHES, -6.1*INCHES);
//         pg.scale(1, -1);
//         pg.translate(-tx, -ty, -tz);
//       }
//     }    
//   }
// }




// See: https://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
public static final class OsUtils {

    private static String osName = null;

    public static String getOsName() {
        if (osName == null) {
            osName = System.getProperty("os.name");
        }
        return osName;
    }

    public static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }

    public static boolean isUnix() {
        return false;
    }

    public static boolean isMacOsX() {
        return getOsName().equals("Mac OS X");
    }

}

// See: http://stackoverflow.com/a/5607373/216311
public class NotYetImplementedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}

public static final class NetworkUtils {

    private static Pattern macAddressPattern = null;

    private static void initMacAddressPattern() {
        if (macAddressPattern == null) {
            macAddressPattern = Pattern.compile("(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2}):(\\p{XDigit}{1,2})");
        }
    }

    public static String normalizeMacAddress(String macAddress) {
        initMacAddressPattern();
        Matcher m = macAddressPattern.matcher(macAddress);
        if (!m.matches()) {
            throw new IllegalArgumentException("NetworkUtils.normalizeMacAddress(String macAddress): Not a mac address: " + macAddress);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            if (i != 1) sb.append(":");
            sb.append(NumberUtils.normalizeHex(m.group(i)));
        }
        return sb.toString();
    }

    public static String normalizeMacAddressUpper(String macAddress) {
        initMacAddressPattern();
        Matcher m = macAddressPattern.matcher(macAddress);
        if (!m.matches()) {
            throw new IllegalArgumentException("NetworkUtils.normalizeMacAddressUpper(String macAddress): Not a mac address: " + macAddress);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            if (i != 1) sb.append(":");
            sb.append(NumberUtils.normalizeHexUpper(m.group(i)));
        }
        return sb.toString();
    }

    public static String macAddrToString(byte[] addr) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (byte b : addr) {
            if (i++ != 0) sb.append(":");
            sb.append(NumberUtils.normalizeHex(b));
        }
        return sb.toString();
    }

    public static InetAddress ipAddrToInetAddr(String addr) {
        try {
            return InetAddress.getByName(addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isValidMacAddr(byte[] macAddr) {
        return macAddr[0] != (byte)0xff && macAddr[1] != (byte)0xff && macAddr[2] != (byte)0xff
            && macAddr[3] != (byte)0xff && macAddr[4] != (byte)0xff && macAddr[5] != (byte)0xff;
    }

}

public static final class NumberUtils {

    public static String normalizeHex(String hex) {
        int value = Integer.parseInt(hex, 16);
        return Integer.toString(value, 16);
    }

    public static String normalizeHex(byte hex) {
        return Integer.toString(hex & 0xFF, 16);
    }

    public static String normalizeHexUpper(String hex) {
        int value = Integer.parseInt(hex, 16);
        return String.format("%02X", value);
    }

    public static String normalizeHexUpper(byte hex) {
        return String.format("%02X", hex & 0xFF);
    }

    public static byte hexStringToByte(String hex) {
        return (byte)Integer.parseInt(hex, 16);
    }

    public static int byteToInt(byte b) {
        return (b + 256) % 256;
    }

}

public static final class MathUtils {

    public static byte byteSubtract(int a, int b) {
        byte res = (byte)(a - b);
        return (byte)(res & (byte)((b&0xFF) <= (a&0xFF) ? -1 : 0));
    }
    
    public static byte byteMultiply(byte b, double s) {
        int res = (int)((b&0xFF) * s);
        byte hi = (byte)(res >> 8);
        byte lo = (byte)(res);
        return (byte)(lo | (byte)(hi==0 ? 0 : -1));
    }

    public static void interpolateArray(float[] in, float[] out) {
        if (out.length == in.length) {
            System.arraycopy(in, 0, out, 0, out.length);
            return;
        }

        float outPerIn = 1.0f * (out.length-1) / (in.length-1);
        for (int outIndex = 0; outIndex < out.length; outIndex++) {
            int inIndex = (int)(outIndex / outPerIn);
            // Test if we're the nearest index to the exact index in the `in` array
            // to keep those crisp and un-aliased
            if ((int)(outIndex % outPerIn) == 0) { //  || inIndex+1 >= in.length
                out[outIndex] = in[inIndex];
            } else {
                // Use spline fitting. (Double up the value if we're at the edge of the `out` array)
                if (inIndex >= 1 && inIndex < in.length-2) {
                    out[outIndex] = Utils.curvePoint(in[inIndex-1], in[inIndex], in[inIndex+1],
                        in[inIndex+2], (outIndex/outPerIn) % 1);
                } else if (inIndex == 0) {
                    out[outIndex] = Utils.curvePoint(in[inIndex], in[inIndex], in[inIndex+1],
                        in[inIndex+2], (outIndex/outPerIn) % 1);
                } else {
                    out[outIndex] = Utils.curvePoint(in[inIndex-1], in[inIndex], in[inIndex+1],
                        in[inIndex+1], (outIndex/outPerIn) % 1);
                }
            }
        }
    }

}

public static final class ColorUtils {

    public static int setAlpha(int rgb, int alpha) {
        return (rgb & (~LXColor.ALPHA_MASK)) | ((alpha << LXColor.ALPHA_SHIFT) & LXColor.ALPHA_MASK);
    }

    public static int setAlpha(int rgb, float alpha) {
        return setAlpha(rgb, (int) (alpha * 0xff));
    }

    public static int setAlpha(int rgb, double alpha) {
        return setAlpha(rgb, (int) (alpha * 0xff));
    }

    public static int scaleAlpha(int argb, double s) {
        return setAlpha(argb, MathUtils.byteMultiply(LXColor.alpha(argb), s));
    }

    public static int subtractAlpha(int argb, int amount) {
        return setAlpha(argb, MathUtils.byteSubtract(LXColor.alpha(argb), amount));
    }

    public static void blend(int[] dst, int[] src) {
        for (int i = 0; i < src.length; i++) {
            dst[i] = ColorUtils.blend(dst[i], src[i]);
        }
    }

    public static int blend(int dst, int src) {
        float dstA = (dst>>24&0xFF) / 255.0f;
        float srcA = (src>>24&0xFF) / 255.0f;
        float outA = srcA + dstA * (1 - srcA);
        if (outA == 0) {
            return 0;
        }
        int outR = FastMath.round(((src>>16&0xFF) * srcA + (dst>>16&0xFF) * dstA * (1 - srcA)) / outA)&0xFF;
        int outG = FastMath.round(((src>>8&0xFF) * srcA + (dst>>8&0xFF) * dstA * (1 - srcA)) / outA)&0xFF;
        int outB = FastMath.round(((src&0xFF) * srcA + (dst&0xFF) * dstA * (1 - srcA)) / outA)&0xFF;
        return (((int)(outA*0xFF))&0xFF)<<24 | outR<<16 | outG<<8 | outB;
    }

    public static int max(int dst, int src) {
        int outA = FastMath.max(src>>24&0xFF, dst>>24&0xFF);
        int outR = FastMath.max(src>>16&0xFF, dst>>16&0xFF);
        int outG = FastMath.max(src>>8&0xFF, dst>>8&0xFF);
        int outB = FastMath.max(src&0xFF, dst&0xFF);
        return outA<<24 | outR<<16 | outG<<8 | outB;
    }

    public static int maxAlpha(int dst, int src) {
        return (src>>24&0xFF) > (dst>>24&0xFF) ? src : dst;
    }

}

public static final class ReflectionUtils {

    public static void replaceAllFields(Object start, List oldObjects, List newObjects) {
        Set duplicateCheck = new HashSet();
        LinkedList traversal = new LinkedList();
        duplicateCheck.add(start);
        traversal.add(start);
        ListIterator iter = traversal.listIterator(0);
        while (iter.hasNext()) {
            Object obj = iter.next();
            Class objClass = obj.getClass();
            // println("objClass.getName(): "+objClass.getName());
            while (objClass != null) {
                for (Field field : objClass.getDeclaredFields()) {
                    // println("field: "+field);
                    field.setAccessible(true);
                    try {
                        Class fieldType = field.getType();
                        Object fieldObj = field.get(obj);
                        if (fieldObj == null) continue;
                        if (fieldType.isAssignableFrom(LXPattern.class)) {
                            if (fieldObj != null) {
                                int index = oldObjects.indexOf(fieldObj);
                                if (index != -1) {
                                    Object newObj = newObjects.get(index);
                                    setField(field, obj, newObj);
                                }
                            }
                        } else if (fieldType.getPackage() == null || fieldType.getPackage().getName().startsWith("heronarts")) {
                            if (!duplicateCheck.contains(fieldObj)) {
                                duplicateCheck.add(fieldObj);
                                iter.add(fieldObj);
                                iter.previous();
                            }
                        } else if (List.class.isAssignableFrom(fieldType)) {
                            List list = (List)fieldObj;
                            for (int i = 0; i < list.size(); i++) {
                                Object o = list.get(i);
                                if (o == null) continue;
                                if (o instanceof LXPattern) {
                                    int index = oldObjects.indexOf(fieldObj);
                                    if (index != -1) {
                                        Object newObj = newObjects.get(index);
                                        list.set(i, newObj);
                                    }
                                } else if (o.getClass().getPackage() == null || o.getClass().getPackage().getName().startsWith("heronarts")) {
                                    if (!duplicateCheck.contains(o)) {
                                        duplicateCheck.add(o);
                                        iter.add(o);
                                        iter.previous();
                                    }
                                }
                            }
                        } else if (fieldType.isArray()) {
                            Object[] array = (Object[])fieldObj;
                            for (int i = 0; i < array.length; i++) {
                                Object o = array[i];
                                if (o == null) continue;
                                if (o instanceof LXPattern) {
                                    int index = oldObjects.indexOf(fieldObj);
                                    if (index != -1) {
                                        Object newObj = newObjects.get(index);
                                        array[i] = newObj;
                                    }
                                } else if (o.getClass().getPackage() == null || o.getClass().getPackage().getName().startsWith("heronarts")) {
                                    if (!duplicateCheck.contains(o)) {
                                        duplicateCheck.add(o);
                                        iter.add(o);
                                        iter.previous();
                                    }
                                }
                            }
                        }
                    } catch (IllegalAccessException e) {
                        println("e: "+e);
                        continue;
                    }
                }
                objClass = objClass.getSuperclass();
            }
        }
    }

    public static void swapObjects(Object original, Object replacement) {
        // println("original.getClass().getName(): "+original.getClass().getName());
        Class<?> originalClass = original.getClass();
        Class<?> replacementClass = replacement.getClass();
        while (originalClass != null && replacementClass != null) {
            for (Field oldField : originalClass.getDeclaredFields()) {
                // println("oldField: "+oldField);
                setField(oldField, replacementClass, original, replacement);
            }
            originalClass = originalClass.getSuperclass();
            replacementClass = replacementClass.getSuperclass();
        }
    }

    public static void setField(Field originalField, Class replacementClass, Object original, Object replacement) {
        try {
            Field newField = replacementClass.getDeclaredField(originalField.getName());

            if (!newField.getType().isAssignableFrom(originalField.getType())) return;

            newField.setAccessible(true);
            originalField.setAccessible(true);

            // ignore final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(newField, newField.getModifiers() & ~Modifier.FINAL);

            newField.set(replacement, originalField.get(original));
        } catch (NoSuchFieldException e) {
            println("setField: "+e);
            return;
        } catch (IllegalAccessException e) {
            println("setField: "+e);
            return;
        }
    }

    public static void setField(Field field, Object owner, Object newValue) {
        try {
            field.setAccessible(true);

            // ignore final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(owner, newValue);
        } catch (NoSuchFieldException e) {
            return;
        } catch (IllegalAccessException e) {
            println("e: "+e);
            return;
        }
    }
}

// See: http://stackoverflow.com/a/692580/216311
/**Writes to nowhere*/
public static class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
    }
}

public static final class PrintUtils {

    private static final PrintStream defaultOut = System.out;
    private static final PrintStream nullOut = new PrintStream(new NullOutputStream());

    public static void disablePrintln() {
        System.setOut(nullOut);
    }

    public static void enablePrintln() {
        System.setOut(defaultOut);
    }

}

// http://stackoverflow.com/a/60766/216311
public static class ClassPathHack {
    private static final Class[] parameters = new Class[] {URL.class};

    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        // try {
        //   Method method = sysclass.getDeclaredMethod("addURL", parameters);
        //   method.setAccessible(true);
        //   method.invoke(sysloader, new Object[] {u});
        // } catch (Throwable t) {
        //   t.printStackTrace();
        //   throw new IOException("Error, could not add URL to system classloader");
        // }
    }

    // http://fahdshariff.blogspot.com/2011/08/changing-java-library-path-at-runtime.html
    /**
    * Adds the specified path to the java library path
    *
    * @param pathToAdd the path to add
    * @throws Exception
    */
    public static void addLibraryPath(String pathToAdd) throws Exception {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);
 
        //get array of paths
        final String[] paths = (String[])usrPathsField.get(null);
 
        //check if the path to add is already present
        for(String path : paths) {
            if(path.equals(pathToAdd)) {
                return;
            }
        }
 
        //add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length-1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }
}

static public float angleBetween(PVector v1, PVector v2) {

    // We get NaN if we pass in a zero vector which can cause problems
    // Zero seems like a reasonable angle between a (0,0,0) vector and something else
    if (v1.x == 0 && v1.y == 0 && v1.z == 0 ) return 0.0f;
    if (v2.x == 0 && v2.y == 0 && v2.z == 0 ) return 0.0f;

    double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    double v1mag = FastMath.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
    double v2mag = FastMath.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
    // This should be a number between -1 and 1, since it's "normalized"
    double amt = dot / (v1mag * v2mag);
    // But if it's not due to rounding error, then we need to fix it
    // http://code.google.com/p/processing/issues/detail?id=340
    // Otherwise if outside the range, acos() will return NaN
    // http://www.cppreference.com/wiki/c/math/acos
    if (amt <= -1) {
        return PConstants.PI;
    } else if (amt >= 1) {
        // http://code.google.com/p/processing/issues/detail?id=435
        return 0;
    }
    return (float) FastMath.acos(amt);
}

static class AudioUtils {

    static final double LOG_2 = Math.log(2);

    public static double freqToOctave(double freq) {
        return freqToOctave(freq, 1);
    }

    public static double freqToOctave(double freq, double freqRef) {
        return Math.log(Math.max(1, freq / freqRef)) / LOG_2;
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
