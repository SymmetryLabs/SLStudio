package com.symmetrylabs.shows.magicleap;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.StripsTopologyComponents.ConnectedComponent;
import com.symmetrylabs.util.StripsTopologyComponents;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.parameter.BooleanParameter.Mode;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import processing.event.KeyEvent;

public class Snake<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = MagicLeapShow.SHOW_NAME;

    enum GameState {
        PLAYING,
        GAME_OVER,
        NO_TOPOLOGY
    }

    private Junction start;
    private Bundle current;
    private Junction target;
    private Junction goal;
    private int progress;

    private Dir nextDir;
    private Sign currentSign;
    private Sign nextSign;

    private final StripsTopology topo;

    private GameState state;

    private double timeSinceTick;
    private int score;

    private final List<Junction> validJunctions;

    private final BooleanParameter reset =
        new BooleanParameter("reset", false)
        .setMode(BooleanParameter.Mode.MOMENTARY);

    private final CompoundParameter stepRate =
        new CompoundParameter("stepRate", 100, 5, 500);

    public Snake(LX lx) {
        super(lx);

        topo = model.getTopology();
        if (topo == null) {
            SLStudio.setWarning("Snake", "no topology on model");
            state = GameState.NO_TOPOLOGY;
            validJunctions = null;
            return;
        }

        List<ConnectedComponent> components =
            new StripsTopologyComponents(topo).getComponents();

        int maxSize = 0;
        ConnectedComponent maxCC = null;
        for (ConnectedComponent cc : components) {
            if (cc.junctions.size() > maxSize) {
                maxSize = cc.junctions.size();
                maxCC = cc;
            }
        }
        validJunctions = new ArrayList<>(maxCC.junctions);

        addParameter(reset);
        addParameter(stepRate);
        reset();
    }

    private void reset() {
        start = randomJunction();
        goal = randomJunction();
        target = null;
        current = null;
        currentSign = null;
        progress = 0;
        nextDir = null;
        nextSign = null;
        state = GameState.PLAYING;
        timeSinceTick = 0;
        score = 0;
    }

    private Junction randomJunction() {
        Random random = new Random();
        return validJunctions.get(random.nextInt(validJunctions.size()));
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == reset && reset.getValueb()) {
            reset();
        }
    }

    @Override
    public void onInactive() {
        super.onInactive();
        SLStudio.setWarning("Snake", null);
    }

    private void paintJunction(Junction j, int[] colors) {
        for (Sign s : Sign.values()) {
            for (Dir d : Dir.values()) {
                Bundle b = j.get(d, s);
                if (b == null) {
                    continue;
                }
                for (int i = 0; i < b.strips.length; i++) {
                    Strip strip = model.getStripByIndex(b.strips[i]);
                    int endIndex = s == b.stripSign[i] ? 0 : strip.size - 1;
                    colors[strip.points[endIndex].index] = 0xFFFFFFFF;
                }
            }
        }
    }

    private void tickGame(double elapsedMs, int[] colors) {
        timeSinceTick += elapsedMs;

        if (progress == 0) {
            paintJunction(start, colors);
            if (nextDir != null && nextSign != null) {
                currentSign = nextSign;
                current = start.get(nextDir, nextSign);
                if (current == null) {
                    state = GameState.GAME_OVER;
                } else {
                    target = current.getOpposite(start);
                }
                if (timeSinceTick > stepRate.getValue()) {
                    progress++;
                    timeSinceTick = 0;
                }
            }
        } else if (current != null && progress == current.getStripPointCount()) {
            paintJunction(target, colors);
            start = target;
            current = null;
            target = null;
            progress = 0;
            if (start == goal) {
                score++;
                goal = randomJunction();
            }
        } else {
            for (int i = 0; i < current.strips.length; i++) {
                Strip strip = model.getStripByIndex(current.strips[i]);
                int pointIndex = current.stripSign[i] == currentSign ? progress : strip.size - progress - 1;
                colors[strip.points[pointIndex].index] = 0xFFFFFFFF;
            }
            if (timeSinceTick > stepRate.getValue()) {
                progress++;
                timeSinceTick = 0;
            }
        }

        for (LXVector v : getVectors()) {
            float d = v.dist(goal.loc);
            if (d < 12) {
                colors[v.index] = 0xFF00FF00;
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s / sco %d / sta %s / tar %s / cur %s / sig %s / ndi %s / nsi %s / pro %02d / tic %05d",
            state,
            score,
            start == null ? "null" : "ok",
            target == null ? "null" : "ok",
            current == null ? "null" : Integer.toString(current.index),
            currentSign,
            nextDir,
            nextSign,
            progress,
            (int) timeSinceTick
        );
    }

    @Override
    public String getCaption() {
        return toString();
    }

    @Override
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        int[] colors = (int[]) getArray(PolyBuffer.Space.RGB8);
        Arrays.fill(colors, 0);
        switch (state) {
        case PLAYING:
            tickGame(elapsedMs, colors);
            break;
        case GAME_OVER:
            Arrays.fill(colors, 0xFFFF0000);
            break;
        case NO_TOPOLOGY:
            Arrays.fill(colors, 0xFFFFFF00);
            break;
        }
        markModified(PolyBuffer.Space.RGB8);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        switch (keyChar) {
        case 'w':
            nextDir = Dir.Y;
            nextSign = Sign.POS;
            break;
        case 'a':
            nextDir = Dir.X;
            nextSign = Sign.NEG;
            break;
        case 's':
            nextDir = Dir.Y;
            nextSign = Sign.NEG;
            break;
        case 'd':
            nextDir = Dir.X;
            nextSign = Sign.POS;
            break;
        case 'f':
            nextDir = Dir.Z;
            nextSign = Sign.NEG;
            break;
        case 'r':
            nextDir = Dir.Z;
            nextSign = Sign.POS;
            break;
        }
        if (current != null && current.dir == nextDir && currentSign != nextSign) {
            currentSign = nextSign;
            progress = current.getStripPointCount() - progress;
            Junction t = start;
            start = target;
            target = t;
        }
    }
}
