package me.lkp111138.mysupercutebot.helpers;

import java.sql.*;

import static me.lkp111138.mysupercutebot.Constants.*;

public class DatabaseHelper {
    private static Connection connection;
    public static void init() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("Connecting to MySQL...");
        connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s&useSSL=false&autoReconnect=true", DB_HOST, DB_NAME, DB_USER, DB_PASS));
        System.out.println("Connected to MySQL.");
    }

    /** @return war id */
    public static int new_war_server(String war_server, int start_time) {
        try {
            PreparedStatement stmt = connection.prepareStatement("insert into war_log (server, start_time) values (?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, war_server);
            stmt.setInt(2, start_time);
            stmt.execute();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    static void terr_log(String terr, String attacker, String defender, int acquired_ts, int hold_time) {
        try {
            PreparedStatement stmt = connection.prepareStatement("insert into terr_log (terr_name, defender, attacker, acquired, hold_time) values (?, ?, ?, ?, ?)");
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

    public static Connection getConnection() {
        return connection;
    }
}
