package com.symmetrylabs.slstudio.pattern.mapping;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.mappings.*;
import com.symmetrylabs.slstudio.mappings.PixliteMapping.DatalineMapping;
import com.symmetrylabs.slstudio.model.CurvedStrip;
import com.symmetrylabs.slstudio.model.Slice;
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

public class StripLengthPattern extends SLPattern {

        private final SunsModel model;
        private final Mappings mappings;
        private final Map<String, int[]> mappingColorsPerPixlite;

        private final BooleanParameter enabled = new BooleanParameter("Enabled");
        private final DiscreteParameter sunId;
        private final DiscreteParameter sliceId;
        private final DiscreteParameter stripIndex = new DiscreteParameter("Strip", 1);

        private final DiscreteParameter stripLength = new DiscreteParameter("Length", 1);

        private boolean saveInProgress = false;
        private boolean resettingInProgress = false;

        public StripLengthPattern(LX lx) {
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

                addParameter(sliceId = new DiscreteParameter("Slice", 1));

                addParameter(stripIndex);

                addParameter(stripLength);
                stripLength.setShouldSerialize(false);

                resetSliceData();

                sunId.addListener(param -> resetSliceData());
                sliceId.addListener(param -> resetStripData());
                stripIndex.addListener(param -> resetOutputData());

                stripLength.addListener(param -> saveOutputData());
        }

        private void clearColorsBuffer() {
                for (int[] mappingColors : mappingColorsPerPixlite.values()) {
                        Arrays.fill(mappingColors, LXColor.BLACK);
                }
        }

        private void resetSliceData() {
                if (saveInProgress || resettingInProgress) return;

                Sun sun = model.getSunById(sunId.getOption());

                String[] sliceIds = sun.getSlices().stream().map(slice -> slice.id).toArray(String[]::new);
                sliceId.setOptions(sliceIds);

                resetStripData();
        }

        private void resetStripData() {
                if (saveInProgress || resettingInProgress) return;

                Sun sun = model.getSunById(sunId.getOption());
                Slice slice = sun.getSliceById(sliceId.getOption());
                stripIndex.setRange(slice.getStrips().size());

                resetOutputData();
        }

        private void resetOutputData() {
                if (saveInProgress || resettingInProgress) return;
                resettingInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                Slice slice = sun.getSliceById(sliceId.getOption());
                CurvedStrip strip = slice.getStrips().get(stripIndex.getValuei());
                StripMapping currentStripMapping = strip.getMappings();

                PixliteDatalineRef datalineRef = currentStripMapping.getOutputAs(PixliteDatalineRef.class);
                DatalineMapping datalineMapping = currentStripMapping.getOutputObjAs(DatalineMapping.class);

                if (datalineRef != null && datalineMapping != null) {
                        checkDatalineConsistency();

                        int numPixelsBefore = 0;
                        for (int i = 0; i <= datalineRef.datalineOrderIndex - 1; i++) {
                                if (!(datalineMapping.mappingItems.get(i) instanceof StripMapping)) continue;
                                StripMapping stripMapping = (StripMapping) datalineMapping.mappingItems.get(i);
                                numPixelsBefore += stripMapping.numPoints;
                        }
                        int nextDatalineIndex = datalineRef.datalineOrderIndex + 1;
                        while (nextDatalineIndex < datalineMapping.mappingItems.size()) {
                                MappingItem nextItem = datalineMapping.mappingItems.get(nextDatalineIndex);
                                if (nextItem instanceof StripMapping) {
                                        StripMapping nextStripMapping = (StripMapping) nextItem;
                                        stripLength.setRange(currentStripMapping.numPoints + nextStripMapping.numPoints + 1);
                                        stripLength.setValue(currentStripMapping.numPoints);
                                        break;
                                }
                                nextDatalineIndex++;
                        }
                        if (nextDatalineIndex >= datalineMapping.mappingItems.size()) {
                                int numPixelsLeft = datalineMapping.numPoints - numPixelsBefore;
                                stripLength.setRange(numPixelsLeft, numPixelsLeft + 1);
                                stripLength.setValue(numPixelsLeft);
                        }
                } else {
                        stripLength.setRange(Pixlite.MAX_NUM_POINTS_PER_DATALINE + 1);
                        stripLength.setValue(currentStripMapping.numPoints);
                }

                resettingInProgress = false;

                clearColorsBuffer();
        }

