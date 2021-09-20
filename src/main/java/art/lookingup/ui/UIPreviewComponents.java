package art.lookingup.ui;

import art.lookingup.PreviewComponents;
import com.symmetrylabs.shows.firefly.FireflyShow;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

public class UIPreviewComponents extends UICollapsibleSection {

    public DiscreteParameter selRunP = new DiscreteParameter("run", 3);
    public DiscreteParameter selBezierP = new DiscreteParameter("curve", 4);
    public DiscreteParameter selCtrlPtP = new DiscreteParameter("ctrl pt", 4);
    public CompoundParameter ptSize = new CompoundParameter("pt sz", 1f, 10f);
   // public Parameter trees = new CompoundParameter("trees", )

    public UIPreviewComponents(final SLStudioLX.UI ui) {
        super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
        setTitle("Overlay");
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
        //showCtrlPointsBtn.setWidth(35).setHeight(16);
        // Disabled for now
        showCtrlPointsBtn.addToContainer(knobsContainer);
        UIButton showTrees = new UIButton() {
            @Override
            public void onToggle(boolean on) {
                FireflyShow.axes.showTrees = on;
            }
        }.setLabel("trees").setActive(FireflyShow.axes.showTrees);
        showTrees.setWidth(35).setHeight(16);
        showTrees.addToContainer(knobsContainer);

        UIButton showCablesBtn = new UIButton() {
            @Override
            public void onToggle(boolean on) {
                FireflyShow.axes.showCables = on;
            }
        }.setLabel("cables").setActive(FireflyShow.axes.showCables);
        showCablesBtn.setWidth(35).setHeight(16);
        showCablesBtn.addToContainer(knobsContainer);


        UI2dContainer knobsContainer2 = new UI2dContainer(0, 30, getContentWidth(), 20);
        knobsContainer2.setLayout(UI2dContainer.Layout.HORIZONTAL);
        knobsContainer2.setPadding(10, 10, 10, 10);
        knobsContainer2.addToContainer(this);

        UIKnob selRun = new UIKnob(selRunP);
        selRunP.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter p) {
                PreviewComponents.Axes.selectedRun = ((DiscreteParameter)p).getValuei();
            }
        });
        selRun.addToContainer(knobsContainer2);
        UIKnob selBezier = new UIKnob(selBezierP);
        selBezierP.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter p) {
                PreviewComponents.Axes.selectedBezier = ((DiscreteParameter)p).getValuei();
            }
        });
        selBezier.addToContainer(knobsContainer2);
        UIKnob selCtrlPt = new UIKnob(selCtrlPtP);
        selCtrlPtP.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter p) {
                PreviewComponents.Axes.selectedCtrlPt = ((DiscreteParameter)p).getValuei();
            }
        });
        selCtrlPt.addToContainer(knobsContainer2);
        UIKnob ptSizeKnob = new UIKnob(ptSize);
        ptSize.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter p) {
                PreviewComponents.Axes.ptSize = ((CompoundParameter)p).getValuef();
            }
        });
        ptSizeKnob.addToContainer(knobsContainer2);
    }
}



