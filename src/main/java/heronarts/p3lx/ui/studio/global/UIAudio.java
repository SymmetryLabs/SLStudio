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

package heronarts.p3lx.ui.studio.global;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;

import heronarts.lx.LXLoopTask;
import heronarts.lx.audio.LXAudioEngine;
import heronarts.lx.audio.LXAudioEngine.Mode;
import heronarts.lx.audio.LXAudioOutput;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIDropMenu;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UIToggleSet;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UIAudio extends UICollapsibleSection {

    public static void addGainAndRange(UI2dContainer container, float yp, BoundedParameter gain, BoundedParameter range) {
        new UILabel(0, yp, 24, 12).setLabel("Gain").addToContainer(container);
        new UIDoubleBox(26, yp, 54, 16).setParameter(gain).setShiftMultiplier(.1f).addToContainer(container);
        new UILabel(84, yp, 34, 12).setLabel("Range").addToContainer(container);
        new UIDoubleBox(118, yp, 54, 16).setParameter(range).setShiftMultiplier(.1f).addToContainer(container);
    }

    public static void addAttackAndRelease(UI2dContainer container, float yp, BoundedParameter attack, BoundedParameter release) {
        new UILabel(0, yp, 24, 12).setLabel("Atck").addToContainer(container);
        new UIDoubleBox(26, yp, 54, 16).setParameter(attack).addToContainer(container);
        new UILabel(84, yp, 30, 12).setLabel("Rels").addToContainer(container);
        new UIDoubleBox(118, yp, 54, 16).setParameter(release).addToContainer(container);
    }

    private static final int HEIGHT = 194;
    private static final int PADDING = 4;

    private final UI ui;
    private final LXAudioEngine audio;
    private final UIMeter meter;
    private final UIGraphicMeter graphicMeter;

    public UIAudio(final UI ui, final LXAudioEngine audio, float w) {
        super(ui, 0, 0, w, HEIGHT);
        this.ui = ui;
        this.audio = audio;
        setTitle("AUDIO");
        setTitleX(20);
        addTopLevelComponent(new UIButton(PADDING, PADDING, 12, 12) {
            @Override
            public void onToggle(boolean on) {
                meter.redraw();
                graphicMeter.redraw();
            }
        }.setParameter(audio.enabled).setBorderRounding(4));

        addTopLevelComponent(this.meter = new UIMeter(ui, 58, PADDING, 102, 12));

        // Mode selector
        new UIToggleSet(0, 2, getContentWidth(), 18)
        .setEvenSpacing()
        .setParameter(audio.mode)
        .addToContainer(this);

        this.graphicMeter = new UIGraphicMeter(ui, 0, 44, getContentWidth(), getContentHeight() - 44);
        this.graphicMeter.addToContainer(this);

        // Input menu
        final UIDropMenu inputMenu = new UIDropMenu(0, 24, getContentWidth(), 16, audio.input.device);
        inputMenu.setVisible(audio.mode.getEnum() == Mode.INPUT);
        inputMenu.addToContainer(this);

        // Output playback
        final UIOutputControls outputControls = new UIOutputControls(ui, audio.output, 0, 24, getContentWidth(), 16);
        outputControls.setVisible(audio.mode.getEnum() == Mode.OUTPUT);
        outputControls.addToContainer(this);

        audio.mode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                inputMenu.setVisible(audio.mode.getEnum() == Mode.INPUT);
                outputControls.setVisible(audio.mode.getEnum() == Mode.OUTPUT);
            }
        });

    }

    public class UIOutputControls extends UI2dContainer {

        UIOutputControls(final UI ui, final LXAudioOutput output, float x, float y, float w, float h) {
            super(x, y, w, h);

            new UIButton(0, 0, 16, getContentHeight())
            .setTriggerable(true)
            .setParameter(output.play)
            .setLabel("►")
            .addToContainer(this);

            final UILabel fileLabel = (UILabel) new UILabel(20, 0, getContentWidth() - 80, getContentHeight())
            .setPadding(0, 4)
            .setLabel(audio.output.getFileName())
            .setBackgroundColor(ui.theme.getControlBackgroundColor())
            .setBorderColor(ui.theme.getControlBorderColor())
            .setFont(ui.theme.getControlFont())
            .setTextAlignment(PConstants.LEFT, PConstants.CENTER)
            .addToContainer(this);

            new UIButton(getContentWidth() - 56, 0, 16, getContentHeight())
            .setTriggerable(true)
            .setIcon(ui.theme.iconTrigger)
            .setParameter(output.trigger)
            .addToContainer(this);

            new UIButton(getContentWidth() - 36, 0, 16, getContentHeight())
                .setIcon(ui.theme.iconLoop)
                .setParameter(output.looping)
                .addToContainer(this);

            new UIButton(getContentWidth() - 16, 0, 16, getContentHeight()) {
                @Override
                public void onToggle(boolean enabled) {
                    if (enabled) {
                        FileDialog dialog = new FileDialog(
                            (Frame) null, "Select a WAV file:", FileDialog.LOAD);
                        dialog.setFilenameFilter((dir, name) -> name.endsWith(".wav"));
                        dialog.setVisible(true);

                        String fname = dialog.getFile();
                        if (fname == null) {
                            return;
                        }
                        Path path = new File(ui.applet.sketchPath()).toPath().relativize(new File(dialog.getDirectory(), fname).toPath());
                        audio.output.file.setValue(path.toString());
                        if (!audio.output.isPlayable()) {
                            fileLabel.setLabel("Unable to play file");
                        }
                    }
                }
            }
            .setIcon(ui.theme.iconLoad)
            .setMomentary(true)
            .setDescription("Opens a new audio file for playback")
            .addToContainer(this);

            audio.output.file.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter p) {
                    fileLabel.setLabel(audio.output.getFileName());
                }
            });
        }
    }

    class UIGraphicMeter extends UI2dContainer {

        private static final int METER_HEIGHT = 60;

        private final UIMeterBand[] bands;

        UIGraphicMeter(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);

            float bandSpace = this.width-2;
            float bandWidth = (bandSpace - audio.meter.numBands + 1) / audio.meter.numBands;
            this.bands = new UIMeterBand[audio.meter.numBands];
            for (int i = 0, bandX = 1; i < audio.meter.numBands; ++i) {
                int nextBandX = Math.round(1 + (i+1)*(bandWidth+1));
                this.bands[i] = new UIMeterBand(ui, i, bandX, 1, nextBandX-bandX-1, METER_HEIGHT-2);
                this.bands[i].addToContainer(this);
                bandX = nextBandX;
            }

            float yp = 62;
            addGainAndRange(this, yp, audio.meter.gain, audio.meter.range);

            yp += 22;
            new UIKnob(8, yp).setParameter(audio.meter.attack).addToContainer(this);
            new UIKnob(62, yp).setParameter(audio.meter.release).addToContainer(this);
            new UIKnob(116, yp).setParameter(audio.meter.slope).addToContainer(this);

            addLoopTask(new LXLoopTask() {
                public void loop(double deltaMs) {
                    if (audio.enabled.isOn()) {
                        for (UIMeterBand band : bands) {
                            band.redraw();
                        }
                    }
                }
            });
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            pg.stroke(ui.theme.getControlBorderColor());
            pg.fill(ui.theme.getDarkBackgroundColor());
            pg.rect(0, 0, width-1, METER_HEIGHT-1);
        }

        class UIMeterBand extends UI2dComponent implements UIModulationSource {

            private final int index;

            UIMeterBand(UI ui, int index, float x, float y, float w, float h) {
                super(x, y, w, h);
                this.index = index;
                setBackgroundColor(ui.theme.getDarkBackgroundColor());
            }

            @Override
            public void onDraw(UI ui, PGraphics pg) {
                pg.noStroke();
                pg.fill(audio.enabled.isOn() ? ui.theme.getPrimaryColor(): ui.theme.getControlDisabledColor());
                float h = height * audio.meter.getBandf(this.index);
                pg.rect(0, height-h, width, h);
            }

            public LXNormalizedParameter getModulationSource() {
                return audio.meter.bands[this.index];
            }
        }

    }

    class UIMeter extends UI2dComponent implements UIModulationSource {

        UIMeter(UI ui, float x, float y, float w, float h) {
            super(x, y, w, h);
            setBackgroundColor(ui.theme.getDarkBackgroundColor());
            setBorderColor(ui.theme.getControlBorderColor());

            addLoopTask(new LXLoopTask() {
                public void loop(double deltaMs) {
                    if (audio.enabled.isOn()) {
                        redraw();
                    }
                }
            });
        }

        @Override
        public LXNormalizedParameter getModulationSource() {
            return audio.meter;
        }

        @Override
        protected void onDraw(UI ui, PGraphics pg) {
            if (audio.enabled.isOn()) {
                pg.fill(ui.theme.getPrimaryColor());
                pg.noStroke();
                pg.rect(1, 1, audio.meter.getNormalizedf() * (width-2), (height-2));
                pg.stroke(ui.theme.getSecondaryColor());
                float peak = audio.meter.getPeakf();
                pg.line(1 + (width-3) * peak, 1, 1 + (width-3) * peak , height-2);
            }
        }
    }
}
