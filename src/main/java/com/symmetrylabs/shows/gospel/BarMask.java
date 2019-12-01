package com.symmetrylabs.shows.gospel;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.LXEffect;
import heronarts.lx.ModelBuffer;
import heronarts.lx.blend.NormalBlend;
import heronarts.lx.blend.ScreenBlend;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.shows.gospel.GospelModel;




public class BarMask extends LXEffect {
  public final String className = "bar";

  private final LX lx;

  public final BooleanParameter exclude = new BooleanParameter("exclude", false);

  public BarMask(LX lx) {
    super(lx);
    this.lx = lx;

    addParameter(exclude);
  }

  public void run(double deltaMs, double amount) {
    for (Strip strip : ((GospelModel)model).strips) {

      boolean in = strip.modelId.contains(className);

      if ((!in && !exclude.isOn()) || (in && exclude.isOn())) {
        for (LXPoint p : strip.points) {
          colors[p.index] = 0;
        }
      }
    }
  }
}