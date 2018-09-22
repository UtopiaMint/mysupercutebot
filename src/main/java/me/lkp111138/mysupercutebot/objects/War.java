package me.lkp111138.mysupercutebot.objects;

import me.lkp111138.mysupercutebot.Main;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import me.lkp111138.mysupercutebot.helpers.Functions;
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
    private boolean closed = false;
    private boolean started = false;

    private static Map<String, War> guild_war = new HashMap<>();
    private static Map<String, War> server_war = new HashMap<>();

    public War(String server, int start_time) {
        this.server = server;
        server_war.put(server, this);
        id = DatabaseHelper.new_war_server(server, start_time);
        System.out.printf("war id: %d\n", id);
    }

    public String getGuild() {
        return guild;
    }

    public void setGuild(String guild) {
        if (this.guild == null) {
            this.guild = guild;
            guild_war.put(guild, this);
            Connection conn = DatabaseHelper.getConnection();
            try {
                PreparedStatement stmt = conn.prepareStatement("update war_log set guild=? where id=?");
                stmt.setString(1, guild);
                stmt.setInt(2, id);
                stmt.execute();
                // update or insert to aggregated data
                stmt = conn.prepareStatement("update war_log_aggregated set total=total+1 where guild=?");
                stmt.setString(1, guild);
                int affected = stmt.executeUpdate();
                if (affected < 1) {
                    // insert
                    stmt = conn.prepareStatement("insert into war_log_aggregated (guild, total, won) VALUES (?, 1, 0)");
                    stmt.setString(1, guild);
                    stmt.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPlayer(String name) {
        System.out.printf("trying to add %s\n", name);
        started = true;
        if (players.add(name)) {
            System.out.printf("adding %s\n", name);
            Connection conn = DatabaseHelper.getConnection();
            try {
                String uuid = Functions.ign2uuid(name);
                PreparedStatement stmt = conn.prepareStatement("insert into player_war_log (war_id, uuid, ign, guild) values (?, ?, ?, ?)");
                stmt.setInt(1, id);
                stmt.setString(2, uuid);
                stmt.setString(3, name);
                stmt.setString(4, guild);
                stmt.execute();
                // update or insert to aggregated data
                stmt = conn.prepareStatement("update player_war_log_aggregated set total=total+1 where uuid=?");
                stmt.setString(1, uuid);
                int affected = stmt.executeUpdate();
                if (affected < 1) {
                    // insert
                    stmt = conn.prepareStatement("insert into player_war_log_aggregated (uuid, guild, total, won, survived) VALUES (?, ?, 1, 0, 0)");
                    stmt.setString(1, uuid);
                    stmt.setString(2, guild);
                    stmt.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void putPlayerList(JSONArray _players, int time) {
        if (_players.isEmpty()) {
            // hold on, the war may have ended, don't mark them dead
            _close(time);
            return;
        }
        // add everybody that's in the war server
        for (int i = 0; i < _players.length(); ++i) {
            String name = _players.getString(i);
            if (!players.contains(name)) {
                addPlayer(name);
            }
        }
        // and remove any that is no longer in
        for (String name : players) {
            if (!_players.toList().contains(name)) {
                removePlayer(name);
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
            stmt.execute();
            // dont need to update aggregated stats cuz +0
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // called when the war server is gone
    public void close(int end_time) {
        started = true;
        _close(end_time);
    }

    private void _close(int end_time) {
        // we wait 20 secs and poll db for a terr gain event for a maximum of 5 times
        // if that doesn't work you lost
        if (closed || !started) return;
        System.out.printf("closing war %d\n", id);
        closed = true;
        guild_war.remove(guild);
        server_war.remove(server);
        PreparedStatement stmt1;
        try {
            stmt1 = DatabaseHelper.getConnection().prepareStatement("update war_log set end_time=? where id=?");
            stmt1.setInt(1, end_time);
            stmt1.setInt(2, id);
            stmt1.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Main.getPool().schedule(() -> {
            long last_run =  System.currentTimeMillis();
            for (int i = 6; i < 11; ++i) {
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
                    // terr gain time < war end time
                    // terr gain time + 50 > war end time
                    PreparedStatement stmt = conn.prepareStatement("select count(*), max(id) from terr_log where attacker=? and acquired>=? and acquired<=?");
                    stmt.setString(1, guild);
                    stmt.setInt(2, end_time - 80);
                    stmt.setInt(3, end_time); // i don't think the war ends before you acquire
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) >= 1) {
                        // congrats on winning
                        stmt = conn.prepareStatement("update player_war_log_aggregated set won=won+1 where uuid in (select uuid from player_war_log where war_id=?) and guild=?");
                        stmt.setInt(1, id);
                        stmt.setString(2, guild);
                        stmt.execute();
                        System.out.printf("found a terr log for %s war %d, iteration %d\n", guild, id, i - 6);
                        stmt = conn.prepareStatement("update player_war_log set won=1 where war_id=?");
                        stmt.setInt(1, id);
                        stmt.execute();
                        stmt = conn.prepareStatement("update player_war_log_aggregated set survived=survived+1 where uuid in (select uuid from player_war_log where war_id=? and survived is null) and guild=?");
                        stmt.setInt(1, id);
                        stmt.setString(2, guild);
                        stmt.execute();
                        stmt = conn.prepareStatement("update player_war_log set survived=1 where war_id=? and survived is null");
                        stmt.setInt(1, id);
                        stmt.execute();
                        stmt = conn.prepareStatement("update war_log_aggregated set won=won+1 where guild=(select guild from war_log where id=?)");
                        stmt.setInt(1, id);
                        stmt.execute();
                        stmt = conn.prepareStatement("update war_log set terr_entry=? where id=?");
                        stmt.setInt(1, rs.getInt(2));
                        stmt.setInt(2, id);
                        stmt.execute();
                        return;
                    }
                    System.out.printf("records found： %d\n", rs.getInt(1));
                    System.out.printf("didnt find a terr log for %s war %d yet, iteration %d, looking for terr log after %d\n", guild, id, i - 6, now - 20 * i);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            // if this is reached then u lost oof
            System.out.printf("didnt find a terr log for %s war %d\n", guild, id);
            try {
                PreparedStatement stmt = DatabaseHelper.getConnection().prepareStatement("update player_war_log set survived=0, won=0 where war_id=?");
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
