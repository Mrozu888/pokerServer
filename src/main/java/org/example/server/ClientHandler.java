package org.example.server;

import org.example.game.Player;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static org.example.Server.players;
import static org.example.server.CommandManager.manage;
import static org.example.server.Message.*;

public class ClientHandler {

    public static void acceptConnection(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client connected: " + socketChannel.getRemoteAddress());
    }

    public static void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = socketChannel.read(buffer);

        if (bytesRead == -1) {
            // Client disconnected, remove them from the map
            Player player = players.remove(socketChannel);
            socketChannel.close();
            System.out.println("Client disconnected: " + (player != null ? player.getName() : "Unknown"));
            return;
        }

        String message = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
        System.out.println("Received message: " + message);

        // Try to parse the incoming message as JSON
        try {
            JSONObject jsonMessage = new JSONObject(message);
            // Check if the message contains a "username" field
            if (jsonMessage.has("username")) {
                String username = jsonMessage.getString("username");
                // Create a new player and associate it with the socketChannel
                Player newPlayer = new Player(username, 1000);
                players.put(socketChannel, newPlayer);  // Add the player and their channel to the map
                System.out.println("Created new player: " + newPlayer);

                // Optionally, send a response back to the client
                JSONObject responseMessage = new JSONObject();
                responseMessage.put("status", "success");
                responseMessage.put("message", "Player created: " + newPlayer.getName());
//                sendMessage(socketChannel, responseMessage.toString());

//                 Create a broadcast message in JSON format
                JSONObject broadcastMessageJson = new JSONObject();
                broadcastMessageJson.put("message", "New player joined: " + newPlayer.getName());

                // Broadcast the JSON message to all clients
                broadcastMessage(broadcastMessageJson.toString());


            }
            if (jsonMessage.has("command")) {
                JSONObject json = manage(players.get(socketChannel), jsonMessage);
                // handling message
                send(socketChannel, json);
            }
        } catch (Exception e) {
            // Handle the case where the message is not valid JSON
            System.out.println("Invalid JSON message received. ClientHandler");
        }


    }

}
