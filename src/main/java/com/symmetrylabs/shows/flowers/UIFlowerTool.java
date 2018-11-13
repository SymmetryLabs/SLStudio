package com.symmetrylabs.shows.flowers;

import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;

public class UIFlowerTool extends UI2dContainer {
    private static UIFlowerTool INSTANCE;

    public static void attach(SLStudioLX lx, SLStudioLX.UI ui) {
        INSTANCE = new UIFlowerTool(lx, ui);
        INSTANCE.addToContainer(ui.rightPane.model);
    }

    static UIFlowerTool get() {
        if (INSTANCE == null) {
            throw new RuntimeException("UIFlowerTool needs to be attached first");
        }
        return INSTANCE;
    }

    private final SLStudioLX lx;
    private final SLStudioLX.UI ui;
    private final FlowersModel model;
    private final FlowerEditor flowerEditor;
    private final UIItemList.ScrollList flowerList;

    public UIFlowerTool(SLStudioLX lx, SLStudioLX.UI ui) {
        super(0, 0, ui.rightPane.model.getContentWidth(), 1000);
        this.lx = lx;
        this.ui = ui;
        model = (FlowersModel) lx.model;

        flowerEditor = new FlowerEditor();
        addTopLevelComponent(flowerEditor);

        flowerList =
            new UIItemList.ScrollList(
                ui, 6, 12 + flowerEditor.getHeight(), getContentWidth() - 12, 200);
        for (FlowerModel fm : model.getFlowers()) {
            flowerList.addItem(new FlowerItem(fm.getFlowerData()));
        }
        addTopLevelComponent(flowerList);
    }

    void onUpdate() {
        flowerList.redraw();
    }

    class FlowerEditor extends UI2dContainer {
        FlowerData current;
        UILabel id;
        UITextBox panel;
        UIIntegerBox harnessIndex;
        UIDoubleBox yOverride;
        UIButton overrideHeight;
        UIButton harnessA;
        UIButton harnessB;

        StringParameter pP = new StringParameter("P");
        BooleanParameter pHA = new BooleanParameter("HA");
        BooleanParameter pHB = new BooleanParameter("HB");
        DiscreteParameter pHI = new DiscreteParameter("HI", 0, -1, 10);

        public FlowerEditor() {
            super(6, 6, UIFlowerTool.this.getContentWidth() - 12, 60);
            id = new UILabel(0, 0, getContentWidth(), 14);
            id.setLabel("Select a flower");
            id.setTextAlignment(PConstants.LEFT, PConstants.TOP).setTextOffset(0,  1);
            id.addToContainer(this);

            pP.addListener(p -> onUpdate());
            pHI.setFormatter(v -> Integer.toString((int) v)).addListener(p -> onUpdate());
            pHA.addListener(p -> {
                    if (pHA.getValueb()) {
                        pHB.setValue(false);
                    }
                    onUpdate();
                });
            pHB.addListener(p -> {
                    if (pHB.getValueb()) {
                        pHA.setValue(false);
                    }
                    onUpdate();
                });

            float LH = 14;
            float Y1 = id.getHeight() + 4;
            float Y2 = Y1 + LH + 4;
            float H = 20;
            float PW = 40;

            new UILabel(0, Y1, PW, LH).setLabel("PANEL").addToContainer(this);
            new UILabel(PW + 4, Y1, getContentWidth() - PW, LH)
                .setLabel("HARNESS").addToContainer(this);

            panel = new UITextBox(0, Y2, PW, H);
            panel.setParameter(pP);
            panel.addToContainer(this);
            panel.setEnabled(false);

            harnessA = new UIButton(PW + 4, Y2, H, H);
            harnessA.setLabel("A").setParameter(pHA);
            harnessA.addToContainer(this);
            harnessA.setEnabled(false);

            harnessB = new UIButton(PW + 4 + H + 2, Y2, H, H);
            harnessB.setLabel("B").setParameter(pHB);
            harnessB.addToContainer(this);
            harnessB.setEnabled(false);

            harnessIndex = new UIIntegerBox(PW + 4 + 2 * (H + 2), Y2, 40, H);
            harnessIndex.setParameter(pHI);
            harnessIndex.addToContainer(this);
            harnessIndex.setEnabled(false);
        }

        void setCurrent(FlowerData fd) {
            current = fd;
            id.setLabel(String.format("FLOWER %04d", fd.record.id));
            pP.setValue(fd.record.panelId == null ? "" : fd.record.panelId);
            pHA.setValue(fd.record.harness == FlowerRecord.Harness.A);
            pHB.setValue(fd.record.harness == FlowerRecord.Harness.B);
            pHI.setValue(fd.record.harnessIndex);

            panel.setEnabled(true);
            harnessA.setEnabled(true);
            harnessB.setEnabled(true);
            harnessIndex.setEnabled(true);

            redraw();
        }

        void onUpdate() {
            current.record.panelId = "".equals(pP.getString()) ? null : pP.getString();
            current.record.harness =
                pHA.getValueb() ? FlowerRecord.Harness.A :
                pHB.getValueb() ? FlowerRecord.Harness.B :
                FlowerRecord.Harness.UNKNOWN;
            current.record.harnessIndex = pHI.getValuei();
            UIFlowerTool.this.onUpdate();
        }
    }

    class FlowerItem extends UIItemList.AbstractItem {
        private final FlowerData data;

        FlowerItem(FlowerData data) {
            this.data = data;
        }

        @Override
        public String getLabel() {
            return data.record.toString();
        }

        @Override
        public void onFocus() {
            flowerEditor.setCurrent(data);
        }
    }
}
