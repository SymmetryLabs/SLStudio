// 144 leds per meter
// 16 vertical strips (3" apart)
// 240 leds per strip

public class PhotoBooth extends SLPattern {
    private final String[] cameras;
    private final Capture camera;
    private final PImage frame;

    private final PhotoBoothWall wall;

    private int width;
    private int height;

    public final CompoundParameter xScale = new CompoundParameter("xScale", 3, 0, 3);
    public final CompoundParameter yScale = new CompoundParameter("yScale", 3, 0, 3);

    // public final CompoundParameter xTrim = new CompoundParameter("xTrim", 0, -300, 300);
    // public final CompoundParameter yTrim = new CompoundParameter("yTrim", 0, -300, 300);

    public PhotoBooth(LX lx) {
        super(lx);
        addParameter(xScale);
        addParameter(yScale);
        // addParameter(xTrim);
        // addParameter(yTrim);

        this.wall = model.photoBoothWall;

        this.width  = (int)Math.abs(wall.xMax - wall.xMin);
        this.height = (int)Math.abs(wall.xMax - wall.xMin);

        this.cameras = Capture.list();
        this.camera = new Capture(applet, cameras[0]);
        camera.start();

        this.frame = createImage(width, height, RGB);
    }

    public void run(double deltaMs) {
        // int frameWidth  = (int)(camera.width  * xScale.getValuef() * 2);
        // int frameHeight = (int)(camera.height * yScale.getValuef() * 2);

        //frame.resize((int)Math.max(frameWidth, 1), (int)Math.max(frameHeight, 1));

        if (camera.available()) {
            for (int i = 0; i < frame.pixels.length; i++) {
                frame.pixels[i] = 0;
            }

            int sourceScaledX = camera.width; //(int)(camera.width * xScale.getValuef());
            int sourceScaledY = camera.height; //(int)(camera.height * yScale.getValuef());

            int destScaledX = width; //(int)(width * xScale.getValuef());
            int destScaledY = height; //(int)(height * yScale.getValuef());

            frame.copy(camera, 0, 0, sourceScaledX, sourceScaledY, 0, 0, destScaledX, destScaledY);
            frame.updatePixels();
            camera.read();
        }

        camera.loadPixels();
        frame.loadPixels();

        setColors(LXColor.RED);

        for (LXPoint p : wall.points) {
            int x = (int)((p.x / width)  * frame.width);
            int y = (int)((p.y / height) * frame.height);

            colors[p.index] = frame.get(x, (int)Math.abs(y - frame.height));
        }
    }
}

public class PhotoBoothDPat extends DPat {
    String[] cameras;
    Capture camera;
    PImage frame;

    private final PhotoBoothWall wall;

    public final CompoundParameter xScale = new CompoundParameter("xScale", 3, 0, 3);
    public final CompoundParameter yScale = new CompoundParameter("yScale", 3, 0, 3);

    public final CompoundParameter xTrim = new CompoundParameter("xTrim", 0, -300, 300);
    public final CompoundParameter yTrim = new CompoundParameter("yTrim", 0, -300, 300);

    private int width;
    private int height;

    public PhotoBoothDPat(LX lx) {
        super(lx);
        addParameter(xScale);
        addParameter(yScale);
        addParameter(xTrim);
        addParameter(yTrim);

        this.wall = model.photoBoothWall;


        this.width  = (int)Math.abs(wall.xMax - wall.xMin);
        this.height = (int)Math.abs(wall.xMax - wall.xMin);

        cameras = Capture.list();
        println("Available cameras: ");
        for (int i = 0; i < cameras.length; i++) {
            println(i + ": " + cameras[i]);
        }
        println(" ");

        camera = new Capture(applet, cameras[0]);
        println("Using camera: " + cameras[0]);
        camera.start();

        frame = createImage(width, height, RGB);
    }

    public void StartRun(double deltaMs) {
        int frameWidth  = (int)(camera.width  * xScale.getValuef() * 2);
        int frameHeight = (int)(camera.height * yScale.getValuef() * 2);

        //frame.resize((int)Math.max(frameWidth, 1), (int)Math.max(frameHeight, 1));

        if (camera.available()) {
            for (int i = 0; i < frame.pixels.length; i++) {
                frame.pixels[i] = 0;
            }

            int sourceScaledX = frameWidth; //(int)(camera.width * xScale.getValuef());
            int sourceScaledY = frameHeight; //(int)(camera.height * yScale.getValuef());

            int destScaledX = width; //(int)(width * xScale.getValuef());
            int destScaledY = height; //(int)(height * yScale.getValuef());

            frame.copy(camera, (int)xTrim.getValuef(), (int)yTrim.getValuef(), sourceScaledX, sourceScaledY, 0, 0, destScaledX, destScaledY);
            frame.updatePixels();
            camera.read();
        }

        camera.loadPixels();
        frame.loadPixels();

        setColors(0);
    }

    public color CalcPoint(PVector p) {
        int x = (int)((p.x / width)  * frame.width);
        int y = (int)((p.y / height) * frame.height);

        return frame.get(x, (int)Math.abs(y - frame.height));
    }
}