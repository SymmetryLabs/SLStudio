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
		 * by the position of the vines hole on the 2x8 frame. The postiion of the vine's leaves2 are then measured
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

		// new VineConfig("A2",
		// 	new LeafConfig[] {
		// 		new LeafConfig(0, 0, 0, 0, 0, 0),
		// 		new LeafConfig(10, 0, 0, 0, 0, 0),
		// 		new LeafConfig(20, 0, 0, 0, 0, 0),
		// 		new LeafConfig(30, 0, 0, 0, 0, 0),
		// 		new LeafConfig(40, 0, 0, 0, 0, 0),
		// 		new LeafConfig(50, 0, 0, 0, 0, 0),
		// 		new LeafConfig(60, 0, 0, 0, 0, 0),
		// 		new LeafConfig(70, 0, 0, 0, 0, 0),
		// 		new LeafConfig(80, 0, 0, 0, 0, 0),
		// 		new LeafConfig(90, 0, 0, 0, 0, 0),
		// 		new LeafConfig(100, 0, 0, 0, 0, 0),
		// 		new LeafConfig(110, 0, 0, 0, 0, 0),
		// 		new LeafConfig(120, 0, 0, 0, 0, 0),
		// 		new LeafConfig(130, 0, 0, 0, 0, 0),
		// 		new LeafConfig(140, 0, 0, 0, 0, 0),
		// 		new LeafConfig(150, 0, 0, 0, 0, 0),
		// 		new LeafConfig(160, 0, 0, 0, 0, 0),
		// 		new LeafConfig(170, 0, 0, 0, 0, 0),
		// 		new LeafConfig(180, 0, 0, 0, 0, 0),
		// 		new LeafConfig(190, 0, 0, 0, 0, 0),
		// 		new LeafConfig(200, 0, 0, 0, 0, 0),
		// 		new LeafConfig(210, 0, 0, 0, 0, 0),
		// 		new LeafConfig(220, 0, 0, 0, 0, 0),
		// 		new LeafConfig(140, 0, 0, 0, 0, 0),
		// 		new LeafConfig(150, 0, 0, 0, 0, 0),
		// 		new LeafConfig(160, 0, 0, 0, 0, 0),
		// 		new LeafConfig(170, 0, 0, 0, 0, 0),
		// 		new LeafConfig(180, 0, 0, 0, 0, 0),
		// 		new LeafConfig(190, 0, 0, 0, 0, 0),
		// 		new LeafConfig(200, 0, 0, 0, 0, 0),
		// 		new LeafConfig(210, 0, 0, 0, 0, 0),
		// 		new LeafConfig(220, 0, 0, 0, 0, 0),
		// 	}
		// ),

		new VineConfig("A4",
			new LeafConfig[] {
				new LeafConfig(82, 144-8, 1, 0, 0, 0),
				new LeafConfig(77, 144-10, 4, 0, 0, 0),
				new LeafConfig(79, 144-13, 2, 0, 0, 0),
				new LeafConfig(94, 144-18, 4, 0, 0, 0),
				new LeafConfig(120, 144-12, 6, 0, 0, 0),
				new LeafConfig(114, 144-14, 7, 0, 0, 0),
				new LeafConfig(114, 144-18, 11, 0, 0, 0),
				new LeafConfig(98, 144-23, 8, 0, 0, 0),
				new LeafConfig(86, 144-22, 9, 0, 0, 0),
				new LeafConfig(82, 144-22, 8, 0, 0, 0),
				new LeafConfig(77, 144-19, 8, 0, 0, 0),
				new LeafConfig(65, 144-16, 4.5f, 0, 0, 0),
				new LeafConfig(62, 144-30, 4, 0, 0, 0),
				new LeafConfig(67, 144-31, 2, 0, 0, 0),
				new LeafConfig(64, 144-34, 4, 0, 0, 0),
			}
		),

		new VineConfig("B2",
			new LeafConfig[] {
				new LeafConfig(17, 144-8, 3, 0, 0, 0),
				new LeafConfig(22, 144-8, 3, 0, 0, 0),
				new LeafConfig(22, 144-5, 4, 0, 0, 0),
				new LeafConfig(30, 144-17, 7, 0, 0, 0),
				new LeafConfig(35, 144-18, 6, 0, 0, 0),
				new LeafConfig(37, 144-17, 5, 0, 0, 0),
				new LeafConfig(50, 144-20, 3, 0, 0, 0),
				new LeafConfig(51, 144-20, 9, 0, 0, 0),
				new LeafConfig(55, 144-12, 7, 0, 0, 0),
				new LeafConfig(65, 144-22, 7, 0, 0, 0),
				new LeafConfig(67, 144-30,3, 0, 0, 0),
				new LeafConfig(70, 144-31 ,4, 0, 0, 0),
				new LeafConfig(78, 144-23, 9, 0, 0, 0),
				new LeafConfig(81, 144-23, 8, 0, 0, 0),
				new LeafConfig(93, 144-20, 9, 0, 0, 0),
				new LeafConfig(108,144-24,4, 0, 0, 0),
				new LeafConfig(110, 144-29, 4, 0, 0, 0),
				new LeafConfig(123, 144-25,5, 0, 0, 0),
				new LeafConfig(131, 144-18, 7, 0, 0, 0),
				new LeafConfig(134, 144-18, 10, 0, 0, 0),
				new LeafConfig(120, 144-11, 5, 0, 0, 0),
				new LeafConfig(118, 144-8, 9.5f, 0, 0, 0),
				new LeafConfig(114, 144-12, 9, 0, 0, 0),
				new LeafConfig(100, 144-14, 8, 0, 0, 0),
				new LeafConfig(96, 144-21, 7, 0, 0, 0),
				new LeafConfig(92, 144-25, 5, 0, 0, 0),
			}
		),

		new VineConfig("B3",
			new LeafConfig[] {
				new LeafConfig(-5, 144-31,-1, 0, 0, 0),
				new LeafConfig(-2, 144-29, 0, 0, 0, 0),
				new LeafConfig(13, 144-36, 4, 0, 0, 0),
				new LeafConfig(20, 144-33, 3.5f, 0, 0, 0),
				new LeafConfig(28, 144-37, 3.5f, 0, 0, 0),
				new LeafConfig(32, 144-41, 6.5f, 0, 0, 0),
				new LeafConfig(40.5f, 144-29, 4.5f, 0, 0, 0),
				new LeafConfig(45.5f, 144-32, 6, 0, 0, 0),
				new LeafConfig(56, 144-29, 8, 0, 0, 0),
				new LeafConfig(60, 144-25, 3, 0, 0, 0),
				new LeafConfig(71, 144-16, 2, 0, 0, 0),
				new LeafConfig(73, 144-18, 1, 0, 0, 0),
				new LeafConfig(84, 144-21, 1, 0, 0, 0),
				new LeafConfig(88, 144-18, 2, 0, 0, 0),
				new LeafConfig(100, 144-25, 1, 0, 0, 0),
				new LeafConfig(103, 144-21, 5, 0, 0, 0),
				new LeafConfig(116, 144-22, 5, 0, 0, 0),
				new LeafConfig(118.5f, 144-18, 7, 0, 0, 0),
				new LeafConfig(130, 144-16, 3, 0, 0, 0),
				new LeafConfig(134, 144-20, 2, 0, 0, 0),
				new LeafConfig(131, 144-19, 2, 0, 0, 0),
				new LeafConfig(136, 144-22, 3, 0, 0, 0),
				new LeafConfig(133, 144-36, 5, 0, 0, 0),
				new LeafConfig(128, 144-34, 3, 0, 0, 0),
			}
		),

		new VineConfig("C1",
			new LeafConfig[] {
				new LeafConfig(0, 95, -2, 0, 0, 0),        //1
				new LeafConfig(3, 101, 1, 0, 0, 0),       //2
				new LeafConfig(9.5f, 99, 1.5f, 0, 0, 0),     //3
				new LeafConfig(16.5f, 105, 3, 0, 0, 0),	  //4	
				new LeafConfig(30.5f, 96, 6, 0, 0, 0),    //5
				new LeafConfig(38.5f, 100, 5, 0, 0, 0),    //6
				new LeafConfig(36.5f, 94, 6, 0, 0, 0),    //7
				new LeafConfig(21.5f, 93, 5, 0, 0, 0),    //8
				new LeafConfig(31.5f, 92, 5, 0, 0, 0),    //9
				new LeafConfig(34, 93.5f, 6, 0, 0, 0),       //10
				new LeafConfig(37, 95.5f, 5, 0, 0, 0),       //11
				new LeafConfig(51, 94.5f, 5.5f, 0, 0, 0),       //12
				new LeafConfig(67, 92.5f, 7, 0, 0, 0),       //13
				new LeafConfig(67, 91, 7, 0, 0, 0),       //14
				new LeafConfig(70, 90, 6, 0, 0, 0),       //15
				new LeafConfig(80, 101, 3, 0, 0, 0),       //16
				new LeafConfig(95, 100, 8, 0, 0, 0),       //17
				new LeafConfig(98.5f, 97, 2, 0, 0, 0),    //18
				new LeafConfig(96.5f, 100, 6, 0, 0, 0),    //19
				new LeafConfig(121.5f, 103, 6, 0, 0, 0),   //20
				new LeafConfig(117.5f, 104, 6, 0, 0, 0),   //21
				new LeafConfig(130.5f, 97, 7, 0, 0, 0),   //22
				new LeafConfig(145.5f, 97, 8, 0, 0, 0),   //23
				new LeafConfig(148.5f, 102, 9, 0, 0, 0),    //24
				new LeafConfig(150.5f, 97, 5, 0, 0, 0),    //25
				new LeafConfig(163.5f, 98, 7, 0, 0, 0),    //26
			}
		),

