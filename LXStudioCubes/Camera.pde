public class PhotoBoothCamera extends DPat {
    String[] cameras;
    Capture camera;
    PImage frame;

    private int WIDTH = (int)model.photoBoothCanvas.xRange*10;
    private int HEIGHT = (int)model.photoBoothCanvas.yRange*10;

    public PhotoBoothCamera(LX lx) {
        super(lx);

        cameras = Capture.list();
        println("Available cameras: " + cameras[0]);

        println("w: " + WIDTH);
        println("h: " + HEIGHT);

        camera = new Capture(applet, cameras[0]);
        camera.start();

        frame = createImage(WIDTH, HEIGHT, RGB);
    }

    public void StartRun(double deltaMs) {
        if (camera.available()) {
            frame.copy(camera, 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT);
            frame.updatePixels();
            camera.read();
        }

        for (int i = 0; i < frame.pixels.length; i++) {
           //print(frame.pixels[i] + ", ");
        }
    }

    public color CalcPoint(PVector p) {
        int x = (int)((p.x / WIDTH) * frame.width);
        int y = (int)((p.y / HEIGHT) * frame.height);
        // print(frame.get(x, y));
        return frame.get(x, y);
    }
}