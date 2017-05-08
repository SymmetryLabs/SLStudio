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

public class LXChannelClip extends LXClip implements LXChannel.Listener {

    public final PatternClipLane patternLane = new PatternClipLane(this);

    public final LXChannel channel;

    public LXChannelClip(LX lx, LXChannel channel, int index) {
        super(lx, channel, index);
        this.channel = channel;
        this.mutableLanes.add(this.patternLane);

        channel.addListener(this);
        channel.fader.addListener(this.parameterRecorder);
        channel.enabled.addListener(this.parameterRecorder);

        for (LXPattern pattern : channel.patterns) {
            registerComponent(pattern);
        }
    }

    @Override
    protected void onStartRecording() {
        this.patternLane.addEvent(new PatternClipEvent(this.patternLane, this.channel, this.channel.getActivePattern()));
    }

    @Override
    public void indexChanged(LXChannel channel) {}

    @Override
    public void patternAdded(LXChannel channel, LXPattern pattern) {
        registerComponent(pattern);
    }

    @Override
    public void patternRemoved(LXChannel channel, LXPattern pattern) {
        unregisterComponent(pattern);
    }

    @Override
    public void patternMoved(LXChannel channel, LXPattern pattern) {
    }

    @Override
    public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
        if (isRunning() && this.bus.arm.isOn()) {
            this.patternLane.addEvent(new PatternClipEvent(this.patternLane, channel, nextPattern));
        }
    }

    @Override
    public void patternDidChange(LXChannel channel, LXPattern pattern) {

    }

    @Override
    protected void loadLane(LX lx, String laneType, JsonObject laneObj) {
        if (laneType.equals(LXClipLane.VALUE_LANE_TYPE_PATTERN)) {
            this.patternLane.load(lx, laneObj);
        } else {
            super.loadLane(lx, laneType, laneObj);
        }
    }
}
