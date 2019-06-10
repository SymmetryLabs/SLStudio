package com.symmetrylabs.slstudio;

import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;

import com.symmetrylabs.slstudio.ui.UILayer;
import com.symmetrylabs.util.MarkerSource;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.shows.tree.Anemometer;
import com.symmetrylabs.slstudio.envelop.Envelop;
import com.symmetrylabs.slstudio.envelop.EnvelopOscListener;
import com.symmetrylabs.slstudio.mappings.Mappings;
import com.symmetrylabs.slstudio.midi.NotationXLListener;
import com.symmetrylabs.slstudio.output.MappingPixlite;
import com.symmetrylabs.slstudio.output.OutputControl;
import com.symmetrylabs.slstudio.performance.APC40Listener;
import com.symmetrylabs.slstudio.performance.FoxListener;
import com.symmetrylabs.slstudio.performance.PerformanceManager;
import com.symmetrylabs.slstudio.ui.UIFramerateControl;
import com.symmetrylabs.slstudio.ui.UISpeed;
import com.symmetrylabs.slstudio.ui.UIWorkspace;
import com.symmetrylabs.util.BlobTracker;
import com.symmetrylabs.util.DrawHelper;
import com.symmetrylabs.util.MarkerSource;
import com.symmetrylabs.util.Utils;
import com.symmetrylabs.util.dispatch.Dispatcher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;
import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.PJOGL;

import static com.symmetrylabs.util.DistanceConstants.FEET;

public class SLStudio extends PApplet {
    public static SLStudio applet;
    public static final Font MONO_FONT = new Font("Inconsolata-Bold-14.vlw", 14, 17);
    public static final String SHOW_FILE_NAME = ".show";
    public static final String RESTART_FILE_NAME = ".restart";
    public static final int ENVELOP_OSC_PORT = 3377;
    public static final String MUTE_FILE_NAME = "start-muted";

    public static final String MAPPINGS_DATA_PATH = System.getProperty("user.home") + "/symmetrylabs/mapping";

    private SLStudioLX lx;
    public Show show;
    public String showName;
    private Dispatcher dispatcher;
    private Mappings mappings;
    public OutputControl outputControl;
    public MappingPixlite[] mappingPixlites;
    public APC40Listener apc40Listener;
    public PerformanceManager performanceManager;
    private BlobTracker blobTracker;
    public Anemometer anemometer;
    public LX lx_OG;

    public final BooleanParameter mappingModeEnabled = new BooleanParameter("Mappings");
    public Map<String, int[]> mappingColorsPerPixlite;

    /** Persistent warning messages to be shown in the UI. */
    protected static final Map<String, String> warnings = new HashMap<>();

    static public void main(String[] args) {
        System.setProperty("com.aparapi.enableShowGeneratedOpenCL", "true");
        System.setProperty("com.aparapi.dumpProfilesOnExit", "true");
        PApplet.main(concat(args, new String[] { SLStudio.class.getName() }));
    }

    @Override
    public void settings() {
        size(displayWidth, displayHeight, P3D);
        PJOGL.setIcon("application.png");

        String hidpiprop = System.getProperty("com.symmetrylabs.hidpi");
        String osname = System.getProperty("os.name");
        boolean useHiDpi =
            hidpiprop != null ||
            displayDensity() == 2 && (osname != null && osname.startsWith("Windows"));
        System.out.println(
            String.format(
                "%s (dpi=%d os=%s)",
                useHiDpi ? "requesting high-DPI pixel density" : "using normal pixel density",
                displayDensity(), osname));
        pixelDensity(useHiDpi ? 2 : 1);
    }

    /** Gets the show name from the -Pshow= argument or .show file. */
    public String getSelectedShowName() {
        String showName = System.getProperty("com.symmetrylabs.show");
        if (showName != null && !showName.isEmpty()) return showName;
        String[] lines = loadStrings(SHOW_FILE_NAME);
        if (lines != null && lines.length > 0) return lines[0].trim();
        return null;
    }

    /** Writes out a show name as the default show on next startup. */
    public void saveSelectedShowName(String showName) {
        if (showName != null) {
            saveStrings(SHOW_FILE_NAME, new String[] {showName});
        }
    }

