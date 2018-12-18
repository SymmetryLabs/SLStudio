package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import static com.symmetrylabs.slstudio.pattern.instruments.EmitterInstrument.*;
import static heronarts.lx.PolyBuffer.Space.RGB16;

public class JetEmitter extends AbstractEmitter implements Emitter {
    public static final int MAX_JETS = 20;

    @Override
    public Jet emit(Instrument.ParameterSet paramSet, int pitch, double intensity) {
        System.out.println("new jet " + pitch + " at " + intensity);
        return new Jet(
            paramSet.getPosition(randomXyDisc()),
            paramSet.getSize(intensity),
            paramSet.getColor(randomVariation()),
            paramSet.getDirection(),
            paramSet.getRate() * 2
        );
    }

    @Override
    public List<Mark> discardMarks(List<Mark> unexpiredMarks) {
        List<Mark> marks = new ArrayList<>(unexpiredMarks);
        List<Mark> discardedMarks = new ArrayList<>();
        while (marks.size() > MAX_JETS) {
            Jet weakest = null;
            float smallestTotal = 0;
            for (Mark mark : marks) {
                if (mark instanceof Jet) {
                    Jet jet = (Jet) mark;
                    if (weakest == null || jet.getFluidTotal() < smallestTotal) {
                        weakest = jet;
                        smallestTotal = jet.getFluidTotal();
                    }
                }
            }
            if (weakest != null) {
                discardedMarks.add(weakest);
                marks.remove(weakest);
            } else {
                break;
            }
        }
        return discardedMarks;
    }

    class Jet implements Mark {
        public LXVector center;
        public double size;
        public long color;
        public LXVector direction;
        public double rate;

        private Fluid fluid = new Fluid(100, 11);
        private int originU = fluid.height / 2;  // near one end
        private int originV = fluid.height / 2;
        private float periodSec;
        private float addRate;

        public Jet(LXVector center, double size, long color, LXVector direction, double rate) {
            this.center = center;
            this.size = size;
            this.color = color;
            this.direction = direction;
            this.rate = rate;

            fluid.setDiffusion(2);
            fluid.setVelocity(new PVector(15, 0));
            periodSec = (1 / 60f) / 3;  // 3 iterations per frame
            addRate = 50;
        }

        public void advance(double deltaSec, double intensity, boolean sustain) {
            // System.out.println("mark " + this + " advance");
            float fluidSec = (float) (deltaSec * rate);
            if (sustain) {
                fluid.addCell(originU, originV, (float) (intensity * addRate * fluidSec));
            }
            fluid.advance(fluidSec, periodSec);
            if (!sustain) {
                fluid.setRetention(0.5f);
            }
        }

        public boolean isExpired() {
            return fluid.getLastMax() < 0.001;
        }

        public float getFluidTotal() {
            return fluid.getLastTotal();
        }

        public void render(LXModel model, PolyBuffer buffer) {
            float scale = 20 * (float) size / fluid.width;
            LXVector uBasis = new LXVector(direction);
            LXVector vBasis = new LXVector(-uBasis.y, uBasis.x, 0);
            LXVector negCenter = new LXVector(center).mult(-1);

            long[] colors = (long[]) buffer.getArray(RGB16);
            int cr = Ops16.red(color);
            int cg = Ops16.green(color);
            int cb = Ops16.blue(color);
            for (int i = 0; i < model.points.length; i++) {
                LXVector p = new LXVector(model.points[i]).add(negCenter);
                float u = originU + p.dot(uBasis) / scale;
                float v = originV + p.dot(vBasis) / scale;
                int uLo = (int) Math.floor(u);
                int vLo = (int) Math.floor(v);
                float value = lerp(
                    lerp(fluid.getCell(uLo, vLo), fluid.getCell(uLo + 1, vLo), u - uLo),
                    lerp(fluid.getCell(uLo, vLo + 1), fluid.getCell(uLo + 1, vLo + 1), u - uLo),
                    v - vLo
                );

                int r = Ops16.red(colors[i]);
                int g = Ops16.green(colors[i]);
                int b = Ops16.blue(colors[i]);
                colors[i] = Ops16.rgba(
                    (int) (r + cr * value),
                    (int) (g + cg * value),
                    (int) (b + cb * value),
                    Ops16.MAX
                );
            }
            buffer.markModified(RGB16);
        }

        private float lerp(float start, float stop, float fraction) {
            return start + (stop - start) * fraction;
        }
    }
}
