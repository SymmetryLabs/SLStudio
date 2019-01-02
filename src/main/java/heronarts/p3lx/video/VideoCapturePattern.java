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
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.video;

import heronarts.p3lx.P3LX;
import heronarts.p3lx.P3LXPattern;
import processing.video.Capture;

public class VideoCapturePattern extends P3LXPattern {

    private Capture capture;

    public VideoCapturePattern(P3LX lx) {
        super(lx);
        this.capture = null;
    }

    @Override
    public void onActive() {
        this.capture = new Capture(this.applet, this.lx.width, this.lx.height);
    }

    @Override
    public void onInactive() {
        this.capture.dispose();
        this.capture = null;
    }

    @Override
    public void run(double deltaMs) {
        if (this.capture.available()) {
            this.capture.read();
        }
        this.capture.loadPixels();
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = this.capture.pixels[i];
        }
    }

}
