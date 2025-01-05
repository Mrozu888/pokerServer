package org.example.server.commands;


import org.example.game.Board;
import org.example.game.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.example.game.Board.boards;

public class ListCommand implements Command{
    @Override
    public JSONObject execute(Player player, String value) {
        return switch (player.getState()) {
            case LOBBY, WAITING_FOR_GAME -> listBoards();
            default -> new JSONObject("{\"status\":\"Wrong operation\"}");
        };
    }

    public static JSONObject listBoards(){
        if (boards.isEmpty()) new JSONObject("{\"status\":\"No active games\"}");
        JSONArray jsonArray = new JSONArray();
        for (Board board : boards) {
            jsonArray.put(board.toJSON());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("boards", jsonArray);
        jsonObject.put("status", "success");
        return jsonObject;
    }
}
