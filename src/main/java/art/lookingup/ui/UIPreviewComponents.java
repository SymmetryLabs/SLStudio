package art.lookingup.ui;

import com.symmetrylabs.shows.firefly.FireflyShow;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

public class UIPreviewComponents extends UICollapsibleSection {

  public UIPreviewComponents(final SLStudioLX.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 50);
    setTitle("Axes");
    UI2dContainer knobsContainer = new UI2dContainer(0, 0, getContentWidth(), 20);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(10, 10, 10, 10);
    knobsContainer.addToContainer(this);

    UIButton showAxesBtn = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        FireflyShow.axes.showAxes = on;
      }
    }.setLabel("axes").setActive(FireflyShow.axes.showAxes);
    showAxesBtn.setWidth(35).setHeight(16);
    showAxesBtn.addToContainer(knobsContainer);
    UIButton showFloorBtn = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        FireflyShow.axes.showFloor = on;
      }
    }.setLabel("floor").setActive(FireflyShow.axes.showFloor);
    showFloorBtn.setWidth(35).setHeight(16);
    showFloorBtn.addToContainer(knobsContainer);
    UIButton showCtrlPointsBtn = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        FireflyShow.axes.showCtrlPoints = on;
      }
    }.setLabel("ctrl pts").setActive(FireflyShow.axes.showCtrlPoints);
    showCtrlPointsBtn.setWidth(35).setHeight(16);
    showCtrlPointsBtn.addToContainer(knobsContainer);
  }
}
