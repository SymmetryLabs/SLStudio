package com.symmetrylabs.shows.ysiadsparty;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Imports a Shapr3D export (many single-cube bodies) and turns each cube body
 * into a CubesShow.TowerConfig-compatible placement: emitted position follows
 * CubesModel.Cube semantics where x,z is the CORNER of the unrotated cube
 * (center minus half EDGE_WIDTH), rotation is about the cube's center, and y
 * is the cube bottom.
 *
 * Supported formats by extension:
 *  - .obj  bodies split by o/g groups (connected components if a single group)
 *  - .stl  ASCII: bodies split by solid/endsolid (components if one solid);
 *          binary: connected components over vertices welded at 1e-3
 *
 * Pure geometry code — no UI dependencies.
 */
public class ShaprCubeImporter {

    /** Cube edge width, matching CubesModel.Cube.Type.LARGE.EDGE_WIDTH. */
    public static final float CUBE_WIDTH =
        com.symmetrylabs.shows.cubes.CubesModel.Cube.Type.LARGE.EDGE_WIDTH;

    private static final float WELD_TOLERANCE = 1e-3f;
    private static final float YAW_SEARCH_STEP = 0.5f;
    private static final float YAW_REFINE_STEP = 0.1f;
    private static final float YAW_SNAP_EPS = 0.25f;

    /** Pass as the scale argument to auto-detect the file's units. */
    public static final float AUTO_SCALE = 0f;

    /** Candidate unit factors (file unit -> inches) for auto-detection. */
    private static final float[] UNIT_FACTORS = { 1f, 12f, 39.3701f, 3.93701f, 0.0393701f };
    private static final String[] UNIT_NAMES  = { "inches", "feet", "meters", "centimeters", "millimeters" };

    /** Cubes plus the unit detection result (when scale <= 0 was requested). */
    public static class ImportResult {
        public final List<Cube> cubes;
        /** The scale actually applied (the detected unit factor, or the given scale). */
        public final float scaleUsed;
        /** Detected unit name ("inches"/"feet"/"meters"/"centimeters"/"millimeters"), or null when scale was given. */
        public final String unitName;
        public ImportResult(List<Cube> cubes, float scaleUsed, String unitName) {
            this.cubes = cubes;
            this.scaleUsed = scaleUsed;
            this.unitName = unitName;
        }
    }

