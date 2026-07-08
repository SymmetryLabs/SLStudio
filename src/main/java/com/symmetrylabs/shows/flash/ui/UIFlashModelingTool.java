package com.symmetrylabs.shows.flash.ui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UITabFocus;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.Strip;
import processing.core.PConstants;

public class UIFlashModelingTool extends UI2dContainer {

    /** Number of Pixlite outputs / ArtNet universes. Fixed at 64. */
    public static final int UNIVERSE_COUNT = 64;

    public static final String[] COLUMN_LABELS = { "tx", "ty", "tz", "az", "rx", "ry", "px", "d", "cv", "grb" };
    public static final String MAPPING_FILE = "data/flash-mapping.json";

    private static final float DEFAULT_BAR_SPACING = 24f;
    private static final int   DEFAULT_PIXELS = 60;
    private static final float DEFAULT_HEIGHT = 1f;
    private static final float DEFAULT_ROTATE_Z = 90f;

    /** Default strips per universe when no file is present. */
    private static final int DEFAULT_STRIPS_PER_UNIVERSE = 1;

    /** Saved file format. */
    public static class MikeyMappingFile {
        public int[]     stripCounts;    // length = UNIVERSE_COUNT
        public float[][] strips;         // length = sum(stripCounts), each row is 6 params
        public String[]  universeLabels; // length = UNIVERSE_COUNT, user-defined labels
    }

    // ── per-universe count boxes (always 54) ──────────────────────────────────
    private final TabbableTextBox[] countBoxes  = new TabbableTextBox[UNIVERSE_COUNT];
    private final TabbableTextBox[] labelBoxes  = new TabbableTextBox[UNIVERSE_COUNT];

    // ── strip parameter boxes, built dynamically ──────────────────────────────
    // Indexed as stripInputs[globalStripIndex][col]  (UIDoubleBox for draggable numeric cols)
    private List<UIDoubleBox[]> stripInputs = new ArrayList<>();


    // Current per-universe strip counts (mirrors countBoxes values after apply)
    private int[] stripCounts = new int[UNIVERSE_COUNT];

    // Width cache for rebuilding the grid
    private float panelW;

    // Container that holds the strip grid rows (so we can rebuild it)
    private UI2dContainer gridContainer;

    // Y offset of gridContainer within the outer panel (computed once in constructor)
    private float gridContainerY;

    // Live model reference for real-time point updates (set via setModel)
    private java.util.List<Strip> liveStrips = null;
    private LXModel liveModel = null;
    private LX liveLX = null;

    // Index of the currently illuminated strip (-1 = none)
    private int illuminatedStrip = -1;
    private heronarts.p3lx.ui.component.UIButton activeLitButton = null;

    // ── Group-select state: strips whose params move together ────────────────
    private final Set<Integer> groupedStrips = new HashSet<>();
    private final List<double[]> lastValues = new ArrayList<>();
    private boolean propagatingGroup = false;

    // ─────────────────────────────────────────────────────────────────────────

    /** Call this after the model is built to enable real-time point dragging. */
    public void setModel(LXModel model, java.util.List<Strip> strips) {
        this.liveModel = model;
        this.liveStrips = strips;
    }

    public void setLX(LX lx) {
        this.liveLX = lx;
    }

    /** Light up one strip via the StripIlluminator ADD channel. */
    private void illuminateStrip(int globalIndex) {
        if (liveLX == null) return;
        // Re-find channel by label in case static ref is stale after project reload
        heronarts.lx.LXChannel ch = com.symmetrylabs.shows.flash.FlashShow.illumChannel;
        if (ch == null || !liveLX.engine.getChannels().contains(ch)) {
            ch = null;
            for (heronarts.lx.LXChannel c : liveLX.engine.getChannels()) {
                if ("StripIllum".equals(c.label.getString())) { ch = c; break; }
            }
        }
        if (ch == null) {
            // Channel was removed by project load — recreate it
            ch = liveLX.engine.addChannel();
            ch.label.setValue("StripIllum");
            ch.fader.setValue(1.0);
            ch.blendMode.setValue(0);
            com.symmetrylabs.shows.flash.FlashShow.illumChannel = ch;
        }
        // Find StripIlluminator fresh from channel pattern list (static ref may be stale after reload)
        com.symmetrylabs.shows.flash.StripIlluminator illum = null;
        for (heronarts.lx.LXPattern p : ch.patterns) {
            if (p instanceof com.symmetrylabs.shows.flash.StripIlluminator) {
                illum = (com.symmetrylabs.shows.flash.StripIlluminator) p;
                break;
            }
        }
        if (illum == null) {
            // Project load cleared the pattern list — re-add it now
            if (liveLX == null) return;
            illum = new com.symmetrylabs.shows.flash.StripIlluminator(liveLX);
            ch.addPattern(illum);
            com.symmetrylabs.shows.flash.FlashShow.illuminator = illum;
        }
        illuminatedStrip = globalIndex;
        ch.fader.setValue(1.0);
        ch.goPattern(illum);
        illum.stripIndex.setValue(globalIndex);
    }

