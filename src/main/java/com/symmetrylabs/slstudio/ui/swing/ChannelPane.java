package com.symmetrylabs.slstudio.ui.swing;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ChannelPane extends JPanel {
    private final LX lx;

    ChannelPane(LX lx) {
        this.lx = lx;
        setPreferredSize(new Dimension(0, 200));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        for (LXChannel chan : lx.engine.getChannels()) {
            add(new ChannelPanel(chan));
        }
    }

    private class ChannelPanel extends JPanel {
        private final LXChannel chan;

        private final JSlider fader;
        private final JToggleButton enabled;
        private final JToggleButton cue;

        public ChannelPanel(LXChannel chan) {
            this.chan = chan;
            setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 5), chan.getLabel()));
            setMaximumSize(new Dimension(100, 200));
            setLayout(new BorderLayout());

            fader = new JSlider(
                SwingConstants.VERTICAL, 0, 100, (int) (100 * chan.fader.getValue()));
            fader.addChangeListener(ce -> {
                    lx.engine.addTask(() -> chan.fader.setValue(0.01f * fader.getValue()));
                });
            chan.fader.addListener(p -> {
                    fader.setValue((int) (100 * chan.fader.getValue()));
                });
            add(fader, BorderLayout.WEST);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

            enabled = new JToggleButton("E", chan.enabled.getValueb());
            enabled.setMaximumSize(new Dimension(60, 30));
            enabled.addChangeListener(ce -> {
                    lx.engine.addTask(() -> chan.enabled.setValue(enabled.isSelected()));
                });
            chan.enabled.addListener(p -> {
                    enabled.setSelected(chan.enabled.getValueb());
                });
            right.add(enabled);

            cue = new JToggleButton("C", chan.cueActive.getValueb());
            cue.setMaximumSize(new Dimension(60, 30));
            cue.addChangeListener(ce -> {
                    lx.engine.addTask(() -> chan.cueActive.setValue(cue.isSelected()));
                });
                chan.cueActive.addListener(p -> {
                        cue.setSelected(chan.cueActive.getValueb());
                    });
            right.add(cue);

            add(right, BorderLayout.EAST);
        }
    }
}
