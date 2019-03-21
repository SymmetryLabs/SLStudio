package heronarts.lx.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public enum ProjectFileType {
    LegacyProjectFile(".lxp");

    public final String extension;

    ProjectFileType(String extension) {
        this.extension = extension;
    }

    public static final Map<String, ProjectFileType> EXTENSION_MAP = new HashMap<>();
    static {
        for (ProjectFileType pft : ProjectFileType.values()) {
            EXTENSION_MAP.put(pft.extension, pft);
        }
    }

    public static ProjectFileType fromExtension(String ext) {
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        return EXTENSION_MAP.get(ext);
    }
}
