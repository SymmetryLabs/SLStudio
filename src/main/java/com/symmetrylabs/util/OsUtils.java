package com.symmetrylabs.util;

 // See: https://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
public final class OsUtils {

    private static String osName = null;

    public static String getOsName() {
        if (osName == null) {
            osName = System.getProperty("os.name");
        }
        return osName;
    }

    public static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }

    public static boolean isUnix() {
        return false;
    }

    public static boolean isMacOsX() {
        return getOsName().equals("Mac OS X");
    }

}
