package com.symmetrylabs.util;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.SLStudio;

public class SLPathsHelper {
    public static String getMappingsDataPath(){
//        return SLStudio.MAPPINGS_DATA_PATH + '/' + SLStudio.applet.showName + ".json";
        String MAPPINGS_DATA_PATH = System.getProperty("user.home") + "/symmetrylabs/mapping";
        return MAPPINGS_DATA_PATH + '/' + ApplicationState.showName() + ".json";
    }
    public static String getMappingsDataDir(){
        return SLStudio.MAPPINGS_DATA_PATH;
    }
}
