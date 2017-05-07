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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import heronarts.lx.LXUtils;
import heronarts.lx.parameter.MutableParameter;

public abstract class LXClipLane {

    public final MutableParameter onChange = new MutableParameter();

    protected final LXClip clip;

    protected final List<LXClipEvent> mutableEvents = new CopyOnWriteArrayList<LXClipEvent>();
    public final List<LXClipEvent> events = Collections.unmodifiableList(this.mutableEvents);

    protected LXClipLane(LXClip clip) {
        this.clip = clip;
    }

    protected LXClipLane appendEvent(LXClipEvent event) {
        this.mutableEvents.add(event);
        this.onChange.bang();
        return this;
    }

    protected LXClipLane insertEvent(LXClipEvent event) {
        int index = 0;
        while (index < this.events.size()) {
            if (event.cursor < this.events.get(index).cursor) {
                break;
            }
            ++index;
        }
        this.mutableEvents.add(index, event);
        this.onChange.bang();
        return this;
    }

    public LXClipLane moveEvent(LXClipEvent event, double basis) {
        double clipLength = this.clip.getLength();
        double min = 0;
        double max = clipLength;
        int index = this.events.indexOf(event);
        if (index > 0) {
            min = this.events.get(index-1).cursor;
        }
        if (index < this.events.size() - 1) {
            max = this.events.get(index+1).cursor;
        }
        double newCursor = LXUtils.constrain(basis * clipLength, min, max);
        if (event.cursor != newCursor) {
            event.cursor = newCursor;
            this.onChange.bang();
        }
        return this;
    }

    public abstract String getLabel();

    void advanceCursor(double from, double to) {
        for (LXClipEvent event : this.mutableEvents) {
            if (from <= event.cursor && event.cursor < to) {
                event.execute();
            }
        }
    }

    public LXClipLane clearSelection(double fromBasis, double toBasis) {
        double from = fromBasis * this.clip.length.getValue();
        double to = toBasis * this.clip.length.getValue();
        int i = 0;
        boolean removed = false;
        while (i < this.mutableEvents.size()) {
            LXClipEvent event = this.mutableEvents.get(i);
            if (from <= event.cursor) {
                if (event.cursor > to) {
                    break;
                }
                removed = true;
                this.mutableEvents.remove(i);
            } else {
                ++i;
            }
        }
        if (removed) {
            this.onChange.bang();
        }
        return this;
    }

    public LXClipLane removeEvent(LXClipEvent event) {
        this.mutableEvents.remove(event);
        this.onChange.bang();
        return this;
    }

    void clear() {
        this.mutableEvents.clear();
        this.onChange.bang();
    }

}
