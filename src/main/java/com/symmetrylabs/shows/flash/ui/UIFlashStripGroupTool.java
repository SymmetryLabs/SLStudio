package com.symmetrylabs.shows.flash.ui;

import com.symmetrylabs.shows.flash.StripGroups;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;

/**
 * GROUPS tab tool. Mirrors the universe/strip layout of UIFlashModelingTool
 * but each strip row has 8 toggle boxes that assign the strip to groups 1-8.
 * Group membership is consumed by the GroupStripFilter effect, which blacks
 * out any strip not in the selected group(s).
 *
 * A Save button persists assignments to data/mikey-groups.json so they
 * survive restarts and rebuilds.
 */
public class UIFlashStripGroupTool extends UI2dContainer {

    private static final int NUM_GROUPS = StripGroups.NUM_GROUPS;

    public UIFlashStripGroupTool(UI ui, float x, float y, float w) {
        super(x, y, w, 100);  // height set after building rows
        setPadding(5);
        setLayout(UI2dContainer.Layout.NONE);

        final float gap      = 2f;
        final float boxH     = 16f;
        final float rowH     = boxH + 2f;
        final float labelColW = 14f;
        final float uHeaderH  = 14f;
        final float colHdrH   = 11f;
        final float groupBoxW = 24f;

        // ── Title ──
        new UILabel(0, 0, w - 10, 16)
            .setLabel("STRIP GROUPS (8 groups)")
            .setFont(ui.theme.getLabelFont())
            .setFontColor(ui.theme.getControlTextColor())
            .addToContainer(this);

        // ── Save button ──
        UIButton saveBtn = new UIButton(0, 20, 100, 18) {
            @Override
            protected void onToggle(boolean active) {
                if (active) StripGroups.saveToDisk();
            }
        };
        saveBtn.setMomentary(true).setLabel("Save Groups");
        saveBtn.addToContainer(this);

        // ── Strip rows grouped by universe (same counts as modeling tool) ──
        int[] stripCounts = UIFlashModelingTool.loadStripCountsFromDisk();
        int totalStrips = 0;
        for (int c : stripCounts) totalStrips += c;
        StripGroups.ensureSize(totalStrips);

        // Load universe labels (if any) from the mapping file for header display
        UIFlashModelingTool.MikeyMappingFile mapping = UIFlashModelingTool.loadFileFromDisk();
        String[] universeLabels = (mapping != null) ? mapping.universeLabels : null;

        float curY = 46f;
        int globalRow = 0;

        for (int u = 0; u < stripCounts.length; u++) {
            int count = stripCounts[u];

            // Universe header
            String uLabel = "U" + (u + 1) + " (" + count + " strip" + (count == 1 ? "" : "s") + ")";
            if (universeLabels != null && u < universeLabels.length
                    && universeLabels[u] != null && !universeLabels[u].isEmpty()) {
                uLabel += " — " + universeLabels[u];
            }
            new UILabel(0, curY, w - 10, uHeaderH - 2)
                .setLabel(uLabel)
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .setFontColor(0xFFAAAAAA)
                .addToContainer(this);
            curY += uHeaderH;

            // Column headers: g1..g8
            float hdrX = labelColW + gap;
            for (int g = 0; g < NUM_GROUPS; g++) {
                new UILabel(hdrX, curY, groupBoxW, colHdrH)
                    .setLabel("g" + (g + 1))
                    .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                    .setFontColor(0xFF666666)
                    .addToContainer(this);
                hdrX += groupBoxW + gap;
            }
            curY += colHdrH;

            // One row per strip: [strip #] [g1] [g2] ... [g8]
            for (int s = 0; s < count; s++) {
                final int stripIndex = globalRow;

                new UILabel(0, curY + 2, labelColW, boxH - 2)
                    .setLabel(String.valueOf(s + 1))
                    .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
                    .addToContainer(this);

                float curX = labelColW + gap;
                for (int g = 0; g < NUM_GROUPS; g++) {
                    final int group = g;
                    UIButton box = new UIButton(curX, curY, groupBoxW, boxH) {
                        @Override
                        protected void onToggle(boolean active) {
                            StripGroups.setInGroup(stripIndex, group, active);
                        }
                    };
                    box.setMomentary(false).setLabel(String.valueOf(g + 1));
                    box.setActive(StripGroups.isInGroup(stripIndex, g));
                    box.addToContainer(this);
                    curX += groupBoxW + gap;
                }

                curY += rowH;
                globalRow++;
            }
        }

        setSize(w, curY + 20);
    }
}
