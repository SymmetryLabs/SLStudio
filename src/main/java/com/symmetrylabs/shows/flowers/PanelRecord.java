package com.symmetrylabs.shows.flowers;

public class PanelRecord implements Comparable<PanelRecord> {
    public final String id;

    public PanelRecord(String id) {
        this.id = id;
    }

    public int row() {
        return id.charAt(0) - 'A';
    }

    public int col() {
        return Integer.parseInt(id.substring(1)) - 1;
    }

    @Override
    public int compareTo(PanelRecord other) {
        int ccompare = Integer.compare(col(), other.col());
        if (ccompare != 0) {
            return ccompare;
        }
        return Integer.compare(row(), other.row());
    }
}