        private void checkDatalineConsistency() {
                Sun sun = model.getSunById(sunId.getOption());
                Slice slice = sun.getSliceById(sliceId.getOption());
                CurvedStrip strip = slice.getStrips().get(stripIndex.getValuei());
                StripMapping currntStripMapping = strip.getMappings();

                DatalineMapping datalineMapping = currntStripMapping.getOutputObjAs(DatalineMapping.class);

                int runningNumPixels = 0;
                for (MappingItem item : datalineMapping.mappingItems) {
                        if (!(item instanceof StripMapping)) continue;
                        StripMapping stripMapping = (StripMapping) item;
                        if (runningNumPixels + stripMapping.numPoints > datalineMapping.numPoints) {
                                stripMapping.numPoints = datalineMapping.numPoints - runningNumPixels;
                                FultonStreetLayout.saveMappings();
                        }
                        runningNumPixels += stripMapping.numPoints;
                }

                int lastIndex = datalineMapping.mappingItems.size() - 1;
                while (lastIndex > 0) {
                        MappingItem lastItem = datalineMapping.mappingItems.get(lastIndex);
                        if (lastItem instanceof StripMapping) {
                                StripMapping lastStripMapping = (StripMapping) lastItem;

                                int numPixelsBefore = 0;
                                for (int i = 0; i <= lastIndex - 1; i++) {
                                        if (!(datalineMapping.mappingItems.get(i) instanceof StripMapping)) continue;
                                        StripMapping stripMapping = (StripMapping) datalineMapping.mappingItems.get(i);
                                        numPixelsBefore += stripMapping.numPoints;
                                }

                                if (numPixelsBefore + lastStripMapping.numPoints < datalineMapping.numPoints) {
                                        lastStripMapping.numPoints = datalineMapping.numPoints - numPixelsBefore;
                                }

                                break;
                        }
                        lastIndex--;
                }
        }

        private void saveOutputData() {
                if (saveInProgress || resettingInProgress) return;
                saveInProgress = true;

                Sun sun = model.getSunById(sunId.getOption());
                Slice slice = sun.getSliceById(sliceId.getOption());
                CurvedStrip strip = slice.getStrips().get(stripIndex.getValuei());
                StripMapping currentStripMapping = strip.getMappings();

                PixliteDatalineRef datalineRef = currentStripMapping.getOutputAs(PixliteDatalineRef.class);
                DatalineMapping datalineMapping = currentStripMapping.getOutputObjAs(DatalineMapping.class);
                if (datalineRef != null) {
                        int pointLengthDiff = stripLength.getValuei() - currentStripMapping.numPoints;
                        if (pointLengthDiff != 0) {
                                int nextDatalineIndex = datalineRef.datalineOrderIndex + 1;
                                while (nextDatalineIndex < datalineMapping.mappingItems.size()) {
                                        MappingItem nextMappingItem = datalineMapping.mappingItems.get(nextDatalineIndex);
                                        if (nextMappingItem instanceof StripMapping) {
                                                StripMapping nextStripMapping = (StripMapping) nextMappingItem;
                                                currentStripMapping.numPoints += pointLengthDiff;
                                                nextStripMapping.numPoints -= pointLengthDiff;
                                                break;
                                        }
                                        nextDatalineIndex++;
                                }
                                if (nextDatalineIndex >= datalineMapping.mappingItems.size()) {
                                        throw new RuntimeException();
                                }

                        }
                }

                saveInProgress = false;

                resetOutputData();
        }

        @Override
        protected void run(double deltaMs) {
                if (!enabled.isOn()) return;

                Sun sun = model.getSunById(sunId.getOption());
                Slice slice = sun.getSliceById(sliceId.getOption());
                CurvedStrip strip = slice.getStrips().get(stripIndex.getValuei());
                StripMapping currentStripMapping = strip.getMappings();

                PixliteDatalineRef datalineRef = currentStripMapping.getOutputAs(PixliteDatalineRef.class);
                if (datalineRef == null) return;

                int[] mappingColors = mappingColorsPerPixlite.get(datalineRef.outputId);
                Arrays.fill(mappingColors, LXColor.gray(0.1));

                int datalineIndex = datalineRef.datalineIndex;

                int datalineStart = datalineIndex * Pixlite.MAPPING_COLORS_POINTS_PER_DATALINE;
                int datalineEnd = datalineStart + Pixlite.MAPPING_COLORS_POINTS_PER_DATALINE;
                Arrays.fill(mappingColors, datalineStart, datalineEnd, LXColor.gray(0.2));

                PixliteMapping pixliteMapping = mappings.getOutputById(datalineRef.outputId, PixliteMapping.class);
                DatalineMapping datalineMapping = pixliteMapping.getDatalineMappings()[datalineIndex];

                int numPixelsBefore = 0;
                for (int i = 0; i <= datalineRef.datalineOrderIndex - 1; i++) {
                        if (!(datalineMapping.mappingItems.get(i) instanceof StripMapping)) continue;
                        StripMapping stripMapping = (StripMapping) datalineMapping.mappingItems.get(i);
                        numPixelsBefore += stripMapping.numPoints;
                }

                if (stripLength.getValuei() > 0) {
                        int stripStart = datalineStart + numPixelsBefore;
                        int stripEnd = stripStart + stripLength.getValuei();
                        Arrays.fill(mappingColors, stripStart, stripEnd, LXColor.rgb(0, 0, 127));

                        if (datalineRef.datalineOrderIndex + 1 < datalineMapping.mappingItems.size()) {
                                int lastIndex = stripEnd - 1;
                                mappingColors[lastIndex] = LXColor.WHITE;
                                mappingColors[lastIndex + 1] = LXColor.GREEN;
                        }
                }
        }
}
