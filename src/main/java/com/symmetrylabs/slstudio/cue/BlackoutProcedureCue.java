package com.symmetrylabs.slstudio.cue;

import com.symmetrylabs.shows.base.SLShow;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.slstudio.ui.v2.UI;
import com.symmetrylabs.util.hardware.powerMon.ControllerWithPowerFeedback;
import heronarts.lx.LX;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.FixedParameter;

import java.util.Collection;

public class BlackoutProcedureCue extends Cue {
//    public BooleanParameter trigger = new BooleanParameter("force procedure run", false).setMode(BooleanParameter.Mode.MOMENTARY);
    LX lx;
    public DiscreteParameter blackoutThreshhold = new DiscreteParameter("blackout threshhold", 280, 200, 400);
    public DiscreteParameter delayBeforeCuttoff = new DiscreteParameter("precuttoff delay", 200, 100, 1000);
    public BlackoutProcedureCue(LX lx, BoundedParameter cuedParameter) {
        super(cuedParameter);
        isHourly = true; // set Static by Nathan.  That's what he needs in Oslo.
        this.lx = lx;
    }

    public void execute(){
        // fade brightness to zero, ensure nothing else is running...
        lx.engine.addTask(()->{

            double startValue = lx.engine.output.brightness.getValue();
            lx.engine.output.brightness.setValue(0);

            // simple wait.. not sure how to instantiate a modulator and do a nice fade...
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < delayBeforeCuttoff.getValuei()) {
                // give some tie for outputs to settle
            }

            SLShow show = SLShow.getInstance(lx);
            Collection<DiscoverableController> ccs = show.getSortedControllers();
            for (DiscoverableController dc : ccs) {
                if (dc instanceof ControllerWithPowerFeedback) {
                    ((ControllerWithPowerFeedback) dc).enableBlackoutProcedure(true);
                    ((ControllerWithPowerFeedback) dc).setBlackoutThreshhold(blackoutThreshhold.getValuei()); // easy static value for starters
                    ((ControllerWithPowerFeedback) dc).killByThreshHold();
                }
            }

            show.allPortsPowerEnableMask.loadControllerSetMaskStateTo_RAM(ccs);

            lx.engine.output.brightness.setValue(startValue);

            // should probably upload this data to some sort of server... for now just see that it works.

        });
    }
}
