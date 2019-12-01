package com.symmetrylabs.shows.gospel;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;

import java.util.List;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.output.LXDatagramOutput;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;


public class GospelShow implements Show {
	public final static String SHOW_NAME = "gospel";

	public SLModel buildModel() {
		return new GospelModel();
	}

	public final String[] IP_ADDRESSES = {
	  "10.200.1.11", // 8
	  "10.200.1.12", // 16
	  "10.200.1.13", // 24
	  "10.200.1.14", // 32
	  "10.200.1.15", // 40
	  "10.200.1.16", // 48
	  "10.200.1.17", // 56
	  "10.200.1.18", // 64
	  "10.200.1.19", // 72
	  "10.200.1.20" // 80
	};

	// public final String[] IP_ADDRESSES = {
	//   "10.200.1.11", // 8
	//   "10.200.1.11", // 16
	//   "10.200.1.11", // 24
	//   "10.200.1.11", // 32
	//   "10.200.1.11", // 40
	//   "10.200.1.11", // 48
	//   "10.200.1.11", // 56
	//   "10.200.1.11", // 64
	//   "10.200.1.11", // 72
	//   "10.200.1.11" // 80
	// };


	int UNIVERSE_1  = 1;  int UNIVERSE_2  = 2;  int UNIVERSE_3  = 3;  int UNIVERSE_4  = 4;  int UNIVERSE_5  = 5;  int UNIVERSE_6  = 6;  int UNIVERSE_7  = 7;  int UNIVERSE_8  = 8;  int UNIVERSE_9  = 9;  int UNIVERSE_10 = 10;
	int UNIVERSE_11 = 11; int UNIVERSE_12 = 12; int UNIVERSE_13 = 13; int UNIVERSE_14 = 14; int UNIVERSE_15 = 15; int UNIVERSE_16 = 16; int UNIVERSE_17 = 17; int UNIVERSE_18 = 18; int UNIVERSE_19 = 19; int UNIVERSE_20 = 20;
	int UNIVERSE_21 = 21; int UNIVERSE_22 = 22; int UNIVERSE_23 = 23; int UNIVERSE_24 = 24; int UNIVERSE_25 = 25; int UNIVERSE_26 = 26; int UNIVERSE_27 = 27; int UNIVERSE_28 = 28; int UNIVERSE_29 = 29; int UNIVERSE_30 = 30;
	int UNIVERSE_31 = 31; int UNIVERSE_32 = 32; int UNIVERSE_33 = 33; int UNIVERSE_34 = 34; int UNIVERSE_35 = 35; int UNIVERSE_36 = 36; int UNIVERSE_37 = 37; int UNIVERSE_38 = 38; int UNIVERSE_39 = 39; int UNIVERSE_40 = 40;
	int UNIVERSE_41 = 41; int UNIVERSE_42 = 42; int UNIVERSE_43 = 43; int UNIVERSE_44 = 44; int UNIVERSE_45 = 45; int UNIVERSE_46 = 46; int UNIVERSE_47 = 47; int UNIVERSE_48 = 48; int UNIVERSE_49 = 49; int UNIVERSE_50 = 50;
	int UNIVERSE_51 = 51; int UNIVERSE_52 = 52; int UNIVERSE_53 = 53; int UNIVERSE_54 = 54; int UNIVERSE_55 = 55; int UNIVERSE_56 = 56; int UNIVERSE_57 = 57; int UNIVERSE_58 = 58; int UNIVERSE_59 = 59; int UNIVERSE_60 = 60;
	int UNIVERSE_61 = 61; int UNIVERSE_62 = 62; int UNIVERSE_63 = 63; int UNIVERSE_64 = 64; int UNIVERSE_65 = 65; int UNIVERSE_66 = 66; int UNIVERSE_67 = 67; int UNIVERSE_68 = 68; int UNIVERSE_69 = 69; int UNIVERSE_70 = 70;
	int UNIVERSE_71 = 71; int UNIVERSE_72 = 72; int UNIVERSE_73 = 73; int UNIVERSE_74 = 74; int UNIVERSE_75 = 75; int UNIVERSE_76 = 76; int UNIVERSE_77 = 77; int UNIVERSE_78 = 78; int UNIVERSE_79 = 79; int UNIVERSE_80 = 80;

