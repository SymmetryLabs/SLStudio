package heronarts.lx.data;

import java.util.HashMap;
import java.util.Map;


public enum ProjectFileType {
    LegacyProjectFile(".lxp", ProjectFileTypeProto.LegacyProjectFile);

    public final String extension;
    public final ProjectFileTypeProto protoType;

    ProjectFileType(String extension, ProjectFileTypeProto protoType) {
        this.extension = extension;
        this.protoType = protoType;
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
