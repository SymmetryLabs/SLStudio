package heronarts.lx.data;

public enum LXVersion {
    SLSTUDIO_ORIG(0),
    VOLUME_ORIG(1),
    SLSTUDIO_WITH_LOOKS(2),
    VOLUME_WITH_LOOKS(3);

    final int versionCode;

    LXVersion(int versionCode) {
        this.versionCode = versionCode;
    }

    public boolean isBefore(LXVersion version) {
        return versionCode < version.versionCode;
    }

    public boolean isEqualOrAfter(LXVersion version) {
        return versionCode >= version.versionCode;
    }

    public static LXVersion fromCode(int code) {
        for (LXVersion v : LXVersion.values()) {
            if (v.versionCode == code) {
                return v;
            }
        }
        return null;
    }
}
