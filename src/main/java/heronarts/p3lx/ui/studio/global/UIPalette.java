/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.ui.studio.global;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXPalette;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDoubleBox;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UISwitch;
import heronarts.p3lx.ui.component.UIToggleSet;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UIPalette extends UICollapsibleSection {

    private static final float HEIGHT = 184;

    private final LXPalette palette;

    public UIPalette(UI ui, final LXPalette palette, float x, float y, float w) {
        super(ui, x, y, w, HEIGHT);
    setTitle("PALETTE");
        this.palette = palette;

        addTopLevelComponent(
            new UIButton(getWidth() - 48, 2, 28, 16)
            .setLabel("CUE")
            .setActiveColor(ui.theme.getAttentionColor())
            .setParameter(palette.cue)
        );

    addTopLevelComponent(
    	new UITopLevelPreview(getWidth() - 100, 2, 50,14)
		);

        new UIToggleSet(0, 2, getContentWidth(), 18)
        .setEvenSpacing()
        .setParameter(palette.hueMode)
        .addToContainer(this);

        final UIColorFixed fixed = new UIColorFixed(ui, 0, 24, getContentWidth(), 40);
        fixed.addToContainer(this);
        fixed.setVisible(palette.hueMode.getObject() == LXPalette.Mode.FIXED);

        final UIColorOscillate oscillate = new UIColorOscillate(ui, 0, 24, getContentWidth(), 40);
        oscillate.addToContainer(this);
        oscillate.setVisible(palette.hueMode.getObject() == LXPalette.Mode.OSCILLATE);

        final UIColorCycle cycle = new UIColorCycle(ui, 0, 24, getContentWidth(), 40);
        cycle.addToContainer(this);
        cycle.setVisible(palette.hueMode.getObject() == LXPalette.Mode.CYCLE);

        palette.hueMode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                fixed.setVisible(palette.hueMode.getObject() == LXPalette.Mode.FIXED);
                cycle.setVisible(palette.hueMode.getObject() == LXPalette.Mode.CYCLE);
                oscillate.setVisible(palette.hueMode.getObject() == LXPalette.Mode.OSCILLATE);
            }
        });


        int ky = 70;
        int ks = UIKnob.WIDTH + 4;
        new UIKnob(0, ky).setParameter(palette.spread).addToContainer(this);
        new UIKnob(ks, ky).setParameter(palette.spreadX).addToContainer(this);
        new UIKnob(2*ks, ky).setParameter(palette.spreadY).addToContainer(this);
        new UIKnob(3*ks, ky).setParameter(palette.spreadZ).addToContainer(this);

        ky += 48;
        new UISwitch(0, ky).setParameter(palette.mirror).addToContainer(this);
        new UIKnob(ks, ky).setParameter(palette.offsetX).addToContainer(this);
        new UIKnob(2*ks, ky).setParameter(palette.offsetY).addToContainer(this);
        new UIKnob(3*ks, ky).setParameter(palette.offsetZ).addToContainer(this);


    }

	class UITopLevelPreview extends UI2dComponent {
		private int xp;
		private float sv;
		private float hv;

		UITopLevelPreview(float x, float y, float w, float h) {
			super(x, y, w, h);
			addLoopTask(new LXLoopTask() {
				public void loop(double deltaMs) {
					int xv = (int) LXUtils.constrainf(palette.getHuef() / 360 * width, 1, width-1);
					float svf = palette.getSaturationf();
//					float hvf = palette.clr.hue.getValuef();
					float hvf = palette.getHuef();
					if (xp != xv || svf != sv || hvf != hv) {
						xp = xv;
						sv = svf;
						hv = hvf;
						redraw();
					}
				}
			});
		}

		@Override
		public void onDraw(UI ui, PGraphics pg) {
			for (int x = 1; x < this.width-1; ++x) {
                int p_color = palette.getColorByRange(x-1, this.width);
                pg.stroke(p_color);
                pg.line(x, 1, x, this.getHeight());
			}
		}
	}

    class UIColorFixed extends UI2dContainer {

        UIColorFixed(UI ui, float x, float y, float w, float   h) {
            super(x, y, w, h);

            new UIHueFixed(0, 0, getContentWidth(), 20).setBorderColor(ui.theme.getControlBorderColor()).addToContainer(this);

            new UIDoubleBox(0, 22, getContentWidth()/2-1, 20)
            .setParameter(palette.clr.hue)
            .addToContainer(this);

            new UIDoubleBox(getContentWidth()/2+1, 22, getContentWidth()/2-1, 20)
            .setParameter(palette.clr.saturation)
            .addToContainer(this);

        }

        class UIHueFixed extends UI2dComponent {
            private int xp;
            private float sv;

            UIHueFixed(float x, float y, float w, float h) {
                super(x, y, w, h);
                addLoopTask(new LXLoopTask() {
                    public void loop(double deltaMs) {
                        int xv = (int) LXUtils.constrainf(palette.getHuef() / 360 * width, 1, width-1);
                        float svf = palette.getSaturationf();
                        if (xp != xv || svf != sv) {
                            xp = xv;
                            sv = svf;
                            redraw();
                        }
                    }
                });
            }

            private void updateHue(float mx, float my) {
                palette.clr.hue.setValue(mx / (this.width-1) * 360);
//				palette.color.hue.setValue(mx / (this.width-1) * 360);
            }

            @Override
            public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                updateHue(mx, my);
            }

            @Override
            public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
                updateHue(mx, my);
            }

            @Override
            public void onDraw(UI ui, PGraphics pg) {
                for (int x = 1; x < this.width-1; ++x) {
                    pg.stroke(LXColor.hsb((x-1) / (this.width-2) * 360, sv, x == xp ? 100 : 75));
                    pg.line(x, 1, x, 19);
                }
            }
        }
    }

    class UIColorCycle extends UI2dContainer {

        UIColorCycle(UI ui, float x, float y, float w, float   h) {
            super(x, y, w, h);

            new UIHueCycle(0, 0, getContentWidth(), 20).setBorderColor(ui.theme.getControlBorderColor()).addToContainer(this);

            new UIDoubleBox(0, 22, getContentWidth()/2-1, 20)
            .setParameter(palette.period)
            .addToContainer(this);

            new UIDoubleBox(getContentWidth()/2+1, 22, getContentWidth()/2-1, 20)
            .setParameter(palette.clr.saturation)
            .addToContainer(this);

        }

        class UIHueCycle extends UI2dComponent {

            private int xp;
            private float sv;

            UIHueCycle(float x, float y, float w, float h) {
                super(x, y, w, h);
                addLoopTask(new LXLoopTask() {
                    public void loop(double deltaMs) {
                        int xv = (int) LXUtils.constrainf(palette.getHuef() / 360 * width, 1, width-1);
                        float svf = palette.getSaturationf();
                        if (xp != xv || svf != sv) {
                            xp = xv;
                            sv = svf;
                            redraw();
                        }
                    }
                });
            }

            @Override
            public void onDraw(UI ui, PGraphics pg) {
                for (int x = 1; x < this.width - 1; ++x) {
                    pg.stroke(LX.hsb((x-1) / (this.width-2) * 360, sv, x == xp ? 100 : 75));
                    pg.line(x, 1, x, 19);
                }
            }
        }
    }

    class UIColorOscillate extends UI2dContainer {

        UIColorOscillate(UI ui, float x, float y, float w, float   h) {
            super(x, y, w, h);

            new UIHueOscillate(0, 0, getContentWidth(), 20).setBorderColor(ui.theme.getControlBorderColor()).addToContainer(this);

            new UIDoubleBox(0, 22, 42, 20)
            .setParameter(palette.period)
            .addToContainer(this);

            new UIDoubleBox(44, 22, 42, 20)
            .setParameter(palette.clr.hue)
            .addToContainer(this);

            new UIDoubleBox(88, 22, 42, 20)
            .setParameter(palette.range)
            .addToContainer(this);

            new UIDoubleBox(132, 22, 40, 20)
            .setParameter(palette.clr.saturation)
            .addToContainer(this);
        }

        class UIHueOscillate extends UI2dComponent {
            private int h1, h2, xp;
            private float sat;

            UIHueOscillate(float x, float y, float w, float h) {
                super(x, y, w, h);
                addLoopTask(new LXLoopTask() {
                    public void loop(double deltaMs) {
                        float sv = palette.clr.saturation.getValuef();
                        int h1v = (int) LXUtils.constrainf(palette.clr.hue.getValuef() / 360 * width, 1, width-1);
                        int h2v = (int) LXUtils.constrainf(((palette.clr.hue.getValuef() + palette.range.getValuef()) % 360) / 360 * width, 1, width-1);
                        int xv = (int) LXUtils.constrainf(palette.getHuef() % 360 / 360 * width, 1, width-1);

                        if (h1v != h1 || h2v != h2 || sv != sat || xv != xp) {
                            h1 = h1v;
                            h2 = h2v;
                            sat = sv;
                            xp = xv;
                            redraw();
                        }
                    }
                });
            }


            boolean moveH1 = false;
            boolean moveH2 = false;
            boolean moveBlock = false;

            @Override
            public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                moveH1 = moveH2 = moveBlock = false;
                if (Math.abs(mx - h1) < 4) {
                    moveH1 = true;
                } else if (Math.abs(mx - h2) < 4) {
                    moveH2 = true;
                } else if (h1 < h2 && mx >= h1 && mx <= h2) {
                    moveBlock = true;
                } else if (h1 > h2 && (mx >= h1 || mx <= h2)) {
                    moveBlock = true;
                }
            }

            @Override
            public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
                if (mx >= 0 && mx < this.width) {
                    if (moveH2) {
                        palette.range.setValue(palette.range.getValue() + dx * 360 / this.width);
                    } else if (moveH1) {
                        palette.clr.hue.setValue(palette.clr.hue.getValue() + dx * 360 / this.width);
                        palette.range.setValue(palette.range.getValue() - dx * 360 / this.width);
                    } else if (moveBlock) {
                        palette.clr.hue.setValue(palette.clr.hue.getValue() + dx * 360 / this.width);
                    }
                }
            }

            @Override
            public void onDraw(UI ui, PGraphics pg) {
                boolean l2r = (h2 > h1) || (palette.range.getValue() == 0);
                for (int x = 1; x < this.width - 1; ++x) {
                    boolean inRange = (l2r && x >= h1 && x <= h2) ||
                            (!l2r && (x <= h2 || x >= h1));
                    pg.stroke(LX.hsb((x-1) / (this.width-2) * 360, sat, (x == xp) ? 100 : (inRange ? 75 : 50)));
                    pg.line(x, 1, x, 19);
                }
                pg.stroke(ui.theme.getControlBorderColor());
                pg.line(h1, 1, h1, 19);
                pg.line(h2, 1, h2, 19);
            }
        }
    }
}
