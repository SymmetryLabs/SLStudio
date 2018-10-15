package com.symmetrylabs.shows.tree;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.parameter.BooleanParameter;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.*;


public class TreeModelingTool extends LXComponent {

    private static TreeModelingTool instance = null;

    public static enum Mode {
        LIMB, BRANCH, TWIG
    }

    // mostly used for modeling pattern
    public final EnumParameter<Mode> mode = new EnumParameter<Mode>("mode", Mode.LIMB);

    public final TreeModel tree;
    private final TreeConfigStore store;

    public final ObjectParameter selectedLimb;
    public final ObjectParameter selectedBranch;
    public final ObjectParameter selectedTwig;

    public final LimbManipulator limbManipulator;
    public final BranchManipulator branchManipulator;
    public final TwigManipulator twigManipulator;

    private TreeModelingTool(LX lx) {
        super(lx, "TreeModelingTool");
        this.tree = (TreeModel) lx.model;
        this.store = new TreeConfigStore(lx);

        this.limbManipulator = new LimbManipulator(lx);
        addSubcomponent(limbManipulator);

        this.branchManipulator = new BranchManipulator(lx);
        addSubcomponent(branchManipulator);

        this.twigManipulator = new TwigManipulator(lx);
        addSubcomponent(twigManipulator);

        this.selectedLimb   = new ObjectParameter<TreeModel.Limb>("selectedLimb", tree.getLimbsArray());
        this.selectedBranch = new ObjectParameter<TreeModel.Branch>("selectedBranch", getSelectedLimb().getBranchesArray());
        this.selectedTwig   = new ObjectParameter<TreeModel.Twig>("selectedTwig", getSelectedBranch().getTwigsArray());

        selectedTwig.setOptions(new String[] {"1", "2", "3", "4", "5", "6", "7", "8"});

        selectedLimb.addListener(parameter -> {
            mode.setValue(Mode.LIMB);
            limbManipulator.repurposeParameters();
            updateBranches();
            updateTwigs();
            mode.setValue(Mode.LIMB);
        });

        selectedBranch.addListener(parameter -> {
            mode.setValue(Mode.BRANCH);
            branchManipulator.repurposeParameters();
            updateTwigs();
            mode.setValue(Mode.BRANCH);
        });

        selectedTwig.addListener(parameter -> {
            mode.setValue(Mode.TWIG);
            twigManipulator.repurposeParameters();
        });

        limbManipulator.repurposeParameters();
        branchManipulator.repurposeParameters();
        twigManipulator.repurposeParameters();
    }

    public static TreeModelingTool getInstance(LX lx) {
        if (instance == null)
            instance = new TreeModelingTool(lx);

        return instance;
    }

    public static TreeModelingTool getInstance() {
        if (instance != null) {
            return instance;
        }
        return null;
    }

    private void updateBranches() {
        selectedBranch.setObjects(getSelectedLimb().getBranchesArray());
        selectedBranch.setValue(getSelectedLimb().getBranches().get(0));
        branchManipulator.repurposeParameters();
    }

    private void updateTwigs() {
        selectedTwig.setObjects(getSelectedBranch().getTwigsArray());
        selectedTwig.setValue(getSelectedBranch().getTwigs().get(0));
        twigManipulator.repurposeParameters();
    }

    public TreeModel.Limb getSelectedLimb() {
        return (TreeModel.Limb) selectedLimb.getObject();
    }

    public void setSelectedLimb(TreeModel.Limb limb) {
        selectedLimb.setValue(limb);
    }

    public TreeModel.Branch getSelectedBranch() {
        return (TreeModel.Branch) selectedBranch.getObject();
    }

    public void setSelectedBranch(TreeModel.Branch branch) {
        selectedBranch.setObjects(getSelectedLimb().getBranchesArray());
        selectedBranch.setValue(branch);
    }

    public TreeModel.Twig getSelectedTwig() {
        return (TreeModel.Twig) selectedTwig.getObject();
    }

    public void setSelectedTwig(TreeModel.Twig twig) {
        selectedTwig.setObjects(getSelectedBranch().getTwigsArray());
        selectedTwig.setValue(twig);
    }

    @Override
    // this is just for now for now, we probably want as a separate saving event than the lxp
    public void save(LX lx, JsonObject obj) {
        store.writeConfig();
    }

    public static boolean isTreeShow() {
        return SLStudio.applet.show instanceof TreeShow;
    }

    // (todo) make these a little more elegant
    public class LimbManipulator extends LXComponent {
        private boolean disableParameters = false;

