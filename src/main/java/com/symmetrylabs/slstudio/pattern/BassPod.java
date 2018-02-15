package com.symmetrylabs.slstudio.pattern;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.slstudio.util.ProxyParameter;

import static processing.core.PApplet.*;

// public class BassPod extends SLPattern {

//   private final LXAudioInput audioInput = lx.engine.audio.getInput();
//   private final GraphicMeter eq = new GraphicMeter(audioInput);
    
//   private final CompoundParameter clr = new CompoundParameter("CLR", 0.5);
    
//   public BassPod(LX lx) {
//     super(lx);
//     addModulator(eq).start();
//     addParameter(clr);
//     addParameter(new ProxyParameter(eq.gain));
//     addParameter(new ProxyParameter(eq.range));
//     addParameter(new ProxyParameter(eq.attack));
//     addParameter(new ProxyParameter(eq.release));
//     addParameter(new ProxyParameter(eq.slope));
//   }
    
//   void onActive() {
//     eq.range.setValue(36);
//     eq.release.setValue(300);
//     eq.gain.setValue(-6);
//     eq.slope.setValue(6);
//   }

//   public void run(double deltaMs) {
//     float bassLevel = eq.getAveragef(0, 5);
//     float satBase = bassLevel*480*clr.getValuef();
        
//     for (LXPoint p : model.points) {
//       int avgIndex = (int) constrain(1 + abs(p.x-model.cx)/(model.cx)*(eq.numBands-5), 0, eq.numBands-5);
//       float value = 0;
//       for (int i = avgIndex; i < avgIndex + 5; ++i) {
//         value += eq.getBandf(i);
//       }
//       value /= 5.;

//       float b = constrain(8 * (value*model.yMax - abs(p.y-model.yMax/2.)), 0, 100);
//       colors[p.index] = lx.hsb(
//         palette.getHuef() + abs(p.y - model.cy) + abs(p.x - model.cx),
//         constrain(satBase - .6*dist(p.x, p.y, model.cx, model.cy), 0, 100),
//         b
//       );
//     }
//   }
// }

public class BassPod extends SLPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();
    private GraphicMeter eq = new GraphicMeter(audioInput);

    private final CompoundParameter clr = new CompoundParameter("CLR", 0.5);
    private final CompoundParameter gain = new CompoundParameter("Gain", 0.5, 0, 5);

    public BassPod(LX lx) {
        super(lx);

        eq.start();

        addParameter(clr);
        addParameter(gain);
        addModulator(eq).start();

        addParameter(new ProxyParameter(eq.gain));
        addParameter(new ProxyParameter(eq.range));
        addParameter(new ProxyParameter(eq.attack));
        addParameter(new ProxyParameter(eq.release));
        addParameter(new ProxyParameter(eq.slope));
    }

    @Override
    public void onActive() {
        super.onActive();
        eq.range.setValue(32);
        eq.release.setValue(500);
        eq.gain.setValue(-6);
        eq.slope.setValue(6);
    }

    public static final float HUE_RATE = 100;
    public static final float BRTNESS_RATE = 800;
    public static final float DESAT_RATE = 50;

    // public void run(double deltaMs) {
    //   float bassLevel = eq.getAveragef(0, 5);
    //   float satBase = bassLevel*480*clr.getValuef();
      
    //   for (LXPoint p : model.points) {
    //     int avgIndex = (int) constrain(1 + abs(p.x-model.cx)/(model.cx)*(eq.numBands-5), 0, eq.numBands-5);
    //     float value = 0;
    //     for (int i = avgIndex; i < avgIndex + 5; ++i) {
    //       value += eq.getBandf(i);
    //     }
    //     value /= 5.;

    //     float b = constrain(8 * (value*model.yMax - abs(p.y-model.yMax/2.)), 0, 100);
    //     colors[p.index] = lx.hsb(
    //       palette.getHuef() + abs(p.y - model.cy) + abs(p.x - model.cx),
    //       constrain(satBase - .6*dist(p.x, p.y, model.cx, model.cy), 0, 100),
    //       b
    //     );
    //   }
    // }

    @Override
    public void run(double deltaMs) {
        final float bassLevel = eq.getAveragef(0, 5);
        final float satBase = bassLevel * 480 * clr.getValuef();

        model.getPoints().parallelStream().forEach(p -> {
            int avgIndex = (int)constrain(1 + abs(p.x - model.cx) / model.xRange * (eq.numBands - 5), 0, eq.numBands - 5);
            float value = 0;
            for (int i = avgIndex; i < avgIndex + 5; ++i) {
                value += eq.getBandf(i) * gain.getValuef();
            }
            value /= 5.;

            float h = palette.getHuef() + HUE_RATE * (abs(p.y - model.cy) / model.yRange + abs(p.x - model.cx) / model.xRange);
            float b = constrain(BRTNESS_RATE * (value * model.yRange - abs(p.y - model.cy)) / model.yRange, 0, 100);
            float s = constrain(satBase - DESAT_RATE * dist(p.x, p.y, model.cx, model.cy) * 2f / (model.xRange + model.yRange), 0, 100);

            colors[p.index] = lx.hsb(h, s, b);
        });
    }
}
