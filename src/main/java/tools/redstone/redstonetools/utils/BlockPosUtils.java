package tools.redstone.redstonetools.utils;

import net.minecraft.util.math.BlockPos;

public class BlockPosUtils {

    public static String toStringPretty(BlockPos pos) {
        return "X: " + pos.getX() + ", Y: " + pos.getY() + ", Z: " + pos.getZ();
    }

}
