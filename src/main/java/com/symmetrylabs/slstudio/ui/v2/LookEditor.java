package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXChannel.CrossfadeGroup;
import heronarts.lx.LXMasterChannel;
import heronarts.lx.color.LXColor;
import com.symmetrylabs.util.IterationUtils;
import org.lwjgl.system.CallbackI.B;
import heronarts.lx.LXLook;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.BoundedParameter;


public class LookEditor implements Window {
    private static final int HEIGHT = 300;
    private static final int PIPELINE_WIDTH = 230;
    private static final int PIPELINE_PAD = 8;
    private static final int MENU_HEIGHT = 22;

    private final LX lx;
    private LXLook look;
    private final WepUi wepUi;
    private final WepUi transformWepUi;
    private boolean showLookTransform = false;
    private float maxWindowHeight = -1;

    static final int[] MAP_COLORS = new int[10];
    static {
        for (int i = 0; i < MAP_COLORS.length; i++) {
            MAP_COLORS[i] = LXColor.hsb(i * 360.f / MAP_COLORS.length, 50, 50);
        }
    };

    public LookEditor(LX lx) {
        this.lx = lx;
        this.wepUi = new WepUi(lx, () -> UI.closePopup());
        this.transformWepUi = new WepUi(lx, false, () -> UI.closePopup());
    }

    public LXLook getLook() {
        return look;
    }

    public void setLook(LXLook l) {
        look = l;
    }

