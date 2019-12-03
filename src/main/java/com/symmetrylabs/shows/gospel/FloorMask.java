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
import heronarts.lx.parameter.DiscreteParameter;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.shows.gospel.GospelModel;




public class FloorMask extends LXEffect {

  private final LX lx;

  public final BooleanParameter exclude = new BooleanParameter("exclude", false);

  public FloorMask(LX lx) {
    super(lx);
    this.lx = lx;

    addParameter(exclude);

  } 


  String[] strips = new String[]{
    "vip-lounge-strip19",
    "vip-lounge-strip20",
    "vip-lounge-strip21",
    "vip-lounge-strip22",
    // "vip-lounge-strip23",
    "vip-lounge-strip24",
  };

  int isFloor(String modelId) {
    for (int i = 0; i < strips.length; i++) {
      if (strips[i].equals(modelId)) {
        return i;
      }
    }
    return -1;
  }

 
  public void run(double deltaMs, double amount) {
    // int j = 0;
    for (Strip strip : ((GospelModel)model).strips) {

      int index = isFloor(strip.modelId);
      boolean in = index != -1;

      if ((!in && !exclude.isOn()) || (in && exclude.isOn())) {
        int n = strip.points.length;
        for (int i = 0; i < n; i++) {
          colors[strip.points[i].index] = 0;
        }
      } 
      // else if (in) {
      //   // if (index == 2) {
      //   //   for (int i = 41; i < strip.points.length; i++) {
      //   //      colors[strip.points[strip.points.length - 1 - i].index] = 0;
      //   //   }
      //   // } else {
      //     int n = Math.min(strip.points.length - 1, parz[index].getValuei());
      //     for (int i = 0; i < n; i++) {
      //        colors[strip.points[strip.points.length - 1 - i].index] = 0;
      //     }
      //   // }
      // }
      // j++;
    }
  }
}