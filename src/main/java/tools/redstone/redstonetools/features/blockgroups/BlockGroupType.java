package tools.redstone.redstonetools.features.blockgroups;

import net.minecraft.util.Formatting;

/**
 * The type of a block group.
 */
public enum BlockGroupType {

    INPUT("input", Formatting.RED + "" + Formatting.BOLD + "I", true, true),
    OUTPUT("output", Formatting.BLUE + "" + Formatting.BOLD + "O", true, false)

    ;

    final String typeName;
    final String formattedTypeIcon;

    final boolean canRead;
    final boolean canWrite;

    BlockGroupType(String typeName, String formattedTypeIcon, boolean canRead, boolean canWrite) {
        this.typeName = typeName;
        this.formattedTypeIcon = formattedTypeIcon;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean canRead() {
        return canRead;
    }

    public boolean canWrite() {
        return canWrite;
    }

    public String getFormattedTypeIcon() {
        return formattedTypeIcon;
    }

    @Override
    public String toString() {
        return typeName;
    }

}
