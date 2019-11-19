package com.symmetrylabs.shows.empirewall.ui;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;
import heronarts.lx.color.LXColor;
import static com.symmetrylabs.util.DistanceConstants.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import static processing.core.PConstants.*;
import processing.core.PGraphics;
import processing.core.PImage;
import com.symmetrylabs.shows.empirewall.*;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.slstudio.SLStudio;


public class UILeaves extends UI3dComponent {
  
  private final VineModel model;
  protected final PImage texImage;
  private Output output;
  
  public UILeaves(LX lx) {
    this.model = (VineModel) lx.model;
    this.texImage = SLStudio.applet.loadImage("leaf.png");
    this.output = new Output(lx);
    lx.addOutput(output);
  }
  
  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    //int[] colors = lx.getColors(); 
    pg.noStroke();
    pg.noFill();
    pg.textureMode(NORMAL);
    pg.beginShape(QUADS);
    pg.texture(this.texImage);
    int i = 0;
    for (TreeModel.Leaf leaf : model.leaves) {
      //System.out.println("drawing leaves2: " + i++ );
      if (output.colors != null) {
        pg.tint(output.colors[leaf.points[0].index]);
      }
      pg.vertex(leaf.coords[0].x, leaf.coords[0].y, leaf.coords[0].z, 0, 1);
      pg.vertex(leaf.coords[1].x, leaf.coords[1].y, leaf.coords[1].z, 0, 0);
      pg.vertex(leaf.coords[2].x, leaf.coords[2].y, leaf.coords[2].z, 1, 0);
      pg.vertex(leaf.coords[3].x, leaf.coords[3].y, leaf.coords[3].z, 1, 1);
    }
    pg.endShape(CLOSE);
    pg.noTexture();
    pg.noTint();
  }

  private static class Output extends LXOutput {
    public int[] colors;

    private Output(LX lx) {
        super(lx);
        enabled.setValue(true);
    }

    protected void onSend(int[] colors) {
      this.colors = colors;
    }
  }
}
