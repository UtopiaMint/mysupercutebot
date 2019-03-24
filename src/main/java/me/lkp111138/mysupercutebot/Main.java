package me.lkp111138.mysupercutebot;

import me.lkp111138.mysupercutebot.api.ApiServer;
import me.lkp111138.mysupercutebot.api.discord.DiscordBot;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import me.lkp111138.mysupercutebot.helpers.Looper;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main {
    // ping wynn api every 20secs yay
    private static ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(2);
    private static long started;

    public static void main(String[] args) throws ClassNotFoundException, SQLException, LoginException {
        // set up sql connection
        DatabaseHelper.init();
        new Thread(new ApiServer()).start();
        started = System.currentTimeMillis();
        if (args.length > 0) {
            DiscordBot.init(args[0]);
        } else {
            DiscordBot.init();
        }
        new Looper().run();
    }

    public static ScheduledThreadPoolExecutor getPool() {
        return pool;
    }

    public static long getStarted() {
        return started;
    }
}
