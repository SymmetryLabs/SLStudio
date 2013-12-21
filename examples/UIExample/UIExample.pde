import heronarts.lx.*;
import heronarts.lx.ui.*;
import ddf.minim.*;

String title = "";
UIWindow window;

void setup() {
  size(400, 400);
  LX lx = new LX(this);
  lx.ui.addContext(window = new UIWindow(lx.ui, "TEST", 10, 10, 100, 300));
}

void draw() {
}

void keyPressed() {
  title = title + new String(new char[] {key });
  if (keyCode == BACKSPACE) {
    title = "";
  }
  window.redraw();
  window.setTitle(title);
}
