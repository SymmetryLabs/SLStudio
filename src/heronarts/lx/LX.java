/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXPalette;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.IteratorTestPattern;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transition.LXTransition;

import java.util.ArrayList;
import java.util.List;

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

    public static class InitTimer {
        private long lastTime;

        protected void init() {
            this.lastTime = System.nanoTime();
        }

        public void log(String label) {
            long thisTime = System.nanoTime();
            if (LX.LOG_INIT_TIMING) {
                System.out.println(String.format("[LX init: %s: %.2fms]", label, (thisTime - lastTime) / 1000000.));
            }
            this.lastTime = thisTime;
        }
    }

    public static final InitTimer initTimer = new InitTimer();

    private static boolean LOG_INIT_TIMING = false;

    public static void logInitTiming() {
        LX.LOG_INIT_TIMING = true;
    }

    /**
     * Listener for top-level events
     */
    public interface Listener {
        public void modelChanged(LX lx, LXModel model);
    }

    private final List<Listener> listeners = new ArrayList<Listener>();

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
     * The default palette.
     */
    public final LXPalette palette;

    /**
     * The animation engine.
     */
    public final LXEngine engine;

    /**
     * The global tempo object.
     */
    public final Tempo tempo;

    /**
     * The global audio input.
     */
    public final LXAudio audio;

    /**
     * Global modulator for shared base hue.
     */
    private LXParameter baseHue;

    private boolean baseHueIsInternalModulator = false;

    /**
     * Creates an LX instance with no nodes.
     */
    public LX() {
        this(null);
    }

    /**
     * Creates an LX instance. This instance will run patterns for a grid of the
     * specified size.
     *
     * @param total Number of nodes
     */
    public LX(int total) {
        this(total, 1);
    }

    /**
     * Creates a LX instance. This instance will run patterns for a grid of the
     * specified size.
     *
     * @param width Width
     * @param height Height
     */
    public LX(int width, int height) {
        this(new GridModel(width, height));
    }

    /**
     * Constructs an LX instance with the given pixel model
     *
     * @param model Pixel model
     */
    public LX(LXModel model) {
        LX.initTimer.init();
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
        LX.initTimer.log("Model");

        // Color palette
        this.palette = new LXPalette(this);
        LX.initTimer.log("Palette");

        // Construct the engine
        this.engine = new LXEngine(this);
        LX.initTimer.log("Engine");

        // Add a default channel
        this.engine.addChannel(new LXPattern[] { new IteratorTestPattern(this) }).fader.setValue(1);
        LX.initTimer.log("Default Channel");

        // Base Hue (deprecated)
        this.baseHue = null;
        this.cycleBaseHue(30000);
        LX.initTimer.log("Base Hue");

        this.tempo = new Tempo(this);
        LX.initTimer.log("Tempo");

        this.audio = new LXAudio(this);
        LX.initTimer.log("Audio");
    }

    public LX addListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LX removeListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    /**
     * Shut down resources of the LX instance.
     */
    public void dispose() {
        this.audio.dispose();
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
     * @param i Node index
     * @return Position of this node in x space, from 0 to 1
     */
    public double xn(int i) {
        return (i % this.width) / (double) (this.width - 1);
    }

    /**
     * Utility function to return the position of an index in x coordinate space
     * normalized from 0 to 1, as a floating point.
     *
     * @param i Node index
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
     * @param i Node index
     * @return Position of this node in y space, from 0 to 1
     */
    public double yn(int i) {
        return (i / this.width) / (double) (this.height - 1);
    }

    /**
     * Utility function to return the position of an index in y coordinate space
     * normalized from 0 to 1, as a floating point.
     *
     * @param i Node index
     * @return Position of this node in y space, from 0 to 1
     */
    public float ynf(int i) {
        return (float) this.yn(i);
    }

    /**
     * Shorthand for LXColor.hsb()
     *
     * @param h Hue 0-360
     * @param s Saturation 0-100
     * @param b Brightness 0-100
     * @return Color
     */
    public static int hsb(float h, float s, float b) {
        return LXColor.hsb(h, s, b);
    }

    /**
     * Shorthand for LXColor.hsa()
     *
     * @param h Hue 0-360
     * @param s Saturation 0-100
     * @param a Alpha 0-1
     * @return Color
     */
    public static int hsa(float h, float s, float a) {
        return LXColor.hsba(h, s, 100, a);
    }

    /**
     * Sets the speed of the entire system. Default is 1.0, any modification will
     * mutate deltaMs values system-wide.
     *
     * @param speed Coefficient, 1 is normal speed
     * @return this
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
     * @param effects Array of effects
     * @return this
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
     * @param effect Effect
     * @return this
     */
    public LX addEffect(LXEffect effect) {
        this.engine.addEffect(effect);
        return this;
    }

    /**
     * Remove an effect from the chain
     *
     * @param effect Effect
     * @return this
     */
    public LX removeEffect(LXEffect effect) {
        this.engine.removeEffect(effect);
        return this;
    }

    /**
     * Add a generic modulator to the engine
     *
     * @param modulator Modulator
     * @return The modulator that was added
     */
    public LXModulator addModulator(LXModulator modulator) {
        this.engine.addModulator(modulator);
        return modulator;
    }

    /**
     * Remove a modulator from the engine
     *
     * @param modulator Modulator
     * @return The modulator that was removed
     */
    public LXModulator removeModulator(LXModulator modulator) {
        this.engine.removeModulator(modulator);
        return modulator;
    }

    /**
     * Pause the engine from running
     *
     * @param paused Whether to pause the engine to pause
     * @return this
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
     *
     * @return this
     */
    public LX togglePaused() {
        return setPaused(!this.engine.isPaused());
    }

    /**
     * Sets the main channel to the previous pattern.
     *
     * @return this
     */
    public LX goPrev() {
        this.engine.goPrev();
        return this;
    }

    /**
     * Sets the main channel to the next pattern.
     *
     * @return this
     */
    public LX goNext() {
        this.engine.goNext();
        return this;
    }

    /**
     * Sets the main channel to a given pattern instance.
     *
     * @param pattern The pattern instance to run
     * @return this
     */
    public LX goPattern(LXPattern pattern) {
        this.engine.goPattern(pattern);
        return this;
    }

    /**
     * Sets the main channel to a pattern of the given index
     *
     * @param i Index of the pattern to run
     * @return this
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

    @Deprecated
    private LXModulator internalBaseHue(LXModulator modulator) {
        clearBaseHue();
        this.engine.addModulator(modulator);
        this.baseHue = modulator;
        this.baseHueIsInternalModulator = true;
        return modulator;
    }

    /**
     * Sets the base hue to be a parameter
     *
     * @param parameter Parameter to control base hue
     * @return this
     */
    @Deprecated
    public LX setBaseHue(LXParameter parameter) {
        clearBaseHue();
        this.baseHue = parameter;
        return this;
    }

    /**
     * Sets the base hue to a fixed value
     *
     * @param hue Fixed value to set hue to, 0-360
     * @return this
     */
    @Deprecated
    public LX setBaseHue(double hue) {
        internalBaseHue(new LinearEnvelope(this.getBaseHue(), hue, 50)).start();
        return this;
    }

    /**
     * Sets the base hue to cycle through the spectrum
     *
     * @param duration Number of milliseconds for hue cycle
     * @return this
     */
    @Deprecated
    public LX cycleBaseHue(double duration) {
        internalBaseHue(new SawLFO(0, 360, duration).setValue(getBaseHue())).start();
        return this;
    }

    /**
     * Sets the base hue to oscillate between two spectrum values
     *
     * @param lowHue Low hue value
     * @param highHue High hue value
     * @param duration Milliseconds for hue oscillation
     * @return this
     */
    @Deprecated
    public LX oscillateBaseHue(double lowHue, double highHue, double duration) {
        internalBaseHue(new TriangleLFO(lowHue, highHue, duration).setValue(
                getBaseHue())).trigger();
        return this;
    }

    /**
     * Stops patterns from automatically rotating
     *
     * @return this
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
     * @return this
     */
    public LX enableAutoTransition(int autoTransitionThreshold) {
        this.engine.enableAutoTransition(autoTransitionThreshold);
        return this;
    }

    /**
     * Adds an output driver
     *
     * @param output Output
     * @return this
     */
    public LX addOutput(LXOutput output) {
        this.engine.addOutput(output);
        return this;
    }

    /**
     * Specifies the set of patterns to be run.
     *
     * @param patterns Array of patterns
     * @return this
     */
    public LX setPatterns(LXPattern[] patterns) {
        this.engine.setPatterns(patterns);
        return this;
    }

    /**
     * Gets the current set of patterns on the main channel.
     *
     * @return The list of patters
     */
    public List<LXPattern> getPatterns() {
        return this.engine.getPatterns();
    }


}