    public UIFlashModelingTool(UI ui, float x, float y, float w) {
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
            .setLabel("Strips per universe (U1 \u2026 U64):")
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
            // Apply universe labels now that labelBoxes have been created by buildStripGrid
            if (saved.universeLabels != null) {
                for (int u = 0; u < UNIVERSE_COUNT; u++) {
                    if (u < saved.universeLabels.length && saved.universeLabels[u] != null && labelBoxes[u] != null) {
                        labelBoxes[u].setValue(saved.universeLabels[u]);
                    }
                }
            }
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
    static final float COUNT_CELL_W = COUNT_BOX_W;

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
            // Universe number label above the count box
            new UILabel(cellX, cellY, COUNT_BOX_W, 10f)
                .setLabel(String.valueOf(u + 1))
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .setFontColor(0xFF888888)
                .addToContainer(this);
            // Count spinner
            TabbableTextBox countBox = new TabbableTextBox(cellX, cellY + 10f, COUNT_BOX_W, COUNT_BOX_H);
            countBox.setValue(String.valueOf(DEFAULT_STRIPS_PER_UNIVERSE));
            countBox.addToContainer(this);
            countBoxes[u] = countBox;
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
        buildStripGrid(existingStrips, -1, -1);
    }

    /**
     * @param insertAfterGlobalIndex  if >= 0, a blank row is inserted after this global strip index
     * @param insertInUniverse        the universe (0-based) that receives the inserted row
     */
    private void buildStripGrid(float[][] existingStrips, int insertAfterGlobalIndex, int insertInUniverse) {
        // Remove all existing children from gridContainer
        for (UIObject child : new ArrayList<>(gridContainer.getChildren())) {
            ((UI2dComponent) child).removeFromContainer();
        }
        stripInputs.clear();
        groupedStrips.clear();
        lastValues.clear();

        final float labelColW = 14f;
        final float gap       = 2f;
        final float plusW     = 12f;
        final float btnsW     = plusW * 2 + gap;  // + and - together (insert/remove strip)
        final float bumpBtnW  = 10f;  // width of each inline ±5 bump button
        final float gridLeft  = labelColW + gap;
        final float boxH      = 16f;
        final float rowH      = boxH + 2f;
        final float uHeaderH  = 14f;

        final float litBtnW  = 18f;  // width of illuminate toggle button
        final float grpBtnW  = 18f;  // width of group-select toggle button
        final float colHdrH = 11f;
        final float xyBoxW  = 26f;  // wider box for tx/ty so 4 digits are visible
        final float azBoxW  = 24f;  // fixed width for az so 3-4 digits are visible
        final float pxBoxW  = 22f;  // fixed width for px (pixel count) so 3 digits are visible
        final float cvBoxW  = 22f;  // fixed width for cv (curve) so 3 digits are visible
        final float grbBoxW = 18f;  // fixed width for grb toggle checkbox

        // tx/ty cell = [-] gap box gap [+]
        final float xyCell    = bumpBtnW + gap + xyBoxW + gap + bumpBtnW;
        // Flexible cols: tz, rx, ry, d  (4 cols) — az, px, cv, grb are fixed width
        final int   otherCols = 4;
        // Each col (flexible or fixed) contributes (width + gap); litBtnW has no trailing gap
        final float fixedUsed = gridLeft + 2*(xyCell+gap) + (azBoxW+gap) + (pxBoxW+gap) + (cvBoxW+gap) + (grbBoxW+gap) + otherCols*gap + (btnsW+gap) + (litBtnW+gap) + grpBtnW;
        final float colWFinal = (panelW - fixedUsed) / otherCols;

        float curY = 0f;
        int globalRow = 0;
        // Track how many source rows we've consumed (insertions don't consume source rows)
        int srcRow = 0;

        for (int u = 0; u < UNIVERSE_COUNT; u++) {
            int count = stripCounts[u];

            // Universe header row: "U1 (N strips):" + name text input
            String headerText = "U" + (u + 1) + " (" + count + " strip" + (count == 1 ? "" : "s") + "):";
            float headerLabelW = 90f;
            new UILabel(0, curY, headerLabelW, uHeaderH - 2)
                .setLabel(headerText != null ? headerText : "")
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .setFontColor(0xFFAAAAAA)
                .addToContainer(gridContainer);
            if (labelBoxes[u] == null) {
                labelBoxes[u] = new TabbableTextBox(headerLabelW + 2, curY, panelW - headerLabelW - 6, uHeaderH - 2);
                labelBoxes[u].setValue("");
            } else {
                labelBoxes[u].setX(headerLabelW + 2);
                labelBoxes[u].setY(curY);
                labelBoxes[u].setSize(panelW - headerLabelW - 6, uHeaderH - 2);
            }
            labelBoxes[u].addToContainer(gridContainer);
            curY += uHeaderH;

            // Column headers for this universe group
            float hdrX = gridLeft;
            for (int c = 0; c < COLUMN_LABELS.length; c++) {
                if (c == 0 || c == 1) {
                    float cellW = bumpBtnW + gap + xyBoxW + gap + bumpBtnW;
                    new UILabel(hdrX, curY, cellW, colHdrH)
                        .setLabel(COLUMN_LABELS[c])
                        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                        .setFontColor(0xFF666666)
                        .addToContainer(gridContainer);
                    hdrX += cellW + gap;
                } else if (c == 3) {
                    new UILabel(hdrX, curY, azBoxW, colHdrH)
                        .setLabel(COLUMN_LABELS[c])
                        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                        .setFontColor(0xFF666666)
                        .addToContainer(gridContainer);
                    hdrX += azBoxW + gap;
                } else if (c == 6) {  // px
                    new UILabel(hdrX, curY, pxBoxW, colHdrH)
                        .setLabel(COLUMN_LABELS[c])
                        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                        .setFontColor(0xFF666666)
                        .addToContainer(gridContainer);
                    hdrX += pxBoxW + gap;
                } else if (c == 8) {  // cv
                    new UILabel(hdrX, curY, cvBoxW, colHdrH)
                        .setLabel(COLUMN_LABELS[c])
                        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                        .setFontColor(0xFF666666)
                        .addToContainer(gridContainer);
                    hdrX += cvBoxW + gap;
                } else if (c == 9) {  // grb
                    new UILabel(hdrX, curY, grbBoxW, colHdrH)
                        .setLabel(COLUMN_LABELS[c])
                        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                        .setFontColor(0xFF666666)
                        .addToContainer(gridContainer);
                    hdrX += grbBoxW + gap;
                } else {
                    new UILabel(hdrX, curY, colWFinal, colHdrH)
                        .setLabel(COLUMN_LABELS[c])
                        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                        .setFontColor(0xFF666666)
                        .addToContainer(gridContainer);
                    hdrX += colWFinal + gap;
                }
            }
            // "lit" column header
            new UILabel(hdrX + btnsW + gap, curY, litBtnW, colHdrH)
                .setLabel("lit")
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .setFontColor(0xFF666666)
                .addToContainer(gridContainer);
            // "grp" column header
            new UILabel(hdrX + btnsW + gap + litBtnW + gap, curY, grpBtnW, colHdrH)
                .setLabel("grp")
                .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                .setFontColor(0xFF666666)
                .addToContainer(gridContainer);
            curY += colHdrH;

            for (int s = 0; s < count; s++) {
                final int capturedGlobalRow = globalRow;
                final int capturedUniverse  = u;

                new UILabel(0, curY + 2, labelColW, boxH - 2)
                    .setLabel(String.valueOf(s + 1))
                    .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                    .addToContainer(gridContainer);

                UIDoubleBox[] row = new UIDoubleBox[COLUMN_LABELS.length];

                // If this is the newly inserted row, fill with defaults; otherwise use source data
                boolean isInserted = (insertAfterGlobalIndex >= 0
                    && u == insertInUniverse
                    && globalRow == insertAfterGlobalIndex + 1);

                final int capturedStripIndex = globalRow;

                // Build a remapped source array always of length COLUMN_LABELS.length (10).
                // Old 6-col files: [tx,ty,tz,az,px,d] → remap to [tx,ty,tz,az,0,0,px,d,0,0]
                // New 10-col files: already correct.
                final float[] srcVals = new float[COLUMN_LABELS.length];
                for (int c = 0; c < COLUMN_LABELS.length; c++) srcVals[c] = defaultValue(c, globalRow);
                if (!isInserted && existingStrips != null && srcRow < existingStrips.length) {
                    float[] src = existingStrips[srcRow];
                    if (src.length >= 10) {
                        // New 10-col format [tx,ty,tz,az,rx,ry,px,d,cv,grb] — copy directly
                        for (int c = 0; c < COLUMN_LABELS.length; c++) srcVals[c] = src[c];
                    } else if (src.length == 6) {
                        // Old 6-col format: [tx,ty,tz,az,px,d]
                        srcVals[0] = src[0]; // tx
                        srcVals[1] = src[1]; // ty
                        srcVals[2] = src[2]; // tz
                        srcVals[3] = src[3]; // az
                        srcVals[4] = 0f;     // rx (new, default 0)
                        srcVals[5] = 0f;     // ry (new, default 0)
                        srcVals[6] = src[4]; // px  ← was at index 4
                        srcVals[7] = src[5]; // d   ← was at index 5
                        srcVals[8] = 0f;     // cv (new, default 0)
                        srcVals[9] = 0f;     // grb (new, default 0 = RGB)
                    } else if (src.length == 8) {
                        // 8-col format: [tx,ty,tz,az,rx,ry,px,d] — no cv/grb yet
                        for (int c = 0; c < 8; c++) srcVals[c] = src[c];
                        srcVals[8] = 0f;     // cv (new, default 0)
                        srcVals[9] = 0f;     // grb (new, default 0 = RGB)
                    } else if (src.length == 9) {
                        // 9-col format: [tx,ty,tz,az,rx,ry,px,d,cv] — no grb yet
                        for (int c = 0; c < 9; c++) srcVals[c] = src[c];
                        srcVals[9] = 0f;     // grb (new, default 0 = RGB)
                    }
                }

                // Compute x positions for each column, with inline bump buttons for X (col 0) and Y (col 1)
                // Layout: [strip#] [X-][tx][X+] [Y-][ty][Y+] [tz] [az] [rx] [ry] [px] [d] [+][-]
                float curX = gridLeft;
                for (int c = 0; c < COLUMN_LABELS.length; c++) {
                    double rangeMin = (c == 6) ? 1 : -9999;  // px col (index 6) min = 1
                    double rangeMax = 9999;
                    // For tx (col 0) and ty (col 1): place [-] box [+] inline
                    if (c == 0 || c == 1) {
                        final float bumpX = curX;
                        final int bumpCol = c;
                        // [-] button
                        UIButton minBtn = new UIButton(bumpX, curY, bumpBtnW, boxH) {
                            @Override protected void onToggle(boolean active) {
                                if (active) {
                                    UIDoubleBox b = row[bumpCol];
                                    if (b != null) b.setValue(b.getValue() - 5);
                                }
                            }
                        };
                        minBtn.setMomentary(true).setLabel("-").addToContainer(gridContainer);
                        curX += bumpBtnW + gap;
                        // value box
                        float boxX = curX;
                        final int capturedCol = c;
                        UIDoubleBox box = new UIDoubleBox(boxX, curY, xyBoxW, boxH) {
                            @Override protected void onValueChange(double value) {
                                handleValueChange(capturedStripIndex, capturedCol, value);
                            }
                        };
                        box.setRange(rangeMin, rangeMax);
                        box.setValue(srcVals[c]);
                        box.addToContainer(gridContainer);
                        row[c] = box;
                        curX += xyBoxW + gap;
                        // [+] button
                        new UIButton(curX, curY, bumpBtnW, boxH) {
                            @Override protected void onToggle(boolean active) {
                                if (active) {
                                    UIDoubleBox b = row[bumpCol];
                                    if (b != null) b.setValue(b.getValue() + 5);
                                }
                            }
                        }.setMomentary(true).setLabel("+").addToContainer(gridContainer);
                        curX += bumpBtnW + gap;
                    } else if (c == 9) {  // grb toggle button
                        float boxW = grbBoxW;
                        final boolean initialGrb = srcVals[c] > 0.5f;
                        heronarts.p3lx.ui.component.UIButton grbBtn = new heronarts.p3lx.ui.component.UIButton(curX, curY, boxW, boxH) {
                            @Override
                            protected void onToggle(boolean active) {
                                setLabel(active ? "grb" : "rgb");
                            }
                        };
                        grbBtn.setMomentary(false).setLabel(initialGrb ? "grb" : "rgb");
                        grbBtn.setActive(initialGrb);
                        grbBtn.addToContainer(gridContainer);
                        // Store a wrapper that exposes the value as double for snapshot compatibility
                        row[c] = new UIDoubleBox(0, 0, 0, 0) {
                            @Override public double getValue() { return grbBtn.isActive() ? 1.0 : 0.0; }
                            @Override public UIDoubleBox setValue(double v) { grbBtn.setActive(v > 0.5); grbBtn.setLabel(v > 0.5 ? "GRB" : "RGB"); return this; }
                        };
                        curX += boxW + gap;
                    } else {
                        // Normal column — az, px, cv get fixed widths; others use colWFinal
                        float boxW = (c == 3) ? azBoxW : (c == 6) ? pxBoxW : (c == 8) ? cvBoxW : colWFinal;
                        final int capturedCol = c;
                        UIDoubleBox box = new UIDoubleBox(curX, curY, boxW, boxH) {
                            @Override protected void onValueChange(double value) {
                                handleValueChange(capturedStripIndex, capturedCol, value);
                            }
                        };
                        box.setRange(rangeMin, rangeMax);
                        box.setValue(srcVals[c]);
                        box.addToContainer(gridContainer);
                        row[c] = box;
                        curX += boxW + gap;
                    }
                }

                // Insert/remove strip buttons at end of row
                new UIButton(curX, curY, plusW, boxH) {
                    @Override
                    protected void onToggle(boolean active) {
                        if (active) insertStripAfter(capturedGlobalRow, capturedUniverse);
                    }
                }.setMomentary(true).setLabel("+").addToContainer(gridContainer);
                final int capturedCount = count;
                new UIButton(curX + plusW + gap, curY, plusW, boxH) {
                    @Override
                    protected void onToggle(boolean active) {
                        if (active) removeStrip(capturedGlobalRow, capturedUniverse, capturedCount);
                    }
                }.setMomentary(true).setLabel("-").addToContainer(gridContainer);

                // Illuminate toggle button — radio-style: only one active at a time
                final int capturedGlobalRowForLit = globalRow;
                heronarts.p3lx.ui.component.UIButton litBtn = new heronarts.p3lx.ui.component.UIButton(curX + btnsW + gap, curY, litBtnW, boxH) {
                    @Override
                    protected void onToggle(boolean active) {
                        if (active) {
                            // Deactivate the previously active button
                            if (activeLitButton != null && activeLitButton != this) {
                                activeLitButton.setActive(false);
                            }
                            activeLitButton = this;
                            illuminateStrip(capturedGlobalRowForLit);
                        } else {
                            if (activeLitButton == this) activeLitButton = null;
                            illuminateStrip(-1);
                        }
                    }
                };
                litBtn.setMomentary(false).setLabel("lit").addToContainer(gridContainer);

                // Group-select toggle button — grouped strips' params move together
                final int capturedGlobalRowForGrp = globalRow;
                heronarts.p3lx.ui.component.UIButton grpBtn = new heronarts.p3lx.ui.component.UIButton(curX + btnsW + gap + litBtnW + gap, curY, grpBtnW, boxH) {
                    @Override
                    protected void onToggle(boolean active) {
                        if (active) {
                            groupedStrips.add(capturedGlobalRowForGrp);
                        } else {
                            groupedStrips.remove(Integer.valueOf(capturedGlobalRowForGrp));
                        }
                    }
                };
                grpBtn.setMomentary(false).setLabel("grp").addToContainer(gridContainer);

                if (!isInserted) srcRow++;
                stripInputs.add(row);
                double[] rowLast = new double[COLUMN_LABELS.length];
                for (int c = 0; c < COLUMN_LABELS.length; c++) rowLast[c] = row[c].getValue();
                lastValues.add(rowLast);
                curY += rowH;
                globalRow++;
            }
        }

        // Resize gridContainer to actual content
        gridContainer.setSize(panelW, curY + 10);
        // Resize outer panel to fit everything
        setSize(panelW, gridContainerY + curY + 20);

        System.out.println("UIFlashModelingTool: built grid with " + globalRow + " total strips across " + UNIVERSE_COUNT + " universes");
    }

