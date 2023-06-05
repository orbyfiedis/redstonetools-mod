package tools.redstone.redstonetools.features.io;

public enum IOGroupType {

    /**
     * The group is an input, the values can be set using the
     * {@link tools.redstone.redstonetools.features.commands.io.InputWriteFeature}.
     */
    INPUT("Input", true, true),

    /**
     * The group is an output, the values can be read using the
     * {@link tools.redstone.redstonetools.features.commands.io.OutputReadFeature}.
     */
    OUTPUT("Input", true, false);

    private final String displayName;
    private final boolean canRead;
    private final boolean canWrite;

    IOGroupType(String displayName, boolean canRead, boolean canWrite) {
        this.displayName = displayName;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canRead() {
        return canRead;
    }

    public boolean canWrite() {
        return canWrite;
    }

}
