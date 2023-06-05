package tools.redstone.redstonetools.features.io;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import tools.redstone.redstonetools.utils.NbtUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The persistent world state.
 */
public class IOState extends PersistentState {

    static final String PERSISTENT_STATE_KEY = "redstonetools:namedio";

    /**
     * The world.
     */
    private final ServerWorld world;

    /**
     * All registered block groups.
     */
    private final Map<String, IOBlockGroup> blockGroupMap = new ConcurrentHashMap<>();

    {
        // test
        createBlockGroup("test")
                .addBlock(new BlockPos(0, 0, 0))
                .addBlock(new BlockPos(1, 0, 0))
                .addBlock(new BlockPos(2, 0, 0))
                .addBlock(new BlockPos(3, 0, 0));
    }

    public IOState(ServerWorld world) {
        this.world = world;
    }

    /**
     * Get the {@link IOState} state for the given world.
     *
     * @param world The world.
     * @return The state.
     */
    public static IOState forWorld(final ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                nbt -> readNbtStatic(world, nbt),
                () -> new IOState(world),
                PERSISTENT_STATE_KEY
        );
    }

    public Map<String, IOBlockGroup> getBlockGroups() {
        return blockGroupMap;
    }

    public IOBlockGroup getOrCreateBlockGroup(final String name) {
        return blockGroupMap.computeIfAbsent(name, __ -> new IOBlockGroup(name));
    }

    public IOBlockGroup getBlockGroup(String name) {
        return blockGroupMap.get(name);
    }

    public IOBlockGroup createBlockGroup(final String name) {
        return blockGroupMap.compute(name, (s, ioBlockGroup) -> new IOBlockGroup(s));
    }

    public boolean hasBlockGroup(String name) {
        return blockGroupMap.containsKey(name);
    }

    public void removeBlockGroup(String name) {
        blockGroupMap.remove(name);
    }

    /*
        Serialization
     */

    static IOState readNbtStatic(ServerWorld world, NbtCompound nbt) {
        return new IOState(world).readNbt(nbt);
    }

    private IOState readNbt(NbtCompound nbt) {
        // read block groups
        NbtCompound blockGroupsTag = nbt.getCompound("BlockGroups");
        NbtUtils.<NbtCompound>forEach(blockGroupsTag, (name, bgTag) -> {
            IOBlockGroup blockGroup = getOrCreateBlockGroup(name);
            blockGroup.load(bgTag);
        });

        return this;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        // write block groups
        NbtCompound blockGroupsTag = NbtUtils.getOrCreateCompound(nbt, "BlockGroups");
        blockGroupMap.forEach((name, blockGroup) -> {
            NbtCompound bg = new NbtCompound();
            blockGroup.save(bg);

            blockGroupsTag.put(name, bg);
        });

        return nbt;
    }

}
