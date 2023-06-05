package tools.redstone.redstonetools.features.arguments.serializers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import tools.redstone.redstonetools.features.io.IOState;
import tools.redstone.redstonetools.utils.CommandSourceUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IOGroupNameSerializer extends TypeSerializer<String, String> {

    static final IOGroupNameSerializer instance = new IOGroupNameSerializer();

    public static IOGroupNameSerializer ioGroupName() {
        return instance;
    }

    protected IOGroupNameSerializer() {
        super(String.class);
    }

    @Override
    public String deserialize(StringReader reader) {
        return deserialize(reader.readUnquotedString());
    }

    @Override
    public String deserialize(String serialized) {
        return serialized;
    }

    @Override
    public String serialize(String value) {
        return value;
    }

    @Override
    public Collection<String> getExamples() {
        return List.of();
    }

    @Override
    public <R> CompletableFuture<Suggestions> listSuggestions(CommandContext<R> context, SuggestionsBuilder builder) {
        ServerWorld world = CommandSourceUtils.getWorld((CommandSource) context.getSource());
        if (world == null)
            return builder.buildFuture();

        IOState state = IOState.forWorld(world);
        for (String key : state.getBlockGroups().keySet()) {
            builder.suggest(key);
        }

        return builder.buildFuture();
    }

}
