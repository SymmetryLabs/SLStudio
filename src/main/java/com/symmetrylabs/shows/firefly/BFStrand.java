package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class BFStrand extends FPSPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    DiscreteParameter strandNum = new DiscreteParameter("strand", 0, 0, 100);
    BooleanParameter tracer = new BooleanParameter("tracer", false);
    // The index of the fixture.  This will dynamically update on whether flowers or butterflies are selected
    // and which strand is selected.
    DiscreteParameter index = new DiscreteParameter("index", 0, 200);
    public int currentIndex;

    public BFStrand(LX lx) {
        super(lx);
        addParameter("fps", fpsKnob);
        addParameter("strand", strandNum);
        addParameter("tracer", tracer);
        addParameter("index", index);
    }

    @Override
    public void onActive() {
        strandNum.setRange(0, KaledoscopeModel.allStrands.size());
    }

    @Override
    protected void renderFrame(double deltaMs) {
        constrainValues();

        // clear everybody
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }

        int color = getColor();

        KaledoscopeModel.Strand strand = KaledoscopeModel.allStrands.get(strandNum.getValuei());

        if (!tracer.getValueb()) {
            if (strand.strandType == KaledoscopeModel.Strand.StrandType.BUTTERFLY) {
                if (index.getValuei() >= strand.butterflies.size())
                    index.setValue(strand.butterflies.size() - 1);
                if (index.getValuei() == -1) {
                    int j = 0;
                    for (LUButterfly butterfly : strand.butterflies) {
                        color = getColorForIndex(j);
                        butterfly.setColor(colors, color);
                        j++;
                    }
                } else {
                    strand.butterflies.get(index.getValuei()).setColor(colors, color);
                }
            } else {
                if (index.getValue() >= strand.flowers.size())
                    index.setValue(strand.flowers.size() - 1);
                if (index.getValuei() == -1) {
                    int j = 0;
                    for (LUFlower flower : strand.flowers) {
                        color = getColorForIndex(j);
                        flower.setColor(colors, color);
                        j++;
                    }
                } else {
                    strand.flowers.get(index.getValuei()).setColor(colors, color);
                }
            }
        } else {
            if (strand.strandType == KaledoscopeModel.Strand.StrandType.BUTTERFLY) {
                // We need to sanity check this because the user might be switching strands.
                if (currentIndex >= strand.butterflies.size())
                    currentIndex = strand.butterflies.size() - 1;
                strand.butterflies.get(currentIndex).setColor(colors, color);
                currentIndex = (currentIndex + 1) % strand.butterflies.size();
            } else {
                if (currentIndex >= strand.flowers.size())
                    currentIndex = strand.flowers.size() - 1;
                strand.flowers.get(currentIndex).setColor(colors, color);
                currentIndex = (currentIndex + 1) % strand.flowers.size();
            }
        }
    }

    /**
     * Strand lengths can vary, so as the user changes the strand number make sure we constrain the values properly.
     */
    public void constrainValues() {
        KaledoscopeModel.Strand strand = KaledoscopeModel.allStrands.get(strandNum.getValuei());
        if (strand.strandType == KaledoscopeModel.Strand.StrandType.BUTTERFLY) {
            index.setRange(-1, strand.butterflies.size());
            if (index.getValuei() >= strand.butterflies.size())
                index.setValue(strand.butterflies.size() - 1);
            if (currentIndex >= strand.butterflies.size())
                currentIndex = strand.butterflies.size() - 1;
        } else {
            index.setRange(-1, strand.flowers.size());
            if (index.getValue() >= strand.flowers.size())
                index.setValue(strand.flowers.size() - 1);
            if (currentIndex >= strand.flowers.size())
                currentIndex = strand.flowers.size() - 1;
        }
    }

    int getColor() {
        int color = getColorForIndex(index.getValuei());
        if (tracer.getValueb())
            color = getColorForIndex(currentIndex);
        return color;
    }

    int getColorForIndex(int index) {
        int color = LXColor.rgb(255, 255, 255);
        if (index % 10 == 0) {
            color = LXColor.rgb(255, 0, 0);
        } else if (index % 5 == 0) {
            color = LXColor.rgb(0, 255, 0);
        } else if ((index + 1)% 10 == 0) {
            color = LXColor.rgb(255, 255, 0);
        } else if ((index - 1) % 10 == 0) {
            color = LXColor.rgb(255, 0, 255);
        } else if ((index + 1) % 5 == 0) {
            color = LXColor.rgb(0, 255, 255);
        } else if ((index - 1)% 5 == 0) {
            color = LXColor.rgb(0, 0, 255);
        }
        return color;
    }
}
