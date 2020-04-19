package me.lkp111138.mysupercutebot.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {
    private final String connString;
    private final int minConnections;
    private final int maxConnections;
    private final List<Connection> connectionList = new ArrayList<>();

    private int utilization;

    public ConnectionPool(String connString, int minConnections, int maxConnections) throws SQLException {
        this.connString = connString;
        this.minConnections = minConnections;
        this.maxConnections = maxConnections;

        for (int i = 0; i < minConnections; i++) {
            connectionList.add(createConnection());
        }
        utilization = minConnections;
    }

    public Connection getConnection() throws SQLException {
        if (connectionList.isEmpty()) {
            if (utilization < maxConnections) {
                connectionList.add(createConnection());
            } else {
                throw new SQLException("Pool exhausted");
            }
        }
        Connection conn = connectionList.get(0);
        connectionList.remove(0);
        if (conn.isValid(2)) {
            return new PooledConnection(this, conn);
        } else {
            return new PooledConnection(this, createConnection());
        }
    }

    void release(Connection conn) {
        connectionList.add(conn);
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(connString);
    }

    @Override
    public String toString() {
        return "ConnectionPool{" +
                "connString='" + connString + '\'' +
                ", minConnections=" + minConnections +
                ", maxConnections=" + maxConnections +
                ", utilization=" + utilization +
                '}';
    }
}