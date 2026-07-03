package com.symmetrylabs.shows.mikey;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

/**
 * Shared store for strip group assignments. Each strip can belong to any of
 * NUM_GROUPS groups (membership is a boolean per group per strip).
 * Persisted to JSON so assignments survive restarts/rebuilds.
 *
 * Used by UIStripGroupTool (editing) and GroupStripFilter (rendering).
 */
public class StripGroups {

    public static final int NUM_GROUPS = 8;
    public static final String GROUPS_FILE = "data/mikey-groups.json";

    /** Saved file format. */
    public static class StripGroupsFile {
        // membership[stripIndex][group] = true if that strip is in that group
        public boolean[][] membership;
    }

    // membership[stripIndex][group]
    private static boolean[][] membership = new boolean[0][NUM_GROUPS];

    static {
        loadFromDisk();
    }

    /** Ensure the table can hold at least stripCount rows (grows, never shrinks). */
    public static synchronized void ensureSize(int stripCount) {
        if (membership.length >= stripCount) return;
        boolean[][] grown = new boolean[stripCount][NUM_GROUPS];
        for (int i = 0; i < membership.length; i++) {
            System.arraycopy(membership[i], 0, grown[i], 0, NUM_GROUPS);
        }
        membership = grown;
    }

    public static synchronized boolean isInGroup(int stripIndex, int group) {
        if (stripIndex < 0 || stripIndex >= membership.length) return false;
        if (group < 0 || group >= NUM_GROUPS) return false;
        return membership[stripIndex][group];
    }

    public static synchronized void setInGroup(int stripIndex, int group, boolean in) {
        if (group < 0 || group >= NUM_GROUPS) return;
        ensureSize(stripIndex + 1);
        membership[stripIndex][group] = in;
    }

    public static synchronized int stripCount() {
        return membership.length;
    }

    /** True if the strip belongs to any group at all. */
    public static synchronized boolean isInAnyGroup(int stripIndex) {
        if (stripIndex < 0 || stripIndex >= membership.length) return false;
        for (int g = 0; g < NUM_GROUPS; g++) {
            if (membership[stripIndex][g]) return true;
        }
        return false;
    }

    public static synchronized void saveToDisk() {
        StripGroupsFile file = new StripGroupsFile();
        file.membership = membership;
        File f = new File(GROUPS_FILE);
        if (f.getParentFile() != null) f.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(f)) {
            new Gson().toJson(file, writer);
            System.out.println("StripGroups: saved " + membership.length + " strips to " + GROUPS_FILE);
        } catch (IOException e) {
            System.err.println("StripGroups: failed to save groups");
            e.printStackTrace();
        }
    }

    public static synchronized void loadFromDisk() {
        File f = new File(GROUPS_FILE);
        if (!f.exists()) {
            System.out.println("StripGroups: no groups file found, starting empty");
            return;
        }
        try (FileReader reader = new FileReader(f)) {
            StripGroupsFile file = new Gson().fromJson(reader, StripGroupsFile.class);
            if (file != null && file.membership != null) {
                // Normalize row widths to NUM_GROUPS
                boolean[][] loaded = new boolean[file.membership.length][NUM_GROUPS];
                for (int i = 0; i < file.membership.length; i++) {
                    boolean[] src = file.membership[i];
                    if (src != null) {
                        System.arraycopy(src, 0, loaded[i], 0, Math.min(src.length, NUM_GROUPS));
                    }
                }
                membership = loaded;
                System.out.println("StripGroups: loaded groups for " + membership.length + " strips");
            }
        } catch (Exception e) {
            System.err.println("StripGroups: failed to load groups");
            e.printStackTrace();
        }
    }
}
