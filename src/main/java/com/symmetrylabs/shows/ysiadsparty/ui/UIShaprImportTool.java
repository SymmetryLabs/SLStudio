package com.symmetrylabs.shows.ysiadsparty.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;

import com.symmetrylabs.shows.ysiadsparty.ShaprCubeImporter;
import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UILabel;

/**
 * UTILITY tab panel for importing a Shapr3D export (.obj / .stl) of cube bodies
 * into data/ysiadsparty-cubes.json (plus a paste-able
 * data/ysiadsparty-towerconfig.txt). The model is built once at startup, so a
 * restart is required after importing before the new layout is used.
 */
public class UIShaprImportTool extends UI2dContainer {

    public static final String CUBES_JSON = "data/ysiadsparty-cubes.json";
    public static final String TOWERCONFIG_TXT = "data/ysiadsparty-towerconfig.txt";

    private final LX lx;
    private final UIDoubleBox scaleBox;
    private final UIButton autoUnitsToggle;
    private final UIButton zUpToggle;
    private final UIButton recenterToggle;
    private final UILabel statusLabel;

    public UIShaprImportTool(LX lx, UI ui, float x, float y, float w) {
        super(x, y, w, 140);
        this.lx = lx;
        setPadding(5);
        setLayout(UI2dContainer.Layout.NONE);

        new UILabel(0, 0, w - 10, 16)
            .setLabel("Shapr3D Import")
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor())
            .addToContainer(this);

        new UILabel(0, 22, 40, 16)
            .setLabel("scale")
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor())
            .addToContainer(this);
        scaleBox = new UIDoubleBox(42, 20, 70, 16);
        scaleBox.setRange(0.0001, 10000);
        scaleBox.setValue(1.0);
        scaleBox.addToContainer(this);

        zUpToggle = new UIButton(118, 20, 48, 16);
        zUpToggle.setMomentary(false).setLabel("z-up").setActive(true);
        zUpToggle.addToContainer(this);

        recenterToggle = new UIButton(170, 20, 66, 16);
        recenterToggle.setMomentary(false).setLabel("recenter").setActive(true);
        recenterToggle.addToContainer(this);

        autoUnitsToggle = new UIButton(0, 44, 110, 18);
        autoUnitsToggle.setMomentary(false).setLabel("auto units").setActive(true);
        autoUnitsToggle.addToContainer(this);

        UIButton importBtn = new UIButton(0, 66, 110, 18) {
            @Override
            protected void onToggle(boolean active) {
                if (active) {
                    statusLabel.setLabel("Opening file dialog...");
                    java.awt.EventQueue.invokeLater(() -> {
                        java.awt.FileDialog dialog = new java.awt.FileDialog(
                            (java.awt.Frame) null, "Import Shapr3D file", java.awt.FileDialog.LOAD);
                        dialog.setFilenameFilter((dir, name) -> {
                            String n = name.toLowerCase();
                            return n.endsWith(".obj") || n.endsWith(".stl");
                        });
                        dialog.setVisible(true);
                        String fname = dialog.getFile();
                        dialog.dispose();
                        if (fname == null) {
                            statusLabel.setLabel("Import cancelled.");
                            return;
                        }
                        File picked = new File(dialog.getDirectory(), fname);
                        lx.engine.addTask(() -> importFile(picked));
                    });
                }
            }
        };
        importBtn.setMomentary(true).setLabel("Import Cubes...");
        importBtn.addToContainer(this);

        UIButton clearBtn = new UIButton(118, 66, 110, 18) {
            @Override
            protected void onToggle(boolean active) {
                if (active) clearImport();
            }
        };
        clearBtn.setMomentary(true).setLabel("Clear Import");
        clearBtn.addToContainer(this);

        statusLabel = new UILabel(0, 92, w - 10, 40);
        statusLabel.setLabel("Import a Shapr3D .obj (recommended) or .stl; tiny pieces are merged into cubes and the file units are detected automatically. Restart to apply.")
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor());
        statusLabel.addToContainer(this);
    }

    private void importFile(File f) {
        if (f == null) return;
        ShaprCubeImporter.ImportResult result;
        try {
            result = ShaprCubeImporter.importCubesAuto(
                f,
                autoUnitsToggle.isActive() ? ShaprCubeImporter.AUTO_SCALE : (float) scaleBox.getValue(),
                zUpToggle.isActive(), recenterToggle.isActive());
        } catch (Exception e) {
            statusLabel.setLabel("Import failed: " + e.getMessage());
            return;
        }
        List<ShaprCubeImporter.Cube> cubes = result.cubes;
        if (cubes == null || cubes.isEmpty()) {
            statusLabel.setLabel("No cube bodies found in " + f.getName());
            return;
        }
        try {
            writeOutputs(cubes);
        } catch (IOException e) {
            statusLabel.setLabel("Failed to write " + CUBES_JSON + ": " + e.getMessage());
            return;
        }
        String status = "Imported " + cubes.size() + " cubes";
        if (result.unitName != null) {
            status += " (units: " + result.unitName + ", scale " + result.scaleUsed + ")";
        }
        statusLabel.setLabel(status + " -> restart to apply");
    }

    private void clearImport() {
        File f = new File(CUBES_JSON);
        if (f.exists() && !f.delete()) {
            statusLabel.setLabel("Failed to delete " + CUBES_JSON);
            return;
        }
        statusLabel.setLabel("Import cleared -> restart to use hardcoded TOWER_CONFIG");
    }

    /** Writes the cubes JSON plus a paste-able TowerConfig listing. */
    private static void writeOutputs(List<ShaprCubeImporter.Cube> cubes) throws IOException {
        File json = new File(CUBES_JSON);
        json.getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(json)) {
            new Gson().toJson(cubes, w);
        }

        File txt = new File(TOWERCONFIG_TXT);
        try (FileWriter w = new FileWriter(txt)) {
            for (ShaprCubeImporter.Cube c : cubes) {
                w.write("        new TowerConfig(" + fmt(c.x) + "f, " + fmt(c.y) + "f, " + fmt(c.z)
                    + "f, 0, " + fmt(c.ry) + "f, 0, new String[] {\"\"}),\n");
            }
        }
    }

    /** Trim to at most 3 decimals (values are already rounded by the importer). */
    private static String fmt(float v) {
        if (v == (int) v) return Integer.toString((int) v);
        return Float.toString(v);
    }
}
