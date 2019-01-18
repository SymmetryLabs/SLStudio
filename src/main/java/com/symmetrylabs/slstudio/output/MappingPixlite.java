package com.symmetrylabs.slstudio.output;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutputGroup;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.mappings.*;
import com.symmetrylabs.slstudio.mappings.PixliteMapping.DatalineMapping;
import com.symmetrylabs.slstudio.output.SLBypassOutputGroup;

public class MappingPixlite extends LXOutputGroup {
    public static final int NUM_DATALINES = 16;
    public static final int MAX_NUM_POINTS_PER_DATALINE = 1020;
    public static final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
    public static final int MAX_NUM_UNIVERSES_PER_DATALINE = (int) FastMath.ceil(1.0 * MAX_NUM_POINTS_PER_DATALINE / MAX_NUM_POINTS_PER_UNIVERSE);

    private static final int[][][] mappingBufferIndices = new int[NUM_DATALINES][][];

    static {
        int index = 0;
        for (int datalineIndex = 0; datalineIndex < mappingBufferIndices.length; datalineIndex++) {
            int[][] datalineIndices = new int[MAX_NUM_UNIVERSES_PER_DATALINE][];
            for (int universe = 0; universe < datalineIndices.length; universe++) {
                int[] universeIndices = new int[MAX_NUM_POINTS_PER_UNIVERSE];
                for (int i = 0; i < universeIndices.length; i++) {
                    universeIndices[i] = index++;
                }
                datalineIndices[universe] = universeIndices;
            }
            mappingBufferIndices[datalineIndex] = datalineIndices;
        }
    }

    public final MappingGroup mappingGroup;
    public final PixliteMapping mapping;
    public final String ipAddress;

    private LXDatagramOutput datagramOutput;
    private final LXOutput mappingOutput;
    private final LX lx;

    public static final int MAPPING_COLORS_POINTS_PER_DATALINE = MAX_NUM_UNIVERSES_PER_DATALINE * MAX_NUM_POINTS_PER_UNIVERSE;
    public final int[] mappingColors = new int[NUM_DATALINES * MAPPING_COLORS_POINTS_PER_DATALINE];

    public MappingPixlite(MappingGroup mappingGroup, PixliteMapping mapping, LX lx) {
        super(lx);

        this.lx = lx;
        this.mappingGroup = mappingGroup;
        this.mapping = mapping;
        this.ipAddress = mapping.ipAddress;

        addChild(mappingOutput = new SLBypassOutputGroup(lx, mappingColors));

        LXDatagramOutput mappingDatagramOutput;
        try {
            datagramOutput = new LXDatagramOutput(lx);
            mappingDatagramOutput = new LXDatagramOutput(lx);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        addChild(datagramOutput);
        mappingOutput.addChild(mappingDatagramOutput);

        updateOutputsEnabled();
        SLStudio.applet.mappingModeEnabled.addListener(param -> updateOutputsEnabled());

        for (int datalineIndex = 0; datalineIndex < NUM_DATALINES; datalineIndex++) {
            for (int universe = 0; universe < MAX_NUM_UNIVERSES_PER_DATALINE; universe++) {
                mappingDatagramOutput.addDatagram(new ArtNetDmxDatagram(lx, ipAddress, mappingBufferIndices[datalineIndex][universe],
                    10 * (datalineIndex + 1) + universe - 1));
            }
        }

        int datalineIndex = 0;
        for (DatalineMapping datalineMapping : mapping.getDatalineMappings()) {
            addDatalineMapping(datalineIndex++, datalineMapping);
        }
    }

    private void updateOutputsEnabled() {
        boolean mappingEnabled = SLStudio.applet.mappingModeEnabled.isOn();
        mappingOutput.enabled.setValue(mappingEnabled);
        datagramOutput.enabled.setValue(!mappingEnabled);
    }

    private void addDatalineMapping(int datalineIndex, DatalineMapping datalineMapping) {
        int firstUniverseOnOutput = (datalineIndex + 1) * 10;

        // the points for one pixlite output have to be spread across multiple universes
        int numPoints = Math.min(datalineMapping.numPoints, datalineMapping.points.length);
        int numUniverses = (numPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
        int counter = 0;

        for (int i = 0; i < numUniverses; i++) {
            int universe = firstUniverseOnOutput + i;
            int numIndices = ((i + 1) * MAX_NUM_POINTS_PER_UNIVERSE) > numPoints
                ? (numPoints % MAX_NUM_POINTS_PER_UNIVERSE)
                : MAX_NUM_POINTS_PER_UNIVERSE;
            int[] indices = new int[numIndices];
            for (int i1 = 0; i1 < numIndices; i1++) {
                indices[i1] = datalineMapping.points[counter++].index;
            }
            datagramOutput.addDatagram(new ArtNetDmxDatagram(lx, ipAddress, indices, universe - 1));
        }
    }
}
