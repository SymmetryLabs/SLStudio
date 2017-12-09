package com.symmetrylabs.util;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */

import java.io.PrintStream;

public final class PrintUtils {

    private static final PrintStream defaultOut = System.out;
    private static final PrintStream nullOut = new PrintStream(new NullOutputStream());

    public static void disablePrintln() {
        System.setOut(nullOut);
    }

    public static void enablePrintln() {
        System.setOut(defaultOut);
    }

}
