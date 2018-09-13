package me.lkp111138.mysupercutebot.objects;

import me.lkp111138.mysupercutebot.Main;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import org.json.JSONArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class War {
    private String server;
    private int id;
    private String guild;
    private Set<String> players = new HashSet<>();

    private static Map<String, War> guild_war = new HashMap<>();
    private static Map<String, War> server_war = new HashMap<>();

    public War(String server) {
        this.server = server;
        server_war.put(server, this);
        id = DatabaseHelper.new_war_server(server);
    }

    public String getGuild() {
        return guild;
    }

    public void setGuild(String guild) {
        if (this.guild == null) {
            this.guild = guild;
            guild_war.put(guild, this);
        }
    }

    private void addPlayer(String name) {
        players.add(name);
    }

    public void putPlayerList(JSONArray _players) {
        if (_players.isEmpty()) {
            // hold on, the war may have ended, don't mark them dead
            close();
            return;
        }
        for (int i = 0; i < _players.length(); ++i) {
            if (!players.contains(_players.getString(i))) {
                removePlayer(_players.getString(i));
            } else {
                addPlayer(_players.getString(i));
            }
        }
    }

    private void removePlayer(String name) {
        // is dead
        Connection conn = DatabaseHelper.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("update player_war_log set survived=0 where ign=? and war_id=?");
            stmt.setString(1, name);
            stmt.setInt(2, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // TODO: implementation
    public void close() {
        // we wait 20 secs and poll db for a terr gain event for a maximum of 5 times
        // if that doesn't work you lost
        Main.getPool().schedule(() -> {
            long last_run =  System.currentTimeMillis();
            for (int i = 1; i <= 5; ++i) {
                try {
                    Thread.sleep(last_run + 20000 - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                last_run = System.currentTimeMillis();
                // search for a terr gain event
                int now = (int) (System.currentTimeMillis() / 1000);
                Connection conn = DatabaseHelper.getConnection();
                try {
                    PreparedStatement stmt = conn.prepareStatement("select count(*) from terr_log where attacker=? and acquired<?");
                    stmt.setString(1, guild);
                    stmt.setInt(2, now - 20 * i);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 1) {
                        // congrats on winning
                        stmt = conn.prepareStatement("update player_war_log set won=1 where war_id=?");
                        stmt.setInt(1, id);
                        stmt.execute();
                        stmt = conn.prepareStatement("update player_war_log set survived=1 where war_id=? and survived is null");
                        stmt.setInt(1, id);
                        stmt.execute();
                        break;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            // if this is reached then u lost oof
            try {
                PreparedStatement stmt = DatabaseHelper.getConnection().prepareStatement("update player_war_log set survived=0, won=0 and war_id=?");
                stmt.setInt(1, id);
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 20, TimeUnit.SECONDS);
    }

    public static War byGuild(String guild) {
        return guild_war.get(guild);
    }

    public static War byServer(String server) {
        return server_war.get(server);
    }
}
