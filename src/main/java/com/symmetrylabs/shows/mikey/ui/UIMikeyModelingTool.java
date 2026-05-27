package com.symmetrylabs.shows.mikey.ui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UITabFocus;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;

public class UIMikeyModelingTool extends UI2dContainer {

    public static final int STRIP_COUNT = 54;
    public static final String[] COLUMN_LABELS = { "tx", "ty", "tz", "az", "px", "d" };

    public static final String MAPPING_FILE = "data/mikey-mapping.json";

    /** Default values that mirror the current MikeyShow.create() mapping. */
    private static final float BAR_SPACING = 24f;
    private static final float STRIP_ROTATE_Z = 1.57f;
    private static final int DEFAULT_PIXELS = 60;
    private static final float DEFAULT_HEIGHT = 1f;

    /** Grid of input boxes [row][col] -- 54 strips x 6 parameters. */
    public final TabbableTextBox[][] inputs = new TabbableTextBox[STRIP_COUNT][COLUMN_LABELS.length];

    public UIMikeyModelingTool(UI ui, float x, float y, float w) {
        super(x, y, w, 1100);  // Extra height to ensure all 54 rows are visible
        setPadding(5);
        setLayout(UI2dContainer.Layout.NONE);  // Use absolute positioning for children

        // Title
        new UILabel(0, 0, w - 10, 16)
            .setLabel("MIKEY MODELER (54 strips)")
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor())
            .addToContainer(this);

        new UILabel(0, 18, w - 10, 15)
            .setLabel("Reload mapping changes:")
            .setPadding(0, 5)
            .addToContainer(this);

        UIButton restart = new UIButton(0, 38, 120, 20) {
            @Override
            protected void onToggle(boolean active) {
                if (active) {
                    saveMappingToDisk();
                    triggerRestart();
                }
            }
        };
        restart.setMomentary(true).setLabel("Restart Software");
        restart.addToContainer(this);

        buildMappingGrid(w);
    }

    private void buildMappingGrid(float w) {
        final float gridTop = 68;  // Adjusted for title (16) + label (15) + button (20) + padding
        final float labelColW = 22;
        final float gap = 2;
        final float gridLeft = labelColW + gap;
        final float boxH = 16;
        final float rowH = boxH + 2;
        final float colW = Math.max(28, (w - gridLeft - 4 - gap * (COLUMN_LABELS.length - 1)) / COLUMN_LABELS.length);

        // Column headers (centered over their columns)
        for (int c = 0; c < COLUMN_LABELS.length; c++) {
            float cx = gridLeft + c * (colW + gap);
            new UILabel(cx, gridTop, colW, 12)
                .setLabel(COLUMN_LABELS[c])
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .addToContainer(this);
        }

        float firstRowY = gridTop + 14;
        float[][] defaults = loadMappingFromDisk();
        if (defaults == null) {
            defaults = buildDefaults();
        }

        for (int r = 0; r < STRIP_COUNT; r++) {
            float ry = firstRowY + r * rowH;
            new UILabel(0, ry + 2, labelColW, boxH - 2)
                .setLabel(String.valueOf(r + 1))
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .addToContainer(this);

            for (int c = 0; c < COLUMN_LABELS.length; c++) {
                float cx = gridLeft + c * (colW + gap);
                TabbableTextBox box = new TabbableTextBox(cx, ry, colW, boxH);
                box.setValue(formatNumber(defaults[r][c]));
                box.addToContainer(this);
                inputs[r][c] = box;
            }
        }
        System.out.println("UIMikeyModelingTool: created UI with " + STRIP_COUNT + " rows (height=" + getHeight() + ")");
    }

    private static String formatNumber(float v) {
        if (v == (int) v) {
            return Integer.toString((int) v);
        }
        return Float.toString(v);
    }

    private static float[][] buildDefaults() {
        float[][] d = new float[STRIP_COUNT][6];
        int barSpacing = 24;
        for (int i = 0; i < STRIP_COUNT; i++) {
            d[i][0] = barSpacing * i; // tx - spaced in a line
            d[i][1] = 0f;           // ty - same y for all
            d[i][2] = 0f;           // tz - same z for all
            d[i][3] = 1.57f;        // rz
            d[i][4] = 60f;          // px
            d[i][5] = 1f;           // h
        }
        return d;
    }

    public void saveMappingToDisk() {
        float[][] values = new float[STRIP_COUNT][COLUMN_LABELS.length];
        for (int r = 0; r < STRIP_COUNT; r++) {
            for (int c = 0; c < COLUMN_LABELS.length; c++) {
                try {
                    values[r][c] = Float.parseFloat(inputs[r][c].getValue().trim());
                } catch (NumberFormatException e) {
                    values[r][c] = 0f;
                }
            }
        }
        File file = new File(MAPPING_FILE);
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(values, writer);
            System.out.println("UIMikeyModelingTool: mapping saved to " + MAPPING_FILE);
        } catch (IOException e) {
            System.err.println("UIMikeyModelingTool: failed to save mapping");
            e.printStackTrace();
        }
    }

    public static float[][] loadMappingFromDisk() {
        File file = new File(MAPPING_FILE);
        if (!file.exists()) {
            System.out.println("UIMikeyModelingTool: no mapping file found, using defaults");
            return null;
        }
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<float[][]>() {}.getType();
            float[][] values = new Gson().fromJson(reader, type);
            int rows = (values != null) ? values.length : 0;
            int cols = (values != null && values.length > 0) ? values[0].length : 0;
            System.out.println("UIMikeyModelingTool: loaded mapping file with " + rows + " rows, " + cols + " cols");
            if (rows == STRIP_COUNT && cols == COLUMN_LABELS.length) {
                System.out.println("UIMikeyModelingTool: using loaded mapping (54 strips)");
                return values;
            }
            System.out.println("UIMikeyModelingTool: WRONG dimensions (got " + rows + "x" + cols
                + ", expected " + STRIP_COUNT + "x" + COLUMN_LABELS.length + "), using defaults");
        } catch (Exception e) {
            System.err.println("UIMikeyModelingTool: failed to load mapping, using defaults");
            e.printStackTrace();
        }
        return null;
    }

    private static void triggerRestart() {
        try {
            File restartFile = new File(".restart");
            if (!restartFile.exists()) {
                restartFile.createNewFile();
            }
            System.out.println("UIMikeyModelingTool: .restart created, exiting JVM to trigger relaunch.");
        } catch (IOException e) {
            System.err.println("UIMikeyModelingTool: failed to create .restart file");
            e.printStackTrace();
            return;
        }
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            System.exit(0);
        }, "MikeyRestart").start();
    }

    /** UITextBox subclass that participates in Tab key focus traversal. */
    public static class TabbableTextBox extends UITextBox implements UITabFocus {
        public TabbableTextBox(float x, float y, float w, float h) {
            super(x, y, w, h);
        }
    }
}
