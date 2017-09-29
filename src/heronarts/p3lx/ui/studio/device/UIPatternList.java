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

package heronarts.p3lx.ui.studio.device;

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIItemList;

public class UIPatternList extends UIItemList.ScrollList {

    private final LXChannel channel;
    final Map<LXPattern, PatternItem> patternToItem = new HashMap<LXPattern, PatternItem>();

    public UIPatternList(UI ui, float x, float y, float w, float h, final LXChannel channel) {
        super(ui, x, y, w, h);
        setRenamable(true);
        setReorderable(true);
        setShowCheckboxes(true);

        this.channel = channel;
        for (LXPattern pattern : channel.getPatterns()) {
            addPattern(pattern);
        }

        // Set up control surface listener
        final LXParameterListener setControlSurfaceFocus = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                setControlSurfaceFocus(
                    channel.controlSurfaceFocusIndex.getValuei(),
                    channel.controlSurfaceFocusLength.getValuei()
                );
            }
        };
        channel.controlSurfaceFocusIndex.addListener(setControlSurfaceFocus);
        channel.controlSurfaceFocusLength.addListener(setControlSurfaceFocus);
        setControlSurfaceFocus.onParameterChanged(null);

        LXChannel.Listener lxListener = new LXChannel.AbstractListener() {

            @Override
            public void patternAdded(LXChannel channel, LXPattern pattern) {
                addPattern(pattern);
            }

            @Override
            public void patternRemoved(LXChannel channel, LXPattern pattern) {
                removePattern(pattern);
            }

            @Override
            public void patternMoved(LXChannel channel, LXPattern pattern) {
                // TODO(mcslee): should we handle? right now only happens from within the UI
            }

            @Override
            public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
                redraw();
            }

            @Override
            public void patternDidChange(LXChannel channel, LXPattern pattern) {
                redraw();
            }
        };

        channel.addListener(lxListener);

        channel.focusedPattern.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                setFocusIndex(channel.focusedPattern.getValuei());
            }
        });

        lxListener.patternDidChange(channel, channel.getActivePattern());
    }

    private void addPattern(LXPattern pattern) {
        PatternItem item = new PatternItem(pattern);
        this.patternToItem.put(pattern, item);
        addItem(item);
    }

    private void removePattern(LXPattern pattern) {
        PatternItem patternItem = this.patternToItem.remove(pattern);
        if (patternItem == null) {
            throw new IllegalStateException("Pattern removed from channel not found in map: " + pattern);
        }
        removeItem(patternItem);
    }

    class PatternItem implements UIItemList.Item {
        private final LXPattern pattern;

        PatternItem(LXPattern pattern) {
            this.pattern = pattern;
            pattern.label.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    redraw();
                }
            });
        }

        @Override
        public boolean isActive() {
            return
                (channel.getActivePattern() == this.pattern) ||
                (channel.getNextPattern() == this.pattern);
        }

        @Override
        public boolean isChecked() {
            return this.pattern.autoCycleEligible.isOn();
        }

        @Override
        public int getActiveColor(UI ui) {
            return (channel.getActivePattern() == this.pattern) ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public String getLabel() {
            return this.pattern.getLabel();
        }

        @Override
        public void onActivate() {
            channel.goPattern(this.pattern);
        }

        @Override
        public void onRename(String label) {
            this.pattern.label.setValue(label);
            // TODO(mcslee): redraw??
        }

        @Override
        public void onReorder(int index) {
            channel.movePattern(this.pattern, index);
        }

        @Override
        public void onCheck(boolean on) {
            this.pattern.autoCycleEligible.setValue(on);
        }

        @Override
        public void onDelete() {
            if (channel.getPatterns().size() > 1) {
                channel.removePattern(this.pattern);
            }
        }

        @Override
        public void onDeactivate() {
        }

        @Override
        public void onFocus() {
            channel.focusedPattern.setValue(this.pattern.getIndex());
        }
    }

}