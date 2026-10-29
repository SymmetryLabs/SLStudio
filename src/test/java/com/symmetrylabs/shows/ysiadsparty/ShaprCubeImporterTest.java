package com.symmetrylabs.shows.ysiadsparty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.junit.Test;

import com.symmetrylabs.shows.cubes.CubesModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

public class ShaprCubeImporterTest {

    private static final float W = ShaprCubeImporter.CUBE_WIDTH;  // 24

    /** Writes content to a temp file and returns it (deleted on exit). */
    private static File tempFile(String suffix, String content) throws Exception {
        File f = File.createTempFile("shapr-test", suffix);
        f.deleteOnExit();
        try (FileWriter w = new FileWriter(f)) {
            w.write(content);
        }
        return f;
    }

    /** The 8 corners of an axis-aligned cube of edge W with center (cx,cz), bottom y. */
    private static float[][] cubeCorners(float cx, float cz, float y) {
        float h = W / 2f;
        float[][] out = new float[8][3];
        int i = 0;
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sy = 0; sy <= 1; sy++)
                for (int sz = -1; sz <= 1; sz += 2)
                    out[i++] = new float[] { cx + sx * h, y + sy * W, cz + sz * h };
        return out;
    }

    /** Rotate an XZ point by LXMatrix.rotateY(a): x'=cos*x+sin*z, z'=-sin*x+cos*z. */
    private static float[] rotY(float x, float z, float deg) {
        float r = (float) Math.toRadians(deg);
        float c = (float) Math.cos(r), s = (float) Math.sin(r);
        return new float[] { c * x + s * z, -s * x + c * z };
    }

    /** OBJ with two 24-edge cubes in separate o groups; second rotated 30deg about Y. */
    private static String twoCubeObj() {
        StringBuilder sb = new StringBuilder();
        int base = 1;
        // Cube A: axis-aligned, center (0,0), bottom 0
        sb.append("o cubeA\n");
        for (float[] v : cubeCorners(0, 0, 0)) sb.append("v ").append(v[0]).append(' ').append(v[1]).append(' ').append(v[2]).append('\n');
        sb.append("f 1 2 3 4\nf 5 6 7 8\n");
        base += 8;
        // Cube B: rotated 30 deg about Y (rotateY convention), center (100,50), bottom 0
        sb.append("o cubeB\n");
        for (float[] v : cubeCorners(0, 0, 0)) {
            float[] r = rotY(v[0], v[2], 30f);
            sb.append("v ").append(r[0] + 100).append(' ').append(v[1]).append(' ').append(r[1] + 50).append('\n');
        }
        for (int i = 0; i < 2; i++) {
            sb.append("f ").append(base).append(' ').append(base + 1).append(' ').append(base + 2).append(' ').append(base + 3).append('\n');
            base += 4;
        }
        return sb.toString();
    }

    @Test
    public void importTwoObjCubes() throws Exception {
        File f = tempFile(".obj", twoCubeObj());
        List<ShaprCubeImporter.Cube> cubes = ShaprCubeImporter.importCubes(f, 1f, false, false);
        assertEquals(2, cubes.size());

        // Sorted by y, then x, then z: cube A (corner -12,-12) first
        ShaprCubeImporter.Cube a = cubes.get(0);
        assertEquals(-W / 2f, a.x, 0.01);
        assertEquals(0f, a.y, 0.01);
        assertEquals(-W / 2f, a.z, 0.01);
        assertEquals(0f, a.ry, 0.25);

        ShaprCubeImporter.Cube b = cubes.get(1);
        assertEquals(100 - W / 2f, b.x, 0.01);
        assertEquals(0f, b.y, 0.01);
        assertEquals(50 - W / 2f, b.z, 0.01);
        assertEquals(30f, b.ry, 0.15);
    }

    /**
     * Sign verification: the emitted ry, when fed into a real CubesModel.Cube,
     * must reproduce the mesh's rotated XZ footprint (not the mirrored one).
     */
    @Test
    public void emittedRyMatchesRealCubeFootprint() throws Exception {
        File f = tempFile(".obj", twoCubeObj());
        List<ShaprCubeImporter.Cube> cubes = ShaprCubeImporter.importCubes(f, 1f, false, false);
        ShaprCubeImporter.Cube b = cubes.get(1);

        // Mesh footprint (XZ bbox) of the 30-degree source body
        float mnx = Float.MAX_VALUE, mxx = -Float.MAX_VALUE, mnz = Float.MAX_VALUE, mxz = -Float.MAX_VALUE;
        for (float[] v : cubeCorners(0, 0, 0)) {
            float[] r = rotY(v[0], v[2], 30f);
            mnx = Math.min(mnx, r[0] + 100); mxx = Math.max(mxx, r[0] + 100);
            mnz = Math.min(mnz, r[1] + 50);  mxz = Math.max(mxz, r[1] + 50);
        }

        // Footprint of a real cube built with the emitted parameters
        CubesModel.Cube real = new CubesModel.Cube("t", b.x, b.y, b.z, 0, b.ry, 0,
            new LXTransform(), CubesModel.Cube.Type.LARGE);
        float cnx = Float.MAX_VALUE, cxx = -Float.MAX_VALUE, cnz = Float.MAX_VALUE, cxz = -Float.MAX_VALUE;
        for (LXPoint p : real.getPoints()) {
            cnx = Math.min(cnx, p.x); cxx = Math.max(cxx, p.x);
            cnz = Math.min(cnz, p.z); cxz = Math.max(cxz, p.z);
        }
        // Cube edge points sit slightly inside the face bbox; allow ~1.5in tolerance
        assertEquals(mnx, cnx, 1.5f);
        assertEquals(mxx, cxx, 1.5f);
        assertEquals(mnz, cnz, 1.5f);
        assertEquals(mxz, cxz, 1.5f);
    }

    // ── Tiny-piece merging ────────────────────────────────────────────────────

    private static int vBase = 1;

    /** Appends one 2x2x2 cubelet `g` group (8 verts + 2 faces) centered at (cx,cy,cz); s scales inch coords to file units. */
    private static void appendPiece(StringBuilder sb, float s, float cx, float cy, float cz) {
        sb.append("g p").append(vBase).append('\n');
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sy = -1; sy <= 1; sy += 2)
                for (int sz = -1; sz <= 1; sz += 2)
                    sb.append("v ").append((cx + sx) * s).append(' ').append((cy + sy) * s).append(' ').append((cz + sz) * s).append('\n');
        sb.append("f ").append(vBase).append(' ').append(vBase + 1).append(' ').append(vBase + 2).append(' ').append(vBase + 3).append('\n');
        sb.append("f ").append(vBase + 4).append(' ').append(vBase + 5).append(' ').append(vBase + 6).append(' ').append(vBase + 7).append('\n');
        vBase += 8;
    }

    /**
     * A 24-wide cube built from ~68 tiny 2x2x2 pieces scattered along its 12 edges
     * (pieces at grid positions where at least two coords are on the cube surface).
     * Adjacent pieces are <=4 apart so their AABBs chain into one cluster.
     */
    private static void appendPieceCube(StringBuilder sb, float s, float cx, float cz) {
        int[] p = { -11, -8, -5, -2, 1, 4, 7, 10, 11 };
        for (int x : p)
            for (int y : p)
                for (int z : p) {
                    int onEdge = (Math.abs(x) == 11 ? 1 : 0) + (Math.abs(y) == 11 ? 1 : 0) + (Math.abs(z) == 11 ? 1 : 0);
                    if (onEdge >= 2) appendPiece(sb, s, cx + x, y + 12, cz + z);  // y+12: cube bottom sits at y=0
                }
    }

    @Test
    public void mergesTinyPiecesAndSplitsFlushNeighbours() throws Exception {
        vBase = 1;
        StringBuilder sb = new StringBuilder();
        appendPieceCube(sb, 1f, 0, 0);        // cube A -> corner (-12,-12)
        appendPieceCube(sb, 1f, 100, 50);     // cube B -> corner (88,38)
        appendPieceCube(sb, 1f, 200, 0);      // touching pair (one 48-wide cluster)
        appendPieceCube(sb, 1f, 224, 0);      //   -> split into corners (188,-12) and (212,-12)
        File f = tempFile(".obj", sb.toString());

        List<ShaprCubeImporter.Cube> cubes = ShaprCubeImporter.importCubes(f, 1f, false, false);
        assertEquals(4, cubes.size());

        float[][] expected = { {-12, -12}, {88, 38}, {188, -12}, {212, -12} };
        for (float[] xz : expected) {
            boolean found = false;
            for (ShaprCubeImporter.Cube c : cubes) {
                if (Math.abs(c.x - xz[0]) < 0.05 && Math.abs(c.z - xz[1]) < 0.05) { found = true; break; }
            }
            assertTrue("no cube at corner (" + xz[0] + "," + xz[1] + ")", found);
        }
        for (ShaprCubeImporter.Cube c : cubes) {
            assertEquals(0f, c.y, 0.01);
            assertTrue("unexpected ry " + c.ry, c.ry < 0.3f || c.ry > 89.7f);
        }
    }

    // ── Auto unit detection ───────────────────────────────────────────────────

    private static void assertAutoUnits(String expectedUnit, float inchesToFileUnits) throws Exception {
        vBase = 1;
        StringBuilder sb = new StringBuilder();
        appendPieceCube(sb, inchesToFileUnits, 0, 0);      // corner (-12,-12)
        appendPieceCube(sb, inchesToFileUnits, 100, 50);   // corner (88,38)
        File f = tempFile(".obj", sb.toString());
        ShaprCubeImporter.ImportResult r =
            ShaprCubeImporter.importCubesAuto(f, ShaprCubeImporter.AUTO_SCALE, false, false);
        assertEquals(expectedUnit, r.unitName);
        assertEquals(2, r.cubes.size());
        assertEquals(-12, r.cubes.get(0).x, 0.05);
        assertEquals(-12, r.cubes.get(0).z, 0.05);
        assertEquals(88, r.cubes.get(1).x, 0.05);
        assertEquals(38, r.cubes.get(1).z, 0.05);
    }

    @Test
    public void autoDetectsMeters() throws Exception {
        assertAutoUnits("meters", 1f / 39.3701f);
    }

    @Test
    public void autoDetectsMillimeters() throws Exception {
        assertAutoUnits("millimeters", 1f / 0.0393701f);
    }
}
