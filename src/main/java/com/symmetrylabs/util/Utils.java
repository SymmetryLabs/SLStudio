package com.symmetrylabs.util;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.data.Table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;


public final class Utils {

    // Processing functions
    // (duplicated here for easy access)

    static final float EPSILON = PConstants.EPSILON;
    static final float MAX_FLOAT = PConstants.MAX_FLOAT;
    static final float MIN_FLOAT = PConstants.MIN_FLOAT;
    static final int MAX_INT = PConstants.MAX_INT;
    static final int MIN_INT = PConstants.MIN_INT;

    // shapes
    static final int VERTEX = PConstants.VERTEX;
    static final int BEZIER_VERTEX = PConstants.BEZIER_VERTEX;
    static final int QUADRATIC_VERTEX = PConstants.QUADRATIC_VERTEX;
    static final int CURVE_VERTEX = PConstants.CURVE_VERTEX;
    static final int BREAK = PConstants.BREAK;

    // useful goodness
    static final float PI = PConstants.PI;
    static final float HALF_PI = PConstants.HALF_PI;
    static final float THIRD_PI = PConstants.THIRD_PI;
    static final float QUARTER_PI = PConstants.QUARTER_PI;
    static final float TWO_PI = PConstants.TWO_PI;
    static final float TAU = PConstants.TAU;

    static final float DEG_TO_RAD = PConstants.DEG_TO_RAD;
    static final float RAD_TO_DEG = PConstants.RAD_TO_DEG;

    static final String WHITESPACE = PConstants.WHITESPACE;

    // for colors and/or images
    static final int RGB = PConstants.RGB;
    static final int ARGB = PConstants.ARGB;
    static final int HSB = PConstants.HSB;
    static final int ALPHA = PConstants.ALPHA;

    // image file types
    static final int TIFF = PConstants.TIFF;
    static final int TARGA = PConstants.TARGA;
    static final int JPEG = PConstants.JPEG;
    static final int GIF = PConstants.GIF;

    // filter/convert types
    static final int BLUR = PConstants.BLUR;
    static final int GRAY = PConstants.GRAY;
    static final int INVERT = PConstants.INVERT;
    static final int OPAQUE = PConstants.OPAQUE;
    static final int POSTERIZE = PConstants.POSTERIZE;
    static final int THRESHOLD = PConstants.THRESHOLD;
    static final int ERODE = PConstants.ERODE;
    static final int DILATE = PConstants.DILATE;

    // blend mode keyword definitions
    static final int REPLACE = PConstants.REPLACE;
    static final int BLEND = PConstants.BLEND;
    static final int ADD = PConstants.ADD;
    static final int SUBTRACT = PConstants.SUBTRACT;
    static final int LIGHTEST = PConstants.LIGHTEST;
    static final int DARKEST = PConstants.DARKEST;
    static final int DIFFERENCE = PConstants.DIFFERENCE;
    static final int EXCLUSION = PConstants.EXCLUSION;
    static final int MULTIPLY = PConstants.MULTIPLY;
    static final int SCREEN = PConstants.SCREEN;
    static final int OVERLAY = PConstants.OVERLAY;
    static final int HARD_LIGHT = PConstants.HARD_LIGHT;
    static final int SOFT_LIGHT = PConstants.SOFT_LIGHT;
    static final int DODGE = PConstants.DODGE;
    static final int BURN = PConstants.BURN;

    // for messages
    static final int CHATTER = PConstants.CHATTER;
    static final int COMPLAINT = PConstants.COMPLAINT;
    static final int PROBLEM = PConstants.PROBLEM;

    // types of transformation matrices
    static final int PROJECTION = PConstants.PROJECTION;
    static final int MODELVIEW = PConstants.MODELVIEW;

    // types of projection matrices
    static final int CUSTOM = PConstants.CUSTOM;
    static final int ORTHOGRAPHIC = PConstants.ORTHOGRAPHIC;
    static final int PERSPECTIVE = PConstants.PERSPECTIVE;

    // shapes
    static final int GROUP = PConstants.GROUP;

    static final int POINT = PConstants.POINT;
    static final int POINTS = PConstants.POINTS;

    static final int LINE = PConstants.LINE;
    static final int LINES = PConstants.LINES;
    static final int LINE_STRIP = PConstants.LINE_STRIP;
    static final int LINE_LOOP = PConstants.LINE_LOOP;

    static final int TRIANGLE = PConstants.TRIANGLE;
    static final int TRIANGLES = PConstants.TRIANGLES;
    static final int TRIANGLE_STRIP = PConstants.TRIANGLE_STRIP;
    static final int TRIANGLE_FAN = PConstants.TRIANGLE_FAN;

    static final int QUAD = PConstants.QUAD;
    static final int QUADS = PConstants.QUADS;
    static final int QUAD_STRIP = PConstants.QUAD_STRIP;

    static final int POLYGON = PConstants.POLYGON;
    static final int PATH = PConstants.PATH;

    static final int RECT = PConstants.RECT;
    static final int ELLIPSE = PConstants.ELLIPSE;
    static final int ARC = PConstants.ARC;

    static final int SPHERE = PConstants.SPHERE;
    static final int BOX = PConstants.BOX;

    // shape closing modes
    static final int OPEN = PConstants.OPEN;
    static final int CLOSE = PConstants.CLOSE;

    // shape drawing modes
    static final int CORNER = PConstants.CORNER;
    static final int CORNERS = PConstants.CORNERS;
    static final int RADIUS = PConstants.RADIUS;
    static final int CENTER = PConstants.CENTER;
    static final int DIAMETER = PConstants.DIAMETER;

