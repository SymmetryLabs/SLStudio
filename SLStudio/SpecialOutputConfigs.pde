public class Sun5BackTopOutputConfig {
    public Sun5BackTopOutputConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("1")
            .addPoints(slice.getStripById("1").points,  PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("2").points)
            .addPoints(slice.getStripById("3").points,  PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("4").points)
            .addPoints(slice.getStripById("5").points,  PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("6").points)
            .addPoints(slice.getStripById("7").points,  PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("8").points)
            .addPoints(slice.getStripById("9").points,  PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("10").points)
            .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("12").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("2")
            .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("14").points)
            .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("16").points)
            .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("18").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("3")
            .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("20").points)
            .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("22").points)
            .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("4")
            .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("25").points)
            .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("27").points)
            .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("5")
            .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("30").points)
            .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING) // short
            .addPoints(slice.getStripById("32").points) // short
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("6")
            .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("34").points)
            .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("36").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("7")
            .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("38").points)
            .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("40").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("8")
            .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("42").points)
            .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("44").points)
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
            new PointsGrouping("9")
              .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING)
              .addPoints(slice.getStripById("46").points) // even used??
              .addPoints(slice.getStripById("47").points, PointsGrouping.REVERSE_ORDERING) //\even used??
              .addPoints(slice.getStripById("48").points) // even used??
        ));

        pixlite.addChild(new PixliteOutput(lx, ipAddress,
          new PointsGrouping("10")
            .addPoints(slice.getStripById("49").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("50").points)
            .addPoints(slice.getStripById("51").points, PointsGrouping.REVERSE_ORDERING)
            .addPoints(slice.getStripById("52").points)
        ));

        // pixlite.addChild(new PixliteOutput(lx, ipAddress, 
        //   new PointsGrouping("11")
        //     .addPoints(slice.getStripById("53").points, PointsGrouping.REVERSE_ORDERING)
        //     .addPoints(slice.getStripById("54").points)
        //     .addPoints(slice.getStripById("55").points, PointsGrouping.REVERSE_ORDERING)
        //     .addPoints(slice.getStripById("56").points)
        // ));

        // pixlite.addChild(new PixliteOutput(lx, ipAddress,
        //   new PointsGrouping("12")
        //     .addPoints(slice.getStripById("57").points, PointsGrouping.REVERSE_ORDERING)
        //     .addPoints(slice.getStripById("58").points)
        //     .addPoints(slice.getStripById("59").points, PointsGrouping.REVERSE_ORDERING)
        // ));

        // pixlite.addChild(new PixliteOutput(lx, ipAddress,
        //   new PointsGrouping("13")
        //     .addPoints(slice.getStripById("60").points, PointsGrouping.REVERSE_ORDERING)
        //     .addPoints(slice.getStripById("61").points)
        //     .addPoints(slice.getStripById("62").points, PointsGrouping.REVERSE_ORDERING)
        // ));

        // pixlite.addChild(new PixliteOutput(lx, ipAddress,
        //   new PointsGrouping("14")
        //     .addPoints(slice.getStripById("63").points, PointsGrouping.REVERSE_ORDERING)
        //     .addPoints(slice.getStripById("64").points)
        //     .addPoints(slice.getStripById("65").points, PointsGrouping.REVERSE_ORDERING)
        // ));

        // pixlite.addChild(new PixliteOutput(lx, ipAddress,
        //   new PointsGrouping("15")
        //     .addPoints(slice.getStripById("66").points, PointsGrouping.REVERSE_ORDERING)
        //     .addPoints(slice.getStripById("67").points)
        //     .addPoints(slice.getStripById("68").points, PointsGrouping.REVERSE_ORDERING)
        // ));

        // pixlite.addChild(new PixliteOutput(lx, ipAddress,
        //   new PointsGrouping("16")
        //     .addPoints(slice.getStripById("69").points, PointsGrouping.REVERSE_ORDERING)  
        // ));
    }
}

