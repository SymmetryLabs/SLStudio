package com.symmetrylabs.shows.flowers;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Guesses at an initial configuration for flower records */
public class Panelizer {
    private static class PanelConfig {
        String id;
        int count;

        PanelConfig(String id, int count) {
            this.id = id;
            this.count = count;
        }
    }

    public static void panelize(List<FlowerData> data) {
        List<PanelConfig> configs = new ArrayList<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(FlowersModelLoader.PANEL_FILE.toPath());
        } catch (IOException e) {
            System.err.println(String.format("couldn't read panel file: %s", e.getMessage()));
            return;
        }
        for (String line : lines) {
            String[] bits = line.trim().split(" ");
            if (bits.length != 2) {
                System.err.println(String.format("bad panel format: '%s'", line));
                return;
            }
            try {
                int count = Integer.parseInt(bits[1]);
                configs.add(new PanelConfig(bits[0], count));
            } catch (NumberFormatException e) {
                System.err.println(String.format("bad panel count: '%s'", line));
                return;
            }
        }

        int p = 0;
        int f = 0;
        int hi = 0;
        int h = 1;
        for (FlowerData fd : data) {
            if (p >= configs.size()) {
                break;
            }
            PanelConfig pc = configs.get(p);
            fd.record.panelId = pc.id;
            fd.record.harness = h;
            fd.record.harnessIndex = hi;
            hi++;
            if (hi >= 9 && h < 4) {
                hi = 0;
                h++;
            } else if (hi >= 9) {
                hi = 0;
                h = FlowerRecord.UNKNOWN_HARNESS;
            }
            f++;
            if (f >= pc.count) {
                f = 0;
                hi = 0;
                h = 1;
                p++;
            }
        }
    }

    private Panelizer() {}
}
