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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LXClipLane {

    protected final LXClip clip;

    protected final List<LXClipEvent> internalEvents = new ArrayList<LXClipEvent>();
    public final List<LXClipEvent> events = Collections.unmodifiableList(this.internalEvents);

    protected LXClipLane(LXClip clip) {
        this.clip = clip;
    }

    protected LXClipLane addEvent(LXClipEvent event) {
        // TODO(mcslee): insertion sort?
        this.internalEvents.add(event);
        return this;
    }

    public abstract String getLabel();

    void executeEvents(double from, double to) {
        for (LXClipEvent event : this.internalEvents) {
            if (from <= event.cursor && to > event.cursor) {
                event.execute();
            }
        }
    }

    void clear() {
        this.internalEvents.clear();
    }

}
