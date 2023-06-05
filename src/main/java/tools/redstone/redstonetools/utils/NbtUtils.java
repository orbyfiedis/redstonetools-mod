package tools.redstone.redstonetools.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;

/**
 * Utilities for working with NBT data.
 */
public class NbtUtils {

    public static NbtCompound getOrCreateCompound(NbtCompound compound, String key) {
        if (!compound.contains(key, NbtElement.COMPOUND_TYPE)) {
            NbtCompound r = new NbtCompound();
            compound.put(key, r);
            return r;
        }

        return compound.getCompound(key);
    }

    @SuppressWarnings("unchecked")
    public static <V> void forEach(NbtCompound compound, BiConsumer<String, V> consumer) {
        for (String s : compound.getKeys()) {
            V tag = (V) compound.get(s);
            consumer.accept(s, tag);
        }
    }

}
