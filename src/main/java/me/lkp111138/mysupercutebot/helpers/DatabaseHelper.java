package me.lkp111138.mysupercutebot.helpers;

import java.sql.*;

public class DatabaseHelper {
    private static Connection connection;
    public static void init() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("Connecting to MySQL...");
        connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s&useSSL=false&autoReconnect=true", "127.0.0.1", "mycutebot", "root", "nwza2662"));
        System.out.println("Connected to MySQL.");
    }

    public static void insert_warlog(String uuid, String ign, String guild, int start_time, String from_server, String to_server) {
        // this is called when the player just joined the war server
        try {
            PreparedStatement stmt = connection.prepareStatement("insert into player_war (mc_uuid, guild, from_server, to_server, start_time, ign) values (?, ?, ?, ?, ?, ?)");
            stmt.setString(1, uuid);
            stmt.setString(2, guild);
            stmt.setString(3, from_server);
            stmt.setString(4, to_server);
            stmt.setInt(5, start_time);
            stmt.setString(6, ign);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void war_verdict(String uuid, String war_server, Boolean survive, Boolean win) {
        // this is used when the player just left the war server
        try {
            PreparedStatement stmt = connection.prepareStatement("select id from player_war where mc_uuid=? and to_server=? order by id desc limit 1");
            stmt.setString(1, uuid);
            stmt.setString(2, war_server);
            ResultSet rs = stmt.executeQuery();
            int row = -1;
            if (rs.next()) {
                row = rs.getInt(1);
            }
            stmt = connection.prepareStatement("update player_war set survive=? where id=? and survive is null");
            if (survive != null) {
                stmt.setBoolean(1, survive);
            } else {
                stmt.setNull(1, Types.BOOLEAN);
            }
            stmt.setInt(2, row);
            stmt.execute();
            stmt = connection.prepareStatement("update player_war set win=? where id=? and win is null");
            if (win != null) {
                stmt.setBoolean(1, win);
            } else {
                stmt.setNull(1, Types.BOOLEAN);
            }
            stmt.setInt(2, row);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void terr_log(String terr, String attacker, String defender, int acquired_ts, int hold_time) {
        try {
            PreparedStatement stmt = connection.prepareStatement("insert into territory_log (terr_name, defender, attacker, acquired_ts, hold_time) values (?, ?, ?, ?, ?)");
            stmt.setString(1, terr);
            stmt.setString(2, attacker);
            stmt.setString(3, defender);
            stmt.setInt(4, acquired_ts);
            stmt.setInt(5, hold_time);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet query(String query, String[] args) {
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            for (int i = 0; i < args.length; ++i) {
                stmt.setString(i + 1, args[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
