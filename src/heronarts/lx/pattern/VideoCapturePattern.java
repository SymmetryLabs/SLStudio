/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.pattern;

import heronarts.lx.HeronLX;
import heronarts.lx.transition.IrisTransition;

import processing.video.Capture;

public class VideoCapturePattern extends LXPattern {
    
    private Capture capture;
    
    public VideoCapturePattern(HeronLX lx) {
        super(lx);
        this.capture = null;
        this.transition = new IrisTransition(lx);
    }

    protected void onActive() {
        this.capture = new Capture(lx.applet, lx.width, lx.height);
    }
    
    protected void onInactive() {
        this.capture.dispose();
        this.capture = null;
    }
    
    public void run(int deltaMs) {
        if (this.capture.available()) {
            this.capture.read();
        }
        this.capture.loadPixels();
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = this.capture.pixels[i];
        }
    }

}
