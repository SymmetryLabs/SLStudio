// // 144 leds per meter
// // 16 vertical strips (3" apart)
// // 240 leds per strip

// public class PhotoBoothCamera extends DPat {
//     String[] cameras;
//     Capture camera;
//     PImage frame;

//     public final CompoundParameter xScale = new CompoundParameter("xScale", 3, 0, 3);
//     public final CompoundParameter yScale = new CompoundParameter("yScale", 3, 0, 3);

//     public final CompoundParameter xTrim = new CompoundParameter("xTrim", 0, -300, 300);
//     public final CompoundParameter yTrim = new CompoundParameter("yTrim", 0, -300, 300);

//     private int width  = (int)Math.abs(model.xMax - model.xMin);
//     private int height = (int)Math.abs(model.xMax - model.xMin);

//     public PhotoBoothCamera(LX lx) {
//         super(lx);
//         addParameter(xScale);
//         addParameter(yScale);
//         addParameter(xTrim);
//         addParameter(yTrim);

//         cameras = Capture.list();
//         println("Available cameras: ");
//         for (int i = 0; i < cameras.length; i++) {
//             println(i + ": " + cameras[i]);
//         }
//         println(" ");

//         camera = new Capture(applet, cameras[18]);
//         println("Using camera: " + cameras[18]);
//         camera.start();

//         frame = createImage(width, height, RGB);
//     }

//     public void StartRun(double deltaMs) {
//         int frameWidth  = (int)(camera.width  * xScale.getValuef() * 2);
//         int frameHeight = (int)(camera.height * yScale.getValuef() * 2);

//         //frame.resize((int)Math.max(frameWidth, 1), (int)Math.max(frameHeight, 1));

//         if (camera.available()) {
//             for (int i = 0; i < frame.pixels.length; i++) {
//                 frame.pixels[i] = 0;
//             }

//             int sourceScaledX = frameWidth; //(int)(camera.width * xScale.getValuef());
//             int sourceScaledY = frameHeight; //(int)(camera.height * yScale.getValuef());

//             int destScaledX = width; //(int)(width * xScale.getValuef());
//             int destScaledY = height; //(int)(height * yScale.getValuef());

//             frame.copy(camera, (int)xTrim.getValuef(), (int)yTrim.getValuef(), sourceScaledX, sourceScaledY, 0, 0, destScaledX, destScaledY);
//             frame.updatePixels();
//             camera.read();
//         }

//         camera.loadPixels();
//         frame.loadPixels();

//         setColors(0);
//     }

//     public color CalcPoint(PVector p) {
//         int x = (int)((p.x / width)  * frame.width);
//         int y = (int)((p.y / height) * frame.height);

//         return frame.get(x, y); //(int)Math.abs(y - frame.height));
//     }
// }