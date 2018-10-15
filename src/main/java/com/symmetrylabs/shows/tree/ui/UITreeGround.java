// package com.symmetrylabs.shows.tree.ui;
//
// import com.symmetrylabs.layouts.tree.TreeModel;
// import static com.symmetrylabs.util.DistanceConstants.*;
//
// import heronarts.p3lx.ui.UI3dComponent;
// import processing.core.PApplet;
// import static processing.core.PConstants.*;
// import processing.core.PGraphics;
// import processing.core.PImage;
//
// public class UITreeGround extends UI3dComponent {
//
//   private final PImage dust;
//   private final PImage person;
//   private static final int DUST_FILL = 0xFFA7784C;
//
//   public UITreeGround(PApplet applet) {
//     this.dust = applet.loadImage("dust.png");
//     this.person = applet.loadImage("person.png");
//   }
//
//   @Override
//   protected void onDraw(heronarts.p3lx.ui.UI ui, PGraphics pg) {
//     pg.tint(DUST_FILL);
//     pg.textureMode(NORMAL);
//     pg.beginShape();
//     pg.texture(this.dust);
//     pg.vertex(-100*FEET, -TreeModel.LIMB_HEIGHT - 1*FEET, -100*FEET, 0, 0);
//     pg.vertex(100*FEET, -TreeModel.LIMB_HEIGHT - 1*FEET, -100*FEET, 0, 1);
//     pg.vertex(100*FEET, -TreeModel.LIMB_HEIGHT - 1*FEET, 100*FEET, 1, 1);
//     pg.vertex(-100*FEET, -TreeModel.LIMB_HEIGHT - 1*FEET, 100*FEET, 1, 0);
//     pg.endShape(CLOSE);
//
//     float personY = -TreeModel.LIMB_HEIGHT - 1*FEET;
//     drawPerson(pg, -10*FEET, personY, 10*FEET, 1.5f*FEET, 1.5f*FEET);
//     drawPerson(pg, 8*FEET, personY, 12*FEET, -1.5f*FEET, 1.5f*FEET);
//     drawPerson(pg, 2*FEET, personY, 8*FEET, -2*FEET, 1*FEET);
//   }
//
//   void drawPerson(PGraphics pg, float personX, float personY, float personZ, float personXW, float personZW) {
//     pg.tint(0xFF393939);
//     pg.noStroke();
//     pg.beginShape();
//     pg.texture(this.person);
//     pg.vertex(personX, personY, personZ, 0, 1);
//     pg.vertex(personX + personXW, personY, personZ + personZW, 1, 1);
//     pg.vertex(personX + personXW, personY + 5*FEET, personZ + personZW, 1, 0);
//     pg.vertex(personX, personY + 5*FEET, personZ, 0, 0);
//     pg.endShape(CLOSE);
//   }
// }
