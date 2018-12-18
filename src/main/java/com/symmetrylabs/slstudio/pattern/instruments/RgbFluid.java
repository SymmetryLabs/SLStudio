package com.symmetrylabs.slstudio.pattern.instruments;

import processing.core.PVector;

public class RgbFluid {
    public final int width;
    public final int height;

    public static class FloatRgb {
        public float r;
        public float g;
        public float b;

        public FloatRgb() {
            this(0, 0, 0);
        }

        public FloatRgb(FloatRgb other) {
            this(other.r, other.g, other.b);
        }

        public FloatRgb(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public FloatRgb scale(float factor) {
            r *= factor;
            g *= factor;
            b *= factor;
            return this;
        }

        public float getMass() {
            return r + g + b;
        }
    }

    FloatRgb[] cells;
    FloatRgb[] nextCells;
    float lastTotal;
    float lastMax;
    float diffusion = 1;
    float retention = 1;
    float vx = 0;
    float vy = 0;
    float dx = 0;
    float dy = 0;
    int iterations = 0;

    public RgbFluid(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new FloatRgb[width * height];
        nextCells = new FloatRgb[width * height];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new FloatRgb();
            nextCells[i] = new FloatRgb();
        }
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
        for (int tx = 0; tx < width; tx++) {
            for (int ty = 0; ty < height; ty++) {
                int x = tx - xi;
                int y = ty - yi;

                FloatRgb cell = getCell(x, y);
                float mass = cell.getMass();
                total += mass;
                max = Math.max(max, mass);
                float pressure = mass * 4
                    - getCell(x - 1, y).getMass() - getCell(x + 1, y).getMass()
                    - getCell(x, y - 1).getMass() - getCell(x, y + 1).getMass();

                FloatRgb nextCell = nextCells[ty * width + tx];
                float outflow = pressure * diffusion * (float) deltaSec;
                nextCell.r = (cell.r - (cell.r / mass) * outflow) * retentionFactor;
                nextCell.g = (cell.g - (cell.g / mass) * outflow) * retentionFactor;
                nextCell.b = (cell.b - (cell.b / mass) * outflow) * retentionFactor;
            }
        }
        FloatRgb[] oldCells = cells;
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

    public FloatRgb getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[y * width + x];
        }
        return new FloatRgb();
    }

    public void setCell(int x, int y, FloatRgb value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[y * width + x] = value;
        }
    }

    public void addCell(int x, int y, FloatRgb value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            FloatRgb cell = cells[y * width + x];
            cell.r += value.r;
            cell.g += value.g;
            cell.b += value.b;
        }
    }
}
