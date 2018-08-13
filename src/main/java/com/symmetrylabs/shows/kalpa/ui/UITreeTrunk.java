 package com.symmetrylabs.shows.kalpa.ui;

 import processing.core.PApplet;
 import processing.core.PGraphics;
 import processing.core.PImage;
 import processing.core.PVector;
 import static processing.core.PConstants.*;

 import heronarts.p3lx.ui.UI3dComponent;
 import heronarts.p3lx.ui.UI;

 import com.symmetrylabs.shows.kalpa.TreeModel;
 import static com.symmetrylabs.util.DistanceConstants.*;
 import static com.symmetrylabs.util.MathUtils.*;


 public class UITreeTrunk extends UI3dComponent {

     public static final float TRUNK_DIAMETER = 3*FEET;
     public static final float LIMB_HEIGHT = 5*FEET;

     private final UICylinder cylinder;
     private final PImage dust;
     private final PImage person;
     private static final int GRASS_FILL = 0xff1b300f;

     public UITreeTrunk(PApplet applet) {
         this.cylinder = new UICylinder(TRUNK_DIAMETER/2, TRUNK_DIAMETER/4, -LIMB_HEIGHT, 6*FEET, 8);
         this.dust = applet.loadImage("dust.png");
         this.person = applet.loadImage("person.png");
         addChild(this.cylinder);
     }

     @Override
     protected void onDraw(UI ui, PGraphics pg) {
         pg.tint(GRASS_FILL);
         pg.textureMode(NORMAL);
         pg.beginShape();
         pg.texture(this.dust);
         pg.vertex(-35*FEET, -LIMB_HEIGHT, -35*FEET, 0, 0);
         pg.vertex(35*FEET, -LIMB_HEIGHT, -35*FEET, 0, 1);
         pg.vertex(35*FEET, -LIMB_HEIGHT, 35*FEET, 1, 1);
         pg.vertex(-35*FEET, -LIMB_HEIGHT, 35*FEET, 1, 0);
         pg.endShape(CLOSE);

//     float personY = -LIMB_HEIGHT - 1*FEET;
//     drawPerson(pg, -10*FEET, personY, 10*FEET, 1.5f*FEET, 1.5f*FEET);
//     drawPerson(pg, 8*FEET, personY, 12*FEET, -1.5f*FEET, 1.5f*FEET);
//     drawPerson(pg, 2*FEET, personY, 8*FEET, -2*FEET, 1*FEET);
     }

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

     public static class UICylinder extends UI3dComponent {

         private final PVector[] base;
         private final PVector[] top;
         private final int detail;
         public final float len;

         public UICylinder(float radius, float len, int detail) {
             this(radius, radius, 0, len, detail);
         }

         public UICylinder(float baseRadius, float topRadius, float len, int detail) {
             this(baseRadius, topRadius, 0, len, detail);
         }

         public UICylinder(float baseRadius, float topRadius, float yMin, float yMax, int detail) {
             this.base = new PVector[detail];
             this.top = new PVector[detail];
             this.detail = detail;
             this.len = yMax - yMin;
             for (int i = 0; i < detail; ++i) {
                 float angle = i * TWO_PI / detail;
                 this.base[i] = new PVector(baseRadius * cos(angle), yMin, baseRadius * sin(angle));
                 this.top[i] = new PVector(topRadius * cos(angle), yMax, topRadius * sin(angle));
             }
         }

         public void onDraw(UI ui, PGraphics pg) {
             pg.beginShape(TRIANGLE_STRIP);
             pg.tint(0x451800);
             for (int i = 0; i <= this.detail; ++i) {
                 int ii = i % this.detail;
                 pg.vertex(this.base[ii].x, this.base[ii].y, this.base[ii].z);
                 pg.vertex(this.top[ii].x, this.top[ii].y, this.top[ii].z);
             }
             pg.endShape(CLOSE);
         }
     }

 }
