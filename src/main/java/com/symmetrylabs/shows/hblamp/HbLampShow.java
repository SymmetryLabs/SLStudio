package com.symmetrylabs.shows.hblamp;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.AllPixelOutput;

public class HbLampShow implements Show {
    public static final String SHOW_NAME = "hblamp";

    @Override
    public SLModel buildModel() {
        return HbLampModel.create();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        lx.addOutput(new AllPixelOutput(lx));
    }
}
