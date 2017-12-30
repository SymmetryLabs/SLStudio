package com.symmetrylabs.slstudio.pattern.mapping;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.mappings.*;
import com.symmetrylabs.slstudio.mappings.PixliteMapping.DatalineMapping;
import com.symmetrylabs.slstudio.model.CurvedStrip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.model.SunsModel;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import com.symmetrylabs.slstudio.pixlites.Pixlite;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.Arrays;
import java.util.Map;

public class MappingStripPixliteAssignmentPattern extends SLPattern {

        private final SunsModel model;
        private final Mappings mappings;
        private final Map<String, int[]> mappingColorsPerPixlite;

        private final BooleanParameter enabled = new BooleanParameter("Enabled");
        private final DiscreteParameter sunId;
        private final DiscreteParameter stripIndex = new DiscreteParameter("Strip", 1);

        private final BooleanParameter outputAssigned = new BooleanParameter("Assigned");

        private final DiscreteParameter pixliteId;
        private final DiscreteParameter datalineIndex = new DiscreteParameter("Dataline", PixliteMapping.NUM_DATALINES);
        private final DiscreteParameter datalineOrderIndex = new DiscreteParameter("Order", 1);

        private boolean saveInProgress = false;
        private boolean resettingInProgress = false;

        public MappingStripPixliteAssignmentPattern(LX lx) {
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

                addParameter(outputAssigned);
                outputAssigned.setShouldSerialize(false);

                addParameter(pixliteId = new DiscreteParameter("Pixlite", mappings.getOutputIds().toArray(new String[0])));
                pixliteId.setShouldSerialize(false);

                addParameter(datalineIndex);
                datalineIndex.setShouldSerialize(false);

                addParameter(datalineOrderIndex);
                datalineOrderIndex.setShouldSerialize(false);

                resetStripData();

                sunId.addListener(param -> resetStripData());
                stripIndex.addListener(param -> resetOutputData());

                outputAssigned.addListener(param -> saveOutputData());

                pixliteId.addListener(param -> saveOutputData());
                datalineIndex.addListener(param -> saveOutputData());
                datalineOrderIndex.addListener(param -> saveOutputData());
        }

        private void clearColorsBuffer() {
                for (int[] mappingColors : mappingColorsPerPixlite.values()) {
                        Arrays.fill(mappingColors, LXColor.BLACK);
                }
        }

        private void resetStripData() {
                if (saveInProgress || resettingInProgress) return;

                Sun sun = model.getSunById(sunId.getOption());
                stripIndex.setRange(sun.getStrips().size());
                resetOutputData();
        }

        private void resetOutputData() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();

                PixliteDatalineRef pixliteDataline = stripMapping.getOutputAs(PixliteDatalineRef.class);
                outputAssigned.setValue(pixliteDataline != null);

                if (pixliteDataline != null) {
                        pixliteId.setValue(Arrays.asList(pixliteId.getOptions()).indexOf(pixliteDataline.outputId));
                        datalineIndex.setValue(pixliteDataline.datalineIndex);

                        PixliteMapping pixliteMapping = mappings.getOutputById(pixliteDataline.outputId, PixliteMapping.class);
                        DatalineMapping datalineMapping = pixliteMapping.getDatalineMappings()[pixliteDataline.datalineIndex];

                        datalineOrderIndex.setRange(datalineMapping.mappingItems.size());
                        datalineOrderIndex.setValue(pixliteDataline.datalineOrderIndex);
                }

                resettingInProgress = false;

                clearColorsBuffer();
        }

        private void saveOutputData() {
                if (saveInProgress || resettingInProgress) return;
                saveInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();

                if ((stripMapping.getOutput() != null) != outputAssigned.isOn()) {
                        if (outputAssigned.isOn()) {
                                stripMapping.getOrCreateOutputAs(PixliteDatalineRef.class);
                        } else {
                                stripMapping.clearOutput();
                        }
                }
                PixliteDatalineRef pixliteDataline = stripMapping.getOutputAs(PixliteDatalineRef.class);
                if (pixliteDataline != null) {
                        pixliteDataline.outputId = pixliteId.getOption();
                        pixliteDataline.datalineIndex = datalineIndex.getValuei();

                        PixliteMapping pixliteMapping = mappings.getOutputById(pixliteDataline.outputId, PixliteMapping.class);
                        DatalineMapping datalineMapping = pixliteMapping.getDatalineMappings()[pixliteDataline.datalineIndex];

                        stripMapping.assignOutputObj(datalineMapping);

                        if (pixliteDataline.datalineOrderIndex != datalineOrderIndex.getValuei()) {
                                datalineMapping.moveMappingItemToIndex(stripMapping, datalineOrderIndex.getValuei());
                        }
                }

                FultonStreetLayout.saveMappings();

                saveInProgress = false;

                resetOutputData();
        }

        @Override
        protected void run(double deltaMs) {
                if (!enabled.isOn()) return;

                int[] mappingColors = mappingColorsPerPixlite.get(this.pixliteId.getOption());
                Arrays.fill(mappingColors, LXColor.gray(10));

                int currentDatalineIndex = datalineIndex.getValuei();
                int datalineStart = currentDatalineIndex * Pixlite.MAPPING_COLORS_POINTS_PER_DATALINE;
                int datalineEnd = datalineStart + Pixlite.MAPPING_COLORS_POINTS_PER_DATALINE;
                Arrays.fill(mappingColors, datalineStart, datalineEnd, LXColor.gray(40));

                PixliteMapping pixliteMapping = mappings.getOutputById(pixliteId.getOption(), PixliteMapping.class);
                DatalineMapping datalineMapping = pixliteMapping.getDatalineMappings()[datalineIndex.getValuei()];

                int i = 0;
                int mappingColorIndex = datalineStart;
                for (MappingItem item : datalineMapping.mappingItems) {
                        if (i == datalineOrderIndex.getValuei()) {
                                Arrays.fill(mappingColors, mappingColorIndex, mappingColorIndex + item.points.length, LXColor.GREEN);
                                break;
                        }
                        mappingColorIndex += item.points.length;
                        i++;
                }
        }
}
