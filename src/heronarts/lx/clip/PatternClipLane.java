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

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;

public class PatternClipLane extends LXClipLane {
    PatternClipLane(LXClip clip) {
        super(clip);
    }

    @Override
    public String getLabel() {
        return "Pattern";
    }

    PatternClipLane addEvent(PatternClipEvent event) {
        super.appendEvent(event);
        return this;
    }

    @Override
    protected LXClipEvent loadEvent(LX lx, JsonObject eventObj) {
        LXChannel channel = (LXChannel) this.clip.bus;
        LXPattern pattern = channel.patterns.get(eventObj.get(PatternClipEvent.KEY_PATTERN_INDEX).getAsInt());
        return new PatternClipEvent(this, channel, pattern);
    }
}
