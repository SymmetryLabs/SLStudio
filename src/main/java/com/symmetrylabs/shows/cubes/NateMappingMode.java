package com.symmetrylabs.shows.cubes;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.symmetrylabs.slstudio.pattern.cubes.NateCubesMappingForkPattern;
import com.symmetrylabs.util.SLPathsHelper;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.StringParameter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Mapping Mode
 * (TODO)
 *  1) iterate through mapped cubes in order (tower by tower, cube by cube)
 *  2) get cubes not mapped but on network to pulse
 *  3) get a "display orientation" mode
 */

public class NateMappingMode {

    private String[] fixtureIdStringArray;

    public static enum MappingModeType {MODEL, HELLOENUM};
    public static enum MappingDisplayModeType {ALL, ITERATE};

    private LXChannel mappingChannel = null;
    private LXPattern mappingPattern = null;

    public final BooleanParameter saveVal = new BooleanParameter("SAVE", false).setMode(BooleanParameter.Mode.MOMENTARY).setDescription("save the current value into nonvolatile JSON file");

    public final BooleanParameter enabled;
    public final EnumParameter<MappingModeType> mode;
    public final EnumParameter<MappingDisplayModeType> displayMode;
    //public final BooleanParameter displayOrientation;

    public final DiscreteParameter selectedModelFixture;

    public final StringParameter selectedOutput = new StringParameter("Select Unmapped Output");

//    public final SortedSet<String> modelFixtures = new TreeSet<String>();
    public final SortedSet<String> modelFixtures = new TreeSet<String>();

    private LX lx;
    private CubesModel cubesModel;

    private static Map<LX, WeakReference<NateMappingMode>> instanceByLX = new WeakHashMap<>();

