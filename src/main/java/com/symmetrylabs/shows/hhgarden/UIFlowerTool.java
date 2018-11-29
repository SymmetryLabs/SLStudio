package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import heronarts.p3lx.ui.component.UIItemList.ScrollList;

public class UIFlowerTool extends UI2dContainer {
    private static UIFlowerTool INSTANCE;

    public static UIFlowerTool attach(SLStudioLX lx, SLStudioLX.UI ui) {
        INSTANCE = new UIFlowerTool(lx, ui);
        INSTANCE.addToContainer(ui.rightPane.model);
        return INSTANCE;
    }

    static UIFlowerTool get() {
        if (INSTANCE == null) {
            throw new RuntimeException("UIFlowerTool needs to be attached first");
        }
        return INSTANCE;
    }

    public interface Listener {
        void onFlowerSelected(FlowerData data);
        void onPixliteSelected(int pixliteId);
        void onUpdate();
    }

    private final SLStudioLX lx;
    private final SLStudioLX.UI ui;
    private final FlowersModel model;
    private final FlowerEditor flowerEditor;
    private final UIItemList.ScrollList flowerList;
    private final UICollapsibleSection flowers;
    private final PixlitePane pixlites;
    private final List<Listener> listeners = new ArrayList<>();
    private final BooleanParameter saveParam =
        new BooleanParameter("save").setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter runPanelizerParam =
        new BooleanParameter("panelizer").setMode(BooleanParameter.Mode.MOMENTARY);

    public UIFlowerTool(SLStudioLX lx, SLStudioLX.UI ui) {
        super(0, 0, ui.rightPane.model.getContentWidth(), 1000);
        this.lx = lx;
        this.ui = ui;
        model = (FlowersModel) lx.model;

        flowers = new UICollapsibleSection(ui, 0, 0, getContentWidth(), 350);
        flowers.setTitle("FLOWERS");

        new UIButton(0, 2, flowers.getContentWidth(), 20)
            .setParameter(saveParam)
            .setLabel("SAVE ALL")
            .addToContainer(flowers);
        saveParam.addListener(p -> model.storeRecords());

        new UIButton(0, 24, flowers.getContentWidth(), 20)
            .setParameter(runPanelizerParam)
            .setLabel("RUN PANELIZER")
            .addToContainer(flowers);
        runPanelizerParam.addListener(p -> model.panelize());

        flowerList =
            new UIItemList.ScrollList(
                ui, 0, 52, flowers.getContentWidth(), 172);
        for (FlowerModel fm : model.getFlowers()) {
            flowerList.addItem(new FlowerItem(fm));
        }
        flowerList.addToContainer(flowers);

        flowerEditor = new FlowerEditor();
        flowerEditor.addToContainer(flowers);

        addTopLevelComponent(flowers);

        pixlites = new PixlitePane();
        addTopLevelComponent(pixlites);
    }

    private void onUpdate() {
        for (Listener l : listeners) {
            l.onUpdate();
        }
        flowerEditor.reload();
        pixlites.reload();
        flowerList.redraw();
    }

    private void setCurrentFlower(FlowerModel fm) {
        flowerEditor.setCurrent(fm);
        for (Listener l : listeners) {
            l.onFlowerSelected(fm.getFlowerData());
        }
    }