    // arc drawing modes
    static final int CHORD = PConstants.CHORD;
    static final int PIE = PConstants.PIE;

    // vertically alignment modes for text
    static final int BASELINE = PConstants.BASELINE;
    static final int TOP = PConstants.TOP;
    static final int BOTTOM = PConstants.BOTTOM;

    // uv texture orientation modes
    static final int NORMAL = PConstants.NORMAL;
    static final int IMAGE = PConstants.IMAGE;

    // texture wrapping modes
    static final int CLAMP = PConstants.CLAMP;
    static final int REPEAT = PConstants.REPEAT;

    // text placement modes
    static final int MODEL = PConstants.MODEL;
    static final int SHAPE = PConstants.SHAPE;

    // stroke modes
    static final int SQUARE = PConstants.SQUARE;
    static final int ROUND = PConstants.ROUND;
    static final int PROJECT = PConstants.PROJECT;
    static final int MITER = PConstants.MITER;
    static final int BEVEL = PConstants.BEVEL;

    // lighting
    static final int AMBIENT = PConstants.AMBIENT;
    static final int DIRECTIONAL = PConstants.DIRECTIONAL;
    static final int SPOT = PConstants.SPOT;

    // key constants
    static final char BACKSPACE = PConstants.BACKSPACE;
    static final char TAB = PConstants.TAB;
    static final char ENTER = PConstants.ENTER;
    static final char RETURN = PConstants.RETURN;
    static final char ESC = PConstants.ESC;
    static final char DELETE = PConstants.DELETE;
    static final int CODED = PConstants.CODED;

    static final int UP = PConstants.UP;
    static final int DOWN = PConstants.DOWN;
    static final int LEFT = PConstants.LEFT;
    static final int RIGHT = PConstants.RIGHT;

    static final int ALT = PConstants.ALT;
    static final int CONTROL = PConstants.CONTROL;
    static final int SHIFT = PConstants.SHIFT;

    // orientations (only used on Android, ignored on desktop)
    static final int PORTRAIT = PConstants.PORTRAIT;
    static final int LANDSCAPE = PConstants.LANDSCAPE;
    static final int SPAN = PConstants.SPAN;

    // cursor types
    static final int ARROW = PConstants.ARROW;
    static final int CROSS = PConstants.CROSS;
    static final int HAND = PConstants.HAND;
    static final int MOVE = PConstants.MOVE;
    static final int TEXT = PConstants.TEXT;
    static final int WAIT = PConstants.WAIT;

    // hints
    static final int DISABLE_DEPTH_TEST = PConstants.DISABLE_DEPTH_TEST;
    static final int ENABLE_DEPTH_TEST = PConstants.ENABLE_DEPTH_TEST;
    static final int ENABLE_DEPTH_SORT = PConstants.ENABLE_DEPTH_SORT;
    static final int DISABLE_DEPTH_SORT = PConstants.DISABLE_DEPTH_SORT;
    static final int DISABLE_OPENGL_ERRORS = PConstants.DISABLE_OPENGL_ERRORS;
    static final int ENABLE_OPENGL_ERRORS = PConstants.ENABLE_OPENGL_ERRORS;
    static final int DISABLE_DEPTH_MASK = PConstants.DISABLE_DEPTH_MASK;
    static final int ENABLE_DEPTH_MASK = PConstants.ENABLE_DEPTH_MASK;
    static final int DISABLE_OPTIMIZED_STROKE = PConstants.DISABLE_OPTIMIZED_STROKE;
    static final int ENABLE_OPTIMIZED_STROKE = PConstants.ENABLE_OPTIMIZED_STROKE;
    static final int ENABLE_STROKE_PERSPECTIVE = PConstants.ENABLE_STROKE_PERSPECTIVE;
    static final int DISABLE_STROKE_PERSPECTIVE = PConstants.DISABLE_STROKE_PERSPECTIVE;
    static final int DISABLE_TEXTURE_MIPMAPS = PConstants.DISABLE_TEXTURE_MIPMAPS;
    static final int ENABLE_TEXTURE_MIPMAPS = PConstants.ENABLE_TEXTURE_MIPMAPS;
    static final int ENABLE_STROKE_PURE = PConstants.ENABLE_STROKE_PURE;
    static final int DISABLE_STROKE_PURE = PConstants.DISABLE_STROKE_PURE;
    static final int ENABLE_BUFFER_READING = PConstants.ENABLE_BUFFER_READING;
    static final int DISABLE_BUFFER_READING = PConstants.DISABLE_BUFFER_READING;
    static final int DISABLE_KEY_REPEAT = PConstants.DISABLE_KEY_REPEAT;
    static final int ENABLE_KEY_REPEAT = PConstants.ENABLE_KEY_REPEAT;
    static final int DISABLE_ASYNC_SAVEFRAME = PConstants.DISABLE_ASYNC_SAVEFRAME;
    static final int ENABLE_ASYNC_SAVEFRAME = PConstants.ENABLE_ASYNC_SAVEFRAME;
    static final int HINT_COUNT = PConstants.HINT_COUNT;

    //////////////////////////////////////////////////////////////
    // getting the time
    static int second() {
        return PApplet.second();
    }

    static int minute() {
        return PApplet.minute();
    }

    static int hour() {
        return PApplet.hour();
    }

    static int day() {
        return PApplet.day();
    }

    static int month() {
        return PApplet.month();
    }

    static int year() {
        return PApplet.year();
    }

    //////////////////////////////////////////////////////////////
    // printing
    static void print(byte what) {
        PApplet.print(what);
    }

    static void print(boolean what) {
        PApplet.print(what);
    }

    static void print(char what) {
        PApplet.print(what);
    }

    static void print(int what) {
        PApplet.print(what);
    }

