package com.symmetrylabs.slstudio.util;



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
