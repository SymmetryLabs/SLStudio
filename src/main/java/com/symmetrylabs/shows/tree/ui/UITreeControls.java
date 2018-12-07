// package com.symmetrylabs.shows.tree.ui;

// import com.symmetrylabs.slstudio.SLStudioLX;
// import heronarts.p3lx.ui.UI2dContainer;
// import heronarts.p3lx.ui.UI3dComponent;
// import heronarts.p3lx.ui.component.UIButton;
// import heronarts.p3lx.ui.studio.UICollapsibleSection;

// public class UITreeControls extends UICollapsibleSection {
//   public final UIButton pointsVisible;
//   public final UIButton leavesVisible;
//   public final UIButton structureVisible;

//   public UITreeControls(final SLStudioLX.UI ui, UI3dComponent uiTreeStructure, UI3dComponent uiLeaves) {
//     super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
//     setTitle("RENDER");
//     setLayout(UI2dContainer.Layout.VERTICAL);
//     setChildMargin(2);

//     this.pointsVisible = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
//       public void onToggle(boolean on) {
//         ui.preview.pointCloud.setVisible(on);
//       }
//     }
//     .setLabel("Points")
//     .setActive(ui.preview.pointCloud.isVisible())
//     .addToContainer(this);

//     this.leavesVisible = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
//       public void onToggle(boolean on) {
//         uiLeaves.setVisible(on);
//       }
//     }
//     .setLabel("Leaves")
//     .setActive(uiLeaves.isVisible())
//     .addToContainer(this);

//     this.structureVisible = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
//       public void onToggle(boolean on) {
//         uiTreeStructure.setVisible(on);
//       }
//     }
//     .setLabel("Structure")
//     .setActive(uiTreeStructure.isVisible())
//     .addToContainer(this);
//   }
// }
