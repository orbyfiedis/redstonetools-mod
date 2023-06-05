package tools.redstone.redstonetools.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.lang.reflect.Field;

public class CommandSourceUtils {
    private static final Field CCS_CLIENT_FIELD;

    static {
        try {
            CCS_CLIENT_FIELD = ClientCommandSource.class.getDeclaredField("client");
            CCS_CLIENT_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private CommandSourceUtils() {
    }

    public static void executeCommand(ServerCommandSource source, String command) {
        source.getServer().getCommandManager().execute(source, command);
    }

    public static MinecraftClient getClient(ClientCommandSource source) {
        try {
            return (MinecraftClient) CCS_CLIENT_FIELD.get(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ServerWorld getWorld(CommandSource source) {
        if (source instanceof ClientCommandSource s) {
            MinecraftClient client = getClient(s);
            if (client.world == null)
                return null;
            if (client.world.getServer() == null)
                return null;
            RegistryKey<World> key = client.world.getRegistryKey();
            return client.world.getServer().getWorld(key);
        }

        if (source instanceof ServerCommandSource s)
            return s.getWorld();

        return null;
    }
}
