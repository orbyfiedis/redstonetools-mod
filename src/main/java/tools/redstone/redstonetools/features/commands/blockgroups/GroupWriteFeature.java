package tools.redstone.redstonetools.features.commands.blockgroups;

import com.google.auto.service.AutoService;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.server.command.ServerCommandSource;
import tools.redstone.redstonetools.features.AbstractFeature;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.arguments.Argument;
import tools.redstone.redstonetools.features.arguments.serializers.BigIntegerSerializer;
import tools.redstone.redstonetools.features.arguments.serializers.BlockStateArgumentSerializer;
import tools.redstone.redstonetools.features.blockgroups.BlockGroup;
import tools.redstone.redstonetools.features.blockgroups.BlockGroupManager;
import tools.redstone.redstonetools.features.commands.CommandFeature;
import tools.redstone.redstonetools.features.feedback.Feedback;

import java.math.BigInteger;
import java.util.BitSet;

@AutoService(AbstractFeature.class)
@Feature(name = "Group Write", description = "Write a value into the group", command = "gwrite")
public class GroupWriteFeature extends CommandFeature {

    public static final Argument<String> groupName = Argument.ofType(BlockGroupsFeature.blockGroupNameSerializer());

    public static final Argument<BigInteger> value = Argument.ofType(BigIntegerSerializer.bigInteger());

    public static final Argument<BlockStateArgument> onState = Argument.ofType(BlockStateArgumentSerializer.blockState())
            .withDefault(BlockStateArgumentSerializer.toArgument(Blocks.REDSTONE_BLOCK.getDefaultState()));
    public static final Argument<BlockStateArgument> offState = Argument.ofType(BlockStateArgumentSerializer.blockState())
            .withDefault(BlockStateArgumentSerializer.toArgument(Blocks.GRAY_STAINED_GLASS.getDefaultState()));

    @Override
    protected Feedback execute(ServerCommandSource source) throws CommandSyntaxException {
        BlockGroupManager blockGroupManager = BlockGroupManager.forServerWorld(source.getWorld());
        BlockGroup blockGroup = blockGroupManager.getBlockGroup(groupName.getValue());
        if (blockGroup == null) {
            return Feedback.error("Unknown block group {}", groupName.getValue());
        }

        if (blockGroup.getType() != null && !blockGroup.getType().canWrite()) {
            return Feedback.error("Can not write to block group {} " +
                    " of type {}", groupName.getValue(), blockGroup.getType().getTypeName());
        }

        BitSet bitSet = BitSet.valueOf(value.getValue().toByteArray());
        if (bitSet.length() > blockGroup.countBlocks()) {
            return Feedback.error("Input too large: {} bits can not be written to {} blocks",
                    bitSet.length(), blockGroup.countBlocks());
        }

        BlockState onState = GroupWriteFeature.onState.getValue().getBlockState();
        BlockState offState = GroupWriteFeature.offState.getValue().getBlockState();

        blockGroup.writeBitSet(bitSet, source.getWorld(), (bit, currentState) ->
                bit ? onState : offState);

        return Feedback.success("Successfully wrote {} bits to {} blocks",
                bitSet.length(), blockGroup.countBlocks());
    }

}
