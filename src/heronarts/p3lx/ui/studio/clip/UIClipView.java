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
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.clip.LXClip;
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

public class UIClipView extends UI2dContainer implements LXClip.TargetListener, LXParameterListener {

    private final static int HEIGHT = 104;
    private final static int PADDING = 4;
    private final static int LIST_WIDTH = 140;

    private LXClip clip;

    private final UIClipInfo info;
    private final UIItemList.ScrollList targetList;
    private final UIClipEnvelope envelope;

    private class TargetItem extends UIItemList.AbstractItem {

        private final LXParameter target;

        TargetItem(LXParameter target) {
            this.target = target;
        }

        @Override
        public String getLabel() {
            return this.target.getComponent().getLabel() + " | " + this.target.getLabel();
        }

        @Override
        public void onFocus() {
            envelope.setTarget(this.target);
        }
    }

    public UIClipView(UI ui, final LX lx, float x, float y, float w) {
        super(x, y, w, HEIGHT);

        this.info = (UIClipInfo)
            new UIClipInfo(ui)
            .addToContainer(this);

        this.targetList = (UIItemList.ScrollList)
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
                this.clip.removeTargetListener(this);
            }
            this.clip = clip;
            this.envelope.setTarget(null);
            List<TargetItem> targets = new ArrayList<TargetItem>();
            if (this.clip != null) {
                this.info.clipName.setParameter(this.clip.label);
                this.info.clipName.setEnabled(true);
                this.info.clipLength.setLabel(this.clip.length.getUnits().format(this.clip.length.getValue()));
                this.clip.length.addListener(this);
                this.info.clipLoop.setParameter(this.clip.loop);
                this.info.clipLoop.setEnabled(true);
                for (LXParameter target : this.clip.targets) {
                    targets.add(new TargetItem(target));
                }
                this.clip.addTargetListener(this);
            } else {
                this.info.clipName.setParameter(null);
                this.info.clipName.setEnabled(false);
                this.info.clipLength.setLabel("");
                this.info.clipLoop.setEnabled(false);
                this.info.clipLoop.setActive(false);
                this.info.clipLoop.setParameter((BooleanParameter) null);
            }
            this.targetList.setItems(targets);
        }
    }

    public void targetAdded(LXClip clip, LXParameter target) {
        this.targetList.addItem(new TargetItem(target));
    }

    public void targetRemoved(LXClip clip, LXParameter target) {
        // TODO(mcslee): remove it
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

        private LXParameter target;

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

        void setTarget(LXParameter target) {
            if (this.target != target) {
                this.target = target;
                redraw();
            }
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            if (clip != null) {
                pg.stroke(0xff555555);
                pg.line(this.cursorX, 0, this.cursorX, this.height-1);
                if (this.target != null) {
                    int startX = -1;
                    int startY = -1;

                    for (LXClip.Event event : clip.events) {
                        if (event instanceof LXClip.ParameterEvent) {
                            LXClip.ParameterEvent parameterEvent = (LXClip.ParameterEvent) event;
                            if (parameterEvent.parameter == this.target) {
                                double eventBasis = parameterEvent.getBasis();
                                double eventNormalized = parameterEvent.getNormalized();
                                int endX = (int) Math.round(eventBasis * (this.width-1));
                                int endY = (int) Math.round(this.height - 1 - eventNormalized * (this.height-1));
                                if (startX >= 0) {
                                    pg.stroke(ui.theme.getPrimaryColor());
                                    pg.line(startX, startY, endX, endY);
                                } else {
                                    pg.stroke(0xff555555);
                                    pg.line(0, endY, endX, endY);
                                }
                                startX = endX;
                                startY = endY;
                            }
                        }
                    }
                    if (startX < this.width-1) {
                        pg.stroke(0xff555555);
                        pg.line(startX, startY, this.width-1, startY);
                    }
                }
            }
        }
    }

}
