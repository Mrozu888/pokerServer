package org.example.server.commands;


import org.example.game.Player;
import org.json.JSONObject;

import java.io.IOException;

public interface Command {
    public abstract JSONObject execute(Player player, String value) throws IOException;
}
