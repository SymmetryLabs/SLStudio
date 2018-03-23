// package com.symmetrylabs.layouts.dollywood;

// import heronarts.lx.LX;
// import heronarts.lx.LXPattern;
// import heronarts.lx.color.LXColor;
// import heronarts.lx.model.LXPoint;

// import heronarts.lx.parameter.CompoundParameter;
// import heronarts.lx.modulator.LXModulator;
// import heronarts.lx.modulator.TriangleLFO;
// import heronarts.lx.modulator.SawLFO;
// import heronarts.lx.modulator.SinLFO;

// import static com.symmetrylabs.util.MathUtils.*;

// import heronarts.lx.LXUtils;

// public class TextureCrawl extends TexturePattern {

//   private final DollywoodModel model;

//   private final int NUM_LEDS_PER_WING = 8;

//   public String getAuthor() {
//     return "Mark C. Slee";
//   }
    
//   private static final int NUM_MASKS = 24;
//   private final int[][] mask = new int[NUM_MASKS][NUM_LEDS_PER_WING];
    
//   private final LXModulator[] pos = new LXModulator[NUM_MASKS];
//   private final LXModulator[] size = new LXModulator[NUM_MASKS];
    
//   public TextureCrawl(LX lx) {
//     super(lx);
//     this.model = ((DollywoodModel)lx.model);
//     for (int i = 0; i < NUM_MASKS; ++i) {
//       this.pos[i] = startModulator(new SawLFO(0, NUM_LEDS_PER_WING, startModulator(new SinLFO(2000, 7000, 19000).randomBasis())));
//       this.size[i] = startModulator(new TriangleLFO(-3, 2*NUM_LEDS_PER_WING, 19000).randomBasis());
//     }
//   }
 
//   public void run(double deltaMs) {
//     for (int i = 0; i < NUM_MASKS; ++i) {
//       float pos = this.pos[i].getValuef();
//       float falloff = 100 / max(1, this.size[i].getValuef()); 
//       for (int j = 0; j < NUM_LEDS_PER_WING; ++j) {
//         this.mask[i][j] = LXColor.gray(max(0, 100 - falloff * LXUtils.wrapdistf(j, pos, NUM_LEDS_PER_WING)));
//       }
//     }
//     int li = 0;
//     for (DollywoodModel.Wing wing : this.model.wings) {
//       int[] mask = this.mask[li++ % NUM_MASKS];
//       int i = 0;
//       for (LXPoint p : wing.points) {
//         colors[p.index] = mask[i++];
//       }
//     }
//   }
// }