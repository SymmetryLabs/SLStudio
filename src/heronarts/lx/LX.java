/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx;

import heronarts.lx.client.UDPClient;
import heronarts.lx.effect.DesaturationEffect;
import heronarts.lx.effect.FlashEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.kinet.Kinet;
import heronarts.lx.kinet.KinetNode;
import heronarts.lx.model.Grid;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transition.LXTransition;
import heronarts.lx.ui.UI;

import java.awt.Color;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import ddf.minim.Minim;
import ddf.minim.AudioInput;

/**
 * Core controller for a LX instance. Each instance drives a
 * grid of nodes with a fixed width and height. The nodes are indexed
 * using typical computer graphics coordinates, with the x-axis going
 * from left to right, y-axis from top to bottom.
 * 
 * <pre>
 *    x
 *  y 0 1 2 .
 *    1 . . .
 *    2 . . .
 *    . . . .
 * </pre>
 *    
 *  Note that the grid layout is just a helper. The node buffer is
 *  actually a 1-d array and can be used to represent any type of layout.
 *  The library just provides helpful accessors for grid layouts.
 *  
 *  The instance manages rotation amongst a set of patterns. There may
 *  be multiple decks, each with its own list of patterns. These decks
 *  are then blended together.
 *  
 *  The color-space used is HSB, with H ranging from 0-360, S from 0-100,
 *  and B from 0-100.
 */
public class LX {
    
    public final static String VERSION = "##library.prettyVersion##";

    /**
     * Returns the version of the library.
     * 
     * @return String
     */
    public static String version() {
        return VERSION;
    }
    
    /**
     * A reference to the applet context.
     */
    public final PApplet applet;
    
    /**
     * The width of the grid, immutable.
     */
    public final int width;
    
    /**
     * The height of the grid, immutable.
     */
    public final int height;
    
    /**
     * The midpoint of the x-space.
     */
    public final float cx;
    
    /**
     * This midpoint of the y-space.
     */
    public final float cy;
    
    /**
     * The pixel model.
     */
    public final LXModel model;
    
    /**
     * The total number of pixels in the grid, immutable.
     */
    public final int total;

    /**
     * The pattern engine.
     */
    public final LXEngine engine;
    
    /**
     * The UI container.
     */
    public final UI ui;
    
    /**
     * Internal buffer for colors, owned by Processing animation thread.
     */
    private final int[] buffer;
    
    /**
     * The current frame's colors, either from the engine or the buffer. Note that
     * this is a reference to an array. 
     */
    private int[] colors;
    
    /**
     * The simulation UI renderer.
     */
    private final Simulation simulation;
    
    /**
     * KiNET output driver.
     */
    private Kinet kinet;
    
    /**
     * Client listener.
     */
    private UDPClient client;
    
    /**
     * Whether drawing is enabled
     */
    private boolean simulationEnabled;
    
    /**
     * The global tempo object. 
     */
    public final Tempo tempo;
    
    /**
     * The global touch object.
     */
    private Touch touch; 
    
    /**
     * Minim instance to provide audio input.
     */
    private Minim minim;

    /**
     * The global audio input.
     */
    private AudioInput audioInput;
    
    /**
     * Global modulator for shared base hue.
     */
    private LXModulator baseHue;
    
    /**
     * Global flash effect.
     */
    private final FlashEffect flash;
    
    /**
     * Global desaturation effect.
     */
    private final DesaturationEffect desaturation;
    
    private final class Flags {
        public boolean showFramerate = false;
        public boolean keyboardTempo = false;
    }
    
    public class Timer {        
        public long drawNanos = 0;
        public long clientNanos = 0;
        public long engineNanos = 0;
        public long simulationNanos = 0;
        public long uiNanos = 0;
        public long kinetNanos = 0;
    }
    
    public final Timer timer = new Timer();
    
    private final Flags flags = new Flags();

