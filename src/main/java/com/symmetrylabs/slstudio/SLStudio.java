package com.symmetrylabs.slstudio;

import java.util.Map;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.layouts.cubes.CubesLayout;
import com.symmetrylabs.layouts.dynamic_JSON.DynamicLayout;
import com.symmetrylabs.layouts.oslo.OsloLayout;
import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.slstudio.output.MappingPixlite;
import heronarts.lx.LX;
import com.symmetrylabs.layouts.LayoutRegistry;
import processing.core.PApplet;

import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.output.OPCOutput;

import com.symmetrylabs.slstudio.mappings.Mappings;
import com.symmetrylabs.slstudio.output.OutputControl;
import com.symmetrylabs.slstudio.palettes.ArrayPalette;
import com.symmetrylabs.slstudio.palettes.ImageLibrary;
import com.symmetrylabs.slstudio.palettes.LinePaletteExtractor;
import com.symmetrylabs.slstudio.palettes.PaletteExtractor;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import com.symmetrylabs.slstudio.performance.APC40Listener;
import com.symmetrylabs.slstudio.performance.FoxListener;
import com.symmetrylabs.slstudio.performance.PerformanceManager;
import com.symmetrylabs.slstudio.ui.UISpeed;
import com.symmetrylabs.util.BlobTracker;
import com.symmetrylabs.util.DrawHelper;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.Utils;

import static com.symmetrylabs.util.DistanceConstants.*;

public class SLStudio extends PApplet {
    public static SLStudio applet;

    private SLStudioLX lx;
    private Layout layout;
    private Dispatcher dispatcher;
    private Mappings mappings;
    public OutputControl outputControl;
    public MappingPixlite[] mappingPixlites;
    public APC40Listener apc40Listener;
    public PerformanceManager performanceManager;
    private BlobTracker blobTracker;
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

    @Override
    public void setup() {
        long setupStart = System.nanoTime();
        applet = this;

        Utils.setSketchPath(sketchPath());

        layout = LayoutRegistry.getLayout(this, args.length > 0 ? args[0] : null);
        LXModel model = layout.buildModel();
        printModelStats(model);

        new SLStudioLX(this, model, true) {

            @Override
            protected void initialize(SLStudioLX lx, SLStudioLX.UI ui) {
                SLStudio.this.lx = lx;
                super.initialize(lx, ui);

                SLStudio.this.dispatcher = Dispatcher.getInstance(lx);

                layout.setupLx(lx);

                lx.addOutput(new OPCOutput(lx, "localhost", 11122));

                outputControl = new OutputControl(lx);
                lx.engine.registerComponent("outputControl", outputControl);
                mappingPixlites = setupPixlites();

                SLStudio.this.apc40Listener = new APC40Listener(lx);
                new FoxListener(lx);

                SLStudio.this.performanceManager = new PerformanceManager(lx);
                lx.engine.registerComponent("performanceManager", performanceManager);

                blobTracker = BlobTracker.getInstance(lx);

                ui.theme.setPrimaryColor(0xff008ba0);
                ui.theme.setSecondaryColor(0xff00a08b);
                ui.theme.setAttentionColor(0xffa00044);
                ui.theme.setFocusColor(0xff0094aa);
                ui.theme.setControlBorderColor(0xff292929);
            }

            @Override
            protected void onUIReady(SLStudioLX lx, SLStudioLX.UI ui) {
                ui.leftPane.audio.setVisible(true);
                ui.preview.setCenter(lx.model.cx, lx.model.cy, lx.model.cz);
                ui.preview.setPhi(0).setMinRadius(0 * FEET).setMaxRadius(150 * FEET).setRadius(150 * FEET);
                new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);

                layout.setupUi(lx, ui);
            }
        };

        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.audio.enabled.setValue(false);
        lx.engine.output.enabled.setValue(true);

        performanceManager.start(lx.ui);

        long setupFinish = System.nanoTime();
        println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms");
    }

    void printModelStats(LXModel model) {
        println("-- Model ----");
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
}
