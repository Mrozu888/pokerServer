package org.example.server.commands;

import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.example.game.Board.boards;


public class QuitCommand implements Command {
    @Override
    public JSONObject execute(Player player, String args) {
        return switch (player.getState()) {
            case WAITING_FOR_GAME -> quit(player);
            default -> new JSONObject("{\"status\":\"Wrong operation\"}");
        };
    }

    private static JSONObject quit(Player player) {
        Board board = null;
        for (Board b : boards) {
            for (Player p : b.getPlayers()) {
                if (p == player) {
                    board = b;
                }
            }
        }
        board.removePlayer(player);

        JSONArray jsonArray = new JSONArray();
        for (Player p : board.getPlayers()) {
            jsonArray.put(p.getId());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("players", jsonArray);
        jsonObject.put("message", "player " + player.getName() + " quitted");
        jsonObject.put("status", "success");
        return jsonObject;
    }
}
