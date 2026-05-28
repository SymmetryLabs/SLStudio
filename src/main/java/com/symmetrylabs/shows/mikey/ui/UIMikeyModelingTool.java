package com.symmetrylabs.shows.mikey.ui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UITabFocus;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;

public class UIMikeyModelingTool extends UI2dContainer {

    /** Number of Pixlite outputs / ArtNet universes. Fixed at 54. */
    public static final int UNIVERSE_COUNT = 54;

    public static final String[] COLUMN_LABELS = { "tx", "ty", "tz", "az", "px", "d" };
    public static final String MAPPING_FILE = "data/mikey-mapping.json";

    private static final float DEFAULT_BAR_SPACING = 24f;
    private static final int   DEFAULT_PIXELS = 60;
    private static final float DEFAULT_HEIGHT = 1f;
    private static final float DEFAULT_ROTATE_Z = 90f;

    /** Default strips per universe when no file is present. */
    private static final int DEFAULT_STRIPS_PER_UNIVERSE = 1;

    /** Saved file format. */
    public static class MikeyMappingFile {
        public int[]     stripCounts;  // length = UNIVERSE_COUNT
        public float[][] strips;       // length = sum(stripCounts), each row is 6 params
    }

    // ── per-universe count boxes (always 54) ──────────────────────────────────
    private final TabbableTextBox[] countBoxes = new TabbableTextBox[UNIVERSE_COUNT];

    // ── strip parameter boxes, built dynamically ──────────────────────────────
    // Indexed as stripInputs[globalStripIndex][col]
    private List<TabbableTextBox[]> stripInputs = new ArrayList<>();

    // Current per-universe strip counts (mirrors countBoxes values after apply)
    private int[] stripCounts = new int[UNIVERSE_COUNT];

    // Width cache for rebuilding the grid
    private float panelW;

    // Container that holds the strip grid rows (so we can rebuild it)
    private UI2dContainer gridContainer;

    // Y offset of gridContainer within the outer panel (computed once in constructor)
    private float gridContainerY;

    // ─────────────────────────────────────────────────────────────────────────

    public UIMikeyModelingTool(UI ui, float x, float y, float w) {
        super(x, y, w, 2000);  // tall enough; inner content is what matters
        this.panelW = w;
        setPadding(5);
        setLayout(UI2dContainer.Layout.NONE);

        // ── Title ──
        new UILabel(0, 0, w - 10, 16)
            .setLabel("MIKEY MODELER (" + UNIVERSE_COUNT + " universes)")
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor())
            .addToContainer(this);

        // ── Strip-count header label ──
        new UILabel(0, 20, w - 10, 13)
            .setLabel("Strips per universe (U0 … U53):")
            .addToContainer(this);

        // ── 54 count boxes laid out in a compact grid ──
        buildCountBoxes(w);

        // ── Buttons (placed below the count box grid) ──
        final float btnY = countBoxGridBottom() + 6f;
        UIButton applyBtn = new UIButton(0, (int) btnY, 100, 18) {
            @Override
            protected void onToggle(boolean active) {
                if (active) rebuildStripGrid();
            }
        };
        applyBtn.setMomentary(true).setLabel("Apply Counts");
        applyBtn.addToContainer(this);

        UIButton saveRestart = new UIButton(106, (int) btnY, 120, 18) {
            @Override
            protected void onToggle(boolean active) {
                if (active) {
                    saveMappingToDisk();
                    triggerRestart();
                }
            }
        };
        saveRestart.setMomentary(true).setLabel("Save & Restart");
        saveRestart.addToContainer(this);

        // ── Strip grid container (placed below buttons) ──
        gridContainerY = btnY + 18f + 6f;
        gridContainer = new UI2dContainer(0, (int) gridContainerY, w, 4000);
        gridContainer.setLayout(UI2dContainer.Layout.NONE);
        gridContainer.addToContainer(this);

