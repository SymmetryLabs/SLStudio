package com.symmetrylabs.shows.hblamp;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.AllPixelOutput;
import com.symmetrylabs.slstudio.output.AllPixelOutput.LedType;

public class HbLampShow implements Show {
    public static final String SHOW_NAME = "hblamp";

    @Override
    public SLModel buildModel() {
        return HbLampModel.create();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        // these strips are BGR
        lx.addOutput(new AllPixelOutput(lx, AllPixelOutput.LedType.APA102, new int[] {2, 1, 0}));
        AllPixelOutput.configureMasterOutput(lx);
    }
}
