package tools.redstone.redstonetools.features.blockgroups;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Fundamentally just represents an ordered list of blocks
 * on which operations can be performed.
 */
public class BlockGroup {

    protected final String name;
    protected final BlockGroupManager manager;

    /**
     * The ordered list of block positions.
     */
    protected final List<BlockPos> blockList = new ArrayList<>();

    /**
     * The type of this block group.
     */
    protected BlockGroupType type;

    protected BlockGroup(String name, BlockGroupManager manager) {
        this.name = name;
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public List<BlockPos> getBlocks() {
        return blockList;
    }

    public int countBlocks() {
        return blockList.size();
    }

    public BlockGroupType getType() {
        return type;
    }

    public BlockGroup setType(BlockGroupType type) {
        this.type = type;
        return this;
    }

    public BlockGroup addBlock(BlockPos pos) {
        if (blockList.contains(pos))
            return this;
        blockList.add(pos);
        return this;
    }

    public BlockGroup addBlock(BlockPos pos, int i) {
        if (blockList.contains(pos))
            return this;
        blockList.add(i, pos);
        return this;
    }

    public BlockGroup removeBlock(BlockPos pos) {
        blockList.remove(pos);
        return this;
    }

    /**
     * Read all bits in this block group into a bit set.
     *
     * @param world The world to provide the block states.
     * @param bitPredicate The predicate to test whether a bit is set or not.
     * @return The bit set.
     */
    public BitSet readBitSet(WorldAccess world, Predicate<BlockState> bitPredicate) {
        int l = blockList.size();
        BitSet bitSet = new BitSet(l);

        for (int i = 0; i < l; i++) {
            BlockPos blockPos = blockList.get(i);

            BlockState state = world.getBlockState(blockPos);
            bitSet.set(i, bitPredicate.test(state));
        }

        return bitSet;
    }

    /**
     * Writes all the values from the bit set into the world in the
     * order of the list of block positions.
     *
     * All the bit state blocks will have their existing block states
     * transformed by the state transformer.
     * For example, to set all enabled bits to lit redstone torches, we can use
     * {@code writeBitSet(..., (on, state) -> on ? state.with(LIT, true) : state)}. This will
     * retain whatever other properties the state had set.
     *
     * @param bitSet The bit set.
     * @param world The world.
     * @param stateTransformer The state transformer.
     */
    public void writeBitSet(BitSet bitSet, WorldAccess world, BiFunction<Boolean, BlockState, BlockState> stateTransformer) {
        int l = bitSet.length();
        int i = 0;

        for (; i < l; i++) {
            BlockPos blockPos = blockList.get(i);

            BlockState state = world.getBlockState(blockPos);
            state = stateTransformer.apply(bitSet.get(i), state);

            world.setBlockState(blockPos, state, 1 | 2, Integer.MAX_VALUE);
        }

        // set the remaining bits to 0
        for (; i < blockList.size(); i++) {
            BlockPos blockPos = blockList.get(i);

            BlockState state = world.getBlockState(blockPos);
            state = stateTransformer.apply(false, state);

            world.setBlockState(blockPos, state, 1 | 2, Integer.MAX_VALUE);
        }
    }

    // called when the block group is
    // removed/destroyed by the manager
    protected void onRemove() {

    }

    /**
     * Remove this block group from the manager.
     */
    public void remove() {
        manager.removeBlockGroup(this);
    }

    /* Serialization */

    public BlockGroup load(NbtCompound compound) {
        // load block positions
        NbtList nbtBlockList = compound.getList("Blocks", NbtElement.LONG_TYPE);
        if (nbtBlockList != null) {
            nbtBlockList.stream()
                    .filter(e -> e.getType() == NbtElement.LONG_TYPE)
                    .map(e -> (NbtLong) e)
                    .mapToLong(NbtLong::longValue)
                    .mapToObj(BlockPos::fromLong)
                    .forEachOrdered(blockList::add);
        }

        return this;
    }

    public NbtCompound save(NbtCompound compound) {
        // save block positions
        NbtList nbtBlockList = new NbtList();
        blockList.stream()
                .mapToLong(BlockPos::asLong)
                .mapToObj(NbtLong::of)
                .forEachOrdered(nbtBlockList::add);
        compound.put("Blocks", nbtBlockList);

        return compound;
    }

}
