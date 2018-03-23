// package com.symmetrylabs.layouts.dollywood;



// public class Snakes extends LXPattern {
//   public String getAuthor() {
//     return "Mark C. Slee";
//   }

//   private final int NUM_WINGS_PER_BUTTERFLY = 4;
    
//   private static final int NUM_SNAKES = 24;
//   private final LXModulator snakes[] = new LXModulator[NUM_SNAKES];
//   private final LXModulator sizes[] = new LXModulator[NUM_SNAKES];
    
//   private final int[][] mask = new int[NUM_SNAKES][NUM_WINGS_PER_BUTTERFLY];
    
//   public final CompoundParameter speed = (CompoundParameter)
//     new CompoundParameter("Speed", 7000, 19000, 2000)
//     .setExponent(.5)
//     .setDescription("Speed of snakes moving");
        
//   public final CompoundParameter modSpeed = (CompoundParameter)
//     new CompoundParameter("ModSpeed", 7000, 19000, 2000)
//     .setExponent(.5)
//     .setDescription("Speed of snake length modulation");    
        
//   public final CompoundParameter size =
//     new CompoundParameter("Size", 15, 10, 100)
//     .setDescription("Size of longest snake");    
            
//   public Snakes(LX lx) {
//     super(lx);
//     addParameter("speed", this.speed);
//     addParameter("modSpeed", this.modSpeed);
//     addParameter("size", this.size);
//     for (int i = 0; i < NUM_SNAKES; ++i) {
//       final int ii = i;
//       this.snakes[i] = startModulator(new SawLFO(0, NUM_WINGS_PER_BUTTERFLY, speed).randomBasis());
//       this.sizes[i] = startModulator(new SinLFO(4, this.size, new FunctionalParameter() {
//         public double getValue() {
//           return modSpeed.getValue() + ii*100;
//         }
//       }).randomBasis());
//     }
//   }
    
//   public void run(double deltaMs) {
//     for (int i = 0; i < NUM_SNAKES; ++i) {
//       float snake = this.snakes[i].getValuef();
//       float falloff = 100 / this.sizes[i].getValuef();
//       for (int j = 0; j < NUM_WINGS_PER_BUTTERFLY; ++j) {
//         this.mask[i][j] = LXColor.gray(max(0, 100 - falloff * LXUtils.wrapdistf(j, snake, NUM_WINGS_PER_BUTTERFLY)));
//       }
//     }
//     int bi = 0;
//     for (Branch branch : model.branches) {
//       int[] mask = this.mask[bi++ % NUM_SNAKES];
//       int li = 0;
//       for (Leaf leaf : branch.leaves) {
//         setColor(leaf, mask[li++]);
//       }
//     }
//   }
// }