    static void print(long what) {
        PApplet.print(what);
    }

    static void print(float what) {
        PApplet.print(what);
    }

    static void print(double what) {
        PApplet.print(what);
    }

    static void print(String what) {
        PApplet.print(what);
    }

    static void print(Object... variables) {
        PApplet.print(variables);
    }

    static void println() {
        PApplet.println();
    }

    static void println(byte what) {
        PApplet.println(what);
    }

    static void println(boolean what) {
        PApplet.println(what);
    }

    static void println(char what) {
        PApplet.println(what);
    }

    static void println(int what) {
        PApplet.println(what);
    }

    static void println(long what) {
        PApplet.println(what);
    }

    static void println(float what) {
        PApplet.println(what);
    }

    static void println(double what) {
        PApplet.println(what);
    }

    static void println(String what) {
        PApplet.println(what);
    }

    static void println(Object... variables) {
        PApplet.println(variables);
    }

    static void println(Object what) {
        PApplet.println(what);
    }

    static void printArray(Object what) {
        PApplet.println(what);
    }

    static void debug(String msg) {
        PApplet.debug(msg);
    }

    //////////////////////////////////////////////////////////////
    // MATH
    static float abs(float n) {
        return PApplet.abs(n);
    }

    static int abs(int n) {
        return PApplet.abs(n);
    }

    static float sq(float n) {
        return PApplet.sq(n);
    }

    static float sqrt(float n) {
        return PApplet.sqrt(n);
    }

    static float log(float n) {
        return PApplet.log(n);
    }

    static float exp(float n) {
        return PApplet.exp(n);
    }

    static float pow(float n, float e) {
        return PApplet.pow(n, e);
    }

    static int max(int a, int b) {
        return PApplet.max(a, b);
    }

    static float max(float a, float b) {
        return PApplet.max(a, b);
    }

    static int max(int a, int b, int c) {
        return PApplet.max(a, b, c);
    }

    static float max(float a, float b, float c) {
        return PApplet.max(a, b, c);
    }

    static int max(int[] list) {
        return PApplet.max(list);
    }

    static float max(float[] list) {
        return PApplet.max(list);
    }

    static int min(int a, int b) {
        return PApplet.min(a, b);
    }

    static float min(float a, float b) {
        return PApplet.min(a, b);
    }

    static int min(int a, int b, int c) {
        return PApplet.min(a, b, c);
    }

    static float min(float a, float b, float c) {
        return PApplet.min(a, b, c);
    }

    static int min(int[] list) {
        return PApplet.min(list);
    }

    static float min(float[] list) {
        return PApplet.min(list);
    }

    static int constrain(int amt, int low, int high) {
        return PApplet.constrain(amt, low, high);
    }

    static float constrain(float amt, float low, float high) {
        return PApplet.constrain(amt, low, high);
    }

    static float sin(float angle) {
        return PApplet.sin(angle);
    }

    static float cos(float angle) {
        return PApplet.cos(angle);
    }

    static float tan(float angle) {
        return PApplet.tan(angle);
    }

    static float asin(float value) {
        return PApplet.asin(value);
    }

    static float acos(float value) {
        return PApplet.acos(value);
    }

    static float atan(float value) {
        return PApplet.atan(value);
    }

    static float atan2(float y, float x) {
        return PApplet.atan2(y, x);
    }

    static float degrees(float radians) {
        return PApplet.degrees(radians);
    }

    static float radians(float degrees) {
        return PApplet.radians(degrees);
    }

    static int ceil(float n) {
        return PApplet.ceil(n);
    }

    static int floor(float n) {
        return PApplet.floor(n);
    }

    static int round(float n) {
        return PApplet.round(n);
    }

    static float mag(float a, float b) {
        return PApplet.mag(a, b);
    }

    static float mag(float a, float b, float c) {
        return PApplet.mag(a, b, c);
    }

    static float dist(float x1, float y1, float x2, float y2) {
        return PApplet.dist(x1, y1, x2, y2);
    }

    static float dist(
        float x1, float y1, float z1,
        float x2, float y2, float z2
    ) {
        return PApplet.dist(x1, y1, z1, x2, y2, z2);
    }

    static float lerp(float start, float stop, float amt) {
        return PApplet.lerp(start, stop, amt);
    }

    static float norm(float value, float start, float stop) {
        return PApplet.norm(value, start, stop);
    }

    public static float map(
        float value,
        float start1, float stop1,
        float start2, float stop2
    ) {
        return PApplet.map(value, start1, stop1, start2, stop2);
    }

    //////////////////////////////////////////////////////////////
    // SORT
    static byte[] sort(byte list[]) {
        return PApplet.sort(list);
    }

    static byte[] sort(byte[] list, int count) {
        return PApplet.sort(list, count);
    }

    static char[] sort(char list[]) {
        return PApplet.sort(list);
    }

    static char[] sort(char[] list, int count) {
        return PApplet.sort(list, count);
    }

    static int[] sort(int list[]) {
        return PApplet.sort(list);
    }

    static int[] sort(int[] list, int count) {
        return PApplet.sort(list, count);
    }

    static float[] sort(float list[]) {
        return PApplet.sort(list);
    }

    static float[] sort(float[] list, int count) {
        return PApplet.sort(list, count);
    }

    static String[] sort(String list[]) {
        return PApplet.sort(list);
    }

    static String[] sort(String[] list, int count) {
        return PApplet.sort(list, count);
    }

    //////////////////////////////////////////////////////////////
    // ARRAY UTILITIES
    static void arrayCopy(
        Object src, int srcPosition,
        Object dst, int dstPosition,
        int length
    ) {
        PApplet.arrayCopy(src, srcPosition, dst, dstPosition, length);
    }

