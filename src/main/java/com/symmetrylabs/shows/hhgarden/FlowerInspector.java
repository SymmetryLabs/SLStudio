package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.hhgarden.FlowerModel.FlowerPoint;
import com.symmetrylabs.util.IdFlasher;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import com.symmetrylabs.util.TextMarker;
import processing.core.PVector;
import heronarts.lx.transform.LXVector;
import heronarts.lx.parameter.CompoundParameter;

public class FlowerInspector extends FlowerPattern implements UIFlowerTool.Listener, MarkerSource {
    public static final String GROUP_NAME = HHGardenShow.SHOW_NAME;

    public static enum Mode {
        ALL,
        PANEL,
        PIXLITE,
        HARNESS,
        HARNESS_INDEX
    };

    private final EnumParameter<Mode> mode = new EnumParameter<Mode>("mode", Mode.ALL);
    private final DiscreteParameter panel = new DiscreteParameter("panel", new String[]{"ALL"});
    private final DiscreteParameter pixlite = new DiscreteParameter("pixlite", new String[]{"ALL"});
    private final DiscreteParameter flowerParam;
    private final CompoundParameter labelSize = new CompoundParameter("labelSize", 8, 0, 18);

    private final BooleanParameter prevPanel =
        new BooleanParameter("-Panel", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter nextPanel =
        new BooleanParameter("+Panel", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter clearPanel =
        new BooleanParameter("clrPanel", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter prevPixlite =
        new BooleanParameter("-Pixlite", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter nextPixlite =
        new BooleanParameter("+Pixlite", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter clearPixlite =
        new BooleanParameter("clrPixlite", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter showUnmapped =
        new BooleanParameter("showUnmapped", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter flashId =
        new BooleanParameter("flashId", false);

    private static final String OPTION_ALL = "ALL";

    private final IdFlasher flasher = new IdFlasher(0.12, 0.06, 1.5, 4);

    private static final int[] COLORS = new int[] {
        0xFFFF0000,
        0xFFFFFF00,
        0xFF00FF00,
        0xFF00FFFF,
        0xFF0000FF,
        0xFFFF00FF,
    };

    private static final int[] COLORS9 = new int[] {
        0xFFFF0000,
        0xFFFF4400,
        0xFFFFFF00,
        0xFF66FF00,
        0xFF00FF00,
        0xFF00FFFF,
        0xFF0000FF,
        0xFFAA00FF,
        0xFFFF00FF,
    };

    public FlowerInspector(LX lx) {
        super(lx);
        flowerParam = new DiscreteParameter("flower", -1, -1, model.getFlowers().size());
        addParameter(flowerParam);
        addParameter(mode);
        addParameter(panel);
        addParameter(pixlite);

        addParameter(prevPanel);
        addParameter(nextPanel);
        addParameter(clearPanel);
        addParameter(prevPixlite);
        addParameter(nextPixlite);
        addParameter(clearPixlite);

        addParameter(showUnmapped);
        addParameter(labelSize);
        addParameter(flashId);
        flashId.addListener(param -> { flasher.restart(); });
    }

    private FlowerModel getSelected() {
        int i = flowerParam.getValuei();
        return i < 0 ? null : model.getFlowers().get(i);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == prevPanel && prevPanel.getValueb()) {
            panel.setValue((panel.getValuei() - 1) % panel.getRange());
        } else if (p == nextPanel && nextPanel.getValueb()) {
            panel.setValue((panel.getValuei() + 1) % panel.getRange());
        } else if (p == clearPanel && clearPanel.getValueb()) {
            panel.setValue(0);
        } else if (p == prevPixlite && prevPixlite.getValueb()) {
            pixlite.setValue((pixlite.getValuei() - 1) % pixlite.getRange());
        } else if (p == nextPixlite && nextPixlite.getValueb()) {
            pixlite.setValue((pixlite.getValuei() + 1) % pixlite.getRange());
        } else if (p == clearPixlite && clearPixlite.getValueb()) {
            pixlite.setValue(0);
        }
    }

    @Override
    public void run(double elapsedMs) {
        Arrays.fill(colors, 0);
        Mode m = mode.getEnum();
        String activePanel = panel.getOption();
        if (activePanel.equals(OPTION_ALL)) {
            activePanel = null;
        }
        int activePixlite = pixlite.getOption().equals(OPTION_ALL) ?
            -1 : Integer.parseInt(pixlite.getOption());
        flasher.advance(elapsedMs / 1000);

        for (FlowerModel fm : model.getFlowers()) {
            FlowerData fd = fm.getFlowerData();

            int panelColor = fd.record.panelId == null ?
                0xFF000000 : COLORS[fd.record.panelId.hashCode() % COLORS.length];
            int pixliteColor = COLORS[fd.record.pixliteId % COLORS.length];
            int harnessColor = fd.record.harness == FlowerRecord.UNKNOWN_HARNESS ?
                0xFF000000 : COLORS[(fd.record.harness - 1) % COLORS.length];
            int harnessIndexColor = fd.record.harnessIndex == FlowerRecord.UNKNOWN_HARNESS_INDEX ?
                0xFF000000 : COLORS9[fd.record.harnessIndex - 1];

            boolean matches =
                activePanel == null
                || (fd.record.panelId != null && activePanel.equals(fd.record.panelId));
            matches = matches && (
                activePixlite == -1 || activePixlite == fd.record.pixliteId);

            if (matches) {
                for (FlowerPoint p : fm.getFlowerPoints()) {
                    switch (p.direction) {
                        case A:
                            if (m == Mode.ALL || m == Mode.PANEL) colors[p.index] = panelColor;
                            break;
                        case B:
                            if (m == Mode.ALL || m == Mode.HARNESS) colors[p.index] = harnessColor;
                            break;
                        case C:
                            if (m == Mode.ALL || m == Mode.HARNESS_INDEX) colors[p.index] = harnessIndexColor;
                            break;
                        case UP:
                            if (m == Mode.ALL || m == Mode.PIXLITE) colors[p.index] = pixliteColor;
                            break;
                    }

                    if (flashId.isOn()) {
                        colors[p.index] = flasher.getColor(fd.record.id);
                    }
                }
            }
        }
        FlowerModel selected = getSelected();
        if (selected != null) {
            for (LXPoint p : selected.points) {
                colors[p.index] = 0xFFFFFFFF;
            }
        }
    }

    @Override
    public String getCaption() {
        FlowerModel selected = getSelected();
        String fstr = selected == null ? "NONE" : selected.getFlowerData().toString();
        String panstr = panel.getOption();
        String pixstr = pixlite.getOption();
        String caption = String.format("flower %s / panel %s / pixlite %s", fstr, panstr, pixstr);

        if (showUnmapped.isOn()) {
            List<Integer> unassignedIds = getUnassignedIds();
            String unassignedList = "";
            for (int id : unassignedIds) {
                if (!unassignedList.isEmpty()) unassignedList += ", ";
                unassignedList += id;
            }
            int count = model.getFlowers().size();
            caption += String.format("\n%d of %d flowers unassigned (%s)",
                unassignedIds.size(), count, unassignedList);
        }
        return caption;
    }

    public List<Integer> getUnassignedIds() {
        List<Integer> ids = new ArrayList<>();
        for (FlowerModel fm : model.getFlowers()) {
            FlowerRecord record = fm.getFlowerData().record;
            if (!record.isAssigned()) {
                ids.add(record.id);
            }
        }
        return ids;
    }

    @Override
    public void onActive() {
        super.onActive();
        UIFlowerTool.get().addListener(this);
        onUpdate();
    }

    @Override
    public void onInactive() {
        super.onInactive();
        UIFlowerTool.get().removeListener(this);
    }

    @Override
    public void onFlowerSelected(FlowerData data) {
        List<FlowerModel> flowers = model.getFlowers();
        for (int i = 0; i < flowers.size(); i++) {
            if (flowers.get(i).getFlowerData().record.id == data.record.id) {
                flowerParam.setValue(i);
                return;
            }
        }
    }

    @Override
    public void onPixliteSelected(int pixliteId) {
        String[] options = pixlite.getOptions();
        String idStr = Integer.toString(pixliteId);
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(idStr)) {
                pixlite.setValue(i);
                return;
            }
        }
    }

    @Override
    public void onUpdate() {
        List<PanelRecord> panels = model.getPanels();
        String[] panelIds = new String[panels.size() + 1];
        panelIds[0] = OPTION_ALL;
        for (int i = 0; i < panels.size(); i++) {
            panelIds[i + 1] = panels.get(i).id;
        }
        panel.setOptions(panelIds);

        HashSet<Integer> pixliteIdSet = new HashSet<>();
        for (FlowerModel fm : model.getFlowers()) {
            pixliteIdSet.add(fm.getFlowerData().record.pixliteId);
        }
        List<Integer> pixliteIds = new ArrayList<>(pixliteIdSet);
        Collections.sort(pixliteIds);
        String[] pixliteOptions = new String[pixliteIds.size() + 1];
        pixliteOptions[0] = OPTION_ALL;
        for (int i = 0; i < pixliteIds.size(); i++) {
            pixliteOptions[i + 1] = Integer.toString(pixliteIds.get(i));
        }
        pixlite.setOptions(pixliteOptions);
    }

    @Override
    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        float size = labelSize.getValuef();
        if (size < 0.1) {
            return markers;
        }
        for (FlowerModel fm : model.getFlowers()) {
            FlowerData fd = fm.getFlowerData();
            LXVector lxloc = fd.location;
            PVector loc = new PVector(lxloc.x, lxloc.y, lxloc.z);
            markers.add(
                new TextMarker(
                    loc, size, 0xFFFFFFFF, TextMarker.FlipDir.Z,
                    String.format("%d", fd.record.id)));
        }
        return markers;
    }
}