        public final CompoundParameter length = new CompoundParameter("length", LimbConfig.DEFAULT_LENGTH, LimbConfig.MIN_LENGTH, LimbConfig.MAX_LENGTH);
        public final CompoundParameter height = new CompoundParameter("height", LimbConfig.DEFAULT_HEIGHT, LimbConfig.MIN_HEIGHT, LimbConfig.MAX_HEIGHT);
        public final CompoundParameter azimuth    = new CompoundParameter("azim", LimbConfig.DEFAULT_AZIMUTH, LimbConfig.MIN_AZIMUTH, LimbConfig.MAX_AZIMUTH);
        public final CompoundParameter elevation = new CompoundParameter("elev", LimbConfig.DEFAULT_ELEVATION, LimbConfig.MIN_ELEVATION, LimbConfig.MAX_ELEVATION);
        public final BooleanParameter locked = new BooleanParameter("locked", false);

        private LimbManipulator(LX lx) {
            super(lx);
            addParameter(length);
            addParameter(height);
            addParameter(azimuth);
            addParameter(elevation);
            addParameter(locked);

            locked.addListener(parameter -> {
                for (TreeModel.Branch branch : getSelectedLimb().getBranches()) {
                    if (((BooleanParameter) parameter).isOn()) {
                        branch.getConfig().locked = true;
                    }
                }
            });
        }

        public void onParameterChanged(LXParameter parameter) {
            if (!disableParameters) {
                LimbConfig config = getSelectedLimb().getConfig();
                config.length = length.getValuef();
                config.height = height.getValuef();
                config.azimuth = azimuth.getValuef();
                config.elevation = elevation.getValuef();
                config.locked = locked.isOn();
                tree.reconfigure();
            }
            if (mode.getEnum() != Mode.LIMB) {
                mode.setValue(Mode.LIMB);
            }
        }

        public void repurposeParameters() {
            disableParameters = true;
            LimbConfig config = getSelectedLimb().getConfig();
            length.setValue(config.length);
            height.setValue(config.height);
            azimuth.setValue(config.azimuth);
            elevation.setValue(config.elevation);
            locked.setValue(config.locked);
            disableParameters = false;
        }
    }

    public class BranchManipulator extends LXComponent {
        private boolean disableParameters = false;

        public final DiscreteParameter type;
        public final StringParameter ipAddress = new StringParameter("ip", "0.0.0.0");
        public final DiscreteParameter channel = new DiscreteParameter("channel", 1, 9);
        public final CompoundParameter xPosition = new CompoundParameter("xPos", BranchConfig.DEFAULT_X, BranchConfig.MIN_X, BranchConfig.MAX_X);
        public final CompoundParameter yPosition = new CompoundParameter("yPos", BranchConfig.DEFAULT_Y, BranchConfig.MIN_Y, BranchConfig.MAX_Y);
        public final CompoundParameter zPosition = new CompoundParameter("zPos", BranchConfig.DEFAULT_Z, BranchConfig.MIN_Z, BranchConfig.MAX_Z);
        public final CompoundParameter azimuth = new CompoundParameter("azim", BranchConfig.DEFAULT_AZIMUTH, BranchConfig.MIN_AZIMUTH, BranchConfig.MAX_AZIMUTH);
        public final CompoundParameter elevation = new CompoundParameter("elev", BranchConfig.DEFAULT_ELEVATION, BranchConfig.MIN_ELEVATION, BranchConfig.MAX_ELEVATION);
        public final CompoundParameter tilt = new CompoundParameter("tilt", BranchConfig.DEFAULT_TILT, BranchConfig.MIN_TILT, BranchConfig.MAX_TILT);
        public final BooleanParameter locked = new BooleanParameter("locked", false);

        private BranchManipulator(LX lx) {
            super(lx);
            addParameter(ipAddress);
            addParameter(channel.setUnits(LXParameter.Units.INTEGER));
            addParameter(xPosition);
            addParameter(yPosition);
            addParameter(zPosition);
            addParameter(azimuth);
            addParameter(elevation);
            addParameter(tilt);
            addParameter(locked);

            locked.addListener(parameter -> {
                if (!((BooleanParameter) parameter).isOn()) {
                    limbManipulator.locked.setValue(false);
                }
            });

            // type has an "exclusive" listener
            // (todo) clean this up a bit
            List<String> types = TreeConfig.getBranchTypes();
            types.add(0, "Custom");
            this.type = new DiscreteParameter("type", types.toArray(new String[types.size()]));
            type.addListener(parameter -> {
                String type = ((DiscreteParameter)parameter).getOption();
                if (!type.equals("Custom")) {
                    TwigConfig[] twigConfigs = TreeConfig.getBranchType(type);
                    getSelectedBranch().getConfig().setTwigs(twigConfigs);
                    tree.reconfigure();
                    repurposeParameters();
                    // why isn't this redrawing with 'repurposeParameters()"?
                }
            });
        }