    /**
     * Creates an LX instance with no nodes.
     * 
     * @param applet
     */
    public LX(PApplet applet) {
        this(applet, 0);
    }
    
    /**
     * Creates an LX instance. This instance will run patterns
     * for a grid of the specified size.
     * 
     * @param applet
     * @param total
     */
    public LX(PApplet applet, int total) {
        this(applet, total, 1);
    }
    
    /**
     * Creates a LX instance. This instance will run patterns
     * for a grid of the specified size.
     * 
     * @param applet
     * @param width
     * @param height
     */
    public LX(PApplet applet, int width, int height) {
        this(applet, new LXModel(new Grid(width, height)));
    }
    
    /**
     * Constructs an LX instance with the given pixel model
     * 
     * @param applet
     * @param model Pixel model
     */
    public LX(PApplet applet, LXModel model) {
        this.applet = applet;
        this.model = model;
        this.total = model.points.size();
        
        this.cx = model.cx;
        this.cy = model.cy;
        
        LXFixture fixture = model.fixtures.get(0);
        if (fixture instanceof Grid) {
            Grid grid = (Grid) fixture;
            this.width = grid.width;
            this.height = grid.height;
        } else {
            this.width = this.height = 0;
        }
        
        this.kinet = null;
        this.client = null;
        
        this.engine = new LXEngine(this);
        this.buffer = new int[this.total];
        this.colors = this.engine.renderBuffer();
        
        this.simulationEnabled = false;
        this.simulation = new Simulation(this);
        
        this.baseHue = null;
        this.cycleBaseHue(30000);
                
        this.touch = new Touch.NullTouch();
        this.tempo = new Tempo();
        
        this.desaturation = new DesaturationEffect(this);
        this.flash = new FlashEffect(this);
        
        applet.colorMode(PConstants.HSB, 360, 100, 100, 100);

        this.ui = new UI(applet);
        
        try {
            // Processing 2.x
            Method m = applet.getClass().getMethod("registerMethod", String.class, Object.class);
            System.out.println("LX detected Processing 2.x");
            m.invoke(applet, "draw", this);
            m.invoke(applet, "dispose", this);
            m.invoke(applet, "keyEvent", new KeyEvent2x());
            m.invoke(applet, "mouseEvent", new MouseEvent2x());
        } catch (Exception x) {
            // Processing 1.x
            System.out.println("LX detected Processing 1.x");
            applet.registerDraw(this);
            applet.registerKeyEvent(new KeyEvent1x());
            applet.registerMouseEvent(new MouseEvent1x());
        }
        
        applet.addMouseWheelListener(new java.awt.event.MouseWheelListener() { 
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent mwe) { 
                ui.mouseWheel(mwe.getX(), mwe.getY(), mwe.getWheelRotation());
            }
        });
    }
    
    public final class KeyEvent1x {
        public void keyEvent(java.awt.event.KeyEvent e) {
            keyEvent1x(e);
        }
    }
    
    public final class KeyEvent2x {
        public void keyEvent(processing.event.KeyEvent e) {
            keyEvent2x(e);
        }
    }
    
    public final class MouseEvent1x {
        public void mouseEvent(java.awt.event.MouseEvent e) {
            mouseEvent1x(e);
        }
    }
    
    public final class MouseEvent2x {
        public void mouseEvent(processing.event.MouseEvent e) {
            mouseEvent2x(e);
        }
    }
    
    /**
     * Invoked by the processing engine on applet shutdown.
     */
    public void dispose() {
        if (this.audioInput != null) {
            this.audioInput.close();
            this.audioInput = null;
        }
        if (this.minim != null) {
            this.minim.stop();
            this.minim = null;
        }
    }
    
    /**
     * Scales the brightness of an array of colors by some factor
     * 
     * @param rgbs Array of color values
     * @param s Factor by which to scale brightness
     * @return Array of new color values
     */
    public static int[] scaleBrightness(int[] rgbs, float s) {
        int[] result = new int[rgbs.length];
        scaleBrightness(rgbs, s, result);
        return result;
    }
    
    /**
     * Scales the brightness of an array of colors by some factor
     * 
     * @param rgbs Array of color values
     * @param s Factor by which to scale brightness
     * @param result Array to write results into, if null, input array is modified
     */
    public static void scaleBrightness(int[] rgbs, float s, int[] result) {
        int r, g, b, rgb;
        float[] hsb = new float[3];
        if (result == null) {
            result = rgbs;
        }
        for (int i = 0; i < rgbs.length; ++i) {
            rgb = rgbs[i];
            r = (rgb >> 16) & 0xff;
            g = (rgb >> 8) & 0xff;
            b = rgb & 0xff;        
            Color.RGBtoHSB(r, g, b, hsb);
            result[i] = Color.HSBtoRGB(hsb[0], hsb[1], Math.min(1, hsb[2] * s));
        }
     }
    
    /**
     * Scales the brightness of a color by a factor
     * 
     * @param rgb Color value
     * @param s Factory by which to scale brightness
     * @return New color
     */
    public static int scaleBrightness(int rgb, float s) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;        
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return Color.HSBtoRGB(hsb[0], hsb[1], Math.min(1, hsb[2] * s));
    }
    
    /**
     * Utility function to invoke Color.RGBtoHSB without requiring the
     * caller to manually unpack bytes from an integer color.
     * 
     * @param rgb ARGB integer color
     * @param hsb Array into which results should be placed
     * @return Array of hsb values, or null if hsb parameter was provided
     */
    public static float[] RGBtoHSB(int rgb, float[] hsb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;             
        return Color.RGBtoHSB(r, g, b, hsb);
    }    
    
    /**
     * Thread-safe accessor for the hue of a color
     * 
     * @param rgb
     * @return Hue value from 0-360
     */
    public static float h(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int max = (r > g) ? r : g;
        if (b > max) max = b;
        int min = (r < g) ? r : g;
        if (b < min) min = b;
        if (max == 0) return 0;
        float range = max - min;
        float h;
        float rc = (max-r)/range;
        float gc = (max-g)/range;
        float bc = (max-b)/range;
        if (r == max) h = bc - gc;
        else if (g == max) h = 2.f + rc - bc;
        else h = 4.f + gc - rc;
        h /= 6.f;
        if (h < 0) {
            h += 1.f;
        }
        return 360.f*h;
    }
    
    /**
     * Thread-safe accessor for the saturation of a color
     * 
     * @param rgb
     * @return Saturation value from 0-100
     */
    public static float s(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int max = (r > g) ? r : g;
        if (b > max) max = b;
        int min = (r < g) ? r : g;
        if (b < min) min = b;
        return (max == 0) ? 0 : (max-min) * 100.f / (float) max;
    }
    
    /**
     * Thread-safe accessor for the brightness of a color
     * 
     * @param rgb
     * @return Brightness from 0-100
     */
    public static float b(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int max = (r > g) ? r : g;
        if (b > max) max = b;
        return 100.f * max / 255.f;
    }

    /**
     * Utility to create a color from double values
     * 
     * @param h Hue
     * @param s Saturation
     * @param b Brightness
     * @return Color value
     */
    public static final int colord(double h, double s, double b) {
        return hsb((float)h, (float)s, (float)b);
    }

    /**
     * Utility to create a color from double values
     * 
     * @param h Hue
     * @param s Saturation
     * @param b Brightness
     * @return Color value
     */
    public static final int hsb(double h, double s, double b) {
        return hsb((float)h, (float)s, (float)b);
    }
    
    /**
     * Thread-safe function to create color from HSB
     * 
     * @param h Hue from 0-360
     * @param s Saturation from 0-100
     * @param b Brightness from 
     * @return rgb color value
     */
    public static int hsb(float h, float s, float b) {
        return Color.HSBtoRGB(h/360.f, s/100.f, b/100.f);
    }
    
    /**
     * Adds basic flash and desaturation effects to the engine, triggerable
     * by the keyboard. The 's' key triggers desaturation, and the '/' key
     * triggers a flash
     */
    public LX enableBasicEffects() {
        this.addEffect(this.desaturation);
        this.addEffect(this.flash);
        return this;
    }
    
    /**
     * Enables the tempo to be controlled by the keyboard arrow keys. Left
     * and right arrows change the tempo by .1 BPM, and the space-bar taps
     * the tempo.
     */
    public LX enableKeyboardTempo() {
        this.flags.keyboardTempo = true;
        return this;
    }
        
    /**
     * Utility logging function
     * 
     * @param s Logs the string with relevant prefix
     */
    private void log(String s) {
        System.out.println("LX: " + s);
    }
    
    /**
     * Returns the current color values
     * 
     * @return Array of the current color values
     */
    public final int[] getColors() {
        return this.colors;
    }
    
    /**
     * Return the currently active transition on the main deck
     * 
     * @return A transition if one is active
     */
    public final LXTransition getTransition() {
        return this.engine.getActiveTransition();
    }
    
    /**
     * Returns the current pattern on the main deck
     * 
     * @return Currently active pattern
     */
    public final LXPattern getPattern() {
        return this.engine.getActivePattern();
    }
    
    /**
     * Returns the pattern being transitioned to on the main deck
     * 
     * @return Next pattern
     */
    public final LXPattern getNextPattern() {
        return this.engine.getNextPattern();
    }
    
    /**
     * Utility method to access the touch object.
     * 
     * @return The touch object
     */
    public Touch touch() {
        return this.touch;
    }
    
    /**
     * The active kinet output.
     * 
     * @return The kinet object
     */
    public Kinet kinet() {
        return this.kinet;
    }
    
    public final AudioInput audioInput() {
        if (audioInput == null) {
            // Lazily instantiated on-demand
            this.minim = new Minim(this.applet);
            this.audioInput = minim.getLineIn(Minim.STEREO, 1024);            
        }
        return audioInput;
    }
    
    /**
     * Utility function to return the row of a given index
     *  
     * @param i Index into colors array
     * @return Which row this index is in
     */
    public int row(int i) {
        return i / this.width;
    }
    
    /**
     * Utility function to return the column of a given index
     *  
     * @param i Index into colors array
     * @return Which column this index is in
     */
    public int column(int i) {
        return i % this.width;
    }

    /**
     * Utility function to get the x-coordinate of a pixel
     * 
     * @param i Node index
     * @return x coordinate
     */
    public int x(int i) {
        return i % this.width;
    }
        
    /**
     * Utility function to return the position of an index in x coordinate
     * space normalized from 0 to 1.
     * 
     * @param i
     * @return Position of this node in x space, from 0 to 1
     */
    public double xn(int i) {
        return (i % this.width) / (double) (this.width - 1);
    }
    
    /**
     * Utility function to return the position of an index in x coordinate
     * space normalized from 0 to 1, as a floating point.
     * 
     * @param i
     * @return Position of this node in x space, from 0 to 1
     */
    public float xnf(int i) {
        return (float)this.xn(i);
    }

    /**
     * Utility function to get the y-coordinate of a pixel
     * 
     * @param i Node index
     * @return y coordinate
     */
    public int y(int i) {
        return i / this.width;
    }
    
    /**
     * Utility function to return the position of an index in y coordinate
     * space normalized from 0 to 1.
     * 
     * @param i
     * @return Position of this node in y space, from 0 to 1
     */
    public double yn(int i) {
        return (i / this.width) / (double) (this.height - 1);
    }
    
    /**
     * Utility function to return the position of an index in y coordinate
     * space normalized from 0 to 1, as a floating point.
     * 
     * @param i
     * @return Position of this node in y space, from 0 to 1
     */
    public float ynf(int i) {
        return (float)this.yn(i);
    }
        
    /**
     * Sets the speed of the entire system. Default is 1.0, any modification will mutate de
     * deltaMs values system-wide.
     */
    public LX setSpeed(double speed) {
        this.engine.setSpeed(speed);
        return this;
    }
    
    /**
     * The effects chain
     * 
     * @return The full effects chain
     */
    public List<LXEffect> getEffects() {
        return this.engine.getEffects();
    }
    
    /**
     * Add multiple effects to the chain
     * 
     * @param effects
     */
    public LX addEffects(LXEffect[] effects) {
        for (LXEffect effect : effects) {
            addEffect(effect);
        }
        return this;
    }
    
    /**
     * Add an effect to the FX chain.
     * 
     * @param effect
     * @return Effect added
     */
    public LX addEffect(LXEffect effect) {
        this.engine.addEffect(effect);
        return this;
    }
    
    /**
     * Remove an effect from the chain
     * 
     * @param effect
     */
    public LX removeEffect(LXEffect effect) {
        this.engine.removeEffect(effect);
        return this;
    }
    
    /**
     * Add a generic modulator to the engine
     * 
     * @param modulator
     * @return Modulator added
     */
    public LX addModulator(LXModulator modulator) {
        this.engine.addModulator(modulator);
        return this;
    }
    
    /**
     * Remove a modulator from the engine
     * 
     * @param modulator
     */
    public LX removeModulator(LXModulator modulator) {
        this.engine.removeModulator(modulator);
        return this;
    }
    
    /**
     * Pause the engine from running
     * 
     * @param paused Whether to pause the engine to pause
     */
    public LX setPaused(boolean paused) {
        this.engine.setPaused(paused);
        return this;
    }
    
    /**
     * Whether the engine is currently running.
     * 
     * @return State of the engine
     */
    public boolean isPaused() {
        return this.engine.isPaused();
    }
    
    /**
     * Toggles the running state of the engine.
     */
    public LX togglePaused() {
        return setPaused(!this.engine.isPaused());
    }
    
    /**
     * Triggers the global flash effect.
     */
    public LX flash() {
        this.flash.trigger();
        return this;
    }
    
    /**
     * Sets the main deck to the previous pattern.
     */
    public LX goPrev() {
        this.engine.goPrev();
        return this;
    }
    
    /**
     * Sets the main deck to the next pattern.
     */
    public LX goNext() {
        this.engine.goNext();
        return this;
    }
    
    /**
     * Sets the main deck to a given pattern instance.
     * 
     * @param pattern The pattern instance to run
     * @return This, for method chaining
     */
    public LX goPattern(LXPattern pattern) {
        this.engine.goPattern(pattern);
        return this;
    }
    
    /**
     * Sets the main deck to a pattern of the given index
     * 
     * @param i Index of the pattern to run
     * @return This, for method chaining
     */
    public LX goIndex(int i) {
        this.engine.goIndex(i);
        return this;
    }

    /**
     * Returns the base hue shared across patterns
     * 
     * @return Base hue value shared by all patterns
     */
    public double getBaseHue() {
        if (this.baseHue == null) {
            return 0;
        }
        return (this.baseHue.getValue() + 360) % 360;
    }
    
    /**
     * Gets the base hue as a float
     * 
     * @return The global base hue
     */
    public float getBaseHuef() {
        return (float)this.getBaseHue();
    }
    
    /**
     * Sets the base hue to a fixed value
     * 
     * @param hue Fixed value to set hue to, 0-360
     */
    public LX setBaseHue(double hue) {
        this.engine.removeModulator(this.baseHue);
        this.engine.addModulator(this.baseHue = new LinearEnvelope(this.getBaseHue(), hue, 50).trigger());
        return this;
    }
    
    /**
     * Sets the base hue to cycle through the spectrum
     * 
     * @param duration Number of milliseconds for hue cycle 
     */
    public LX cycleBaseHue(double duration) {
        double currentHue = this.getBaseHue();
        this.engine.removeModulator(this.baseHue);
        this.engine.addModulator(this.baseHue = new SawLFO(0, 360, duration).setValue(currentHue).start());
        return this;
    }
    
    /**
     * Sets the base hue to oscillate between two spectrum values
     * 
     * @param lowHue Low hue value
     * @param highHue High hue value
     * @param duration Milliseconds for hue oscillation
     */
    public LX oscillateBaseHue(double lowHue, double highHue, double duration) {
        double value = this.getBaseHue();
        this.engine.removeModulator(this.baseHue);
        this.engine.addModulator(this.baseHue = new TriangleLFO(lowHue, highHue, duration).setValue(value).trigger());
        return this;
    }

    /**
     * Stops patterns from automatically rotating
     */
    public LX disableAutoTransition() {
        this.engine.disableAutoTransition();
        return this;
    }
    
    /**
     * Sets the patterns to rotate automatically
     * 
     * @param autoTransitionThreshold Number of milliseconds after which to rotate pattern
     */
    public LX enableAutoTransition(int autoTransitionThreshold) {
        this.engine.enableAutoTransition(autoTransitionThreshold);
        return this;
    }
    
    /**
     * Whether auto transition is enabled.
     * 
     * @return Whether auto transition is enabled.
     */
    public boolean isAutoTransitionEnabled() {
        return this.engine.isAutoTransitionEnabled();
    }
    
    /**
     * Enable the simulation renderer
     * 
     * @param s Whether to automatically draw a simulation
     */
    public LX setSimulationEnabled(boolean s) {
        this.simulationEnabled = s;
        return this;
    }
    
    /**
     * Sets the size of the drawn simulation in the Processing window
     * 
     * @param x Top left x coordinate
     * @param y Top left y coordinate
     * @param w Width of simulation in pixels
     * @param h Height of simulation in pixels
     */
    public LX setSimulationBounds(int x, int y, int w, int h) {
        this.simulation.setBounds(x, y, w, h);
        return this;
    }
    
    /**
     * Listens for UDP packets from LX clients.
     */
    public LX enableUDPClient() {
        this.client = UDPClient.getInstance();
        this.client.addListener(this);
        this.touch = this.client.getTouch();
        return this;
    }
    
    /**
     * Specifies an array of kinet nodes to send output to.
     * 
     * @param kinetNodes Array of KinetNode objects, must have length equal to width * height
     */
    public LX setKinetNodes(KinetNode[] kinetNodes) {
        if (kinetNodes == null) {
            this.kinet = null;
        } else if (kinetNodes.length != this.total) {
            throw new RuntimeException("Array provided to setKinetNodes is the wrong length, must equal length of LX, use null for non-mapped output nodes.");
        } else {
            try {
                this.kinet = new Kinet(this, kinetNodes);
            } catch (SocketException sx) {
                this.kinet = null;
                throw new RuntimeException("Could not create UDP socket for Kinet", sx);
            }
        }
        return this;
    }
    
    /**
     * Specifies a Kinet object to run lighting output
     * 
     * @param kinet Kinet instance with total size equal to width * height
     */
    public LX setKinet(Kinet kinet) {
        if (kinet != null && (kinet.size() != this.total)) {
            throw new RuntimeException("Kinet provided to setKinet is the wrong size, must equal length of LX, use null for non-mapped output nodes.");            
        }
        this.kinet = kinet;
        return this;
    }

    /**
     * Specifies the set of patterns to be run. 
     * 
     * @param patterns
     */
    public LX setPatterns(LXPattern[] patterns) {
        this.engine.setPatterns(patterns);
        return this;
    }
    
    /**
     * Gets the current set of patterns on the main deck.
     * 
     * @return The pattern set
     */
    public LXPattern[] getPatterns() {
        return this.engine.getPatterns();
    }

    /**
     * Core function invoked by the processing engine on each iteration of the
     * run cycle.
     */
    public void draw() {
        long drawStart = System.nanoTime();
        
        this.timer.clientNanos = 0;
        if (this.client != null) {
            long clientStart = System.nanoTime();
            this.client.receive();
            this.timer.clientNanos = System.nanoTime() - clientStart;
        }
        
        long engineStart = System.nanoTime();
        if (this.engine.isThreaded()) {
            // If the engine is threaded, it is running itself. We just need
            // to copy its current color buffer into our own in a thread-safe
            // manner.
            this.engine.copyBuffer(this.colors = this.buffer);
        } else {
            // If the engine is not threaded, then we run it ourselves, and
            // we can just use its color buffer, as there is no thread contention.
            this.engine.run();
            this.colors = this.engine.renderBuffer();
        }
        this.timer.engineNanos = System.nanoTime() - engineStart;
        
        this.timer.kinetNanos = 0;
        if (this.kinet != null) {
            long kinetStart = System.nanoTime();
            this.kinet.sendThrottledColors(this.colors);
            this.timer.kinetNanos = System.nanoTime() - kinetStart;
        }
        
        // TODO(mcslee): remove this and convert simulation into a UIObject
        this.timer.simulationNanos = 0;
        if (this.simulationEnabled) {
            long simulationStart = System.nanoTime();
            this.simulation.draw(this.colors);
            this.timer.simulationNanos = System.nanoTime() - simulationStart;
        }
        
        long uiStart = System.nanoTime();
        this.ui.draw();
        this.timer.uiNanos = System.nanoTime() - uiStart;
                
        if (this.flags.showFramerate) {
            System.out.println("Framerate: " + this.applet.frameRate);
        }
        
        this.timer.drawNanos = System.nanoTime() - drawStart;
    }
    
    private void keyEvent2x(processing.event.KeyEvent e) {
        // TODO(mcslee): update for processing 2.0
    }
    
    private void keyEvent1x(java.awt.event.KeyEvent e) {
        if (e.getID() == java.awt.event.KeyEvent.KEY_RELEASED) {
            switch (e.getKeyCode()) {
            case java.awt.event.KeyEvent.VK_UP:
                engine.goPrev();
                break;
            case java.awt.event.KeyEvent.VK_DOWN:
                engine.goNext();
                break;
            case java.awt.event.KeyEvent.VK_LEFT:
                if (flags.keyboardTempo) {
                    tempo.setBpm(tempo.bpm() - .1);
                }
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                if (flags.keyboardTempo) {
                    tempo.setBpm(tempo.bpm() + .1);
                }
                break;
            }
            
            switch (Character.toLowerCase(e.getKeyChar())) {
            case '[':
                engine.goPrev();
                break;
            case ']':
                engine.goNext();
                break;
            case 'f':
                flags.showFramerate = false;
                break;
            case ' ':
                if (flags.keyboardTempo) {
                    tempo.tap();
                }
                break;
            case 's':
                desaturation.disable();
                break;
            case '/':
                flash.disable();
                break;
            }
        } else if (e.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
            switch (e.getKeyChar()) {
            case 'f':
                flags.showFramerate = true;
                break;
            case 's':
                desaturation.enable();
                break;
            case '/':
                flash.enable();
                break;
            }
        }
    }
    
    private void mouseEvent2x(processing.event.MouseEvent e) {
        // TODO(mcslee): update for processing 2.0
    }
    
    private void mouseEvent1x(java.awt.event.MouseEvent e) {
        switch (e.getID()) {
        case java.awt.event.MouseEvent.MOUSE_PRESSED:
            ui.mousePressed(e.getX(), e.getY());
            break;
        case java.awt.event.MouseEvent.MOUSE_RELEASED:
            ui.mouseReleased(e.getX(), e.getY());
            break;
        case java.awt.event.MouseEvent.MOUSE_DRAGGED:
            ui.mouseDragged(e.getX(), e.getY());
            break;
        }
    }
    
    public final PGraphics getGraphics() {
        return this.applet.g;
    }
}