    /** One imported cube placement: (x,z) corner, y bottom, ry yaw in degrees. */
    public static class Cube {
        public final float x;
        public final float y;
        public final float z;
        public final float ry;
        public Cube(float x, float y, float z, float ry) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.ry = ry;
        }
    }

    /**
     * Parses the file and returns one Cube per detected cube body.
     * @param scale    unit multiplier applied to model coordinates (e.g. mm->inches = 0.0393701);
     *                 scale <= 0 (AUTO_SCALE) auto-detects the file unit
     * @param zUp      true if the file is Z-up (Shapr3D); applies (x,y,z) -> (x, z, -y)
     * @param recenter true to drop the lowest cube bottom to y=0 and center the
     *                 overall XZ extents about the origin
     */
    public static List<Cube> importCubes(File f, float scale, boolean zUp, boolean recenter) throws IOException {
        return importCubesAuto(f, scale, zUp, recenter).cubes;
    }

    /** Same as {@link #importCubes} but also reports the detected/used unit. */
    public static ImportResult importCubesAuto(File f, float scale, boolean zUp, boolean recenter) throws IOException {
        String name = f.getName().toLowerCase();
        List<List<float[]>> bodies;
        if (name.endsWith(".obj")) {
            bodies = parseObj(f);
        } else if (name.endsWith(".stl")) {
            bodies = parseStl(f);
        } else {
            throw new IOException("Unsupported file type (expected .obj or .stl): " + f.getName());
        }

        int bodyCount = bodies.size();
        int[] clusterCount = new int[1];
        bodies = mergePiecesIntoCubes(bodies, clusterCount);

        // Auto unit detection: median of each merged body's MIN AABB dimension
        // (in file coords — rotation-insensitive for the vertical axis) snapped
        // to the closest known unit factor.
        float scaleUsed = scale;
        String unitName = null;
        if (scale <= 0) {
            List<Float> minDims = new ArrayList<>();
            for (List<float[]> body : bodies) {
                if (body.size() < 8) continue;
                BodyBox bb = new BodyBox();
                for (float[] v : body) bb.add(v);
                minDims.add(Math.min(bb.maxX - bb.minX,
                    Math.min(bb.maxY - bb.minY, bb.maxZ - bb.minZ)));
            }
            if (!minDims.isEmpty()) {
                java.util.Collections.sort(minDims);
                float edgeFile = minDims.get(minDims.size() / 2);
                int best = 0;
                double bestErr = Double.MAX_VALUE;
                for (int i = 0; i < UNIT_FACTORS.length; i++) {
                    double err = Math.abs(Math.log(edgeFile * UNIT_FACTORS[i] / CUBE_WIDTH));
                    if (err < bestErr) { bestErr = err; best = i; }
                }
                scaleUsed = UNIT_FACTORS[best];
                unitName = UNIT_NAMES[best];
                System.out.println("ShaprCubeImporter: detected units " + unitName +
                    " (scale " + scaleUsed + ", cube edge " + (edgeFile * scaleUsed) + " in)");
            } else {
                scaleUsed = 1f;
            }
        }

        List<Cube> cubes = new ArrayList<>();
        float globalMinY = Float.MAX_VALUE;
        double globalMinX = Double.MAX_VALUE, globalMaxX = -Double.MAX_VALUE;
        double globalMinZ = Double.MAX_VALUE, globalMaxZ = -Double.MAX_VALUE;
        List<float[]> allVerts = new ArrayList<>();

        for (List<float[]> body : bodies) {
            if (body.size() < 8) continue;  // a cube body has at least 8 corners
            List<float[]> verts = new ArrayList<>(body.size());
            for (float[] v : body) {
                float tx = v[0] * scaleUsed, ty = v[1] * scaleUsed, tz = v[2] * scaleUsed;
                if (zUp) {
                    // Shapr3D is Z-up, SLStudio is Y-up: (x,y,z) -> (x, z, -y)
                    verts.add(new float[] { tx, tz, -ty });
                } else {
                    verts.add(new float[] { tx, ty, tz });
                }
            }

            // Yaw: rotate vertices by -ry (LXMatrix.rotateY convention:
            // x' = cos*x + sin*z, z' = -sin*x + cos*z) and minimize XZ bbox area.
            float bestRy = searchYaw(verts, 0f, 90f, YAW_SEARCH_STEP);
            bestRy = searchYaw(verts, bestRy - YAW_SEARCH_STEP, bestRy + YAW_SEARCH_STEP, YAW_REFINE_STEP);
            if (bestRy < YAW_SNAP_EPS || bestRy > 90f - YAW_SNAP_EPS) bestRy = 0f;

            // Center from the rotated-frame bbox, rotated back into model space.
            float rad = (float) Math.toRadians(-bestRy);
            float cos = (float) Math.cos(rad), sin = (float) Math.sin(rad);
            float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            for (float[] v : verts) {
                float rx = cos * v[0] + sin * v[2];
                float rz = -sin * v[0] + cos * v[2];
                if (rx < minX) minX = rx;
                if (rx > maxX) maxX = rx;
                if (rz < minZ) minZ = rz;
                if (rz > maxZ) maxZ = rz;
                if (v[1] < minY) minY = v[1];
            }
            float cx = (minX + maxX) / 2f, cz = (minZ + maxZ) / 2f;
            float radBack = (float) Math.toRadians(bestRy);
            float cosB = (float) Math.cos(radBack), sinB = (float) Math.sin(radBack);
            float centerX = cosB * cx + sinB * cz;
            float centerZ = -sinB * cx + cosB * cz;

            cubes.add(new Cube(centerX, minY, centerZ, bestRy));  // center stored; corner applied after recenter
            if (minY < globalMinY) globalMinY = minY;
            allVerts.addAll(verts);
        }

        if (cubes.isEmpty()) return new ImportResult(cubes, scaleUsed, unitName);

        float offY = recenter ? globalMinY : 0f;
        float offX = 0f, offZ = 0f;
        if (recenter) {
            for (float[] v : allVerts) {
                if (v[0] < globalMinX) globalMinX = v[0];
                if (v[0] > globalMaxX) globalMaxX = v[0];
                if (v[2] < globalMinZ) globalMinZ = v[2];
                if (v[2] > globalMaxZ) globalMaxZ = v[2];
            }
            offX = (float) ((globalMinX + globalMaxX) / 2.0);
            offZ = (float) ((globalMinZ + globalMaxZ) / 2.0);
        }

        List<Cube> out = new ArrayList<>();
        for (Cube c : cubes) {
            float centerX = c.x - offX;
            float centerZ = c.z - offZ;
            out.add(new Cube(
                round3(centerX - CUBE_WIDTH / 2f),
                round3(c.y - offY),
                round3(centerZ - CUBE_WIDTH / 2f),
                round3(c.ry)));
        }
        out.sort((a, b) -> {
            if (a.y != b.y) return Float.compare(a.y, b.y);
            if (a.x != b.x) return Float.compare(a.x, b.x);
            return Float.compare(a.z, b.z);
        });
        System.out.println("ShaprCubeImporter: " + bodyCount + " bodies -> " + clusterCount[0] +
            " clusters -> " + out.size() + " cubes");
        return new ImportResult(out, scaleUsed, unitName);
    }

    private static float searchYaw(List<float[]> verts, float fromDeg, float toDeg, float step) {
        float bestRy = 0f;
        double bestArea = Double.MAX_VALUE;
        // Subsample big bodies: bbox area converges well before 5000 samples.
        int stride = Math.max(1, verts.size() / 5000);
        for (float ry = fromDeg; ry < toDeg; ry += step) {
            float rad = (float) Math.toRadians(-ry);
            float cos = (float) Math.cos(rad), sin = (float) Math.sin(rad);
            float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
            for (int vi = 0; vi < verts.size(); vi += stride) {
                float[] v = verts.get(vi);
                float rx = cos * v[0] + sin * v[2];
                float rz = -sin * v[0] + cos * v[2];
                if (rx < minX) minX = rx;
                if (rx > maxX) maxX = rx;
                if (rz < minZ) minZ = rz;
                if (rz > maxZ) maxZ = rz;
            }
            double area = (double) (maxX - minX) * (maxZ - minZ);
            if (area < bestArea - 1e-9) {
                bestArea = area;
                bestRy = ry;
            }
        }
        return bestRy;
    }

    private static float round3(float v) {
        return Math.round(v * 1000f) / 1000f;
    }

    // ── Merge tiny pieces into cube bodies ────────────────────────────────────

    private static class BodyBox {
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        void add(float[] v) {
            if (v[0] < minX) minX = v[0];
            if (v[0] > maxX) maxX = v[0];
            if (v[1] < minY) minY = v[1];
            if (v[1] > maxY) maxY = v[1];
            if (v[2] < minZ) minZ = v[2];
            if (v[2] > maxZ) maxZ = v[2];
        }
        float maxDim() { return Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ)); }
        float volume() { return (maxX - minX) * (maxY - minY) * (maxZ - minZ); }
        float cx() { return (minX + maxX) / 2f; }
        float cy() { return (minY + maxY) / 2f; }
        float cz() { return (minZ + maxZ) / 2f; }
    }

    /**
     * Merges bodies that are tiny sub-pieces of one physical cube into merged
     * bodies: union-find over bodies whose tol-expanded AABBs overlap, then a
     * split step that subdivides clusters which are flush neighbours (e.g. two
     * cubes touching along a face) into unit-sized cells. Already-unit bodies
     * (one cluster per body) pass through unchanged. Operates in FILE
     * coordinates, before the axis swap.
     * @param clusterCountOut if non-null, receives the number of AABB clusters
     *                        found before the neighbour-split step
     */
    private static List<List<float[]>> mergePiecesIntoCubes(List<List<float[]>> bodies, int[] clusterCountOut) {
        int n = bodies.size();
        if (n <= 1) {
            if (clusterCountOut != null) clusterCountOut[0] = n;
            return bodies;
        }

        BodyBox[] boxes = new BodyBox[n];
        float[] maxDims = new float[n];
        float maxMaxDim = 0f;
        for (int i = 0; i < n; i++) {
            BodyBox b = new BodyBox();
            for (float[] v : bodies.get(i)) b.add(v);
            boxes[i] = b;
            maxDims[i] = b.maxDim();
            if (maxDims[i] > maxMaxDim) maxMaxDim = maxDims[i];
        }
        float[] sorted = maxDims.clone();
        java.util.Arrays.sort(sorted);
        float medianMaxDim = sorted[n / 2];
        float tol = 0.5f * medianMaxDim;

        // Union-find over bodies whose tol-expanded AABBs overlap, using a
        // spatial hash grid so it is near-linear in the number of bodies.
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        float cell = Math.max(4f * tol, maxMaxDim + 2f * tol);
        Map<Long, List<Integer>> grid = new HashMap<>();
        for (int i = 0; i < n; i++) {
            BodyBox b = boxes[i];
            int x0 = (int) Math.floor((b.minX - tol) / cell), x1 = (int) Math.floor((b.maxX + tol) / cell);
            int y0 = (int) Math.floor((b.minY - tol) / cell), y1 = (int) Math.floor((b.maxY + tol) / cell);
            int z0 = (int) Math.floor((b.minZ - tol) / cell), z1 = (int) Math.floor((b.maxZ + tol) / cell);
            for (int gx = x0; gx <= x1; gx++)
                for (int gy = y0; gy <= y1; gy++)
                    for (int gz = z0; gz <= z1; gz++) {
                        long key = (gx * 73856093L) ^ (gy * 19349663L) ^ (gz * 83492791L);
                        List<Integer> occupants = grid.computeIfAbsent(key, k -> new ArrayList<>());
                        for (int j : occupants) {
                            if (boxesOverlap(boxes[i], boxes[j], tol)) union(parent, i, j);
                        }
                        occupants.add(i);
                    }
        }

        Map<Integer, List<Integer>> clustersByRoot = new HashMap<>();
        for (int i = 0; i < n; i++) {
            clustersByRoot.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(i);
        }
        List<List<Integer>> clusters = new ArrayList<>(clustersByRoot.values());
        if (clusterCountOut != null) clusterCountOut[0] = clusters.size();

        // Reference unit box: smallest-volume cluster among those with a
        // typical body count (>= half the median cluster size).
        int[] counts = new int[clusters.size()];
        BodyBox[] clusterBoxes = new BodyBox[clusters.size()];
        for (int i = 0; i < clusters.size(); i++) {
            counts[i] = clusters.get(i).size();
            BodyBox cb = new BodyBox();
            for (int j : clusters.get(i)) {
                BodyBox b = boxes[j];
                cb.add(new float[] { b.minX, b.minY, b.minZ });
                cb.add(new float[] { b.maxX, b.maxY, b.maxZ });
            }
            clusterBoxes[i] = cb;
        }
        int[] sortedCounts = counts.clone();
        java.util.Arrays.sort(sortedCounts);
        int medianCount = sortedCounts[clusters.size() / 2];
        BodyBox unit = null;
        for (int i = 0; i < clusters.size(); i++) {
            if (counts[i] >= 0.5 * medianCount
                && (unit == null || clusterBoxes[i].volume() < unit.volume())) {
                unit = clusterBoxes[i];
            }
        }

        List<List<float[]>> merged = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            BodyBox cb = clusterBoxes[i];
            int nx = 1, ny = 1, nz = 1;
            if (unit != null) {
                nx = Math.max(1, Math.round((cb.maxX - cb.minX) / Math.max(1e-6f, unit.maxX - unit.minX)));
                ny = Math.max(1, Math.round((cb.maxY - cb.minY) / Math.max(1e-6f, unit.maxY - unit.minY)));
                nz = Math.max(1, Math.round((cb.maxZ - cb.minZ) / Math.max(1e-6f, unit.maxZ - unit.minZ)));
            }
            if (nx * ny * nz == 1) {
                List<float[]> body = new ArrayList<>();
                for (int j : clusters.get(i)) body.addAll(bodies.get(j));
                merged.add(body);
                continue;
            }
            // Flush neighbours: split this cluster into unit-sized cells by
            // assigning each original body's AABB center to a cell.
            float dx = (cb.maxX - cb.minX) / nx, dy = (cb.maxY - cb.minY) / ny, dz = (cb.maxZ - cb.minZ) / nz;
            Map<Integer, List<float[]>> cells = new HashMap<>();
            for (int j : clusters.get(i)) {
                BodyBox b = boxes[j];
                int ix = clamp((int) ((b.cx() - cb.minX) / dx), nx);
                int iy = clamp((int) ((b.cy() - cb.minY) / dy), ny);
                int iz = clamp((int) ((b.cz() - cb.minZ) / dz), nz);
                cells.computeIfAbsent((ix * ny + iy) * nz + iz, k -> new ArrayList<>())
                    .addAll(bodies.get(j));
            }
            merged.addAll(cells.values());
        }
        return merged;
    }

    private static int clamp(int v, int n) { return Math.min(Math.max(v, 0), n - 1); }

    /** True when the two AABBs, each expanded by tol, overlap on all 3 axes. */
    private static boolean boxesOverlap(BodyBox a, BodyBox b, float tol) {
        return a.minX - b.maxX <= 2f * tol && b.minX - a.maxX <= 2f * tol
            && a.minY - b.maxY <= 2f * tol && b.minY - a.maxY <= 2f * tol
            && a.minZ - b.maxZ <= 2f * tol && b.minZ - a.maxZ <= 2f * tol;
    }

    // ── OBJ ───────────────────────────────────────────────────────────────────

    private static List<List<float[]>> parseObj(File f) throws IOException {
        List<float[]> verts = new ArrayList<>();
        List<List<int[]>> groups = new ArrayList<>();
        List<int[]> current = new ArrayList<>();
        try (java.io.BufferedReader reader = Files.newBufferedReader(f.toPath())) {
            for (String line; (line = reader.readLine()) != null; ) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#")) continue;
            String[] tok = t.split("\\s+");
            if (tok[0].equals("v") && tok.length >= 4) {
                verts.add(new float[] {
                    Float.parseFloat(tok[1]), Float.parseFloat(tok[2]), Float.parseFloat(tok[3])});
            } else if (tok[0].equals("o") || tok[0].equals("g")) {
                if (!current.isEmpty()) {
                    groups.add(current);
                    current = new ArrayList<>();
                }
            } else if (tok[0].equals("f") && tok.length >= 4) {
                int[] face = new int[tok.length - 1];
                for (int i = 1; i < tok.length; i++) {
                    String vi = tok[i].split("/")[0];
                    int idx = Integer.parseInt(vi);
                    face[i - 1] = idx < 0 ? verts.size() + idx : idx - 1;
                }
                current.add(face);
            }
            }
        }
        if (!current.isEmpty()) groups.add(current);

        if (groups.size() <= 1) {
            // Single group (or none): fall back to connected components.
            List<int[]> faces = groups.isEmpty() ? new ArrayList<>() : groups.get(0);
            return components(verts, faces);
        }
        List<List<float[]>> bodies = new ArrayList<>();
        for (List<int[]> faces : groups) {
            TreeSet<Integer> idxs = new TreeSet<>();
            for (int[] face : faces) for (int i : face) idxs.add(i);
            List<float[]> body = new ArrayList<>();
            for (int i : idxs) if (i >= 0 && i < verts.size()) body.add(verts.get(i));
            if (!body.isEmpty()) bodies.add(body);
        }
        return bodies;
    }

    // ── STL ───────────────────────────────────────────────────────────────────

    private static List<List<float[]>> parseStl(File f) throws IOException {
        byte[] data = Files.readAllBytes(f.toPath());
        if (data.length >= 84) {
            ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            bb.position(80);
            long triCount = bb.getInt() & 0xffffffffL;
            if (84 + 50 * triCount == data.length) {
                // Binary STL: connected components over welded vertices.
                List<float[]> verts = new ArrayList<>();
                Map<String, Integer> welded = new HashMap<>();
                List<int[]> faces = new ArrayList<>();
                for (long t = 0; t < triCount; t++) {
                    int base = 84 + (int) (50 * t);
                    int[] face = new int[3];
                    for (int k = 0; k < 3; k++) {
                        int o = base + 12 + 12 * k;
                        float x = bb.getFloat(o), y = bb.getFloat(o + 4), z = bb.getFloat(o + 8);
                        face[k] = weldIndex(verts, welded, x, y, z);
                    }
                    faces.add(face);
                }
                return components(verts, faces);
            }
        }
        return parseAsciiStl(new String(data, "UTF-8"));
    }

    private static List<List<float[]>> parseAsciiStl(String text) throws IOException {
        if (!text.contains("facet")) {
            throw new IOException("Unrecognized STL file (no facets found)");
        }
        List<List<float[]>> solids = new ArrayList<>();
        List<float[]> cur = null;
        for (String line : text.split("\n")) {
            String t = line.trim();
            if (t.startsWith("solid")) {
                cur = new ArrayList<>();
                solids.add(cur);
            } else if (t.startsWith("endsolid")) {
                cur = null;
            } else if (t.startsWith("vertex") && cur != null) {
                String[] tok = t.split("\\s+");
                if (tok.length >= 4) {
                    cur.add(new float[] {
                        Float.parseFloat(tok[1]), Float.parseFloat(tok[2]), Float.parseFloat(tok[3])});
                }
            }
        }
        if (solids.size() <= 1) {
            // One solid: split into connected components via welded triangle vertices.
            List<float[]> raw = solids.isEmpty() ? new ArrayList<>() : solids.get(0);
            List<float[]> verts = new ArrayList<>();
            Map<String, Integer> welded = new HashMap<>();
            List<int[]> faces = new ArrayList<>();
            for (int i = 0; i + 2 < raw.size(); i += 3) {
                int[] face = new int[3];
                for (int k = 0; k < 3; k++) {
                    float[] v = raw.get(i + k);
                    face[k] = weldIndex(verts, welded, v[0], v[1], v[2]);
                }
                faces.add(face);
            }
            return components(verts, faces);
        }
        return solids;
    }

    private static int weldIndex(List<float[]> verts, Map<String, Integer> welded, float x, float y, float z) {
        String key = Math.round(x / WELD_TOLERANCE) + "," + Math.round(y / WELD_TOLERANCE) + "," + Math.round(z / WELD_TOLERANCE);
        Integer idx = welded.get(key);
        if (idx != null) return idx;
        verts.add(new float[] { x, y, z });
        welded.put(key, verts.size() - 1);
        return verts.size() - 1;
    }

    /** Splits faces into connected components (union-find over vertex indices). */
    private static List<List<float[]>> components(List<float[]> verts, List<int[]> faces) {
        int[] parent = new int[verts.size()];
        for (int i = 0; i < parent.length; i++) parent[i] = i;
        for (int[] face : faces) {
            for (int i = 1; i < face.length; i++) {
                union(parent, face[0], face[i]);
            }
        }
        Map<Integer, List<float[]>> byRoot = new HashMap<>();
        TreeSet<Integer> used = new TreeSet<>();
        for (int[] face : faces) for (int i : face) used.add(i);
        for (int i : used) {
            byRoot.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(verts.get(i));
        }
        return new ArrayList<>(byRoot.values());
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) { parent[i] = parent[parent[i]]; i = parent[i]; }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        parent[find(parent, a)] = find(parent, b);
    }
}
