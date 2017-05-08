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
import heronarts.lx.LXChannel;
import heronarts.lx.LXLoopTask;
import heronarts.lx.LXUtils;
import heronarts.lx.clip.LXClip;
import heronarts.lx.clip.LXClipEvent;
import heronarts.lx.clip.LXClipLane;
import heronarts.lx.clip.ParameterClipEvent;
import heronarts.lx.clip.ParameterClipLane;
import heronarts.lx.clip.PatternClipEvent;
import heronarts.lx.clip.PatternClipLane;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIClipView extends UI2dContainer implements LXClip.Listener, LXParameterListener {

    public final static int HEIGHT = 124;
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
        if (this.envelope.lane == lane) {
            this.envelope.setLane(null);
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

    private class UIClipEnvelope extends UI2dContainer implements UIFocus {

        private int cursorX = 0;

        private LXClipLane lane;
        private LaneImpl impl;

        private final ParameterLaneImpl parameterLane = new ParameterLaneImpl();
        private final PatternLaneImpl patternLane = new PatternLaneImpl();

        private double selectionStart = 0;
        private double selectionEnd = 0;
        private boolean hasSelection = false;

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

        private LXParameterListener redraw = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                redraw();
            }
        };

        void setLane(LXClipLane lane) {
            if (this.lane != lane) {
                if (this.lane != null) {
                    this.lane.onChange.removeListener(this.redraw);
                }
                this.lane = lane;
                if (this.lane != null) {
                    this.lane.onChange.addListener(this.redraw);
                    if (this.lane instanceof ParameterClipLane) {
                        this.impl = this.parameterLane;
                    } else if (this.lane instanceof PatternClipLane) {
                        this.impl = this.patternLane;
                    }
                } else {
                    this.impl = null;
                }
                if (this.impl != null) {
                    this.impl.initialize();
                }
                clearSelection();
                redraw();
            }
        }

        private void clearSelection() {
            if (this.hasSelection) {
                this.hasSelection = false;
                redraw();
            }
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            if (clip != null) {
                // Highlight background selection
                if (this.lane != null && this.hasSelection) {
                    pg.noStroke();
                    pg.fill(ui.theme.getSelectionColor());
                    int startX = (int) (Math.min(this.selectionStart, this.selectionEnd) * (this.width-1));
                    int endX = (int) (Math.max(this.selectionStart, this.selectionEnd) * (this.width-1));
                    pg.rect(startX, 0, endX-startX, this.height);
                }

                // Draw position cursor
                pg.stroke(ui.theme.getCursorColor());
                pg.line(this.cursorX, 0, this.cursorX, this.height-1);

                if (this.impl != null) {
                    this.impl.onDraw(ui, pg);
                }
            }
        }

        @Override
        protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
            clearSelection();
            this.selectionStart = this.selectionEnd = LXUtils.constrain(mx / (this.width-1), 0, 1);
            if (this.impl != null) {
                this.impl.onMousePressed(mouseEvent, mx, my);
            }
        }

        @Override
        protected void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {
            clearSelection();
            if (this.impl != null) {
                this.impl.onMouseClicked(mouseEvent, mx, my);
            }
        }

        @Override
        protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
            boolean implHandled = false;
            if (this.impl != null) {
                implHandled = this.impl.onMouseDragged(mouseEvent, mx, my, dx, dy);
            }
            if (!implHandled) {
                this.hasSelection = true;
                this.selectionEnd = LXUtils.constrain(mx / (this.width-1), 0, 1);
                redraw();
            }
        }

        @Override
        protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
            if (this.impl != null) {
                this.impl.onKeyPressed(keyEvent, keyChar, keyCode);
            }
            if (!keyEventConsumed() && this.hasSelection) {
                consumeKeyEvent();
                double clearBegin = Math.min(this.selectionStart, this.selectionEnd);
                double clearEnd = Math.max(this.selectionStart, this.selectionEnd);
                this.lane.clearSelection(clearBegin, clearEnd);
            }
        }

        private abstract class LaneImpl {
            protected abstract void initialize();
            protected abstract void onDraw(UI ui, PGraphics pg);
            protected abstract void onMousePressed(MouseEvent mouseEvent, float mx, float my);
            protected abstract void onMouseClicked(MouseEvent mouseEvent, float mx, float my);
            protected abstract boolean onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy);
            protected abstract void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode);
        }

        private class ParameterLaneImpl extends LaneImpl {
            private LXClipEvent editEvent;

            @Override
            protected void initialize() {
                this.editEvent = null;
            }

            @Override
            protected void onDraw(UI ui, PGraphics pg) {
                int startX = -1;
                int startY = -1;

                // Lines
                pg.stroke(ui.theme.getPrimaryColor());
                for (LXClipEvent event : lane.events) {
                    ParameterClipEvent parameterEvent = (ParameterClipEvent) event;
                    double eventBasis = parameterEvent.getBasis();
                    double eventNormalized = parameterEvent.getNormalized();
                    int endX = (int) Math.round(eventBasis * (width-1));
                    int endY = (int) Math.round(height - 1 - eventNormalized * (height-1));
                    if (startX >= 0) {
                        pg.line(startX, startY, endX, endY);
                    } else {
                        pg.line(0, endY, endX, endY);
                    }
                    startX = endX;
                    startY = endY;
                }
                if (startX < width-1 && startY >= 0) {
                    pg.line(startX, startY, width-1, startY);
                }

                // Dots
                pg.noStroke();
                for (LXClipEvent event : lane.events) {
                    ParameterClipEvent parameterEvent = (ParameterClipEvent) event;
                    double eventBasis = parameterEvent.getBasis();
                    double eventNormalized = parameterEvent.getNormalized();
                    int endX = (int) Math.round(eventBasis * (width-1));
                    int endY = (int) Math.round(height - 1 - eventNormalized * (height-1));
                    float rectWidth = 5;
                    float rectHeight = 5;
                    float rectX = endX - 2;
                    float rectY = endY - 2;
                    if (rectY < 0) {
                        rectHeight += rectY;
                        rectY = 0;
                    } else if (rectY > height-5) {
                        rectHeight -= rectY - (height-5);
                    }
                    if (rectX < 0) {
                        rectWidth += rectX;
                        rectX = 0;
                    } else if (rectX > width-5) {
                        rectWidth -= rectX - (width-5);
                    }

                    if (this.editEvent == event) {
                        pg.fill(ui.theme.getRecordingColor());
                    } else {
                        pg.fill(ui.theme.getPrimaryColor());
                    }
                    pg.rect(rectX, rectY, rectWidth, rectHeight);
                }

            }

            private final static int EVENT_SELECTION_THRESHOLD = 6;

            @Override
            protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                LXClipEvent edit = null;
                for (LXClipEvent event : lane.events) {
                    ParameterClipEvent parameterEvent = (ParameterClipEvent) event;
                    double eventBasis = parameterEvent.getBasis();
                    double eventNormalized = parameterEvent.getNormalized();
                    int endX = (int) (eventBasis * (width-1));
                    int endY = (int) (height - 1 - eventNormalized * (height-1));
                    if (Math.abs(mx - endX) < EVENT_SELECTION_THRESHOLD && Math.abs(my - endY) < EVENT_SELECTION_THRESHOLD) {
                        edit = parameterEvent;
                        break;
                    }
                }
                if (edit != this.editEvent) {
                    this.editEvent = edit;
                    redraw();
                }
            }

            @Override
            protected void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {
                if (mouseEvent.getCount() == 2) {
                    double basis = mx / (width - 1);
                    double normalized = 1. - my / (height-1);
                    ((ParameterClipLane) lane).insertEvent(basis, normalized);
                }
            }


            @Override
            protected boolean onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
                if (this.editEvent != null) {
                    lane.moveEvent(this.editEvent, mx / (width-1));
                    ((ParameterClipEvent) this.editEvent).setNormalized(1. - my / (height-1));
                    return true;
                }
                return false;
            }

            @Override
            protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                if (this.editEvent != null && keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    consumeKeyEvent();
                    LXClipEvent edit = this.editEvent;
                    this.editEvent = null;
                    lane.removeEvent(edit);
                }
            }

        }

        private class PatternLaneImpl extends LaneImpl {

            private PatternClipEvent selectedEvent = null;

            @Override
            protected void initialize() {
                this.selectedEvent = null;
            }

            @Override
            protected void onDraw(UI ui, PGraphics pg) {
                LXChannel channel = (LXChannel) lane.clip.bus;
                int numPatterns = channel.patterns.size();
                int patternRowHeight = (int) Math.floor(height / numPatterns);
                for (int i = 1; i < numPatterns; ++i) {
                    pg.stroke(ui.theme.getSelectionColor());
                    pg.line(1, i * patternRowHeight, width-2, i * patternRowHeight);
                }

                int startX = 0;
                PatternClipEvent lastPattern = null;
                for (LXClipEvent event : lane.events) {
                    int endX = (int) Math.round(event.getBasis() * (width-1));
                    if (lastPattern != null) {
                        drawPattern(ui, pg, startX, endX, lastPattern, patternRowHeight);
                    }
                    lastPattern = (PatternClipEvent) event;
                    startX = endX;
                }
                if (lastPattern != null) {
                    drawPattern(ui, pg, startX, (int) (width-1), lastPattern, patternRowHeight);
                }
            }

            private void drawPattern(UI ui, PGraphics pg, int fromX, int toX, PatternClipEvent patternEvent, int patternRowHeight) {
                int y = patternEvent.pattern.getIndex() * patternRowHeight;
                if (patternEvent == this.selectedEvent) {
                    pg.fill(ui.theme.getPrimaryColor());
                } else {
                    pg.noFill();
                }
                pg.stroke(ui.theme.getPrimaryColor());
                pg.rect(fromX, y, toX - fromX, patternRowHeight-1, 4);
                pg.fill((patternEvent == this.selectedEvent) ? UI.WHITE : ui.theme.getPrimaryColor());
                pg.textAlign(PConstants.CENTER, PConstants.CENTER);
                pg.textFont(ui.theme.getControlFont());
                int textPos = fromX + (toX - fromX) / 2;
                String text = clipTextToWidth(
                    pg,
                    patternEvent.pattern.getLabel(),
                    2 * Math.min(textPos, width - textPos)
                );
                pg.text(text, textPos, y + patternRowHeight / 2);
            }

            @Override
            protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                LXChannel channel = (LXChannel) lane.clip.bus;
                int numPatterns = channel.patterns.size();
                int patternRowHeight = (int) Math.floor(height / numPatterns);
                int startX = 0;
                PatternClipEvent selected = null;
                PatternClipEvent lastPattern = null;
                for (LXClipEvent event : lane.events) {
                    int endX = (int) Math.round(event.getBasis() * (width-1));
                    if (lastPattern != null) {
                        if (startX <= mx && mx < endX) {
                            int y = lastPattern.pattern.getIndex() * patternRowHeight;
                            if (y <= my && my < (y + patternRowHeight)) {
                                selected = lastPattern;
                                break;
                            }
                        }
                    }
                    lastPattern = (PatternClipEvent) event;
                    startX = endX;
                }
                if (selected == null && lastPattern != null) {
                    int y = lastPattern.pattern.getIndex() * patternRowHeight;
                    if (y <= my && my < (y + patternRowHeight)) {
                        selected = lastPattern;
                    }
                }
                if (this.selectedEvent != selected) {
                    this.selectedEvent = selected;
                    redraw();
                }
            }

            @Override
            protected void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {

            }

            @Override
            protected boolean onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
                if (this.selectedEvent != null) {
                    lane.moveEvent(this.selectedEvent, this.selectedEvent.getBasis() + dx / width);
                    return true;
                }
                return false;
            }

            @Override
            protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                if (this.selectedEvent != null && keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    lane.removeEvent(this.selectedEvent);
                }
            }

        }
    }
}