    static void arrayCopy(Object src, Object dst, int length) {
        PApplet.arrayCopy(src, dst, length);
    }

    static void arrayCopy(Object src, Object dst) {
        PApplet.arrayCopy(src, dst);
    }

    static boolean[] expand(boolean list[]) {
        return PApplet.expand(list);
    }

    static boolean[] expand(boolean list[], int newSize) {
        return PApplet.expand(list, newSize);
    }

    static byte[] expand(byte list[]) {
        return PApplet.expand(list);
    }

    static byte[] expand(byte list[], int newSize) {
        return PApplet.expand(list, newSize);
    }

    static char[] expand(char list[]) {
        return PApplet.expand(list);
    }

    static char[] expand(char list[], int newSize) {
        return PApplet.expand(list, newSize);
    }

    static int[] expand(int list[]) {
        return PApplet.expand(list);
    }

    static int[] expand(int list[], int newSize) {
        return PApplet.expand(list, newSize);
    }

    static long[] expand(long list[]) {
        return PApplet.expand(list);
    }

    static long[] expand(long list[], int newSize) {
        return PApplet.expand(list, newSize);
    }

    static float[] expand(float list[]) {
        return PApplet.expand(list);
    }

    static float[] expand(float list[], int newSize) {
        return PApplet.expand(list, newSize);
    }

    static double[] expand(double list[]) {
        return PApplet.expand(list);
    }

    static double[] expand(double list[], int newSize) {
        return PApplet.expand(list, newSize);
    }

    static String[] expand(String list[]) {
        return PApplet.expand(list);
    }

    static String[] expand(String list[], int newSize) {
        return PApplet.expand(list, newSize);
    }

    static Object expand(Object array) {
        return PApplet.expand(array);
    }

