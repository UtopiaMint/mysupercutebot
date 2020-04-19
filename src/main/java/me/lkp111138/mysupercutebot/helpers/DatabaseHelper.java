package me.lkp111138.mysupercutebot.helpers;

import me.lkp111138.mysupercutebot.api.discord.DiscordBot;
import me.lkp111138.mysupercutebot.db.ConnectionPool;

import java.sql.*;
import java.text.SimpleDateFormat;

import static me.lkp111138.mysupercutebot.Constants.*;

public class DatabaseHelper {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static ConnectionPool pool;

    public static void init() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("Connecting to MySQL...");
        pool = new ConnectionPool(String.format("jdbc:mysql://%s/%s?user=%s&password=%s&useSSL=false&autoReconnect=true", DB_HOST, DB_NAME, DB_USER, DB_PASS), 3, 10);
        System.out.println("Connected to MySQL.");
    }

    /** @return war id */
    public static int new_war_server(String war_server, int start_time) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("insert into war_log (server, start_time) values (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, war_server);
                stmt.setInt(2, start_time);
                stmt.execute();
                ResultSet rs = stmt.getGeneratedKeys();
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    static void terr_log(String terr, String defender, String attacker, int acquired_ts, int hold_time) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("insert into terr_log (terr_name, defender, attacker, acquired, hold_time) values (?, ?, ?, ?, ?)")) {
                stmt.setString(1, terr);
                stmt.setString(2, defender);
                stmt.setString(3, attacker);
                stmt.setInt(4, acquired_ts);
                stmt.setInt(5, hold_time);
                stmt.execute();
            }
            if (DiscordBot.isReady()) {
                try (PreparedStatement stmt = connection.prepareStatement("select channel_id from discord_terr_log where guild in (?, ?) or guild is null")) {
                    stmt.setString(1, defender);
                    stmt.setString(2, attacker);
                    ResultSet rs = stmt.executeQuery();
                    StringBuilder msg_sb = new StringBuilder("*");
                    int atker_terrs = Looper.terr_count(attacker);
                    int defer_terrs = Looper.terr_count(defender);
                    msg_sb.append(terr).append("*: ").append(defender).append(" [").append(defer_terrs).append("] -> **");
                    msg_sb.append(attacker).append("** [").append(atker_terrs).append("]\nHold Time: ");
                    msg_sb.append(sdf.format(1000L * (acquired_ts - hold_time))).append(" - ").append(sdf.format(1000L * acquired_ts));
                    msg_sb.append("\nHeld For ");
                    int _d = hold_time / 86400;
                    int _h = (hold_time / 3600) % 24;
                    int _m = (hold_time / 60) % 60;
                    int _s = hold_time % 60;
                    if (hold_time > 86399) {
                        msg_sb.append(_d).append("d ");
                    }
                    if (hold_time > 3599) {
                        msg_sb.append(_h).append("h ");
                    }
                    if (hold_time > 59) {
                        msg_sb.append(_m).append("m ");
                    }
                    msg_sb.append(_s).append("s ");
                    while (rs.next()) {
                        DiscordBot.send(rs.getLong(1), msg_sb.toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return pool.getConnection();
    }
}
