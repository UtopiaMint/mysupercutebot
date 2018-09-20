package me.lkp111138.mysupercutebot.api;

import com.sun.net.httpserver.HttpServer;
import me.lkp111138.mysupercutebot.api.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ApiServer implements Runnable {
    private static Executor pool = Executors.newCachedThreadPool();

    @Override
    public void run() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(6080), 0);
            server.createContext("/v1/warlogs/guild/", new GuildWarLogHandler());
            server.createContext("/v1/warlogs/player/", new PlayerWarLogHandler());
            server.createContext("/v1/warlogs/leaderboard/guild", new GuildWarLeaderboardHandler());
            server.createContext("/v1/warlogs/leaderboard/player", new PlayerWarLeaderboardHandler());
            server.createContext("/v1/warlogs/leaderboard/guild/", new GuildPlayerWarLeaderboardHandler());
            server.setExecutor(pool);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
