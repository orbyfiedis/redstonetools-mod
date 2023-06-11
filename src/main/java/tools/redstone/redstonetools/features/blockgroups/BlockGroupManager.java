package tools.redstone.redstonetools.features.blockgroups;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.*;

/**
 * Manages the block groups for a given world.
 */
public class BlockGroupManager extends PersistentState {

    public static BlockGroupManager forServerWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                nbt -> new BlockGroupManager().loadNbt(nbt),
                BlockGroupManager::new,
                "rstBlockGroups"
        );
    }

    /**
     * The block groups by name.
     */
    final Map<String, BlockGroup> blockGroups = new HashMap<>();

    public Collection<BlockGroup> getBlockGroups() {
        return blockGroups.values();
    }

    public BlockGroup getBlockGroup(String name) {
        return blockGroups.get(name);
    }

    public BlockGroup createBlockGroup(String name) {
        markDirty();
        return blockGroups.computeIfAbsent(name, __ -> new BlockGroup(name, this));
    }

    public void removeBlockGroup(BlockGroup blockGroup) {
        blockGroup.onRemove();
        blockGroups.remove(blockGroup.getName());

        markDirty();
    }

    /* Persistent State */

    // load all persistent data for this
    // manager from the given compound
    public BlockGroupManager loadNbt(NbtCompound nbt) {
        NbtList nbtBlockGroups = nbt.getList("BlockGroups", NbtElement.COMPOUND_TYPE);
        if (nbtBlockGroups != null) {
            nbtBlockGroups.stream()
                    .map(e -> (NbtCompound) e)
                    .map(c -> new BlockGroup(c.getString("Name"), this).load(c))
                    .forEach(g -> blockGroups.put(g.getName(), g));
        }

        return this;
    }

    // serialize all persistent data in this
    // manager into the given compound
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList nbtBlockGroups = new NbtList();
        blockGroups.values().stream()
                .map(g -> {
                    NbtCompound compound = new NbtCompound();
                    compound.putString("Name", g.getName());
                    g.save(compound);
                    return compound;
                })
                .forEach(nbtBlockGroups::add);
        nbt.put("BlockGroups", nbtBlockGroups);

        return nbt;
    }

}
