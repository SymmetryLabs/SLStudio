// package com.symmetrylabs.shows.empirewall;

// import java.net.SocketException;

// import heronarts.lx.LX;

// import com.symmetrylabs.slstudio.model.SLModel;
// import com.symmetrylabs.shows.Show;
// import com.symmetrylabs.shows.vines.*;


// public class EmpireWallPrototypeShow implements Show {
// 	public final static String SHOW_NAME = "empirewallprototype";

// 	final VineConfig[] VINES_CONFIG = new VineConfig[] {
// 		new VineConfig(
// 			new LeafConfig[] {							//Vine 1
// 				new LeafConfig(-2,  27.5f, 0, 0, 0,  135),
// 				new LeafConfig(2.5f, 24.5f, 0, 0, 0, 225),
// 				new LeafConfig(-2, 21.5f, 0, 0, 0, 135),
// 				new LeafConfig(0, 18.25f, 0, 0, 0, 185),
// 				new LeafConfig(-2, 12, 0, 0, 0, 175),
// 				new LeafConfig(1, 9, 0, 0, 0, 180),
// 				new LeafConfig(8, 8, 0, 0, 0, 270),
// 				new LeafConfig(6.5f, 2, 0, 0, 0, 180),
// 				new LeafConfig(13, 3, 0, 0, 0, 270),
// 				new LeafConfig(10, 2, 0, 0, 0, 90),
// 				new LeafConfig(24, 2, 0, 0, 0, 315),
// 				new LeafConfig(24.5f, -4, 0, 0, 0, 310),
// 				new LeafConfig(21, -7, 0, 0, 0, 220),
// 				new LeafConfig(31, -7.5f, 0, 0, 0, 190),
// 				new LeafConfig(31, -2.5f, 0, 0, 0, 315),
// 			}
// 		),
// 		new VineConfig(							//Vine 2
// 			new LeafConfig[] {
// 				new LeafConfig(9,  44, 0, 0, 0, 180),
// 				new LeafConfig(18, 43.5f, 0, 0, 0, 185),
// 				new LeafConfig(21, 40.5f, 0, 0, 0, 310),
// 				new LeafConfig(19, 36, 0, 0, 0, 225),
// 				new LeafConfig(16, 30, 0, 0, 0, 180),
// 				new LeafConfig(9, 37, 0, 0, 0, 90),
// 				new LeafConfig(11, 26, 0, 0, 0, 135),
// 				new LeafConfig(23, 19, 0, 0, 0, 225),
// 				new LeafConfig(21, 13, 0, 0, 0, 225),
// 				new LeafConfig(16, 0, 0, 0, 0, 180),
// 				new LeafConfig(16, 16, 0, 0, 0, 315),
// 			}
// 		),
// 		new VineConfig(							//Vine 3
// 			new LeafConfig[] {
// 				new LeafConfig(38,  45.5f, 0, 0, 0, 180),
// 				new LeafConfig(46, 40.5f, 0, 0, 0, 225),
// 				new LeafConfig(37, 30, 0, 0, 0, 135),
// 				new LeafConfig(41, 22.5f, 0, 0, 0, 135),
// 				new LeafConfig(47, 13.5f, 0, 0, 0, 180),
// 				new LeafConfig(45, 7, 0, 0, 0, 190),
// 				new LeafConfig(36, 9, 0, 0, 0, 255),
// 				new LeafConfig(27, 7, 0, 0, 0, 180),
// 				new LeafConfig(34, 4, 0, 0, 0, 190),
// 				new LeafConfig(21, 21.5f, 0, 0, 0, 75),
// 				new LeafConfig(34, 17, 0, 0, 0, 270),
// 				new LeafConfig(32, 25, 0, 0, 0, 190),
// 				new LeafConfig(24, 30, 0, 0, 0, 145),
// 				new LeafConfig(23, 43, 0, 0, 0, 35),
// 			}
// 		),
// 		new VineConfig(							//Vine 4
// 			new LeafConfig[] {
// 				new LeafConfig(47,  24.5f, 0, 0, 0, 180),
// 				new LeafConfig(39.5f, 29, 0, 0, 0, 250),
// 				new LeafConfig(35, 33, 0, 0, 0, 250),
// 				new LeafConfig(31, 37f, 0, 0, 0, 180),
// 				new LeafConfig(31, 41, 0, 0, 0, 135),
// 				new LeafConfig(35, 39, 0, 0, 0, 180),
// 			}
// 		),
// 	};

// 	public SLModel buildModel() {
// 		return new VineModel(SHOW_NAME, VINES_CONFIG);
// 	}

// 	public void setupLx(final LX lx) {
// 		try {
// 			EmpireWallPrototypeController controller = new EmpireWallPrototypeController(lx, "10.200.1.217", (VineModel) lx.model);
// 			lx.addOutput(controller);
// 		} catch (SocketException e) { }
// 	}
// }
