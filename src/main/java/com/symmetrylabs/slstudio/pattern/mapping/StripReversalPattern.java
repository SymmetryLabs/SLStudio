package com.symmetrylabs.slstudio.pattern.mapping;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.mappings.*;
import com.symmetrylabs.slstudio.model.CurvedStrip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.model.SunsModel;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.Arrays;
import java.util.Map;

public class StripReversalPattern extends SLPattern {

        private final SunsModel model;
        private final Mappings mappings;
        private final Map<String, int[]> mappingColorsPerPixlite;

        private final BooleanParameter enabled = new BooleanParameter("Enabled");

        private final DiscreteParameter sunId;
        private final DiscreteParameter stripIndex = new DiscreteParameter("Strip", 1);

        private final BooleanParameter reversed = new BooleanParameter("Reversed");

        public StripReversalPattern(LX lx) {
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

        private void clearColorsBuffer() {
                for (int[] mappingColors : mappingColorsPerPixlite.values()) {
                        Arrays.fill(mappingColors, LXColor.BLACK);
                }

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();
        }

        private void resetStripData() {
                Sun sun = model.getSunById(sunId.getOption());
                stripIndex.setRange(sun.getStrips().size());
                resetOutputData();
        }

        private void resetOutputData() {
                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();

                reversed.setValue(stripMapping.reversed);

                clearColorsBuffer();
        }

        private void saveOutputData() {
                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();

                stripMapping.reversed = reversed.isOn();

                resetOutputData();
        }

        @Override
        protected void run(double deltaMs) {
                if (!enabled.isOn()) return;

                Sun sun = model.getSunById(sunId.getOption());
                CurvedStrip strip = (CurvedStrip) sun.getStrips().get(stripIndex.getValuei());
                StripMapping stripMapping = strip.getMappings();

//        int[] mappingColors = mappingColorsPerPixlite[this.pixliteId.getValuei()];
        }
}
