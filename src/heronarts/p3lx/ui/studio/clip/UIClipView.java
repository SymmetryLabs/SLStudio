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

package heronarts.p3lx.ui.studio.clip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.LXUtils;
import heronarts.lx.clip.LXClip;
import heronarts.lx.clip.LXClipEvent;
import heronarts.lx.clip.LXClipLane;
import heronarts.lx.clip.ParameterClipEvent;
import heronarts.lx.clip.ParameterClipLane;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UIClipView extends UI2dContainer implements LXClip.Listener, LXParameterListener {

    private final static int HEIGHT = 124;
    private final static int PADDING = 4;
    private final static int LIST_WIDTH = 140;

    private LXClip clip;

    private final UIClipInfo info;
    private final UIItemList.ScrollList laneList;
    private final UIClipEnvelope envelope;

    private final Map<LXClipLane, LaneItem> laneMap =
        new HashMap<LXClipLane, LaneItem>();

    private class LaneItem extends UIItemList.AbstractItem {

        private final LXClipLane lane;

        LaneItem(LXClipLane lane) {
            this.lane = lane;
        }

        @Override
        public String getLabel() {
            return this.lane.getLabel();
        }

        @Override
        public void onFocus() {
            envelope.setLane(this.lane);
        }

        @Override
        public void onDelete() {
            if (this.lane instanceof ParameterClipLane) {
                clip.removeParameterLane((ParameterClipLane) this.lane);
            }
        }
    }

    public UIClipView(UI ui, final LX lx, float x, float y, float w) {
        super(x, y, w, HEIGHT);

        this.info = (UIClipInfo)
            new UIClipInfo(ui)
            .addToContainer(this);

        this.laneList = (UIItemList.ScrollList)
            new UIItemList.ScrollList(ui, UIClipInfo.WIDTH + PADDING, 0, LIST_WIDTH, HEIGHT)
            .addToContainer(this);

        this.envelope = (UIClipEnvelope)
            new UIClipEnvelope(ui, getContentWidth() - (UIClipInfo.WIDTH + LIST_WIDTH + 2*PADDING))
            .addToContainer(this);

        lx.engine.focusedClip.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter parameter) {
                setClip(lx.engine.focusedClip.getClip());
            }
        });
    }

    @Override
    protected void onResize() {
        this.envelope.setWidth(getContentWidth() - (UIClipInfo.WIDTH + LIST_WIDTH + 2*PADDING));
    }

    private void setClip(LXClip clip) {
        if (this.clip != clip) {
            if (this.clip != null) {
                this.clip.length.removeListener(this);
                this.clip.removeListener(this);
            }
            this.clip = clip;
            this.envelope.setLane(null);
            List<LaneItem> laneItems = new ArrayList<LaneItem>();
            this.laneMap.clear();
            if (this.clip != null) {
                this.info.clipName.setParameter(this.clip.label);
                this.info.clipName.setEnabled(true);
                this.info.clipLength.setLabel(this.clip.length.getUnits().format(this.clip.length.getValue()));
                this.clip.length.addListener(this);
                this.info.clipLoop.setParameter(this.clip.loop);
                this.info.clipLoop.setEnabled(true);
                for (LXClipLane lane : this.clip.lanes) {
                    LaneItem laneItem = new LaneItem(lane);
                    this.laneMap.put(lane, laneItem);
                    laneItems.add(laneItem);
                }
                this.clip.addListener(this);
            } else {
                this.info.clipName.setParameter(null);
                this.info.clipName.setEnabled(false);
                this.info.clipLength.setLabel("");
                this.info.clipLoop.setEnabled(false);
                this.info.clipLoop.setActive(false);
                this.info.clipLoop.setParameter((BooleanParameter) null);
            }
            this.laneList.setItems(laneItems);
        }
    }

    public void parameterLaneAdded(LXClip clip, ParameterClipLane lane) {
        LaneItem laneItem = new LaneItem(lane);
        this.laneMap.put(lane, laneItem);
        this.laneList.addItem(laneItem);
    }

    public void parameterLaneRemoved(LXClip clip, ParameterClipLane lane) {
        LaneItem laneItem = this.laneMap.remove(lane);
        if (laneItem != null) {
            this.laneList.removeItem(laneItem);
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (this.clip != null) {
            if (p == this.clip.length) {
                this.info.clipLength.setLabel(this.clip.length.getUnits().format(this.clip.length.getValue()));
            }
        }
    }

    private class UIClipInfo extends UI2dContainer {

        private final static int PADDING = 4;
        private final static int VALUE_WIDTH = 64;
        private final static int WIDTH = VALUE_WIDTH + 2*PADDING;

        private final static int ROW_HEIGHT = 16;
        private final static int ROW_SPACING = 4;

        private final UITextBox clipName;
        private final UILabel clipLength;
        private final UIButton clipLoop;

        UIClipInfo(UI ui) {
            super(0, 0, WIDTH, HEIGHT);
            setBackgroundColor(ui.theme.getDarkBackgroundColor());
            setBorderRounding(4);
            setPadding(2, PADDING, 2, PADDING);
            setMinHeight(HEIGHT);
            setLayout(UI2dContainer.Layout.VERTICAL);
            setChildMargin(ROW_SPACING);

            new UILabel(PADDING, 0, VALUE_WIDTH, ROW_HEIGHT)
            .setLabel("Clip")
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);

            this.clipName = (UITextBox) new UITextBox(PADDING, 0, VALUE_WIDTH, ROW_HEIGHT)
            .setEnabled(false)
            .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
            .addToContainer(this);

            new UILabel(PADDING, 0, VALUE_WIDTH, ROW_HEIGHT)
            .setLabel("Length")
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);

            this.clipLength = (UILabel) new UILabel(PADDING, 0, VALUE_WIDTH, ROW_HEIGHT)
            .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
            .setBackgroundColor(ui.theme.getControlBackgroundColor())
            .setBorderColor(ui.theme.getControlBorderColor())
            .setFont(ui.theme.getControlFont())
            .addToContainer(this);

            this.clipLoop = (UIButton) new UIButton(PADDING, 0, VALUE_WIDTH, ROW_HEIGHT)
            .setLabel("Loop")
            .setEnabled(false)
            .addToContainer(this);
        }

    }

    private class UIClipEnvelope extends UI2dContainer {

        private int cursorX = 0;

        private LXClipLane lane;

        UIClipEnvelope(UI ui, float w) {
            super(UIClipInfo.WIDTH + LIST_WIDTH + 2*PADDING, 0, w, HEIGHT);
            setBackgroundColor(ui.theme.getDarkBackgroundColor());

            addLoopTask(new LXLoopTask() {
                @Override
                public void loop(double deltaMs) {
                    if (clip != null) {
                        int cx = (int) Math.round(clip.getBasis() * (width-1));
                        if (cx != cursorX) {
                            cursorX = cx;
                            redraw();
                        }
                    }
                }
            });
        }

        void setLane(LXClipLane lane) {
            if (this.lane != lane) {
                this.lane = lane;
                redraw();
            }
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            if (clip != null) {
                pg.stroke(ui.theme.getCursorColor());
                pg.line(this.cursorX, 0, this.cursorX, this.height-1);
                if (this.lane != null && this.lane instanceof ParameterClipLane) {
                    int startX = -1;
                    int startY = -1;
                    for (LXClipEvent event : this.lane.events) {
                        ParameterClipEvent parameterEvent = (ParameterClipEvent) event;
                        double eventBasis = parameterEvent.getBasis();
                        double eventNormalized = parameterEvent.getNormalized();
                        int endX = (int) Math.round(eventBasis * (this.width-1));
                        int endY = (int) Math.round(this.height - 1 - eventNormalized * (this.height-1));
                        if (startX >= 0) {
                            pg.stroke(ui.theme.getPrimaryColor());
                            pg.line(startX, startY, endX, endY);
                        } else {
                            pg.stroke(ui.theme.getCursorColor());
                            pg.line(0, endY, endX, endY);
                        }
                        float rectX = LXUtils.constrainf(endX, 0, this.width-3);
                        float rectY = LXUtils.constrainf(endY, 0, this.height-3);
                        pg.noStroke();
                        pg.fill(ui.theme.getPrimaryColor());
                        pg.rect(rectX, rectY, 3, 3);
                        startX = endX;
                        startY = endY;
                    }
                    if (startX < this.width-1 && startY >= 0) {
                        pg.stroke(ui.theme.getCursorColor());
                        pg.line(startX, startY, this.width-1, startY);
                    }
                }
            }
        }
    }
}
