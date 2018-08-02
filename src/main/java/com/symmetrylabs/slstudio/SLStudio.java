package com.symmetrylabs.slstudio;

import java.util.Map;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.layouts.tree.Anemometer;
import com.symmetrylabs.slstudio.output.MappingPixlite;
import heronarts.lx.LX;
import com.symmetrylabs.layouts.LayoutRegistry;
import com.symmetrylabs.layouts.tree.TreeModelingTool;
import com.symmetrylabs.layouts.tree.ui.*;
import processing.core.PApplet;

import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;
import processing.core.PFont;

import com.symmetrylabs.slstudio.mappings.Mappings;
import com.symmetrylabs.slstudio.output.OutputControl;
import com.symmetrylabs.slstudio.performance.APC40Listener;
import com.symmetrylabs.slstudio.performance.FoxListener;
import com.symmetrylabs.slstudio.performance.PerformanceManager;
import com.symmetrylabs.slstudio.ui.UISpeed;
import com.symmetrylabs.slstudio.ui.UIFramerateControl;
import com.symmetrylabs.layouts.tree.ui.UITreeModelingTool;
import com.symmetrylabs.layouts.tree.ui.UITreeModelAxes;
import com.symmetrylabs.util.BlobTracker;
import com.symmetrylabs.util.DrawHelper;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.Utils;

import static com.symmetrylabs.util.DistanceConstants.*;

public class SLStudio extends PApplet {
    public static SLStudio applet;
    public static final Font MONO_FONT = new Font("Inconsolata-Bold-14.vlw", 14);
    public static final String LAYOUT_FILE_NAME = ".layout";
    public static final String RESTART_FILE_NAME = ".restart";

    private SLStudioLX lx;
    public Layout layout;
    private Dispatcher dispatcher;
    private Mappings mappings;
    public OutputControl outputControl;
    public MappingPixlite[] mappingPixlites;
    public APC40Listener apc40Listener;
    public PerformanceManager performanceManager;
    private BlobTracker blobTracker;
    public TreeModelingTool treeModelingTool;
    public UITreeModelingTool uiTreeModelingTool = null;
    public UITreeModelAxes uiTreeModelAxes = null;
    public Anemometer anemometer;
    public LX lx_OG;

    public final BooleanParameter mappingModeEnabled = new BooleanParameter("Mappings");
    public Map<String, int[]> mappingColorsPerPixlite;

    static public void main(String[] args) {
        System.setProperty("com.aparapi.enableShowGeneratedOpenCL", "true");
        System.setProperty("com.aparapi.dumpProfilesOnExit", "true");
        PApplet.main(concat(new String[] { SLStudio.class.getName() }, args));
    }

    @Override
    public void settings() {
        size(displayWidth, displayHeight, P3D);
    }

    /** Gets the layout name from the -Playout= argument or .layout file. */
    public String getSelectedLayoutName() {
        String layoutName = System.getProperty("com.symmetrylabs.layout");
        if (layoutName != null && !layoutName.isEmpty()) return layoutName;
        String[] lines = loadStrings(LAYOUT_FILE_NAME);
        if (lines != null && lines.length > 0) return lines[0].trim();
        return null;
    }

    /** Writes out a layout name as the default layout on next startup. */
    public void saveSelectedLayoutName(String layoutName) {
        if (layoutName != null) {
            saveStrings(LAYOUT_FILE_NAME, new String[] {layoutName});
        }
    }

    @Override
    public void setup() {
        long setupStart = System.nanoTime();
        applet = this;

        Utils.setSketchPath(sketchPath());

        String layoutName = getSelectedLayoutName();
        saveSelectedLayoutName(layoutName);
        println("\n---- Layout: " + layoutName + " ----");

        layout = LayoutRegistry.getLayout(this, layoutName);
        LXModel model = layout.buildModel();
        printModelStats(model);

        new SLStudioLX(this, model, true) {

            @Override
            protected void initialize(SLStudioLX lx, SLStudioLX.UI ui) {
                SLStudio.this.lx = lx;
                super.initialize(lx, ui);

                SLStudio.this.dispatcher = Dispatcher.getInstance(lx);

                layout.setupLx(lx);

                if (TreeModelingTool.isTreeLayout()) {
                    treeModelingTool = new TreeModelingTool(lx);
                    lx.engine.registerComponent("treeModelingTool", treeModelingTool);

                    anemometer = new Anemometer();
                    lx.engine.modulation.addModulator(anemometer.speedModulator);
                    lx.engine.modulation.addModulator(anemometer.directionModulator);
                    lx.engine.registerComponent("anemomter", anemometer);
                    lx.engine.addLoopTask(anemometer);
                    anemometer.start();
                }

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
                ui.preview.setPhi(0).setMinRadius(0 * FEET).setMaxRadius(150 * FEET).setRadius(150 * FEET);
                new UIFramerateControl(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);
                new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 2);

                if (TreeModelingTool.isTreeLayout()) {
                    ui.preview.addComponent(new UITreeTrunk(applet));
                    uiTreeModelAxes = new UITreeModelAxes();
                    ui.preview.addComponent(uiTreeModelAxes);
                }

                layout.setupUi(lx, ui);
            }
        };

        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.audio.enabled.setValue(false);
        lx.engine.output.enabled.setValue(true);
        lx.engine.framesPerSecond.setValue(120);

    //performanceManager.start(lx.ui);

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

        public Font(String filename, int size) {
            this.filename = filename;
            this.size = size;
            this.lineHeight = (int) (size * 1.4);
        }

        public PFont getFont() {
            if (font == null) {
                font = applet.loadFont(filename);
            }
            return font;
        }
    }
}