    /** Insert a blank strip after globalRow in the given universe, then rebuild. */
    private void insertStripAfter(int globalRow, int universe) {
        float[][] snapshot = snapshotStripValues();
        stripCounts[universe]++;
        countBoxes[universe].setValue(String.valueOf(stripCounts[universe]));
        buildStripGrid(snapshot, globalRow, universe);
    }

    /** Remove the strip at globalRow from the given universe, then rebuild. */
    private void removeStrip(int globalRow, int universe, int universeStripCount) {
        if (universeStripCount <= 1) return;  // keep at least 1 strip per universe
        float[][] snapshot = snapshotStripValues();
        float[][] shrunk = new float[snapshot.length - 1][COLUMN_LABELS.length];
        for (int i = 0, j = 0; i < snapshot.length; i++) {
            if (i != globalRow) shrunk[j++] = snapshot[i];
        }
        stripCounts[universe]--;
        countBoxes[universe].setValue(String.valueOf(stripCounts[universe]));
        buildStripGrid(shrunk);
    }

    /**
     * Called whenever a strip parameter box changes. If the strip is part of the
     * selected group, the same delta is applied to that parameter on every other
     * grouped strip so they move together.
     */
    private void handleValueChange(int stripIndex, int col, double newValue) {
        if (stripIndex < lastValues.size()) {
            double old = lastValues.get(stripIndex)[col];
            lastValues.get(stripIndex)[col] = newValue;
            double delta = newValue - old;
            if (!propagatingGroup && delta != 0 && groupedStrips.contains(stripIndex) && groupedStrips.size() > 1) {
                propagatingGroup = true;
                try {
                    for (Integer gi : groupedStrips) {
                        if (gi == stripIndex || gi >= stripInputs.size()) continue;
                        UIDoubleBox other = stripInputs.get(gi)[col];
                        if (other != null) {
                            other.setValue(other.getValue() + delta);
                        }
                    }
                } finally {
                    propagatingGroup = false;
                }
            }
        }
        updateLiveStrip(stripIndex);
    }

