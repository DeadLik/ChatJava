package com.example.java2homework2.server;

import com.example.java2homework2.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    private static final Logger log = LogManager.getLogger(ChatServer.class);

    private final Map<String, ClientHandler> clients;

    public ChatServer() {
        this.clients = new HashMap<>();

        if (!Database.connect()) {
            log.error("Не удается подключиться к базе данных");
            throw new RuntimeException();
        }
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new DatabaseAuthService()) {
            while (true) {
                log.info("Ожидание подключения...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                log.info("Клиент подключился");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Database.disconnect();
        }
    }

    public boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
    }

    private void broadcastClientList() {
        StringBuilder nicks = new StringBuilder();
        for (ClientHandler value : clients.values()) {
            nicks.append(value.getNick()).append(" ");
        }

        broadcast(Command.CLIENTS, nicks.toString().trim());
    }

    private void broadcast(Command command, String nicks) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command, nicks);
        }
    }

    public void broadcast(String msg) {
        clients.values().forEach(client -> client.sendMessage(msg));
    }

    public void sendMessageToClient(ClientHandler sender, String to, String message) {
        final ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.sendMessage("от " + sender.getNick() + ": " + message);
            sender.sendMessage("участнику " + to + ": " + message);
        } else {
            sender.sendMessage(Command.ERROR, "Участника с ником " + to + " нет в чате!");
        }
    }
}
