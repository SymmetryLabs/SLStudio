public class Vortex extends LXPattern {
  public String getAuthor() {
    return "Mark C. Slee and Alex Green";
  }
  
  public final CompoundParameter speed = (CompoundParameter)
    new CompoundParameter("Speed", 2000, 15000, 300)
    .setExponent(.2)
    .setDescription("Speed of vortex motion");
  
  public final CompoundParameter size =
    new CompoundParameter("Size",  2*INCHES, 0, 50*INCHES)
    .setDescription("Size of vortex");
  
  public final CompoundParameter xPos = (CompoundParameter)
    new CompoundParameter("XPos", model.cx, model.xMin, model.xMax)
    .setPolarity(LXParameter.Polarity.BIPOLAR)
    .setDescription("X-position of vortex center");
    
  public final CompoundParameter yPos = (CompoundParameter)
    new CompoundParameter("YPos", model.cy, model.yMin, model.yMax)
    .setPolarity(LXParameter.Polarity.BIPOLAR)
    .setDescription("Y-position of vortex center");
    
  public final CompoundParameter xSlope = (CompoundParameter)
    new CompoundParameter("XSlp", .2, -1, 1)
    .setPolarity(LXParameter.Polarity.BIPOLAR)
    .setDescription("X-slope of vortex center");
    
  public final CompoundParameter ySlope = (CompoundParameter)
    new CompoundParameter("YSlp", .5, -1, 1)
    .setPolarity(LXParameter.Polarity.BIPOLAR)
    .setDescription("Y-slope of vortex center");
    
  public final CompoundParameter zSlope = (CompoundParameter)
    new CompoundParameter("ZSlp", .3, -1, 1)
    .setPolarity(LXParameter.Polarity.BIPOLAR)
    .setDescription("Z-slope of vortex center");
  
  private final LXModulator pos = startModulator(new SawLFO(1, 0, this.speed));
  
  private final LXModulator sizeDamped = startModulator(new DampedParameter(this.size, 0, 2*INCHES));
  private final LXModulator xPosDamped = startModulator(new DampedParameter(this.xPos, model.xRange, 2*model.xRange));
  private final LXModulator yPosDamped = startModulator(new DampedParameter(this.yPos, model.yRange, 2*model.yRange));
  private final LXModulator xSlopeDamped = startModulator(new DampedParameter(this.xSlope, .1));
  private final LXModulator ySlopeDamped = startModulator(new DampedParameter(this.ySlope, .1));
  private final LXModulator zSlopeDamped = startModulator(new DampedParameter(this.zSlope, .1));

  public Vortex(LX lx) {
    super(lx);
    addParameter("speed", this.speed);
    addParameter("size", this.size);
    addParameter("xPos", this.xPos);
    addParameter("yPos", this.yPos);
    addParameter("xSlope", this.xSlope);
    addParameter("ySlope", this.ySlope);
    addParameter("zSlope", this.zSlope);
  }

  public void run(double deltaMs) {
    final float xPos = this.xPosDamped.getValuef();
    final float yPos = this.yPosDamped.getValuef();
    final float size = this.sizeDamped.getValuef();
    final float pos = this.pos.getValuef();
    
    final float xSlope = this.xSlopeDamped.getValuef();
    final float ySlope = this.ySlopeDamped.getValuef();
    final float zSlope = this.zSlopeDamped.getValuef();

    float dMult = 100/ size;
    for (LXPoint point : model.points) {
      float radix = abs((xSlope*abs(point.x-model.cx) + ySlope*abs(point.y-model.cy) + zSlope*abs(point.z-model.cz)));
      float dist = dist(point.x, point.y, xPos, yPos); 
    //  float falloff = 100 / max(.5*INCHES, 2*size - .5*dist);
      //float b = 100 - falloff * LXUtils.wrapdistf(radix, pos * size, size);
      float b = abs(((dist + radix + pos * size) % size)*dMult - 1);
      colors[point.index] = lx.hsb(palette.getHuef(), 100, b);
      // setColor(leaf, (b > 0) ? LXColor.gray(b*b*100) : #000000);
    }
  }
}