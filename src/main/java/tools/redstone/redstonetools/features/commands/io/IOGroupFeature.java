package tools.redstone.redstonetools.features.commands.io;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.arguments.Argument;
import tools.redstone.redstonetools.features.arguments.serializers.EnumSerializer;
import tools.redstone.redstonetools.features.arguments.serializers.IOGroupNameSerializer;
import tools.redstone.redstonetools.features.arguments.serializers.StringSerializer;
import tools.redstone.redstonetools.features.commands.CommandFeature;
import tools.redstone.redstonetools.features.feedback.Feedback;
import tools.redstone.redstonetools.features.io.IOGroupType;
import tools.redstone.redstonetools.features.io.IOState;

import java.util.Locale;
import java.util.function.Predicate;

@Feature(name = "IO Group", description = "Modifies the IO groups in this world", command = "iogroup")
public class IOGroupFeature extends CommandFeature {

    public enum Subcommand {
        /** Create a new group */
        CREATE,

        /** Remove the group by name */
        REMOVE,

        /** Select the group by name */
        SELECT;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Argument<Subcommand> subcommand = Argument.ofType(EnumSerializer.of(Subcommand.class));
    public static final Argument<String> name = Argument.ofType(IOGroupNameSerializer.ioGroupName());

    public volatile String selectedGroup;
    public volatile Predicate<ItemStack> toolPredicate = stack -> stack.getItem() == Items.LARGE_FERN;

    @Override
    protected Feedback execute(ServerCommandSource source) throws CommandSyntaxException {
        final ServerWorld world = source.getWorld();
        final IOState ioState = IOState.forWorld(world);

        final String name = IOGroupFeature.name.getValue();

        switch (subcommand.getValue()) {
            case CREATE -> {
                if (ioState.hasBlockGroup(name)) {
                    return Feedback.error("An IO group with the name '" + name + "' already exists");
                }

                ioState.createBlockGroup(name);
                return Feedback.success("Successfully created IO group");
            }

            case REMOVE -> {
                if (!ioState.hasBlockGroup(name)) {
                    return Feedback.error("No IO group by name '" + name + "' was found");
                }

                ioState.removeBlockGroup(name);
                return Feedback.success("Successfully removed IO group");
            }

            case SELECT -> {
                if ("none".equals(name)) {
                    synchronized (this) {
                        selectedGroup = null;
                    }

                    return Feedback.success("Unselected any IO group");
                }

                if (!ioState.hasBlockGroup(name)) {
                    return Feedback.error("No IO group by name '" + name + "' was found");
                }

                synchronized (this) {
                    selectedGroup = name;
                }

                return Feedback.success("Selected IO group '" + name + "'");
            }
        }

        return Feedback.error("wtf");
    }

}
