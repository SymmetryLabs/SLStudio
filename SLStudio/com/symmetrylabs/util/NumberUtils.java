package com.symmetrylabs.util;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public static final class NumberUtils {

    public static String normalizeHex(String hex) {
        int value = Integer.parseInt(hex, 16);
        return Integer.toString(value, 16);
    }

    public static String normalizeHex(byte hex) {
        return Integer.toString(hex & 0xFF, 16);
    }

    public static String normalizeHexUpper(String hex) {
        int value = Integer.parseInt(hex, 16);
        return String.format("%02X", value);
    }

    public static String normalizeHexUpper(byte hex) {
        return String.format("%02X", hex & 0xFF);
    }

    public static byte hexStringToByte(String hex) {
        return (byte) Integer.parseInt(hex, 16);
    }

    public static int byteToInt(byte b) {
        return (b + 256) % 256;
    }

}
