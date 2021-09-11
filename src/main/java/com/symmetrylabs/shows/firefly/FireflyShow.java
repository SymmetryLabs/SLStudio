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

import java.util.List;

public class FireflyShow implements Show {
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
        previewComponents = (UIPreviewComponents) new UIPreviewComponents(ui).addToContainer(lx.ui.leftPane.global);

        pixliteConfig = (UIPixliteConfig) new UIPixliteConfig(ui, lx).addToContainer(lx.ui.leftPane.global);
        mappingConfig = (MappingConfig) new MappingConfig(lx.ui, lx).addToContainer(lx.ui.leftPane.global);
        runsConfig = (RunsConfig) new RunsConfig(lx.ui, lx, runsConfigParams).addToContainer(lx.ui.leftPane.global);
        strandLengths = (StrandLengths) new StrandLengths(lx.ui, lx, strandLengthsParams).addToContainer(lx.ui.leftPane.global);

        KaledoscopeOutput.configurePixliteOutput(lx);
    }
}

