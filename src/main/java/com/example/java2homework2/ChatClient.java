package com.example.java2homework2;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    private static final Logger log = LogManager.getLogger(ChatClient.class);

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final Controller controller;

    private File historyFile;
    private String nick;

    ExecutorService executorService = Executors.newCachedThreadPool();

    public ChatClient(Controller controller) {
        this.controller = controller;
    }

    public void openConnection() throws Exception {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        executorService.execute(() -> {
            try {
                waitAuthenticate();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
                executorService.shutdown();
            }
        });

        /*        final Thread readThread = new Thread(() -> {
            try {
                waitAuthenticate();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });
        readThread.setDaemon(true);
        readThread.start();*/

    }

    private void readMessage() throws IOException {
        while (true) {
            final String message = in.readUTF();
            System.out.println("Receive message: " + message);
            if (Command.isCommand(message)) {
                final Command command = Command.getCommand(message);
                final String[] params = command.parse(message);
                if (command == Command.END) {
                    controller.setAuth(false);
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params));
                    continue;
                }
                if (command == Command.CLIENTS) {
                    controller.updateClientList(params);
                    continue;
                }
            }
            controller.addMessage(message);
        }
    }

    private void waitAuthenticate() throws IOException {

        Thread waitTime = new Thread(() -> {
            while (true && !Thread.interrupted()) {
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                closeConnection();
            }
        });

        waitTime.start();

        while (true) {
            final String msgAuth = in.readUTF();
            if (Command.isCommand(msgAuth)) {
                final Command command = Command.getCommand(msgAuth);
                final String[] params = command.parse(msgAuth);
                if (command == Command.AUTHOK) {
                    nick = params[0];
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    controller.setAuth(true);

                    historyFile = new File("history_" + nick + ".his");
                    try {
                        if (!historyFile.exists()) {
                            historyFile.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    waitTime.interrupt();
                    break;
                }
                if (Command.ERROR.equals(command)) {
                    Platform.runLater(() -> controller.showError(params));
                }
            }
        }
    }

    private void closeConnection() {

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
        executorService.shutdown();
    }

    public void sendMessage(String message) {
        try {
            log.info("Send message: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    public String getNick() {
        return nick;
    }
}