    /** Recompute the LXPoint positions for strip at globalIndex using current box values. */
    private void updateLiveStrip(int globalIndex) {
        if (liveStrips == null || globalIndex >= liveStrips.size()) return;
        if (globalIndex >= stripInputs.size()) return;
        UIDoubleBox[] row = stripInputs.get(globalIndex);
        float tx    = (float) row[0].getValue();
        float ty    = (float) row[1].getValue();
        float tz    = (float) row[2].getValue();
        float az    = (float) row[3].getValue();
        float rx    = (float) row[4].getValue();
        float ry    = (float) row[5].getValue();
        float pitch = (float) row[7].getValue();  // d = pixelPitch/height (col 7)
        float cv    = (float) row[8].getValue();  // cv = bezier curve amount (col 8)

        float rotZRad = (float) Math.toRadians(-(az > 180f ? az - 360f : az));
        float rotXRad = (float) Math.toRadians(-(rx > 180f ? rx - 360f : rx));
        float rotYRad = (float) Math.toRadians(-(ry > 180f ? ry - 360f : ry));

        Strip strip = liveStrips.get(globalIndex);
        LXTransform t = new LXTransform();
        t.translate(tx, ty, tz);
        t.rotateX(rotXRad);
        t.rotateY(rotYRad);
        t.rotateZ(rotZRad);
        List<LXPoint> points = strip.getPoints();
        int n = points.size();
        for (int i = 0; i < n; i++) {
            float tParam = (n > 1) ? (float) i / (n - 1) : 0f;
            float bezier = 4f * cv * tParam * (1f - tParam);  // peaks at middle
            t.push();
            t.translate(pitch * i, 0, bezier);
            points.get(i).update(t.x(), t.y(), t.z());
            t.pop();
        }
        // Notify UIGLPointCloud to re-upload vertex positions to GPU
        if (liveModel != null) {
            liveModel.bang();
        }
    }

