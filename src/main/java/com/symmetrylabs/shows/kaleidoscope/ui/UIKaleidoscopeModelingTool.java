package com.symmetrylabs.shows.kaleidoscope.ui;

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

public class UIKaleidoscopeModelingTool extends UI2dContainer {

    /** Number of controller output channels across both WLED controllers. */
    public static final int CHANNEL_COUNT = 12;

    public static final String[] COLUMN_LABELS = { "tx", "ty", "tz", "az", "rx", "ry", "px", "d", "cv", "grb", "cir", "dia" };
    public static final String MAPPING_FILE = "data/kaleidoscope-mapping.json";

    private static final float DEFAULT_BAR_SPACING = 24f;
    private static final int   DEFAULT_PIXELS = 60;
    private static final float DEFAULT_HEIGHT = 1f;
    private static final float DEFAULT_ROTATE_Z = 90f;
    private static final float DEFAULT_DIAMETER = 24f;

    /** Default strips per channel when no file is present. */
    private static final int DEFAULT_STRIPS_PER_CHANNEL = 1;

    /** Saved file format. */
    public static class MikeyMappingFile {
        public int[]     stripCounts;    // length = CHANNEL_COUNT
        public float[][] strips;         // length = sum(stripCounts), each row is 6 params
        public String[]  universeLabels; // length = CHANNEL_COUNT, user-defined labels
    }

    // ── per-channel count boxes and mute buttons ──────────────────────────────
    private final TabbableTextBox[] countBoxes  = new TabbableTextBox[CHANNEL_COUNT];
    private final TabbableTextBox[] labelBoxes  = new TabbableTextBox[CHANNEL_COUNT];
    private final UIButton[]        muteButtons = new UIButton[CHANNEL_COUNT];

    // ── strip parameter boxes, built dynamically ──────────────────────────────
    // Indexed as stripInputs[globalStripIndex][col]  (UIDoubleBox for draggable numeric cols)
    private List<UIDoubleBox[]> stripInputs = new ArrayList<>();


