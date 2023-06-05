package tools.redstone.redstonetools.features.io;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import tools.redstone.redstonetools.features.feedback.Feedback;
import tools.redstone.redstonetools.utils.NbtSerializable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A named group of blocks representing an input or output.
 */
public class IOBlockGroup implements NbtSerializable {

    private final String name;

    /**
     * The positions of all blocks included in this block group,
     * in order of the bits written or read.
     */
    private final List<BlockPos> blocks = new ArrayList<>();

    /**
     * The type of this group.
     */
    private IOGroupType type;

    // user defined predicates
    private BlockState onBitState  = Blocks.REDSTONE_BLOCK.getDefaultState();
    private BlockState offBitState = Blocks.GRAY_STAINED_GLASS.getDefaultState();

    public IOBlockGroup(String name) {
        this.name = name;
    }

    public BlockState getOnBitState() {
        return onBitState;
    }

    public IOBlockGroup setOnBitState(BlockState onBitState) {
        this.onBitState = onBitState;
        return this;
    }

    public BlockState getOffBitState() {
        return offBitState;
    }

    public IOBlockGroup setOffBitState(BlockState offBitState) {
        this.offBitState = offBitState;
        return this;
    }

    public IOBlockGroup setType(IOGroupType type) {
        this.type = type;
        return this;
    }

    public IOGroupType getType() {
        return type;
    }

    public boolean canWrite() {
        return type == null || type.canWrite();
    }

    public boolean canRead() {
        return type == null || type.canRead();
    }

    public String getTypeDisplayName() {
        return type == null ? "undefined" : type.getDisplayName();
    }

    public String getName() {
        return name;
    }

    public List<BlockPos> getBlocks() {
        return blocks;
    }

    public IOBlockGroup addBlock(BlockPos pos) {
        blocks.add(pos);
        return this;
    }

    public IOBlockGroup sortBlocks(Comparator<BlockPos> comparator) {
        blocks.sort(comparator);
        return this;
    }

    /**
     * Reads all the bits into a bit set from the world,
     * in the order of the block position list.
     *
     * @param world The world.
     * @param onPredicate The predicate to test block states for a 1 or 0.
     * @return The bit set.
     */
    public BitSet readBitSet(WorldAccess world, Predicate<BlockState> onPredicate) {
        final int l = blocks.size();
        BitSet set = new BitSet(l);
        for (int i = 0; i < l; i++) {
            BlockPos pos = blocks.get(i);

            set.set(i, onPredicate.test(world.getBlockState(pos)));
        }

        return set;
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
     * @param set The bit set.
     * @param world The world.
     * @param stateTransformer The state transformer.
     * @return The feedback.
     */
    public Feedback writeBitSet(BitSet set, WorldAccess world, BiFunction<Boolean, BlockState, BlockState> stateTransformer) {
        final int l = set.length();
        if (l > blocks.size()) {
            return Feedback.error("Value is too large for group, value bits: " + l + ", world bits: " + blocks.size());
        }

        for (int i = 0; i < l; i++) {
            BlockPos pos = blocks.get(i);

            BlockState state = world.getBlockState(pos);
            state = stateTransformer.apply(set.get(i), state);
            world.setBlockState(pos, state, 1 | 2, Integer.MAX_VALUE);
        }

        // set remaining blocks to off
        for (int i = l; i < blocks.size(); i++) {
            BlockPos pos = blocks.get(i);

            BlockState state = world.getBlockState(pos);
            state = stateTransformer.apply(false, state);
            world.setBlockState(pos, state, 1 | 2, Integer.MAX_VALUE);
        }

        return Feedback.success("Successfully wrote " + l + " bits to " + blocks.size() + " blocks.");
    }

    /*
        Persistence
     */

    @Override
    public void save(NbtCompound nbt) {
        NbtList blockPosList = new NbtList();
        for (BlockPos pos : blocks) {
            blockPosList.add(NbtLong.of(pos.asLong()));
        }

        nbt.put("Blocks", blockPosList);
        if (type != null)
            nbt.putInt("Type", type.ordinal());
    }

    @Override
    public void load(NbtCompound nbt) {
        NbtList blockPosList = nbt.getList("Blocks", NbtElement.LONG_TYPE);
        for (NbtElement element : blockPosList) {
            blocks.add(BlockPos.fromLong(
                    ((NbtLong)element).longValue()
            ));
        }

        if (nbt.contains("Type"))
            type = IOGroupType.values()[nbt.getInt("Type")];
    }

}
