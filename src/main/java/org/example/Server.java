package org.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;

import org.example.game.Player;
import org.json.JSONObject;

import static org.example.server.ClientHandler.acceptConnection;
import static org.example.server.ClientHandler.handleRead;

public class Server {
    // Store the mapping of SocketChannel to Player
    public static HashMap<SocketChannel, Player> players = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new java.net.InetSocketAddress(12345));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server is running...");

        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    acceptConnection(serverSocketChannel, selector);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }
}