//d1 is missing leaves2
		new VineConfig("D1",
			new LeafConfig[] {
				new LeafConfig(2, 48, -6, 0, 0, 0),    //1
				new LeafConfig(-4, 44, -3, 0, 0, 0),   //2
				new LeafConfig(-3, 44, 2, 0, 0, 0),    //3
				new LeafConfig(10, 52, 3.5f, 0, 0, 0),    //4
				new LeafConfig(28, 67, 0, 0, 0, 0),    //5
				new LeafConfig(36, 71, 6, 0, 0, 0),    //6
				new LeafConfig(41, 68, 2, 0, 0, 0),    //7
				new LeafConfig(57, 68.5f, 3, 0, 0, 0),  //8
				new LeafConfig(70, 70.5f, 3.5f, 0, 0, 0),    //9
				new LeafConfig(72, 71, 4, 0, 0, 0),    //10
				new LeafConfig(71, 74, 2, 0, 0, 0),   //11
				new LeafConfig(84, 75, 3, 0, 0, 0),   //12
				new LeafConfig(97.5f, 61.5f, 3, 0, 0, 0),   //13
				new LeafConfig(100.5f, 54.5f, 2, 0, 0, 0),   //14
				new LeafConfig(103.5f, 58.5f, 4, 0, 0, 0),   //15
				new LeafConfig(129.5f, 52.5f, 4.5f, 0, 0, 0),   //16
				new LeafConfig(128.5f, 50.5f, 1, 0, 0, 0),   //17
				new LeafConfig(132.5f, 51.5f, 2, 0, 0, 0),   //18
				new LeafConfig(143.5f, 47.5f, 4, 0, 0, 0),   //19
				new LeafConfig(151.5f, 38.5f, 4, 0, 0, 0),   //20
				new LeafConfig(154.5f, 38.5f-4, 2.5f, 0, 0, 0),   //21
				new LeafConfig(154.5f+5f, 38.5f-4+4, 4, 0, 0, 0),   //22
				new LeafConfig(154.5f+5f+10, 38.5f-4+4-11, 2, 0, 0, 0),   //23
				new LeafConfig(154.5f+5f+10, 38.5f-4+4-11, 2, 0, 0, 0),   //23
				new LeafConfig(154.5f+5f+10, 38.5f-4+4-11, 2, 0, 0, 0),   //23
				new LeafConfig(154.5f+5f+10, 38.5f-4+4-11, 2, 0, 0, 0),   //23
				new LeafConfig(154.5f+5f+10, 38.5f-4+4-11, 2, 0, 0, 0),   //23
				new LeafConfig(154.5f+5f+10, 38.5f-4+4-11, 2, 0, 0, 0),   //23
				new LeafConfig(154.5f+5f+10, 38.5f-4+4-11, 2, 0, 0, 0),   //23
			}
		),

		new VineConfig("D2",
			new LeafConfig[] {
				new LeafConfig(-1, 57, 3, 0, 0, 0),  //1
				new LeafConfig(4, 56, 5, 0, 0, 0),   //2 
				new LeafConfig(11, 65, 3, 0, 0, 0),  //3
				new LeafConfig(12, 66, 0, 0, 0, 0),  //4
				new LeafConfig(23, 70, 5, 0, 0, 0),  //5
				new LeafConfig(36, 70, 5.5f, 0, 0, 0),  //6
				new LeafConfig(38, 75, 10, 0, 0, 0),  //7
				new LeafConfig(50, 76, 7, 0, 0, 0),  //8
				new LeafConfig(53, 79, 3, 0, 0, 0),  //9
				new LeafConfig(64, 79, 2, 0, 0, 0),  //10
				new LeafConfig(66, 78, 5, 0, 0, 0), //11
				new LeafConfig(79, 73, 6, 0, 0, 0), //12
				new LeafConfig(77, 78, 6.5f, 0, 0, 0), //13
				new LeafConfig(82, 69, 4, 0, 0, 0), //14
				new LeafConfig(87.5f, 69, 6, 0, 0, 0), //15
				new LeafConfig(83.5f, 55, 7, 0, 0, 0), //16
				new LeafConfig(89.5f, 60, 5, 0, 0, 0), //17
				new LeafConfig(91.5f, 52, 4, 0, 0, 0), //18
				new LeafConfig(98.5f, 55, 5.5f, 0, 0, 0), //19
				new LeafConfig(109.5f, 55, 4.5f, 0, 0, 0), //20
				new LeafConfig(112.5f, 50, 3.5f, 0, 0, 0), //21
				new LeafConfig(124.5f, 57, 2.5f, 0, 0, 0), //22
				new LeafConfig(121.5f, 62, 4, 0, 0, 0), //23
				new LeafConfig(121.5f, 62, 4, 0, 0, 0), //23


			}
		),