        // ── Load saved data and populate everything ──
        MikeyMappingFile saved = loadFileFromDisk();
        if (saved != null && saved.stripCounts != null && saved.stripCounts.length == UNIVERSE_COUNT) {
            for (int u = 0; u < UNIVERSE_COUNT; u++) {
                stripCounts[u] = Math.max(1, saved.stripCounts[u]);
                countBoxes[u].setValue(String.valueOf(stripCounts[u]));
            }
            buildStripGrid(saved.strips);
        } else {
            for (int u = 0; u < UNIVERSE_COUNT; u++) {
                stripCounts[u] = DEFAULT_STRIPS_PER_UNIVERSE;
                countBoxes[u].setValue(String.valueOf(DEFAULT_STRIPS_PER_UNIVERSE));
            }
            buildStripGrid(null);
        }
    }

    // Layout constants shared between constructor and buildCountBoxes
    static final int   COUNT_BOX_COLS  = 9;
    static final float COUNT_BOX_W     = 22f;
    static final float COUNT_BOX_H     = 16f;
    static final float COUNT_BOX_GAP   = 2f;
    static final float COUNT_BOX_START_Y = 36f;  // below "Strips per universe" label
    // Each cell = label (10px) + box (COUNT_BOX_H) + gap
    static final float COUNT_CELL_H = 10f + COUNT_BOX_H + COUNT_BOX_GAP;

    /** Y coordinate of the first pixel below the count-box grid. */
    static float countBoxGridBottom() {
        int rows = (int) Math.ceil((double) UNIVERSE_COUNT / COUNT_BOX_COLS);
        return COUNT_BOX_START_Y + rows * COUNT_CELL_H;
    }

    // ── Build the 54 count boxes in a 9-column grid ───────────────────────────

    private void buildCountBoxes(float w) {
        for (int u = 0; u < UNIVERSE_COUNT; u++) {
            int col = u % COUNT_BOX_COLS;
            int row = u / COUNT_BOX_COLS;
            float cellX = col * (COUNT_BOX_W + COUNT_BOX_GAP);
            float cellY = COUNT_BOX_START_Y + row * COUNT_CELL_H;
            // Label above box showing universe number 1-54
            new UILabel(cellX, cellY, COUNT_BOX_W, 10f)
                .setLabel(String.valueOf(u + 1))
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .setFontColor(0xFF888888)
                .addToContainer(this);
            TabbableTextBox box = new TabbableTextBox(cellX, cellY + 10f, COUNT_BOX_W, COUNT_BOX_H);
            box.setValue(String.valueOf(DEFAULT_STRIPS_PER_UNIVERSE));
            box.addToContainer(this);
            countBoxes[u] = box;
        }
    }

    // ── Read count boxes → update stripCounts → rebuild grid ─────────────────

    private void rebuildStripGrid() {
        // Snapshot current strip values before clearing
        float[][] oldStrips = snapshotStripValues();
        // Read new counts
        for (int u = 0; u < UNIVERSE_COUNT; u++) {
            try {
                stripCounts[u] = Math.max(1, Integer.parseInt(countBoxes[u].getValue().trim()));
            } catch (NumberFormatException e) {
                stripCounts[u] = 1;
                countBoxes[u].setValue("1");
            }
        }
        buildStripGrid(oldStrips);
    }

    // ── Core grid builder ─────────────────────────────────────────────────────

    private void buildStripGrid(float[][] existingStrips) {
        // Remove all existing children from gridContainer
        for (UIObject child : new ArrayList<>(gridContainer.getChildren())) {
            ((UI2dComponent) child).removeFromContainer();
        }
        stripInputs.clear();

        final float labelColW = 22f;
        final float gap       = 2f;
        final float gridLeft  = labelColW + gap;
        final float boxH      = 16f;
        final float rowH      = boxH + 2f;
        final float uHeaderH  = 14f;
        final float colW      = Math.max(26f, (panelW - gridLeft - 4f - gap * (COLUMN_LABELS.length - 1)) / COLUMN_LABELS.length);

        // Column headers
        for (int c = 0; c < COLUMN_LABELS.length; c++) {
            float cx = gridLeft + c * (colW + gap);
            new UILabel(cx, 0, colW, 12)
                .setLabel(COLUMN_LABELS[c])
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .addToContainer(gridContainer);
        }

        float curY = 14f;
        int globalRow = 0;

        for (int u = 0; u < UNIVERSE_COUNT; u++) {
            int count = stripCounts[u];

            // Universe header (1-indexed)
            new UILabel(0, curY, panelW - 4, uHeaderH - 2)
                .setLabel("U" + (u + 1) + " (" + count + " strip" + (count == 1 ? "" : "s") + ")")
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .setFontColor(0xFFAAAAAA)
                .addToContainer(gridContainer);
            curY += uHeaderH;

            for (int s = 0; s < count; s++) {
                new UILabel(0, curY + 2, labelColW, boxH - 2)
                    .setLabel(String.valueOf(s + 1))
                    .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                    .addToContainer(gridContainer);

                TabbableTextBox[] row = new TabbableTextBox[COLUMN_LABELS.length];
                for (int c = 0; c < COLUMN_LABELS.length; c++) {
                    float cx = gridLeft + c * (colW + gap);
                    TabbableTextBox box = new TabbableTextBox(cx, curY, colW, boxH);
                    float defaultVal = defaultValue(c, globalRow);
                    float val = (existingStrips != null && globalRow < existingStrips.length)
                        ? existingStrips[globalRow][c] : defaultVal;
                    box.setValue(formatNumber(val));
                    box.addToContainer(gridContainer);
                    row[c] = box;
                }
                stripInputs.add(row);
                curY += rowH;
                globalRow++;
            }
        }

        // Resize gridContainer to actual content
        gridContainer.setSize(panelW, curY + 10);
        // Resize outer panel to fit everything
        setSize(panelW, gridContainerY + curY + 20);

        System.out.println("UIMikeyModelingTool: built grid with " + globalRow + " total strips across " + UNIVERSE_COUNT + " universes");
    }

    private float defaultValue(int col, int stripIndex) {
        switch (col) {
            case 0: return DEFAULT_BAR_SPACING * stripIndex;
            case 1: return 0f;
            case 2: return 0f;
            case 3: return DEFAULT_ROTATE_Z;
            case 4: return DEFAULT_PIXELS;
            case 5: return DEFAULT_HEIGHT;
            default: return 0f;
        }
    }

    // ── Snapshot current strip input values into a flat array ─────────────────

    private float[][] snapshotStripValues() {
        int n = stripInputs.size();
        float[][] out = new float[n][COLUMN_LABELS.length];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < COLUMN_LABELS.length; c++) {
                try {
                    out[r][c] = Float.parseFloat(stripInputs.get(r)[c].getValue().trim());
                } catch (NumberFormatException e) {
                    out[r][c] = 0f;
                }
            }
        }
        return out;
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    public void saveMappingToDisk() {
        MikeyMappingFile file = new MikeyMappingFile();
        // Read counts from boxes (in case user edited without pressing Apply)
        file.stripCounts = new int[UNIVERSE_COUNT];
        for (int u = 0; u < UNIVERSE_COUNT; u++) {
            try {
                file.stripCounts[u] = Math.max(1, Integer.parseInt(countBoxes[u].getValue().trim()));
            } catch (NumberFormatException e) {
                file.stripCounts[u] = 1;
            }
        }
        file.strips = snapshotStripValues();
        File f = new File(MAPPING_FILE);
        f.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(f)) {
            new Gson().toJson(file, writer);
            System.out.println("UIMikeyModelingTool: saved " + file.strips.length + " strips to " + MAPPING_FILE);
        } catch (IOException e) {
            System.err.println("UIMikeyModelingTool: failed to save mapping");
            e.printStackTrace();
        }
    }

    public static MikeyMappingFile loadFileFromDisk() {
        File f = new File(MAPPING_FILE);
        if (!f.exists()) {
            System.out.println("UIMikeyModelingTool: no mapping file found, using defaults");
            return null;
        }
        try (FileReader reader = new FileReader(f)) {
            MikeyMappingFile file = new Gson().fromJson(reader, MikeyMappingFile.class);
            if (file == null || file.stripCounts == null || file.strips == null) {
                System.out.println("UIMikeyModelingTool: mapping file empty/invalid, using defaults");
                return null;
            }
            System.out.println("UIMikeyModelingTool: loaded " + file.strips.length + " strips across " + file.stripCounts.length + " universes");
            return file;
        } catch (Exception e) {
            System.err.println("UIMikeyModelingTool: failed to load mapping, using defaults");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the per-universe strip counts from disk, or a default array.
     * Called by MikeyShow at model-build time.
     */
    public static int[] loadStripCountsFromDisk() {
        MikeyMappingFile file = loadFileFromDisk();
        int[] counts = new int[UNIVERSE_COUNT];
        if (file != null && file.stripCounts != null && file.stripCounts.length == UNIVERSE_COUNT) {
            for (int u = 0; u < UNIVERSE_COUNT; u++) {
                counts[u] = Math.max(1, file.stripCounts[u]);
            }
        } else {
            for (int u = 0; u < UNIVERSE_COUNT; u++) {
                counts[u] = DEFAULT_STRIPS_PER_UNIVERSE;
            }
        }
        return counts;
    }

    /**
     * Returns the flat strip parameter array from disk, or null.
     * Called by MikeyShow at model-build time.
     */
    public static float[][] loadStripsFromDisk() {
        MikeyMappingFile file = loadFileFromDisk();
        return (file != null) ? file.strips : null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String formatNumber(float v) {
        if (v == (int) v) return Integer.toString((int) v);
        return Float.toString(v);
    }

    private static void triggerRestart() {
        try {
            File restartFile = new File(".restart");
            if (!restartFile.exists()) restartFile.createNewFile();
            System.out.println("UIMikeyModelingTool: .restart created, exiting JVM to trigger relaunch.");
        } catch (IOException e) {
            System.err.println("UIMikeyModelingTool: failed to create .restart file");
            e.printStackTrace();
            return;
        }
        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
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
