package com.symmetrylabs.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes to nowhere
 */
public class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
    }
}
