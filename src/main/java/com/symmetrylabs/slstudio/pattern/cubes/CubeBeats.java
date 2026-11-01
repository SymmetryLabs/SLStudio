package com.symmetrylabs.slstudio.pattern.cubes;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.*;

/**
 * Audio-reactive pattern pack for the cubes model, driven by CubeBeatsAudio
 * (AGC-normalized envelopes, kick/snare/hat onsets, kick-IOI tempo).
 * Modes: TowerVU, Shockwave, EdgeRacers, FaceSpin, Shatter,
 * LavaBreath, LaserPlanes, Heartbeat.
 */
public class CubeBeats extends SLPattern<CubesModel> {

    private static final String[] MODE_NAMES = {
        "TowerVU", "Shockwave", "EdgeRacers", "FaceSpin",
        "Shatter", "LavaBreath", "LaserPlanes", "Heartbeat"
    };
    private static final int NUM_MODES = MODE_NAMES.length;
    private static final int MAX_WAVES = 10;
    private static final int MAX_PULSES = 8;
    private static final int MAX_BUBBLES = 6;
    private static final float[] EL_LIST = {0, 25, -20, 40, -35};

    private final GraphicMeter eq = new GraphicMeter(lx.engine.audio.getInput());
    private final CubeBeatsAudio audio = new CubeBeatsAudio();
    private final Random rng = new Random(9377);
    private final float[] eqBands = new float[CubeBeatsAudio.NUM_BANDS];

    public final DiscreteParameter modeParam =
        new DiscreteParameter("Mode", MODE_NAMES);
    public final CompoundParameter gainParam = new CompoundParameter("Gain", 0.5);
    public final CompoundParameter rangeParam = new CompoundParameter("Range", 0.4);
    public final CompoundParameter attackParam = new CompoundParameter("Attack", 0.2);
    public final CompoundParameter releaseParam = new CompoundParameter("Release", 0.35);
    public final CompoundParameter sensParam = new CompoundParameter("Sens", 0.5);
    public final CompoundParameter smoothParam = new CompoundParameter("Smooth", 0.4);
    public final CompoundParameter speedParam = new CompoundParameter("Speed", 0.4);
    public final CompoundParameter sizeParam = new CompoundParameter("Size", 0.5);
    public final CompoundParameter hueParam = new CompoundParameter("Hue", 0, -180, 180);
    public final CompoundParameter spreadParam = new CompoundParameter("Spread", 40, 0, 360);
    public final CompoundParameter satParam = new CompoundParameter("Sat", 85, 0, 100);
    public final CompoundParameter trailsParam = new CompoundParameter("Trails", 0.25);
    public final CompoundParameter xfadeParam = new CompoundParameter("Xfade", 400, 0, 2000);

    // per-point geometry, indexed by point.index
    private int n;
    private int[] pCube, pTower, pLevel, pFace, pStrip, pStripG, pCubePts, pTowerRank;
    private boolean[] pVert;
    private float[] pLevelN, pStripT, pPathT, pLx, pLy, pLz, pRad, pCubeDist, pStripTau, pCornerU;

    // per-cube / per-tower geometry
    private int numCubes, numTowers, maxLevels;
    private float[] cubeCx, cubeCy, cubeCz, cubeDist, cubeLevelN, cubePhase;
    private int[] cubeTower, cubeLevelIdx, towerMidCube, towerOrderX;
    private int[][] towerCubeOrder;
    private float[] towerXPos, towerZPos;
    private float modelCx, modelCy, modelCz, modelExtent;
    private float avgEdge = 24;

    // global look state
    private double nowMs;
    private float idleMix, hueDrift, liftRemaining, lowIntensityMs, evoClock;
    private float limiterGain = 1;

    // mode state (aged in run() every frame)
    private float[] towerBandPos, towerPeakPos, towerPeakVel;
    private final List<Wave> waves = new ArrayList<Wave>();
    private int lastKickRank = -1;
    private float[] dashT, dashPrev, dashSign, racerOffset;
    private float dirF = 1, dirTarget = 1;
    private float[] theta, thetaV, thetaT, spinDelay, pendingSpin;
    private float[] cubeHitT, cubeHitPeak;
    private int[] cubeHitRole;
    private float[] sparkle;
    private int lastHitCube = -1;
    private float bassSlow, flowT;
    private final List<Bubble> bubbles = new ArrayList<Bubble>();
    private float azA = 0.4f, elA = 0.3f, azB = 1.9f, elB = -0.4f;
    private float azAv, elAv, azBv, elBv, azAT = 0.4f, elAT = 0.3f, azBT = 1.9f, elBT = -0.4f;
    private float sweepS, ang3;
    private final List<Pulse> pulses = new ArrayList<Pulse>();
    private float idleWaveMs, idleEmberMs, idlePulseMs;

    // frame buffers
    private float[] bufH, bufS, bufB, prevH, prevS, prevB, finB;
    private float[] trailH, trailS, trailB;
    private int prevMode = -1;
    private float xfadeMs;
    private int lastRenderedMode = -1;

    private static class Wave {
        float x, y, z, r, speed, str;
        int role;
        boolean thin;
    }
    private static class Bubble {
        float x, y, z, r;
    }
    private static class Pulse {
        double t0;
        float str, dur;
        float[] delay;
    }

    public CubeBeats(LX lx) {
        super(lx);
        addParameter(modeParam);
        addParameter(gainParam);
        addParameter(rangeParam);
        addParameter(attackParam);
        addParameter(releaseParam);
        addParameter(sensParam);
        addParameter(smoothParam);
        addParameter(speedParam);
        addParameter(sizeParam);
        addParameter(hueParam);
        addParameter(spreadParam);
        addParameter(satParam);
        addParameter(trailsParam);
        addParameter(xfadeParam);

        CubeBeatsAudio.configureMeter(eq);
        eq.start();
        addModulator(eq).start();

        precompute();

        modeParam.addListener(parameter -> {
            if (prevMode == -1 && n > 0 && lastRenderedMode >= 0) {
                prevMode = lastRenderedMode;
                xfadeMs = 0;
            }
        });
    }