        public void onParameterChanged(LXParameter parameter) {
            if (!disableParameters) {
                BranchConfig config = getSelectedBranch().getConfig();
                config.ipAddress = ipAddress.getString();
                config.channel = (int) channel.getValue();
                config.x = xPosition.getValuef();
                config.y = yPosition.getValuef();
                config.z = zPosition.getValuef();
                config.azimuth = azimuth.getValuef();
                config.elevation = elevation.getValuef();
                config.tilt = tilt.getValuef();
                config.locked = locked.isOn();
                tree.reconfigure();
            }
            if (mode.getEnum() != Mode.BRANCH) {
                mode.setValue(Mode.BRANCH);
            }
        }

        public void repurposeParameters() {
            disableParameters = true;
            BranchConfig config = getSelectedBranch().getConfig();
            ipAddress.setValue(config.ipAddress);
            channel.setValue(config.channel);
            xPosition.setValue(config.x);
            yPosition.setValue(config.y);
            zPosition.setValue(config.x);
            azimuth.setValue(config.azimuth);
            elevation.setValue(config.elevation);
            tilt.setValue(config.tilt);
            locked.setValue(config.locked);
            disableParameters = false;
        }
    }

    public class TwigManipulator extends LXComponent {
        private boolean disableParameters = false;

        public final CompoundParameter xPosition = new CompoundParameter("xPos", TwigConfig.DEFAULT_X, TwigConfig.MIN_X, TwigConfig.MAX_X);
        public final CompoundParameter yPosition = new CompoundParameter("yPos", TwigConfig.DEFAULT_Y, TwigConfig.MIN_Y, TwigConfig.MAX_Y);
        public final CompoundParameter zPosition = new CompoundParameter("zPos", TwigConfig.DEFAULT_Z, TwigConfig.MIN_Z, TwigConfig.MAX_Z);
        public final CompoundParameter azimuth = new CompoundParameter("azim", TwigConfig.DEFAULT_AZIMUTH, TwigConfig.MIN_AZIMUTH, TwigConfig.MAX_AZIMUTH);
        public final CompoundParameter elevation = new CompoundParameter("elev", TwigConfig.DEFAULT_ELEVATION, TwigConfig.MIN_ELEVATION, TwigConfig.MAX_ELEVATION);
        public final CompoundParameter tilt = new CompoundParameter("tilt", TwigConfig.DEFAULT_TILT, TwigConfig.MIN_TILT, TwigConfig.MAX_TILT);
        public final DiscreteParameter index = new DiscreteParameter("index", new String[] {"1", "2", "3", "4", "5", "6", "7", "8"});
        public final BooleanParameter locked = new BooleanParameter("locked", false);

        private TwigManipulator(LX lx) {
            super(lx);
            addParameter(xPosition);
            addParameter(yPosition);
            addParameter(zPosition);
            addParameter(azimuth);
            addParameter(elevation);
            addParameter(tilt);
            addParameter(index);
        }

        public void onParameterChanged(LXParameter parameter) {
            if (!disableParameters) {
                TwigConfig config = getSelectedTwig().getConfig();
                config.x = xPosition.getValuef();
                config.y = yPosition.getValuef();
                config.z = zPosition.getValuef();
                config.azimuth = azimuth.getValuef();
                config.elevation = elevation.getValuef();
                config.tilt = tilt.getValuef();
                config.index = index.getValuei()+1;
                tree.reconfigure();
                branchManipulator.type.setValue(0); // Custom
            }
            if (mode.getEnum() != Mode.TWIG) {
                mode.setValue(Mode.TWIG);
            }
        }

        public void repurposeParameters() {
            disableParameters = true;
            TwigConfig config = getSelectedTwig().getConfig();
            xPosition.setValue(config.x);
            yPosition.setValue(config.y);
            zPosition.setValue(config.z);
            azimuth.setValue(config.azimuth);
            elevation.setValue(config.elevation);
            tilt.setValue(config.tilt);
            index.setValue(config.index-1);

            //System.out.println("config: " + config.index + ", param: " + index.getValuei());
            disableParameters = false;
        }
    }
}