	public OutputGroup[] buildOutputGroups(GospelModel model) {

	  OutputGroup[] prelim = new OutputGroup[] {

	    /**
	     * Vip Booth
	     *------------------------------------------------------------------------------------*/
	    new OutputGroup(UNIVERSE_1)
	      .addPoints(model.splicePoints("vip-lounge-strip17", 0, 72))
	      .addPoints(model.splicePoints("vip-lounge-strip7",  0, 71), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vip-lounge-strip10", 41, 8), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_2).addDeadPoints(3, model)
	      .addPoints(model.splicePoints("vip-lounge-strip10", 0,  41), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vip-lounge-strip5",  0, 105))
	      .addDeadPoints(2, model),

	    new OutputGroup(UNIVERSE_3)
	      .addPoints(model.splicePoints("vip-lounge-strip5", 104, 17), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_4)
	      .addPoints(model.splicePoints("vip-lounge-strip8", 0, 125), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vip-lounge-strip18", 47, 26), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_6).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("vip-lounge-strip18", 0, 48), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vip-lounge-strip6", 0, 66))
	      .addPoints(model.splicePoints("vip-lounge-strip14", 0, 37)),

	    new OutputGroup(UNIVERSE_9)
	      .addPoints(model.splicePoints("vip-lounge-strip3", 0, 92), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vip-lounge-strip9", 0, 50), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vip-lounge-strip1", 0, 10)),

	    new OutputGroup(UNIVERSE_10).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("vip-lounge-strip1", 10, 34))
	      .addPoints(model.splicePoints("vip-lounge-strip15", 0, 69)),

	    new OutputGroup(UNIVERSE_11).addDeadPoints(2, model)
	      .addPoints(model.splicePoints("vip-lounge-strip4", 0, 44), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_64, 1)
	      .addPoints(model.splicePoints("vip-lounge-strip16", 0, 67), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vip-lounge-strip2", 0, 84))
	      .addDeadPoints(2, model),

	    new OutputGroup(UNIVERSE_67).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("vip-lounge-strip2", 83, 58))
	      // .addDeadPoints(1, model)
	      .addPoints(model.splicePoints("vip-lounge-strip11", 0, 39))
	      .addPoints(model.splicePoints("vip-lounge-strip12", 0, 3))
	      .addPoints(model.splicePoints("vip-lounge-strip13", 0, 10))
	      .addPoints(model.splicePoints("vip-lounge-strip4", 55, 44), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_67, -1).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("vip-lounge-strip2", 83, 58))
	      // .addDeadPoints(1, model)
	      .addPoints(model.splicePoints("vip-lounge-strip11", 0, 39))
	      .addPoints(model.splicePoints("vip-lounge-strip12", 0, 3))
	      .addPoints(model.splicePoints("vip-lounge-strip13", 0, 10))
	      .addPoints(model.splicePoints("vip-lounge-strip4", 55, 44), OutputGroup.REVERSE),




