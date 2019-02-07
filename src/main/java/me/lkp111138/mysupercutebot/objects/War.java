package me.lkp111138.mysupercutebot.objects;

import me.lkp111138.mysupercutebot.Main;
import me.lkp111138.mysupercutebot.api.discord.DiscordBot;
import me.lkp111138.mysupercutebot.helpers.DatabaseHelper;
import me.lkp111138.mysupercutebot.helpers.Functions;
import net.dv8tion.jda.core.entities.Message;
import org.json.JSONArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class War {
    private String server;
    private final int start_time;
    private int end_time;
    private int id;
    private String guild;
    private Set<String> players = new HashSet<>();
    private Set<String> unique_players = new HashSet<>();
    private boolean closed = false;
    private boolean started = false;
    private HashMap<Long, Message> messages = new HashMap<>();

    private static Map<String, War> guild_war = new HashMap<>();
    private static Map<String, War> server_war = new HashMap<>();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public War(String server, int start_time) {
        this.server = server;
        this.start_time = start_time;
        server_war.put(server, this);
        id = DatabaseHelper.new_war_server(server, start_time);
        System.out.printf("war id: %d\n", id);
    }

    public static String export() {
        StringBuilder sb = new StringBuilder("----- Ongoing wars -----\n\n");
        for (War war : server_war.values()) {
            sb.append(war.export_long());
        }
        return sb.toString();
    }

    public String getGuild() {
        return guild;
    }

    private String export_long() {
        StringBuilder sb = new StringBuilder();
        sb.append(server).append(" / ");
        if (guild != null) {
            sb.append(guild);
        } else {
            sb.append("(Not started)");
        }
        // war duration
        int now = (int) (System.currentTimeMillis() / 1000);
        int dur = now - start_time;
        int mins = dur / 60;
        int secs = dur % 60;
        sb.append("\nDuration:");
        if (mins > 0) {
            sb.append(" ").append(mins).append(" m");
        }
        sb.append(" ").append(secs).append(" s");
        sb.append("\nPlayers: ").append(players.size()).append("/").append(unique_players.size()).append(" - ");
        for (String p : unique_players) {
            if (players.contains(p)) {
                // alive
                sb.append(p).append(", ");
            } else {
                sb.append("(").append(p).append(")").append(", ");
            }
        }
        sb.setLength(sb.length() - 2);
        return sb.append("\n\n").toString();
    }

    public void setGuild(String guild) {
        if (this.guild == null) {
            this.guild = guild;
            guild_war.put(guild, this);
            Connection conn = DatabaseHelper.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement("update war_log set guild=? where id=?")) {
                stmt.setString(1, guild);
                stmt.setInt(2, id);
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPlayer(String name) {
        System.out.printf("trying to add %s\n", name);
        started = true;
        if (guild == null) {
            try {
                guild = Functions.playerInfo(name).getJSONObject("guild").getString("name");
            } catch (NullPointerException ignored) {
                // blame wynn api
            }
        }
        if (players.add(name)) {
            System.out.printf("adding %s\n", name);
            unique_players.add(name);
            Connection conn = DatabaseHelper.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement("insert into player_war_log (war_id, uuid, ign, guild) values (?, ?, ?, ?)")){
                String uuid = Functions.ign2uuid(name);
                stmt.setInt(1, id);
                stmt.setString(2, uuid);
                stmt.setString(3, name);
                stmt.setString(4, guild);
                stmt.execute();
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
        update_discord_shit();
    }

    private void update_discord_shit() {
        // update discord shit
        StringBuilder _msg = new StringBuilder("**").append(server).append("** / ");
        if (guild == null) {
            _msg.append("???");
        } else {
            _msg.append(guild);
        }
        _msg.append(" - ");
        for (String name : unique_players) {
            if (players.contains(name)) {
                _msg.append(name);
            } else {
                _msg.append("~~").append(name).append("~~");
            }
            _msg.append(", ");
        }
        _msg.setLength(_msg.length() - 2);
        _msg.append("\nTime: ").append(sdf.format(start_time * 1000L)).append(" - ");
        if (!closed) {
            _msg.append(sdf.format(System.currentTimeMillis()));
            _msg.append(" *(Ongoing)*");
        } else {
            _msg.append(sdf.format(end_time * 1000L));

        }
        if (messages.isEmpty()) {
            // send
            Connection conn = DatabaseHelper.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement("select channel_id from discord_war_log where guild=?")) {
                stmt.setString(1, guild);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    DiscordBot.send(rs.getLong(1), _msg.toString()).whenComplete((message, throwable) -> {
                        messages.put(message.getIdLong(), message);
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // update
            for (long key : messages.keySet()) {
                Message m = messages.get(key);
                m.editMessage(_msg.toString()).submit();
            }
        }
    }

    private void removePlayer(String name) {
        // is dead
        Connection conn = DatabaseHelper.getConnection();
        players.remove(name);
        try (PreparedStatement stmt = conn.prepareStatement("update player_war_log set survived=0 where ign=? and war_id=?")) {
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
        this.end_time = end_time;
        // we wait 20 secs and poll db for a terr gain event for a maximum of 5 times
        // if that doesn't work you lost
        if (closed || !started) return;
        System.out.printf("closing war %d\n", id);
        closed = true;
        guild_war.remove(guild);
        server_war.remove(server);
        update_discord_shit();
        try (PreparedStatement stmt1 = DatabaseHelper.getConnection().prepareStatement("update war_log set end_time=? where id=?")) {
            stmt1.setInt(1, end_time);
            stmt1.setInt(2, id);
            stmt1.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (guild != null) {
            Main.getPool().schedule(() -> {
                long last_run = System.currentTimeMillis();
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
                    try (PreparedStatement stmt = conn.prepareStatement("select count(*), max(id) from terr_log where attacker=? and acquired>=? and acquired<=?")) {
                        // terr gain time < war end time
                        // terr gain time + 50 > war end time
                        stmt.setString(1, guild);
                        stmt.setInt(2, end_time - 80);
                        stmt.setInt(3, end_time); // i don't think the war ends before you acquire
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next() && rs.getInt(1) >= 1) {
                            // congrats on winning
                            System.out.printf("found a terr log for %s war %d, iteration %d\n", guild, id, i - 6);
                            try (PreparedStatement stmt1 = conn.prepareStatement("update player_war_log set won=1 where war_id=?")) {
                                stmt1.setInt(1, id);
                                stmt1.execute();
                            }
                            try (PreparedStatement stmt1 = conn.prepareStatement("update player_war_log set survived=1 where war_id=? and survived is null")) {
                                stmt1.setInt(1, id);
                                stmt1.execute();
                            }
                            try (PreparedStatement stmt1 = conn.prepareStatement("update war_log set terr_entry=? where id=?")){
                                stmt1.setInt(1, rs.getInt(2));
                                stmt1.setInt(2, id);
                                stmt1.execute();
                            }
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
                try (PreparedStatement stmt = DatabaseHelper.getConnection().prepareStatement("update player_war_log set survived=0, won=0 where war_id=?")) {
                    stmt.setInt(1, id);
                    stmt.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, 20, TimeUnit.SECONDS);
        }
    }

    public static War byGuild(String guild) {
        return guild_war.get(guild);
    }

    public static War byServer(String server) {
        return server_war.get(server);
    }
}
