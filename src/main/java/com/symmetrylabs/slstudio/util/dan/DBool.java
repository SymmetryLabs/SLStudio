package com.symmetrylabs.slstudio.util.dan;

public class DBool {
    boolean def, b;
    String tag;
    int row, col;

    void reset() {
        b = def;
    }

    boolean set(int r, int c, boolean val) {
        if (r != row || c != col) return false;
        b = val;
        return true;
    }

    boolean toggle(int r, int c) {
        if (r != row || c != col) return false;
        b = !b;
        return true;
    }

    DBool(String _tag, boolean _def, int _row, int _col) {
        def = _def;
        b = _def;
        tag = _tag;
        row = _row;
        col = _col;
    }
}
