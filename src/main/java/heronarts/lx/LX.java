/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import com.google.common.base.Preconditions;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXPalette;
import heronarts.lx.data.Project;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.LXOutput;
import heronarts.lx.pattern.IteratorTestPattern;
import heronarts.lx.warp.LXWarp;
import heronarts.lx.data.LegacyProjectLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;

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

    public static final double HALF_PI = Math.PI / 2.;
    public static final double TWO_PI = Math.PI * 2.;

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

    public interface ProjectListener {

        enum Change {
            NEW,
            SAVE,
            OPEN
        };

        public void projectChanged(Project project, Change change);
    }

    private final List<ProjectListener> projectListeners = new ArrayList<ProjectListener>();

    public final LXComponent.Registry componentRegistry = new LXComponent.Registry();

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

    /** The list of globally registered pattern classes */
    private final List<Class<? extends LXPattern>> registeredPatterns = new ArrayList<Class<? extends LXPattern>>();

    /** The list of globally registered warps */
    private final List<Class<? extends LXWarp>> registeredWarps = new ArrayList<Class<? extends LXWarp>>();

    /** The list of globally registered effects */
    private final List<Class<? extends LXEffect>> registeredEffects = new ArrayList<Class<? extends LXEffect>>();


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
            this.total = model.points.length;
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
        model.normalize();
        LX.initTimer.log("Model");

        // Color palette
        this.palette = new LXPalette(this);
        LX.initTimer.log("Palette");

        // Construct the engine
        this.engine = new LXEngine(this);
        LX.initTimer.log("Engine");

        // Midi
        this.engine.midi.initialize();

        // Tempo
        this.tempo = new Tempo(this);
        LX.initTimer.log("Tempo");

        // Add a default channel
        this.engine.addLook().channels.get(0).fader.setValue(1);
        LX.initTimer.log("Default Channel");

    }

    public LX addListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LX removeListener(Listener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LX addProjectListener(ProjectListener listener) {
        this.projectListeners.add(listener);
        return this;
    }

    public LX removeProjectListener(ProjectListener listener) {
        this.projectListeners.remove(listener);
        return this;
    }

    public LXComponent getProjectComponent(int projectId) {
        return this.componentRegistry.getProjectComponent(projectId);
    }

    /**
     * Shut down resources of the LX instance.
     */
    public void dispose() {
        this.engine.audio.dispose();
    }

    /**
     * Utility function to return the row of a given index
     *
     * @param i Index into colors array
     * @return Which row this index is in
     */
    public int row(int i) {
        return (this.width == 0) ? 0 : (i / this.width);
    }

    /**
     * Utility function to return the column of a given index
     *
     * @param i Index into colors array
     * @return Which column this index is in
     */
    public int column(int i) {
        return (this.width == 0) ? 0 : (i % this.width);
    }

    /**
     * Utility function to get the x-coordinate of a pixel
     *
     * @param i Node index
     * @return x coordinate
     */
    public int x(int i) {
        return (this.width == 0) ? 0 : (i % this.width);
    }

    /**
     * Utility function to return the position of an index in x coordinate space
     * normalized from 0 to 1.
     *
     * @param i Node index
     * @return Position of this node in x space, from 0 to 1
     */
    public double xn(int i) {
        return (this.width == 0) ? 0 : ((i % this.width) / (double) (this.width - 1));
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
        return (this.width == 0) ? 0 : (i / this.width);
    }

    /**
     * Utility function to return the position of an index in y coordinate space
     * normalized from 0 to 1.
     *
     * @param i Node index
     * @return Position of this node in y space, from 0 to 1
     */
    public double yn(int i) {
        return (this.width == 0) ? 0 : ((i / this.width) / (double) (this.height - 1));
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

    /** Alias for LXColor.rgb(). */
    public static int rgb(byte r, byte g, byte b) {
        return LXColor.rgb(r, g, b);
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
        this.engine.masterChannel.addEffect(effect);
        return this;
    }

    /**
     * Remove an effect from the chain
     *
     * @param effect Effect
     * @return this
     */
    public LX removeEffect(LXEffect effect) {
        this.engine.masterChannel.removeEffect(effect);
        return this;
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
     * Removes an output driver
     *
     * @param output Output
     * @return this
     */
    public LX removeOutput(LXOutput output) {
        this.engine.removeOutput(output);
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

    /**
     * Register a pattern class with the engine
     *
     * @param pattern
     * @return this
     */
    public LX registerPattern(Class<? extends LXPattern> pattern) {
        this.registeredPatterns.add(pattern);
        return this;
    }

    /**
     * Register a pattern class with the engine
     *
     * @param patterns
     * @return this
     */
    public LX registerPatterns(Class<LXPattern>[] patterns) {
        for (Class<LXPattern> pattern : patterns) {
            registerPattern(pattern);
        }
        return this;
    }

    /**
     * Gets the list of registered pattern classes
     *
     * @return Pattern classes
     */
    public List<Class<? extends LXPattern>> getRegisteredPatterns() {
        return this.registeredPatterns;
    }

    public void registerWarp(Class<? extends LXWarp> warp) {
        registeredWarps.add(warp);
    }

    public List<Class<? extends LXWarp>> getRegisteredWarps() { return registeredWarps; }

    /**
     * Register an effect class with the engine
     *
     * @param effect
     * @return this
     */
    public LX registerEffect(Class<? extends LXEffect> effect) {
        this.registeredEffects.add(effect);
        return this;
    }

    /**
     * Register an effect class with the engine
     *
     * @param effects
     * @return this
     */
    public LX registerEffects(Class<? extends LXEffect>[] effects) {
        for (Class<? extends LXEffect> effect : effects) {
            registerEffect(effect);
        }
        return this;
    }

    /**
     * Gets the list of registered effect classes
     *
     * @return Effect classes
     */
    public List<Class<? extends LXEffect>> getRegisteredEffects() {
        return this.registeredEffects;
    }

    private final Map<String, LXSerializable> externals = new HashMap<String, LXSerializable>();

    private final static String KEY_VERSION = "version";
    private final static String KEY_TIMESTAMP = "timestamp";
    private final static String KEY_ENGINE = "engine";
    private final static String KEY_EXTERNALS = "externals";

    private Project project;

    protected void setProject(Project project, ProjectListener.Change change) {
        this.project = project;
        for (ProjectListener projectListener : this.projectListeners) {
            projectListener.projectChanged(project, change);
        }
    }

    public Project getProject() {
        return project;
    }

    public void saveProject() {
        if (project != null) {
            this.saveProject(project);
        }
    }

    public void saveProject(Project newProject) {
        newProject.save(this);
        setProject(newProject, ProjectListener.Change.SAVE);
    }

    public void newProject() {
        this.componentRegistry.resetProject();
        this.engine.load(this, new JsonObject());
        setProject(null, ProjectListener.Change.NEW);
    }

    public LX registerExternal(String key, LXSerializable serializable) {
        if (this.externals.containsKey(key)) {
            throw new IllegalStateException("Duplicate external for key: " + key + " (already: " + serializable + ")");
        }
        this.externals.put(key,  serializable);
        return this;
    }

    public Map<String, LXSerializable> getExternals() {
        return Collections.unmodifiableMap(externals);
    }

    public void openProject(Project project) {
        project.load(this);
        setProject(project, ProjectListener.Change.OPEN);
    }

    private <T extends LXComponent> T instantiateComponent(String className, Class<T> type) {
        try {
            Class<? extends T> cls = Class.forName(className).asSubclass(type);
            return cls.getConstructor(LX.class).newInstance(this);
        } catch (Exception x) {
            System.err.println("Exception in instantiateComponent: " + x.getLocalizedMessage());
            x.printStackTrace();
        }
        return null;
    }

    protected LXPattern instantiatePattern(String className) {
        return instantiateComponent(className, LXPattern.class);
    }

    protected LXEffect instantiateEffect(String className) {
        return instantiateComponent(className, LXEffect.class);
    }

    protected LXWarp instantiateWarp(String className) {
        return instantiateComponent(className, LXWarp.class);
    }
}
