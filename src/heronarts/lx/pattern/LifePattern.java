/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.transition.IrisTransition;

import java.util.HashMap;

public class LifePattern extends LXPattern {

    private enum CellState {
        DEAD, BIRTHING, ALIVE, DYING
    };

    private final SawLFO sPos;
    private final SinLFO hCenter;

    private CellState[] state;
    private CellState[] newState;
    private int spawnCounter = 0;

    private final HashMap<String, Integer> stateCount = new HashMap<String, Integer>();

    public LifePattern(LX lx) {
        super(lx);
        this.state = new CellState[lx.total];
        this.newState = new CellState[lx.total];
        this.addModulator(
                this.hCenter = new SinLFO(lx.width * .25, lx.width * .75,
                        lx.width * 1000)).trigger();
        this.addModulator(
                this.sPos = new SawLFO(-Math.max(lx.height, lx.width), lx.height
                        + lx.width, lx.width * 300)).trigger();
        this.spawn();
        this.setTransition(new IrisTransition(lx));
    }

    private boolean isLiveState(CellState state) {
        return (state == CellState.BIRTHING) || (state == CellState.ALIVE);
    }

    private void respawn() {
        boolean anyAlive = false;
        for (int i = 0; i < this.state.length; ++i) {
            if (isLiveState(this.state[i])) {
                anyAlive = true;
                this.state[i] = CellState.DYING;
            }
        }
        this.spawnCounter = anyAlive ? 2 : 1;
    }

    private void spawn() {
        for (int i = 0; i < this.state.length; ++i) {
            this.state[i] = (LXUtils.random(0, 100) > 70) ? CellState.BIRTHING
                    : CellState.DEAD;
        }
    }

    private int neighborsAlive(int i) {
        int x = i % this.lx.width;
        int y = i / this.lx.width;
        return isAlive(x - 1, y - 1) + isAlive(x, y - 1) + isAlive(x + 1, y - 1)
                + isAlive(x - 1, y) + isAlive(x + 1, y) + isAlive(x - 1, y + 1)
                + isAlive(x, y + 1) + isAlive(x + 1, y + 1);
    }

    private int isAlive(int x, int y) {
        if (x < 0 || x >= lx.width) {
            return 0;
        }
        if (y < 0 || y >= lx.height) {
            return 0;
        }
        int idx = x + y * lx.width;
        if (this.isLiveState(this.state[idx])) {
            return 1;
        }
        return 0;
    }

    @SuppressWarnings("fallthrough")
    private void transition() {
        for (int i = 0; i < state.length; ++i) {
            int nA = neighborsAlive(i);
            switch (state[i]) {
            case DEAD:
            case DYING:
                this.newState[i] = (nA == 3) ? CellState.BIRTHING : CellState.DEAD;
                break;
            case ALIVE:
            case BIRTHING:
                this.newState[i] = (nA == 2 || nA == 3) ? CellState.ALIVE
                        : CellState.DYING;
                break;
            }
        }
        CellState[] tmp = this.state;
        this.state = this.newState;
        this.newState = tmp;

        String stateSerial = "";
        for (int i = 0; i < this.state.length; ++i) {
            stateSerial += this.isLiveState(this.state[i]) ? "1" : "0";
        }
        Integer count = 0;
        if (this.stateCount.containsKey(stateSerial)) {
            count = this.stateCount.get(stateSerial);
        }
        if (count.equals(3)) {
            this.stateCount.clear();
            this.respawn();
        } else {
            this.stateCount.put(stateSerial, count + 1);
        }
    }

    @Override
    public void run(double deltaMs) {
        if (this.lx.tempo.beat()) {
            if ((this.spawnCounter > 0) && (--this.spawnCounter == 0)) {
                this.spawn();
            } else {
                this.transition();
            }
        }
        for (int i = 0; i < lx.total; ++i) {
            double b = 0;
            switch (this.state[i]) {
            case ALIVE:
                b = 100;
                break;
            case BIRTHING:
                b = this.lx.tempo.ramp() * 100;
                break;
            case DEAD:
                b = 0;
                break;
            case DYING:
                b = 100 - this.lx.tempo.ramp() * 100;
                break;
            }
            this.colors[i] = LXColor.hsb(
                    (this.lx.getBaseHue() + lx.row(i) * 2. + Math.abs(lx.column(i)
                            - this.hCenter.getValue()) * 0.4) % 360,
                    Math.min(
                            100,
                            30. + Math.abs(i / this.lx.width - this.sPos.getValue() + i
                                    % this.lx.width) * 10.), b);
        }
    }
}
