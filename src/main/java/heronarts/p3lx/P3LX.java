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
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.p3lx.ui.UI;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import heronarts.lx.data.LXVersion;

import java.lang.reflect.Modifier;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

/**
 * Harness to run LX inside a Processing 2 sketch
 */
public class P3LX extends LX {

    public final static String VERSION = "##library.prettyVersion##";

    public static boolean isProcessing2X = false;

    /** The runtime version number for all p3lx-derived frontends (LXStudio, SLStudio), stored in project files. */
    public static LXVersion RUNTIME_VERSION = LXVersion.SLSTUDIO_WITH_LOOKS;

    public static final String DEFAULT_P3LX_MODEL_NAME = "P3LXDefaultModel";

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
     * The UI container.
     */
    public final UI ui;

    /**
     * Internal buffer for colors, owned by Processing animation thread.
     */
    private final PolyBuffer buffer;

    /** Color space for preview rendering.  Must be RGB8 or SRGB8. */
    private final PolyBuffer.Space UI_COLOR_SPACE = SRGB8;

    /**
     * The current frame's colors, either from the engine or the buffer. Note that
     * this is a reference to an array.
     */
    private int[] colors;

    public class Flags {
        public boolean keyboardTempo = false;
        public boolean showFramerate = false;
    }

    public final Flags flags = new Flags();

    public class Timer {
        public long drawNanos = 0;
        public long engineNanos = 0;
    }

    public final Timer timer = new Timer();

    public P3LX(PApplet applet) {
        this(applet, new LXModel(DEFAULT_P3LX_MODEL_NAME));
    }

    public P3LX(PApplet applet, LXModel model) {
        super(RUNTIME_VERSION, model);
        this.applet = applet;

        String sketchPath = applet.sketchPath();
        this.engine.script.setScriptPath(sketchPath);
        this.engine.audio.output.setMediaPath(sketchPath);

        for (Class<?> cls : applet.getClass().getDeclaredClasses()) {
            if (!Modifier.isAbstract(cls.getModifiers())) {
                if (LXPattern.class.isAssignableFrom(cls)) {
                    registerPattern(cls.asSubclass(LXPattern.class));
                } else if (LXEffect.class.isAssignableFrom(cls)) {
                    registerEffect(cls.asSubclass(LXEffect.class));
                }
            }
        }

        this.buffer = new PolyBuffer(this);
        this.colors = (int[]) buffer.getArray(UI_COLOR_SPACE);
        LX.initTimer.log("P3LX: ModelBuffer");

        this.ui = buildUI();
        LX.initTimer.log("P3LX: UI");

        applet.colorMode(PConstants.HSB, 360, 100, 100, 100);
        LX.initTimer.log("P3LX: colorMode");

        applet.registerMethod("draw", this);
        applet.registerMethod("dispose", this);
        LX.initTimer.log("P3LX: registerMethod");
    }

    /**
     * Subclass may override.
     *
     * @return UI
     */
    protected UI buildUI() {
        return new UI(this);
    }

    /**
     * Redundant, but making it obvious that Processing depends on this
     * method being named dispose(). This protects us from a rename in LX
     * where someone doesn't realize the Processing naming dependency.
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Enables the tempo to be controlled by the keyboard arrow keys. Left and
     * right arrows change the tempo by .1 BPM, and the space-bar taps the tempo.
     *
     * @return this
     */
    public LX enableKeyboardTempo() {
        this.flags.keyboardTempo = true;
        return this;
    }

    public final PGraphics getGraphics() {
        return this.applet.g;
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
     * Core function invoked by the processing engine on each iteration of the run
     * cycle.
     */
    public void draw() {
        long drawStart = System.nanoTime();

        long engineStart = System.nanoTime();
        String frameRateStr = "";

        // Give the engine a chance to sort itself out each frame
        this.engine.onDraw();

        if (this.engine.isThreadRunning()) {
            // NOTE: because we don't hold a lock, it is *possible* that the
            // engine stops being in threading mode just between these lines,
            // triggered by some action on the engine thread itself. It's okay
            // if this happens, worst side effect is the UI getting the last frame
            // from the copy buffer.
            this.engine.copyUIBuffer(this.buffer, UI_COLOR_SPACE);
            if (this.flags.showFramerate) {
                frameRateStr =
                    "Engine: " + this.engine.frameRate() + " " +
                    "UI: " + this.applet.frameRate;
                if (this.engine.isNetworkMultithreaded.isOn()) {
                    frameRateStr += " Network: " + this.engine.network.frameRate();
                }
            }
        } else {
            // If the engine is not threaded, then we run it ourselves, and
            // we can just use its color buffer, as there is no thread contention.
            // We don't need to worry about lock contention because we are
            // currently on the only thread that *could* start the engine.
            this.engine.run();
            this.colors = (int[]) this.engine.getUIPolyBufferNonThreadSafe().getArray(UI_COLOR_SPACE);
            if (this.flags.showFramerate) {
                frameRateStr = "Framerate: " + this.applet.frameRate;
                if (this.engine.isNetworkMultithreaded.isOn()) {
                    frameRateStr += " Network: " + this.engine.network.frameRate();
                }
            }
        }
        this.timer.engineNanos = System.nanoTime() - engineStart;

        // Print framerate
        if (this.flags.showFramerate) {
            PApplet.println(frameRateStr);
        }

        this.timer.drawNanos = System.nanoTime() - drawStart;
    }

    @Override
    protected LXEffect instantiateEffect(String className) {
        Class<? extends LXEffect> cls;
        try {
            cls = Class.forName(className).asSubclass(LXEffect.class);
        } catch (ClassNotFoundException cnfx) {
            System.err.println("No effect class found for " + className + ": " + cnfx.getLocalizedMessage());
            return null;
        }
        try {
            return cls.getConstructor(LX.class).newInstance(this);
        } catch (Exception x) {
            try {
                return cls.getConstructor(applet.getClass(), LX.class).newInstance(applet, this);
            } catch (Exception x2) {
                System.err.println("Effect instantiation failed: " + x2.getLocalizedMessage());
                return null;
            }
        }
    }

    @Override
    protected LXPattern instantiatePattern(String className) {
        Class<? extends LXPattern> cls;
        try {
            cls = Class.forName(className).asSubclass(LXPattern.class);
        } catch (ClassNotFoundException cnfx) {
            System.err.println("No pattern class found for " + className + ": " + cnfx.getLocalizedMessage());
            return null;
        }
        try {
            return cls.getConstructor(LX.class).newInstance(this);
        } catch (Exception x) {
            try {
                return cls.getConstructor(applet.getClass(), LX.class).newInstance(applet, this);
            } catch (Exception x2) {
                System.err.println("Pattern instantiation failed: " + x2.getLocalizedMessage());
                return null;
            }
        }
    }


}
