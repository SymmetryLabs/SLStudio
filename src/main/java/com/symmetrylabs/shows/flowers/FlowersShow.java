package com.symmetrylabs.shows.flowers;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import java.util.ArrayList;
import java.util.List;

public class FlowersShow implements Show {
    public static final String SHOW_NAME = "flowers";

    @Override
    public SLModel buildModel() {
        return FlowersModelLoader.load();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UIFlowerTool.attach(lx, ui);
    }
}