    // Current per-channel strip counts (mirrors countBoxes values after apply)
    private int[] stripCounts = new int[CHANNEL_COUNT];

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
        heronarts.lx.LXChannel ch = com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.illumChannel;
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
            com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.illumChannel = ch;
        }
        // Find StripIlluminator fresh from channel pattern list (static ref may be stale after reload)
        com.symmetrylabs.shows.kaleidoscope.StripIlluminator illum = null;
        for (heronarts.lx.LXPattern p : ch.patterns) {
            if (p instanceof com.symmetrylabs.shows.kaleidoscope.StripIlluminator) {
                illum = (com.symmetrylabs.shows.kaleidoscope.StripIlluminator) p;
                break;
            }
        }
        // If the model has been rebuilt, the old illuminator still references the old model/point indices,
        // so lighting a strip by global index can illuminate the wrong points across multiple channels.
        if (illum != null && liveLX.model != null && liveLX.model != illum.getModel()) {
            ch.removePattern(illum);
            illum = null;
        }
        if (illum == null) {
            // Project load cleared the pattern list, or model was rebuilt — re-add it now
            if (liveLX == null) return;
            illum = new com.symmetrylabs.shows.kaleidoscope.StripIlluminator(liveLX);
            ch.addPattern(illum);
            com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.illuminator = illum;
        }
        illuminatedStrip = globalIndex;
        ch.fader.setValue(1.0);
        ch.goPattern(illum);
        illum.stripIndex.setValue(globalIndex);
    }

    public UIKaleidoscopeModelingTool(UI ui, float x, float y, float w) {
        super(x, y, w, 2000);  // tall enough; inner content is what matters
        this.panelW = w;
        setPadding(5);
        setLayout(UI2dContainer.Layout.NONE);

        // ── Title ──
        new UILabel(0, 0, w - 10, 16)
            .setLabel("KALEIDOSCOPE MODELER (" + CHANNEL_COUNT + " channels, " + com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.CONTROLLER_IPS.length + " controllers)")
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor())
            .addToContainer(this);

        // ── Strip-count header label ──
        new UILabel(0, 20, w - 10, 13)
            .setLabel("Strips per channel (Ch1 \u2026 Ch" + CHANNEL_COUNT + "):")
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
        if (saved != null && saved.stripCounts != null && saved.stripCounts.length > 0) {
            for (int u = 0; u < CHANNEL_COUNT; u++) {
                stripCounts[u] = (u < saved.stripCounts.length) ? Math.max(0, saved.stripCounts[u]) : 0;
                countBoxes[u].setValue(String.valueOf(stripCounts[u]));
            }
            buildStripGrid(saved.strips);
            // Apply channel labels now that labelBoxes have been created by buildStripGrid
            if (saved.universeLabels != null) {
                for (int u = 0; u < CHANNEL_COUNT; u++) {
                    if (u < saved.universeLabels.length && saved.universeLabels[u] != null && labelBoxes[u] != null) {
                        labelBoxes[u].setValue(saved.universeLabels[u]);
                    }
                }
            }
        } else {
            for (int u = 0; u < CHANNEL_COUNT; u++) {
                stripCounts[u] = DEFAULT_STRIPS_PER_CHANNEL;
                countBoxes[u].setValue(String.valueOf(DEFAULT_STRIPS_PER_CHANNEL));
            }
            buildStripGrid(null);
        }
    }

    // Layout constants shared between constructor and buildCountBoxes
    static final int   COUNT_BOX_COLS  = 4;
    static final float COUNT_BOX_W     = 22f;
    static final float COUNT_BOX_H     = 16f;
    static final float COUNT_BOX_GAP   = 2f;
    static final float COUNT_BOX_START_Y = 36f;  // below "Strips per channel" label
    static final float COUNT_MUTE_BTN_H = 14f;  // height of the per-channel mute toggle button
    // Each cell = mute button + box (COUNT_BOX_H) + gap
    static final float COUNT_CELL_H = COUNT_MUTE_BTN_H + COUNT_BOX_H + COUNT_BOX_GAP;
    static final float COUNT_CELL_W = COUNT_BOX_W;

    /** Y coordinate of the first pixel below the count-box grid. */
    static float countBoxGridBottom() {
        int rows = (int) Math.ceil((double) CHANNEL_COUNT / COUNT_BOX_COLS);
        return COUNT_BOX_START_Y + rows * COUNT_CELL_H;
    }

    // ── Build the CHANNEL_COUNT count boxes in a 4-column grid with mute toggle ──
    // Row 0 = controller 1's channels 1-4, rows 1-2 = controller 2's channels 5-12.

    private void buildCountBoxes(float w) {
        for (int u = 0; u < CHANNEL_COUNT; u++) {
            int col = u % COUNT_BOX_COLS;
            int row = u / COUNT_BOX_COLS;
            float cellX = col * (COUNT_BOX_W + COUNT_BOX_GAP);
            float cellY = COUNT_BOX_START_Y + row * COUNT_CELL_H;
            // Channel number as a mute toggle button (ON = sending, OFF = muted)
            final int capturedCh = u;
            UIButton muteBtn = new UIButton(cellX, cellY, COUNT_BOX_W, COUNT_MUTE_BTN_H) {
                @Override
                protected void onToggle(boolean active) {
                    // active=true means ON (sending), active=false means muted
                    com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.KaleidoscopePixlite
                        .setChannelMuted(capturedCh, !active);
                }
            };
            muteBtn.setMomentary(false)
                   .setLabel(String.valueOf(u + 1))
                   .setActive(true);
            muteBtn.addToContainer(this);
            muteButtons[u] = muteBtn;
            // Count spinner
            TabbableTextBox countBox = new TabbableTextBox(cellX, cellY + COUNT_MUTE_BTN_H, COUNT_BOX_W, COUNT_BOX_H);
            countBox.setValue(String.valueOf(DEFAULT_STRIPS_PER_CHANNEL));
            countBox.addToContainer(this);
            countBoxes[u] = countBox;
        }
    }

    // ── Apply count boxes → adjust stripCounts per channel → rebuild grid ─────
    // Counts are applied per channel: a channel grows by appending copies of its
    // last strip (or the previous strip overall / defaults if it was empty) and
    // shrinks by trimming from the end, so other channels' rows never shift.

    private void rebuildStripGrid() {
        // Snapshot current strip values and counts before clearing
        float[][] old = snapshotStripValues();
        int[] oldCounts = stripCounts.clone();
        // Read new counts
        for (int u = 0; u < CHANNEL_COUNT; u++) {
            try {
                stripCounts[u] = Math.max(0, Integer.parseInt(countBoxes[u].getValue().trim()));
            } catch (NumberFormatException e) {
                stripCounts[u] = 0;
                countBoxes[u].setValue("0");
            }
        }
        List<float[]> rebuilt = new ArrayList<>();
        int oldOffset = 0;
        for (int u = 0; u < CHANNEL_COUNT; u++) {
            int keep = Math.min(oldCounts[u], stripCounts[u]);
            for (int i = 0; i < keep && oldOffset + i < old.length; i++) {
                rebuilt.add(old[oldOffset + i]);
            }
            if (stripCounts[u] > oldCounts[u]) {
                // Template: this channel's last kept row, else the last row already
                // appended (previous strip overall), else defaults.
                float[] template = null;
                if (keep > 0) {
                    template = rebuilt.get(rebuilt.size() - 1);
                } else if (!rebuilt.isEmpty()) {
                    template = rebuilt.get(rebuilt.size() - 1);
                }
                for (int i = oldCounts[u]; i < stripCounts[u]; i++) {
                    if (template != null) {
                        rebuilt.add(template.clone());
                    } else {
                        float[] d = new float[COLUMN_LABELS.length];
                        for (int c = 0; c < COLUMN_LABELS.length; c++) d[c] = defaultValue(c, rebuilt.size());
                        rebuilt.add(d);
                    }
                }
            }
            oldOffset += oldCounts[u];  // trimmed rows are skipped
        }
        buildStripGrid(rebuilt.toArray(new float[0][]));
    }

    // ── Core grid builder ─────────────────────────────────────────────────────

    private void buildStripGrid(float[][] existingStrips) {
        buildStripGrid(existingStrips, null, -1, -1);
    }

    /**
     * @param insertRows              if non-null, these rows (each COLUMN_LABELS.length values) are inserted after insertAfterGlobalIndex
     * @param insertAfterGlobalIndex  if >= 0, the insertRows are inserted after this global strip index
     * @param insertInChannel         the channel (0-based) that receives the inserted rows
     */
    private void buildStripGrid(float[][] existingStrips, float[][] insertRows, int insertAfterGlobalIndex, int insertInChannel) {
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
        final float pxBoxW  = 32f;  // fixed width for px (pixel count) so 3 digits are clearly visible
        final float cvBoxW  = 22f;  // fixed width for cv (curve) so 3 digits are visible
        final float grbBoxW = 18f;  // fixed width for grb toggle checkbox
        final float cirBoxW = 18f;
        final float diaBoxW = 28f;

        // tx/ty cell = [-] gap box gap [+]
        final float xyCell    = bumpBtnW + gap + xyBoxW + gap + bumpBtnW;
        // Flexible cols: tz, rx, ry, d  (4 cols) — az, px, cv, grb are fixed width
        final int   otherCols = 4;
        // Each col (flexible or fixed) contributes (width + gap); litBtnW has no trailing gap
        final float fixedUsed = gridLeft + 2*(xyCell+gap) + (azBoxW+gap) + (pxBoxW+gap) + (cvBoxW+gap) + (grbBoxW+gap) + (cirBoxW+gap) + (diaBoxW+gap) + otherCols*gap + (btnsW+gap) + (litBtnW+gap) + grpBtnW;
        final float colWFinal = (panelW - fixedUsed) / otherCols;

        float curY = 0f;
        int globalRow = 0;
        // Track how many source rows we've consumed (insertions don't consume source rows)
        int srcRow = 0;
        // Running pixel offset within the current controller's flat LED buffer;
        // resets at each controller's first channel.
        int controllerPixelOffset = 0;

        for (int u = 0; u < CHANNEL_COUNT; u++) {
            int count = stripCounts[u];
            int controller = com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.controllerForChannel(u);
            if (u == com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.firstChannelOfController(controller)) {
                controllerPixelOffset = 0;
            }

            // Pre-pass: sum this channel's pixel counts (same source-selection logic the
            // rows will use) so the header can show the ArtNet universe range it spans.
            int chPixels = 0;
            {
                int simSrcRow = srcRow;
                for (int s = 0; s < count; s++) {
                    int g = globalRow + s;
                    int off = (insertRows != null && u == insertInChannel) ? g - insertAfterGlobalIndex - 1 : -1;
                    boolean ins = off >= 0 && off < insertRows.length;
                    float[] vals = resolveRowValues(insertRows, off, existingStrips, ins ? -1 : simSrcRow, g);
                    chPixels += (int) vals[6];
                    if (!ins) simSrcRow++;
                }
            }

            // Channel header row: "Ch1 10.200.1.2 (N strips, Mpx, Ua-Ub):" + name text input
            String ip = com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.CONTROLLER_IPS[controller];
            String headerText;
            if (chPixels == 0) {
                headerText = "Ch" + (u + 1) + " " + ip + " (0 strips)";
            } else {
                int firstU = com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.ARTNET_START_UNIVERSE
                    + controllerPixelOffset / com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.PIXELS_PER_UNIVERSE;
                int lastU = com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.ARTNET_START_UNIVERSE
                    + (controllerPixelOffset + chPixels - 1) / com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.PIXELS_PER_UNIVERSE;
                headerText = "Ch" + (u + 1) + " " + ip + " (" + count + " strip" + (count == 1 ? "" : "s") +
                    ", " + chPixels + "px, U" + firstU + "-U" + lastU + ")";
            }
            controllerPixelOffset += chPixels;
            float headerLabelW = 170f;
            new UILabel(0, curY, headerLabelW, uHeaderH - 2)
                .setLabel(headerText != null ? headerText : "")
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .setFontColor(0xFFAAAAAA)
                .addToContainer(gridContainer);
            if (labelBoxes[u] == null) {
                labelBoxes[u] = new TabbableTextBox(headerLabelW + 2, curY, panelW - headerLabelW - 6 - plusW - gap, uHeaderH - 2);
                labelBoxes[u].setValue("");
            } else {
                labelBoxes[u].setX(headerLabelW + 2);
                labelBoxes[u].setY(curY);
                labelBoxes[u].setSize(panelW - headerLabelW - 6 - plusW - gap, uHeaderH - 2);
            }
            labelBoxes[u].addToContainer(gridContainer);
            // Channel-level "+" appends a strip (or the group selection) at the end of
            // this channel — the only way to add strips to a channel that has none.
            final int capturedChannelForHeader = u;
            new UIButton(panelW - plusW - 6, curY, plusW, uHeaderH - 2) {
                @Override
                protected void onToggle(boolean active) {
                    if (active) insertStripAtChannelEnd(capturedChannelForHeader);
                }
            }.setMomentary(true).setLabel("+").addToContainer(gridContainer);
            curY += uHeaderH;

            // Column headers for this channel group
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
                } else if (c == 10) {
                    new UILabel(hdrX, curY, cirBoxW, colHdrH)
                        .setLabel(COLUMN_LABELS[c])
                        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                        .setFontColor(0xFF666666)
                        .addToContainer(gridContainer);
                    hdrX += cirBoxW + gap;
                } else if (c == 11) {
                    new UILabel(hdrX, curY, diaBoxW, colHdrH)
                        .setLabel(COLUMN_LABELS[c])
                        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                        .setFontColor(0xFF666666)
                        .addToContainer(gridContainer);
                    hdrX += diaBoxW + gap;
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
                final int capturedChannel   = u;

                new UILabel(0, curY + 2, labelColW, boxH - 2)
                    .setLabel(String.valueOf(s + 1))
                    .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                    .addToContainer(gridContainer);

                UIDoubleBox[] row = new UIDoubleBox[COLUMN_LABELS.length];

                // If this row is one of the newly inserted rows, take its values from insertRows; otherwise use source data
                int insertOffset = (insertRows != null && u == insertInChannel) ? globalRow - insertAfterGlobalIndex - 1 : -1;
                boolean isInserted = insertOffset >= 0 && insertOffset < insertRows.length;

                final int capturedStripIndex = globalRow;

                final float[] srcVals = resolveRowValues(insertRows, insertOffset, existingStrips, srcRow, globalRow);

                // Compute x positions for each column, with inline bump buttons for X (col 0) and Y (col 1)
                // Layout: [strip#] [X-][tx][X+] [Y-][ty][Y+] [tz] [az] [rx] [ry] [px] [d] [+][-]
                float curX = gridLeft;
                for (int c = 0; c < COLUMN_LABELS.length; c++) {
                    double rangeMin = (c == 6 || c == 11) ? 1 : -9999;  // px col (index 6) min = 1
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
                                setLabel(active ? "rgb" : "grb");
                            }
                        };
                        grbBtn.setMomentary(false).setLabel(initialGrb ? "rgb" : "grb");
                        grbBtn.setActive(initialGrb);
                        grbBtn.addToContainer(gridContainer);
                        // Store a wrapper that exposes the value as double for snapshot compatibility
                        row[c] = new UIDoubleBox(0, 0, 0, 0) {
                            @Override public double getValue() { return grbBtn.isActive() ? 1.0 : 0.0; }
                            @Override public UIDoubleBox setValue(double v) { grbBtn.setActive(v > 0.5); grbBtn.setLabel(v > 0.5 ? "rgb" : "grb"); return this; }
                        };
                        curX += boxW + gap;
                    } else if (c == 10) {
                        float boxW = cirBoxW;
                        final boolean initialCir = srcVals[c] > 0.5f;
                        final int capturedCol = c;
                        heronarts.p3lx.ui.component.UIButton cirBtn = new heronarts.p3lx.ui.component.UIButton(curX, curY, boxW, boxH) {
                            @Override
                            protected void onToggle(boolean active) {
                                setLabel(active ? "cir" : "str");
                                handleValueChange(capturedStripIndex, capturedCol, active ? 1.0 : 0.0);
                            }
                        };
                        cirBtn.setMomentary(false).setLabel(initialCir ? "cir" : "str");
                        cirBtn.setActive(initialCir);
                        cirBtn.addToContainer(gridContainer);
                        row[c] = new UIDoubleBox(0, 0, 0, 0) {
                            @Override public double getValue() { return cirBtn.isActive() ? 1.0 : 0.0; }
                            @Override public UIDoubleBox setValue(double v) { cirBtn.setActive(v > 0.5); cirBtn.setLabel(v > 0.5 ? "cir" : "str"); return this; }
                        };
                        curX += boxW + gap;
                    } else {
                        // Normal column — az, px, cv get fixed widths; others use colWFinal
                        float boxW = (c == 3) ? azBoxW : (c == 6) ? pxBoxW : (c == 8) ? cvBoxW : (c == 11) ? diaBoxW : colWFinal;
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
                        if (active) insertStripAfter(capturedGlobalRow, capturedChannel);
                    }
                }.setMomentary(true).setLabel("+").addToContainer(gridContainer);
                final int capturedCount = count;
                new UIButton(curX + plusW + gap, curY, plusW, boxH) {
                    @Override
                    protected void onToggle(boolean active) {
                        if (active) removeStrip(capturedGlobalRow, capturedChannel, capturedCount);
                    }
                }.setMomentary(true).setLabel("-").addToContainer(gridContainer);

                // Illuminate toggle button — radio-style: only one active at a time
                final int capturedGlobalRowForLit = globalRow;
                final int capturedStripInChannel = s;
                heronarts.p3lx.ui.component.UIButton litBtn = new heronarts.p3lx.ui.component.UIButton(curX + btnsW + gap, curY, litBtnW, boxH) {
                    @Override
                    protected void onToggle(boolean active) {
                        if (active) {
                            // Deactivate the previously active button
                            if (activeLitButton != null && activeLitButton != this) {
                                activeLitButton.setActive(false);
                            }
                            activeLitButton = this;
                            // Resolve via the output's authoritative (channel, strip) table so the
                            // illuminated strip matches what is sent on this channel even if the UI
                            // grid counts have been edited since the model/output were built.
                            int modelIdx = com.symmetrylabs.shows.kaleidoscope.KaleidoscopeShow.KaleidoscopePixlite
                                .getModelStripIndex(capturedChannel, capturedStripInChannel);
                            if (modelIdx < 0) {
                                modelIdx = capturedGlobalRowForLit;
                            }
                            illuminateStrip(modelIdx);
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

        System.out.println("UIKaleidoscopeModelingTool: built grid with " + globalRow + " total strips across " + CHANNEL_COUNT + " channels");
    }

    /**
     * Insert copies after globalRow in the given channel, then rebuild. If any strips are
     * currently group-selected (in any channel), every grouped strip is copied in ascending
     * order; otherwise just the clicked strip is copied.
     */
    private void insertStripAfter(int globalRow, int channel) {
        insertRowsAfter(globalRow, channel);
    }

    /** Append strip(s) to the end of the given channel (works for empty channels), then rebuild. */
    private void insertStripAtChannelEnd(int channel) {
        int firstRow = 0;
        for (int c = 0; c < channel; c++) firstRow += stripCounts[c];
        int lastRow = firstRow + stripCounts[channel] - 1;   // -1 for an empty first channel; may be previous channel's last strip
        insertRowsAfter(lastRow, channel);
    }

    /**
     * Insert copies after afterGlobalRow in the given channel, then rebuild. If any strips are
     * currently group-selected (in any channel), every grouped strip is copied in ascending
     * order; otherwise the strip at afterGlobalRow is copied. If there is nothing to copy
     * (no group selection and afterGlobalRow < 0, i.e. the very first strip in the whole
     * grid), one row of defaults is inserted instead.
     */
    private void insertRowsAfter(int afterGlobalRow, int channel) {
        float[][] snapshot = snapshotStripValues();
        List<Integer> sources = new ArrayList<>();
        if (!groupedStrips.isEmpty()) {
            sources.addAll(groupedStrips);
            java.util.Collections.sort(sources);
        } else {
            sources.add(afterGlobalRow);
        }
        List<float[]> rows = new ArrayList<>();
        for (int gi : sources) {
            if (gi >= 0 && gi < snapshot.length) rows.add(snapshot[gi]);
        }
        if (rows.isEmpty()) {
            float[] d = new float[COLUMN_LABELS.length];
            for (int c = 0; c < COLUMN_LABELS.length; c++) d[c] = defaultValue(c, 0);
            rows.add(d);
        }
        stripCounts[channel] += rows.size();
        countBoxes[channel].setValue(String.valueOf(stripCounts[channel]));
        buildStripGrid(snapshot, rows.toArray(new float[0][]), afterGlobalRow, channel);
    }

    /** Remove the strip at globalRow from the given channel, then rebuild. */
    private void removeStrip(int globalRow, int channel, int channelStripCount) {
        if (channelStripCount <= 0) return;
        float[][] snapshot = snapshotStripValues();
        float[][] shrunk = new float[snapshot.length - 1][COLUMN_LABELS.length];
        for (int i = 0, j = 0; i < snapshot.length; i++) {
            if (i != globalRow) shrunk[j++] = snapshot[i];
        }
        stripCounts[channel]--;
        countBoxes[channel].setValue(String.valueOf(stripCounts[channel]));
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
        boolean circle = row[10].getValue() > 0.5;
        float diameter = (float) row[11].getValue();

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
        float radius = diameter / 2f;
        for (int i = 0; i < n; i++) {
            float tParam = (n > 1) ? (float) i / (n - 1) : 0f;
            float bezier = 4f * cv * tParam * (1f - tParam);  // peaks at middle
            t.push();
            if (circle) {
                float angle = (n > 0) ? (float) (2 * Math.PI * i / n) : 0f;
                t.translate(radius * (float) Math.cos(angle), radius * (float) Math.sin(angle), 0);
            } else {
                t.translate(pitch * i, 0, bezier);
            }
            points.get(i).update(t.x(), t.y(), t.z());
            t.pop();
        }
        // Notify UIGLPointCloud to re-upload vertex positions to GPU
        if (liveModel != null) {
            liveModel.bang();
        }
    }

    /**
     * Resolves the COLUMN_LABELS.length value set a strip row will display.
     * Inserted rows take their values from insertRows[insertOffset]; other rows use
     * existingStrips[sourceRow] with format remapping, falling back to defaults.
     * Used both by the row builder and the channel-header pixel pre-pass.
     */
    private float[] resolveRowValues(float[][] insertRows, int insertOffset, float[][] existingStrips, int sourceRow, int globalRow) {
        // Build a remapped source array always of length COLUMN_LABELS.length.
        // Old 6-col files: [tx,ty,tz,az,px,d] → remap to [tx,ty,tz,az,0,0,px,d,0,0]
        // New 10-col files: already correct.
        final float[] srcVals = new float[COLUMN_LABELS.length];
        for (int c = 0; c < COLUMN_LABELS.length; c++) srcVals[c] = defaultValue(c, globalRow);
        if (insertOffset >= 0 && insertRows != null && insertOffset < insertRows.length) {
            float[] src = insertRows[insertOffset];
            for (int c = 0; c < COLUMN_LABELS.length && c < src.length; c++) srcVals[c] = src[c];
        } else if (existingStrips != null && sourceRow >= 0 && sourceRow < existingStrips.length) {
            float[] src = existingStrips[sourceRow];
            if (src.length >= 12) {
                for (int c = 0; c < COLUMN_LABELS.length; c++) srcVals[c] = src[c];
            } else if (src.length >= 10) {
                for (int c = 0; c < 10; c++) srcVals[c] = src[c];
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
        return srcVals;
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
            case 10: return 0f;
            case 11: return DEFAULT_DIAMETER;
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
        file.stripCounts = new int[CHANNEL_COUNT];
        file.universeLabels = new String[CHANNEL_COUNT];
        for (int u = 0; u < CHANNEL_COUNT; u++) {
            file.stripCounts[u] = stripCounts[u];
            file.universeLabels[u] = labelBoxes[u] != null ? labelBoxes[u].getValue() : "";
        }
        file.strips = snapshotStripValues();
        File f = new File(MAPPING_FILE);
        f.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(f)) {
            new Gson().toJson(file, writer);
            System.out.println("UIKaleidoscopeModelingTool: saved " + file.strips.length + " strips to " + MAPPING_FILE);
        } catch (IOException e) {
            System.err.println("UIKaleidoscopeModelingTool: failed to save mapping");
            e.printStackTrace();
        }
    }

    public static MikeyMappingFile loadFileFromDisk() {
        File f = new File(MAPPING_FILE);
        if (!f.exists()) {
            System.out.println("UIKaleidoscopeModelingTool: no mapping file found, using defaults");
            return null;
        }
        try (FileReader reader = new FileReader(f)) {
            MikeyMappingFile file = new Gson().fromJson(reader, MikeyMappingFile.class);
            if (file == null || file.stripCounts == null || file.strips == null) {
                System.out.println("UIKaleidoscopeModelingTool: mapping file empty/invalid, using defaults");
                return null;
            }
            System.out.println("UIKaleidoscopeModelingTool: loaded " + file.strips.length + " strips across " + file.stripCounts.length + " channels");
            return file;
        } catch (Exception e) {
            System.err.println("UIKaleidoscopeModelingTool: failed to load mapping, using defaults");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the per-channel strip counts from disk, or a default array.
     * Called by KaleidoscopeShow at model-build time. Saved arrays shorter than
     * CHANNEL_COUNT are padded with 0 (the missing channels are unused outputs
     * and must contribute no pixels to the flat buffer).
     */
    public static int[] loadStripCountsFromDisk() {
        MikeyMappingFile file = loadFileFromDisk();
        int[] counts = new int[CHANNEL_COUNT];
        if (file != null && file.stripCounts != null && file.stripCounts.length > 0) {
            for (int u = 0; u < CHANNEL_COUNT; u++) {
                counts[u] = (u < file.stripCounts.length) ? Math.max(0, file.stripCounts[u]) : 0;
            }
        } else {
            for (int u = 0; u < CHANNEL_COUNT; u++) {
                counts[u] = DEFAULT_STRIPS_PER_CHANNEL;
            }
        }
        return counts;
    }

    /**
     * Returns the flat strip parameter array from disk, or null.
     * Called by KaleidoscopeShow at model-build time.
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
