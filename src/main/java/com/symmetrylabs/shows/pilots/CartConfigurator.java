package com.symmetrylabs.shows.pilots;

import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;

import java.util.ArrayList;
import java.util.List;

/** A UI component for switching the IP address of each cart. */
public class CartConfigurator extends UICollapsibleSection {
    private static final int PAD = 4;

    public interface ConfigChangedListener {
        void onConfigChanged(CartConfig[] configs);
    }

    private final List<StringParameter> addrParams;
    private ConfigChangedListener listener;

    public CartConfigurator(UI ui, float x, float y, float w) {
        super(ui, x, y, w, 220);
        setTitle("MODULE ADDRESSES");

        addrParams = new ArrayList<>();
        for (String id : CartConfig.ids) {
            StringParameter p = new StringParameter(id);
            p.setShouldSerialize(false);
            addrParams.add(p);
        }

        final float labelWidth = 50;
        final float inputWidth = getContentWidth() - labelWidth - PAD;
        final float elemHeight = 20;
        float yAccum = PAD;

        for (StringParameter addrParam : addrParams) {
            new UILabel(PAD, yAccum, labelWidth, elemHeight)
                .setLabel(addrParam.getLabel())
                .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
                .addToContainer(this);
            new UITextBox(PAD + labelWidth, yAccum, inputWidth, elemHeight)
                .setParameter(addrParam)
                .addToContainer(this);
            yAccum += elemHeight + PAD;
        }

        new SaveButton(getContentWidth() - labelWidth, yAccum, labelWidth, elemHeight).addToContainer(this);
        yAccum += elemHeight + PAD;
    }

    public void setListener(ConfigChangedListener listener) {
        this.listener = listener;
    }

    public void applyConfigs(CartConfig[] configs) {
        for (CartConfig cc : configs) {
            for (StringParameter p : addrParams) {
                if (p.getLabel().equals(cc.modelId)) {
                    p.setValue(cc.address);
                }
            }
        }
    }

    private void sendUpdate() {
        if (listener != null) {
            CartConfig[] configs = new CartConfig[addrParams.size()];
            for (int i = 0; i < addrParams.size(); i++) {
                StringParameter p = addrParams.get(i);
                configs[i] = new CartConfig(p.getLabel(), p.getString());
            }
            listener.onConfigChanged(configs);
        }
    }

    private class SaveButton extends UIButton {
        public SaveButton(float x, float y, float w, float h) {
            super(x, y, w, h);
            setLabel("Save");
            setMomentary(true);
            setTextAlignment(PConstants.CENTER, PConstants.CENTER);
        }

        @Override
        protected void onToggle(boolean active) {
            if (active) {
                sendUpdate();
            }
        }
    }
}