public class Sun3BackTopOutputConfig {
  public Sun3BackTopOutputConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
    println("Special output configuration for (" + slice.id + ", " + ipAddress + ")");

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("1")
        .addPoints(slice.getStripById("1").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("2").points)
        .addPoints(slice.getStripById("3").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("4").points)
        .addPoints(slice.getStripById("5").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("6").points)
        .addPoints(slice.getStripById("7").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("8").points)
        .addPoints(slice.getStripById("9").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("10").points)
        .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("12").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("2")
        .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("14").points)
        .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("16").points)
        .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("18").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("3")
        .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("20").points)
        .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("22").points)
        .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("4")
        .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("25").points)
        .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("27").points)
        .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("5")
        .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("30").points)
        .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("32").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("6")
        .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("34").points)
        .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("36").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("7")
        .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("38").points)
        .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("40").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("8")
        .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("42").points)
        .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("44").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("9")
        .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING)
    ));
  }
}

public class Sun3FrontTopOutputConfig {
  public Sun3FrontTopOutputConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
    println("Special output configuration for (" + slice.id + ", " + ipAddress + ")");

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("1")
        .addPoints(slice.getStripById("1").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("2").points)
        .addPoints(slice.getStripById("3").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("4").points)
        .addPoints(slice.getStripById("5").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("6").points)
        .addPoints(slice.getStripById("7").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("8").points)
        .addPoints(slice.getStripById("9").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("10").points)
        .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("12").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("2")
        .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("14").points)
        .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("16").points)
        .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("18").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("3")
        .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("20").points)
        .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("22").points)
        .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("4")
        .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("25").points)
        .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("27").points)
        .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("5")
        .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("30").points)
        .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("32").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("6")
        .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("34").points)
        .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("36").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("7")
        .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("38").points, PointsGrouping.Shift.LEFT)
        .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("40").points, PointsGrouping.Shift.LEFT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("8")
        .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("42").points)
        .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("44").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("9")
        .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING)
    ));
  }
}

