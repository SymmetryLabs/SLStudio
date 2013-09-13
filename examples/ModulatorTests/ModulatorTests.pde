import heronarts.lx.*;
import heronarts.lx.modulator.*;
import heronarts.lx.pattern.*;
import ddf.minim.*;

void setup() {
  size(400, 200);
  HeronLX lx = new HeronLX(this, 20, 1);
  lx.setPatterns(new LXPattern[] {
    new AcceleratorTest(lx),
    new ClickTest(lx),
    new LinearEnvelopeTest(lx),
    new QuadraticEnvelopeTest(lx),
    new SawLFOTest(lx),
    new SinLFOTest(lx),
    new SquareLFOTest(lx),
    new TriangleLFOTest(lx),
  });
}

void draw() {}

class AcceleratorTest extends LXPattern {
  Accelerator a = new Accelerator(lx.total/2, 5, -5);
  
  AcceleratorTest(HeronLX lx) {
    super(lx);
    addModulator(a.trigger());
  }
  
  public void run(int deltaMs) {
    if (a.getValue() < 0) {
      a.trigger();
    }
    for (int i = 0; i < colors.length; ++i) {
      colors[i] = color(
        lx.getBaseHuef(),
        100,
        max(0, 100 - 30*abs(i - a.getValuef()))
      );
    }
  }
}

class ClickTest extends LXPattern {
  Click click = new Click(1000);
  
  ClickTest(HeronLX lx) {
    super(lx);
    addModulator(click.trigger());
  }
  
  public void run(int deltaMs) {
    for (int i = 0; i < colors.length; ++i) {
      colors[i] = color(
        lx.getBaseHuef(),
        100,
        click.click() ? 100 : 0
      );
    }
  }
}

abstract class EnvelopeTest extends LXPattern {
  RangeModulator env;
  Click click = new Click(2000);
  
  EnvelopeTest(HeronLX lx, RangeModulator rm) {
    super(lx);
    addModulator(env = rm).trigger();
    addModulator(click.trigger());
  }
  
  public void run(int deltaMs) {
    if (click.click()) {
      env.setRangeFromHereTo(random(0, lx.total)).start();
    }
    for (int i = 0; i < colors.length; ++i) {
      colors[i] = color(
        lx.getBaseHuef(),
        100,
        max(0, 100 - 30*abs(i - env.getValuef()))
      );
    }
  }
}

class LinearEnvelopeTest extends EnvelopeTest {
  
  LinearEnvelopeTest(HeronLX lx) {
    super(lx, new LinearEnvelope(0, lx.total, 1000));
  }
}

class QuadraticEnvelopeTest extends EnvelopeTest {
  
  QuadraticEnvelopeTest(HeronLX lx) {
    super(lx, new QuadraticEnvelope(0, lx.total, 1000).setEase(QuadraticEnvelope.Ease.BOTH));
  }
}

abstract class OscillatorTest extends LXPattern {
  RangeModulator env;
  
  OscillatorTest(HeronLX lx, RangeModulator rm) {
    super(lx);
    addModulator(env = rm).trigger();
  }
  
  public void run(int deltaMs) {
    for (int i = 0; i < colors.length; ++i) {
      colors[i] = color(
        lx.getBaseHuef(),
        100,
        max(0, 100 - 30*abs(i - env.getValuef()))
      );
    }
  }
}

class SawLFOTest extends OscillatorTest {
  SawLFOTest(HeronLX lx) {
    super(lx, new SawLFO(0, lx.total, 1000));
  }
}

class SinLFOTest extends OscillatorTest {
  SinLFOTest(HeronLX lx) {
    super(lx, new SinLFO(0, lx.total, 1000));
  }
}

class SquareLFOTest extends OscillatorTest {
  SquareLFOTest(HeronLX lx) {
    super(lx, new SquareLFO(0, lx.total, 1000));
  }
}

class TriangleLFOTest extends OscillatorTest {
  TriangleLFOTest(HeronLX lx) {
    super(lx, new TriangleLFO(0, lx.total, 1000));
  }
}
