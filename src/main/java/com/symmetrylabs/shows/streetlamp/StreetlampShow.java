package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;

public class StreetlampShow implements Show {
    @Override
    public SLModel buildModel() {
        return StreetlampModel.create();
    }

    @Override
    public void setupLx(SLStudioLX lx) {

    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {

    }
}
