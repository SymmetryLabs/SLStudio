package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.KaledoscopeOutput;
import art.lookingup.ParameterFile;
import art.lookingup.PreviewComponents;
import art.lookingup.ui.*;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.p3lx.ui.UIEventHandler;
import heronarts.p3lx.ui.studio.mixer.UIMixerStrip;
import processing.event.KeyEvent;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FireflyShow implements Show {
    private static final Logger logger = Logger.getLogger(FireflyShow.class.getName());

    public final static String SHOW_NAME = "firefly";

    public static UIPixliteConfig pixliteConfig;
    public static MappingConfig mappingConfig;
    public static StrandLengths strandLengths;
    public static RunsConfig runsConfig;
    UIPreviewComponents previewComponents;
    public static PreviewComponents.Axes axes;

    // This are values stored in parameter files that we need to load before we build the UI.  These are
    // pre-requisites for the model construction.
    public static ParameterFile runsConfigParams;
    public static ParameterFile strandLengthsParams;
    static public int runsButterflies;
    static public int runsFlowers;
    static public List<Integer> allStrandLengths;

    /**
     * These are parameters we need for building the model. We bind the UI to these ParameterFile's
     * in onUIReady.
     * We need to know the number of butterfly runs, the number of flower runs, and for each strand, the length
     * of that strand in LEDs, which will tell us the number of fixtures.
     * TODO(tracy): It would be better to have a strand type and then just list the number of fixtures on that
     * strand from which we can compute the number of LEDs.
     */
    public void loadModelParams() {
        runsConfigParams = ParameterFile.instantiateAndLoad(RunsConfig.filename);
        strandLengthsParams = ParameterFile.instantiateAndLoad(StrandLengths.filename);
        runsButterflies = Integer.parseInt(runsConfigParams.getStringParameter(RunsConfig.BUTTERFLY_RUNS, "3").getString());
        runsFlowers = Integer.parseInt(runsConfigParams.getStringParameter(RunsConfig.FLOWER_RUNS, "4").getString());
        allStrandLengths = StrandLengths.getAllStrandLengths(strandLengthsParams);
    }

    public SLModel buildModel() {
        loadModelParams();
        return KaledoscopeModel.createModel(3, 2, 20);
    }

    public void setupLx(final LX lx) {
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        axes = new PreviewComponents.Axes();
        ui.preview.addComponent(axes);
        previewComponents = (UIPreviewComponents) new UIPreviewComponents(ui).setExpanded(true).addToContainer(lx.ui.leftPane.global);
        pixliteConfig = (UIPixliteConfig) new UIPixliteConfig(ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);
        mappingConfig = (MappingConfig) new MappingConfig(lx.ui, lx).setExpanded(false).addToContainer(lx.ui.leftPane.global);
        runsConfig = (RunsConfig) new RunsConfig(lx.ui, lx, runsConfigParams).setExpanded(false).addToContainer(lx.ui.leftPane.global);
        strandLengths = (StrandLengths) new StrandLengths(lx.ui, lx, strandLengthsParams).setExpanded(false).addToContainer(lx.ui.leftPane.global);

        KaledoscopeOutput.configurePixliteOutput(lx);
        lx.ui.setTopLevelKeyEventHandler(new TopLevelKeyEventHandler(lx));
    }

    public class TopLevelKeyEventHandler extends UIEventHandler {
        LX lx;

        TopLevelKeyEventHandler(LX lx) {
            super();
            this.lx = lx;
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
            KaledoscopeModel.Point curPt = PreviewComponents.Axes.getCurSelPt();
            KaledoscopeModel.Bezier prevBezier = null;
            KaledoscopeModel.Bezier curBezier = PreviewComponents.Axes.getCurBezier();
            KaledoscopeModel.Bezier nextBezier = null;
            KaledoscopeModel.Run curRun = PreviewComponents.Axes.getCurSelRun();
            float moveIncr = 12.0f;
            boolean needsUpdate = false;
            logger.info("Received key char: " + keyChar);
            // If we are changing an end point control point we should also adjust
            // the start point of the following curve.
            int selCtrlPt = PreviewComponents.Axes.selectedCtrlPt;
            if (selCtrlPt == 3 || selCtrlPt == 2) {
                if (PreviewComponents.Axes.selectedBezier != 3) {
                    nextBezier = curRun.beziers.get(PreviewComponents.Axes.selectedBezier + 1);
                }
            }
            if (selCtrlPt == 0 || selCtrlPt == 1) {
                if (PreviewComponents.Axes.selectedBezier != 0) {
                    prevBezier = curRun.beziers.get(PreviewComponents.Axes.selectedBezier - 1);
                }
            }
            if (keyEvent.isControlDown()) {
                moveIncr *= 0.1;
            }
            if (keyChar == 'j') {
                curPt.x = curPt.x - moveIncr;
                if (nextBezier != null) {
                    if (selCtrlPt == 3) nextBezier.start.x = nextBezier.start.x - moveIncr;
                    if (selCtrlPt == 2) nextBezier.c1.x = nextBezier.c1.x + moveIncr;
                }
                if (prevBezier != null) {
                    if (selCtrlPt == 0) prevBezier.end.x = prevBezier.end.x - moveIncr;
                    if (selCtrlPt == 1) prevBezier.c2.x = prevBezier.c2.x + moveIncr;
                }
                needsUpdate = true;
            } else if (keyChar == 'k') {
                curPt.x = curPt.x + moveIncr;
                if (nextBezier != null) {
                    if (selCtrlPt == 3) nextBezier.start.x = nextBezier.start.x + moveIncr;
                    if (selCtrlPt == 2) nextBezier.c1.x = nextBezier.c1.x - moveIncr;
                }
                if (prevBezier != null) {
                    if (selCtrlPt == 0) prevBezier.end.x = prevBezier.end.x + moveIncr;
                    if (selCtrlPt == 1) prevBezier.c2.x = prevBezier.c2.x - moveIncr;
                }
                needsUpdate = true;
            } else if (keyChar == 'i') {
                curPt.y = curPt.y + moveIncr;
                if (nextBezier != null) {
                    if (selCtrlPt == 3) nextBezier.start.y = nextBezier.start.y + moveIncr;
                    if (selCtrlPt == 2) nextBezier.c1.y = nextBezier.c1.y - moveIncr;
                }
                if (prevBezier != null) {
                    if (selCtrlPt == 0) prevBezier.end.y = prevBezier.end.y + moveIncr;
                    if (selCtrlPt == 1) prevBezier.c2.y = prevBezier.c2.y - moveIncr;
                }
                needsUpdate = true;
            } else if (keyChar == 'm') {
                curPt.y = curPt.y - moveIncr;
                if (nextBezier != null) {
                    if (selCtrlPt == 3) nextBezier.start.y = nextBezier.start.y - moveIncr;
                    if (selCtrlPt == 2) nextBezier.c1.y = nextBezier.c1.y + moveIncr;
                }
                if (prevBezier != null) {
                    if (selCtrlPt == 0) prevBezier.end.y = prevBezier.end.y - moveIncr;
                    if (selCtrlPt == 1) prevBezier.c2.y = prevBezier.c2.y + moveIncr;
                }
                needsUpdate = true;
            }
            // If needsUpdate, recompute all LXPoint positions!
            if (needsUpdate) {
                curBezier.computeArcLengths();
                KaledoscopeModel.recomputeRunBezier(curRun);
                lx.model.update(true, true);
            }

        }
    }
}

