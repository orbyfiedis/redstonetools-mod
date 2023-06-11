package tools.redstone.redstonetools.features.commands.blockgroups;

import com.google.auto.service.AutoService;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.AbstractBlock;
import net.minecraft.server.command.ServerCommandSource;
import tools.redstone.redstonetools.features.AbstractFeature;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.arguments.Argument;
import tools.redstone.redstonetools.features.arguments.serializers.NumberBaseSerializer;
import tools.redstone.redstonetools.features.blockgroups.BlockGroup;
import tools.redstone.redstonetools.features.blockgroups.BlockGroupManager;
import tools.redstone.redstonetools.features.commands.CommandFeature;
import tools.redstone.redstonetools.features.feedback.Feedback;

import java.math.BigInteger;
import java.util.BitSet;

@AutoService(AbstractFeature.class)
@Feature(name = "Group Read", description = "Read a value from a block group", command = "gread")
public class GroupReadFeature extends CommandFeature {

    public static final Argument<String> groupName = Argument.ofType(BlockGroupsFeature.blockGroupNameSerializer());

    public static final Argument<Integer> base = Argument.ofType(NumberBaseSerializer.numberBase())
            .withDefault(10);

    @Override
    protected Feedback execute(ServerCommandSource source) throws CommandSyntaxException {
        BlockGroupManager blockGroupManager = BlockGroupManager.forServerWorld(source.getWorld());
        BlockGroup blockGroup = blockGroupManager.getBlockGroup(groupName.getValue());
        if (blockGroup == null) {
            return Feedback.error("Unknown block group {}", groupName.getValue());
        }

        if (blockGroup.getType() != null && !blockGroup.getType().canRead()) {
            return Feedback.error("Can not read from block group {} " +
                    " of type {}", groupName.getValue(), blockGroup.getType().getTypeName());
        }

        BitSet bitSet = blockGroup.readBitSet(source.getWorld(),
                // todo: also accept as on if it is powered,
                //  not just when it emits power
                AbstractBlock.AbstractBlockState::emitsRedstonePower);
        BigInteger bigInteger = new BigInteger(bitSet.toByteArray());

        return Feedback.success("Read {} bits from {} blocks, " +
                "result in base {}: {}", bitSet.length(), blockGroup.countBlocks(),
                base.getValue(), bigInteger.toString(base.getValue()));
    }

}