    public static NateMappingMode getInstance(LX lx) {
        WeakReference<NateMappingMode> weakRef = instanceByLX.get(lx);
        NateMappingMode ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new NateMappingMode(lx)));
        }
        return ref;
    }

    private NateMappingMode(LX lx) {
        this.lx = lx;




//        cubesModel = lx.model instanceof CubesModel ? (CubesModel)lx.model : new CubesModel();
        cubesModel = lx.model instanceof CubesModel ? (CubesModel)lx.model : new CubesModel();

        fixtureIdStringArray = new String[cubesModel.getCubes().size()*2];

        int ii = 0;
        for (CubesModel.Cube cube : cubesModel.getCubes()) {
            // initially just add all fixtures to the fixtures pool

            CubesModel.DoubleControllerCube cubei = (CubesModel.DoubleControllerCube) cube;

            //these copy paste brackets should be function
            {
                modelFixtures.add(cubei.modelId);
                fixtureIdStringArray[ii++] = cubei.modelId;
            }

            {
                modelFixtures.add(cubei.modelId + "_B");
                fixtureIdStringArray[ii++] = cubei.modelId + "_B";
            }
        }
        this.selectedModelFixture = new DiscreteParameter("SelectFixtureInModel", fixtureIdStringArray)
         .setDescription("Select which fixture in abstract model");

        this.enabled = new BooleanParameter("enabled", false)
         .setDescription("Mapping Mode: toggle on/off");

        this.mode = new EnumParameter<MappingModeType>("mode", MappingModeType.MODEL)
         .setDescription("Mapping Mode: toggle between mapped/unmapped fixtures");

        this.displayMode = new EnumParameter<MappingDisplayModeType>("displayMode", MappingDisplayModeType.ALL)
         .setDescription("Mapping Mode: display all mapped/unmapped fixtures");


        saveVal.addListener(p -> {
            if (saveVal.getValueb()) {
                saveToJson();
            }
        });


//        String[] emptyOptions = new String[] {"-"};
//
//        String[] initialMappedFixtures = fixturesMappedButNotOnNetwork.isEmpty()
//                ? emptyOptions : fixturesMappedButNotOnNetwork.toArray(new String[0]);
//
//        selectedModelFixture = new DiscreteParameter("selectedModelFixture", initialMappedFixtures);
//        selectedUnMappedFixture = new DiscreteParameter("selectedUnMappedFixture", emptyOptions);
//
//        selectedOutput = new StringParameter("uninitialized");
//        selectedControllerB = new StringParameter("uninitialized");
//
//        CubesShow show = CubesShow.getInstance(lx);
//
//        if (show != null) {
//            show.addControllerSetListener(new SetListener<CubesController>() {
//                public void onItemAdded(final CubesController c) {
//                    if (isFixtureMapped(c.id)) {
//                        fixturesMappedButNotOnNetwork.remove(c.id);
//                        fixturesMappedAndOnTheNetwork.add(c.id);
//                    } else {
//                        fixturesOnNetworkButNotMapped.add(c.id);
//                    }
//
//                    selectedModelFixture.setOptions(fixturesMappedAndOnTheNetwork.isEmpty() ? emptyOptions
//                            : fixturesMappedAndOnTheNetwork.toArray(new String[0]));
//                    selectedUnMappedFixture.setOptions(fixturesOnNetworkButNotMapped.isEmpty() ? emptyOptions
//                            : fixturesOnNetworkButNotMapped.toArray(new String[0]));
//                }
//                public void onItemRemoved(final CubesController c) {}
//            });
//        }
//        else {
//            System.err.println("**WARNING** CubesMappingMode used before CubesShow has been initialized.");
//        }
//
        enabled.addListener(p -> {
            if (((BooleanParameter)p).isOn()) {
                addChannel();
            }
            else {
                removeChannel();
            }
        });
    }

    private void writeData(String outputJsonString, String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(outputJsonString);
        writer.close();
    }

    private void saveToJson() {
        String idToMap = selectedOutput.getString();
        fixtureIdStringArray[selectedModelFixture.getValuei()] = idToMap;

        HashMap<String, String[]> controllers = new HashMap<>();
        controllers.put("controllers", fixtureIdStringArray);


        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String outString = gson.toJson(controllers);
        System.out.println(outString);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd__HH_mm_ss");
        System.out.println(sdf);

        String dateString = sdf.format(new Date());

        String pathRaw = SLPathsHelper.getMappingsDataPath();
        String pathWithDate = SLPathsHelper.getMappingsDataDir() + '/' + dateString + ".json";
        try {
            writeData(outString, pathRaw);
            writeData(outString, pathWithDate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFixtureMapped(String id) {
        for (CubesModel.Cube fixture : cubesModel.getCubes()) {
            if (fixture.modelId.equals(id))
                return true;
        }
        return false;
    }

    public boolean inMappedMode() {
        return mode.getObject() == MappingModeType.MODEL;
    }

    public boolean inUnMappedMode() {
        return mode.getObject() == MappingModeType.HELLOENUM;
    }

    public boolean inDisplayAllMode() {
        return displayMode.getObject() == MappingDisplayModeType.ALL;
    }

    public boolean inIterateFixturesMode() {
        return displayMode.getObject() == MappingDisplayModeType.ITERATE;
    }

//    public String getSelectedMappedFixtureId() {
//        String returnval = (String) selectedModelFixture.getOption();
//        selectedOutput.setValue(returnval);
//        return returnval;
//    }

//    public String getSelectedUnMappedFixtureId() {
//        String returnval = (String)selectedUnMappedFixture.getOption();
//        selectedControllerB.setValue(returnval);
//        return returnval;
//    }

//    public boolean isSelectedUnMappedFixture(String id) {
//        return id.equals(selectedUnMappedFixture.getOption());
//    }
//
//    public int getUnMappedColor() {
//        //if (mappingPattern != null)
//        //    return mappingPattern.getUnMappedButOnNetworkColor;
//        //return 0;
//        return LXColor.RED; // temp
//    }
//
    private void addChannel() {
        mappingPattern = new NateCubesMappingForkPattern(lx);
        mappingChannel = lx.engine.addChannel(new LXPattern[] {mappingPattern});

        for (LXChannel channel : lx.engine.getAllSubChannels())
            channel.cueActive.setValue(false);

        mappingChannel.fader.setValue(1);
        mappingChannel.label.setValue("Mapping");
        mappingChannel.cueActive.setValue(true);
    }

    private void removeChannel() {
        lx.engine.removeChannel(mappingChannel);
        mappingChannel = null;
        mappingPattern = null;
    }
}
