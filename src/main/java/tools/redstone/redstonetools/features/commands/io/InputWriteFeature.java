package tools.redstone.redstonetools.features.commands.io;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.arguments.Argument;
import tools.redstone.redstonetools.features.arguments.serializers.BigIntegerSerializer;
import tools.redstone.redstonetools.features.arguments.serializers.IOGroupNameSerializer;
import tools.redstone.redstonetools.features.commands.CommandFeature;
import tools.redstone.redstonetools.features.feedback.Feedback;
import tools.redstone.redstonetools.features.io.IOBlockGroup;
import tools.redstone.redstonetools.features.io.IOState;

import java.math.BigInteger;
import java.util.BitSet;

import static tools.redstone.redstonetools.RedstoneToolsClient.INJECTOR;

@Feature(name = "Input Write", description = "Writes a value to an IO input.", command = "iwrite")
public class InputWriteFeature extends CommandFeature {

    public static final Argument<String> groupName = Argument.ofType(IOGroupNameSerializer.ioGroupName());

    public static final Argument<BigInteger> value = Argument.ofType(BigIntegerSerializer.bigInteger());

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
        if (!blockGroup.canWrite()) {
            return Feedback.error("IO group '" + blockGroupName + "' of type " +
                    blockGroup.getTypeDisplayName() + " can not be written to.");
        }

        BlockState onBit = blockGroup.getOnBitState();
        BlockState offBit = blockGroup.getOffBitState();

        BitSet value = BitSet.valueOf(InputWriteFeature.value.getValue().toByteArray());
        return blockGroup.writeBitSet(
                value,
                source.getWorld(),
                (on, state) -> on ? onBit : offBit
        );
    }

}