public class Sun6BackTopOutputConfig {
  public Sun6BackTopOutputConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
    println("Special output configuration for (" + slice.id + ", " + ipAddress + ")");

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("1")
        .addPoints(slice.getStripById("1").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("2").points)
        .addPoints(slice.getStripById("3").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("4").points)
        .addPoints(slice.getStripById("5").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("6").points)
        .addPoints(slice.getStripById("7").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("8").points)
        .addPoints(slice.getStripById("9").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("10").points)
        .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("12").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("2")
        .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("14").points)
        .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.LEFT)
        .addPoints(slice.getStripById("16").points)
        .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("18").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("3")
        .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("20").points)
        .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("22").points)
        .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("4")
        .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("25").points)
        .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("27").points)
        .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("5")
        .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("30").points)
        .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("32").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("6")
        .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("34").points)
        .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("36").points, PointsGrouping.Shift.LEFT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("7")
        .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("38").points)
        .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("40").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("8")
        .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("42").points)
        .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("44").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("9")
        .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("46").points)
        .addPoints(slice.getStripById("47").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("48").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("10")
        .addPoints(slice.getStripById("49").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("50").points)
        .addPoints(slice.getStripById("51").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("52").points, PointsGrouping.Shift.LEFT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress, 
      new PointsGrouping("11")
        .addPoints(slice.getStripById("53").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("54").points)
        .addPoints(slice.getStripById("55").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("56").points, PointsGrouping.Shift.LEFT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("12")
        .addPoints(slice.getStripById("57").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("58").points)
        .addPoints(slice.getStripById("59").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("13")
        .addPoints(slice.getStripById("60").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("61").points, PointsGrouping.Shift.LEFT)
        .addPoints(slice.getStripById("62").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("14")
        .addPoints(slice.getStripById("63").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("64").points)
        .addPoints(slice.getStripById("65").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("15")
        .addPoints(slice.getStripById("66").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("67").points, PointsGrouping.Shift.LEFT)
        .addPoints(slice.getStripById("68").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("16")
        .addPoints(slice.getStripById("69").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)  
    ));
  }
}

public class Sun6FrontTopOutputConfig {
  public Sun6FrontTopOutputConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
    println("Special output configuration for (" + slice.id + ", " + ipAddress + ")");

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("1")
        .addPoints(slice.getStripById("1").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("2").points)
        .addPoints(slice.getStripById("3").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("4").points)
        .addPoints(slice.getStripById("5").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("6").points)
        .addPoints(slice.getStripById("7").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("8").points)
        .addPoints(slice.getStripById("9").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("10").points)
        .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("12").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("2")
        .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("14").points)
        .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("16").points)
        .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("18").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("3")
        .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("20").points)
        .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("22").points)
        .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("4")
        .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("25").points)
        .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("27").points)
        .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("5")
        .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("30").points)
        .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("32").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("6")
        .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("34").points)
        .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("36").points, PointsGrouping.Shift.RIGHT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("7")
        .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("38").points, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("40").points, PointsGrouping.Shift.RIGHT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("8")
        .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("42").points)
        .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("44").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("9")
        .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("46").points) // even used??
        .addPoints(slice.getStripById("47").points, PointsGrouping.REVERSE_ORDERING) // even used??
        .addPoints(slice.getStripById("48").points) // even used??
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("10")
        .addPoints(slice.getStripById("49").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("50").points)
        .addPoints(slice.getStripById("51").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("52").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress, 
      new PointsGrouping("11")
        .addPoints(slice.getStripById("53").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("54").points)
        .addPoints(slice.getStripById("55").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("56").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("12")
        .addPoints(slice.getStripById("57").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("58").points)
        .addPoints(slice.getStripById("59").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("13")
        .addPoints(slice.getStripById("60").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("61").points)
        .addPoints(slice.getStripById("62").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("14")
        .addPoints(slice.getStripById("63").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("64").points)
        .addPoints(slice.getStripById("65").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("15")
        .addPoints(slice.getStripById("66").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("67").points)
        .addPoints(slice.getStripById("68").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("16")
        .addPoints(slice.getStripById("69").points, PointsGrouping.REVERSE_ORDERING)  
    ));
  }
}

public class Sun1BackOutputConfig {
  public Sun1BackOutputConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
	println("Special output configuration for (" + slice.id + ", " + ipAddress + ")");

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("1")
        .addPoints(slice.getStripById("1").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("2").points)
        .addPoints(slice.getStripById("3").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("4").points)
        .addPoints(slice.getStripById("5").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("6").points)
        .addPoints(slice.getStripById("7").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("8").points)
        .addPoints(slice.getStripById("9").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("10").points)
        .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("12").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("2")
        .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("14").points)
        .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("16").points)
        .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("18").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("3")
        .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("20").points)
        .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("22").points)
        .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("4")
        .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("25").points)
        .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("27").points)
        .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("5")
        .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("30").points)
        .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("32").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("6")
        .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("34").points)
        .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("36").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("7")
        .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("38").points, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("40").points, PointsGrouping.Shift.LEFT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("8")
        .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("42").points)
        .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("44").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("9")
        .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING)
    ));
  }
}


public class Sun1FrontOutputConfig {
  public Sun1FrontOutputConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
	println("Special output configuration for (" + slice.id + ", " + ipAddress + ")");

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("1")
        .addPoints(slice.getStripById("1").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("2").points)
        .addPoints(slice.getStripById("3").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("4").points)
        .addPoints(slice.getStripById("5").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("6").points)
        .addPoints(slice.getStripById("7").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("8").points)
        .addPoints(slice.getStripById("9").points,  PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("10").points, PointsGrouping.Shift.LEFT)
        .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("12").points, PointsGrouping.Shift.LEFT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("2")
        .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("14").points)
        .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("16").points)
        .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("18").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("3")
        .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("20").points, PointsGrouping.Shift.LEFT)
        .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("22").points)
        .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("4")
        .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("25").points)
        .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("27").points)
        .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("5")
        .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("30").points)
        .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("32").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("6")
        .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("34").points)
        .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("36").points)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("7")
        .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("38").points)
        .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING, PointsGrouping.Shift.RIGHT)
        .addPoints(slice.getStripById("40").points, PointsGrouping.Shift.LEFT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("8")
        .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("42").points)
        .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
        .addPoints(slice.getStripById("44").points, PointsGrouping.Shift.RIGHT)
    ));

    pixlite.addChild(new PixliteOutput(lx, ipAddress,
      new PointsGrouping("9")
        .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING)
    ));
  }
}

