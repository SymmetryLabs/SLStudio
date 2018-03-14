package com.symmetrylabs.slstudio.pattern.mapping;

import com.symmetrylabs.slstudio.Installation;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.mappings.Mappings;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteMapping;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteMapping.DatalineMapping;
import com.symmetrylabs.slstudio.output.pixlites.PixliteHardware;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import com.symmetrylabs.slstudio.output.pixlites.Pixlite;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.event.KeyEvent;

import java.util.Arrays;
import java.util.Map;

public class Mapping1PixliteDatalineLengthPattern extends SLPattern {

        private Mappings mappings;
        private Map<String, int[]> mappingColorsPerPixlite;

        private BooleanParameter enabled = new BooleanParameter("Enabled");

        private final DiscreteParameter pixliteId = new DiscreteParameter("Pixlite", 1);
        private final DiscreteParameter datalineIndex = new DiscreteParameter("Dataline", PixliteMapping.NUM_DATALINES);
        private final DiscreteParameter numPoints = new DiscreteParameter("# Points", 0, Pixlite.MAPPING_COLORS_POINTS_PER_DATALINE);

        private boolean saveInProgress = false;
        private boolean resettingInProgress = false;
        private boolean needsColorBufferReset = true;

        public Mapping1PixliteDatalineLengthPattern(LX lx) {
                super(lx);
                if (!Installation.getLayout().isMappable()) return;
                if (!(Installation.getHardware() instanceof PixliteHardware)) return;
                PixliteHardware hardware = (PixliteHardware) Installation.getHardware();

                mappings = Installation.getMappings();
                mappingColorsPerPixlite = hardware.mappingColorsPerPixlite;

                addParameter(enabled);
                enabled.setShouldSerialize(false);
                enabled.addListener(param -> SLStudio.applet.mappingModeEnabled.setValue(enabled.isOn()));
                enabled.addListener(param -> resetNumPoints());

                pixliteId.setOptions(mappings.getOutputIds().toArray(new String[0]));
                addParameter(pixliteId);
                addParameter(datalineIndex);
                addParameter(numPoints);
                numPoints.setShouldSerialize(false);

                pixliteId.addListener(param -> resetNumPoints());
                datalineIndex.addListener(param -> resetNumPoints());
                resetNumPoints();

                numPoints.addListener(param -> saveNumPoints());
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                if (mappings == null) return;
                int amt = keyEvent.isShiftDown() ? 10 : 1;
                if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                        numPoints.increment(-amt);
                        consumeKeyEvent();
                } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                        numPoints.increment(amt);
                        consumeKeyEvent();
                }
        }

        private void resetNumPoints() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                PixliteMapping pixliteMapping = mappings.getOutputById(pixliteId.getOption(), PixliteMapping.class);
                DatalineMapping datalineMapping = pixliteMapping.getDatalineMappings()[datalineIndex.getValuei()];
                numPoints.setValue(datalineMapping.numPoints);

                resettingInProgress = false;

                needsColorBufferReset = true;
        }

        private void saveNumPoints() {
                if (saveInProgress || resettingInProgress) return;
                saveInProgress = true;

                PixliteMapping pixliteMapping = mappings.getOutputById(pixliteId.getOption(), PixliteMapping.class);
                DatalineMapping datalineMapping = pixliteMapping.getDatalineMappings()[datalineIndex.getValuei()];
                datalineMapping.numPoints = numPoints.getValuei();
                Installation.getHardware().saveMappings();

                saveInProgress = false;
        }

        private void clearColorsBuffer() {
                for (int[] mappingColors : mappingColorsPerPixlite.values()) {
                        Arrays.fill(mappingColors, LXColor.BLACK);
                }
        }

        @Override
        protected void run(double deltaMs) {
                if (mappings == null || !enabled.isOn()) return;

                if (needsColorBufferReset) {
                        needsColorBufferReset = false;
                        clearColorsBuffer();
                }

                int[] mappingColors = mappingColorsPerPixlite.get(this.pixliteId.getOption());

                int currentDatalineIndex = this.datalineIndex.getValuei();
                int datalineStart = currentDatalineIndex * Pixlite.MAPPING_COLORS_POINTS_PER_DATALINE;

                int datalineEnd = datalineStart + numPoints.getValuei();
                int lastPixel = datalineEnd - 1;
                if (datalineStart <= lastPixel - 1) {
                        Arrays.fill(mappingColors, datalineStart, datalineEnd - 1, LXColor.rgb(0, 0, 127));
                }
                mappingColors[lastPixel] = LXColor.RED;
                Arrays.fill(mappingColors, lastPixel + 1,
                        datalineStart + Pixlite.MAPPING_COLORS_POINTS_PER_DATALINE, LXColor.GREEN);
        }
}
