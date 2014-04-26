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
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transition.LXTransition;
import heronarts.lx.ui.UI;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import ddf.minim.AudioInput;
import ddf.minim.Minim;

/**
 * Core controller for a LX instance. Each instance drives a grid of nodes with
 * a fixed width and height. The nodes are indexed using typical computer
 * graphics coordinates, with the x-axis going from left to right, y-axis from
 * top to bottom.
 *
 * <pre>
 *    x
 *  y 0 1 2 .
 *    1 . . .
 *    2 . . .
 *    . . . .
 * </pre>
 *
 * Note that the grid layout is just a helper. The node buffer is actually a 1-d
 * array and can be used to represent any type of layout. The library just
 * provides helpful accessors for grid layouts.
 *
 * The instance manages rotation amongst a set of patterns. There may be
 * multiple channels, each with its own list of patterns. These channels are then
 * blended together.
 *
 * The color-space used is HSB, with H ranging from 0-360, S from 0-100, and B
 * from 0-100.
 */
public class LX {

    public final static String VERSION = "##library.prettyVersion##";

    public static boolean isProcessing2X = false;

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
     * Client listener.
     */
    private UDPClient client;

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
    private LXParameter baseHue;

    private boolean baseHueIsInternalModulator = false;

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
        public long uiNanos = 0;
    }

    public final Timer timer = new Timer();

    private final Flags flags = new Flags();

    /**
     * Creates an LX instance with no nodes.
     *
     * @param applet
     */
    public LX(PApplet applet) {
        this(applet, null);
    }

    /**
     * Creates an LX instance. This instance will run patterns for a grid of the
     * specified size.
     *
     * @param applet
     * @param total
     */
    public LX(PApplet applet, int total) {
        this(applet, total, 1);
    }

    /**
     * Creates a LX instance. This instance will run patterns for a grid of the
     * specified size.
     *
     * @param applet
     * @param width
     * @param height
     */
    public LX(PApplet applet, int width, int height) {
        this(applet, new GridModel(width, height));
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

        if (model == null) {
            this.total = this.width = this.height = 0;
            this.cx = this.cy = 0;
        } else {
            this.total = model.points.size();
            this.cx = model.cx;
            this.cy = model.cy;
            if (model instanceof GridModel) {
                GridModel grid = (GridModel) model;
                this.width = grid.width;
                this.height = grid.height;
            } else {
                this.width = this.height = 0;
            }
        }

        this.client = null;

        this.engine = new LXEngine(this);
        this.buffer = new int[this.total];
        this.colors = this.engine.renderBuffer();

        this.baseHue = null;
        this.cycleBaseHue(30000);

        this.touch = new Touch.NullTouch();
        this.tempo = new Tempo();

        this.desaturation = new DesaturationEffect(this);
        this.flash = new FlashEffect(this);

        if (applet != null) {
            this.ui = new UI(applet);
            initProcessing(applet);
        } else {
            this.ui = null;
        }
    }

    @SuppressWarnings("deprecation")
    private void initProcessing(PApplet applet) {
        applet.colorMode(PConstants.HSB, 360, 100, 100, 100);

        try {
            // Processing 2.x
            Method m = applet.getClass().getMethod("registerMethod", String.class,
                    Object.class);
            System.out.println("LX detected Processing 2.x");
            isProcessing2X = true;
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
            applet.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
                public void mouseWheelMoved(java.awt.event.MouseWheelEvent mwe) {
                    ui.mouseWheel(mwe.getX(), mwe.getY(), mwe.getWheelRotation());
                }
            });
        }

    }

    public final class KeyEvent1x {
        public void keyEvent(java.awt.event.KeyEvent e) {
            try {
                LX.this.keyEvent(new LXKeyEvent(e));
            } catch (LXKeyEvent.UnsupportedActionException uax) {
                // No problem
            }
        }
    }

    public final class KeyEvent2x {
        public void keyEvent(processing.event.KeyEvent e) {
            try {
                LX.this.keyEvent(new LXKeyEvent(e));
            } catch (LXKeyEvent.UnsupportedActionException uax) {
                // No problem
            }
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
     * Utility function to invoke Color.RGBtoHSB without requiring the caller to
     * manually unpack bytes from an integer color.
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
        if (b > max)
            max = b;
        int min = (r < g) ? r : g;
        if (b < min)
            min = b;
        if (max == 0)
            return 0;
        float range = max - min;
        float h;
        float rc = (max - r) / range;
        float gc = (max - g) / range;
        float bc = (max - b) / range;
        if (r == max)
            h = bc - gc;
        else if (g == max)
            h = 2.f + rc - bc;
        else
            h = 4.f + gc - rc;
        h /= 6.f;
        if (h < 0) {
            h += 1.f;
        }
        return 360.f * h;
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
        if (b > max)
            max = b;
        int min = (r < g) ? r : g;
        if (b < min)
            min = b;
        return (max == 0) ? 0 : (max - min) * 100.f / max;
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
        if (b > max)
            max = b;
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
    public static final int hsbd(double h, double s, double b) {
        return hsb((float) h, (float) s, (float) b);
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
        return hsb((float) h, (float) s, (float) b);
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
        return Color.HSBtoRGB((h % 360) / 360.f, s / 100.f, b / 100.f);
    }

    /**
     * Adds basic flash and desaturation effects to the engine, triggerable by the
     * keyboard. The 's' key triggers desaturation, and the '/' key triggers a
     * flash
     */
    public LX enableBasicEffects() {
        this.addEffect(this.desaturation);
        this.addEffect(this.flash);
        return this;
    }

    /**
     * Enables the tempo to be controlled by the keyboard arrow keys. Left and
     * right arrows change the tempo by .1 BPM, and the space-bar taps the tempo.
     */
    public LX enableKeyboardTempo() {
        this.flags.keyboardTempo = true;
        return this;
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
     * Return the currently active transition on the main channel
     *
     * @return A transition if one is active
     */
    public final LXTransition getTransition() {
        return this.engine.getActiveTransition();
    }

    /**
     * Returns the current pattern on the main channel
     *
     * @return Currently active pattern
     */
    public final LXPattern getPattern() {
        return this.engine.getActivePattern();
    }

    /**
     * Returns the pattern being transitioned to on the main channel
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

    public final AudioInput audioInput() {
        return audioInput(44100);
    }

    public final AudioInput audioInput(int sampleRate) {
        if (this.audioInput == null) {
            // Lazily instantiated on-demand
            this.minim = new Minim(this.applet);
            this.audioInput = minim.getLineIn(Minim.STEREO, 1024, sampleRate);
        }
        return this.audioInput;
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
     * Utility function to return the position of an index in x coordinate space
     * normalized from 0 to 1.
     *
     * @param i
     * @return Position of this node in x space, from 0 to 1
     */
    public double xn(int i) {
        return (i % this.width) / (double) (this.width - 1);
    }

    /**
     * Utility function to return the position of an index in x coordinate space
     * normalized from 0 to 1, as a floating point.
     *
     * @param i
     * @return Position of this node in x space, from 0 to 1
     */
    public float xnf(int i) {
        return (float) this.xn(i);
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
     * Utility function to return the position of an index in y coordinate space
     * normalized from 0 to 1.
     *
     * @param i
     * @return Position of this node in y space, from 0 to 1
     */
    public double yn(int i) {
        return (i / this.width) / (double) (this.height - 1);
    }

    /**
     * Utility function to return the position of an index in y coordinate space
     * normalized from 0 to 1, as a floating point.
     *
     * @param i
     * @return Position of this node in y space, from 0 to 1
     */
    public float ynf(int i) {
        return (float) this.yn(i);
    }

    /**
     * Sets the speed of the entire system. Default is 1.0, any modification will
     * mutate de deltaMs values system-wide.
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
     * Sets the main channel to the previous pattern.
     */
    public LX goPrev() {
        this.engine.goPrev();
        return this;
    }

    /**
     * Sets the main channel to the next pattern.
     */
    public LX goNext() {
        this.engine.goNext();
        return this;
    }

    /**
     * Sets the main channel to a given pattern instance.
     *
     * @param pattern The pattern instance to run
     * @return This, for method chaining
     */
    public LX goPattern(LXPattern pattern) {
        this.engine.goPattern(pattern);
        return this;
    }

    /**
     * Sets the main channel to a pattern of the given index
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
        return (float) this.getBaseHue();
    }

    private void clearBaseHue() {
        if (this.baseHueIsInternalModulator) {
            this.engine.removeModulator((LXModulator) this.baseHue);
            this.baseHueIsInternalModulator = false;
        }
    }

    private void internalBaseHue(LXModulator modulator) {
        clearBaseHue();
        this.engine.addModulator(modulator);
        this.baseHue = modulator;
        this.baseHueIsInternalModulator = true;
    }

    public LX setBaseHue(LXParameter parameter) {
        clearBaseHue();
        this.baseHue = parameter;
        return this;
    }

    /**
     * Sets the base hue to a fixed value
     *
     * @param hue Fixed value to set hue to, 0-360
     */
    public LX setBaseHue(double hue) {
        internalBaseHue(new LinearEnvelope(this.getBaseHue(), hue, 50).start());
        return this;
    }

    /**
     * Sets the base hue to cycle through the spectrum
     *
     * @param duration Number of milliseconds for hue cycle
     */
    public LX cycleBaseHue(double duration) {
        internalBaseHue(new SawLFO(0, 360, duration).setValue(getBaseHue()).start());
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
        internalBaseHue(new TriangleLFO(lowHue, highHue, duration).setValue(
                getBaseHue()).trigger());
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
     * @param autoTransitionThreshold Number of milliseconds after which to rotate
     *          pattern
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
     * Listens for UDP packets from LX clients.
     */
    public LX enableUDPClient() {
        this.client = UDPClient.getInstance();
        this.client.addListener(this);
        this.touch = this.client.getTouch();
        return this;
    }

    /**
     * Adds an output driver
     *
     * @param output
     * @return this
     */
    public LX addOutput(LXOutput output) {
        this.engine.addOutput(output);
        return this;
    }

    /**
     * Removes an output driver
     *
     * @param output
     * @return this
     */
    public LX removeOutput(LXOutput output) {
        this.engine.removeOutput(output);
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
     * Gets the current set of patterns on the main channel.
     *
     * @return The pattern set
     */
    public LXPattern[] getPatterns() {
        return this.engine.getPatterns();
    }

    /**
     * Core function invoked by the processing engine on each iteration of the run
     * cycle.
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

        long uiStart = System.nanoTime();
        this.ui.draw();
        this.timer.uiNanos = System.nanoTime() - uiStart;

        if (this.flags.showFramerate) {
            if (this.engine.isThreaded()) {
                System.out.println("Engine: " + this.engine.frameRate + " "
                        + "Render: " + this.applet.frameRate);
            } else {
                System.out.println("Framerate: " + this.applet.frameRate);
            }
        }

        this.timer.drawNanos = System.nanoTime() - drawStart;
    }

    private void keyEvent(LXKeyEvent keyEvent) {
        char keyChar = keyEvent.getKeyChar();
        int keyCode = keyEvent.getKeyCode();
        LXKeyEvent.Action action = keyEvent.getAction();
        if (action == LXKeyEvent.Action.RELEASED) {
            this.ui.keyReleased(keyEvent, keyChar, keyCode);

            switch (Character.toLowerCase(keyChar)) {
            case '[':
                this.engine.goPrev();
                break;
            case ']':
                this.engine.goNext();
                break;
            case 'f':
                this.flags.showFramerate = false;
                break;
            case ' ':
                if (this.flags.keyboardTempo) {
                    this.tempo.tap();
                }
                break;
            case 's':
                this.desaturation.disable();
                break;
            case '/':
                this.flash.disable();
                break;
            }
        } else if (action == LXKeyEvent.Action.PRESSED) {
            this.ui.keyPressed(keyEvent, keyChar, keyCode);
            switch (keyCode) {
            case java.awt.event.KeyEvent.VK_UP:
                if (keyEvent.isMetaDown()) {
                    this.engine.goPrev();
                }
                break;
            case java.awt.event.KeyEvent.VK_DOWN:
                if (keyEvent.isMetaDown()) {
                    this.engine.goNext();
                }
                break;
            case java.awt.event.KeyEvent.VK_LEFT:
                if (this.flags.keyboardTempo) {
                    this.tempo.setBpm(this.tempo.bpm() - .1);
                }
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                if (this.flags.keyboardTempo) {
                    this.tempo.setBpm(this.tempo.bpm() + .1);
                }
                break;
            }
            switch (keyChar) {
            case 'f':
                this.flags.showFramerate = true;
                break;
            case 's':
                this.desaturation.enable();
                break;
            case '/':
                this.flash.enable();
                break;
            }
        } else if (action == LXKeyEvent.Action.TYPED) {
            this.ui.keyTyped(keyEvent, keyChar, keyCode);
        }
    }

    private static enum MouseEventType {
        PRESSED, RELEASED, CLICKED, DRAGGED, MOVED,
    };

    private void mouseEvent1x(java.awt.event.MouseEvent e) {
        MouseEventType type;
        switch (e.getID()) {
        case java.awt.event.MouseEvent.MOUSE_PRESSED:
            type = MouseEventType.PRESSED;
            break;
        case java.awt.event.MouseEvent.MOUSE_RELEASED:
            type = MouseEventType.RELEASED;
            break;
        case java.awt.event.MouseEvent.MOUSE_CLICKED:
            type = MouseEventType.CLICKED;
            break;
        case java.awt.event.MouseEvent.MOUSE_DRAGGED:
            type = MouseEventType.DRAGGED;
            break;
        default:
            return;
        }
        mouseEvent(type, e.getX(), e.getY());
    }

    private void mouseEvent2x(processing.event.MouseEvent e) {
        MouseEventType type;
        switch (e.getAction()) {
        case processing.event.MouseEvent.WHEEL:
            this.ui.mouseWheel(e.getX(), e.getY(), e.getCount());
            return;
        case processing.event.MouseEvent.PRESS:
            type = MouseEventType.PRESSED;
            break;
        case processing.event.MouseEvent.RELEASE:
            type = MouseEventType.RELEASED;
            break;
        case processing.event.MouseEvent.CLICK:
            type = MouseEventType.CLICKED;
            break;
        case processing.event.MouseEvent.DRAG:
            type = MouseEventType.DRAGGED;
            break;
        default:
            return;
        }
        mouseEvent(type, e.getX(), e.getY());
    }

    private void mouseEvent(MouseEventType type, int x, int y) {
        switch (type) {
        case PRESSED:
            this.ui.mousePressed(x, y);
            break;
        case RELEASED:
            this.ui.mouseReleased(x, y);
            break;
        case CLICKED:
            this.ui.mouseClicked(x, y);
            break;
        case DRAGGED:
            this.ui.mouseDragged(x, y);
            break;
        case MOVED:
            break;
        default:
            break;
        }
    }

    public final PGraphics getGraphics() {
        return this.applet.g;
    }
}
