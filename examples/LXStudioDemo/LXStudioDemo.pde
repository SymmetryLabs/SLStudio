/**
 * LXStudio demo. This is a simple project which gets the LX harness
 * up and running. Copy this off or fork it for your own project!
 */

// Let's work in inches
final static float INCHES = 1;
final static float FEET = 12*INCHES;

// Top-level, we have a model and an LXStudio instance
Model model;
LXStudio lx;

// Setup establishes the windowing and LX constructs
void setup() {
  size(1280, 960, P3D);
  
  // Create the model, which describes where our light points are
  model = new Model();
  
  // Create the P3LX engine
  lx = new LXStudio(this, model)  {
    @Override
    protected void initialize(LXStudio lx, LXStudio.UI ui) {
      // Add custom LXComponents or LXOutput objects to the engine here,
      // before the UI is constructed
    }
    
    @Override
    protected void onUIReady(LXStudio lx, LXStudio.UI ui) {
      // The UI is now ready, can add custom UI components if desired
      ui.preview.addComponent(new UIWalls());
    }
  };

}

void draw() {
  // Empty placeholder... LX handles everything for us!
}