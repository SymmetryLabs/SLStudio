package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

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
        List<String> lines = FileUtils.readShowLines(FlowersModelLoader.PANEL_FILENAME);
        if (lines == null) return;

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
        for (FlowerData fd : data) {
            if (p >= configs.size()) {
                break;
            }
            PanelConfig pc = configs.get(p);
            fd.record.panelId = pc.id;
            f++;
            if (f >= pc.count) {
                f = 0;
                p++;
            }
        }
    }

    public static void harnessize(List<FlowerData> data) {
        List<String> lines = FileUtils.readShowLines(FlowersModelLoader.PIXLITE_FILENAME);
        if (lines == null) return;

        List<PixliteRecord> records = new ArrayList<>();
        HashMap<String, PixliteRecord> panelPixlites = new HashMap<>();
        for (String line : lines) {
            String[] bits = line.trim().split("/");
            if (bits.length != 3) {
                System.err.println(String.format("bad pixlite format: '%s'", line));
                return;
            }
            try {
                int id = Integer.parseInt(bits[0].trim());
                PixliteRecord pr =
                    new PixliteRecord(id, bits[1].trim(), bits[2].trim());
                records.add(pr);
                for (String panel : pr.panels) {
                    panelPixlites.put(panel, pr);
                }
            } catch (NumberFormatException e) {
                System.err.println(String.format("bad pixlite humanID: '%s'", bits[0]));
                return;
            }
        }

        for (FlowerData fd : data) {
            if (!panelPixlites.containsKey(fd.record.panelId)) {
                System.err.println(
                    String.format(
                        "panel %s on flower %s not in pixlite list",
                        fd.record.panelId, fd.record.id));
            } else {
                panelPixlites.get(fd.record.panelId).assignedFlowers.add(fd);
            }
        }

        for (PixliteRecord pr_ : records) {
            final PixliteRecord pr = pr_;
            Collections.sort(pr.assignedFlowers, (fd1, fd2) -> {
                    int pidx1 = pr.panels.indexOf(fd1.record.panelId);
                    int pidx2 = pr.panels.indexOf(fd2.record.panelId);
                    if (pidx1 != pidx2) {
                        return Integer.compare(pidx1, pidx2);
                    }
                    float row1 = fd1.record.x;
                    float row2 = fd2.record.x;
                    float col1 = fd1.record.z;
                    float col2 = fd2.record.z;
                    if (Math.abs(row1 - row2) > 5) {
                        return -Float.compare(row1, row2);
                    }
                    return Float.compare(col1, col2);
                });
            int h = 1;
            int hi = 1;
            for (FlowerData fd : pr.assignedFlowers) {
                while (h <= 4 && hi > pr.harnessCounts[h - 1]) {
                    h++;
                    hi = 1;
                }
                if (h > 4) {
                    System.err.println(
                        String.format("pixlite %d doesn't have enough flowers", pr.id));
                    break;
                }
                fd.record.pixliteId = pr.id;
                fd.record.harness = h;
                fd.record.harnessIndex = hi;
                hi++;
            }
        }
    }

    private Panelizer() {}

    private static class PixliteRecord {
        int id;
        List<String> panels;
        int[] harnessCounts;
        List<FlowerData> assignedFlowers;

        public PixliteRecord(int id, String panelStr, String harnessStr) {
            this.id = id;
            panels = Arrays.asList(panelStr.split(" "));
            harnessCounts = new int[4];
            for (int i = 0; i < 4; i++) {
                harnessCounts[i] = harnessStr.charAt(i) - '0';
            }
            assignedFlowers = new ArrayList<>();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String p : panels) {
                sb.append(p);
                sb.append(" ");
            }
            return String.format(
                "%s / %s/ %d%d%d%d", id, sb.toString(),
                harnessCounts[0], harnessCounts[1],
                harnessCounts[2], harnessCounts[3]);
        }
    }
}
