package com.symmetrylabs.util;

/**
 * Writes to nowhere
 */
public static class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
    }
}
