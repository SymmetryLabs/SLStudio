package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.component.GammaExpander;
import com.symmetrylabs.slstudio.dmx.DmxUsbOutput;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.DummyOutput;
import com.symmetrylabs.slstudio.workspaces.Workspace;
import heronarts.p3lx.ui.component.UIKnob;

public class StreetlampShow implements Show, HasWorkspace {
    public static final String SHOW_NAME = "streetlamp";

        private Workspace workspace;

    @Override
    public SLModel buildModel() {
        return StreetlampModel.create();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
                GammaExpander g = GammaExpander.getInstance(lx);
                g.redGammaFactor.setValue(0.21);
                g.greenGammaFactor.setValue(0.37);
                g.blueGammaFactor.setValue(0.80);

        DmxUsbOutput output = new DmxUsbOutput(lx)
                        .setRGBWMode(DmxUsbOutput.RGBWMode.ADD_WHITE)
                        .setGammaExpander(g);

        output.setColorChannels(new int[] {
            DmxUsbOutput.RED,
            DmxUsbOutput.GREEN,
            DmxUsbOutput.BLUE,
            DmxUsbOutput.WHITE,
        });

        //lx.addOutput(new DummyOutput(lx));
        lx.addOutput(output);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
                workspace = new Workspace(lx, ui, "shows/streetlamp");
        ui.preview.addComponent(new StreetlampVisualizer(lx));
    }

        @Override
        public Workspace getWorkspace() {
                return workspace;
        }
}
