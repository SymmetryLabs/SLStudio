package com.symmetrylabs.slstudio.pattern.mapping;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.layout.FultonStreetLayout;
import com.symmetrylabs.slstudio.mappings.*;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteDatalineRef;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteMapping;
import com.symmetrylabs.slstudio.mappings.pixlite.PixliteMapping.DatalineMapping;
import com.symmetrylabs.slstudio.model.CurvedStrip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.model.SunsModel;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import com.symmetrylabs.slstudio.output.pixlites.Pixlite;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.Arrays;
import java.util.Map;

public class MappingStripReversalPattern extends SLPattern {

        private final SunsModel model;
        private final Mappings mappings;
        private final Map<String, int[]> mappingColorsPerPixlite;

        private final BooleanParameter enabled = new BooleanParameter("Enabled");

        private final DiscreteParameter sunId;
        private final DiscreteParameter stripIndex = new DiscreteParameter("Strip", 1);

        private final BooleanParameter reversed = new BooleanParameter("Reversed");

        private boolean saveInProgress = false;
        private boolean resettingInProgress = false;
        private boolean needsColorBufferReset = true;

        public MappingStripReversalPattern(LX lx) {
                super(lx);
                model = (SunsModel) lx.model;
                mappings = FultonStreetLayout.mappings;
                mappingColorsPerPixlite = SLStudio.applet.mappingColorsPerPixlite;

                addParameter(enabled);
                enabled.setShouldSerialize(false);
                enabled.addListener(param -> SLStudio.applet.mappingModeEnabled.setValue(enabled.isOn()));
                enabled.addListener(param -> resetStripData());

                String[] sunIds = model.getSuns().stream().map(sun -> sun.id).toArray(String[]::new);
                addParameter(sunId = new DiscreteParameter("Sun", sunIds));

                addParameter(stripIndex);

                addParameter(reversed);
                reversed.setShouldSerialize(false);

                resetStripData();

                sunId.addListener(param -> resetStripData());
                stripIndex.addListener(param -> resetOutputData());

                reversed.addListener(param -> saveOutputData());
        }

        private void resetStripData() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                stripIndex.setRange(sun.getStrips().size());

                resettingInProgress = false;

                resetOutputData();
        }

        private void resetOutputData() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();

                reversed.setValue(stripMapping.reversed);

                resettingInProgress = false;

                needsColorBufferReset = true;
        }

        private void saveOutputData() {
                if (saveInProgress || resettingInProgress) return;
                saveInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();

                stripMapping.reversed = reversed.isOn();

                FultonStreetLayout.saveMappings();

                saveInProgress = false;
        }

        private void clearColorsBuffer() {
                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping currentStripMapping = strip.getMappings();

                PixliteDatalineRef datalineRef = currentStripMapping.getOutputAs(PixliteDatalineRef.class);

                for (String pixliteId : mappings.getOutputIds()) {
                        PixliteMapping pixliteMapping = mappings.getOutputById(pixliteId, PixliteMapping.class);
                        if (pixliteMapping == null) continue;

                        for (int datalineIndex = 0; datalineIndex < pixliteMapping.getDatalineMappings().length; datalineIndex++) {
                                if (datalineRef != null && datalineRef.outputId.equals(pixliteId)) {
                                        drawDataline(pixliteId, datalineIndex, -1, 100);
                                } else {
                                        drawDataline(pixliteId, datalineIndex, -1, 31);
                                }
                        }
                }
        }

        private void drawDataline(String pixliteId, int datalineIndex, int datalineOrderIndex, int brightness) {
                int[] mappingColors = mappingColorsPerPixlite.get(pixliteId);

                PixliteMapping pixliteMapping = mappings.getOutputById(pixliteId, PixliteMapping.class);
                if (pixliteMapping == null) return;
                DatalineMapping datalineMapping = pixliteMapping.getDatalineMappings()[datalineIndex];

                int datalineStart = datalineIndex * Pixlite.MAX_NUM_POINTS_PER_DATALINE;
                int datalineEnd = datalineStart + Pixlite.MAX_NUM_POINTS_PER_DATALINE;

                if (datalineMapping.mappingItems.size() == 0) {
                        Arrays.fill(mappingColors, datalineStart, datalineEnd, LXColor.RED);
                } else {
                        int mappingItemIndex = 0;
                        int runningStripStart = datalineStart;
                        for (MappingItem mappingItem : datalineMapping.mappingItems) {
                                if (!(mappingItem instanceof StripMapping)) continue;
                                StripMapping stripMapping = (StripMapping) mappingItem;

                                if (stripMapping.numPoints >= 2) {
                                        int br = datalineOrderIndex != -1 && datalineOrderIndex == mappingItemIndex ? 255 : brightness;

                                        int green = LXColor.rgb(0, br, 0);
                                        int blue = LXColor.rgb(0, 0, br);

                                        int leftColor = stripMapping.reversed ? green : blue;
                                        int rightColor = stripMapping.reversed ? blue : green;

                                        int midPointIndex = runningStripStart + stripMapping.numPoints / 2;
                                        int stripEnd = runningStripStart + stripMapping.numPoints;

                                        Arrays.fill(mappingColors, runningStripStart, midPointIndex, leftColor);
                                        Arrays.fill(mappingColors, midPointIndex, stripEnd, rightColor);
                                }

                                runningStripStart += stripMapping.numPoints;
                                mappingItemIndex++;
                        }
                }
        }


        @Override
        protected void run(double deltaMs) {
                if (!enabled.isOn()) return;

                if (needsColorBufferReset) {
                        needsColorBufferReset = false;
                        clearColorsBuffer();
                }

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping currentStripMapping = strip.getMappings();

                PixliteDatalineRef datalineRef = currentStripMapping.getOutputAs(PixliteDatalineRef.class);
                if (datalineRef == null) return;

                drawDataline(datalineRef.outputId, datalineRef.datalineIndex, datalineRef.datalineOrderIndex, 100);
        }
}
