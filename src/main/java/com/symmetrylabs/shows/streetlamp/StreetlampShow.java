package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.dmx.DmxUsbOutput;
import com.symmetrylabs.slstudio.output.DummyOutput;

public class StreetlampShow implements Show {
    @Override
    public SLModel buildModel() {
        return StreetlampModel.create();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        DmxUsbOutput output = new DmxUsbOutput(lx);

        output.setColorChannels(new int[] {
            DmxUsbOutput.RED,
            DmxUsbOutput.GREEN,
            DmxUsbOutput.BLUE,
            DmxUsbOutput.WHITE,
        });

        lx.addOutput(new DummyOutput(lx));
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {

    }
}