    @Override
    public void draw() {
        UI.setNextWindowPosition(0, UI.height, 0, 1);
        float desiredWidth = 260 + 100 * look.channels.size();
        UI.setNextWindowSize(Float.min(desiredWidth + 22, UI.width), HEIGHT);
        UI.setNextWindowContentSize(desiredWidth, HEIGHT - 22);
        UI.begin(
            "Look Editor Bottom Pane",
            UI.WINDOW_NO_MOVE | UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_TITLE_BAR | UI.WINDOW_NO_DOCKING
            | UI.WINDOW_NO_SCROLLBAR | (desiredWidth > UI.width ? UI.WINDOW_FORCE_HORIZ_SCROLL : 0));

        UI.beginGroup();
        UI.pushFont(FontLoader.DEFAULT_FONT_XL);
        UI.text(look.getLabel());
        UI.popFont();

        if (look.shelf != null) {
            int rows = look.shelf.rows();
            int cols = look.shelf.cols();
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    LXParameter param = look.shelf.getParameter(row, col);
                    if (param instanceof BoundedParameter) {
                        ParameterUI.draw(lx, (BoundedParameter) param, ParameterUI.WidgetType.KNOB);
                    } else {
                        ParameterUI.draw(lx, param);
                    }
                    if (col < cols - 1) {
                        UI.sameLine();
                    }
                }
            }
        }

        showLookTransform = UI.checkbox("Edit look transform", showLookTransform);
        UI.endGroup();
        UI.sameLine();
        UI.spacing(10, 10);

        int visibleWindowCount = IterationUtils.reduceIgnoreModification(look.channels, 0, (vwc, chan) -> {
                String chanName = chan.getLabel();
                UI.sameLine();
                UI.beginChild(chanName, false, 0, 90, HEIGHT);

                vwc = channelHeader(chan, chanName, vwc);

                float fader = UI.vertSliderFloat("##fader-" + chanName, chan.fader.getValuef(), 0, 1, "", 30, 180);
                if (fader != chan.fader.getValuef()) {
                    lx.engine.addTask(() -> chan.fader.setValue(fader));
                }
                UI.sameLine();

                UI.beginGroup();
                CrossfadeGroup group = chan.crossfadeGroup.getEnum();

                ParameterUI.toggle(lx, chan.enabled, false, 40);
                boolean cueStart = chan.cueActive.getValueb();
                if (ParameterUI.toggle(lx, chan.cueActive, true, 40) && !cueStart) {
                    for (LXChannel cc : look.channels) {
                        if (cc != chan) {
                            cc.cueActive.setValue(false);
                        }
                    }
                }
                boolean A = ParameterUI.toggle("A##" + chanName, group == CrossfadeGroup.A, false, 40);
                boolean B = ParameterUI.toggle("B##" + chanName, group == CrossfadeGroup.B, false, 40);

                if (A && group != CrossfadeGroup.A) {
                    group = CrossfadeGroup.A;
                } else if (B && group != CrossfadeGroup.B) {
                    group = CrossfadeGroup.B;
                } else if (!A && !B) {
                    group = CrossfadeGroup.BYPASS;
                }
                chan.crossfadeGroup.setValue(group);
                UI.endGroup();

                UI.pushWidth(80);
                ParameterUI.draw(lx, chan.blendMode, ParameterUI.ShowLabel.NO);
                UI.popWidth();

                UI.endChild();
                return vwc;
            });

        UI.sameLine();
        UI.pushFont(FontLoader.DEFAULT_FONT_XL);
        if (UI.button("+", 30, 230)) {
            lx.engine.addTask(() -> {
                    LXChannel chan = look.addChannel();
                    look.setFocusedChannel(chan);
                    chan.editorVisible.setValue(true);
                });
        }
        UI.popFont();

        UI.end();

        if (showLookTransform) {
            visibleWindowCount++;
        }

        if (visibleWindowCount == 0) {
            return;
        }

        float capWindowHeight = UI.height - HEIGHT - MENU_HEIGHT;
        float h = maxWindowHeight;
        if (h <= 0 || h > capWindowHeight) {
            h = capWindowHeight;
        }
        UI.setNextWindowPosition(0, MENU_HEIGHT, 0, 0);
        UI.setNextWindowSize(visibleWindowCount * (PIPELINE_WIDTH + PIPELINE_PAD), h);
        UI.begin("Pipeline Windows",
                 UI.WINDOW_NO_MOVE | UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_DECORATION | UI.WINDOW_NO_DOCKING | UI.WINDOW_NO_SCROLL_WITH_MOUSE);

        IterationUtils.reduceIgnoreModification(look.channels, 0, (pipelineIndex, chan) -> {
                if (chan.editorVisible.getValueb()) {
                    UI.beginChild(chan.getLabel() + "##channel-child", false, 0, PIPELINE_WIDTH, (int) UI.height);
                    pipelineIndex = channelHeader(chan, chan.getLabel(), pipelineIndex);
                    ChannelUI.draw(lx, chan, wepUi);
                    maxWindowHeight = Float.max(maxWindowHeight, UI.getCursorPosition().y);
                    UI.endChild();
                    UI.sameLine();
                }
                return pipelineIndex;
            });

        if (showLookTransform) {
            UI.beginChild("Look Transform##look-transform", false, 0, PIPELINE_WIDTH, (int) UI.height);

            UI.pushFont(FontLoader.DEFAULT_FONT_L);
            showLookTransform = UI.selectable("Look Transform##look-transform-header", true);
            UI.popFont();

            LXMasterChannel mc = lx.engine.masterChannel;
            ChannelUI.drawEffects(lx, "Look", mc);
            ChannelUI.drawWarps(lx, "Look", mc);
            ChannelUI.drawWepPopup(lx, mc, transformWepUi);

            UI.endChild();
        }
        UI.end();
    }

    private final int channelHeader(LXChannel chan, String chanName, int visibleWindowCount) {
        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        boolean isVisible = chan.editorVisible.getValueb();
        if (isVisible) {
            int c = MAP_COLORS[visibleWindowCount % MAP_COLORS.length];
            visibleWindowCount++;
            UI.pushColor(UI.COLOR_HEADER, c);
            UI.pushColor(UI.COLOR_HEADER_ACTIVE, c);
            UI.pushColor(UI.COLOR_HEADER_HOVERED, c);
        }
        boolean newVisible = UI.selectable(chanName + "##header", isVisible);
        if (isVisible != newVisible) {
            lx.engine.addTask(() -> chan.editorVisible.setValue(newVisible));
        }
        if (isVisible) {
            UI.popColor(3);
        }
        UI.popFont();
        return visibleWindowCount;
    }
}