	    new OutputGroup(UNIVERSE_71).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("vip-lounge-strip14", 35, 14))
	      .addDeadPoints(1, model),

	    new OutputGroup(UNIVERSE_31)
	      .addPoints(model.splicePoints("vip-lounge-strip19", 0, 100))
	      .addPoints(model.splicePoints("vip-lounge-strip20", 0, 51)),

	    new OutputGroup(UNIVERSE_42)
	      .addPoints(model.splicePoints("vip-lounge-strip22", 0, 100))
	      .addPoints(model.splicePoints("vip-lounge-strip23", 0, 73)),

	    new OutputGroup(UNIVERSE_54).addDeadPoints(10, model)
	      .addPoints(model.splicePoints("vip-lounge-strip23", 49, 26))
	      .addPoints(model.splicePoints("vip-lounge-strip24", 0, 80)),

	    new OutputGroup(UNIVERSE_58).addDeadPoints(2, model)
	      .addPoints(model.splicePoints("vip-lounge-strip20", 49, 48))
	      .addPoints(model.splicePoints("vip-lounge-strip21", 0, 78)),

	    /**
	     * Long Skinny
	     *------------------------------------------------------------------------------------*/

	    new OutputGroup(UNIVERSE_13).addDeadPoints(2, model)
	      .addPoints(model.splicePoints("long-skinny-strip12", 28, 5), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("long-skinny-strip6", 0, 68), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("long-skinny-strip11", 0, 32), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_14)
	      .addPoints(model.splicePoints("long-skinny-strip5", 0, 44), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("long-skinny-strip9", 0, 18), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("long-skinny-strip1", 0, 90)),

	    new OutputGroup(UNIVERSE_15)
	      .addPoints(model.splicePoints("long-skinny-strip7", 0, 144), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("long-skinny-strip13", 23, 11), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_17)
	      .addPoints(model.splicePoints("long-skinny-strip2", 0, 121))
	      .addDeadPoints(3, model)
	      .addPoints(model.splicePoints("long-skinny-strip12", 0, 28)),

	    new OutputGroup(UNIVERSE_20)
	      .addPoints(model.splicePoints("long-skinny-strip13", 0, 23), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("long-skinny-strip3", 0, 84))
	      .addPoints(model.splicePoints("long-skinny-strip14", 0, 33)),

	    new OutputGroup(UNIVERSE_22)
	      .addPoints(model.splicePoints("long-skinny-strip15", 0, 33))
	      .addPoints(model.splicePoints("long-skinny-strip8", 0, 76))
	      .addDeadPoints(2, model)
	      .addPoints(model.splicePoints("long-skinny-strip16", 21, 11), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_25)
	      .addDeadPoints(3, model)
	      .addPoints(model.splicePoints("long-skinny-strip16", 0, 21), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("long-skinny-strip4", 0, 79), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_27)
	      .addPoints(model.splicePoints("long-skinny-strip1", 89, 8))
	      .addPoints(model.splicePoints("long-skinny-strip10", 0, 32))
	      .addDeadPoints(3, model)
	      .addPoints(model.splicePoints("long-skinny-strip5", 43, 83), OutputGroup.REVERSE),


	    /**
	     * Columns (and ceiling above)
	     *------------------------------------------------------------------------------------*/

	    //ceiling
	    new OutputGroup(UNIVERSE_79)
	      .addPoints(model.splicePoints("columns-strip1", 0, 49)),

	    new OutputGroup(UNIVERSE_30)
	      .addPoints(model.splicePoints("columns-strip2", 0, 148), OutputGroup.REVERSE)
	      .addDeadPoints(3, model),

	    new OutputGroup(UNIVERSE_36).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("columns-strip3", 0, 148), OutputGroup.REVERSE)
	      .addDeadPoints(3, model),

	    new OutputGroup(UNIVERSE_35)
	      .addPoints(model.splicePoints("columns-strip4", 0, 145), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_37)
	      .addPoints(model.splicePoints("columns-strip5", 0, 49)),

	    new OutputGroup(UNIVERSE_49).addDeadPoints(2, model)
	      .addPoints(model.splicePoints("columns-strip6", 0, 146), OutputGroup.REVERSE)
	      .addDeadPoints(2, model),

	    new OutputGroup(UNIVERSE_59).addDeadPoints(8, model)
	      .addPoints(model.splicePoints("columns-strip7", 0, 143), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_53)
	      .addPoints(model.splicePoints("columns-strip8", 0, 151), OutputGroup.REVERSE),

	    // first square
	    new OutputGroup(UNIVERSE_24).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("columns-strip9", 8, 18))
	      .addPoints(model.splicePoints("columns-strip10", 0, 25))
	      .addPoints(model.splicePoints("columns-strip11", 0, 26))
	      .addPoints(model.splicePoints("columns-strip12", 0, 26))
	      .addPoints(model.splicePoints("columns-strip9", 0, 8)),

	    new OutputGroup(UNIVERSE_19).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("columns-strip13", 8, 19))
	      .addPoints(model.splicePoints("columns-strip14", 0, 25))
	      .addPoints(model.splicePoints("columns-strip15", 0, 26))
	      .addPoints(model.splicePoints("columns-strip16", 0, 26))
	      .addPoints(model.splicePoints("columns-strip13", 0, 8)),

	    new OutputGroup(UNIVERSE_16).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("columns-strip17", 8, 19))
	      .addPoints(model.splicePoints("columns-strip18", 0, 27))
	      .addPoints(model.splicePoints("columns-strip19", 0, 26))
	      .addPoints(model.splicePoints("columns-strip20", 0, 26))
	      .addPoints(model.splicePoints("columns-strip17", 0, 8)),

	    /**
	     * Stairs
	     *------------------------------------------------------------------------------------*/

	    new OutputGroup(UNIVERSE_41)
	      .addPoints(model.splicePoints("stairs-strip1", 0, 34), OutputGroup.REVERSE) // WEIRD OFFSET PROBLEMS
	      .addPoints(model.splicePoints("stairs-strip2", 0, 46), OutputGroup.REVERSE) // WEIRD OFFSET PROBLEMS
	      .addPoints(model.splicePoints("stairs-strip3", 0, 51), OutputGroup.REVERSE), // WEIRD OFFSET PROBLEMS

	    new OutputGroup(UNIVERSE_46)
	      .addPoints(model.splicePoints("stairs-strip4", 0, 51), OutputGroup.REVERSE) // WEIRD OFFSET PROBLEMS
	      .addPoints(model.splicePoints("stairs-strip5", 0, 51), OutputGroup.REVERSE), // WEIRD OFFSET PROBLEMS

	    new OutputGroup(UNIVERSE_28)
	      .addPoints(model.splicePoints("stairs-strip6", 0, 51), OutputGroup.REVERSE) // WEIRD OFFSET PROBLEMS
	      .addPoints(model.splicePoints("stairs-strip7", 0, 51), OutputGroup.REVERSE), // WEIRD OFFSET PROBLEMS

	    new OutputGroup(UNIVERSE_34)
	      .addPoints(model.splicePoints("stairs-strip8", 0, 51), OutputGroup.REVERSE) // WEIRD OFFSET PROBLEMS
	      .addPoints(model.splicePoints("stairs-strip9", 0, 73), OutputGroup.REVERSE), // WEIRD OFFSET PROBLEMS


	    /**
	     * Big Long Section
	     *------------------------------------------------------------------------------------*/
	    new OutputGroup(UNIVERSE_43).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("big-long-section-strip1", 149, 48))
	      .addPoints(model.splicePoints("big-long-section-strip8", 0, 70))
	      .addPoints(model.splicePoints("big-long-section-strip4", 51, 33), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_45).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("big-long-section-strip5", 148, 36))
	      .addPoints(model.splicePoints("big-long-section-strip10", 0, 72), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("big-long-section-strip2", 29, 43), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_47)
	      .addPoints(model.splicePoints("big-long-section-strip5", 0, 150)),

	    new OutputGroup(UNIVERSE_48)
	      .addPoints(model.splicePoints("big-long-section-strip6", 0, 93), OutputGroup.REVERSE),
	      // .addPoints(model.splicePoints("big-long-section-strip6", 76, 17), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_50)
	      .addPoints(model.splicePoints("big-long-section-strip4", 0, 52), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("big-long-section-strip7", 0, 73), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_51)
	      .addPoints(model.splicePoints("big-long-section-strip1", 0, 150)),

	    new OutputGroup(UNIVERSE_52).addDeadPoints(1, model)
	      .addPoints(model.splicePoints("big-long-section-strip2", 0, 32), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("big-long-section-strip9", 0, 69)),

	    new OutputGroup(UNIVERSE_66, -1)
	      .addPoints(model.splicePoints("big-long-section-strip3", 0, 153), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_66)
	      .addPoints(model.splicePoints("big-long-section-strip3", 0, 153), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_68)
	      .addPoints(model.splicePoints("big-long-section-strip11", 1, 70))
	      .addPoints(model.splicePoints("big-long-section-strip6", 0, 78))
	      .addDeadPoints(3, model),

	    /**
	     * Stage and Entrace
	     *------------------------------------------------------------------------------------*/
	     // SOMETHING WEIRD IN THE LONG RUN LIKE WE ARE SKIPPING OUT OF SOME LEDS!!!
	    new OutputGroup(UNIVERSE_65)
	      .addPoints(model.splicePoints("stage-and-entrance-strip1", 37, 112))
	      .addDeadPoints(1, model)
	      .addPoints(model.splicePoints("stage-and-entrance-strip2", 0, 45)),

	    // new OutputGroup(UNIVERSE_65)
	    //   .addPoints(model.splicePoints("stage-and-entrance-strip1", 0, 150)),


	    new OutputGroup(UNIVERSE_75)
	      .addPoints(model.splicePoints("stage-and-entrance-strip2", 45, 104))
	      .addPoints(model.splicePoints("stage-and-entrance-strip3", 0, 46)),

	    new OutputGroup(UNIVERSE_62)
	      .addPoints(model.splicePoints("stage-and-entrance-strip5", 0, 29), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("stage-and-entrance-strip4", 0, 72), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("stage-and-entrance-strip3", 45, 15), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_74).addDeadPoints(2, model)
	      .addPoints(model.splicePoints("stage-and-entrance-strip7", 0, 10), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("stage-and-entrance-strip6", 0, 101), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("stage-and-entrance-strip1", 0, 39)),

	    new OutputGroup(UNIVERSE_69)
	      .addPoints(model.splicePoints("stage-and-entrance-strip7", 8, 150), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_61)
	      .addPoints(model.splicePoints("stage-and-entrance-strip8", 0, 26))
	      .addPoints(model.splicePoints("stage-and-entrance-strip9", 0, 75))
	      .addPoints(model.splicePoints("stage-and-entrance-strip10", 0, 49)),

	    new OutputGroup(UNIVERSE_77)
	      .addPoints(model.splicePoints("back-hall-strip1", 0, 170)),
	      // .addPoints(model.splicePoints("back-hall-strip2", 0, 150))
	      // .addPoints(model.splicePoints("back-hall-strip3", 0, 150)),



	    /**
	     * VJ Booth
	     *------------------------------------------------------------------------------------*/
	    new OutputGroup(UNIVERSE_23)
	      .addPoints(model.splicePoints("vj_booth-strip2", 2, 30), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vj_booth-strip1", 0, 35), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vj_booth-strip4", 0, 8))
	      .addPoints(model.splicePoints("vj_booth-strip5", 0, 81)),

	    new OutputGroup(UNIVERSE_26)
	      .addPoints(model.splicePoints("vj_booth-strip2", 30, 3))
	      .addPoints(model.splicePoints("vj_booth-strip3", 0, 47))
	      .addPoints(model.splicePoints("vj_booth-strip6", 0, 39), OutputGroup.REVERSE),

	    new OutputGroup(UNIVERSE_5)
	      .addPoints(model.splicePoints("vj_booth-strip9", 0, 103))
	      .addDeadPoints(2, model),

	    new OutputGroup(UNIVERSE_7)
	      .addPoints(model.splicePoints("vj_booth-strip10", 0, 49), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vj_booth-strip7", 0, 82))
	      .addPoints(model.splicePoints("vj_booth-strip11", 0, 26)),

	    new OutputGroup(UNIVERSE_8)
	      .addPoints(model.splicePoints("vj_booth-strip14", 0, 64))
	      .addPoints(model.splicePoints("vj_booth-strip12", 0, 52))
	      .addDeadPoints(2, model),

	    new OutputGroup(UNIVERSE_72)
	      .addPoints(model.splicePoints("vj_booth-strip15", 0, 44))
	      .addPoints(model.splicePoints("vj_booth-strip8", 0, 25))
	      .addPoints(model.splicePoints("vj_booth-strip13", 0, 72)),

	    new OutputGroup(UNIVERSE_40)
	      .addPoints(model.splicePoints("vj_booth-strip16", 0, 66)),

	    new OutputGroup(UNIVERSE_29)
	      .addPoints(model.splicePoints("vj_booth-strip18", 0, 75), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vj_booth-strip17", 0, 75), OutputGroup.REVERSE)
	      .addPoints(model.splicePoints("vj_booth-strip16", 64, 11), OutputGroup.REVERSE)
	      .addDeadPoints(3, model),



	    /**
	     * Bar
	     *------------------------------------------------------------------------------------*/
	     // CAN'T ADDRESS THEM INDIVIDUALLY!
	    new OutputGroup(UNIVERSE_78)
	      .addPoints(model.splicePoints("bar-strip1", 0, 49)),
	  };

	  // OutputGroup[] ret = new OutputGroup[80];
	  // for (int i = 0; i < 80; i++) {
	  // 	ret[i] = null;
	  // }
	  // for (int i = 0; i < prelim.length; i++) {
	  // 	ret[prelim[i].universe - 1] = prelim[i];
	  // }
	  // for (int i = 0; i < 80; i++) {
	  // 	if (ret[i] == null) {
	  // 		ret[i] = new OutputGroup(i + 1).addPoints(model.splicePoints("back-hall-strip1", 0, 170));
	  // 	}
	  // }
	  return prelim;	
	}

	public class OutputGroup {

	  public static final boolean REVERSE = true;

	  private final int MAX_NUMBER_LEDS_PER_UNVERSE = 170;

	  private final List<LXPoint> points = new ArrayList<LXPoint>();

	  public final String ipAddress;
	  public final int universe;

	  public OutputGroup(int universe) {
	    this(universe, 0);
	  }


	  public OutputGroup(int universe, int offset) {
	    // int ipIndex = (int)Math.ceil((float)universe / (float)IP_ADDRESSES.length);
	    int ipIndex = (int)Math.floor(((float)universe - 1) / 8.0);
	    ipIndex += offset;
	    this.ipAddress = IP_ADDRESSES[ipIndex];
	    this.universe = universe;
	    // println("Creating an OutputGroup with IP Address: [" + ipAddress + "] and universe: [" + universe + "]");
	  }

	  public List<LXPoint> getPoints() {
	    return points;
	  }

	  public int[] getIndices() {
	    int[] indices = new int[points.size()];

	    for (int i = 0; i < points.size(); i++) {
	      indices[i] = points.get(i).index;
	    }
	    return indices;
	  }

	  public OutputGroup reversePoints() {
	    Collections.reverse(Arrays.asList(points));
	    return this;
	  }

	  public OutputGroup addPoints(LXPoint[] pointsToAdd) {
	    for (LXPoint p : pointsToAdd) {
	      this.points.add(p);
	    }
	    return this;
	  }

	  public OutputGroup addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
	    if (reverseOrdering) {
	      Collections.reverse(Arrays.asList(pointsToAdd));
	    }
	    for (LXPoint p : pointsToAdd) {
	      this.points.add(p);
	    }
	    return this;
	  }

	  public OutputGroup addPoints(List<LXPoint> pointsToAdd) {
	    for (LXPoint p : pointsToAdd) {
	      this.points.add(p);
	    }
	    return this;
	  }

	  public OutputGroup addPoints(List<LXPoint> pointsToAdd, boolean reverseOrdering) {
	    if (reverseOrdering) {
	      Collections.reverse(Arrays.asList(pointsToAdd));
	    }
	    for (LXPoint p : pointsToAdd) {
	      this.points.add(p);
	    }
	    return this;
	  }

	  public OutputGroup addDeadPoints(int numPointsToAdd, SLModel model) {
	    LXPoint deadPoint = ((SLModel)model).points[((SLModel)model).points.length-1];

	    for (int i = 0; i < numPointsToAdd; i++) {
	      this.points.add(deadPoint);
	    }

	    return this;
	  }
	}


	public void setupLx(final LX lx) {
		// pixlite yada yada

		GospelModel model = (GospelModel) lx.model;
		final OutputGroup[] OUTPUT_GROUP_CONFIG = buildOutputGroups(model);


		  try {
		    
		    final LXDatagramOutput artnetController = new LXDatagramOutput(lx);

		    for (OutputGroup og : OUTPUT_GROUP_CONFIG) {
		      artnetController.addDatagram(new ArtNetDatagram(og.ipAddress, og.getIndices(), og.universe));
		    }

		    lx.addOutput(artnetController);

		  } catch (SocketException e) {
		   e.printStackTrace();
		  }
	}

	public class ArtNetDatagram extends LXDatagram {

	  private final static int DEFAULT_UNIVERSE = 0;
	  private final static int ARTNET_HEADER_LENGTH = 18;
	  private final static int ARTNET_PORT = 6454;
	  private final static int SEQUENCE_INDEX = 12;

	  private final int[] pointIndices;

	  private boolean sequenceEnabled = false;

	  private byte sequence = 1;

	  public ArtNetDatagram(String ipAddress, int[] indices, int universeNumber) {
	    this(ipAddress, indices, 3*indices.length, universeNumber);
	  }

	  public ArtNetDatagram(String ipAddress, int[] indices, int dataLength, int universeNumber) {
	    super(ARTNET_HEADER_LENGTH + dataLength + (dataLength % 2));

	    this.pointIndices = indices;

	    setByteOrder(ByteOrder.BRG);

	    try {
	        setAddress(ipAddress);
	        setPort(ARTNET_PORT);
	    } catch (UnknownHostException e) {
	        System.out.println("COULD NOT FIND PIXLITE HOST WITH IP: " + ipAddress);
	    }

	    this.buffer[0] = 'A';
	    this.buffer[1] = 'r';
	    this.buffer[2] = 't';
	    this.buffer[3] = '-';
	    this.buffer[4] = 'N';
	    this.buffer[5] = 'e';
	    this.buffer[6] = 't';
	    this.buffer[7] = 0;
	    this.buffer[8] = 0x00; // ArtDMX opcode
	    this.buffer[9] = 0x50; // ArtDMX opcode
	    this.buffer[10] = 0; // Protcol version
	    this.buffer[11] = 14; // Protcol version
	    this.buffer[12] = 0; // Sequence
	    this.buffer[13] = 0; // Physical
	    this.buffer[14] = (byte) (universeNumber & 0xff); // Universe LSB
	    this.buffer[15] = (byte) ((universeNumber >>> 8) & 0xff); // Universe MSB
	    this.buffer[16] = (byte) ((dataLength >>> 8) & 0xff);
	    this.buffer[17] = (byte) (dataLength & 0xff);

	    // Ensure zero rest of buffer
	    for (int i = ARTNET_HEADER_LENGTH; i < this.buffer.length; ++i) {
	     this.buffer[i] = 0;
	    }
	  }

	  /**
	   * Set whether to increment and send sequence numbers
	   *
	   * @param sequenceEnabled true if sequence should be incremented and transmitted
	   * @return this
	   */
	  public ArtNetDatagram setSequenceEnabled(boolean sequenceEnabled) {
	    this.sequenceEnabled = sequenceEnabled;
	    return this;
	  }

	  @Override
	  public void onSend(int[] colors) {
	    copyPoints(colors, this.pointIndices, ARTNET_HEADER_LENGTH);
	    
	    if (this.sequenceEnabled) {
	      if (++this.sequence == 0) {
	        ++this.sequence;
	      }
	      this.buffer[SEQUENCE_INDEX] = this.sequence;
	    }
	  }
	}
}