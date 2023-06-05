package tools.redstone.redstonetools.utils;

import net.minecraft.nbt.NbtCompound;

public interface NbtSerializable {

    void save(NbtCompound nbt);
    void load(NbtCompound nbt);

}