//labeled c1
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

			}
		),

		new VineConfig("E1",
			new LeafConfig[] {
				new LeafConfig(22, -5.5f, -1, 0, 0, 0),
				new LeafConfig(21.5f, -13.5f, -2, 0, 0, 0),
				new LeafConfig(20, -11.5f, 2, 0, 0, 0),
				new LeafConfig(13, 1.5f, 7, 0, 0, 0),
				new LeafConfig(19, 4, 7, 0, 0, 0),
				new LeafConfig(13, 5, 11, 0, 0, 0),
				new LeafConfig(14, 18, 7, 0, 0, 0),
				new LeafConfig(13, 21.5f, 9.5f, 0, 0, 0),
				new LeafConfig(19.5f, 24.5f, 8, 0, 0, 0),
				new LeafConfig(22.5f, 36.5f, 3.5f, 0, 0, 0),
				new LeafConfig(24, 40.5f, 3.5f, 0, 0, 0),
				new LeafConfig(33, 48.5f, 7.5f, 0, 0, 0),
				new LeafConfig(36, 49, 7.5f, 0, 0, 0),
				new LeafConfig(34.5f, 47.5f, 4, 0, 0, 0),
				new LeafConfig(43.5f, 41.5f, 2.5f, 0, 0, 0),
				new LeafConfig(46.5f, 38.5f, 3.5f, 0, 0, 0),
				new LeafConfig(49.5f, 42.5f, 1, 0, 0, 0),
				new LeafConfig(61.5f, 32, 3, 0, 0, 0),
				new LeafConfig(75.5f, 39, 2, 0, 0, 0),
				new LeafConfig(80, 33.5f, 3.5f, 0, 0, 0),
				new LeafConfig(82.5f, 40, 2, 0, 0, 0),
				new LeafConfig(94.5f, 38.5f, 3, 0, 0, 0),
				new LeafConfig(107, 40.5f, 2, 0, 0, 0),
				new LeafConfig(110, 38.5f, 6, 0, 0, 0),
				new LeafConfig(112, 40.5f, 1, 0, 0, 0),
				new LeafConfig(112.5f, 49.5f, 3.5f, 0, 0, 0),
				new LeafConfig(115.5f, 61.5f, 1, 0, 0, 0),
				new LeafConfig(123.5f, 64, .5f, 0, 0, 0),
				new LeafConfig(115.5f, 67, 2.5f, 0, 0, 0),
				new LeafConfig(115.5f, 67, 2.5f, 0, 0, 0),
				new LeafConfig(115.5f, 67, 2.5f, 0, 0, 0),
				new LeafConfig(115.5f, 67, 2.5f, 0, 0, 0),
				new LeafConfig(115.5f, 67, 2.5f, 0, 0, 0),
				new LeafConfig(115.5f, 67, 2.5f, 0, 0, 0),
			}
		),

		new VineConfig("E3",
			new LeafConfig[] {
				new LeafConfig(28, -2.5f, 0, 0, 0, 0),
				new LeafConfig(24, -3.5f, 4, 0, 0, 0),
				new LeafConfig(30,  -1, 1, 0, 0, 0),
				new LeafConfig(33, 17, 4.5f, 0, 0, 0),
				new LeafConfig(39, 12, 4.5f, 0, 0, 0),
				new LeafConfig(42, 15, 2, 0, 0, 0),
				new LeafConfig(45.5f, 20, 3, 0, 0, 0),
				new LeafConfig(51, 26, 3, 0, 0, 0),
				new LeafConfig(51, 54, 4, 0, 0, 0),
				new LeafConfig(66, 19, 4, 0, 0, 0),
				new LeafConfig(68, 15, .5f, 0, 0, 0),
				new LeafConfig(69, 16, 3, 0, 0, 0),
				new LeafConfig(85, 14, 0, 0, 0, 0),
				new LeafConfig(96, 16, 1, 0, 0, 0),
				new LeafConfig(107, 8, 2, 0, 0, 0),
				new LeafConfig(111, 12, 2.5f, 0, 0, 0),
				new LeafConfig(110, 5, 2, 0, 0, 0),
				new LeafConfig(120, 0, 1, 0, 0, 0),
				new LeafConfig(124, 3, 3, 0, 0, 0),
				new LeafConfig(125, 9, 4, 0, 0, 0),
				new LeafConfig(135, 7, 3, 0, 0, 0),
				new LeafConfig(135, 13, 5, 0, 0, 0),
				new LeafConfig(139, 9, 3.5f, 0, 0, 0),
				new LeafConfig(149, 19, 1, 0, 0, 0),
				new LeafConfig(152, 20, 1, 0, 0, 0),
				new LeafConfig(150, 28, 1, 0, 0, 0),
			}
		),
