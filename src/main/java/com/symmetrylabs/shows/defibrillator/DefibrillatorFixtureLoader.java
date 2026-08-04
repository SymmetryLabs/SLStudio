package com.symmetrylabs.shows.defibrillator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses the Robot Heart / defibrillator_clean .lxf fixture files (a subset of the
 * heronarts LX "Custom JSON Fixture" format: see
 * https://github.com/heronarts/LXStudio/wiki/Custom-JSON-Fixtures) and produces a
 * flat list of {@link StripSpec} describing every pixel strip that is driven over
 * the KiNET protocol (Color Kinetics sPDS-480 controllers).
 *
 * Only the "kinet" output protocol is handled here. Fixtures/components whose
 * output uses "artnet" (e.g. the shady/2d LED screens) are skipped; those are
 * already supported by SLStudio's existing {@code SimplePixlite}/{@code ArtNetDmxDatagram}
 * machinery and don't need a new loader.
 */
public class DefibrillatorFixtureLoader {

    /** One strip (or single point) of pixels destined for a single KiNET port. */
    public static class StripSpec {
        public final String id;
        public final float x, y, z;
        public final float dirX, dirY, dirZ;
        public final int numPoints;
        public final float spacing;
        public final String host;
        public final int kinetPort;
        /** Offset (in pixels) of this strip's data within its KiNET port's data stream. */
        public final int channelOffset;
        /** The "label" field of the owning .lxf fixture file, e.g. "R1", "B28". */
        public final String fixtureLabel;
        /** A human-readable tag from the fixture (e.g. "mouth1left"), or null if none was found. */
        public final String friendlyTag;

        StripSpec(String id, float x, float y, float z, float dirX, float dirY, float dirZ,
                   int numPoints, float spacing, String host, int kinetPort, int channelOffset,
                   String fixtureLabel, String friendlyTag) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dirX = dirX;
            this.dirY = dirY;
            this.dirZ = dirZ;
            this.numPoints = numPoints;
            this.spacing = spacing;
            this.host = host;
            this.kinetPort = kinetPort;
            this.channelOffset = channelOffset;
            this.fixtureLabel = fixtureLabel;
            this.friendlyTag = friendlyTag;
        }
    }

    /** Generic/uninformative tags to skip when hunting for a human-friendly fixture name. */
    private static final Set<String> GENERIC_TAGS = new HashSet<>();
    static {
        for (String t : new String[] {
            "head", "body", "bbody", "bheart", "brandeaux", "screen", "bface",
            "exterior", "interior", "bar", "shady", "angler_heart", "heart"
        }) {
            GENERIC_TAGS.add(t);
        }
    }

    private static boolean looksLikeIp(String s) {
        return s.matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    /** Picks the first tag that isn't generic, purely numeric, an IP address, or the label itself. */
    private static String findFriendlyTag(JsonObject root, String label) {
        if (!root.has("tags") || !root.get("tags").isJsonArray()) {
            return null;
        }
        for (JsonElement te : root.getAsJsonArray("tags")) {
            String tag = te.getAsString();
            if (tag.equalsIgnoreCase(label)) continue;
            if (GENERIC_TAGS.contains(tag.toLowerCase())) continue;
            if (tag.matches("\\d+")) continue;
            if (looksLikeIp(tag)) continue;
            return tag;
        }
        return null;
    }

    /**
     * Loads all KiNET-driven strips referenced (directly or transitively) by the
     * given top-level index fixtures (e.g. "heart", "brandeaux", "shady").
     *
     * @param fixturesRoot the Fixtures/ directory of the defibrillator_clean checkout
     * @param indexFixtures relative fixture paths (no .lxf suffix) to load, e.g. "heart"
     */
    public static List<StripSpec> loadKinetStrips(File fixturesRoot, String... indexFixtures) {
        List<StripSpec> out = new ArrayList<>();
        for (String indexFixture : indexFixtures) {
            loadFixture(fixturesRoot, indexFixture, null, out);
        }
        return out;
    }

    /**
     * Loads a single fixture file (leaf or index) and appends any KiNET strips found
     * to {@code out}. Index fixtures (whose components only reference other fixture
     * files by path) are recursed into automatically.
     *
     * @param inheritedOutput the output block inherited from an enclosing fixture, if any
     */
    private static void loadFixture(File fixturesRoot, String relPath, JsonObject inheritedOutput, List<StripSpec> out) {
        File file = new File(fixturesRoot, relPath + ".lxf");
        if (!file.exists()) {
            System.err.println("DefibrillatorFixtureLoader: missing fixture file " + file);
            return;
        }

        JsonObject root;
        try (FileReader reader = new FileReader(file)) {
            root = new JsonParser().parse(reader).getAsJsonObject();
        } catch (Exception e) {
            System.err.println("DefibrillatorFixtureLoader: failed to parse " + file);
            e.printStackTrace();
            return;
        }

        String label = root.has("label") ? root.get("label").getAsString() : new File(relPath).getName();
        String friendlyTag = findFriendlyTag(root, label);

        JsonObject fixtureOutput = root.has("output") ? root.getAsJsonObject("output") : inheritedOutput;

        if (!root.has("components")) {
            return;
        }

        JsonArray components = root.getAsJsonArray("components");
        int componentIndex = 0;
        for (JsonElement ce : components) {
            JsonObject comp = ce.getAsJsonObject();
            String type = comp.has("type") ? comp.get("type").getAsString() : null;

            boolean isReference = type != null
                && !type.equals("strip")
                && !type.equals("point")
                && !type.equals("points");
            if (isReference) {
                // e.g. { "type": "heart/R1" } -- reference to another fixture file
                loadFixture(fixturesRoot, type, fixtureOutput, out);
                componentIndex++;
                continue;
            }

            int instances = comp.has("instances") ? comp.get("instances").getAsInt() : 1;
            for (int inst = 0; inst < instances; inst++) {
                addComponentStrip(comp, type, label, friendlyTag, componentIndex, inst, instances, fixtureOutput, out);
            }
            componentIndex++;
        }
    }

    private static void addComponentStrip(JsonObject comp, String type, String fixtureLabel, String friendlyTag,
                                           int componentIndex, int instance, int instances,
                                           JsonObject fixtureOutput, List<StripSpec> out) {
        float x = evalNumericField(comp.get("x"), instance);
        float y = evalNumericField(comp.get("y"), instance);
        float z = evalNumericField(comp.get("z"), instance);

        int numPoints;
        float spacing;
        if ("point".equals(type) || "points".equals(type)) {
            numPoints = 1;
            spacing = 0f;
        } else {
            numPoints = comp.has("numPoints") ? (int) evalNumericField(comp.get("numPoints"), instance) : 0;
            spacing = comp.has("spacing") ? evalNumericField(comp.get("spacing"), instance) : 1f;
        }
        if (numPoints <= 0) {
            return;
        }

        float dx = 1, dy = 0, dz = 0;
        if (comp.has("direction")) {
            JsonObject dir = comp.getAsJsonObject("direction");
            dx = dir.has("x") ? evalNumericField(dir.get("x"), instance) : 0;
            dy = dir.has("y") ? evalNumericField(dir.get("y"), instance) : 0;
            dz = dir.has("z") ? evalNumericField(dir.get("z"), instance) : 0;
            if (dx == 0 && dy == 0 && dz == 0) {
                dx = 1;
                dy = 0;
                dz = 0;
            }
        }

        JsonObject outputObj = null;
        if (comp.has("outputs") && comp.getAsJsonArray("outputs").size() > 0) {
            outputObj = comp.getAsJsonArray("outputs").get(0).getAsJsonObject();
        } else if (comp.has("output")) {
            outputObj = comp.getAsJsonObject("output");
        } else {
            outputObj = fixtureOutput;
        }

        if (outputObj == null) {
            return;
        }
        String protocol = outputObj.has("protocol") ? outputObj.get("protocol").getAsString() : "kinet";
        if (!"kinet".equals(protocol)) {
            // artnet/sacn/etc. handled elsewhere by existing SLStudio output classes
            return;
        }

        String host = outputObj.has("host") && !outputObj.get("host").isJsonNull()
            ? outputObj.get("host").getAsString() : null;
        if (host == null) {
            return;
        }
        int kinetPort = outputObj.has("kinetPort") ? parseIntFlexible(outputObj.get("kinetPort")) : 1;
        int channel = outputObj.has("channel") ? parseIntFlexible(outputObj.get("channel")) : 0;
        int start = outputObj.has("start") ? parseIntFlexible(outputObj.get("start")) : 0;

        String id = fixtureLabel + "_" + componentIndex + (instances > 1 ? ("_" + instance) : "");
        out.add(new StripSpec(id, x, y, z, dx, dy, dz, numPoints, spacing, host, kinetPort, channel + start,
            fixtureLabel, friendlyTag));
    }

    private static int parseIntFlexible(JsonElement el) {
        JsonPrimitive p = el.getAsJsonPrimitive();
        if (p.isString()) {
            return Integer.parseInt(p.getAsString().trim());
        }
        return p.getAsInt();
    }

    private static float evalNumericField(JsonElement el, int instance) {
        if (el == null || el.isJsonNull()) {
            return 0f;
        }
        JsonPrimitive p = el.getAsJsonPrimitive();
        if (p.isNumber()) {
            return p.getAsFloat();
        }
        return evalExpr(p.getAsString(), instance);
    }

    // ---- Tiny arithmetic expression evaluator for "$instance"-templated fields ----
    // Grammar: expr := term (('+'|'-') term)* ; term := factor (('*'|'/') factor)* ;
    //          factor := number | '$instance' | '(' expr ')' | ('-') factor

    private static float evalExpr(String s, int instance) {
        return new ExprParser(s.replace("$instance", String.valueOf(instance))).parseExpr();
    }

    private static class ExprParser {
        private final String s;
        private int pos = 0;

        ExprParser(String s) {
            this.s = s;
        }

        float parseExpr() {
            float value = parseTerm();
            while (true) {
                skipSpaces();
                if (peek() == '+') {
                    pos++;
                    value += parseTerm();
                } else if (peek() == '-') {
                    pos++;
                    value -= parseTerm();
                } else {
                    break;
                }
            }
            return value;
        }

        private float parseTerm() {
            float value = parseFactor();
            while (true) {
                skipSpaces();
                if (peek() == '*') {
                    pos++;
                    value *= parseFactor();
                } else if (peek() == '/') {
                    pos++;
                    value /= parseFactor();
                } else {
                    break;
                }
            }
            return value;
        }

        private float parseFactor() {
            skipSpaces();
            if (peek() == '-') {
                pos++;
                return -parseFactor();
            }
            if (peek() == '(') {
                pos++;
                float value = parseExpr();
                skipSpaces();
                if (peek() == ')') {
                    pos++;
                }
                return value;
            }
            int start = pos;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) {
                pos++;
            }
            if (pos == start) {
                throw new NumberFormatException("Cannot parse expression: " + s);
            }
            return Float.parseFloat(s.substring(start, pos));
        }

        private void skipSpaces() {
            while (pos < s.length() && s.charAt(pos) == ' ') {
                pos++;
            }
        }

        private char peek() {
            return pos < s.length() ? s.charAt(pos) : '\0';
        }
    }
}
