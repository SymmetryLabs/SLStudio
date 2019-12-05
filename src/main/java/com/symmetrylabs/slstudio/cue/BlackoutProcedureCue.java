package com.symmetrylabs.slstudio.cue;

import com.badlogic.gdx.utils.Timer;
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

import java.io.IOException;
import java.util.Collection;
import java.util.TimerTask;

public class BlackoutProcedureCue extends Cue {
//    public BooleanParameter trigger = new BooleanParameter("force procedure run", false).setMode(BooleanParameter.Mode.MOMENTARY);
    LX lx;
    public DiscreteParameter blackoutThreshhold = new DiscreteParameter("blackout threshhold", 280, 200, 400);
    public DiscreteParameter delayBeforeCuttoff = new DiscreteParameter("precuttoff delay", 400, 100, 1000);
    public BlackoutProcedureCue(LX lx, BoundedParameter cuedParameter) {
        super(cuedParameter);
        isHourly = true; // set Static by Nathan.  That's what he needs in Oslo.
        this.lx = lx;
    }

    public void execute(){
        // fade brightness to zero, ensure nothing else is running...
        double startValue = lx.engine.output.brightness.getValue();

        lx.engine.addTask(()->{
            lx.engine.output.brightness.setValue(0);
        });

        // allow some time for readings to come in...
        // schedule task to take the reading

        java.util.Timer t = new java.util.Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
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

                try {
                    show.allPortsPowerEnableMask.saveToDisk();
                    show.allPortsPowerEnableMask.postToFirebase();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lx.engine.output.brightness.setValue(startValue);
            }
        }, delayBeforeCuttoff.getValuei() );
    }
}
