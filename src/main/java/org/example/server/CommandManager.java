package org.example.server;

import org.example.game.Player;
import org.example.server.commands.Command;
import org.json.JSONObject;
import org.reflections.Reflections;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.example.Server.players;

public class CommandManager {
    private static final Map<String, Class<? extends Command>> commands = new HashMap<>();

    static {
        // Automatyczne skanowanie klas w pakiecie
        Reflections reflections = new Reflections("org.example.server.commands");
        for (Class<? extends Command> clazz : reflections.getSubTypesOf(Command.class)) {
            commands.put(clazz.getSimpleName().toLowerCase().replace("command",""), clazz);
        }
    }

    public static Command getCommand(String name) {
        Class<? extends Command> commandClass = commands.get(name.toLowerCase());
        if (commandClass == null) {
            return new Command() {
                @Override
                public JSONObject execute(Player player, String value) {
                    JSONObject response = new JSONObject();
                    response.put("status", "wrong command");
                    return response;
                }
            };
        }
        try {
            return commandClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate command: " + name, e);
        }
    }

    public static JSONObject manage(Player player, JSONObject json) throws IOException {
        Command command = getCommand(json.getString("command").toLowerCase());
        return command.execute(player, json.getString("value"));
    }

    public static SocketChannel getPlayerChannel(Player player) {
        for (Map.Entry<SocketChannel, Player> entry : players.entrySet()) {
            if (entry.getValue().equals(player)) {
                return entry.getKey();
            }
        }
        return null; // Jeśli nie znaleziono gracza
    }

}
