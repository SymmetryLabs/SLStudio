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
import heronarts.lx.LXChannel;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter.Mode;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import heronarts.lx.osc.LXOscEngine;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import heronarts.lx.osc.OscMessage;
import processing.event.KeyEvent;
import processing.core.PConstants;

public class Snake<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = MagicLeapShow.SHOW_NAME;

    private static final int TAIL_PIXELS_PER_POINT = 10;
    private static final double SPARKLE_TIME = 1200;

    private LXOscEngine.Transmitter oscTransmitter = null;

    enum GameState {
        PLAYING,
        GAME_OVER,
        NO_TOPOLOGY
    }

    private static class TailBit {
        final int[] pointIndexes;

        TailBit(int size) {
            pointIndexes = new int[size];
            Arrays.fill(pointIndexes, -1);
        }

        boolean matches(TailBit other) {
            for (int pi : pointIndexes) {
                for (int oi : other.pointIndexes) {
                    if (pi == oi) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private Junction start;
    private Bundle current;
    private Junction target;
    private Junction goal;
    private int progress;
    private int player;

    private Dir nextDir;
    private Sign currentSign;
    private Sign nextSign;

    private final StripsTopology topo;

    private GameState state;

    private double t;
    private double timeSinceTick;
    private int score;

    private Junction sparkleAt;
    private double sparkleAge;

    private LXVector diedAt;
    private double diedAge;

    private Deque<TailBit> tail;

    private final List<Junction> validJunctions;

    private final Random random = new Random();

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

        try {
            this.oscTransmitter = lx.engine.osc.transmitter("localhost", 3344);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        lx.engine.getChannel(0).enabled.addListener(parameter -> {
            // getChannel() returns null?
            if (getChannel().enabled.isOn()) {
                reset();
                System.out.println("Reset Snake Game");
            }
        });

    }

    private void endGame() {
        reset();
        if (oscTransmitter != null) {
            try {
                OscMessage message1 = new OscMessage("/lx/channel/Snake/enabled").add(0);
                oscTransmitter.send(message1);

                OscMessage message2 = new OscMessage("/lx/channel/Cycle/enabled").add(1);
                oscTransmitter.send(message2);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        t = 0;
        tail = new LinkedList<>();
        sparkleAt = null;
        sparkleAge = 0;
        player = 0;
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

    private TailBit paintJunction(Junction j, int[] colors) {
        ArrayList<Integer> points = new ArrayList<>();
        for (Sign s : Sign.values()) {
            for (Dir d : Dir.values()) {
                Bundle b = j.get(d, s);
                if (b == null) {
                    continue;
                }
                for (int i = 0; i < b.strips.length; i++) {
                    Strip strip = model.getStripByIndex(b.strips[i]);
                    int endIndex = s == b.stripSign[i] ? 0 : strip.size - 1;
                    int index = strip.points[endIndex].index;
                    points.add(index);
                    colors[index] = 0xFFFFFFFF;
                }
            }
        }
        TailBit tb = new TailBit(points.size());
        for (int i = 0; i < points.size(); i++) {
            tb.pointIndexes[i] = points.get(i);
        }
        return tb;
    }

    private void tickGame(double elapsedMs, int[] colors) {
        timeSinceTick += elapsedMs;
        sparkleAge += elapsedMs;

        for (TailBit tb : tail) {
            for (int i = 0; i < tb.pointIndexes.length; i++) {
                colors[tb.pointIndexes[i]] = 0xFF777777;
            }
        }

        /* draw goal and goal sparkle */
        for (LXVector v : getVectors()) {
            float d = v.dist(goal.loc);
            if (d < 6 + 1.8 * Math.sin(t / 1200)) {
                colors[v.index] = 0xFFFFFF00;
            } else if (sparkleAt != null && sparkleAge < SPARKLE_TIME) {
                float sd = v.dist(sparkleAt.loc);
                if (sd < 8 * sparkleAge / SPARKLE_TIME && random.nextFloat() < 0.35) {
                    colors[v.index] = 0xFF00FF00;
                } else if (random.nextFloat() < 0.002) {
                    colors[v.index] = 0xFF00FF00;
                }
            }
        }

        if (progress == 0) {
            if (nextDir != null && nextSign != null) {
                currentSign = nextSign;
                current = start.get(nextDir, nextSign);
                if (current == null) {
                    state = GameState.GAME_OVER;
                    diedAt = start.loc;
                    diedAge = 0;
                } else {
                    target = current.getOpposite(start);
                }
                if (timeSinceTick > stepRate.getValue()) {
                    progress++;
                    timeSinceTick = 0;
                }
            } else {
                paintJunction(start, colors);
            }
        } else if (current != null && progress == current.getStripPointCount()) {
            tail.add(paintJunction(target, colors));
            start = target;
            current = null;
            target = null;
            progress = 0;
            if (start == goal) {
                score++;
                sparkleAt = goal;
                sparkleAge = 0;
                goal = randomJunction();
            }
        } else {
            TailBit tb = new TailBit(current.strips.length);
            for (int i = 0; i < current.strips.length; i++) {
                Strip strip = model.getStripByIndex(current.strips[i]);
                int pointIndex = current.stripSign[i] == currentSign
                    ? progress : strip.size - progress - 1;
                tb.pointIndexes[i] = strip.points[pointIndex].index;
                colors[tb.pointIndexes[i]] = 0xFFFFFFFF;
            }

            if (timeSinceTick > stepRate.getValue()) {
                for (TailBit old : tail) {
                    if (old.matches(tb)) {
                        state = GameState.GAME_OVER;
                        LXPoint p = model.points[tb.pointIndexes[0]];
                        diedAt = new LXVector(p.x, p.y, p.z);
                        diedAge = 0;
                    }
                }
                tail.addFirst(tb);
                progress++;
                timeSinceTick = 0;
            }
        }
        while (tail.size() > TAIL_PIXELS_PER_POINT * score) {
            tail.removeLast();
        }
    }

    private void drawDead(double elapsedMs, int[] colors) {
        diedAge += elapsedMs;
        double limit = diedAge * 36 / 1000;
        for (LXVector v : getVectors()) {
            double dist = v.dist(diedAt);
            boolean red =
                dist < limit ||
                (dist < 1.2 * limit && random.nextFloat() < 0.5) ||
                (dist < 2 * limit && random.nextFloat() < 0.1) ||
                (random.nextFloat() < 0.002);
            colors[v.index] = red ? 0xFFFF0000 : 0xFF000000;
        }
        if (diedAge > 4200) {
            endGame();
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
        t += elapsedMs;

        int[] colors = (int[]) getArray(PolyBuffer.Space.RGB8);
        Arrays.fill(colors, 0);
        switch (state) {
        case PLAYING:
            tickGame(elapsedMs, colors);
            break;
        case GAME_OVER:
            drawDead(elapsedMs, colors);
            break;
        case NO_TOPOLOGY:
            Arrays.fill(colors, 0xFFFFFF00);
            break;
        }
        markModified(PolyBuffer.Space.RGB8);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (state == GameState.GAME_OVER) {
            if (keyCode == PConstants.SHIFT) {
                reset();
            }
            return;
        }
        if (player == 0) {
            switch (keyChar) {
            case 'w':
            case 'W':
            case 'a':
            case 's':
            case 'S':
            case 'd':
            case 'f':
            case 'r':
                player = 1;
                break;
            case 'i':
            case 'I':
            case 'j':
            case 'k':
            case 'K':
            case 'l':
            case 'p':
            case ';':
                player = 2;
                break;
            }
        }
        if (player == 1) {
            switch (keyChar) {
                case 'w':
                    nextDir = Dir.Y;
                    nextSign = Sign.POS;
                    break;
                case 's':
                    nextDir = Dir.Y;
                    nextSign = Sign.NEG;
                    break;
                case 'a':
                    nextDir = Dir.X;
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
        } else {
            switch (keyChar) {
            case 'i':
                nextDir = Dir.Y;
                nextSign = Sign.POS;
                break;
                case 'k':
                    nextDir = Dir.Y;
                    nextSign = Sign.NEG;
                    break;
            case 'j':
                nextDir = Dir.X;
                nextSign = Sign.NEG;
                break;
            case 'l':
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
        }
        if (current != null && current.dir == nextDir && currentSign != nextSign) {
            if (score == 0) {
                currentSign = nextSign;
                progress = current.getStripPointCount() - progress;
                Junction t = start;
                start = target;
                target = t;
            } else {
                /* don't let people reverse back onto themselves */
                nextSign = currentSign;
            }
        }
    }
}
