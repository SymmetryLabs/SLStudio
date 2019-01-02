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

package heronarts.p3lx.ui.studio;

import heronarts.lx.LX;
import heronarts.p3lx.LXStudio;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.component.UIImage;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.studio.global.UIAudio;
import heronarts.p3lx.ui.studio.global.UICamera;
import heronarts.p3lx.ui.studio.global.UIEngine;
import heronarts.p3lx.ui.studio.global.UIPalette;
import heronarts.p3lx.ui.studio.global.UITempo;
import heronarts.p3lx.ui.studio.project.UIEffectManager;
import heronarts.p3lx.ui.studio.project.UIPatternManager;
import heronarts.p3lx.ui.studio.project.UIProjectManager;
import heronarts.p3lx.ui.studio.project.UIWarpManager;

public class UILeftPane extends UIPane {

    public final UI2dScrollContext browser;
    public final UI2dScrollContext global;
    public final UIAudio audio;
    public final UIPalette palette;
    public final UIEngine engine;
    public final UICamera camera;

    public static final int WIDTH = 4*UIKnob.WIDTH + 20 + 2*UIPane.PADDING + 2*UIPane.MARGIN;
    public static final int TOP_OFFSET = 32;

    public UILeftPane(UI ui, LX lx) {
        super(ui, lx, new String[] { "BROWSER", "GLOBAL" }, 0, WIDTH, TOP_OFFSET);

        new UIImage(ui.applet.loadImage("lxstudio.png"), MARGIN, 4)
        .setDescription(LXStudio.COPYRIGHT)
        .addToContainer(this);

        this.browser = this.sections[0];
        this.global = this.sections[1];
        setActiveSection(1);

        // Global elements
        new UITempo(ui, lx, 0, 0, this.global.getContentWidth(), 26).addToContainer(this.global);
        this.audio = (UIAudio) new UIAudio(ui, lx.engine.audio, this.global.getContentWidth()).addToContainer(this.global);
        this.palette = (UIPalette) new UIPalette(ui, lx.palette, 0, 0, this.global.getContentWidth()).addToContainer(this.global);
        this.engine = (UIEngine) new UIEngine(ui, lx.engine, 0, 0, this.global.getContentWidth()).addToContainer(this.global);
        this.engine.setVisible(false);
        if (ui instanceof LXStudio.UI) {
            this.camera = (UICamera) new UICamera(ui, ((LXStudio.UI) ui).preview, 0, 0, this.global.getContentWidth()).addToContainer(this.global);
            this.camera.setVisible(false);
        } else {
            this.camera = null;
        }

        // Project elements
        new UIProjectManager(ui, lx, 0, 0, this.browser.getContentWidth()).addToContainer(this.browser);
        new UIPatternManager(ui, lx, 0, 0, this.browser.getContentWidth()).addToContainer(this.browser);
        new UIWarpManager(ui, lx, 0, 0, this.browser.getContentWidth()).addToContainer(this.browser);
        new UIEffectManager(ui, lx, 0, 9, this.browser.getContentWidth()).addToContainer(this.browser);
    }
}