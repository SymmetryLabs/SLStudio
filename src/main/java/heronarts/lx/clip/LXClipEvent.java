/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.clip;

import java.util.Comparator;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXSerializable;

public abstract class LXClipEvent implements Comparator<LXClipEvent>, LXSerializable {

    protected final LXClipLane lane;
    protected final LXComponent component;
    protected double cursor;

    LXClipEvent(LXClipLane lane) {
        this(lane, lane.clip.cursor, null);
    }

    LXClipEvent(LXClipLane lane, LXComponent component) {
        this(lane, lane.clip.cursor, component);
    }

    LXClipEvent(LXClipLane lane, double cursor) {
        this(lane, cursor, null);
    }

    LXClipEvent(LXClipLane lane, double cursor, LXComponent component) {
        this.lane = lane;
        this.cursor = cursor;
        this.component = component;
    }

    public double getCursor() {
        return this.cursor;
    }

    LXClipEvent setCursor(double cursor) {
        this.cursor = cursor;
        return this;
    }

    public double getBasis() {
        return this.cursor / this.lane.clip.length.getValue();
    }

    @Override
    public int compare(LXClipEvent arg0, LXClipEvent arg1) {
        if (arg0.cursor < arg1.cursor) {
            return -1;
        } else if (arg0.cursor > arg1.cursor) {
            return 1;
        }
        return 0;
    }

    public abstract void execute();

    protected static final String KEY_CURSOR = "cursor";

    @Override
    public void load(LX lx, JsonObject obj) {
        if (obj.has(KEY_CURSOR)) {
            this.cursor = obj.get(KEY_CURSOR).getAsDouble();
        }
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        obj.addProperty(KEY_CURSOR, this.cursor);
    }
}
