package tools.redstone.redstonetools.features.commands.io;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.arguments.Argument;
import tools.redstone.redstonetools.features.arguments.serializers.IOGroupNameSerializer;
import tools.redstone.redstonetools.features.arguments.serializers.IntegerSerializer;
import tools.redstone.redstonetools.features.commands.CommandFeature;
import tools.redstone.redstonetools.features.feedback.Feedback;
import tools.redstone.redstonetools.features.io.IOBlockGroup;
import tools.redstone.redstonetools.features.io.IOState;

import java.math.BigInteger;
import java.util.BitSet;

import static tools.redstone.redstonetools.RedstoneToolsClient.INJECTOR;

@Feature(name = "Output Read", description = "Read the output IO group.", command = "oread")
public class OutputReadFeature extends CommandFeature {

    public static final Argument<String> groupName = Argument.ofType(IOGroupNameSerializer.ioGroupName());

    public static final Argument<Integer> base = Argument.ofType(IntegerSerializer.integer(2, 36))
            .withDefault(2);

    @Override
    protected Feedback execute(ServerCommandSource source) throws CommandSyntaxException {
        IOState ioState = IOState.forWorld(source.getWorld());
        String blockGroupName = groupName.getValue() == null ?
                INJECTOR.getInstance(IOGroupFeature.class).selectedGroup :
                groupName.getValue();
        if (blockGroupName == null || !ioState.hasBlockGroup(blockGroupName)) {
            return Feedback.success("Please specify or select a valid IO group");
        }

        IOBlockGroup blockGroup = ioState.getBlockGroup(blockGroupName);
        if (!blockGroup.canRead()) {
            return Feedback.error("IO group '" + blockGroupName + "' of type " +
                    blockGroup.getTypeDisplayName() + " can not be read from.");
        }

        int base = OutputReadFeature.base.getValue();

        BlockState onBit = blockGroup.getOnBitState();
        BitSet bitSet = blockGroup.readBitSet(source.getWorld(), state -> state == onBit);

        return Feedback.success("Read " + blockGroup.getBlocks().size() + " bits, " +
                ", value(radix " + base + "): " + new BigInteger(bitSet.toByteArray()));
    }

}
