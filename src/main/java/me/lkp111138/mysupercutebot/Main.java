package me.lkp111138.mysupercutebot;

import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import me.lkp111138.mysupercutebot.helpers.Looper;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    private static Connection connection;
    // ping wynn api every 20secs yay
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // set up sql connection
        DatabaseHelper.init();
        new Looper().run();
    }
}
