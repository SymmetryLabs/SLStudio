package com.symmetrylabs.util.dmx.ui;

import java.util.Map;
import java.util.HashMap;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;

import heronarts.lx.LX;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UIParameterLabel;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.studio.modulation.UIModulator;

import com.symmetrylabs.util.dmx.DMXParameterMapper;

public class UIDmxMappings extends UICollapsibleSection {

    private static final int SPACING = 2;

    private final DMXParameterMapper dmxMapper;
    private final UI ui;
    private final Map<LXParameter, UIDmxMapping> uiMappings = new HashMap<>();

    public UIDmxMappings(UI ui, final LX lx, float x, float y, float w) {
        super(ui, x, y, w, 0);

        this.ui = ui;
        this.dmxMapper = DMXParameterMapper.getInstance(lx);

        setTitle("DMX MAPPINGS");

        for (DMXParameterMapper.Mapping mapping : dmxMapper.getMappings()) {
            addMapping(mapping.parameter, mapping.channel);
        }

        dmxMapper.addMappingListener(new DMXParameterMapper.MappingListener() {
            @Override
            public void mappingAdded(DMXParameterMapper.Mapping mapping) {
                addMapping(mapping.parameter, mapping.channel);
            }

            @Override
            public void mappingRemoved(DMXParameterMapper.Mapping mapping) {
                removeMapping(mapping.parameter);
            }
        });

        setLayout(UI2dContainer.Layout.VERTICAL);
        setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.VERTICAL);
        setChildMargin(SPACING);

        final UIButton mapButton = (UIButton) new UIButton(getContentWidth() - 36, 3, UIModulator.MAP_WIDTH, 14) {
            @Override
            protected void onToggle(boolean on) {
                if (on) {
                    lx.engine.mapping.setMode(LXMappingEngine.Mode.MIDI);
                } else {
                    lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                }
            }
        }
        .setIcon(ui.theme.iconMap)
        .setDescription("Enter DMX mapping mode");

        lx.engine.mapping.mode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (lx.engine.mapping.getMode() != LXMappingEngine.Mode.MIDI) {
                    mapButton.setActive(false);
                }
            }
        });

        lx.engine.mapping.addControlTargetListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (p != null && mapButton.isActive()) {
                    addMapping(p, -1);
                    mapButton.setActive(false);
                }
            }
        });

        addTopLevelComponent(mapButton);
    }

    private void addMapping(LXParameter parameter, int channel) {
        if (!uiMappings.containsKey(parameter)) {
            UIDmxMapping uiDmxMapping = new UIDmxMapping(ui, parameter, channel, 0, 0, getContentWidth());
            uiDmxMapping.addToContainer(this);
            uiMappings.put(parameter, uiDmxMapping);
        }
    }

    private void removeMapping(LXParameter parameter) {
        UIDmxMapping uiDmxMapping = uiMappings.get(parameter);
        if (uiDmxMapping != null) {
            uiDmxMapping.removeFromContainer();
            uiMappings.remove(uiDmxMapping);
        }
    }

    private class UIDmxMapping extends UI2dContainer implements UIFocus {

        private static final int PADDING = 4;
        private static final int HEIGHT = 20;
        private static final int CHANNEL_INPUT_WIDTH = 40;

        private final LXParameter parameter;
        private final int channel;

        private final DiscreteParameter channelParameter;

        UIDmxMapping(UI ui, LXParameter parameter, int channel, float x, float y, float w) {
            super(x, y, w, HEIGHT);

            this.parameter = parameter;
            this.channel = channel;

            channelParameter = new DiscreteParameter("channel", channel + 1, 0, 257);
            channelParameter.setFormatter(v -> ((int)v) > 0 ? Integer.toString((int) v) : "-");
            channelParameter.addListener(p -> dmxMapper.mapParameter(parameter, ((DiscreteParameter)p).getValuei() - 1));

            setBackgroundColor(ui.theme.getDarkBackgroundColor());
            setBorderRounding(4);

            new UIParameterLabel(PADDING, 0, getContentWidth() - CHANNEL_INPUT_WIDTH - PADDING, HEIGHT)
                    .setParameter(parameter)
                    .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                    .addToContainer(this);
            new UIIntegerBox(getContentWidth() - CHANNEL_INPUT_WIDTH, 0, CHANNEL_INPUT_WIDTH, HEIGHT)
                    .setParameter(channelParameter)
                    .setMappable(false)
                    .addToContainer(this);
        }

        @Override
        public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE ||
                    ((keyEvent.isControlDown() || keyEvent.isMetaDown()) && keyCode == java.awt.event.KeyEvent.VK_D)) {
                removeMapping(parameter);
                dmxMapper.unmapParameter(parameter);
            }
        }

        @Override
        public void drawFocus(UI ui, PGraphics pg) {
            pg.stroke(ui.theme.getPrimaryColor());
            pg.line(0, 0, 0, this.height-1);
        }

    }
}