    @Override
    public void setup() {
        long setupStart = System.nanoTime();
        applet = this;

        Utils.setSketchPath(sketchPath());

        showName = getSelectedShowName();
        saveSelectedShowName(showName);
        println("\n---- Show: " + showName + " ----");

        show = ShowRegistry.getShow(this, showName);
        LXModel model = show.buildModel();
        printModelStats(model);

        new SLStudioLX(this, model, true) {

            @Override
            protected void initialize(SLStudioLX lx, SLStudioLX.UI ui) {
                SLStudio.this.lx = lx;
                super.initialize(lx, ui);

                SLStudio.this.dispatcher = Dispatcher.getInstance(lx);

                Envelop envelop = Envelop.getInstance(lx);
                lx.engine.registerComponent("envelop", envelop);
                lx.engine.addLoopTask(envelop);

                try {
                    lx.engine.osc.receiver(ENVELOP_OSC_PORT).addListener(new EnvelopOscListener(lx, envelop));
                } catch (SocketException sx) {
                    throw new RuntimeException(sx);
                }

                show.setupLx(lx);

                outputControl = new OutputControl(lx);
                lx.engine.registerComponent("outputControl", outputControl);
                mappingPixlites = setupPixlites();

                SLStudio.this.apc40Listener = new APC40Listener(lx);
                new FoxListener(lx);

                // SLStudio.this.performanceManager = new PerformanceManager(lx);
                // lx.engine.registerComponent("performanceManager", performanceManager);

                blobTracker = BlobTracker.getInstance(lx);

                ui.theme.setPrimaryColor(0xff008ba0);
                ui.theme.setSecondaryColor(0xff00a08b);
                ui.theme.setAttentionColor(0xffa00044);
                ui.theme.setFocusColor(0xff0094aa);
                ui.theme.setControlBorderColor(0xff292929);
                ui.theme.setDeviceFocusedBackgroundColor(0xff3E585A);
            }

            @Override
            protected void onUIReady(SLStudioLX lx, SLStudioLX.UI ui) {
                ui.leftPane.audio.setVisible(true);
                ui.preview.setCenter(lx.model.cx, lx.model.cy, lx.model.cz);
                ui.preview.setPhi(0).setMinRadius(0 * FEET).setMaxRadius(150 * FEET).setRadius(25 * FEET);
                new UIFramerateControl(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);
                new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 2);
                new UILayer(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 3);

                show.setupUi(lx, ui);

                if (show instanceof HasWorkspace) {
                    HasWorkspace hwShow = (HasWorkspace) show;
                    new UIWorkspace(ui, lx, hwShow.getWorkspace(), 0, 0, ui.leftPane.global.getContentWidth())
                        .addToContainer(ui.leftPane.global);
                }
                if (show instanceof MarkerSource) {
                    ui.markerPainter.addSource((MarkerSource) show);
                }

                lx.engine.midi.whenReady(new Runnable() {
                    @Override
                    public void run() {
                        NotationXLListener.bindMidi(lx, ui);
                    }
                });
            }
        };

        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.audio.enabled.setValue(false);

        /* don't mess with enabled if the mute file isn't there; output.enabled is
             set to the value stored in the project file */
        if (new File(MUTE_FILE_NAME).exists()) {
            lx.engine.output.enabled.setValue(false);
        }

        long setupFinish = System.nanoTime();
        println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms");
    }

    void printModelStats(LXModel model) {
        println("# of points: " + model.points.length);
        println("model.xMin: " + model.xMin);
        println("model.xMax: " + model.xMax);
        println("model.xRange: " + model.xRange);
        println("model.yMin: " + model.yMin);
        println("model.yMax: " + model.yMax);
        println("model.yRange: " + model.yRange);
        println("model.zMin: " + model.zMin);
        println("model.zMax: " + model.zMax);
        println("model.zRange: " + model.zRange + "\n");
    }

    public void draw() {
        background(lx.ui.theme.getDarkBackgroundColor());
        DrawHelper.runAll();
        dispatcher.draw();
    }

    public static void setWarning(String key, String message) {
        if (message != null && !message.isEmpty()) {
            System.err.println("WARNING: " + key + ": " + message);
            warnings.put(key, message);
        } else {
            warnings.remove(key);
        }
        if (applet != null && applet.lx != null && applet.lx.ui != null) {
            applet.lx.ui.updateWarningText(warnings);
        }
    }

    public void restart() {
        saveStrings(SLStudio.RESTART_FILE_NAME, new String[0]);
        exit();
    }

    private MappingPixlite[] setupPixlites() {
        return new MappingPixlite[0]; // todo
    }

    public final static int CHAN_WIDTH = 200;
    public final static int CHAN_HEIGHT = 650;
    public final static int CHAN_Y = 20;
    public final static int PAD = 5;

    public static class Font {
        public final String filename;
        public final int size;
        public final int lineHeight;
        private static PFont font = null;

        public Font(String filename, int size, int lineHeight) {
            this.filename = filename;
            this.size = size;
            this.lineHeight = lineHeight;
        }

        public PFont getFont() {
            if (font == null) {
                font = applet.loadFont(filename);
            }
            return font;
        }
    }
}
