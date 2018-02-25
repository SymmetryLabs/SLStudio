package com.symmetrylabs.slstudio.pixlites;

import heronarts.lx.LX;
import com.symmetrylabs.slstudio.pixlites.NissanPixlite;
import com.symmetrylabs.slstudio.pixlites.PointsGrouping;
import com.symmetrylabs.slstudio.model.NissanModel;

public class NissanPixliteConfigs {

    public static NissanPixlite[] setupPixlites(LX lx) {
        NissanModel model = ((NissanModel)lx.model);

        /**
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         * EXAMPLE
         */
        return new NissanPixlite[] {

//        // CAR 1
//
//            new NissanPixlite(lx, "10.200.1.6")
//                // don't forget strips start at the bottom of windows
//                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-driver-side-front-strip1").getPoints())
//                .addPoints(model.getStripById("car1-driver-side-front-strip2").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-driver-side-front-strip3").getPoints())
//                .addPoints(model.getStripById("car1-driver-side-front-strip4").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-driver-side-front-strip5").getPoints())
//                .addPoints(model.getStripById("car1-driver-side-front-strip6").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-driver-side-front-strip7").getPoints())
//                .addPoints(model.getStripById("car1-driver-side-front-strip8").getPoints(), PointsGrouping.REVERSE_ORDERING)
//            )
//        .addPixliteOutput(new PointsGrouping("2") // <- output index on pixlite
//        .addPoints(model.getStripById("car1-driver-side-front-strip9").getPoints())
//        .addPoints(model.getStripById("car1-driver-side-front-strip10").getPoints(), PointsGrouping.REVERSE_ORDERING)
//        .addPoints(model.getStripById("car1-driver-side-front-strip11").getPoints())
//        .addPoints(model.getStripById("car1-driver-side-front-strip12").getPoints(), PointsGrouping.REVERSE_ORDERING)
//        .addPoints(model.getStripById("car1-driver-side-front-strip13").getPoints())
//        .addPoints(model.getStripById("car1-driver-side-front-strip14").getPoints(), PointsGrouping.REVERSE_ORDERING)
//        .addPoints(model.getStripById("car1-driver-side-front-strip15").getPoints())
//        .addPoints(model.getStripById("car1-driver-side-front-strip16").getPoints(), PointsGrouping.REVERSE_ORDERING)
//      )
//        .addPixliteOutput(new PointsGrouping("3") // <- output index on pixlite
//        .addPoints(model.getStripById("car1-driver-side-front-strip17").getPoints())
//        .addPoints(model.getStripById("car1-driver-side-front-strip18").getPoints(), PointsGrouping.REVERSE_ORDERING)
//        .addPoints(model.getStripById("car1-driver-side-front-strip19").getPoints())
//        .addPoints(model.getStripById("car1-driver-side-front-strip20").getPoints(), PointsGrouping.REVERSE_ORDERING)
//        .addPoints(model.getStripById("car1-driver-side-front-strip21").getPoints())
//        .addPoints(model.getStripById("car1-driver-side-front-strip22").getPoints(), PointsGrouping.REVERSE_ORDERING)
//        .addPoints(model.getStripById("car1-driver-side-front-strip23").getPoints())
//        .addPoints(model.getStripById("car1-driver-side-front-strip24").getPoints(), PointsGrouping.REVERSE_ORDERING)
//        .addPoints(model.getStripById("car1-driver-side-front-strip25").getPoints())
//      )
//                .addPixliteOutput(new PointsGrouping("6") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-driver-side-back-strip1").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-driver-side-back-strip2").getPoints())
//                    .addPoints(model.getStripById("car1-driver-side-back-strip3").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-driver-side-back-strip4").getPoints())
//                    .addPoints(model.getStripById("car1-driver-side-back-strip5").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-driver-side-back-strip6").getPoints())
//                    .addPoints(model.getStripById("car1-driver-side-back-strip7").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-driver-side-back-strip8").getPoints())
//                )
//                .addPixliteOutput(new PointsGrouping("5") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-driver-side-back-strip9").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-driver-side-back-strip10").getPoints())
//                    .addPoints(model.getStripById("car1-driver-side-back-strip11").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-driver-side-back-strip12").getPoints())
//                    .addPoints(model.getStripById("car1-driver-side-back-strip13").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-driver-side-back-strip14").getPoints())
//                    .addPoints(model.getStripById("car1-driver-side-back-strip15").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-driver-side-back-strip16").getPoints())
//                )
//
//                .addPixliteOutput(new PointsGrouping("4") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-driver-side-back-strip17").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-driver-side-back-strip18").getPoints())
//                .addPoints(model.getStripById("car1-driver-side-back-strip19").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-driver-side-back-strip20").getPoints())
//                .addPoints(model.getStripById("car1-driver-side-back-strip21").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-driver-side-back-strip22").getPoints())
//                .addPoints(model.getStripById("car1-driver-side-back-strip23").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-driver-side-back-strip24").getPoints())
//                .addPoints(model.getStripById("car1-driver-side-back-strip25").getPoints(), PointsGrouping.REVERSE_ORDERING)
//
//            ),
//
//            new NissanPixlite(lx, "10.200.1.5")
//                // don't forget strips start at the bottom of windows
//                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-passenger-side-front-strip1").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip2").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip3").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip4").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip5").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip6").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip7").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip8").getPoints(), PointsGrouping.REVERSE_ORDERING)
//            )
//                .addPixliteOutput(new PointsGrouping("2") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-passenger-side-front-strip9").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip10").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip11").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip12").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip13").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip14").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip15").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip16").getPoints(), PointsGrouping.REVERSE_ORDERING)
////                    .addPoints(model.getStripById("car1-passenger-side-front-strip17").getPoints(), PointsGrouping.REVERSE_ORDERING)
////                    .addPoints(model.getStripById("car1-passenger-side-front-strip18").getPoints())
//            )
//                .addPixliteOutput(new PointsGrouping("3") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-passenger-side-front-strip17").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip18").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip19").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip20").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip21").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip22").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip23").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-front-strip24").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-front-strip25").getPoints())
//
//            )
//
//            .addPixliteOutput(new PointsGrouping("16") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-passenger-side-back-strip1").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip2").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip3").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip4").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip5").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip6").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip7").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip8").getPoints())
//            )
//                .addPixliteOutput(new PointsGrouping("15") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-passenger-side-back-strip9").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip10").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip11").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip12").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip13").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip14").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip15").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip16").getPoints())
//            )
//
//                .addPixliteOutput(new PointsGrouping("14") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-passenger-side-back-strip17").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip18").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip19").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip20").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip21").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip22").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip23").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-passenger-side-back-strip24").getPoints())
//                .addPoints(model.getStripById("car1-passenger-side-back-strip25").getPoints(), PointsGrouping.REVERSE_ORDERING)
//
//            ),
//
//            new NissanPixlite(lx, "10.200.1.7")
//                // don't forget strips start at the bottom of windows
//                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
//                     .addPoints(model.getStripById("car1-windshield-strip1").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip2").getPoints())
//                .addPoints(model.getStripById("car1-windshield-strip3").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip4").getPoints())
//
//            )
//                .addPixliteOutput(new PointsGrouping("2") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-windshield-strip5").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-windshield-strip6").getPoints())
//                .addPoints(model.getStripById("car1-windshield-strip7").getPoints(), PointsGrouping.REVERSE_ORDERING)
//
//                .addPoints(model.getStripById("car1-windshield-strip8").getPoints())
//            )
//                .addPixliteOutput(new PointsGrouping("3") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-windshield-strip9").getPoints(), PointsGrouping.REVERSE_ORDERING)
//              .addPoints(model.getStripById("car1-windshield-strip10").getPoints())
//                .addPoints(model.getStripById("car1-windshield-strip11").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip12").getPoints())
//            )
//
//                .addPixliteOutput(new PointsGrouping("4") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-windshield-strip13").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip14").getPoints())
//                .addPoints(model.getStripById("car1-windshield-strip15").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip16").getPoints())
//
//            )
//                .addPixliteOutput(new PointsGrouping("5") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-windshield-strip17").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-windshield-strip18").getPoints())
//                    .addPoints(model.getStripById("car1-windshield-strip19").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip20").getPoints())
//
//            )
//
//                .addPixliteOutput(new PointsGrouping("6") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-windshield-strip21").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-windshield-strip22").getPoints())
//
//                    .addPoints(model.getStripById("car1-windshield-strip23").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip24").getPoints())
//
//            )
//
//                .addPixliteOutput(new PointsGrouping("7") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-windshield-strip25").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-windshield-strip26").getPoints())
//                    .addPoints(model.getStripById("car1-windshield-strip27").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip28").getPoints())
//
//            )
//
//                .addPixliteOutput(new PointsGrouping("8") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-windshield-strip29").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-windshield-strip30").getPoints())
//                    .addPoints(model.getStripById("car1-windshield-strip31").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip32").getPoints())
//
//            )
//
//                .addPixliteOutput(new PointsGrouping("9") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-windshield-strip33").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-windshield-strip34").getPoints())
//                    .addPoints(model.getStripById("car1-windshield-strip35").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip36").getPoints())
//
//            )
//
//                .addPixliteOutput(new PointsGrouping("10") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-windshield-strip37").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-windshield-strip38").getPoints())
//                    .addPoints(model.getStripById("car1-windshield-strip39").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip40").getPoints())
//
//            )
//
//                .addPixliteOutput(new PointsGrouping("11") // <- output index on pixlite
//                    .addPoints(model.getStripById("car1-windshield-strip41").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car1-windshield-strip42").getPoints())
//                    .addPoints(model.getStripById("car1-windshield-strip43").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip44").getPoints())
//
//            )
//
//                .addPixliteOutput(new PointsGrouping("12") // <- output index on pixlite
//                .addPoints(model.getStripById("car1-windshield-strip45").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip46").getPoints())
//                    .addPoints(model.getStripById("car1-windshield-strip47").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip48").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip49").getPoints())
//                .addPoints(model.getStripById("car1-windshield-strip50").getPoints())
//                .addPoints(model.getStripById("car1-windshield-strip51").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip52").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car1-windshield-strip53").getPoints())
//                .addPoints(model.getStripById("car1-windshield-strip54").getPoints())
//            ),


                // CAR 3

            new NissanPixlite(lx, "10.200.1.10")
                // don't forget strips start at the bottom of windows
                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-driver-side-front-strip1").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip2").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip3").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip4").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip5").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip6").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip7").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip8").getPoints(), PointsGrouping.REVERSE_ORDERING)
                )
                .addPixliteOutput(new PointsGrouping("2") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-driver-side-front-strip9").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip10").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip11").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip12").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip13").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip14").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip15").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip16").getPoints(), PointsGrouping.REVERSE_ORDERING)
                )
                .addPixliteOutput(new PointsGrouping("3") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-driver-side-front-strip17").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip18").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip19").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip20").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip21").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip22").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip23").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-front-strip24").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-front-strip25").getPoints())
                )
                .addPixliteOutput(new PointsGrouping("6") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-driver-side-back-strip1").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-back-strip2").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-back-strip3").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-back-strip4").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-back-strip5").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-back-strip6").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-back-strip7").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-back-strip8").getPoints())
                )
                .addPixliteOutput(new PointsGrouping("5") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-driver-side-back-strip9").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-back-strip10").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-back-strip11").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-back-strip12").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-back-strip13").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-back-strip14").getPoints())
                    .addPoints(model.getStripById("car3-driver-side-back-strip15").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-driver-side-back-strip16").getPoints())
                )

                .addPixliteOutput(new PointsGrouping("4") // <- output index on pixlite
                .addPoints(model.getStripById("car3-driver-side-back-strip17").getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(model.getStripById("car3-driver-side-back-strip18").getPoints())
                .addPoints(model.getStripById("car3-driver-side-back-strip19").getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(model.getStripById("car3-driver-side-back-strip20").getPoints())
                .addPoints(model.getStripById("car3-driver-side-back-strip21").getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(model.getStripById("car3-driver-side-back-strip22").getPoints())
                .addPoints(model.getStripById("car3-driver-side-back-strip23").getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(model.getStripById("car3-driver-side-back-strip24").getPoints())
                .addPoints(model.getStripById("car3-driver-side-back-strip25").getPoints(), PointsGrouping.REVERSE_ORDERING)

            ),

            new NissanPixlite(lx, "10.200.1.9")
                // don't forget strips start at the bottom of windows
                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-passenger-side-front-strip1").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-front-strip2").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-front-strip3").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-front-strip4").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-front-strip5").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-front-strip6").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-front-strip7").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-front-strip8").getPoints(), PointsGrouping.REVERSE_ORDERING)
                )
                .addPixliteOutput(new PointsGrouping("2") // <- output index on pixlite
                        .addPoints(model.getStripById("car3-passenger-side-front-strip9").getPoints())
                        .addPoints(model.getStripById("car3-passenger-side-front-strip10").getPoints(), PointsGrouping.REVERSE_ORDERING)
                        .addPoints(model.getStripById("car3-passenger-side-front-strip11").getPoints())
                        .addPoints(model.getStripById("car3-passenger-side-front-strip12").getPoints(), PointsGrouping.REVERSE_ORDERING)
                        .addPoints(model.getStripById("car3-passenger-side-front-strip13").getPoints())
                        .addPoints(model.getStripById("car3-passenger-side-front-strip14").getPoints(), PointsGrouping.REVERSE_ORDERING)
                        .addPoints(model.getStripById("car3-passenger-side-front-strip15").getPoints())
                        .addPoints(model.getStripById("car3-passenger-side-front-strip16").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-passenger-side-front-strip17").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-passenger-side-front-strip18").getPoints())
                )
                .addPixliteOutput(new PointsGrouping("3") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-passenger-side-front-strip17").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-front-strip18").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-front-strip19").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-front-strip20").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-front-strip21").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-front-strip22").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-front-strip23").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-front-strip24").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-front-strip25").getPoints())

                )

                .addPixliteOutput(new PointsGrouping("16") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-passenger-side-back-strip1").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-back-strip2").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-back-strip3").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-back-strip4").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-back-strip5").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-back-strip6").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-back-strip7").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-back-strip8").getPoints())
                )
                .addPixliteOutput(new PointsGrouping("15") // <- output index on pixlite
                    .addPoints(model.getStripById("car3-passenger-side-back-strip9").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-back-strip10").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-back-strip11").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-back-strip12").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-back-strip13").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-back-strip14").getPoints())
                    .addPoints(model.getStripById("car3-passenger-side-back-strip15").getPoints(), PointsGrouping.REVERSE_ORDERING)
                    .addPoints(model.getStripById("car3-passenger-side-back-strip16").getPoints())
                )

                .addPixliteOutput(new PointsGrouping("14") // <- output index on pixlite
                .addPoints(model.getStripById("car3-passenger-side-back-strip17").getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(model.getStripById("car3-passenger-side-back-strip18").getPoints())
                .addPoints(model.getStripById("car3-passenger-side-back-strip19").getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(model.getStripById("car3-passenger-side-back-strip20").getPoints())
                .addPoints(model.getStripById("car3-passenger-side-back-strip21").getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(model.getStripById("car3-passenger-side-back-strip22").getPoints())
                .addPoints(model.getStripById("car3-passenger-side-back-strip23").getPoints(), PointsGrouping.REVERSE_ORDERING)
                .addPoints(model.getStripById("car3-passenger-side-back-strip24").getPoints())
                .addPoints(model.getStripById("car3-passenger-side-back-strip25").getPoints(), PointsGrouping.REVERSE_ORDERING)

            ),
//            new NissanPixlite(lx, "10.200.1.8")
//                // don't forget strips start at the bottom of windows
//                .addPixliteOutput(new PointsGrouping("1") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip1").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip2").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip3").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip4").getPoints())
//
//                )
//                .addPixliteOutput(new PointsGrouping("2") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip5").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip6").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip7").getPoints(), PointsGrouping.REVERSE_ORDERING)
//
//                    .addPoints(model.getStripById("car3-windshield-strip8").getPoints())
//                )
//                .addPixliteOutput(new PointsGrouping("3") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip9").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip10").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip11").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip12").getPoints())
//                )
//
//                .addPixliteOutput(new PointsGrouping("4") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip13").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip14").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip15").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip16").getPoints())
//
//                )
//                .addPixliteOutput(new PointsGrouping("5") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip17").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip18").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip19").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip20").getPoints())
//
//                )
//
//                .addPixliteOutput(new PointsGrouping("6") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip21").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip22").getPoints())
//
//                    .addPoints(model.getStripById("car3-windshield-strip23").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip24").getPoints())
//
//                )
//
//                .addPixliteOutput(new PointsGrouping("7") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip25").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip26").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip27").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip28").getPoints())
//
//                )
//
//                .addPixliteOutput(new PointsGrouping("8") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip29").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip30").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip31").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip32").getPoints())
//
//                )
//
//                .addPixliteOutput(new PointsGrouping("9") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip33").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip34").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip35").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip36").getPoints())
//
//                )
//
//                .addPixliteOutput(new PointsGrouping("10") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip37").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip38").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip39").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip40").getPoints())
//
//                )
//
//                .addPixliteOutput(new PointsGrouping("11") // <- output index on pixlite
//                    .addPoints(model.getStripById("car3-windshield-strip41").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip42").getPoints())
//                    .addPoints(model.getStripById("car3-windshield-strip43").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                    .addPoints(model.getStripById("car3-windshield-strip44").getPoints())
//
//                )
//
//                .addPixliteOutput(new PointsGrouping("12") // <- output index on pixlite
//                .addPoints(model.getStripById("car3-windshield-strip45").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car3-windshield-strip46").getPoints())
//                .addPoints(model.getStripById("car3-windshield-strip47").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car3-windshield-strip48").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car3-windshield-strip49").getPoints())
//                .addPoints(model.getStripById("car3-windshield-strip50").getPoints())
//                .addPoints(model.getStripById("car3-windshield-strip51").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car3-windshield-strip52").getPoints(), PointsGrouping.REVERSE_ORDERING)
//                .addPoints(model.getStripById("car3-windshield-strip53").getPoints())
//                .addPoints(model.getStripById("car3-windshield-strip54").getPoints())
//            )


        };


    }
}
