package tools.redstone.redstonetools.features.commands.io;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.commands.CommandFeature;
import tools.redstone.redstonetools.features.feedback.Feedback;
import tools.redstone.redstonetools.features.io.IOBlockGroup;
import tools.redstone.redstonetools.features.io.IOState;
import tools.redstone.redstonetools.utils.BlockPosUtils;

import static tools.redstone.redstonetools.RedstoneToolsClient.INJECTOR;

@Feature(name = "IO Add", description = "Adds the block you're looking at to the selected IO group.", command = "ioadd")
public class IOAddFeature extends CommandFeature {

    @Override
    protected Feedback execute(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();

        // get block position
        Vec3d hit = player.raycast(3.0f, 0, true).getPos();
        BlockPos blockPos = new BlockPos((int)hit.x, (int)hit.y, (int)hit.z);

        IOState ioState = IOState.forWorld(source.getWorld());
        String selectedGroup = INJECTOR.getInstance(IOGroupFeature.class).selectedGroup;
        IOBlockGroup blockGroup;
        if (selectedGroup == null || (blockGroup = ioState.getBlockGroup(selectedGroup)) == null)
            return Feedback.error("No IO group currently selected");

        blockGroup.addBlock(blockPos);
        return Feedback.success("Added block " + BlockPosUtils.toStringPretty(blockPos) + " to IO block group: " + selectedGroup);
    }

}
