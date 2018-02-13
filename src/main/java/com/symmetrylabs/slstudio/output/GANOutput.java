package com.symmetrylabs.slstudio.output;

import afu.org.checkerframework.checker.oigj.qual.O;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXOutput;
import com.symmetrylabs.slstudio.model.SLModel;

public class GANOutput extends LXOutput {

    final LX lx;

    public GANOutput(LX lx) {
            super(lx);
            this.lx = lx;
        }
        @Override
        protected void onSend(int[] colors) {

            final BufferItem[] items = new BufferItem[lx.model.points.length];
            System.out.println("printing lx model length!");
            System.out.println(lx.model.points.length);
            int i = 0;
            for (LXPoint p : lx.model.points) {
                items[i++] = new BufferItem(p.x, p.y, p.z, colors[p.index]);
            }
        }

    }

    class BufferItem {

      float x;
        float y;
        float z;
        int col;

        public BufferItem(float x, float y, float z, int col) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.col = col;
        }


     }

