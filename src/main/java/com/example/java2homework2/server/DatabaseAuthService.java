package com.example.java2homework2.server;

import java.io.IOException;

public class DatabaseAuthService implements AuthService{
    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        return Database.getUserNickname(login, password);
    }

    @Override
    public void run() {

    }

    @Override
    public void close() throws IOException {

    }
}