    private void setCurrentPixlite(int pixliteId) {
        for (Listener l : listeners) {
            l.onPixliteSelected(pixliteId);
        }
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    class FlowerEditor extends UI2dContainer {
        FlowerModel currentModel;
        FlowerData current;
        UILabel id;
        UITextBox panel;
        UIIntegerBox pixliteId;
        UIIntegerBox harnessIndex;
        UIDoubleBox yOverride;
        UIButton overrideHeight;
        UIIntegerBox harness;

        StringParameter pP = new StringParameter("P");
        DiscreteParameter pPX = new DiscreteParameter("PX", 0, 0, 256);
        DiscreteParameter pH = new DiscreteParameter("H", FlowerRecord.UNKNOWN_HARNESS, FlowerRecord.UNKNOWN_HARNESS, 5);
        BooleanParameter pHA = new BooleanParameter("HA");
        BooleanParameter pHB = new BooleanParameter("HB");
        DiscreteParameter pHI = new DiscreteParameter("HI", 0, 0, 10);
        BooleanParameter pOH = new BooleanParameter("OH");
        CompoundParameter pYO = new CompoundParameter("YO", 0, -1000, 1000);

        boolean loading = false;

        public FlowerEditor() {
            super(0, 236, flowers.getContentWidth(), 80);
            id = new UILabel(0, 0, getContentWidth(), 10);
            id.setLabel("Select a flower");
            id.setTextAlignment(PConstants.LEFT, PConstants.TOP).setTextOffset(0,  1);
            id.addToContainer(this);

            pP.addListener(p -> onUpdate());
            pHI.setFormatter(v -> Integer.toString((int) v)).addListener(p -> onUpdate());
            pH.setFormatter(v -> Integer.toString((int) v)).addListener(p -> onUpdate());
            pOH.addListener(p -> {
                    yOverride.setEnabled(pOH.getValueb());
                    onUpdate();
                });
            pYO.addListener(p -> onUpdate());
            pPX.setFormatter(v -> Integer.toString((int) v)).addListener(p -> onUpdate());

            float LH = 10;
            float H = 20;
            float PW = 40;
            float PP = 12;
            float RP = 4;
            float Y1 = id.getHeight() + RP;
            float Y2 = Y1 + LH + RP;
            float Y3 = Y2 + H + RP;
            float Y4 = Y3 + LH + RP;

            new UILabel(0, Y1, PW, LH).setLabel("PANEL").addToContainer(this);
            new UILabel(PW + PP, Y1, PW, LH).setLabel("PIXLITE").addToContainer(this);
            new UILabel(2 * (PW + PP), Y1, getContentWidth() - PW, LH)
                .setLabel("HARNESS").addToContainer(this);

            panel = new UITextBox(0, Y2, PW, H);
            panel.setParameter(pP);
            panel.addToContainer(this);
            panel.setEnabled(false);

            pixliteId = new UIIntegerBox(PW + PP, Y2, PW, H);
            pixliteId.setParameter(pPX);
            pixliteId.addToContainer(this);
            pixliteId.setEnabled(false);

            harness = new UIIntegerBox(2 * (PW + PP), Y2, PW, H);
            harness.setParameter(pH);
            harness.addToContainer(this);
            harness.setEnabled(false);

            harnessIndex = new UIIntegerBox(3 * (PW + PP), Y2, PW, H);
            harnessIndex.setParameter(pHI);
            harnessIndex.addToContainer(this);
            harnessIndex.setEnabled(false);

            new UILabel(0, Y3, getContentWidth(), LH)
                .setLabel("OVERRIDE HEIGHT").addToContainer(this);

            overrideHeight = new UIButton(0, Y4, H, H);
            overrideHeight.setLabel("").setParameter(pOH);
            overrideHeight.addToContainer(this);
            overrideHeight.setEnabled(false);

            yOverride = new UIDoubleBox(H + 4, Y4, PW, H);
            yOverride.setParameter(pYO);
            yOverride.addToContainer(this);
            yOverride.setEnabled(false);
        }

        void setCurrent(FlowerModel fm) {
            currentModel = fm;
            current = fm.getFlowerData();
            reload();

            panel.setEnabled(true);
            pixliteId.setEnabled(true);
            harness.setEnabled(true);
            harnessIndex.setEnabled(true);
            yOverride.setEnabled(pOH.getValueb());
            overrideHeight.setEnabled(true);

            redraw();
        }

        void reload() {
            if (current == null) {
                return;
            }
            loading = true;
            id.setLabel(String.format("FLOWER %04d", current.record.id));
            pP.setValue(current.record.panelId == null ? "" : current.record.panelId);
            pPX.setValue(current.record.pixliteId);
            pH.setValue(current.record.harness);
            pHI.setValue(current.record.harnessIndex);
            pYO.setValue(current.record.yOverride);
            pOH.setValue(current.record.overrideHeight);
            loading = false;
        }

        void onUpdate() {
            if (loading) {
                return;
            }
            current.record.panelId = "".equals(pP.getString()) ? null : pP.getString();
            current.record.pixliteId = pPX.getValuei();
            current.record.harness = pH.getValuei();
            current.record.harnessIndex = pHI.getValuei();
            current.record.overrideHeight = pOH.getValueb();
            current.record.yOverride = pYO.getValuef();
            current.recalculateLocation();
            currentModel.onDataUpdated();
            UIFlowerTool.this.onUpdate();
            ui.preview.pointCloud.updateVertexPositions();
        }
    }

    class PixlitePane extends UICollapsibleSection {
        UIItemList.ScrollList pixliteList;
        int selectedPixlite;
        UILabel selectedLabel;

        HarnessIndexEditParameter[][] harnessEditorP;

        DiscreteParameter swapPixWithP = new DiscreteParameter("swapWith", 2, 2, 256);
        DiscreteParameter swapHarness1P = new DiscreteParameter("swapH1", 1, 1, 5);
        DiscreteParameter swapHarness2P = new DiscreteParameter("swapH2", 2, 1, 5);

        boolean loading = false;

        public PixlitePane() {
            super(ui, 0, 360, UIFlowerTool.this.getContentWidth(), 550);
            setTitle("PIXLITES");

            pixliteList =
                new UIItemList.ScrollList(ui, 0, 2, getContentWidth(), 180);
            pixliteList.addToContainer(this);

            float y = 185;
            selectedLabel = new UILabel(4, y, getContentWidth(), 20);
            selectedLabel.addToContainer(this);
            y += 20;

            new UILabel(4, y, getContentWidth(), 14).setLabel("QUICK FIXES").addToContainer(this);
            y += 18;

            swapPixWithP.setFormatter(v -> Integer.toString((int) v));
            new UILabel(4, y, 100, 20)
                .setLabel("Swap with pixlite")
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .addToContainer(this);
            new UIIntegerBox(100, y, 40, 20)
                .setParameter(swapPixWithP)
                .addToContainer(this);
            final BooleanParameter swapPixGo =
                new BooleanParameter("swap", false).setMode(BooleanParameter.Mode.MOMENTARY);
            swapPixGo.addListener(p -> {
                    if (swapPixGo.getValueb()) {
                        runSwapPixlite();
                    }
                });
            new UIButton(145, y, 40, 20)
                .setLabel("SWAP")
                .setParameter(swapPixGo)
                .addToContainer(this);
            y += 22;

            swapHarness1P.setFormatter(v -> Integer.toString((int) v));
            swapHarness2P.setFormatter(v -> Integer.toString((int) v));
            new UILabel(4, y, 80, 20)
                .setLabel("Swap harness")
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .addToContainer(this);
            new UIIntegerBox(80, y, 40, 20)
                .setParameter(swapHarness1P)
                .addToContainer(this);
            new UILabel(120, y, 35, 20)
                .setLabel("with")
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .addToContainer(this);
            new UIIntegerBox(155, y, 40, 20)
                .setParameter(swapHarness2P)
                .addToContainer(this);

            final BooleanParameter swapHarnessGo =
                new BooleanParameter("swap", false).setMode(BooleanParameter.Mode.MOMENTARY);
            swapHarnessGo.addListener(p -> {
                    if (swapHarnessGo.getValueb()) {
                        runSwapHarness();
                    }
                });
            new UIButton(200, y, 40, 20)
                .setLabel("SWAP")
                .setParameter(swapHarnessGo)
                .addToContainer(this);
            y += 22;

            new UILabel(4, y, getContentWidth(), 14).setLabel("HARNESS EDITOR").addToContainer(this);
            y += 18;

            harnessEditorP = new HarnessIndexEditParameter[4][9];
            for (int h = 0; h < 4; h++) {
                for (int i = 0; i < 9; i++) {
                    harnessEditorP[h][i] = new HarnessIndexEditParameter(h + 1, i + 1, this);
                }
            }

            final float labelWidth = 30;
            final float spacing = (getContentWidth() - labelWidth - 10) / 4;
            final float width = spacing - 4;
            final float height = 20;
            final float[] cols = new float[] {
                4,
                4 + labelWidth,
                4 + labelWidth + spacing,
                4 + labelWidth + 2 * spacing,
                4 + labelWidth + 3 * spacing };
            final float[] widths = new float[] {labelWidth - 4, width, width, width, width};

            for (int i = 0; i <= 9; i++) {
                for (int h = 0; h <= 4; h++) {
                    if (i == 0) {
                        if (h == 0) {
                            continue;
                        }
                        new UILabel(cols[h], y, widths[h], height)
                            .setLabel(String.format("H%d", h))
                            .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                            .addToContainer(this);
                    } else {
                        if (h == 0) {
                            new UILabel(cols[h], y, widths[h], height)
                                .setLabel(String.format("%d", i))
                                .setTextAlignment(PConstants.RIGHT, PConstants.CENTER)
                                .addToContainer(this);
                        } else {
                            new UIIntegerBox(cols[h], y, widths[h], height)
                                .setParameter(harnessEditorP[h - 1][i - 1])
                                .addToContainer(this);
                        }
                    }
                }
                y += 22;
            }

            reload();
        }

        void runSwapPixlite() {
            int a = selectedPixlite;
            int b = swapPixWithP.getValuei();

            for (FlowerModel fm : model.getFlowers()) {
                FlowerData fd = fm.getFlowerData();
                if (fd.record.pixliteId == a) {
                    fd.record.pixliteId = b;
                } else if (fd.record.pixliteId == b) {
                    fd.record.pixliteId = a;
                }
            }

            model.onDataUpdated();
            UIFlowerTool.this.onUpdate();

            selectedPixlite = b;
            swapPixWithP.setValue(a);

            List<? extends UIItemList.Item> items = pixliteList.getItems();
            for (int i = 0; i < items.size(); i++) {
                PixliteItem pi = (PixliteItem) items.get(i);
                if (pi.id == selectedPixlite) {
                    pixliteList.setFocusIndex(i);
                    break;
                }
            }
        }

        void runSwapHarness() {
            int a = swapHarness1P.getValuei();
            int b = swapHarness2P.getValuei();

            for (FlowerModel fm : model.getFlowers()) {
                FlowerData fd = fm.getFlowerData();
                if (fd.record.pixliteId == selectedPixlite) {
                    if (fd.record.harness == a) {
                        fd.record.harness = b;
                    } else if (fd.record.harness == b) {
                        fd.record.harness = a;
                    }
                }
            }

            model.onDataUpdated();
            UIFlowerTool.this.onUpdate();
        }

        void setHarnessIndex(int harness, int index, int newFlowerId) {
            for (FlowerModel fm : model.getFlowers()) {
                FlowerData fd = fm.getFlowerData();
                if (fd.record.id == newFlowerId) {
                    fd.record.pixliteId = selectedPixlite;
                    fd.record.harness = harness;
                    fd.record.harnessIndex = index;
                } else if (fd.record.pixliteId == selectedPixlite) {
                    if (fd.record.harness == harness && fd.record.harnessIndex == index) {
                        fd.record.pixliteId = FlowerRecord.UNKNOWN_PIXLITE_ID;
                        fd.record.harness = FlowerRecord.UNKNOWN_HARNESS;
                        fd.record.harnessIndex = FlowerRecord.UNKNOWN_HARNESS_INDEX;
                    }
                }
            }

            model.onDataUpdated();
            UIFlowerTool.this.onUpdate();
        }

        void reload() {
            List<PixliteItem> items = new ArrayList<>();
            for (Integer pixliteId : model.getPixliteIds()) {
                items.add(new PixliteItem(pixliteId));
            }
            pixliteList.setItems(items);
            reloadSelected();
        }

        void reloadSelected() {
            loading = true;
            selectedLabel.setLabel(
                String.format("PIXLITE " + HHGardenShow.PIXLITE_IP_FORMAT, selectedPixlite));

            Map<Integer, List<FlowerModel>> flowerByHarness = model.getPixliteHarnesses(selectedPixlite);
            StringBuilder sb;

            for (int h = 1; h <= 4; h++) {
                List<FlowerModel> fms =
                    flowerByHarness != null && flowerByHarness.containsKey(h) ?
                    flowerByHarness.get(h) : FlowersModel.emptyDataLine();
                for (int i = 0; i < 9; i++) {
                    harnessEditorP[h - 1][i].setValue(
                        fms.get(i) == null ? -1 : fms.get(i).getFlowerData().record.id);
                }
            }
            loading = false;
            redraw();
        }
    }

    class FlowerItem extends UIItemList.AbstractItem {
        private final FlowerModel model;

        FlowerItem(FlowerModel model) {
            this.model = model;
        }

        @Override
        public String getLabel() {
            return model.getFlowerData().record.toString();
        }

        @Override
        public void onFocus() {
            UIFlowerTool.this.setCurrentFlower(model);
        }
    }

    class PixliteItem extends UIItemList.AbstractItem {
        private final int id;

        PixliteItem(int id) {
            this.id = id;
        }

        @Override
        public String getLabel() {
            return String.format("%d", id);
        }

        @Override
        public void onFocus() {
            pixlites.selectedPixlite = id;
            pixlites.reloadSelected();
            UIFlowerTool.this.setCurrentPixlite(id);
        }
    }

    class HarnessIndexEditParameter extends DiscreteParameter implements LXParameterListener {
        final int h;
        final int i;
        final PixlitePane pp;

        HarnessIndexEditParameter(int h, int i, PixlitePane pp) {
            super(String.format("h%di%d", h, i), -1, -1, 10000);
            this.h = h;
            this.i = i;
            this.pp = pp;
            addListener(this);
            setFormatter(v -> Integer.toString((int) v));
        }

        public void onParameterChanged(LXParameter p) {
            pp.setHarnessIndex(h, i, getValuei());
        }
    }
}