    static Object expand(Object list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    static byte[] append(byte array[], byte value) {
        return PApplet.append(array, value);
    }

    static char[] append(char array[], char value) {
        return PApplet.append(array, value);
    }

    static int[] append(int array[], int value) {
        return PApplet.append(array, value);
    }

    static float[] append(float array[], float value) {
        return PApplet.append(array, value);
    }

    static String[] append(String array[], String value) {
        return PApplet.append(array, value);
    }

    static Object append(Object array, Object value) {
        return PApplet.append(array, value);
    }

    static boolean[] shorten(boolean list[]) {
        return PApplet.shorten(list);
    }

    static byte[] shorten(byte list[]) {
        return PApplet.shorten(list);
    }

    static char[] shorten(char list[]) {
        return PApplet.shorten(list);
    }

    static int[] shorten(int list[]) {
        return PApplet.shorten(list);
    }

    static float[] shorten(float list[]) {
        return PApplet.shorten(list);
    }

    static String[] shorten(String list[]) {
        return PApplet.shorten(list);
    }

    static Object shorten(Object list) {
        return PApplet.shorten(list);
    }

    static boolean[] splice(
        boolean list[],
        boolean value, int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static boolean[] splice(
        boolean list[],
        boolean value[], int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static byte[] splice(
        byte list[],
        byte value, int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static byte[] splice(
        byte list[],
        byte value[], int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static char[] splice(
        char list[],
        char value, int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static char[] splice(
        char list[],
        char value[], int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static int[] splice(
        int list[],
        int value, int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static int[] splice(
        int list[],
        int value[], int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static float[] splice(
        float list[],
        float value, int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static float[] splice(
        float list[],
        float value[], int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static String[] splice(
        String list[],
        String value, int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static String[] splice(
        String list[],
        String value[], int index
    ) {
        return PApplet.splice(list, value, index);
    }

    static Object splice(Object list, Object value, int index) {
        return PApplet.splice(list, value, index);
    }

    static boolean[] subset(boolean list[], int start) {
        return PApplet.subset(list, start);
    }

    static boolean[] subset(boolean list[], int start, int count) {
        return PApplet.subset(list, start, count);
    }

    static byte[] subset(byte list[], int start) {
        return PApplet.subset(list, start);
    }

    static byte[] subset(byte list[], int start, int count) {
        return PApplet.subset(list, start, count);
    }

    static char[] subset(char list[], int start) {
        return PApplet.subset(list, start);
    }

    static char[] subset(char list[], int start, int count) {
        return PApplet.subset(list, start, count);
    }

    static int[] subset(int list[], int start) {
        return PApplet.subset(list, start);
    }

    static int[] subset(int list[], int start, int count) {
        return PApplet.subset(list, start, count);
    }

    static float[] subset(float list[], int start) {
        return PApplet.subset(list, start);
    }

    static float[] subset(float list[], int start, int count) {
        return PApplet.subset(list, start, count);
    }

    static String[] subset(String list[], int start) {
        return PApplet.subset(list, start);
    }

    static String[] subset(String list[], int start, int count) {
        return PApplet.subset(list, start, count);
    }

    static Object subset(Object list, int start) {
        return PApplet.subset(list, start);
    }

    static Object subset(Object list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    static boolean[] concat(boolean a[], boolean b[]) {
        return PApplet.concat(a, b);
    }

    static byte[] concat(byte a[], byte b[]) {
        return PApplet.concat(a, b);
    }

    static char[] concat(char a[], char b[]) {
        return PApplet.concat(a, b);
    }

    static int[] concat(int a[], int b[]) {
        return PApplet.concat(a, b);
    }

    static float[] concat(float a[], float b[]) {
        return PApplet.concat(a, b);
    }

    static String[] concat(String a[], String b[]) {
        return PApplet.concat(a, b);
    }

    static Object concat(Object a, Object b) {
        return PApplet.concat(a, b);
    }

    static boolean[] reverse(boolean list[]) {
        return PApplet.reverse(list);
    }

    static byte[] reverse(byte list[]) {
        return PApplet.reverse(list);
    }

    static char[] reverse(char list[]) {
        return PApplet.reverse(list);
    }

    static int[] reverse(int list[]) {
        return PApplet.reverse(list);
    }

    static float[] reverse(float list[]) {
        return PApplet.reverse(list);
    }

    static String[] reverse(String list[]) {
        return PApplet.reverse(list);
    }

    static Object reverse(Object list) {
        return PApplet.reverse(list);
    }

    //////////////////////////////////////////////////////////////
    // STRINGS
    static String trim(String str) {
        return PApplet.trim(str);
    }

    static String[] trim(String[] array) {
        return PApplet.trim(array);
    }

    static String join(String[] list, char separator) {
        return PApplet.join(list, separator);
    }

    static String join(String[] list, String separator) {
        return PApplet.join(list, separator);
    }

    static String[] splitTokens(String value) {
        return PApplet.splitTokens(value);
    }

    static String[] splitTokens(String value, String delim) {
        return PApplet.splitTokens(value, delim);
    }

    static String[] split(String value, char delim) {
        return PApplet.split(value, delim);
    }

    static String[] split(String value, String delim) {
        return PApplet.split(value, delim);
    }

    static String[] match(String str, String regexp) {
        return PApplet.match(str, regexp);
    }

    static String[][] matchAll(String str, String regexp) {
        return PApplet.matchAll(str, regexp);
    }

    //////////////////////////////////////////////////////////////
    // CASTING FUNCTIONS
    static boolean parseBoolean(int what) {
        return PApplet.parseBoolean(what);
    }

    static boolean parseBoolean(String what) {
        return PApplet.parseBoolean(what);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static boolean[] parseBoolean(int what[]) {
        return PApplet.parseBoolean(what);
    }

    static boolean[] parseBoolean(String what[]) {
        return PApplet.parseBoolean(what);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static byte parseByte(boolean what) {
        return PApplet.parseByte(what);
    }

    static byte parseByte(char what) {
        return PApplet.parseByte(what);
    }

    static byte parseByte(int what) {
        return PApplet.parseByte(what);
    }

    static byte parseByte(float what) {
        return PApplet.parseByte(what);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static byte[] parseByte(boolean what[]) {
        return PApplet.parseByte(what);
    }

    static byte[] parseByte(char what[]) {
        return PApplet.parseByte(what);
    }

    static byte[] parseByte(int what[]) {
        return PApplet.parseByte(what);
    }

    static byte[] parseByte(float what[]) {
        return PApplet.parseByte(what);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static char parseChar(byte what) {
        return PApplet.parseChar(what);
    }

    static char parseChar(int what) {
        return PApplet.parseChar(what);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static char[] parseChar(byte what[]) {
        return PApplet.parseChar(what);
    }

    static char[] parseChar(int what[]) {
        return PApplet.parseChar(what);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static int parseInt(boolean what) {
        return PApplet.parseInt(what);
    }

    static int parseInt(byte what) {
        return PApplet.parseInt(what);
    }

    static int parseInt(char what) {
        return PApplet.parseInt(what);
    }

    static int parseInt(float what) {
        return PApplet.parseInt(what);
    }

    static int parseInt(String what) {
        return PApplet.parseInt(what);
    }

    static int parseInt(String what, int otherwise) {
        return PApplet.parseInt(what, otherwise);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static int[] parseInt(boolean what[]) {
        return PApplet.parseInt(what);
    }

    static int[] parseInt(byte what[]) {
        return PApplet.parseInt(what);
    }

    static int[] parseInt(char what[]) {
        return PApplet.parseInt(what);
    }

    static int[] parseInt(float what[]) {
        return PApplet.parseInt(what);
    }

    static int[] parseInt(String what[]) {
        return PApplet.parseInt(what);
    }

    static int[] parseInt(String what[], int missing) {
        return PApplet.parseInt(what, missing);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static float parseFloat(int what) {
        return PApplet.parseFloat(what);
    }

    static float parseFloat(String what) {
        return PApplet.parseFloat(what);
    }

    static float parseFloat(String what, float otherwise) {
        return PApplet.parseFloat(what, otherwise);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static float[] parseFloat(byte what[]) {
        return PApplet.parseFloat(what);
    }

    static float[] parseFloat(int what[]) {
        return PApplet.parseFloat(what);
    }

    static float[] parseFloat(String what[]) {
        return PApplet.parseFloat(what);
    }

    static float[] parseFloat(String what[], float missing) {
        return PApplet.parseFloat(what, missing);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static String str(boolean x) {
        return PApplet.str(x);
    }

    static String str(byte x) {
        return PApplet.str(x);
    }

    static String str(char x) {
        return PApplet.str(x);
    }

    static String str(int x) {
        return PApplet.str(x);
    }

    static String str(float x) {
        return PApplet.str(x);
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
    static String[] str(boolean x[]) {
        return PApplet.str(x);
    }

    static String[] str(byte x[]) {
        return PApplet.str(x);
    }

    static String[] str(char x[]) {
        return PApplet.str(x);
    }

    static String[] str(float x[]) {
        return PApplet.str(x);
    }

    //////////////////////////////////////////////////////////////
    // INT NUMBER FORMATTING
    static String nf(float num) {
        return PApplet.nf(num);
    }

    static String[] nf(float[] num) {
        return PApplet.nf(num);
    }

    static String[] nf(int num[], int digits) {
        return PApplet.nf(num, digits);
    }

    static String nf(int num, int digits) {
        return PApplet.nf(num, digits);
    }

    static String[] nfc(int num[]) {
        return PApplet.nfc(num);
    }

    static String nfc(int num) {
        return PApplet.nfc(num);
    }

    static String nfs(int num, int digits) {
        return PApplet.nfs(num, digits);
    }

    static String[] nfs(int num[], int digits) {
        return PApplet.nfs(num, digits);
    }

    static String nfp(int num, int digits) {
        return PApplet.nfp(num, digits);
    }

    static String[] nfp(int num[], int digits) {
        return PApplet.nfp(num, digits);
    }

    //////////////////////////////////////////////////////////////
    // FLOAT NUMBER FORMATTING
    static String[] nf(float num[], int left, int right) {
        return PApplet.nf(num, left, right);
    }

    static String nf(float num, int left, int right) {
        return PApplet.nf(num, left, right);
    }

    static String[] nfc(float num[], int right) {
        return PApplet.nfc(num, right);
    }

    static String nfc(float num, int right) {
        return PApplet.nfc(num, right);
    }

    static String[] nfs(float num[], int left, int right) {
        return PApplet.nfs(num, left, right);
    }

    static String nfs(float num, int left, int right) {
        return PApplet.nfs(num, left, right);
    }

    static String[] nfp(float num[], int left, int right) {
        return PApplet.nfp(num, left, right);
    }

    static String nfp(float num, int left, int right) {
        return PApplet.nfp(num, left, right);
    }

    //////////////////////////////////////////////////////////////
    // HEX/BINARY CONVERSION
    static String hex(byte value) {
        return PApplet.hex(value);
    }

    static String hex(char value) {
        return PApplet.hex(value);
    }

    static String hex(int value) {
        return PApplet.hex(value);
    }

    static String hex(int value, int digits) {
        return PApplet.hex(value, digits);
    }

    static int unhex(String value) {
        return PApplet.unhex(value);
    }

    static String binary(byte value) {
        return PApplet.binary(value);
    }

    static String binary(char value) {
        return PApplet.binary(value);
    }

    static String binary(int value) {
        return PApplet.binary(value);
    }

    static String binary(int value, int digits) {
        return PApplet.binary(value, digits);
    }

    static int unbinary(String value) {
        return PApplet.unbinary(value);
    }

    //////////////////////////////////////////////////////////////
    // COLOR FUNCTIONS
    static int blendColor(int c1, int c2, int mode) {
        return PApplet.blendColor(c1, c2, mode);
    }

    static int lerpColor(int c1, int c2, float amt, int mode) {
        return PApplet.lerpColor(c1, c2, amt, mode);
    }

    // Processing stuff that needs to be converted to static

    static private final long millisOffset = System.currentTimeMillis();

    private static String sketchPath;

    public static void setSketchPath(String newSketchPath) {
        sketchPath = newSketchPath;
    }

    static public int millis() {
        return (int) (System.currentTimeMillis() - millisOffset);
    }

    public static Table loadTable(String filename, String options) {
        try {
            String optionStr = Table.extensionOptions(true, filename, options);
            String[] optionList = PApplet.trim(PApplet.split(optionStr, ','));

            Table dictionary = null;
            for (String opt : optionList) {
                if (opt.startsWith("dictionary=")) {
                    dictionary = loadTable(opt.substring(opt.indexOf('=') + 1), "tsv");
                    return dictionary.typedParse(createInput(filename), optionStr);
                }
            }
            InputStream input = createInput(filename);
            if (input == null) {
                System.err.println(filename + " does not exist or could not be read");
                return null;
            }
            return new Table(input, optionStr);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] loadStrings(String filename) {
        InputStream is = createInput(filename);
        return PApplet.loadStrings(is);
    }

    public static void saveStrings(String filename, String data[]) {
        PApplet.saveStrings(saveFile(filename), data);
    }

    //////////////////////////////////////////////////////////////
    // FILE INPUT
    public static InputStream createInput(String filename) {
        InputStream input = createInputRaw(filename);
        final String lower = filename.toLowerCase();
        if ((input != null) &&
            (lower.endsWith(".gz") || lower.endsWith(".svgz"))) {
            try {
                return new GZIPInputStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return input;
    }

    public static InputStream createInputRaw(String filename) {
        if (filename == null) return null;

        if (sketchPath == null) {
            System.err.println("The sketch path is not set.");
            throw new RuntimeException("Files must be loaded inside setup() or after it has been called.");
        }

        if (filename.length() == 0) {
            // an error will be called by the parent function
            //System.err.println("The filename passed to openStream() was empty.");
            return null;
        }

        // First check whether this looks like a URL. This will prevent online
        // access logs from being spammed with GET /sketchfolder/http://blahblah
        if (filename.contains(":")) {  // at least smells like URL
            try {
                URL url = new URL(filename);
                URLConnection conn = url.openConnection();
                if (conn instanceof HttpURLConnection) {
                    HttpURLConnection httpConn = (HttpURLConnection) conn;
                    // Will not handle a protocol change (see below)
                    httpConn.setInstanceFollowRedirects(true);
                    int response = httpConn.getResponseCode();
                    // Normally will not follow HTTPS redirects from HTTP due to security concerns
                    // http://stackoverflow.com/questions/1884230/java-doesnt-follow-redirect-in-urlconnection/1884427
                    if (response >= 300 && response < 400) {
                        String newLocation = httpConn.getHeaderField("Location");
                        return createInputRaw(newLocation);
                    }
                    return conn.getInputStream();
                } else if (conn instanceof JarURLConnection) {
                    return url.openStream();
                }
            } catch (MalformedURLException mfue) {
                // not a url, that's fine

            } catch (FileNotFoundException fnfe) {
                // Added in 0119 b/c Java 1.5 throws FNFE when URL not available.
                // http://dev.processing.org/bugs/show_bug.cgi?id=403

            } catch (IOException e) {
                // changed for 0117, shouldn't be throwing exception
                e.printStackTrace();
                //System.err.println("Error downloading from URL " + filename);
                return null;
                //throw new RuntimeException("Error downloading from URL " + filename);
            }
        }

        InputStream stream = null;

        // Moved this earlier than the getResourceAsStream() checks, because
        // calling getResourceAsStream() on a directory lists its contents.
        // http://dev.processing.org/bugs/show_bug.cgi?id=716
        try {
            // First see if it's in a data folder. This may fail by throwing
            // a SecurityException. If so, this whole block will be skipped.
            File file = new File(dataPath(filename));
            if (!file.exists()) {
                // next see if it's just in the sketch folder
                file = sketchFile(filename);
            }

            if (file.isDirectory()) {
                return null;
            }
            if (file.exists()) {
                try {
                    // handle case sensitivity check
                    String filePath = file.getCanonicalPath();
                    String filenameActual = new File(filePath).getName();
                    // make sure there isn't a subfolder prepended to the name
                    String filenameShort = new File(filename).getName();
                    // if the actual filename is the same, but capitalized
                    // differently, warn the user.
                    //if (filenameActual.equalsIgnoreCase(filenameShort) &&
                    //!filenameActual.equals(filenameShort)) {
                    if (!filenameActual.equals(filenameShort)) {
                        throw new RuntimeException("This file is named " +
                            filenameActual + " not " +
                            filename + ". Rename the file " +
                            "or change your code.");
                    }
                } catch (IOException e) {
                }
            }

            // if this file is ok, may as well just load it
            stream = new FileInputStream(file);
            if (stream != null) return stream;

            // have to break these out because a general Exception might
            // catch the RuntimeException being thrown above
        } catch (IOException ioe) {
        } catch (SecurityException se) {
        }

        // Using getClassLoader() prevents java from converting dots
        // to slashes or requiring a slash at the beginning.
        // (a slash as a prefix means that it'll load from the root of
        // the jar, rather than trying to dig into the package location)
        ClassLoader cl = Utils.class.getClassLoader();

        // by default, data files are exported to the root path of the jar.
        // (not the data folder) so check there first.
        stream = cl.getResourceAsStream("data/" + filename);
        if (stream != null) {
            String cn = stream.getClass().getName();
            // this is an irritation of sun's java plug-in, which will return
            // a non-null stream for an object that doesn't exist. like all good
            // things, this is probably introduced in java 1.5. awesome!
            // http://dev.processing.org/bugs/show_bug.cgi?id=359
            if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
                return stream;
            }
        }

        // When used with an online script, also need to check without the
        // data folder, in case it's not in a subfolder called 'data'.
        // http://dev.processing.org/bugs/show_bug.cgi?id=389
        stream = cl.getResourceAsStream(filename);
        if (stream != null) {
            String cn = stream.getClass().getName();
            if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
                return stream;
            }
        }

        try {
            // attempt to load from a local file, used when running as
            // an application, or as a signed applet
            try {  // first try to catch any security exceptions
                try {
                    stream = new FileInputStream(dataPath(filename));
                    if (stream != null) return stream;
                } catch (IOException e2) {
                }

                try {
                    stream = new FileInputStream(sketchPath(filename));
                    if (stream != null) return stream;
                } catch (Exception e) {
                }  // ignored

                try {
                    stream = new FileInputStream(filename);
                    if (stream != null) return stream;
                } catch (IOException e1) {
                }

            } catch (SecurityException se) {
            }  // online, whups

        } catch (Exception e) {
            //die(e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    //////////////////////////////////////////////////////////////

    public static String sketchPath() {
        return sketchPath;
    }

    public static String sketchPath(String where) {
        if (sketchPath() == null) {
            return where;
        }
        // isAbsolute() could throw an access exception, but so will writing
        // to the local disk using the sketch path, so this is safe here.
        // for 0120, added a try/catch anyways.
        try {
            if (new File(where).isAbsolute()) return where;
        } catch (Exception e) {
        }

        return sketchPath() + File.separator + where;
    }

    public static File sketchFile(String where) {
        return new File(sketchPath(where));
    }

    public static String savePath(String where) {
        if (where == null) return null;
        String filename = sketchPath(where);
        PApplet.createPath(filename);
        return filename;
    }

    public static File saveFile(String where) {
        return new File(savePath(where));
    }

    public static String dataPath(String where) {
        return dataFile(where).getAbsolutePath();
    }

    public static File dataFile(String where) {
        // isAbsolute() could throw an access exception, but so will writing
        // to the local disk using the sketch path, so this is safe here.
        File why = new File(where);
        if (why.isAbsolute()) return why;

        URL jarURL = Utils.class.getProtectionDomain().getCodeSource().getLocation();
        // Decode URL
        String jarPath;
        try {
            jarPath = jarURL.toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        if (jarPath.contains("Contents/Java/")) {
            File containingFolder = new File(jarPath).getParentFile();
            File dataFolder = new File(containingFolder, "data");
            return new File(dataFolder, where);
        }
        // Windows, Linux, or when not using a Mac OS X .app file
        File workingDirItem =
            new File(sketchPath + File.separator + "data" + File.separator + where);
//    if (workingDirItem.exists()) {
        return workingDirItem;
//    }
//    // In some cases, the current working directory won't be set properly.
    }

    //////////////////////////////////////////////////////////////
    // SPLINE UTILITY FUNCTIONS (used by both Bezier and Catmull-Rom)
    protected static void splineForward(int segments, PMatrix3D matrix) {
        float f = 1.0f / segments;
        float ff = f * f;
        float fff = ff * f;

        matrix.set(0, 0, 0, 1,
            fff, ff, f, 0,
            6 * fff, 2 * ff, 0, 0,
            6 * fff, 0, 0, 0
        );
    }


    //////////////////////////////////////////////////////////////
    // BEZIER

    protected static boolean bezierInited = false;
    public static int bezierDetail = 20;

    // used by both curve and bezier, so just init here
    protected static PMatrix3D bezierBasisMatrix =
        new PMatrix3D(-1, 3, -3, 1,
            3, -6, 3, 0,
            -3, 3, 0, 0,
            1, 0, 0, 0
        );

    //protected PMatrix3D bezierForwardMatrix;
    protected static PMatrix3D bezierDrawMatrix;

    public static float bezierPoint(float a, float b, float c, float d, float t) {
        float t1 = 1.0f - t;
        return a * t1 * t1 * t1 + 3 * b * t * t1 * t1 + 3 * c * t * t * t1 + d * t * t * t;
    }

    public static float bezierTangent(float a, float b, float c, float d, float t) {
        return (3 * t * t * (-a + 3 * b - 3 * c + d) +
            6 * t * (a - 2 * b + c) +
            3 * (-a + b));
    }

    protected static void bezierInitCheck() {
        if (!bezierInited) {
            bezierInit();
        }
    }

    protected static void bezierInit() {
        // overkill to be broken out, but better parity with the curve stuff below
        bezierDetail(bezierDetail);
        bezierInited = true;
    }

    public static void bezierDetail(int detail) {
        bezierDetail = detail;

        if (bezierDrawMatrix == null) {
            bezierDrawMatrix = new PMatrix3D();
        }

        // setup matrix for forward differencing to speed up drawing
        splineForward(detail, bezierDrawMatrix);

        // multiply the basis and forward diff matrices together
        // saves much time since this needn't be done for each curve
        //mult_spline_matrix(bezierForwardMatrix, bezier_basis, bezierDrawMatrix, 4);
        //bezierDrawMatrix.set(bezierForwardMatrix);
        bezierDrawMatrix.apply(bezierBasisMatrix);
    }

    //////////////////////////////////////////////////////////////
    // CATMULL-ROM CURVE

    protected static boolean curveInited = false;
    public static int curveDetail = 20;
    public static float curveTightness = 0;
    // catmull-rom basis matrix, perhaps with optional s parameter
    protected static PMatrix3D curveBasisMatrix;
    protected static PMatrix3D curveDrawMatrix;

    protected static PMatrix3D bezierBasisInverse;
    protected static PMatrix3D curveToBezierMatrix;

    public static float curvePoint(float a, float b, float c, float d, float t) {
        curveInitCheck();

        float tt = t * t;
        float ttt = t * tt;
        PMatrix3D cb = curveBasisMatrix;

        // not optimized (and probably need not be)
        return (a * (ttt * cb.m00 + tt * cb.m10 + t * cb.m20 + cb.m30) +
            b * (ttt * cb.m01 + tt * cb.m11 + t * cb.m21 + cb.m31) +
            c * (ttt * cb.m02 + tt * cb.m12 + t * cb.m22 + cb.m32) +
            d * (ttt * cb.m03 + tt * cb.m13 + t * cb.m23 + cb.m33));
    }

    public static float curveTangent(float a, float b, float c, float d, float t) {
        curveInitCheck();

        float tt3 = t * t * 3;
        float t2 = t * 2;
        PMatrix3D cb = curveBasisMatrix;

        // not optimized (and probably need not be)
        return (a * (tt3 * cb.m00 + t2 * cb.m10 + cb.m20) +
            b * (tt3 * cb.m01 + t2 * cb.m11 + cb.m21) +
            c * (tt3 * cb.m02 + t2 * cb.m12 + cb.m22) +
            d * (tt3 * cb.m03 + t2 * cb.m13 + cb.m23));
    }

    public static void curveDetail(int detail) {
        curveDetail = detail;
        curveInit();
    }

    public static void curveTightness(float tightness) {
        curveTightness = tightness;
        curveInit();
    }

    protected static void curveInitCheck() {
        if (!curveInited) {
            curveInit();
        }
    }

    protected static void curveInit() {
        // allocate only if/when used to save startup time
        if (curveDrawMatrix == null) {
            curveBasisMatrix = new PMatrix3D();
            curveDrawMatrix = new PMatrix3D();
            curveInited = true;
        }

        float s = curveTightness;
        curveBasisMatrix.set((s - 1) / 2f, (s + 3) / 2f, (-3 - s) / 2f, (1 - s) / 2f,
            (1 - s), (-5 - s) / 2f, (s + 2), (s - 1) / 2f,
            (s - 1) / 2f, 0, (1 - s) / 2f, 0,
            0, 1, 0, 0
        );

        //setup_spline_forward(segments, curveForwardMatrix);
        splineForward(curveDetail, curveDrawMatrix);

        if (bezierBasisInverse == null) {
            bezierBasisInverse = bezierBasisMatrix.get();
            bezierBasisInverse.invert();
            curveToBezierMatrix = new PMatrix3D();
        }

        // TODO only needed for PGraphicsJava2D? if so, move it there
        // actually, it's generally useful for other renderers, so keep it
        // or hide the implementation elsewhere.
        curveToBezierMatrix.set(curveBasisMatrix);
        curveToBezierMatrix.preApply(bezierBasisInverse);

        // multiply the basis and forward diff matrices together
        // saves much time since this needn't be done for each curve
        curveDrawMatrix.apply(curveBasisMatrix);
    }

}
