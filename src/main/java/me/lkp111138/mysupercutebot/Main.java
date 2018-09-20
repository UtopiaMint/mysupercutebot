package me.lkp111138.mysupercutebot;

import me.lkp111138.mysupercutebot.api.ApiServer;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import me.lkp111138.mysupercutebot.helpers.Looper;

import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main {
    // ping wynn api every 20secs yay
    private static ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(2);

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // set up sql connection
        DatabaseHelper.init();
        new Thread(new ApiServer()).start();
        new Looper().run();
    }

    public static ScheduledThreadPoolExecutor getPool() {
        return pool;
    }
}