//E429 no data
		new VineConfig("E4",
			new LeafConfig[] {
				new LeafConfig(-2, 0, 4.5f, 0, 0, 0),   
				new LeafConfig(13, 7, 5, 0, 0, 0),
				new LeafConfig(24, 5.5f, 6, 0, 0, 0),
				new LeafConfig(28, -4, 0, 0, 0, 0),
				new LeafConfig(34, -4, 0, 0, 0, 0),
				new LeafConfig(41, -4, 6, 0, 0, 0),
				new LeafConfig(47.5f, -3, 2, 0, 0, 0),
				new LeafConfig(58.5f, 9, 8, 0, 0, 0),
				new LeafConfig(62.4f, 8, 10, 0, 0, 0),
				new LeafConfig(77.4f, 7, 2.5f, 0, 0, 0),
				new LeafConfig(75.4f, 14, 8, 0, 0, 0),
				new LeafConfig(88.4f, 21, 7, 0, 0, 0),
				new LeafConfig(98.4f, 17, 4, 0, 0, 0),
				new LeafConfig(109.4f, 15, 3, 0, 0, 0),
				new LeafConfig(118.4f, 14, 2, 0, 0, 0),
				new LeafConfig(134, 26, 4, 0, 0, 0),
				new LeafConfig(131, 18, 7, 0, 0, 0),
				new LeafConfig(149, 20, 1, 0, 0, 0),
				new LeafConfig(143, 8, 9, 0, 0, 0),
				new LeafConfig(152, 10, 11, 0, 0, 0),
				new LeafConfig(161.5f, 19, 4, 0, 0, 0),
				new LeafConfig(173.5f, 6, 8, 0, 0, 0),
				new LeafConfig(175.5f, 4, 1, 0, 0, 0),
				new LeafConfig(180, 9, 5, 0, 0, 0),
				new LeafConfig(190, 9, 6, 0, 0, 0),
				new LeafConfig(199.5f, 13, 8, 0, 0, 0),
				new LeafConfig(196.5f, 24, 5, 0, 0, 0),
				new LeafConfig(209.5f, 21, 4, 0, 0, 0),
				new LeafConfig(215, 28, 5, 0, 0, 0),
				new LeafConfig(200, 0, 0, 0, 0, 0),
				new LeafConfig(210, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
				new LeafConfig(220, 0, 0, 0, 0, 0),
			}
		),

		new VineConfig("F3",
			new LeafConfig[] {
				new LeafConfig(216-104.5f, -2.5f, 5, 0, 0, 0),
				new LeafConfig(216-99, 1, 2, 0, 0, 0),
				new LeafConfig(216-104.5f, 12, 8, 0, 0, 0),
				new LeafConfig(216-106.5f, 10, 4, 0, 0, 0),
				new LeafConfig(216-98.5f, 24, 4, 0, 0, 0),
				new LeafConfig(216-104.5f, 25, 2, 0, 0, 0),
				new LeafConfig(216-101.5f, 47, 7, 0, 0, 0),
				new LeafConfig(216-97.5f, 50, 6, 0, 0, 0),
				new LeafConfig(216-97.5f, 66.5f, 3.5f, 0, 0, 0),
				new LeafConfig(216-103.5f, 78.5f, 5, 0, 0, 0),
				new LeafConfig(216-101, 82.5f, 8.5f, 0, 0, 0),
				new LeafConfig(216-110, 93.5f, 2.5f, 0, 0, 0),
				new LeafConfig(216-106, 96.5f, 6, 0, 0, 0),
				new LeafConfig(216-105, 106.5f, 5, 0, 0, 0),
				new LeafConfig(216-108, 109.5f, 8, 0, 0, 0),
				new LeafConfig(216-104.5f, 120, 15, 0, 0, 0),
				new LeafConfig(216-107.5f, 123, 7.5f, 0, 0, 0),
				new LeafConfig(216-112, 131, 5, 0, 0, 0),
				new LeafConfig(216-110, 137, 4.5f, 0, 0, 0),
				new LeafConfig(216-123, 140, 6, 0, 0, 0),
				new LeafConfig(216-121, 143.5f, 4, 0, 0, 0),
				new LeafConfig(216-122.5f, 155, 3.5f, 0, 0, 0),
				new LeafConfig(216-122.5f, 155, 3.5f, 0, 0, 0),

			}
		),

		new VineConfig("F4",
			new LeafConfig[] {
				new LeafConfig(216-103,-2.5f, 4.5f, 0, 0, 0),
				new LeafConfig(216-106.5f,-6, 9, 0, 0, 0),
				new LeafConfig(216-114.5f, 3, 2.5f, 0, 0, 0),
				new LeafConfig(216-112, 9, 7, 0, 0, 0),
				new LeafConfig(216-118, 18.5f, 6.5f, 0, 0, 0),
				new LeafConfig(216-118, 23, 4.5f, 0, 0, 0),
				new LeafConfig(216-131.5f, 27.5f, 9, 0, 0, 0),
				new LeafConfig(216-132.5f, 32.5f, 10.5f, 0, 0, 0),
				new LeafConfig(216-144, 41, 12, 0, 0, 0),
				new LeafConfig(216-146.5f, 37, 8, 0, 0, 0),
				new LeafConfig(216-150.5f, 52, 9, 0, 0, 0),
				new LeafConfig(216-152.5f, 54, 7.5f, 0, 0, 0),
				new LeafConfig(216-161.5f, 53, 8, 0, 0, 0),
				new LeafConfig(216-162.5f, 50, 9, 0, 0, 0),
				new LeafConfig(216-175.5f, 48, 5, 0, 0, 0),
				new LeafConfig(216-180.5f, 43.5f, 3.5f, 0, 0, 0),
			}
		),

		new VineConfig("G1",
			new LeafConfig[] {
				new LeafConfig(216-37.5f, 0, 3, 0, 0, 0),
				new LeafConfig(216-33.5f, -2, 6.5f, 0, 0, 0),
				new LeafConfig(216-35, 2, 4, 0, 0, 0),
				new LeafConfig(216-40, 8, 6.5f, 0, 0, 0),
				new LeafConfig(216-52, 14, 8, 0, 0, 0), 
				new LeafConfig(216-54, 13, 4.5f, 0, 0, 0), 
				new LeafConfig(216-56, 15.5f, 7.5f, 0, 0, 0),
				new LeafConfig(216-69, 13.5f, 7, 0, 0, 0),
				new LeafConfig(216-82, 9.5f, 7, 0, 0, 0),
				new LeafConfig(216-85, 13, 8.5f, 0, 0, 0),
				new LeafConfig(216-87.5f, 14, 5.5f, 0, 0, 0),
				new LeafConfig(216-101, 21, 9.5f, 0, 0, 0),
				new LeafConfig(216-113, 20, 7, 0, 0, 0),
				new LeafConfig(216-119, 21, 6, 0, 0, 0),
				new LeafConfig(216-118.5f, 18, 9.5f, 0, 0, 0),
				new LeafConfig(216-131.5f, 21, 7, 0, 0, 0),
			}
		),

		new VineConfig("G4",
			new LeafConfig[] {
				new LeafConfig(216-38.5f, -4, 4.5f, 0, 0, 0),
				new LeafConfig(216-46.5f, -6.5f, 7, 0, 0, 0),
				new LeafConfig(216-42, 4, 11, 0, 0, 0),
				new LeafConfig(216-38.5f, 10, 4, 0, 0, 0),
				new LeafConfig(216-36, 19, 3, 0, 0, 0),
				new LeafConfig(216-47, 12, 6, 0, 0, 0),
				new LeafConfig(216-60.5f, 26, 8, 0, 0, 0),
				new LeafConfig(216-64.5f, 28, 4.5f, 0, 0, 0),
				new LeafConfig(216-64.5f, 20.5f, 6, 0, 0, 0),
				new LeafConfig(216-72.5f, 19.5f, 10, 0, 0, 0),
				new LeafConfig(216-76.5f, 28.5f, 4.5f, 0, 0, 0),
				new LeafConfig(216-91, 27, 9, 0, 0, 0),
				new LeafConfig(216-99, 37, 3.5f, 0, 0, 0),
				new LeafConfig(216-89, 43.5f, 11, 0, 0, 0),
				new LeafConfig(216-110, 50, 8, 0, 0, 0),
				new LeafConfig(216-103, 59, 3.5f, 0, 0, 0),
			}
		),
//added a few leaves2
		new VineConfig("H1",
			new LeafConfig[] {
				new LeafConfig(216-1, 15, 1, 0, 0, 0),
				new LeafConfig(216-9, 14, 3, 0, 0, 0),
				new LeafConfig(216-11.5f, 15, 4.5f, 0, 0, 0),
				new LeafConfig(216-24, 17, 4, 0, 0, 0),
				new LeafConfig(216-25, 22, 2.5f, 0, 0, 0),
				new LeafConfig(216-30, 34, 6, 0, 0, 0),
				new LeafConfig(216-31, 31, 3, 0, 0, 0),
				new LeafConfig(216-34.5f, 34, 4, 0, 0, 0),
				new LeafConfig(216-38.5f, 36, 1, 0, 0, 0),
				new LeafConfig(216-50, 38, 1, 0, 0, 0),
				new LeafConfig(216-62, 40, 1, 0, 0, 0),
				new LeafConfig(216-76, 42, 2, 0, 0, 0),
				new LeafConfig(216-78, 43.5f, 1, 0, 0, 0),
				new LeafConfig(216-87, 47.5f, 1, 0, 0, 0),
				new LeafConfig(216-89, 49, 4, 0, 0, 0),
				new LeafConfig(216-100, 46, 4.5f, 0, 0, 0),
				new LeafConfig(216-103, 46, 6, 0, 0, 0),
				new LeafConfig(216-115, 47.5f, 3, 0, 0, 0),
				new LeafConfig(216-118, 46.5f, 3, 0, 0, 0),
				new LeafConfig(216-129, 44.5f, 1.5f, 0, 0, 0),
				new LeafConfig(216-132, 44, 1, 0, 0, 0),
				new LeafConfig(216-143.5f, 46, 6, 0, 0, 0),
				new LeafConfig(216-142.5f, 54, .5f, 0, 0, 0),
				new LeafConfig(216-157.5f, 59.5f, 2, 0, 0, 0),
				new LeafConfig(216-158, 64.5f, 2, 0, 0, 0),
				new LeafConfig(216-165.5f, 72.5f, 2.5f, 0, 0, 0),
				new LeafConfig(216-171.5f, 74.5f, .5f, 0, 0, 0),
				new LeafConfig(216-181.5f, 78.5f, 1, 0, 0, 0),
				new LeafConfig(216-181.5f, 78.5f, 2, 0, 0, 0),
				new LeafConfig(216-181.5f, 78.5f, 2, 0, 0, 0),
				new LeafConfig(216-181.5f, 78.5f, 2, 0, 0, 0),


			}
		),

		new VineConfig("H2",
			new LeafConfig[] {
				new LeafConfig(216-38.5f, 3, 2, 0, 0, 0),
				new LeafConfig(216-32.5f, 1, 2, 0, 0, 0),
				new LeafConfig(216-33.5f, 6, .5f, 0, 0, 0),
				new LeafConfig(216-38, 19, 3.5f, 0, 0, 0),
				new LeafConfig(216-48, 25.5f, 4, 0, 0, 0),
				new LeafConfig(216-50.5f, 20.5f, 1, 0, 0, 0),
				new LeafConfig(216-53, 23.5f, 0, 0, 0, 0),
				new LeafConfig(216-49, 37.5f, 6, 0, 0, 0),
				new LeafConfig(216-35.5f, 42.5f, 4.5f, 0, 0, 0),
				new LeafConfig(216-39, 45.5f, 7, 0, 0, 0),
				new LeafConfig(216-37, 51.5f, 3, 0, 0, 0),
				new LeafConfig(216-33, 61.5f, 1, 0, 0, 0),
				new LeafConfig(216-30, 72.5f, 2, 0, 0, 0),
				new LeafConfig(216-34, 74.5f, 3.5f, 0, 0, 0) ,
				new LeafConfig(216-38.5f, 88.5f, 4, 0, 0, 0),
				new LeafConfig(216-29.5f, 95, 5.5f, 0, 0, 0),
				new LeafConfig(216-27, 97, 5.5f, 0, 0, 0),
				new LeafConfig(216-28, 101.5f, 6.5f, 0, 0, 0),
				new LeafConfig(216-15, 107.5f, 2.5f, 0, 0, 0),
				new LeafConfig(216-8.5f, 114.5f, 1.5f, 0, 0, 0),
				new LeafConfig(216-9, 121, 2, 0, 0, 0),
				new LeafConfig(216-9, 121, 2, 0, 0, 0),

			}
		),

		new VineConfig("H3",
			new LeafConfig[] {
			    new LeafConfig(216-26.5f, -2, -1, 0, 0, 0),
			    new LeafConfig(216-23, -2.5f,  -2.5f, 0, 0, 0),
			    new LeafConfig(216-21, -5, .5f, 0, 0, 0),
			    new LeafConfig(216-13.5f, 7, 5, 0, 0, 0),
			    new LeafConfig(216-14.5f, 9, 3, 0, 0, 0),
			    new LeafConfig(216-13, 11, 7, 0, 0, 0),
			    new LeafConfig(216-10, 27.5f, 4, 0, 0, 0),
			    new LeafConfig(216-10.5f, 26, 3, 0, 0, 0),
			    new LeafConfig(216-16, 38, 2, 0, 0, 0),
			    new LeafConfig(216-17, 39, 5, 0, 0, 0),
			    new LeafConfig(216-21, 41, 6, 0, 0, 0),
			    new LeafConfig(216-33, 45.5f, 1.5f, 0, 0, 0),
			    new LeafConfig(216-35.5f, 47, 3, 0, 0, 0),
			    new LeafConfig(216-34, 50, 3.5f, 0, 0, 0),
			    new LeafConfig(216-45, 58.5f, .5f, 0, 0, 0),
			    new LeafConfig(216-45.5f, 63, 3, 0, 0, 0),
			    new LeafConfig(216-47.5f, 64, 3.5f, 0, 0, 0),
			    new LeafConfig(216-46, 76, 3, 0, 0, 0),
			    new LeafConfig(216-44, 80, 5.5f, 0, 0, 0),
			    new LeafConfig(216-39, 81, 0, 0, 0, 0),
			    new LeafConfig(216-51.5f, 86, 1.5f, 0, 0, 0),
			    new LeafConfig(216-48.5f, 89, 2, 0, 0, 0),
			    new LeafConfig(216-51.5f, 90.5f, 3.5f, 0, 0, 0),
			    new LeafConfig(216-56, 104, 0, 0, 0, 0),
			    new LeafConfig(216-52, 106, 3, 0, 0, 0),
			    new LeafConfig(216-54.5f, 110, 1, 0, 0, 0),
			}
		),

		new VineConfig("H4",
			new LeafConfig[] {
		        new LeafConfig(216-2.5f, 56, 5, 0, 0, 0),
                new LeafConfig(216-5, 53, 3, 0, 0, 0),
                new LeafConfig(216-6, 56, 7.5f, 0, 0, 0),
                new LeafConfig(216-22, 55, 5, 0, 0, 0),
                new LeafConfig(216-31, 67, 4, 0, 0, 0),
                new LeafConfig(216-33, 64.5f, 2.5f, 0, 0, 0),
                new LeafConfig(216-34.5f, 69.5f, 5, 0, 0, 0),
                new LeafConfig(216-47.5f, 66, 4.5f, 0, 0, 0),
                new LeafConfig(216-57.5f, 74, 3.5f, 0, 0, 0),
                new LeafConfig(216-60, 75.5f, 4, 0, 0, 0),
                new LeafConfig(216-75, 80.5f, 2, 0, 0, 0),
                new LeafConfig(216-88, 81.5f, 2, 0, 0, 0),
                new LeafConfig(216-92, 82.5f, 2.5f, 0, 0, 0),
                new LeafConfig(216-94, 82, 1.5f, 0, 0, 0),
                new LeafConfig(216-107, 89, 1.5f, 0, 0, 0),
                new LeafConfig(216-119, 82, 1.5f, 0, 0, 0),
                new LeafConfig(216-120, 81.5f, 4, 0, 0, 0),
                new LeafConfig(216-122.5f, 79.5f, 2, 0, 0, 0),
                new LeafConfig(216-136.5f, 82.5f, 3, 0, 0, 0) ,
                new LeafConfig(216-158.5f, 85.5f, 3, 0, 0, 0),
                new LeafConfig(216-158.5f, 85.5f, 3, 0, 0, 0),
                new LeafConfig(216-158.5f, 85.5f, 3, 0, 0, 0),
                new LeafConfig(216-158.5f, 85.5f, 3, 0, 0, 0),
                new LeafConfig(216-158.5f, 85.5f, 3, 0, 0, 0),
                new LeafConfig(216-158.5f, 85.5f, 3, 0, 0, 0),
                new LeafConfig(216-158.5f, 85.5f, 3, 0, 0, 0),
                new LeafConfig(216-158.5f, 85.5f, 3, 0, 0, 0),
			}
		),

		new VineConfig("I3",
			new LeafConfig[] {
				new LeafConfig(216-0, 90, 7, 0, 0, 0),
				new LeafConfig(216-5, 93, 5.5f, 0, 0, 0),
				new LeafConfig(216-6.5f, 92, 7, 0, 0, 0),
				new LeafConfig(216-14, 97.5f, 9, 0, 0, 0),
				new LeafConfig(216-16, 100.5f, 7, 0, 0, 0),
				new LeafConfig(216-12, 105.5f, 7.5f, 0, 0, 0), 
				new LeafConfig(216-24, 112.5f, 8, 0, 0, 0),
				new LeafConfig(216-23, 110, 3, 0, 0, 0),
				new LeafConfig(216-26, 109, 5.5f, 0, 0, 0),
				new LeafConfig(216-38, 112.5f, 4.5f, 0, 0, 0), 
				new LeafConfig(216-41.5f, 106.5f, 6.5f, 0, 0, 0),
				new LeafConfig(216-42.5f, 114.5f, 6, 0, 0, 0),
				new LeafConfig(216-43.5f, 126.5f, 5, 0, 0, 0),
				new LeafConfig(216-50, 128.5f, 5, 0, 0, 0), 
				new LeafConfig(216-45.5f, 133, 5.5f, 0, 0, 0), 
				new LeafConfig(216-59.5f, 139, 3, 0, 0, 0), 
				new LeafConfig(216-62.5f, 130, 3, 0, 0, 0),
				new LeafConfig(216-65, 130.5f, 4.5f, 0, 0, 0),
			}
		),
//Last 3 of I4
		new VineConfig("I4",
			new LeafConfig[] {
				new LeafConfig(216-.5f, 75, 1, 0, 0, 0),
				new LeafConfig(216-0, 76, 3, 0, 0, 0),
				new LeafConfig(216-2, 72, 6.5f, 0, 0, 0),
				new LeafConfig(216-4.5f, 69, 6.5f, 0, 0, 0),
				new LeafConfig(216-7, 67.5f, 7, 0, 0, 0),
				new LeafConfig(216-12, 66.5f, 5, 0, 0, 0),
				new LeafConfig(216-14, 69.5f, 3.5f, 0, 0, 0),
				new LeafConfig(216-18, 67.5f, 4, 0, 0, 0),
				new LeafConfig(216-16, 71.5f, 7, 0, 0, 0),
				new LeafConfig(216-19.5f, 74.5f, 8, 0, 0, 0),
				new LeafConfig(216-21, 77, 6.5f, 0, 0, 0),
				new LeafConfig(216-26, 78.5f, 8, 0, 0, 0),
				new LeafConfig(216-32, 74.5f, 8.5f, 0, 0, 0),
				new LeafConfig(216-33, 74, 8.75f, 0, 0, 0),
				new LeafConfig(216-38, 73, 6, 0, 0, 0),
				new LeafConfig(216-40.5f, 77, 6, 0, 0, 0),
				new LeafConfig(216-44.5f, 69, 6, 0, 0, 0),
				new LeafConfig(216-49.5f, 73, 3.5f, 0, 0, 0),
				new LeafConfig(216-49.5f, 64, 3, 0, 0, 0),
				new LeafConfig(216-56.5f, 68, 7, 0, 0, 0),
				new LeafConfig(216-61.5f, 70, 4, 0, 0, 0),
				new LeafConfig(216-63.5f, 65, 5, 0, 0, 0),
				new LeafConfig(216-68, 65, 3.5f, 0, 0, 0),
				new LeafConfig(216-74, 66, 4.5f, 0, 0, 0),
				new LeafConfig(216-72, 73, 6.5f, 0, 0, 0),
				new LeafConfig(216-76, 76, 3, 0, 0, 0),
				new LeafConfig(216-76, 76, 3, 0, 0, 0),
				new LeafConfig(216-76, 76, 3, 0, 0, 0),
				new LeafConfig(216-76, 76, 3, 0, 0, 0),
				new LeafConfig(216-76, 76, 3, 0, 0, 0),	
			}
		),

		new VineConfig("J1",
			new LeafConfig[] {
				new LeafConfig(216-4, 144+4.5f, -4.5f, 0, 0, 0),
				new LeafConfig(216-8.5f, 144+5, 1, 0, 0, 0),
				new LeafConfig(216-8.5f, 144-2.5f, .5f, 0, 0, 0),
				new LeafConfig(216-13, 144-13.5f, 4, 0, 0, 0),
				new LeafConfig(216-15.5f, 144-25, 4, 0, 0, 0),
				new LeafConfig(216-20, 144-26.5f, 3, 0, 0, 0),
				new LeafConfig(216-23, 144-23.5f, 7.5f, 0, 0, 0),
				new LeafConfig(216-34.5f, 144-27.5f, 12.5f, 0, 0, 0),
				new LeafConfig(216-37.5f, 144-21, 9, 0, 0, 0),
				new LeafConfig(216-44.5f, 144-19, 7, 0, 0, 0),
				new LeafConfig(216-42, 144-15, 7, 0, 0, 0),
				new LeafConfig(216-45, 144-9.5f, 4, 0, 0, 0),
				new LeafConfig(216-56, 144-7, 4.5f, 0, 0, 0),
				new LeafConfig(216-60, 144-7, 4, 0, 0, 0),
				new LeafConfig(216-60.5f, 144-5, 3, 0, 0, 0),
				new LeafConfig(216-71, 144-16, 1.5f, 0, 0, 0),
			}
		),

		new VineConfig("J2",
			new LeafConfig[] {
			    new LeafConfig(216-2, 144-37, 2.5f, 0, 0, 0),
			    new LeafConfig(216-3, 144-39, 5, 0, 0, 0),
			    new LeafConfig(216-5, 144-44, 4, 0, 0, 0),
			    new LeafConfig(216-9, 144-36.5f, 3, 0, 0, 0),
			    new LeafConfig(216-14, 144-40.5f, 2.5f, 0, 0, 0),
			    new LeafConfig(216-14, 144-50, 6, 0, 0, 0),
			    new LeafConfig(216-14, 144-46, 6.5f, 0, 0, 0),
			    new LeafConfig(216-18.5f, 144-50.5f, 1.5f, 0, 0, 0),
			    new LeafConfig(216-22, 144-56.5f, 2, 0, 0, 0),
			    new LeafConfig(216-23, 144-58.5f, 4, 0, 0, 0),
			    new LeafConfig(216-21, 144-52.5f, 5, 0, 0, 0),
			    new LeafConfig(216-26, 144-53.5f, 5.5f, 0, 0, 0),
			    new LeafConfig(216-29.5f, 144-57, 8.5f, 0, 0, 0),
			    new LeafConfig(216-32.5f, 144-59, 7, 0, 0, 0),
			    new LeafConfig(216-36.5f, 144-49, 8.5f, 0, 0, 0),
			    new LeafConfig(216-38, 144-56, 9, 0, 0, 0),
			    new LeafConfig(216-43, 144-53, 5.5f, 0, 0, 0),
			    new LeafConfig(216-39, 144-47, 5, 0, 0, 0),
			    new LeafConfig(216-48, 144-45, 5, 0, 0, 0),
			    new LeafConfig(216-43, 144-40, 4, 0, 0, 0),
			    new LeafConfig(216-51, 144-39, 7, 0, 0, 0),
			    new LeafConfig(216-50, 144-35.5f, 4, 0, 0, 0),
			    new LeafConfig(216-54, 144-33.5f, 6.5f, 0, 0, 0), 
			    new LeafConfig(216-59, 144-34.5f, 2, 0, 0, 0),
			    new LeafConfig(216-60, 144-29, 3, 0, 0, 0),
			    new LeafConfig(216-64.5f, 144-26.5f, 2.5f, 0, 0, 0),
			    new LeafConfig(216-66.5f, 144-25.5f, 1.5f, 0, 0, 0),
			    new LeafConfig(216-64.5f, 144-22, 4.5f, 0, 0, 0),
			    new LeafConfig(216-70, 144-22, 2.5f, 0, 0, 0),
			    new LeafConfig(216-71, 144-18.5f, 5, 0, 0, 0),
			}
		),

		// new VineConfig("J3",
		// 	new LeafConfig[] {
		// 		new LeafConfig(0, 120, 0, 0, 0, 0),
		// 		new LeafConfig(10, 120, 0, 0, 0, 0),
		// 		new LeafConfig(20, 120, 0, 0, 0, 0),
		// 		new LeafConfig(30, 120, 0, 0, 0, 0),
		// 		new LeafConfig(40, 120, 0, 0, 0, 0),
		// 		new LeafConfig(50, 120, 0, 0, 0, 0),
		// 		new LeafConfig(60, 120, 0, 0, 0, 0),
		// 		new LeafConfig(70, 120, 0, 0, 0, 0),
		// 		new LeafConfig(80, 120, 0, 0, 0, 0),
		// 		new LeafConfig(90, 120, 0, 0, 0, 0),
		// 		new LeafConfig(100, 120, 0, 0, 0, 0),
		// 		new LeafConfig(110, 120, 0, 0, 0, 0),
		// 		new LeafConfig(120, 120, 0, 0, 0, 0),
		// 		new LeafConfig(130, 120, 0, 0, 0, 0),
		// 		new LeafConfig(140, 120, 0, 0, 0, 0),
		// 		new LeafConfig(150, 120, 0, 0, 0, 0),
		// 		new LeafConfig(160, 120, 0, 0, 0, 0),
		// 		new LeafConfig(170, 120, 0, 0, 0, 0),
		// 		new LeafConfig(180, 120, 0, 0, 0, 0),
		// 		new LeafConfig(190, 120, 0, 0, 0, 0),
		// 		new LeafConfig(200, 120, 0, 0, 0, 0),
		// 		new LeafConfig(210, 120, 0, 0, 0, 0),
		// 		new LeafConfig(220, 120, 0, 0, 0, 0),
		// 		new LeafConfig(140, 0, 0, 0, 0, 0),
		// 		new LeafConfig(150, 0, 0, 0, 0, 0),
		// 		new LeafConfig(160, 0, 0, 0, 0, 0),
		// 		new LeafConfig(170, 0, 0, 0, 0, 0),
		// 		new LeafConfig(180, 0, 0, 0, 0, 0),
		// 		new LeafConfig(190, 0, 0, 0, 0, 0),
		// 		new LeafConfig(200, 0, 0, 0, 0, 0),
		// 		new LeafConfig(210, 0, 0, 0, 0, 0),
		// 		new LeafConfig(220, 0, 0, 0, 0, 0),
		// 	}
		// ),

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
	// 	String humanID;
	// 	Quadrant quadrant;
	// 	List<LeafConfig> leaves2 = new ArrayList<LeafConfig>();

	// 	public VineConfig(String humanID, Quadrant quadrant, LeafConfig[] leaves2) {
	// 		this.humanID = humanID;
	// 		this.leaves2 = Arrays.asList(leaves2);
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
