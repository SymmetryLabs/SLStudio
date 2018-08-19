package com.symmetrylabs.shows.pilots;

import heronarts.lx.LX;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;


public class PilotsPixlite extends SimplePixlite {

    public PilotsPixlite(LX lx, String ipAddress, PilotsModel.Cart cart) {
        super(lx, ipAddress);

        // vertical - col 1 (left)
        addPixliteOutput(
            new PointsGrouping("1")
                .addPoints(cart.getStrip((37*0)+1).getPoints())
                .addPoints(cart.getStrip((37*0)+9).getPoints())
                .addPoints(cart.getStrip((37*0)+17).getPoints())
                .addPoints(cart.getStrip((37*0)+25).getPoints())
                .addPoints(cart.getStrip((37*0)+28).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*0)+20).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*0)+12).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*0)+4).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*0)+7).getPoints())
                .addPoints(cart.getStrip((37*0)+15).getPoints())
                .addPoints(cart.getStrip((37*0)+23).getPoints())
                .addPoints(cart.getStrip((37*0)+31).getPoints())
        );

        // vertical - col 2
        addPixliteOutput(
            new PointsGrouping("2")
                .addPoints(cart.getStrip((37*1)+1).getPoints())
                .addPoints(cart.getStrip((37*1)+9).getPoints())
                .addPoints(cart.getStrip((37*1)+17).getPoints())
                .addPoints(cart.getStrip((37*1)+25).getPoints())
                .addPoints(cart.getStrip((37*1)+28).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*1)+20).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*1)+12).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*1)+4).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*1)+7).getPoints())
                .addPoints(cart.getStrip((37*1)+15).getPoints())
                .addPoints(cart.getStrip((37*1)+23).getPoints())
                .addPoints(cart.getStrip((37*1)+31).getPoints())
        );

        // vertical - col 3
        addPixliteOutput(
            new PointsGrouping("3")
                .addPoints(cart.getStrip((37*2)+1).getPoints())
                .addPoints(cart.getStrip((37*2)+9).getPoints())
                .addPoints(cart.getStrip((37*2)+17).getPoints())
                .addPoints(cart.getStrip((37*2)+25).getPoints())
                .addPoints(cart.getStrip((37*2)+28).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*2)+20).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*2)+12).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*2)+4).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*2)+7).getPoints())
                .addPoints(cart.getStrip((37*2)+15).getPoints())
                .addPoints(cart.getStrip((37*2)+23).getPoints())
                .addPoints(cart.getStrip((37*2)+31).getPoints())
        );

        // vertical - col 4
        addPixliteOutput(
            new PointsGrouping("4")
                .addPoints(cart.getStrip((37*3)+1).getPoints())
                .addPoints(cart.getStrip((37*3)+9).getPoints())
                .addPoints(cart.getStrip((37*3)+17).getPoints())
                .addPoints(cart.getStrip((37*3)+25).getPoints())
                .addPoints(cart.getStrip((37*3)+28).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*3)+20).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*3)+12).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*3)+4).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((37*3)+7).getPoints())
                .addPoints(cart.getStrip((37*3)+15).getPoints())
                .addPoints(cart.getStrip((37*3)+23).getPoints())
                .addPoints(cart.getStrip((37*3)+31).getPoints())
        );

        // vertical - col 5
        addPixliteOutput(
            new PointsGrouping("5")
                .addPoints(cart.getStrip(148).getPoints())
                .addPoints(cart.getStrip(153).getPoints())
                .addPoints(cart.getStrip(158).getPoints())
                .addPoints(cart.getStrip(163).getPoints())
                .addPoints(cart.getStrip(165).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(160).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(155).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(150).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(152).getPoints())
                .addPoints(cart.getStrip(157).getPoints())
                .addPoints(cart.getStrip(162).getPoints())
                .addPoints(cart.getStrip(167).getPoints())
        );

        // horizontal - row 1 - A (bottom)
        addPixliteOutput(
            new PointsGrouping("6")
                .addPoints(cart.getStrip(79).getPoints())
                .addPoints(cart.getStrip(43).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(6).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(5).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(2).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(0).getPoints())
                .addPoints(cart.getStrip(37).getPoints())
                .addPoints(cart.getStrip(40).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(3).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(39).getPoints())
                .addPoints(cart.getStrip(42).getPoints())
        );

        // horizontal - row 1 - B
        addPixliteOutput(
            new PointsGrouping("7")
                .addPoints(cart.getStrip(76).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(74).getPoints())
                .addPoints(cart.getStrip(111).getPoints())
                .addPoints(cart.getStrip(149).getPoints())
                .addPoints(cart.getStrip(151).getPoints())
                .addPoints(cart.getStrip(117).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(80).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(77).getPoints())
                .addPoints(cart.getStrip(114).getPoints())
                .addPoints(cart.getStrip(116).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(113).getPoints(), PointsGrouping.REVERSE_ORDERING)
        );

        // horizontal - row 2 - A
        addPixliteOutput(
            new PointsGrouping("8")
                .addPoints(cart.getStrip((8*1)+79).getPoints())
                .addPoints(cart.getStrip((8*1)+43).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*1)+6).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*1)+5).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*1)+2).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*1)+0).getPoints())
                .addPoints(cart.getStrip((8*1)+37).getPoints())
                .addPoints(cart.getStrip((8*1)+40).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*1)+3).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*1)+39).getPoints())
                .addPoints(cart.getStrip((8*1)+42).getPoints())
        );

        // horizontal - row 2 - B
        addPixliteOutput(
            new PointsGrouping("9")
                .addPoints(cart.getStrip(84).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(82).getPoints())
                .addPoints(cart.getStrip(119).getPoints())
                .addPoints(cart.getStrip(154).getPoints())
                .addPoints(cart.getStrip(156).getPoints())
                .addPoints(cart.getStrip(125).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(88).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(85).getPoints())
                .addPoints(cart.getStrip(122).getPoints())
                .addPoints(cart.getStrip(124).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(121).getPoints(), PointsGrouping.REVERSE_ORDERING)
        );

        // horizontal - row 3 - A
        addPixliteOutput(
            new PointsGrouping("10")
                .addPoints(cart.getStrip((8*2)+79).getPoints())
                .addPoints(cart.getStrip((8*2)+43).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*2)+6).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*2)+5).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*2)+2).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*2)+0).getPoints())
                .addPoints(cart.getStrip((8*2)+37).getPoints())
                .addPoints(cart.getStrip((8*2)+40).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*2)+3).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*2)+39).getPoints())
                .addPoints(cart.getStrip((8*2)+42).getPoints())
        );

        // horizontal - row 3 - B
        addPixliteOutput(
            new PointsGrouping("11")
                .addPoints(cart.getStrip(92).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(90).getPoints())
                .addPoints(cart.getStrip(127).getPoints())
                .addPoints(cart.getStrip(159).getPoints())
                .addPoints(cart.getStrip(161).getPoints())
                .addPoints(cart.getStrip(133).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(96).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(93).getPoints())
                .addPoints(cart.getStrip(130).getPoints())
                .addPoints(cart.getStrip(132).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(129).getPoints(), PointsGrouping.REVERSE_ORDERING)
        );

        // horizontal - row 4 - A
        addPixliteOutput(
            new PointsGrouping("12")
                .addPoints(cart.getStrip((8*3)+79).getPoints())
                .addPoints(cart.getStrip((8*3)+43).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*3)+6).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*3)+5).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*3)+2).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*3)+0).getPoints())
                .addPoints(cart.getStrip((8*3)+37).getPoints())
                .addPoints(cart.getStrip((8*3)+40).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*3)+3).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip((8*3)+39).getPoints())
                .addPoints(cart.getStrip((8*3)+42).getPoints())
        );

        // horizontal - row 4 - B
        addPixliteOutput(
            new PointsGrouping("13")
                .addPoints(cart.getStrip(100).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(98).getPoints())
                .addPoints(cart.getStrip(135).getPoints())
                .addPoints(cart.getStrip(164).getPoints())
                .addPoints(cart.getStrip(166).getPoints())
                .addPoints(cart.getStrip(141).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(104).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(101).getPoints())
                .addPoints(cart.getStrip(138).getPoints())
                .addPoints(cart.getStrip(140).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(137).getPoints(), PointsGrouping.REVERSE_ORDERING)
        );

        // horizontal - row 5 - A
        addPixliteOutput(
            new PointsGrouping("14")
                .addPoints(cart.getStrip(109).getPoints())
                .addPoints(cart.getStrip(73).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(36).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(35).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(33).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(32).getPoints())
                .addPoints(cart.getStrip(69).getPoints())
                .addPoints(cart.getStrip(71).getPoints(), PointsGrouping.REVERSE_ORDERING) 
                .addPoints(cart.getStrip(34).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(70).getPoints())
                .addPoints(cart.getStrip(72).getPoints())
        );

        // horizontal - row 5 - B
        addPixliteOutput(
            new PointsGrouping("15")
                .addPoints(cart.getStrip(107).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(106).getPoints())
                .addPoints(cart.getStrip(143).getPoints())
                .addPoints(cart.getStrip(168).getPoints())
                .addPoints(cart.getStrip(169).getPoints())
                .addPoints(cart.getStrip(147).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(110).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(108).getPoints())
                .addPoints(cart.getStrip(145).getPoints())
                .addPoints(cart.getStrip(146).getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(cart.getStrip(144).getPoints(), PointsGrouping.REVERSE_ORDERING)
        );
    }
}