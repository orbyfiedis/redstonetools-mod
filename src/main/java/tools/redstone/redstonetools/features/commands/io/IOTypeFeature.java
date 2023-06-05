package tools.redstone.redstonetools.features.commands.io;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.arguments.Argument;
import tools.redstone.redstonetools.features.arguments.serializers.EnumSerializer;
import tools.redstone.redstonetools.features.arguments.serializers.IOGroupNameSerializer;
import tools.redstone.redstonetools.features.commands.CommandFeature;
import tools.redstone.redstonetools.features.feedback.Feedback;
import tools.redstone.redstonetools.features.io.IOBlockGroup;
import tools.redstone.redstonetools.features.io.IOGroupType;
import tools.redstone.redstonetools.features.io.IOState;

import static tools.redstone.redstonetools.RedstoneToolsClient.INJECTOR;

@Feature(name = "IO Type", description = "Set/get the type of the given IO group.", command = "iotype")
public class IOTypeFeature extends CommandFeature {

    public static final Argument<String> groupName = Argument.ofType(IOGroupNameSerializer.ioGroupName());
    public static final Argument<IOGroupType> type = Argument.ofType(EnumSerializer.of(IOGroupType.class));

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

        if (type.getValue() == null) {
            // return current type
            return Feedback.success("Block group '" + blockGroupName + "' is of type " + blockGroup.getTypeDisplayName());
        } else {
            // set group type
            IOGroupType type = IOTypeFeature.type.getValue();
            blockGroup.setType(type);

            return Feedback.success("Set type of block group '" + blockGroupName + "' to " + blockGroup.getTypeDisplayName());
        }
    }

}
