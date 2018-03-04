/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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
 */

package heronarts.lx.headless;

import java.io.File;
import heronarts.lx.LX;
import heronarts.lx.model.GridModel;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.output.LXDatagramOutput;

/**
 * Example headless CLI for the LX engine. Just write a bit of scaffolding code
 * to load your model, define your outputs, then we're off to the races.
 */
public class LXHeadless {

    public static LXModel buildModel() {
        // TODO: implement code that loads and builds your model here
        return new GridModel(10, 10);
    }

    public static void addArtNetOutput(LX lx) throws Exception {
        lx.engine.addOutput(
            new LXDatagramOutput(lx).addDatagram(
                new ArtNetDatagram(lx.model, 512, 0)
                .setAddress("localhost")
            )
        );
    }

    public static void addFadecandyOutput(LX lx) throws Exception {
        lx.engine.addOutput(new FadecandyOutput(lx, "localhost", 7890, lx.model));
    }

    public static void main(String[] args) {
        try {
            LXModel model = buildModel();
            LX lx = new LX(model);

            // TODO: add your own output code here
            // addArtNetOutput(lx);
             addFadecandyOutput(lx);

            // On the CLI you specify an argument with an .lxp file
            if (args.length > 0) {
                lx.openProject(new File(args[0]));
            }

            lx.engine.start();
            lx.engine.onDraw();
        } catch (Exception x) {
            System.err.println(x.getLocalizedMessage());
        }
    }
}
