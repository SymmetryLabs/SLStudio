package com.symmetrylabs.shows.empirewall;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.empirewall.config.*;
import com.symmetrylabs.shows.empirewall.ui.*;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;


public class EmpireWallShow implements Show {
	public final static String SHOW_NAME = "empirewall";

	final Map<String, String> ipAddresses = new HashMap<String, String>() {{
    	put("A", "192.168.1.100");
    	put("B", "192.168.1.101");
    	put("C", "192.168.1.102");
    	put("D", "192.168.1.103");
    	put("E", "192.168.1.104");	
    	put("F", "192.168.1.105");
    	put("G", "192.168.1.106");
    	put("H", "192.168.1.107");
    	put("I", "192.168.1.108");
    	put("J", "192.168.1.109");
    	put("K", "192.168.1.110");
	}};

	final static float WALL_HEIGHT   = FEET * 12.f;
	final static float WALL_WIDTH    = FEET * 18.f;
	final static float PANEL_WIDTH   = WALL_WIDTH / 3.f;
	final static float PANEL_HEIGHT  = WALL_HEIGHT;

	final Map<String, VineModel.Vine> vineMap = new HashMap<>();
	final Map<String, Strip> stripMap = new HashMap<>();

	final VineConfig[] VINES_CONFIG = new VineConfig[] {

		/**
		 * Quadrants!
		 *
		 * The wall can be broken in to four quadrants, top left, top right, bottom left, and bottom right.
		 * The origin of each quadrant is it's corner of the wall. The quadrant a vine belongs is determined 
		 * by the position of the vines hole on the 2x8 frame. The postiion of the vine's leaves are then measured 
		 * relative to the origin of the quadrant the vine belongs. 
		 * 
		 * TOP LEFT:     x goes to the right,  y goes down,  z goes away from the wall
		 * TOP RIGHT:    x goes to the left,   y goes down,  z goes away from the wall
		 * BOTTOM LEFT:  x goes to the right,  y goes up,    z goes away from the wall
		 * BOTTOM RIGHT: x goes to the left,   y goes up,    z goes away from the wall
		 */

		/**
		 * Vine Config
		 *
		 * String: Pixlite Id / Pixlite Port (A-K / 1-4) Example: D3
		 * Leaf Configs (x position, y position, z position, x rotation, y rotation, z rotation)
		 */
//NO VINE YET
		new VineConfig("A1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),
//MAPPED
		new VineConfig("A2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),
//NO VINE YET
		new VineConfig("A3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),
//NO VINE YET
		new VineConfig("A4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("B1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("B2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("B3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("B4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("C1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("C2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("C3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("C4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("D1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("D2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("D3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("D4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("E1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("E2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("E3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("E4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("G1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("G2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("G3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("G4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("H1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("H2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("H3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("H4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("I1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("I2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("I3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("I4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),
//MAPPED
		new VineConfig("J1",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),
//MAPPED
		new VineConfig("J2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),

		new VineConfig("J3",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),
	
		new VineConfig("J4",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(0, 0, 0, 0, 0, 0)
			}
		),
	};

	final float STRIP_LED_PITCH = 1.3f;

	final StripConfig[] STRIPS_CONFIG = new StripConfig[] {

		/**
		 * New Perimeter Strips!  
		 *
		 *           TL ←           TR → 
		 *      -------------- --------------
		 *     |                             |
		 *     |                             |
		 *     |                             |
		 *   ↑ |                             | R
		 *   L |                             | ↓
		 *     |                             |
		 *     |                             |
		 *     |                             | 
		 *      -------------- --------------
		 *           BL ←            BR →
		 */

		/**
		 * Strip Config
		 *
		 * String: Pixlite Id / Pixlite Port (A-K / 1-4) Example: D3
	     * Float: x position
	     * Float: y position
	     * Float: z rotation
	     * Int: number of leds
		 */

		new StripConfig("F1", x(             6), y(6), zRot( 90), 101), // L
		new StripConfig("F2", x(WALL_WIDTH/2.f), y(6), zRot(180),  79), // BL
		new StripConfig("F3", x(WALL_WIDTH/2.f), y(6), zRot(  0),  79), // BR
		new StripConfig("F4", x(WALL_WIDTH-6.f), y(6), zRot( 90), 101), // R
		new StripConfig("K4", x(WALL_WIDTH/2.f), y(WALL_HEIGHT-6), zRot(  0),  79), // TR
		new StripConfig("K1", x(WALL_WIDTH/2.f), y(WALL_HEIGHT-6), zRot(180),  79), // TL

		// // measurement to the start of the strip
		// new StripConfig("B1", x(6),             y(WALL_HEIGHT-6), zRot(0),    101), // TL
		// new StripConfig("A1", x(PANEL_WIDTH),   y(WALL_HEIGHT-6), zRot(0),    108), // TM
		// new StripConfig("K2", x(PANEL_WIDTH*2),  y(WALL_HEIGHT-6), zRot(0),    100), // TR

		// new StripConfig("I2", x(WALL_WIDTH-6),  y(WALL_HEIGHT-6), zRot(-90),   118), // RU
		// new StripConfig("I1", x(WALL_WIDTH-6),  y(60),            zRot(-90),   83), // RL

		// new StripConfig("G3", x(PANEL_WIDTH*2),   y(6),             zRot(0),    101), // BR
		// new StripConfig("G2", x(PANEL_WIDTH*2),   y(6),           zRot(180),  108), // BM
		// new StripConfig("E2", x(6),               y(6),             zRot(0),    101), // BL

		// new StripConfig("D3", x(6),             y(6),             zRot(90),  82), // LL
		// new StripConfig("C4", x(6),             y(60),            zRot(90),  119), // LU
	};	

	public SLModel buildModel() {
		List<Strip> strips = new ArrayList<>();
		for (StripConfig stripConfig : STRIPS_CONFIG) {
			LXTransform t = new LXTransform();
			t.push();
			t.translate(stripConfig.x, stripConfig.y, 2);
			t.rotateZ(stripConfig.zRot * PI / 180.f);

			Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, STRIP_LED_PITCH);
			Strip strip = new Strip(stripConfig.id, metrics, t);
			stripMap.put(stripConfig.id, strip);
			strips.add(strip);

			t.pop();
		}

		return new VineModel(SHOW_NAME, new VineWallConfig(VINES_CONFIG), strips);
	}

	public void setupLx(final LX lx) {
		//try {
			final Map<String, SimplePixlite> pixlites = new HashMap<String, SimplePixlite>() {{
			    put("A", new SimplePixlite(lx, ipAddresses.get("A")));
			    put("B", new SimplePixlite(lx, ipAddresses.get("B")));
			    put("C", new SimplePixlite(lx, ipAddresses.get("C")));
			    put("D", new SimplePixlite(lx, ipAddresses.get("D")));
			    put("E", new SimplePixlite(lx, ipAddresses.get("E")));
			    put("F", new SimplePixlite(lx, ipAddresses.get("F")));
			    put("G", new SimplePixlite(lx, ipAddresses.get("G")));
			    put("H", new SimplePixlite(lx, ipAddresses.get("H")));
			    put("I", new SimplePixlite(lx, ipAddresses.get("I")));
			    put("J", new SimplePixlite(lx, ipAddresses.get("J")));
			    put("K", new SimplePixlite(lx, ipAddresses.get("K")));
			}};

			VineModel model = (VineModel) lx.model;
			System.out.println("Setting up Pixlite outputs...");

			// vines
			VineConfig[] vineConfigs = model.getConfig().getVinesArray();
			int vineIndex = 0;

			for (VineConfig config : vineConfigs) {
				SimplePixlite pixlite = pixlites.get(Character.toString(config.id.charAt(0)));
				String output = Character.toString(config.id.charAt(1));
				System.out.println("Pixlite: " + pixlite.ipAddress + ", output: " + output + "(vine)");
				pixlite.addPixliteOutput(output, new PointsGrouping(model.vines.get(vineIndex++).points));
			}
			System.out.println("");

			// Strips
			int stripIndex = 0;
			for (String id : stripMap.keySet()) {
				SimplePixlite pixlite = pixlites.get(Character.toString(id.charAt(0)));
				String output = Character.toString(id.charAt(1));
				System.out.println("Pixlite: " + pixlite.ipAddress + ", output: " + output + " (strip)");

				pixlite.addPixliteOutput(output, new PointsGrouping(stripMap.get(id).points));
			}
			System.out.println("");

			for (String id : pixlites.keySet()) {
				SimplePixlite pixlite = pixlites.get(id);
				pixlite.enabled.setValue(true);
				lx.addOutput(pixlite);
			}


		//} catch (SocketException e) { }

		lx.engine.registerComponent("vineModelingTool", VineWallModelingTool.getInstance(lx, true));
	
		MotionSensor motionSensor = MotionSensor.initialize("192.168.1.50");
		lx.engine.addLoopTask(motionSensor);
		// motionSensor.addListener(() -> {
		// 	Channel channel = lx.engine.getChannel("Motion");
		// });
	}

	@Override
	public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
	    UIVineWallModelingTool uivineWallModelingTool = UIVineWallModelingTool.getInstance(
	        lx, ui, VineWallModelingTool.getInstance(lx), 0, 0, ui.rightPane.model.getContentWidth()
	    );
	    uivineWallModelingTool.addToContainer(ui.rightPane.model);

	    ui.preview.addComponent(new UIVineWall());
	    ui.preview.addComponent(new UILeaves(lx));
	}


	// static class VineConfig {
	// 	String id;
	// 	Quadrant quadrant;
	// 	List<LeafConfig> leaves = new ArrayList<LeafConfig>();

	// 	public VineConfig(String id, Quadrant quadrant, LeafConfig[] leaves) {
	// 		this.id = id;
	// 		this.leaves = Arrays.asList(leaves);
	// 	}
	// }

	static class StripConfig {
		String id;
		float x;
		float y;
		float zRot;
		int numPoints;

		StripConfig(String id, float x, float y, float zRot, int numPoints) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.zRot = zRot;
			this.numPoints = numPoints;
		}
	}

	private float x(float x) { return x; };
	private float y(float y) { return y; };
	private float z(float z) { return z; };
	private float xRot(float xRot) { return xRot; };
	private float yRot(float yRot) { return yRot; };
	private float zRot(float zRot) { return zRot; };
}
