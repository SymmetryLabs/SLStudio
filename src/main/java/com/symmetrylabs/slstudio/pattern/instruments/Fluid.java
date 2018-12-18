package com.symmetrylabs.slstudio.pattern.instruments;

import processing.core.PVector;

public class Fluid {
    public final int width;
    public final int height;

    float[] cells;
    float[] nextCells;
    float lastTotal;
    float lastMax;
    float diffusion = 1;
    float retention = 1;
    float vx = 0;
    float vy = 0;
    float dx = 0;
    float dy = 0;
    int iterations = 0;

    public Fluid(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new float[width * height];
        nextCells = new float[width * height];
    }

    public void setDiffusion(float diffusion) {
        this.diffusion = diffusion;
    }

    public void setRetention(float retention) {
        this.retention = retention;
    }

    public void setVelocity(PVector velocity) {
        this.vx = velocity.x;
        this.vy = velocity.y;
    }

    public double advance(double deltaSec, double periodSec) {
        while (deltaSec > periodSec) {
            advance(periodSec);
            deltaSec -= periodSec;
        }
        return deltaSec;
    }

    public void advance(double deltaSec) {
        dx += vx * deltaSec;
        dy += vy * deltaSec;
        int xi = (int) dx;
        int yi = (int) dy;
        dx -= xi;
        dy -= yi;
        float total = 0;
        float max = 0;
        float retentionFactor = (float) Math.pow(retention, deltaSec);
        System.out.println(String.format("%s: ret=%.4f retFac=%.4f", this, retention, retentionFactor));
        for (int tx = 0; tx < width; tx++) {
            for (int ty = 0; ty < height; ty++) {
                int x = tx - xi;
                int y = ty - yi;
                float cell = getCell(x, y);
                total += cell;
                max = Math.max(max, cell);
                float pressure = cell * 4
                    - getCell(x - 1, y) - getCell(x + 1, y)
                    - getCell(x, y - 1) - getCell(x, y + 1);
                nextCells[ty * width + tx] =
                    (cell - pressure * diffusion * (float) deltaSec) * retentionFactor;
            }
        }
        float[] oldCells = cells;
        cells = nextCells;
        nextCells = oldCells;
        lastTotal = total;
        lastMax = max;
        iterations++;
    }

    public int getIterations() {
        return iterations;
    }

    public float getLastTotal() {
        return lastTotal;
    }

    public float getLastMax() {
        return lastMax;
    }

    public float getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[y * width + x];
        }
        return 0;
    }

    public void setCell(int x, int y, float value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[y * width + x] = value;
        }
    }

    public void addCell(int x, int y, float value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[y * width + x] += value;
        }
    }
}
