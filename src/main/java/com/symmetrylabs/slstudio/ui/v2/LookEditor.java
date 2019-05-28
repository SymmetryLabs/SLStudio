package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXChannel.CrossfadeGroup;
import heronarts.lx.LXMasterChannel;
import heronarts.lx.color.LXColor;
import com.symmetrylabs.util.IterationUtils;
import heronarts.lx.LXLook;
import heronarts.lx.mutation.AddChannel;
import heronarts.lx.mutation.RemoveChannel;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.mutation.Mutations;


public class LookEditor implements Window {
    private static final int HEIGHT = 300;
    private static final int PIPELINE_WIDTH = 230;
    private static final int PIPELINE_PAD = 8;
    private static final int MENU_HEIGHT = 22;

    private final LX lx;
    private LXLook look;
    private final WepUI wepUi;
    private final WepUI transformWepUi;
    private final ParameterUI pui;
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
        this.wepUi = new WepUI(lx, () -> UI.closePopup());
        this.transformWepUi = new WepUI(lx, false, () -> UI.closePopup());
        this.pui = ParameterUI.getMappable(lx).preferKnobs(true);
    }

    public void dispose() {
        pui.dispose();
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
                    pui.draw(param);
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
                UI.beginChild("chanWindow" + chan.getIndex(), false, 0, 90, HEIGHT);

                vwc = channelHeader(chan, chanName, vwc);

                float fader = UI.vertSliderFloat("##fader-" + chanName, chan.fader.getValuef(), 0, 1, "", 30, 180);
                if (fader != chan.fader.getValuef()) {
                    lx.engine.addTask(() -> chan.fader.setValue(fader));
                }
                UI.sameLine();

                UI.beginGroup();
                CrossfadeGroup group = chan.crossfadeGroup.getEnum();

                pui.toggle(chan.enabled, false, 40);
                boolean cueStart = chan.cueActive.getValueb();
                if (pui.toggle(chan.cueActive, true, 40) && !cueStart) {
                    for (LXChannel cc : look.channels) {
                        if (cc != chan) {
                            cc.cueActive.setValue(false);
                        }
                    }
                }
                boolean A = pui.toggle("A##" + chanName, group == CrossfadeGroup.A, false, 40);
                boolean B = pui.toggle("B##" + chanName, group == CrossfadeGroup.B, false, 40);

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
                pui.showLabel(false);
                pui.draw(chan.blendMode);
                pui.showLabel(true);
                UI.popWidth();

                UI.endChild();
                return vwc;
            });

        UI.sameLine();
        UI.pushFont(FontLoader.DEFAULT_FONT_XL);
        if (UI.button("+", 30, 230)) {
            lx.engine.mutations.enqueue(AddChannel.newBuilder().setLook(look.getIndex()));
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
                    UI.beginChild("pipeline" + chan.getIndex(), false, 0, PIPELINE_WIDTH, (int) UI.height);
                    pipelineIndex = channelHeader(chan, chan.getLabel(), pipelineIndex);
                    ChannelUI.draw(lx, chan, pui, wepUi);
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

            pui.push().allowMapping(true);

            LXMasterChannel mc = lx.engine.masterChannel;
            ChannelUI.drawEffects(lx, "Look", mc, pui);
            ChannelUI.drawWarps(lx, "Look", mc, pui);
            ChannelUI.drawWepPopup(lx, mc, transformWepUi);

            pui.pop();

            UI.endChild();
        }
        UI.end();
    }

    private int channelHeader(final LXChannel chan, String chanName, int visibleWindowCount) {
        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        boolean isVisible = chan.editorVisible.getValueb();
        if (isVisible) {
            int c = MAP_COLORS[visibleWindowCount % MAP_COLORS.length];
            visibleWindowCount++;
            UI.pushColor(UI.COLOR_HEADER, c);
            UI.pushColor(UI.COLOR_HEADER_ACTIVE, c);
            UI.pushColor(UI.COLOR_HEADER_HOVERED, c);
        }

        // use the index here so that if we rename it we don't close the popup
        String chanRef = "Chan" + chan.getIndex();

        boolean newVisible = UI.selectable(chanName + "###header" + chanRef, isVisible);
        if (isVisible != newVisible) {
            lx.engine.addTask(() -> chan.editorVisible.setValue(newVisible));
        }
        if (isVisible) {
            UI.popColor(3);
        }
        UI.popFont();
        if (UI.beginContextMenu(chanRef)) {
            UI.text("Rename channel:");
            chan.label.setValue(UI.inputText("##newChanName", chan.getLabel()));
            if (UI.contextMenuItem("Delete", look.channels.size() > 1)) {
                lx.engine.mutations.enqueue(
                    RemoveChannel.newBuilder()
                        .setLook(look.getIndex()).setChannel(chan.getIndex()));
            }
            UI.endContextMenu();
        }
        return visibleWindowCount;
    }
}
