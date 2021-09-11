package art.lookingup.ui;

import art.lookingup.ParameterFile;
import art.lookingup.PropertyFile;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.osc.*;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.*;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class UIConfig extends UICollapsibleSection implements LXParameterListener {
  private static final Logger logger = Logger.getLogger(UIConfig.class.getName());
  public static Map<String, UIConfig> allConfigs = new HashMap<String, UIConfig>();

  public ParameterFile paramFile;
  public List<LXParameter> parameters = new ArrayList<LXParameter>();
  public Map<String, LXParameter> paramLookup = new HashMap<String, LXParameter>();
  public String title;
  public String filename;
  float contentWidth;

  /**
   * Creates a UIConfig object with backing json file.
   * @param ui Adds the config section to the left pane of the LXStudio.UI object.
   * @param title Title of the config section.  Must not contain spaces for OSC address compatibility.
   * @param filename The name of the backing json file to store the values.
   */
  public UIConfig(final SLStudioLX.UI ui, String title, String filename) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    this.title = title;
    this.filename = filename;
    load();
    setTitle(title);
    // Keep track of all UIConfigs so we have a handy reference for exposing them via
    // OSC.  Eventually helpful for OSCQuery.
    allConfigs.put(title, this);
    contentWidth = ui.leftPane.global.getContentWidth();
  }

  /**
   * Creates a UIConfig object with backing json file where the ParameterFile has already been
   * pre-loaded.  UIConfigs are instantiated after the model has been created and LX Studio has
   * been instantiated.
   * @param ui
   * @param title
   * @param filename
   * @param paramFile
   */
  public UIConfig(final SLStudioLX.UI ui, String title, String filename, ParameterFile paramFile) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    this.title = title;
    this.filename = filename;
    this.paramFile = paramFile;
    setTitle(title);
    // Keep track of all UIConfigs so we have a handy reference for exposing them via
    // OSC.  Eventually helpful for OSCQuery.
    allConfigs.put(title, this);
  }

  static public Map<String, UIConfig> getAllConfigs() {
    return allConfigs;
  }

  static public UIConfig getUIConfig(String title) {
    return allConfigs.get(title);
  }

  public void load() {
    paramFile = new ParameterFile(filename);
    try {
      paramFile.load();
    } catch (PropertyFile.NotFound nfex) {
      // System.out.println(filename + ", property not found.");
    } catch (IOException ioex) {
      logger.info(filename + " not found, will be created.");
    }
  }

  public StringParameter registerStringParameter(String label, String value) {
    StringParameter sp = paramFile.getStringParameter(label, value);
    parameters.add(sp);
    paramLookup.put(label, sp);
    return sp;
  }

  public CompoundParameter registerCompoundParameter(String label, double value, double base, double range) {
    CompoundParameter cp = paramFile.getCompoundParameter(label, value, base, range);
    parameters.add(cp);
    paramLookup.put(label, cp);
    return cp;
  }

  public DiscreteParameter registerDiscreteParameter(String label, int value, int min, int max) {
    DiscreteParameter dp = paramFile.getDiscreteParameter(label, value, min, max);
    parameters.add(dp);
    paramLookup.put(label, dp);
    return dp;
  }

  public BooleanParameter registerBooleanParameter(String label, boolean value) {
    BooleanParameter bp = paramFile.getBooleanParameter(label, value);
    parameters.add(bp);
    paramLookup.put(label, bp);
    return bp;
  }

  public StringParameter getStringParameter(String label) {
    return (StringParameter) paramLookup.get(label);
  }

  public CompoundParameter getCompoundParameter(String label) {
    return (CompoundParameter) paramLookup.get(label);
  }

  public DiscreteParameter getDiscreteParameter(String label) {
    return (DiscreteParameter) paramLookup.get(label);
  }

  public BooleanParameter getBooleanParameter(String label) { return (BooleanParameter) paramLookup.get(label); }

  public void save() {
    try {
      paramFile.save();
    } catch (IOException ioex) {
      System.err.println("Error saving " + filename + " " + ioex.getMessage());
    }
    onSave();
  }

  public void onParameterChanged(LXParameter p) {
    OscMessage oscMessage = new OscMessage("");
    String address = "/kaledoscope/" + title + "/" + p.getLabel();
    oscMessage.setAddressPattern(address);
    if (p instanceof DiscreteParameter) {
      OscInt oscInt = new OscInt(((DiscreteParameter) p).getValuei());
      oscMessage.add(oscInt);
    } else if (p instanceof StringParameter) {
      OscString oscString = new OscString(((StringParameter)p).getString());
      oscMessage.add(oscString);
    } else if (p instanceof CompoundParameter) {
      OscFloat oscFloat = new OscFloat(((CompoundParameter)p).getValuef());
      oscMessage.add(oscFloat);
    }
  }

  public void buildUI(SLStudioLX.UI ui) {
    buildUI(ui, 4);
  }

  public void buildUI(SLStudioLX.UI ui, int knobsPerRow) {
    int knobCountThisRow = 0;
    //setTitle(title);
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    UI2dContainer horizContainer = null;
    for (LXParameter p : parameters) {
      if (p instanceof BooleanParameter) {
        UIButton button = (UIButton) new UIButton(0, 0, getContentWidth(), 18)
            .setParameter((BooleanParameter)p)
            .setLabel(p.getLabel())
            .addToContainer(this);
        ((LXListenableParameter)p).addListener(this);
      }
      else if (p instanceof LXListenableNormalizedParameter) {
        if (knobCountThisRow == 0) {
          horizContainer = new UI2dContainer(0, 30, ui.leftPane.global.getContentWidth(), 45);
          horizContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
          horizContainer.setPadding(0, 0, 0, 0);
          horizContainer.addToContainer(this);
        }
        UIKnob knob = new UIKnob((LXListenableNormalizedParameter)p);
        ((LXListenableParameter)p).addListener(this);
        knob.addToContainer(horizContainer);
        ++knobCountThisRow;
        if (knobCountThisRow == knobsPerRow) {
          knobCountThisRow = 0;
        }
      }
      if (p instanceof StringParameter) {
        knobCountThisRow = 0; // Reset the counter for knob containers
        UI2dContainer textRow = new UI2dContainer(0, 30, ui.leftPane.global.getContentWidth(), 20);
        textRow.setLayout(UI2dContainer.Layout.HORIZONTAL);
        textRow.setPadding(0, 0, 0, 0);
        textRow.addToContainer(this);

        UILabel label = new UILabel(0, 0, 45, 20);
        label.setLabel(p.getLabel());
        label.addToContainer(textRow);
        label.setPadding(5, 0);
        UITextBox textBox = new UITextBox(50,0, ui.leftPane.global.getContentWidth() - 55, 20 );
        ((LXListenableParameter)p).addListener(this);
        textBox.setParameter((StringParameter)p);
        textBox.addToContainer(textRow);
      }
    }
    // Button saving config.
    new UIButton(getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          save();
        }
      }
    }
        .setLabel("\u21BA").setMomentary(true).addToContainer(this);
  }

  /**
   * Method is called after saving a config.  Subclasses should override this method if they
   * need to perform some action only after all the parameters are set.
   */
  public void onSave() {
  }

  /**
   * Method for converting UIConfig into an OSC message for remote control clients such as a
   * phone or tablet.  Pathname format:
   *
   * /rainbow/config/title/parameterLabel:value
   *
   * @return Returns an OSCBundle with all parameter values nested as invidual OscMessages.
   */
  public OscBundle asOscBundle() {
    OscBundle oscBundle = new OscBundle();

    for (LXParameter p : parameters) {
      OscMessage oscMessage = new OscMessage();
      oscMessage.setAddressPattern("/kaledoscope/" + title + "/" + p.getLabel());
      if (p instanceof StringParameter) {
        oscMessage.add(((StringParameter)p).getString());
        oscBundle.addElement(oscMessage);
      } else if (p instanceof DiscreteParameter) {
        oscMessage.add(p.getValue());
        oscBundle.addElement(oscMessage);
      } else if (p instanceof CompoundParameter) {
        oscMessage.add(p.getValuef());
        oscBundle.addElement(oscMessage);
      }
    }

    return oscBundle;
  }
}
