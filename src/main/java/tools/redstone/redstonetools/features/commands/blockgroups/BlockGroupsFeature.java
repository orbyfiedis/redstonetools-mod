package tools.redstone.redstonetools.features.commands.blockgroups;

import com.google.auto.service.AutoService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.sk89q.worldedit.regions.Region;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import tools.redstone.redstonetools.features.AbstractFeature;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.arguments.serializers.EnumSerializer;
import tools.redstone.redstonetools.features.arguments.serializers.StringSerializer;
import tools.redstone.redstonetools.features.arguments.serializers.TypeSerializer;
import tools.redstone.redstonetools.features.blockgroups.BlockGroup;
import tools.redstone.redstonetools.features.blockgroups.BlockGroupManager;
import tools.redstone.redstonetools.features.blockgroups.BlockGroupType;
import tools.redstone.redstonetools.features.feedback.Feedback;
import tools.redstone.redstonetools.utils.WorldEditUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@AutoService(AbstractFeature.class)
@Feature(name = "Block Groups", description = "Block groups management feature", command = "bg")
public class BlockGroupsFeature
        /* extends directly because we want to use brigadier to create our commands */
        extends AbstractFeature {

    public static TypeSerializer<String, String> blockGroupNameSerializer() {
        return StringSerializer.word(); // todo: add completion
    }

    @Override
    protected void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // bg
        var baseCommand = literal("bg")
                .executes(context -> Feedback.error("Please provide a subcommand").send(context));

        // bg list
        baseCommand.then(literal("list").executes(context -> {
            ServerCommandSource source = context.getSource();
            BlockGroupManager bg = BlockGroupManager.forServerWorld(source.getWorld());

            // list all block groups
            MutableText text = new LiteralText(Formatting.DARK_AQUA + "All block groups for world " + Formatting.AQUA + source.getWorld().getRegistryKey()
                    .getValue().getPath() +
                    Formatting.GRAY + " (" + bg.getBlockGroups().size() + ")\n");

            List<BlockGroup> blockGroupsOrdered = new ArrayList<>(bg.getBlockGroups()); // sort alphabetically
            blockGroupsOrdered.sort(Comparator.comparing(BlockGroup::getName));
            for (BlockGroup g : blockGroupsOrdered) {
                text.append(Text.of(
                        Formatting.DARK_GRAY + "» "
                ));

                // append buttons
                MutableText buttons = new LiteralText("");
                buttons.append(new LiteralText("[␡] ").setStyle(
                        Style.EMPTY
                                .withFormatting(Formatting.DARK_RED)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bg remove " + g.getName()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(
                                        Formatting.RED + "Delete the block group " + Formatting.WHITE + g.getName())))
                ));
                buttons.append(new LiteralText("[✎] ").setStyle(
                        Style.EMPTY
                                .withFormatting(Formatting.DARK_AQUA)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bg edit " + g.getName()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(
                                        Formatting.RED + "Edit the block group " + Formatting.AQUA + g.getName())))
                ));
                text.append(buttons);

                text.append(Text.of(
                        Formatting.GOLD + g.getName() + " " +
                                (g.getType() == null ? Formatting.DARK_GRAY + "" + Formatting.BOLD + "- " :
                                        g.getType().getFormattedTypeIcon() + " ") +
                                Formatting.GRAY + "(" + g.countBlocks() + ")\n"
                ));
            }

            // add creation button
            text.append(new LiteralText("[+] ").setStyle(
                    Style.EMPTY.withBold(true).withFormatting(Formatting.GREEN)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/bg create <name> <type>")))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bg create "))
            ));

            text.append(new LiteralText("Create new block group").setStyle(
                    Style.EMPTY.withFormatting(Formatting.GREEN)
            ));

            source.sendFeedback(text, false);
            return 1;
        }));

        // bg remove
        baseCommand.then(literal("remove").then(argument("name", blockGroupNameSerializer()).executes(context -> {
            ServerCommandSource source = context.getSource();
            BlockGroupManager blockGroupManager = BlockGroupManager.forServerWorld(source.getWorld());

            String name = context.getArgument("name", String.class);
            BlockGroup blockGroup = blockGroupManager.getBlockGroup(name);
            if (blockGroup == null) {
                return Feedback.error("No block group by name {}", name).send(source);
            }

            blockGroup.remove();
            return Feedback.success("Removed block group by name {}", name).send(source);
        })));

        // bg sort
        /* sorts all blocks in the given block group from
           left to right based on the players facing */
        baseCommand.then(literal("sort").then(argument("group", blockGroupNameSerializer()).executes(context -> {
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity player = source.getPlayer();

            String groupName = context.getArgument("group", String.class);
            BlockGroupManager blockGroupManager = BlockGroupManager.forServerWorld(source.getWorld());
            BlockGroup blockGroup = blockGroupManager.getBlockGroup(groupName);
            if (blockGroup == null) {
                return Feedback.error("No block group by name {}", groupName).send(source);
            }

            // sort blocks based on player facing
            // get facing to the left from the player facing
            Direction direction = player.getHorizontalFacing()
                    .rotateCounterclockwise(Direction.Axis.Y);

            // todo: account for vertical pos as well
            blockGroup.getBlocks().sort((pos1, pos2) -> switch (direction) {
                case EAST -> pos1.getX() > pos2.getX() ? 1 : -1;
                case WEST -> pos1.getX() < pos2.getX() ? 1 : -1;
                case NORTH -> pos1.getZ() > pos2.getZ() ? 1 : -1;
                case SOUTH -> pos1.getZ() < pos2.getZ() ? 1 : -1;

                default -> 0;
            });

            return Feedback.success("Sorted all {} blocks from block group {}",
                    blockGroup.countBlocks(), blockGroup.getName()).send(source);
        })));

        // bg create
        baseCommand.then(literal("create")
                .then(argument("name", StringSerializer.word())
                .then(argument("type", EnumSerializer.of(BlockGroupType.class))
                .executes(context -> {
                    String name = context.getArgument("name", String.class);
                    BlockGroupType type = context.getArgument("type", BlockGroupType.class);

                    ServerCommandSource source = context.getSource();
                    BlockGroupManager blockGroupManager = BlockGroupManager.forServerWorld(source.getWorld());

                    BlockGroup blockGroup = blockGroupManager.createBlockGroup(name)
                            .setType(type);

                    // try to add initial blocks from world edit selection
                    Either<Region, Feedback> selOptional;
                    if (FabricLoader.getInstance().isModLoaded("WorldEdit") &&
                            (selOptional = WorldEditUtils.getSelection(context.getSource().getPlayer())).left().isPresent()) {
                        Region selection = selOptional.left().get();
                        ServerWorld world = source.getWorld();

                        // match blocks and add if matching
                        WorldEditUtils.forEachBlockInRegion(selection, blockVector3 -> {
                            BlockPos blockPos = WorldEditUtils.toBlockPos(blockVector3);
                            BlockState state = world.getBlockState(blockPos);

                            if (/* condition */ state.isOpaque()) {
                                blockGroup.addBlock(blockPos);
                            }
                        });
                    } else {
                        Feedback.warning("No WorldEdit selection, no blocks will be added initially").send(source);
                    }

                    return Feedback.success("Created block group {} of type {} with {} blocks initialized",
                            name, type, blockGroup.countBlocks()).send(source);
                }))));

        // bg edit
        baseCommand.then(literal("edit").then(argument("group", blockGroupNameSerializer()).executes(context -> {
            return Feedback.warning("TODO").send(context.getSource());
        })));

        dispatcher.register(baseCommand);
    }

}