    private float defaultValue(int col, int stripIndex) {
        switch (col) {
            case 0: return DEFAULT_BAR_SPACING * stripIndex;  // tx
            case 1: return 0f;   // ty
            case 2: return 0f;   // tz
            case 3: return DEFAULT_ROTATE_Z;  // az
            case 4: return 0f;   // rx
            case 5: return 0f;   // ry
            case 6: return DEFAULT_PIXELS;  // px
            case 7: return DEFAULT_HEIGHT;  // d
            case 8: return 0f;   // cv
            case 9: return 0f;   // grb (0 = RGB, 1 = GRB)
            default: return 0f;
        }
    }

    // ── Snapshot current strip input values into a flat array ─────────────────

    private float[][] snapshotStripValues() {
        int n = stripInputs.size();
        float[][] out = new float[n][COLUMN_LABELS.length];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < COLUMN_LABELS.length; c++) {
                out[r][c] = (float) stripInputs.get(r)[c].getValue();
            }
        }
        return out;
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    public void saveMappingToDisk() {
        MikeyMappingFile file = new MikeyMappingFile();
        // Read counts from boxes (in case user edited without pressing Apply)
        file.stripCounts = new int[UNIVERSE_COUNT];
        file.universeLabels = new String[UNIVERSE_COUNT];
        for (int u = 0; u < UNIVERSE_COUNT; u++) {
            file.stripCounts[u] = stripCounts[u];
            file.universeLabels[u] = labelBoxes[u] != null ? labelBoxes[u].getValue() : "";
        }
        file.strips = snapshotStripValues();
        File f = new File(MAPPING_FILE);
        f.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(f)) {
            new Gson().toJson(file, writer);
            System.out.println("UIFlashModelingTool: saved " + file.strips.length + " strips to " + MAPPING_FILE);
        } catch (IOException e) {
            System.err.println("UIFlashModelingTool: failed to save mapping");
            e.printStackTrace();
        }
    }

    public static MikeyMappingFile loadFileFromDisk() {
        File f = new File(MAPPING_FILE);
        if (!f.exists()) {
            System.out.println("UIFlashModelingTool: no mapping file found, using defaults");
            return null;
        }
        try (FileReader reader = new FileReader(f)) {
            MikeyMappingFile file = new Gson().fromJson(reader, MikeyMappingFile.class);
            if (file == null || file.stripCounts == null || file.strips == null) {
                System.out.println("UIFlashModelingTool: mapping file empty/invalid, using defaults");
                return null;
            }
            System.out.println("UIFlashModelingTool: loaded " + file.strips.length + " strips across " + file.stripCounts.length + " universes");
            return file;
        } catch (Exception e) {
            System.err.println("UIFlashModelingTool: failed to load mapping, using defaults");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the per-universe strip counts from disk, or a default array.
     * Called by FlashShow at model-build time.
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
     * Called by FlashShow at model-build time.
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
        // Use proper PApplet restart which handles cleanup correctly
        SLStudio.applet.restart();
    }

    /** UITextBox subclass that participates in Tab key focus traversal. */
    public static class TabbableTextBox extends UITextBox implements UITabFocus {
        public TabbableTextBox(float x, float y, float w, float h) {
            super(x, y, w, h);
        }
    }

    /** UIDoubleBox with a finer drag sensitivity (1 unit per 3px) for strip params. */
    public static class DraggableDoubleBox extends UIDoubleBox {
        public DraggableDoubleBox(float x, float y, float w, float h) {
            super(x, y, w, h);
        }
    }
}
