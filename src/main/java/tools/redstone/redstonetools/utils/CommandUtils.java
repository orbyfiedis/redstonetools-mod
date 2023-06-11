package tools.redstone.redstonetools.utils;

import tools.redstone.redstonetools.features.arguments.Argument;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import tools.redstone.redstonetools.features.feedback.Feedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandUtils {
    private CommandUtils() { }

    public static void register(String name, List<Argument<?>> arguments, Command<ServerCommandSource> executor, CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // add command handler to executor
        Command<ServerCommandSource> safeExecutor = context -> {
            try {
                return executor.run(context);
            } catch (Exception e) {
                Feedback.error("An unexpected error occurred while trying to execute that command")
                        .send(context.getSource());
                e.printStackTrace();
                return 0;
            }
        };

        var base = CommandManager.literal(name);

        if (arguments.stream().allMatch(Argument::isOptional)) {
            base.executes(safeExecutor);
        }

        if (!arguments.isEmpty()) {
            base.then(createArgumentChain(arguments.stream()
                    .sorted((a, b) -> Boolean.compare(a.isOptional(), b.isOptional()))
                    .toList(), safeExecutor));
        }

        dispatcher.register(base);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createArgumentChain(List<Argument<?>> arguments, Command<ServerCommandSource> executor) {
        var reversedArguments = new ArrayList<>(arguments);
        Collections.reverse(reversedArguments);

        ArgumentBuilder<ServerCommandSource, ?> argument = null;
        for (var arg : reversedArguments) {
            if (argument == null) {
                argument = CommandManager.argument(arg.getName(), arg.getType()).executes(executor);
            } else {
                argument = CommandManager.argument(arg.getName(), arg.getType()).then(argument);

                // If the argument is optional or if this is the last required argument it should run the executor
                if (arg.isOptional() || reversedArguments.get(reversedArguments.indexOf(arg) - 1).isOptional()) {
                    argument.executes(executor);
                }
            }
        }

        return argument;
    }
}
