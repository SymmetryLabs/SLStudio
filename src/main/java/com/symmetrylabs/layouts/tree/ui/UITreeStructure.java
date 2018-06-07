// package com.symmetrylabs.layouts.tree.ui;

// import com.symmetrylabs.layouts.tree.TreeModel;
// import static com.symmetrylabs.util.DistanceConstants.*;

// import com.symmetrylabs.slstudio.ui.UICylinder;
// import heronarts.p3lx.ui.UI;
// import heronarts.p3lx.ui.UI3dComponent;
// import static processing.core.PConstants.*;
// import processing.core.PGraphics;

// public class UITreeStructure extends UI3dComponent {

//   private final TreeModel tree;
//   private static final int WOOD_FILL = 0xFF281403;

//   public UITreeStructure(TreeModel tree) {
//     this.tree = tree;
//     addChild(new UICylinder(
//       TreeModel.TRUNK_DIAMETER/2, TreeModel.TRUNK_DIAMETER/4,
//       -TreeModel.LIMB_HEIGHT, 6*FEET, 8, WOOD_FILL));
//     for (TreeModel.Limb limb : tree.limbs) {
//       addChild(new UILimb(limb));
//     }
//     for (TreeModel.Branch branch : tree.branches) {
//       if (branch.orientation != null) {
//         addChild(new UIBranch(branch));
//       }
//     }
//   }

//   public static class UILimb extends UI3dComponent {

//     private final static UICylinder section1;
//     private final static UICylinder section2;
//     private final static UICylinder section3;
//     private final static UICylinder section4;

//     static {
//       section1 = new UICylinder(TreeModel.Limb.SECTION_1.radius, TreeModel.Limb.SECTION_1.len, 5, WOOD_FILL);
//       section2 = new UICylinder(TreeModel.Limb.SECTION_2.radius, TreeModel.Limb.SECTION_2.len, 5, WOOD_FILL);
//       section3 = new UICylinder(TreeModel.Limb.SECTION_3.radius, TreeModel.Limb.SECTION_3.len, 5, WOOD_FILL);
//       section4 = new UICylinder(TreeModel.Limb.SECTION_4.radius, TreeModel.Limb.SECTION_4.len, 5, WOOD_FILL);
//     }

//     private final float azimuth;
//     private final float y;
//     private final TreeModel.Limb.Size size;

//     public UILimb(TreeModel.Limb limb) {
//       this(limb.y, limb.azimuth, limb.size);
//     }

//     public UILimb(float azimuth, TreeModel.Limb.Size size) {
//       this(0, azimuth, size);
//     }

//     public UILimb(float azimuth) {
//       this(0, azimuth);
//     }

//     public UILimb(float y, float azimuth) {
//       this(y, azimuth, TreeModel.Limb.Size.FULL);
//     }

//     public UILimb(float y, float azimuth, TreeModel.Limb.Size size) {
//       this.y = y;
//       this.azimuth = azimuth;
//       this.size = size;
//     }

//     public void onDraw(UI ui, PGraphics pg) {
//       pg.noStroke();
//       pg.fill(WOOD_FILL);

//       pg.pushMatrix();
//       pg.translate(0, this.y, 0);
//       pg.rotateY(HALF_PI - this.azimuth);
//       pg.rotateX(HALF_PI - PI/12);

//       if (this.size == TreeModel.Limb.Size.FULL) {
//         section1.onDraw(ui, pg);
//         pg.translate(0, section1.len, 0);
//       }
//       if (this.size != TreeModel.Limb.Size.SMALL) {
//         section2.onDraw(ui, pg);
//         pg.translate(0, section2.len, 0);
//       }
//       pg.rotateX(-PI/6);
//       section3.onDraw(ui, pg);
//       pg.translate(0, section3.len, 0);
//       pg.rotateX(-PI/6);
//       section4.onDraw(ui, pg);
//       pg.popMatrix();
//     }
//   }

//   public static class UIBranch extends UI3dComponent {

//     private final TreeModel.Branch branch;

//     private final static UICylinder cylinder;

//     static {
//       cylinder = new UICylinder(1f*INCHES, .5f*INCHES, 44f*INCHES, 8, WOOD_FILL);
//     }

//     public UIBranch(TreeModel.Branch branch) {
//       this.branch = branch;
//       for (TreeModel.LeafAssemblage assemblage : branch.assemblages) {
//         addChild(new UILeafAssemblage(assemblage));
//       }

//     }

//     @Override
//     protected void beginDraw(UI ui, PGraphics pg) {
//       pg.pushMatrix();
//       pg.translate(this.branch.x, this.branch.y, this.branch.z);
//       pg.rotateY(HALF_PI - this.branch.orientation.azimuth);
//       pg.rotateX(HALF_PI - this.branch.orientation.elevation);
//       pg.rotateY(this.branch.orientation.tilt);
//     }

//     @Override
//     protected void onDraw(UI ui, PGraphics pg) {
//       pg.fill(WOOD_FILL);
//       pg.noStroke();
//       cylinder.onDraw(ui, pg);
//     }

//     @Override
//     protected void endDraw(UI ui, PGraphics pg) {
//       pg.popMatrix();
//     }
//   }

//   public static class UILeafAssemblage extends UI3dComponent {

//     private final TreeModel.LeafAssemblage assemblage;
//     private static final UICylinder cylinder;

//     static {
//       cylinder = new UICylinder(.4f*INCHES, .4f*INCHES, -12f*INCHES, TreeModel.LeafAssemblage.LEAVES[8].y, 5, WOOD_FILL);
//     }

//     public UILeafAssemblage(TreeModel.LeafAssemblage assemblage) {
//       this.assemblage = assemblage;
//     }

//     @Override
//     protected void beginDraw(heronarts.p3lx.ui.UI ui, PGraphics pg) {
//       pg.pushMatrix();
//       pg.translate(this.assemblage.orientation.x, this.assemblage.orientation.y);
//       pg.rotateZ(this.assemblage.orientation.theta);
//       pg.rotateY(this.assemblage.orientation.tilt);
//     }

//     @Override
//     protected void endDraw(heronarts.p3lx.ui.UI ui, PGraphics pg) {
//       pg.popMatrix();
//     }

//     @Override
//     protected void onDraw(heronarts.p3lx.ui.UI ui, PGraphics pg) {
//       pg.fill(WOOD_FILL);
//       pg.noStroke();
//       cylinder.onDraw(ui, pg);
//     }
//   }
// }