public class Sun4BackOutputConfig {
  public Sun4BackOutputConfig(LX lx, Slice slice, String ipAddress, Pixlite pixlite) throws SocketException {
	println("Special output configuration for (" + slice.id + ", " + ipAddress + ")");

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("1")
	    .addPoints(slice.getStripById("1").points,  PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("2").points)
	    .addPoints(slice.getStripById("3").points,  PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("4").points)
	    .addPoints(slice.getStripById("5").points,  PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("6").points)
	    .addPoints(slice.getStripById("7").points,  PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("8").points)
	    .addPoints(slice.getStripById("9").points,  PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("10").points)
	    .addPoints(slice.getStripById("11").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("12").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("2")
	    .addPoints(slice.getStripById("13").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("14").points)
	    .addPoints(slice.getStripById("15").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("16").points)
	    .addPoints(slice.getStripById("17").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("18").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("3")
	    .addPoints(slice.getStripById("19").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("20").points)
	    .addPoints(slice.getStripById("21").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("22").points)
	    .addPoints(slice.getStripById("23").points, PointsGrouping.REVERSE_ORDERING)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("4")
	    .addPoints(slice.getStripById("24").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("25").points)
	    .addPoints(slice.getStripById("26").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("27").points)
	    .addPoints(slice.getStripById("28").points, PointsGrouping.REVERSE_ORDERING)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("5")
	    .addPoints(slice.getStripById("29").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("30").points)
	    .addPoints(slice.getStripById("31").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("32").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("6")
	    .addPoints(slice.getStripById("33").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("34").points)
	    .addPoints(slice.getStripById("35").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("36").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("7")
	    .addPoints(slice.getStripById("37").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("38").points)
	    .addPoints(slice.getStripById("39").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("40").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("8")
	    .addPoints(slice.getStripById("41").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("42").points)
	    .addPoints(slice.getStripById("43").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("44").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("9")
	    .addPoints(slice.getStripById("45").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("46").points)
	    .addPoints(slice.getStripById("47").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("48").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("10")
	    .addPoints(slice.getStripById("49").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("50").points)
	    .addPoints(slice.getStripById("51").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("52").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress, 
	  new PointsGrouping("11")
	    .addPoints(slice.getStripById("54").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("55").points)
	    .addPoints(slice.getStripById("56").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("57").points)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("12")
	    .addPoints(slice.getStripById("58").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("59").points)
	    .addPoints(slice.getStripById("60").points, PointsGrouping.REVERSE_ORDERING)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("13")
	    .addPoints(slice.getStripById("61").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("62").points)
	    .addPoints(slice.getStripById("63").points, PointsGrouping.REVERSE_ORDERING)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("14")
	    .addPoints(slice.getStripById("64").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("65").points)
	    .addPoints(slice.getStripById("66").points, PointsGrouping.REVERSE_ORDERING)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("15")
	    .addPoints(slice.getStripById("67").points, PointsGrouping.REVERSE_ORDERING)
	    .addPoints(slice.getStripById("68").points)
	    .addPoints(slice.getStripById("69").points, PointsGrouping.REVERSE_ORDERING)
	));

	pixlite.addChild(new PixliteOutput(lx, ipAddress,
	  new PointsGrouping("16")
	    .addPoints(slice.getStripById("53").points, PointsGrouping.REVERSE_ORDERING)  
	));
  }
}