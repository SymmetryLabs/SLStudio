package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.slstudio.effect.SLEffect;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput; 
import java.util.LinkedList;
import java.util.Queue; 
import com.symmetrylabs.shows.empirewall.VineModel;
import com.symmetrylabs.shows.empirewall.VineModel.Vine;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.color.LXColor;


public class VineWallMask extends SLEffect {
    BooleanParameter showStrips = new BooleanParameter("strips", true);
    BooleanParameter showLeaves = new BooleanParameter("leaves", true);


    public VineWallMask(LX lx) {
        super(lx);

        addParameter(showStrips);
        addParameter(showLeaves);


    }

    public String getLabel() {
        return "VineWallMask";
    }


    public void run(double deltaMs, double enabledAmount) {
        VineModel m = (VineModel)model;
        if (!showStrips.isOn()) {
            for (Strip s : m.strips) {
                setColor(s, LXColor.BLACK);
            }
        }
        if (!showLeaves.isOn()) {
            for (Vine v : m.vines) {
                setColor(v, LXColor.BLACK);
            }
        }

    }
}