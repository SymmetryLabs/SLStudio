package com.symmetrylabs.util;

import com.symmetrylabs.slstudio.SLStudio;

public class SLPathsHelper {
    public static String getMappingsDataPath(){
        return SLStudio.MAPPINGS_DATA_PATH + '/' + SLStudio.applet.showName + ".json";
    }
    public static String getMappingsDataDir(){
        return SLStudio.MAPPINGS_DATA_PATH;
    }
}
