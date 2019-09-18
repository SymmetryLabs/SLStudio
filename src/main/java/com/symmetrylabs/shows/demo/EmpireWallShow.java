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
    	put("A", "192.168.0.100");
    	put("B", "192.168.0.101");
    	put("C", "192.168.0.102");
    	put("D", "192.168.0.103");
    	put("E", "192.168.0.104");	
    	put("F", "192.168.0.105");
    	put("G", "192.168.0.106");
    	put("H", "192.168.0.107");
    	put("I", "192.168.0.108");
    	put("J", "192.168.0.109");
    	put("K", "192.168.0.110");
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

		new VineConfig("A2",
			new LeafConfig[] {
				new LeafConfig(0, 0, 0, 0, 0, 0),
				new LeafConfig(10, 0, 0, 0, 0, 0),
				new LeafConfig(20, 0, 0, 0, 0, 0),
				new LeafConfig(30, 0, 0, 0, 0, 0),
				new LeafConfig(40, 0, 0, 0, 0, 0),
				new LeafConfig(50, 0, 0, 0, 0, 0),
				new LeafConfig(60, 0, 0, 0, 0, 0),
				new LeafConfig(70, 0, 0, 0, 0, 0),
				new LeafConfig(80, 0, 0, 0, 0, 0),
				new LeafConfig(90, 0, 0, 0, 0, 0),
				new LeafConfig(100, 0, 0, 0, 0, 0),
				new LeafConfig(110, 0, 0, 0, 0, 0),
				new LeafConfig(120, 0, 0, 0, 0, 0),
				new LeafConfig(130, 0, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("A4",
			new LeafConfig[] {
				new LeafConfig(0, 10, 0, 0, 0, 0),
				new LeafConfig(10, 10, 0, 0, 0, 0),
				new LeafConfig(20, 10, 0, 0, 0, 0),
				new LeafConfig(30, 10, 0, 0, 0, 0),
				new LeafConfig(40, 10, 0, 0, 0, 0),
				new LeafConfig(50, 10, 0, 0, 0, 0),
				new LeafConfig(60, 10, 0, 0, 0, 0),
				new LeafConfig(70, 10, 0, 0, 0, 0),
				new LeafConfig(80, 10, 0, 0, 0, 0),
				new LeafConfig(90, 10, 0, 0, 0, 0),
				new LeafConfig(100, 10, 0, 0, 0, 0),
				new LeafConfig(110, 10, 0, 0, 0, 0),
				new LeafConfig(120, 10, 0, 0, 0, 0),
				new LeafConfig(130, 10, 0, 0, 0, 0),
				new LeafConfig(140, 10, 0, 0, 0, 0),
				new LeafConfig(150, 10, 0, 0, 0, 0),
				new LeafConfig(160, 10, 0, 0, 0, 0),
				new LeafConfig(170, 10, 0, 0, 0, 0),
				new LeafConfig(180, 10, 0, 0, 0, 0),
				new LeafConfig(190, 10, 0, 0, 0, 0),
				new LeafConfig(200, 10, 0, 0, 0, 0),
				new LeafConfig(210, 10, 0, 0, 0, 0),
				new LeafConfig(220, 10, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("B2",
			new LeafConfig[] {
				new LeafConfig(0, 20, 0, 0, 0, 0),
				new LeafConfig(10, 20, 0, 0, 0, 0),
				new LeafConfig(20, 20, 0, 0, 0, 0),
				new LeafConfig(30, 20, 0, 0, 0, 0),
				new LeafConfig(40, 20, 0, 0, 0, 0),
				new LeafConfig(50, 20, 0, 0, 0, 0),
				new LeafConfig(60, 20, 0, 0, 0, 0),
				new LeafConfig(70, 20, 0, 0, 0, 0),
				new LeafConfig(80, 20, 0, 0, 0, 0),
				new LeafConfig(90, 20, 0, 0, 0, 0),
				new LeafConfig(100, 20, 0, 0, 0, 0),
				new LeafConfig(110, 20, 0, 0, 0, 0),
				new LeafConfig(120, 20, 0, 0, 0, 0),
				new LeafConfig(130, 20, 0, 0, 0, 0),
				new LeafConfig(140, 20, 0, 0, 0, 0),
				new LeafConfig(150, 20, 0, 0, 0, 0),
				new LeafConfig(160, 20, 0, 0, 0, 0),
				new LeafConfig(170, 20, 0, 0, 0, 0),
				new LeafConfig(180, 20, 0, 0, 0, 0),
				new LeafConfig(190, 20, 0, 0, 0, 0),
				new LeafConfig(200, 20, 0, 0, 0, 0),
				new LeafConfig(210, 20, 0, 0, 0, 0),
				new LeafConfig(220, 20, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("B3",
			new LeafConfig[] {
				new LeafConfig(0, 30, 0, 0, 0, 0),
				new LeafConfig(10, 30, 0, 0, 0, 0),
				new LeafConfig(20, 30, 0, 0, 0, 0),
				new LeafConfig(30, 30, 0, 0, 0, 0),
				new LeafConfig(40, 30, 0, 0, 0, 0),
				new LeafConfig(50, 30, 0, 0, 0, 0),
				new LeafConfig(60, 30, 0, 0, 0, 0),
				new LeafConfig(70, 30, 0, 0, 0, 0),
				new LeafConfig(80, 30, 0, 0, 0, 0),
				new LeafConfig(90, 30, 0, 0, 0, 0),
				new LeafConfig(100, 30, 0, 0, 0, 0),
				new LeafConfig(110, 30, 0, 0, 0, 0),
				new LeafConfig(120, 30, 0, 0, 0, 0),
				new LeafConfig(130, 30, 0, 0, 0, 0),
				new LeafConfig(140, 30, 0, 0, 0, 0),
				new LeafConfig(150, 30, 0, 0, 0, 0),
				new LeafConfig(160, 30, 0, 0, 0, 0),
				new LeafConfig(170, 30, 0, 0, 0, 0),
				new LeafConfig(180, 30, 0, 0, 0, 0),
				new LeafConfig(190, 30, 0, 0, 0, 0),
				new LeafConfig(200, 30, 0, 0, 0, 0),
				new LeafConfig(210, 30, 0, 0, 0, 0),
				new LeafConfig(220, 30, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("C1",
			new LeafConfig[] {
				new LeafConfig(0, 40, 0, 0, 0, 0),
				new LeafConfig(10, 40, 0, 0, 0, 0),
				new LeafConfig(20, 40, 0, 0, 0, 0),
				new LeafConfig(30, 40, 0, 0, 0, 0),
				new LeafConfig(40, 40, 0, 0, 0, 0),
				new LeafConfig(50, 40, 0, 0, 0, 0),
				new LeafConfig(60, 40, 0, 0, 0, 0),
				new LeafConfig(70, 40, 0, 0, 0, 0),
				new LeafConfig(80, 40, 0, 0, 0, 0),
				new LeafConfig(90, 40, 0, 0, 0, 0),
				new LeafConfig(100, 40, 0, 0, 0, 0),
				new LeafConfig(110, 40, 0, 0, 0, 0),
				new LeafConfig(120, 40, 0, 0, 0, 0),
				new LeafConfig(130, 40, 0, 0, 0, 0),
				new LeafConfig(140, 40, 0, 0, 0, 0),
				new LeafConfig(150, 40, 0, 0, 0, 0),
				new LeafConfig(160, 40, 0, 0, 0, 0),
				new LeafConfig(170, 40, 0, 0, 0, 0),
				new LeafConfig(180, 40, 0, 0, 0, 0),
				new LeafConfig(190, 40, 0, 0, 0, 0),
				new LeafConfig(200, 40, 0, 0, 0, 0),
				new LeafConfig(210, 40, 0, 0, 0, 0),
				new LeafConfig(220, 40, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("D1",
			new LeafConfig[] {
				new LeafConfig(0, 50, 0, 0, 0, 0),
				new LeafConfig(10, 50, 0, 0, 0, 0),
				new LeafConfig(20, 50, 0, 0, 0, 0),
				new LeafConfig(30, 50, 0, 0, 0, 0),
				new LeafConfig(40, 50, 0, 0, 0, 0),
				new LeafConfig(50, 50, 0, 0, 0, 0),
				new LeafConfig(60, 50, 0, 0, 0, 0),
				new LeafConfig(70, 50, 0, 0, 0, 0),
				new LeafConfig(80, 50, 0, 0, 0, 0),
				new LeafConfig(90, 50, 0, 0, 0, 0),
				new LeafConfig(100, 50, 0, 0, 0, 0),
				new LeafConfig(110, 50, 0, 0, 0, 0),
				new LeafConfig(120, 50, 0, 0, 0, 0),
				new LeafConfig(130, 50, 0, 0, 0, 0),
				new LeafConfig(140, 50, 0, 0, 0, 0),
				new LeafConfig(150, 50, 0, 0, 0, 0),
				new LeafConfig(160, 50, 0, 0, 0, 0),
				new LeafConfig(170, 50, 0, 0, 0, 0),
				new LeafConfig(180, 50, 0, 0, 0, 0),
				new LeafConfig(190, 50, 0, 0, 0, 0),
				new LeafConfig(200, 50, 0, 0, 0, 0),
				new LeafConfig(210, 50, 0, 0, 0, 0),
				new LeafConfig(220, 50, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("D2",
			new LeafConfig[] {
				new LeafConfig(0, 60, 0, 0, 0, 0),
				new LeafConfig(10, 60, 0, 0, 0, 0),
				new LeafConfig(20, 60, 0, 0, 0, 0),
				new LeafConfig(30, 60, 0, 0, 0, 0),
				new LeafConfig(40, 60, 0, 0, 0, 0),
				new LeafConfig(50, 60, 0, 0, 0, 0),
				new LeafConfig(60, 60, 0, 0, 0, 0),
				new LeafConfig(70, 60, 0, 0, 0, 0),
				new LeafConfig(80, 60, 0, 0, 0, 0),
				new LeafConfig(90, 60, 0, 0, 0, 0),
				new LeafConfig(100, 60, 0, 0, 0, 0),
				new LeafConfig(110, 60, 0, 0, 0, 0),
				new LeafConfig(120, 60, 0, 0, 0, 0),
				new LeafConfig(130, 60, 0, 0, 0, 0),
				new LeafConfig(140, 60, 0, 0, 0, 0),
				new LeafConfig(150, 60, 0, 0, 0, 0),
				new LeafConfig(160, 60, 0, 0, 0, 0),
				new LeafConfig(170, 60, 0, 0, 0, 0),
				new LeafConfig(180, 60, 0, 0, 0, 0),
				new LeafConfig(190, 60, 0, 0, 0, 0),
				new LeafConfig(200, 60, 0, 0, 0, 0),
				new LeafConfig(210, 60, 0, 0, 0, 0),
				new LeafConfig(220, 60, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("D4",
			new LeafConfig[] {
				new LeafConfig(0, 70, 0, 0, 0, 0),
				new LeafConfig(10, 70, 0, 0, 0, 0),
				new LeafConfig(20, 70, 0, 0, 0, 0),
				new LeafConfig(30, 70, 0, 0, 0, 0),
				new LeafConfig(40, 70, 0, 0, 0, 0),
				new LeafConfig(50, 70, 0, 0, 0, 0),
				new LeafConfig(60, 70, 0, 0, 0, 0),
				new LeafConfig(70, 70, 0, 0, 0, 0),
				new LeafConfig(80, 70, 0, 0, 0, 0),
				new LeafConfig(90, 70, 0, 0, 0, 0),
				new LeafConfig(100, 70, 0, 0, 0, 0),
				new LeafConfig(110, 70, 0, 0, 0, 0),
				new LeafConfig(120, 70, 0, 0, 0, 0),
				new LeafConfig(130, 70, 0, 0, 0, 0),
				new LeafConfig(140, 70, 0, 0, 0, 0),
				new LeafConfig(150, 70, 0, 0, 0, 0),
				new LeafConfig(160, 70, 0, 0, 0, 0),
				new LeafConfig(170, 70, 0, 0, 0, 0),
				new LeafConfig(180, 70, 0, 0, 0, 0),
				new LeafConfig(190, 70, 0, 0, 0, 0),
				new LeafConfig(200, 70, 0, 0, 0, 0),
				new LeafConfig(210, 70, 0, 0, 0, 0),
				new LeafConfig(220, 70, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("E1",
			new LeafConfig[] {
				new LeafConfig(0, 80, 0, 0, 0, 0),
				new LeafConfig(10, 80, 0, 0, 0, 0),
				new LeafConfig(20, 80, 0, 0, 0, 0),
				new LeafConfig(30, 80, 0, 0, 0, 0),
				new LeafConfig(40, 80, 0, 0, 0, 0),
				new LeafConfig(50, 80, 0, 0, 0, 0),
				new LeafConfig(60, 80, 0, 0, 0, 0),
				new LeafConfig(70, 80, 0, 0, 0, 0),
				new LeafConfig(80, 80, 0, 0, 0, 0),
				new LeafConfig(90, 80, 0, 0, 0, 0),
				new LeafConfig(100, 80, 0, 0, 0, 0),
				new LeafConfig(110, 80, 0, 0, 0, 0),
				new LeafConfig(120, 80, 0, 0, 0, 0),
				new LeafConfig(130, 80, 0, 0, 0, 0),
				new LeafConfig(140, 80, 0, 0, 0, 0),
				new LeafConfig(150, 80, 0, 0, 0, 0),
				new LeafConfig(160, 80, 0, 0, 0, 0),
				new LeafConfig(170, 80, 0, 0, 0, 0),
				new LeafConfig(180, 80, 0, 0, 0, 0),
				new LeafConfig(190, 80, 0, 0, 0, 0),
				new LeafConfig(200, 80, 0, 0, 0, 0),
				new LeafConfig(210, 80, 0, 0, 0, 0),
				new LeafConfig(220, 80, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("E3",
			new LeafConfig[] {
				new LeafConfig(0, 90, 0, 0, 0, 0),
				new LeafConfig(10, 90, 0, 0, 0, 0),
				new LeafConfig(20, 90, 0, 0, 0, 0),
				new LeafConfig(30, 90, 0, 0, 0, 0),
				new LeafConfig(40, 90, 0, 0, 0, 0),
				new LeafConfig(50, 90, 0, 0, 0, 0),
				new LeafConfig(60, 90, 0, 0, 0, 0),
				new LeafConfig(70, 90, 0, 0, 0, 0),
				new LeafConfig(80, 90, 0, 0, 0, 0),
				new LeafConfig(90, 90, 0, 0, 0, 0),
				new LeafConfig(100, 90, 0, 0, 0, 0),
				new LeafConfig(110, 90, 0, 0, 0, 0),
				new LeafConfig(120, 90, 0, 0, 0, 0),
				new LeafConfig(130, 90, 0, 0, 0, 0),
				new LeafConfig(140, 90, 0, 0, 0, 0),
				new LeafConfig(150, 90, 0, 0, 0, 0),
				new LeafConfig(160, 90, 0, 0, 0, 0),
				new LeafConfig(170, 90, 0, 0, 0, 0),
				new LeafConfig(180, 90, 0, 0, 0, 0),
				new LeafConfig(190, 90, 0, 0, 0, 0),
				new LeafConfig(200, 90, 0, 0, 0, 0),
				new LeafConfig(210, 90, 0, 0, 0, 0),
				new LeafConfig(220, 90, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("E4",
			new LeafConfig[] {
				new LeafConfig(0, 100, 0, 0, 0, 0),
				new LeafConfig(10, 100, 0, 0, 0, 0),
				new LeafConfig(20, 100, 0, 0, 0, 0),
				new LeafConfig(30, 100, 0, 0, 0, 0),
				new LeafConfig(40, 100, 0, 0, 0, 0),
				new LeafConfig(50, 100, 0, 0, 0, 0),
				new LeafConfig(60, 100, 0, 0, 0, 0),
				new LeafConfig(70, 100, 0, 0, 0, 0),
				new LeafConfig(80, 100, 0, 0, 0, 0),
				new LeafConfig(90, 100, 0, 0, 0, 0),
				new LeafConfig(100, 100, 0, 0, 0, 0),
				new LeafConfig(110, 100, 0, 0, 0, 0),
				new LeafConfig(120, 100, 0, 0, 0, 0),
				new LeafConfig(130, 100, 0, 0, 0, 0),
				new LeafConfig(140, 100, 0, 0, 0, 0),
				new LeafConfig(150, 100, 0, 0, 0, 0),
				new LeafConfig(160, 100, 0, 0, 0, 0),
				new LeafConfig(170, 100, 0, 0, 0, 0),
				new LeafConfig(180, 100, 0, 0, 0, 0),
				new LeafConfig(190, 100, 0, 0, 0, 0),
				new LeafConfig(200, 100, 0, 0, 0, 0),
				new LeafConfig(210, 100, 0, 0, 0, 0),
				new LeafConfig(220, 100, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("F3",
			new LeafConfig[] {
				new LeafConfig(0, 110, 0, 0, 0, 0),
				new LeafConfig(10, 110, 0, 0, 0, 0),
				new LeafConfig(20, 110, 0, 0, 0, 0),
				new LeafConfig(30, 110, 0, 0, 0, 0),
				new LeafConfig(40, 110, 0, 0, 0, 0),
				new LeafConfig(50, 110, 0, 0, 0, 0),
				new LeafConfig(60, 110, 0, 0, 0, 0),
				new LeafConfig(70, 110, 0, 0, 0, 0),
				new LeafConfig(80, 110, 0, 0, 0, 0),
				new LeafConfig(90, 110, 0, 0, 0, 0),
				new LeafConfig(100, 110, 0, 0, 0, 0),
				new LeafConfig(110, 110, 0, 0, 0, 0),
				new LeafConfig(120, 110, 0, 0, 0, 0),
				new LeafConfig(130, 110, 0, 0, 0, 0),
				new LeafConfig(140, 110, 0, 0, 0, 0),
				new LeafConfig(150, 110, 0, 0, 0, 0),
				new LeafConfig(160, 110, 0, 0, 0, 0),
				new LeafConfig(170, 110, 0, 0, 0, 0),
				new LeafConfig(180, 110, 0, 0, 0, 0),
				new LeafConfig(190, 110, 0, 0, 0, 0),
				new LeafConfig(200, 110, 0, 0, 0, 0),
				new LeafConfig(210, 110, 0, 0, 0, 0),
				new LeafConfig(220, 110, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("F4",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("G1",
			new LeafConfig[] {
				new LeafConfig(0, 130, 0, 0, 0, 0),
				new LeafConfig(10, 130, 0, 0, 0, 0),
				new LeafConfig(20, 130, 0, 0, 0, 0),
				new LeafConfig(30, 130, 0, 0, 0, 0),
				new LeafConfig(40, 130, 0, 0, 0, 0),
				new LeafConfig(50, 130, 0, 0, 0, 0),
				new LeafConfig(60, 130, 0, 0, 0, 0),
				new LeafConfig(70, 130, 0, 0, 0, 0),
				new LeafConfig(80, 130, 0, 0, 0, 0),
				new LeafConfig(90, 130, 0, 0, 0, 0),
				new LeafConfig(100, 130, 0, 0, 0, 0),
				new LeafConfig(110, 130, 0, 0, 0, 0),
				new LeafConfig(120, 130, 0, 0, 0, 0),
				new LeafConfig(130, 130, 0, 0, 0, 0),
				new LeafConfig(140, 130, 0, 0, 0, 0),
				new LeafConfig(150, 130, 0, 0, 0, 0),
				new LeafConfig(160, 130, 0, 0, 0, 0),
				new LeafConfig(170, 130, 0, 0, 0, 0),
				new LeafConfig(180, 130, 0, 0, 0, 0),
				new LeafConfig(190, 130, 0, 0, 0, 0),
				new LeafConfig(200, 130, 0, 0, 0, 0),
				new LeafConfig(210, 130, 0, 0, 0, 0),
				new LeafConfig(220, 130, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("G4",
			new LeafConfig[] {
				new LeafConfig(0, 140, 0, 0, 0, 0),
				new LeafConfig(10, 140, 0, 0, 0, 0),
				new LeafConfig(20, 140, 0, 0, 0, 0),
				new LeafConfig(30, 140, 0, 0, 0, 0),
				new LeafConfig(40, 140, 0, 0, 0, 0),
				new LeafConfig(50, 140, 0, 0, 0, 0),
				new LeafConfig(60, 140, 0, 0, 0, 0),
				new LeafConfig(70, 140, 0, 0, 0, 0),
				new LeafConfig(80, 140, 0, 0, 0, 0),
				new LeafConfig(90, 140, 0, 0, 0, 0),
				new LeafConfig(100, 140, 0, 0, 0, 0),
				new LeafConfig(110, 140, 0, 0, 0, 0),
				new LeafConfig(120, 140, 0, 0, 0, 0),
				new LeafConfig(130, 140, 0, 0, 0, 0),
				new LeafConfig(140, 140, 0, 0, 0, 0),
				new LeafConfig(150, 140, 0, 0, 0, 0),
				new LeafConfig(160, 140, 0, 0, 0, 0),
				new LeafConfig(170, 140, 0, 0, 0, 0),
				new LeafConfig(180, 140, 0, 0, 0, 0),
				new LeafConfig(190, 140, 0, 0, 0, 0),
				new LeafConfig(200, 140, 0, 0, 0, 0),
				new LeafConfig(210, 140, 0, 0, 0, 0),
				new LeafConfig(220, 140, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("H1",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("H2",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("H3",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("H4",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("I3",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("I4",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("J1",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("J2",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("J3",
			new LeafConfig[] {
				new LeafConfig(0, 120, 0, 0, 0, 0),
				new LeafConfig(10, 120, 0, 0, 0, 0),
				new LeafConfig(20, 120, 0, 0, 0, 0),
				new LeafConfig(30, 120, 0, 0, 0, 0),
				new LeafConfig(40, 120, 0, 0, 0, 0),
				new LeafConfig(50, 120, 0, 0, 0, 0),
				new LeafConfig(60, 120, 0, 0, 0, 0),
				new LeafConfig(70, 120, 0, 0, 0, 0),
				new LeafConfig(80, 120, 0, 0, 0, 0),
				new LeafConfig(90, 120, 0, 0, 0, 0),
				new LeafConfig(100, 120, 0, 0, 0, 0),
				new LeafConfig(110, 120, 0, 0, 0, 0),
				new LeafConfig(120, 120, 0, 0, 0, 0),
				new LeafConfig(130, 120, 0, 0, 0, 0),
				new LeafConfig(140, 120, 0, 0, 0, 0),
				new LeafConfig(150, 120, 0, 0, 0, 0),
				new LeafConfig(160, 120, 0, 0, 0, 0),
				new LeafConfig(170, 120, 0, 0, 0, 0),
				new LeafConfig(180, 120, 0, 0, 0, 0),
				new LeafConfig(190, 120, 0, 0, 0, 0),
				new LeafConfig(200, 120, 0, 0, 0, 0),
				new LeafConfig(210, 120, 0, 0, 0, 0),
				new LeafConfig(220, 120, 0, 0, 0, 0),
				new LeafConfig(140, 0, 0, 0, 0, 0),
				new LeafConfig(150, 0, 0, 0, 0, 0),
				new LeafConfig(160, 0, 0, 0, 0, 0),
				new LeafConfig(170, 0, 0, 0, 0, 0),
				new LeafConfig(180, 0, 0, 0, 0, 0),
				new LeafConfig(190, 0, 0, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

	};

	final StripConfig[] STRIPS_CONFIG = new StripConfig[] {

		/**
		 * Perimeter Strips!  
		 *
		 *         TL ➜     TM ➜      TR ←
		 *      --------- --------- ---------
		 *     |                             |
		 *   ↑ |                             | RU
		 *  LU |                             | ↓
		 *     |                             |
		 *
		 *   ↑ |                             | RL
		 *  LL |                             | ↓
		 *     |                             | 
		 *      --------- --------- ---------
		 *         BL →     BM ←      BR →
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

		// measurement to the start of the strip
		new StripConfig("B1", x(6),             y(WALL_HEIGHT-6), zRot(0),    101), // TL
		new StripConfig("A1", x(PANEL_WIDTH),   y(WALL_HEIGHT-6), zRot(0),    108), // TM
		new StripConfig("K2", x(PANEL_WIDTH*2),  y(WALL_HEIGHT-6), zRot(0),    100), // TR

		new StripConfig("I2", x(WALL_WIDTH-6),  y(WALL_HEIGHT-6), zRot(-90),   118), // RU
		new StripConfig("I1", x(WALL_WIDTH-6),  y(60),            zRot(-90),   83), // RL

		new StripConfig("G3", x(PANEL_WIDTH*2),   y(6),             zRot(0),    101), // BR
		new StripConfig("G2", x(PANEL_WIDTH*2),   y(6),           zRot(180),  108), // BM
		new StripConfig("E2", x(6),               y(6),             zRot(0),    101), // BL

		new StripConfig("D3", x(6),             y(6),             zRot(90),  82), // LL
		new StripConfig("C4", x(6),             y(60),            zRot(90),  119), // LU
	};	

	public SLModel buildModel() {
		List<Strip> strips = new ArrayList<>();
		for (StripConfig stripConfig : STRIPS_CONFIG) {
			LXTransform t = new LXTransform();
			t.push();
			t.translate(stripConfig.x, stripConfig.y, 2);
			t.rotateZ(stripConfig.zRot * PI / 180.f);

			Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, 0.65f);
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
	}

	@Override
	public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
	    UIVineWallModelingTool uivineWallModelingTool = UIVineWallModelingTool.getInstance(
	        lx, ui, VineWallModelingTool.getInstance(lx), 0, 0, ui.rightPane.model.getContentWidth());
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
