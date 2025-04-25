package com.symmetrylabs.slstudio;

import processing.core.PApplet;

public class MinimalTest extends PApplet {
    public void settings() {
        System.out.println("MinimalTest: settings() called");
        System.out.println("MinimalTest: settings() - before size()");
        size(400, 400, P3D);
        System.out.println("MinimalTest: settings() - after size()");
    }
    public void draw() {
        System.out.println("MinimalTest: draw() called");
        System.out.println("MinimalTest: draw() - before background()");
        background(0);
        System.out.println("MinimalTest: draw() - after background()");
        System.out.println("MinimalTest: draw() - before fill()");
        fill(255, 0, 0);
        System.out.println("MinimalTest: draw() - after fill()");
        System.out.println("MinimalTest: draw() - before ellipse()");
        ellipse(width/2, height/2, 100, 100);
        System.out.println("MinimalTest: draw() - after ellipse()");
    }
    public static void main(String[] args) {
        System.out.println("MinimalTest: main() called");
        System.out.println("MinimalTest: main() - before PApplet.main()");
        PApplet.main("com.symmetrylabs.slstudio.MinimalTest");
        System.out.println("MinimalTest: main() - after PApplet.main()");
    }
}