    private static float hash01(int i) {
        return ((i * 1103515245 + 12345) & 0x7fffffff) / (float) 0x7fffffff;
    }

    private static int pingpong(int i, int m) {
        if (m <= 1) return 0;
        int period = 2 * m - 2;
        int j = ((i % period) + period) % period;
        return j < m ? j : period - j;
    }

    private static float circDist(float a, float b, float mod) {
        float d = (a - b) % mod;
        if (d < 0) d += mod;
        return Math.min(d, mod - d);
    }

    private static float easeOutCubic(float t) {
        float u = 1 - t;
        return 1 - u * u * u;
    }

    private float springW() {
        return 30 + (8 - 30) * smoothParam.getValuef();
    }

    private void precompute() {
        n = model.points.length;
        pCube = new int[n]; pTower = new int[n]; pLevel = new int[n];
        pFace = new int[n]; pStrip = new int[n]; pStripG = new int[n];
        pCubePts = new int[n]; pTowerRank = new int[n];
        pVert = new boolean[n];
        pLevelN = new float[n]; pStripT = new float[n]; pPathT = new float[n];
        pLx = new float[n]; pLy = new float[n]; pLz = new float[n];
        pRad = new float[n]; pCubeDist = new float[n]; pStripTau = new float[n];
        pCornerU = new float[n];
        Arrays.fill(pCube, -1); Arrays.fill(pTower, -1); Arrays.fill(pFace, -1);

        bufH = new float[n]; bufS = new float[n]; bufB = new float[n];
        prevH = new float[n]; prevS = new float[n]; prevB = new float[n];
        finB = new float[n];
        trailH = new float[n]; trailS = new float[n]; trailB = new float[n];
        sparkle = new float[n];

        List<CubesModel.Cube> cubes = model.getCubes();
        List<CubesModel.Tower> towers = model.getTowers();
        numCubes = cubes.size();
        numTowers = towers.size();
        if (n == 0 || numCubes == 0) return;

        cubeCx = new float[numCubes]; cubeCy = new float[numCubes];
        cubeCz = new float[numCubes]; cubeDist = new float[numCubes];
        cubeLevelN = new float[numCubes]; cubePhase = new float[numCubes];
        cubeTower = new int[numCubes]; cubeLevelIdx = new int[numCubes];
        cubeHitT = new float[numCubes]; cubeHitPeak = new float[numCubes];
        cubeHitRole = new int[numCubes];
        Arrays.fill(cubeTower, -1);
        Arrays.fill(cubeHitT, 1e9f);

        modelCx = model.xMin + model.xRange / 2f;
        modelCy = model.yMin + model.yRange / 2f;
        modelCz = model.zMin + model.zRange / 2f;
        modelExtent = 0.5f * (float) Math.sqrt(
            model.xRange * model.xRange + model.yRange * model.yRange + model.zRange * model.zRange);
        if (modelExtent <= 0) modelExtent = 1;

        towerXPos = new float[numTowers]; towerZPos = new float[numTowers];
        towerMidCube = new int[numTowers];
        towerBandPos = new float[numTowers]; Arrays.fill(towerBandPos, 7);
        towerPeakPos = new float[numTowers]; towerPeakVel = new float[numTowers];
        towerCubeOrder = new int[numTowers][];

        dashT = new float[numTowers]; dashPrev = new float[numTowers];
        dashSign = new float[numTowers]; Arrays.fill(dashSign, 1);
        racerOffset = new float[numTowers];

        float edgeSum = 0;
        for (int ci = 0; ci < numCubes; ci++) {
            CubesModel.Cube cube = cubes.get(ci);
            float cx = 0, cy = 0, cz = 0;
            for (LXPoint p : cube.points) { cx += p.x; cy += p.y; cz += p.z; }
            int np = Math.max(1, cube.points.length);
            cubeCx[ci] = cx / np; cubeCy[ci] = cy / np; cubeCz[ci] = cz / np;
            edgeSum += cube.type != null ? cube.type.EDGE_WIDTH : 24;
            cubePhase[ci] = (ci * 0.6180339f) % 1f;
        }
        avgEdge = edgeSum / numCubes;

        for (int ci = 0; ci < numCubes; ci++) {
            float dx = cubeCx[ci] - modelCx, dy = cubeCy[ci] - modelCy, dz = cubeCz[ci] - modelCz;
            cubeDist[ci] = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / modelExtent;
        }

        for (int ti = 0; ti < numTowers; ti++) {
            List<CubesModel.Cube> tc = new ArrayList<CubesModel.Cube>(towers.get(ti).getCubes());
            Collections.sort(tc, new Comparator<CubesModel.Cube>() {
                public int compare(CubesModel.Cube a, CubesModel.Cube b) {
                    return Float.compare(a.y, b.y);
                }
            });
            int[] order = new int[tc.size()];
            towerCubeOrder[ti] = order;
            float tx = 0, tz = 0;
            for (int k = 0; k < tc.size(); k++) {
                int ci = cubes.indexOf(tc.get(k));
                if (ci < 0) continue;
                order[k] = ci;
                cubeTower[ci] = ti;
                cubeLevelIdx[ci] = k;
                cubeLevelN[ci] = tc.size() <= 1 ? 0.5f : (float) k / (tc.size() - 1);
                tx += cubeCx[ci]; tz += cubeCz[ci];
                for (LXPoint p : tc.get(k).points) {
                    pCube[p.index] = ci; pTower[p.index] = ti; pLevel[p.index] = k;
                    pLevelN[p.index] = cubeLevelN[ci];
                }
            }
            if (tc.size() > maxLevels) maxLevels = tc.size();
            towerXPos[ti] = tc.isEmpty() ? modelCx : tx / tc.size();
            towerZPos[ti] = tc.isEmpty() ? modelCz : tz / tc.size();
            towerMidCube[ti] = tc.isEmpty() ? -1 : order[tc.size() / 2];
        }

        // tower ordering by x position
        Integer[] order = new Integer[numTowers];
        for (int ti = 0; ti < numTowers; ti++) order[ti] = ti;
        Arrays.sort(order, new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                return Float.compare(towerXPos[a], towerXPos[b]);
            }
        });
        towerOrderX = new int[numTowers];
        for (int r = 0; r < numTowers; r++) towerOrderX[r] = order[r];

        for (int ci = 0; ci < numCubes; ci++) {
            CubesModel.Cube cube = cubes.get(ci);
            float half = Math.max(1f, (cube.type != null ? cube.type.EDGE_WIDTH : avgEdge) / 2f);
            List<CubesModel.Face> faces = cube.getFaces();
            for (int fi = 0; fi < faces.size(); fi++) {
                List<CubesModel.CubesStrip> strips = faces.get(fi).getStrips();
                for (int si = 0; si < strips.size(); si++) {
                    CubesModel.CubesStrip strip = strips.get(si);
                    LXPoint[] pts = strip.points;
                    int sg = fi * strips.size() + si;
                    for (int k = 0; k < pts.length; k++) {
                        int idx = pts[k].index;
                        pFace[idx] = fi; pStrip[idx] = si; pStripG[idx] = sg;
                        pVert[idx] = !strip.isHorizontal;
                        pStripT[idx] = pts.length <= 1 ? 0 : (float) k / (pts.length - 1);
                        pCornerU[idx] = Math.abs(2 * pStripT[idx] - 1);
                        pStripTau[idx] = 0.6f + 0.8f * hash01(ci * 12 + sg);
                    }
                }
            }
            int cnt = 0;
            for (LXPoint p : cube.points) {
                int idx = p.index;
                pPathT[idx] = cube.points.length <= 1 ? 0 : (float) cnt++ / cube.points.length;
                pCubePts[idx] = cube.points.length;
                pTowerRank[idx] = -1;
                pLx[idx] = (p.x - cubeCx[ci]) / half;
                pLy[idx] = (p.y - cubeCy[ci]) / half;
                pLz[idx] = (p.z - cubeCz[ci]) / half;
                pRad[idx] = (float) Math.sqrt(pLx[idx] * pLx[idx] + pLy[idx] * pLy[idx] + pLz[idx] * pLz[idx])
                    / 1.7320508f;
                pCubeDist[idx] = cubeDist[ci];
            }
        }
        for (int r = 0; r < numTowers; r++) {
            int ti = towerOrderX[r];
            for (int ci : towerCubeOrder[ti]) {
                if (ci < 0) continue;
                for (LXPoint p : model.getCubes().get(ci).points) pTowerRank[p.index] = r;
            }
        }

        theta = new float[numCubes]; thetaV = new float[numCubes]; thetaT = new float[numCubes];
        spinDelay = new float[numCubes]; pendingSpin = new float[numCubes];
        for (int ci = 0; ci < numCubes; ci++) theta[ci] = thetaT[ci] = cubePhase[ci] * 4f;
    }

    private float bandN(float pos) {
        pos = constrain(pos, 1, 14.999f);
        int i = (int) pos;
        return lerp(audio.bandNorm[i], audio.bandNorm[i + 1], pos - i);
    }

    private float hueRole(int role, float base, float spread) {
        switch (role) {
            case 1: return base + spread;
            case 2: return base + 180 - spread * 0.5f;
            default: return base;
        }
    }

    // ---------- event spawning (called from run() for all modes) ----------

    private void onKickAudio() {
        float speed = speedParam.getValuef();
        int kc = audio.kickCount;

        // Shockwave shell
        if (waves.size() < MAX_WAVES) {
            Wave w = new Wave();
            if ((kc / 16) % 2 == 0 && numTowers > 0) {
                int rank = pingpong(kc, numTowers);
                int ti = towerOrderX[rank];
                w.x = towerXPos[ti]; w.y = model.yMin; w.z = towerZPos[ti];
                lastKickRank = rank;
            } else {
                w.x = modelCx; w.y = model.yMin; w.z = modelCz;
                lastKickRank = -1;
            }
            w.speed = audio.tempoConfidence > 0.5f
                ? 1.2f * modelExtent / audio.periodMs
                : 0.02f + 0.12f * speed;
            w.str = audio.kickStrength;
            w.role = kc % 2;
            waves.add(w);
        }

        // EdgeRacers dash, cascade order by variation
        if (numTowers > 0) {
            int ovar = (kc / 16) % 3;
            float center = (numTowers - 1) / 2f;
            for (int ti = 0; ti < numTowers; ti++) {
                int rank = rankOfTower(ti);
                float c;
                switch (ovar) {
                    case 0: c = rank; break;
                    case 1: c = numTowers - 1 - rank; break;
                    default: c = Math.abs(rank - center) * 2; break;
                }
                dashT[ti] = -c * 40;
                dashPrev[ti] = 0;
                dashSign[ti] = dirTarget;
            }
            if (kc % 16 == 0) dirTarget = -dirTarget;
        }

        // FaceSpin quarter turn, domino by level
        int dvar = (kc / 16) % 2;
        for (int ci = 0; ci < numCubes; ci++) {
            int dir;
            if (dvar == 0) {
                dir = (cubeTower[ci] >= 0 && rankOfTower(cubeTower[ci]) % 2 == 1) ? -1 : 1;
            } else {
                dir = (cubeLevelIdx[ci] % 2 == 1) ? -1 : 1;
            }
            spinDelay[ci] = Math.max(1, cubeLevelIdx[ci] * 50);
            pendingSpin[ci] = dir;
        }

        // Shatter choreography
        shatterHit();

        // LavaBreath bubble
        if (numTowers > 0 && bubbles.size() < MAX_BUBBLES) {
            int ti = towerOrderX[kc % numTowers];
            Bubble b = new Bubble();
            b.x = towerXPos[ti]; b.z = towerZPos[ti]; b.y = model.yMin;
            b.r = avgEdge * 0.5f * (0.7f + audio.kickStrength);
            bubbles.add(b);
        }

        // LaserPlanes re-aim, alternating planes
        float azSign = ((kc / 16) % 2 == 0) ? 1 : -1;
        if (kc % 2 == 0) {
            azAT += (float) Math.toRadians(45) * azSign;
            elAT = (float) Math.toRadians(EL_LIST[kc % EL_LIST.length]);
        } else {
            azBT += (float) Math.toRadians(45) * azSign;
            elBT = (float) Math.toRadians(EL_LIST[kc % EL_LIST.length]);
        }

        // Heartbeat pulse
        spawnPulse(audio.kickStrength);
    }

    private void onSnareAudio() {
        if (waves.size() < MAX_WAVES && numTowers > 0) {
            Wave w = new Wave();
            int rank = lastKickRank < 0 ? numTowers - 1 : numTowers - 1 - lastKickRank;
            int ti = towerOrderX[rank];
            w.x = towerXPos[ti]; w.y = model.yMin; w.z = towerZPos[ti];
            w.speed = audio.tempoConfidence > 0.5f
                ? 1.2f * modelExtent / audio.periodMs
                : 0.02f + 0.12f * speedParam.getValuef();
            w.str = 0.5f * audio.snareStrength;
            w.role = 2;
            w.thin = true;
            waves.add(w);
        }
        if (lastHitCube >= 0 && numCubes > 0) {
            int best = -1; float bd = -1;
            for (int ci = 0; ci < numCubes; ci++) {
                float dx = cubeCx[ci] - cubeCx[lastHitCube];
                float dy = cubeCy[ci] - cubeCy[lastHitCube];
                float dz = cubeCz[ci] - cubeCz[lastHitCube];
                float d = dx * dx + dy * dy + dz * dz;
                if (d > bd) { bd = d; best = ci; }
            }
            if (best >= 0) {
                cubeHitT[best] = 0;
                cubeHitPeak[best] = 60;
                cubeHitRole[best] = 2;
            }
        }
        if (audio.msSinceKick < 0.6 * audio.periodMs) {
            spawnPulse(0.5f * audio.snareStrength);
        }
    }

    private void onHatAudio() {
        int picked = 0;
        for (int tries = 0; tries < 60 && picked < 6; tries++) {
            int i = rng.nextInt(n);
            int ci = pCube[i];
            if (ci >= 0 && cubeHitT[ci] < 1e8f
                && cubeHitPeak[ci] * Math.exp(-cubeHitT[ci] / 400f) > 10f) {
                if (sparkle[i] < 0.05f) {
                    sparkle[i] = audio.hatStrength;
                    picked++;
                }
            }
        }
    }

    private void shatterHit() {
        int kc = audio.kickCount;
        float str = audio.kickStrength;
        int var = (kc / 16) % 3;
        if (var == 0 && numTowers > 0) {
            int ti = towerOrderX[pingpong(kc, numTowers)];
            for (int ci = 0; ci < numCubes; ci++) {
                if (cubeTower[ci] == ti) hitCube(ci, 85 * str, 1);
            }
        } else if (var == 1 && maxLevels > 0) {
            int lvl = kc % maxLevels;
            for (int ci = 0; ci < numCubes; ci++) {
                if (cubeLevelIdx[ci] == lvl) hitCube(ci, 85 * str, 1);
            }
        } else {
            int count = 1 + Math.round(str * audio.intensity * numCubes * 0.25f);
            for (int j = 0; j < count; j++) {
                float h = (kc * 0.6180339f + j * 0.7548777f) % 1f;
                if (h < 0) h += 1f;
                hitCube((int) (h * numCubes), 85 * str, 1);
            }
        }
    }

    private void hitCube(int ci, float peak, int role) {
        if (ci < 0 || ci >= numCubes) return;
        cubeHitT[ci] = 0;
        cubeHitPeak[ci] = peak;
        cubeHitRole[ci] = role;
        lastHitCube = ci;
    }

    private int heartCube() {
        if (numTowers == 0) return -1;
        int ti = towerOrderX[(audio.kickCount / 16) % numTowers];
        return towerMidCube[ti];
    }

    private void spawnPulse(float str) {
        int heart = heartCube();
        if (heart < 0 || pulses.size() >= MAX_PULSES) return;
        Pulse p = new Pulse();
        p.t0 = nowMs;
        p.str = str;
        p.dur = Math.min(0.5f * audio.periodMs, 350);
        p.delay = new float[numCubes];
        for (int ci = 0; ci < numCubes; ci++) {
            float dx = cubeCx[ci] - cubeCx[heart];
            float dy = cubeCy[ci] - cubeCy[heart];
            float dz = cubeCz[ci] - cubeCz[heart];
            p.delay[ci] = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / (avgEdge * 1.5f) * 70f;
        }
        pulses.add(p);
    }

    private int rankOfTower(int ti) {
        for (int r = 0; r < numTowers; r++) if (towerOrderX[r] == ti) return r;
        return 0;
    }

    // ---------- per-frame aging of all mode state ----------

    private void ageState(double deltaMs) {
        float dt = (float) deltaMs;
        for (int w = waves.size() - 1; w >= 0; w--) {
            Wave wave = waves.get(w);
            wave.r += wave.speed * dt;
            wave.str *= (float) Math.exp(-dt / 900);
            if (wave.r > modelExtent * 1.6f || wave.str < 0.03f) waves.remove(w);
        }
        for (int i = pulses.size() - 1; i >= 0; i--) {
            Pulse p = pulses.get(i);
            if (nowMs - p.t0 > p.dur + 3000) pulses.remove(i);
        }
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            Bubble b = bubbles.get(i);
            b.y += model.yRange * dt / 1500f;
            if (b.y > model.yMax + b.r) bubbles.remove(i);
        }
        for (int ci = 0; ci < numCubes; ci++) {
            if (cubeHitT[ci] < 1e9f) cubeHitT[ci] += dt;
        }
        float sparkDecay = (float) Math.exp(-dt / 80);
        for (int i = 0; i < n; i++) sparkle[i] *= sparkDecay;

        // racer integrated offset + signed dash deltas
        float dashDur = Math.min(0.6f * audio.periodMs, 260);
        float lapsPerSec = lerp(0.02f,
            0.03f + 0.07f * speedParam.getValuef() + 0.12f * audio.mid, audio.presence);
        for (int ti = 0; ti < numTowers; ti++) {
            racerOffset[ti] += dirF * lapsPerSec * dt / 1000f;
            dashT[ti] += dt;
            if (dashT[ti] > 0) {
                float t = Math.min(1, dashT[ti] / dashDur);
                float eased = easeOutCubic(t);
                racerOffset[ti] += dashSign[ti] * (eased - dashPrev[ti]) * 0.25f;
                dashPrev[ti] = eased;
            }
        }
        dirF += (dirTarget - dirF) * (1f - (float) Math.exp(-dt / 400));

        // facespin springs + queued turns + slow drift
        float w = springW();
        float dtSec = dt / 1000f;
        float drift = 0.12f * speedParam.getValuef() * dtSec;
        for (int ci = 0; ci < numCubes; ci++) {
            if (spinDelay[ci] > 0) {
                spinDelay[ci] -= dt;
                if (spinDelay[ci] <= 0) thetaT[ci] += pendingSpin[ci];
            }
            thetaT[ci] += drift;
            float acc = w * w * (thetaT[ci] - theta[ci]) - 2 * w * thetaV[ci];
            thetaV[ci] += acc * dtSec;
            theta[ci] += thetaV[ci] * dtSec;
        }

        // laser plane angle springs + drift
        float azDrift = (float) Math.toRadians(6) * speedParam.getValuef() * dtSec;
        azAT += azDrift; azBT += azDrift;
        azAv += (w * w * (azAT - azA) - 2 * w * azAv) * dtSec; azA += azAv * dtSec;
        elAv += (w * w * (elAT - elA) - 2 * w * elAv) * dtSec; elA += elAv * dtSec;
        azBv += (w * w * (azBT - azB) - 2 * w * azBv) * dtSec; azB += azBv * dtSec;
        elBv += (w * w * (elBT - elB) - 2 * w * elBv) * dtSec; elB += elBv * dtSec;
        ang3 += dt * 0.0003f;
        float sRate = audio.tempoConfidence > 0.5f
            ? 1f / (2f * audio.periodMs)
            : (0.1f + 0.4f * audio.mid) / 1000f;
        sweepS += sRate * dt;
        if (audio.tempoConfidence > 0.5f) {
            float tgt = audio.beatPhase / 2f + (audio.kickCount % 2) * 0.5f;
            float err = tgt - sweepS;
            err -= Math.floor(err + 0.5f);
            sweepS += err * (1f - (float) Math.exp(-dt / 500));
        }
        sweepS -= Math.floor(sweepS);

        // idle timers
        idleWaveMs += dt; idleEmberMs += dt; idlePulseMs += dt;
        if (audio.presence < 0.3f) {
            if (idleWaveMs > 4000 && waves.size() < MAX_WAVES) {
                Wave wv = new Wave();
                wv.x = modelCx; wv.y = modelCy; wv.z = modelCz;
                wv.speed = 0.02f + 0.12f * speedParam.getValuef();
                wv.str = 0.15f; wv.role = 0;
                waves.add(wv);
                idleWaveMs = 0;
            }
            if (idleEmberMs > 3000 && numCubes > 0) {
                hitCube(rng.nextInt(numCubes), 15, 1);
                idleEmberMs = 0;
            }
            if (idlePulseMs > 3000) {
                spawnPulse(0.2f);
                idlePulseMs = 0;
            }
        }

        // hue drift + section lift
        hueDrift += dt * (1.5f / 60000f) * (0.5f + audio.intensity);
        if (audio.intensity < 0.35f) {
            lowIntensityMs += dt;
        } else {
            if (audio.intensity > 0.6f && lowIntensityMs >= 8000) liftRemaining += 35;
            lowIntensityMs = 0;
        }
        if (liftRemaining > 0) {
            float d = Math.min(liftRemaining, 35f / 4000f * dt);
            hueDrift += d;
            liftRemaining -= d;
        }
        evoClock += dt * (0.3f + audio.intensity);
    }

    // ---------- main loop ----------

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        nowMs += deltaMs;
        if (n == 0) {
            markModified(SRGB8);
            return;
        }

        eq.gain.setNormalized(gainParam.getValuef());
        eq.range.setNormalized(rangeParam.getValuef());
        float av = attackParam.getValuef(), rv = releaseParam.getValuef();
        audio.attackMs = 5 + 145 * av;
        audio.releaseMs = 60 + 1440 * rv * rv;
        audio.sensitivity = sensParam.getValuef();
        for (int i = 0; i < eqBands.length; i++) {
            eqBands[i] = i < eq.numBands ? eq.getBandf(i) : 0;
        }
        audio.update(eqBands, deltaMs);
        idleMix = 1 - audio.presence;

        if (audio.kick) onKickAudio();
        if (audio.snare) onSnareAudio();
        if (audio.hat) onHatAudio();
        ageState(deltaMs);

        int mode = modeParam.getValuei() % NUM_MODES;
        float speed = speedParam.getValuef();
        float size = sizeParam.getValuef();
        float baseHue = palette.getHuef() + hueParam.getValuef() + hueDrift;
        float sat = satParam.getValuef();
        float spread = spreadParam.getValuef();

        renderMode(mode, deltaMs, baseHue, sat, spread, speed, size, bufH, bufS, bufB);
        lastRenderedMode = mode;

        if (prevMode >= 0) {
            xfadeMs += deltaMs;
            float xf = Math.max(1f, xfadeParam.getValuef());
            float k = Math.min(1f, xfadeMs / xf);
            renderMode(prevMode, deltaMs, baseHue, sat, spread, speed, size, prevH, prevS, prevB);
            for (int i = 0; i < n; i++) {
                if (prevB[i] >= 0.5f && bufB[i] >= 0.5f) {
                    float dh = ((bufH[i] - prevH[i]) % 360 + 540) % 360 - 180;
                    bufH[i] = prevH[i] + dh * k;
                } else if (bufB[i] < 0.5f && prevB[i] >= 0.5f) {
                    bufH[i] = prevH[i];
                }
                bufS[i] = lerp(prevS[i], bufS[i], k);
                bufB[i] = lerp(prevB[i], bufB[i], k);
            }
            if (k >= 1f) prevMode = -1;
        }

        // trails combine
        float tv = trailsParam.getValuef();
        float decay = tv <= 0 ? 0 : (float) Math.exp(-deltaMs / (25 + tv * tv * 1600));
        for (int i = 0; i < n; i++) {
            float tb = trailB[i] * decay;
            if (tb > bufB[i]) {
                trailB[i] = tb;
                finB[i] = tb;
                bufH[i] = trailH[i];
                bufS[i] = trailS[i];
            } else {
                trailB[i] = bufB[i];
                trailH[i] = bufH[i];
                trailS[i] = bufS[i];
                finB[i] = bufB[i];
            }
        }

        // soft knee then frame mean limiter
        float mean = 0;
        for (int i = 0; i < n; i++) {
            float b = finB[i];
            if (b > 70) b = 70 + 30 * (float) Math.tanh((b - 70) / 30);
            finB[i] = b;
            mean += b;
        }
        mean /= n;
        float target = mean > 35 ? 35 / mean : 1;
        float tau = target < limiterGain ? 50 : 500;
        limiterGain += (target - limiterGain) * (1f - (float) Math.exp(-deltaMs / tau));
        for (int i = 0; i < n; i++) {
            colors[i] = LXColor.hsb(bufH[i], bufS[i], Math.min(100, finB[i] * limiterGain));
        }
        markModified(SRGB8);
    }

    private void renderMode(int mode, double deltaMs, float baseHue, float sat,
                            float spread, float speed, float size,
                            float[] H, float[] S, float[] B) {
        switch (mode) {
            case 0: renderTowerVU(deltaMs, baseHue, sat, spread, H, S, B); break;
            case 1: renderShockwave(baseHue, sat, spread, size, H, S, B); break;
            case 2: renderEdgeRacers(deltaMs, baseHue, sat, spread, speed, size, H, S, B); break;
            case 3: renderFaceSpin(baseHue, sat, spread, H, S, B); break;
            case 4: renderShatter(baseHue, sat, spread, size, H, S, B); break;
            case 5: renderLavaBreath(deltaMs, baseHue, sat, spread, speed, H, S, B); break;
            case 6: renderLaserPlanes(baseHue, sat, spread, speed, size, H, S, B); break;
            case 7: renderHeartbeat(baseHue, sat, H, S, B); break;
        }
    }

    private void clearBuf(float[] H, float[] S, float[] B, float s) {
        Arrays.fill(H, 0); Arrays.fill(S, s); Arrays.fill(B, 0);
    }

    /** Towers as VU meters; tower x-rank picks the EQ band. */
    private void renderTowerVU(double deltaMs, float baseHue, float sat, float spread,
                               float[] H, float[] S, float[] B) {
        clearBuf(H, S, B, sat);
        if (numTowers == 0) return;
        int var = (audio.kickCount / 32) % 2;
        float center = (numTowers - 1) / 2f;
        float accent = baseHue + 180 - spread * 0.5f;
        float breath = (float) (0.5 + 0.5 * Math.sin(nowMs * TWO_PI / 6000));
        List<CubesModel.Cube> cubes = model.getCubes();

        for (int r = 0; r < numTowers; r++) {
            int ti = towerOrderX[r];
            int[] order = towerCubeOrder[ti];
            int len = order.length;
            if (len == 0) continue;

            float target;
            if (var == 0) {
                target = numTowers <= 1 ? 1 : 1 + r * 13f / (numTowers - 1);
            } else {
                float dn = numTowers <= 1 ? 0 : Math.abs(r - center) / Math.max(1f, center);
                target = 1 + 13 * dn;
            }
            float alpha = 1f - (float) Math.exp(-deltaMs / 600);
            towerBandPos[ti] += (target - towerBandPos[ti]) * alpha;

            float bv = bandN(towerBandPos[ti]);
            float fill = (float) Math.pow(bv, 0.8f) * len;
            fill *= 1 + 0.15f * audio.kickEnv;

            if (fill > towerPeakPos[ti]) {
                towerPeakPos[ti] = fill;
                towerPeakVel[ti] = 0;
            } else {
                towerPeakVel[ti] += (float) deltaMs * 2f * len / (1200f * 1200f);
                towerPeakPos[ti] = Math.max(0, towerPeakPos[ti] - towerPeakVel[ti] * (float) deltaMs);
            }
            int peakCube = Math.min(len - 1, (int) towerPeakPos[ti]);

            for (int k = 0; k < len; k++) {
                int ci = order[k];
                if (ci < 0) continue;
                float lit = constrain(fill - k, 0, 1);
                float hue = lerp(baseHue, baseHue + spread, len <= 1 ? 0 : (float) k / (len - 1));
                boolean isPeak = k == peakCube && towerPeakPos[ti] > 0.2f;
                boolean isBottom = k == 0;
                boolean isTop = k == len - 1;
                for (LXPoint p : cubes.get(ci).points) {
                    int i = p.index;
                    float b = 0, h = hue, s = sat;
                    if (lit > 0) {
                        float within = (pLy[i] + 1f) * 0.5f;
                        b = lit >= 1f || within < lit ? 85 : 0;
                    }
                    if (isPeak && !pVert[i] && pLy[i] > 0) {
                        h = accent; s = sat * 0.55f; b = Math.max(b, 70 + 30 * audio.hatEnv);
                    }
                    if (isBottom && !pVert[i] && pLy[i] < 0) {
                        float th = 70 * audio.kickEnv * audio.kickStrength;
                        if (th > b) { b = th; h = accent; s = sat * 0.55f; }
                    }
                    if (isTop && !pVert[i] && pLy[i] > 0) {
                        b = Math.max(b, idleMix * 4 * breath);
                    }
                    H[i] = h; S[i] = s; B[i] = b;
                }
            }
        }
    }

    /** Kick-spawned spherical shells rising from tower bases. */
    private void renderShockwave(float baseHue, float sat, float spread, float size,
                                 float[] H, float[] S, float[] B) {
        clearBuf(H, S, B, sat);
        float thickBase = avgEdge * 0.25f * (0.6f + size) * (1 + 0.8f * audio.mid);
        float bright = 85 * (0.6f + 0.4f * audio.intensity);
        for (int i = 0; i < n; i++) {
            LXPoint p = model.points[i];
            float acc = 0, hue = baseHue, hs = sat;
            for (Wave wave : waves) {
                float dx = p.x - wave.x, dy = p.y - wave.y, dz = p.z - wave.z;
                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                float thick = thickBase * (wave.thin ? 0.5f : 1f);
                float d = dist - wave.r;
                float g = (float) Math.exp(-d * d / (thick * thick)) * wave.str;
                if (d < 0) g += g * 0.15f;
                if (g > acc) { acc = g; hue = hueRole(wave.role, baseHue, spread); }
                else acc = Math.min(1f, acc + g * 0.5f);
            }
            H[i] = hue; S[i] = hs; B[i] = acc * bright;
        }
    }

    /** Comets racing around cube perimeter paths; kick dashes cascade. */
    private void renderEdgeRacers(double deltaMs, float baseHue, float sat, float spread,
                                  float speed, float size, float[] H, float[] S, float[] B) {
        clearBuf(H, S, B, sat);
        float accent = baseHue + 180 - spread * 0.5f;
        float bright = 40 + 45 * audio.level;
        float dim = lerp(0.10f, 1f, audio.presence);
        for (int i = 0; i < n; i++) {
            int ci = pCube[i];
            if (ci < 0) continue;
            float s = pTowerRank[i] % 2 == 1 ? -1 : 1;
            int ti = pTower[i] >= 0 ? pTower[i] : 0;
            float pos = (s * (pPathT[i] - cubePhase[ci]) - racerOffset[ti]) % 1f;
            if (pos < 0) pos += 1f;
            float tailLap = (4 + 10 * size) / Math.max(4, pCubePts[i]);
            float acc = 0; boolean head = false;
            for (int k = 0; k < 4; k++) {
                float wgt = constrain(1 + audio.mid * 3 - k, 0, 1);
                if (wgt <= 0) continue;
                float d = (pos - k / 4f) % 1f;
                if (d < 0) d += 1f;
                float v = (float) Math.exp(-d / Math.max(0.02f, tailLap)) * wgt;
                if (v > acc) {
                    acc = v;
                    head = d < 1.5f / Math.max(4, pCubePts[i]);
                }
            }
            if (head && acc > 0.5f) {
                H[i] = accent; S[i] = sat * 0.55f;
                B[i] = Math.min(100, (acc * bright + 20 * audio.hatEnv) * dim);
            } else {
                H[i] = baseHue + spread * (1 - Math.min(1, acc));
                B[i] = Math.min(100, acc * bright * dim);
            }
        }
    }

    /** Per-cube lit face spring-rotating a quarter turn per kick. */
    private void renderFaceSpin(float baseHue, float sat, float spread,
                              float[] H, float[] S, float[] B) {
        clearBuf(H, S, B, sat);
        float accent = baseHue + 180 - spread * 0.5f;
        float width = 0.6f + 0.9f * audio.mid;
        for (int i = 0; i < n; i++) {
            int ci = pCube[i];
            if (ci < 0 || pFace[i] < 0) continue;
            float th = theta[ci] % 4f;
            if (th < 0) th += 4f;
            float w = Math.max(0, 1 - circDist(pFace[i], th, 4) / width);
            float ow = Math.max(0, 1 - circDist(pFace[i], (th + 2) % 4f, 4) / width);
            float lit = (30 + 60 * audio.bass) * w;
            float opp = 35 * audio.high * ow;
            if (opp > lit) {
                H[i] = accent; B[i] = opp;
            } else {
                H[i] = baseHue + pCubeDist[i] * spread * 0.4f; B[i] = lit;
            }
            B[i] = Math.max(B[i], idleMix * 8 * w);
        }
    }

    /** Kick-choreographed cube hits shattering into per-strip decay fragments. */
    private void renderShatter(float baseHue, float sat, float spread, float size,
                               float[] H, float[] S, float[] B) {
        clearBuf(H, S, B, sat);
        float baseTau = audio.tempoConfidence > 0.5f
            ? 0.7f * audio.periodMs : 400 * (0.5f + size);
        for (int i = 0; i < n; i++) {
            int ci = pCube[i];
            if (ci < 0 || cubeHitT[ci] > 1e8f) continue;
            float tau = baseTau * pStripTau[i];
            float v = (float) Math.exp(-cubeHitT[ci] / Math.max(20, tau));
            if (v < 0.01f && sparkle[i] < 0.01f) continue;
            if (cubeHitRole[ci] == 2) {
                H[i] = baseHue + 180 - spread * 0.5f;
            } else {
                H[i] = baseHue + spread * v;
            }
            S[i] = sat * (1 - 0.5f * v);
            B[i] = Math.min(100, cubeHitPeak[ci] * v + 30 * sparkle[i]);
        }
    }

    /** Lava surface rising with slow bass; kick bubbles float up through it. */
    private void renderLavaBreath(double deltaMs, float baseHue, float sat, float spread,
                                  float speed, float[] H, float[] S, float[] B) {
        clearBuf(H, S, B, sat);
        bassSlow += (audio.bass - bassSlow) * (1f - (float) Math.exp(-deltaMs / 400));
        flowT += deltaMs * (0.0002f + 0.0012f * speed) * (0.3f + 1.4f * audio.mid);
        float bs = lerp(0.12f, bassSlow, audio.presence);
        float surfBase = model.yMin + model.yRange * (0.15f + 0.7f * bs);
        float scale = 3f / Math.max(1f, modelExtent);
        float t = flowT;
        float ang = evoClock * 0.0004f;
        float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);
        float accent = baseHue + 180 - spread * 0.5f;
        float dim = lerp(0.4f, 1f, audio.presence);
        float secondary = baseHue + spread;

        for (int i = 0; i < n; i++) {
            LXPoint p = model.points[i];
            float dx = (p.x - modelCx) * scale, dy = (p.y - modelCy) * scale, dz = (p.z - modelCz) * scale;
            float xr = dx * ca - dz * sa, zr = dx * sa + dz * ca;
            float f = ((float) Math.sin(xr * 2.1 + t * 2.3)
                + (float) Math.sin(dy * 1.7 - t * 3.1)
                + (float) Math.sin((xr + zr) * 1.3 + t * 1.1)) / 3f;
            f = (f + 1f) * 0.5f;
            float surface = surfBase + (f - 0.5f) * avgEdge * 0.8f * (0.5f + audio.mid);
            float depth = surface - p.y;
            float inside = depth >= 0 ? 1 : (float) Math.exp(depth / (avgEdge * 0.3f));
            float tt = constrain((p.y - model.yMin) / Math.max(1f, surface - model.yMin), 0, 1);
            float b = (20 + 45 * f) * (0.45f + 0.55f * pCornerU[i]) * inside * dim;
            float h = accent + (baseHue - accent) * tt;
            // bubbles
            for (Bubble bub : bubbles) {
                float bx = p.x - bub.x, by = p.y - bub.y, bz = p.z - bub.z;
                if (bx * bx + by * by + bz * bz < bub.r * bub.r) {
                    float fade = (float) Math.exp(-Math.max(0, bub.y - surfBase) / (avgEdge * 0.5f));
                    float bv = 70 * fade;
                    if (bv > b) { b = bv; h = secondary; }
                }
            }
            H[i] = h; B[i] = Math.min(100, b);
        }
    }

    /** Two spring-aimed laser planes; kicks re-aim alternately. */
    private void renderLaserPlanes(float baseHue, float sat, float spread, float speed, float size,
                                   float[] H, float[] S, float[] B) {
        clearBuf(H, S, B, sat);
        float offA = (float) Math.sin(TWO_PI * sweepS) * modelExtent * 0.6f;
        float offB = (float) Math.sin(TWO_PI * (sweepS + 0.5f)) * modelExtent * 0.6f;
        float thick = avgEdge * 0.12f * (0.6f + size) * (1 + 1.2f * audio.mid);
        float thickB = thick * (1 + audio.snareEnv);
        float bright = (30 + 55 * audio.bass) * lerp(0.35f, 1f, audio.presence);
        float accent = baseHue + 180 - spread * 0.5f;
        float secondary = baseHue + spread;
        float w3 = constrain((audio.intensity - 0.65f) / 0.15f, 0, 1);

        float ca = (float) Math.cos(elA), sa = (float) Math.sin(elA);
        float n1x = ca * (float) Math.cos(azA), n1y = sa, n1z = ca * (float) Math.sin(azA);
        float cb = (float) Math.cos(elB), sb = (float) Math.sin(elB);
        float n2x = cb * (float) Math.cos(azB), n2y = sb, n2z = cb * (float) Math.sin(azB);
        float n3x = (float) Math.cos(ang3), n3y = 0.4f, n3z = (float) Math.sin(ang3);
        float l3 = (float) Math.sqrt(n3x * n3x + n3y * n3y + n3z * n3z);
        n3x /= l3; n3y /= l3; n3z /= l3;

        for (int i = 0; i < n; i++) {
            LXPoint p = model.points[i];
            float dx = p.x - modelCx, dy = p.y - modelCy, dz = p.z - modelCz;
            float d1 = Math.abs(dx * n1x + dy * n1y + dz * n1z - offA);
            float d2 = Math.abs(dx * n2x + dy * n2y + dz * n2z - offB);
            float g1 = (float) Math.exp(-d1 * d1 / (thick * thick));
            float g2 = (float) Math.exp(-d2 * d2 / (thickB * thickB));
            float d3 = Math.abs(dx * n3x + dy * n3y + dz * n3z);
            float g3 = (float) Math.exp(-d3 * d3 / (thick * thick)) * w3;
            if (g1 > 0.3f && g2 > 0.3f) {
                H[i] = accent; S[i] = sat * 0.55f;
                B[i] = Math.min(100, Math.min(g1, g2) * bright + 35 * audio.hatEnv);
            } else if (g3 > Math.max(g1, g2) && g3 > 0.05f) {
                H[i] = accent; B[i] = g3 * bright;
            } else if (g1 >= g2) {
                H[i] = baseHue; B[i] = g1 * bright;
            } else {
                H[i] = secondary; B[i] = g2 * bright;
            }
        }
    }

    /** Rings contracting inside each cube, rippling out from a heart cube. */
    private void renderHeartbeat(float baseHue, float sat, float[] H, float[] S, float[] B) {
        clearBuf(H, S, B, sat);
        float accent = baseHue + 180 - spreadParam.getValuef() * 0.5f;
        float width = 0.18f + 0.2f * audio.mid;
        for (int i = 0; i < n; i++) {
            int ci = pCube[i];
            if (ci < 0) continue;
            float u = pCornerU[i];
            float b = pVert[i] ? (6 + 14 * audio.bass) * (1 - u) : 0;
            for (Pulse p : pulses) {
                float te = (float) (nowMs - p.t0) - p.delay[ci];
                if (te <= 0 || te > p.dur) continue;
                float ringPos = 1 - te / p.dur;
                float dd = u - ringPos;
                b += 75 * p.str * (float) Math.exp(-dd * dd / (width * width));
            }
            H[i] = pVert[i] ? baseHue : accent;
            B[i] = Math.min(100, b);
        }
    }
}
