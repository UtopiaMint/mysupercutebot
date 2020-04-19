package me.lkp111138.mysupercutebot.db;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class PooledConnection implements Connection {
    private final ConnectionPool pool;
    private final Connection delegate;

    private boolean locked = true;

    PooledConnection(ConnectionPool pool, Connection delegate) {
        this.pool = pool;
        this.delegate = delegate;
    }

    @Override
    public Statement createStatement() throws SQLException {
        if (locked) {
            return delegate.createStatement();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (locked) {
            return delegate.prepareStatement(sql);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        if (locked) {
            return delegate.prepareCall(sql);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        if (locked) {
            return delegate.nativeSQL(sql);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (locked) {
            delegate.setAutoCommit(autoCommit);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        if (locked) {
            return delegate.getAutoCommit();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void commit() throws SQLException {
        if (locked) {
            delegate.commit();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (locked) {
            delegate.rollback();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void close() throws SQLException {
        locked = false;
        pool.release(delegate);
    }

    @Override
    public boolean isClosed() throws SQLException {
        if (locked) {
            return delegate.isClosed();
        } else {
            return true;
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        if (locked) {
            return delegate.getMetaData();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        if (locked) {
            delegate.setReadOnly(readOnly);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        if (locked) {
            return delegate.isReadOnly();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        if (locked) {
            delegate.setCatalog(catalog);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public String getCatalog() throws SQLException {
        if (locked) {
            return delegate.getCatalog();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        if (locked) {
            delegate.setTransactionIsolation(level);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        if (locked) {
            return delegate.getTransactionIsolation();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        if (locked) {
            return delegate.getWarnings();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void clearWarnings() throws SQLException {
        if (locked) {
            delegate.clearWarnings();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        if (locked) {
            return delegate.createStatement(resultSetType, resultSetConcurrency);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (locked) {
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (locked) {
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        if (locked) {
            return delegate.getTypeMap();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        if (locked) {
            delegate.setTypeMap(map);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        if (locked) {
            delegate.setHoldability(holdability);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public int getHoldability() throws SQLException {
        if (locked) {
            return delegate.getHoldability();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        if (locked) {
            return delegate.setSavepoint();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        if (locked) {
            return delegate.setSavepoint(name);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        if (locked) {
            delegate.rollback(savepoint);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        if (locked) {
            delegate.releaseSavepoint(savepoint);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (locked) {
            return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (locked) {
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (locked) {
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        if (locked) {
            return delegate.prepareStatement(sql, autoGeneratedKeys);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        if (locked) {
            return delegate.prepareStatement(sql, columnIndexes);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        if (locked) {
            return delegate.prepareStatement(sql, columnNames);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Clob createClob() throws SQLException {
        if (locked) {
            return delegate.createClob();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Blob createBlob() throws SQLException {
        if (locked) {
            return delegate.createBlob();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public NClob createNClob() throws SQLException {
        if (locked) {
            return delegate.createNClob();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        if (locked) {
            return delegate.createSQLXML();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        if (locked) {
            return delegate.isValid(timeout);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        if (locked) {
            delegate.setClientInfo(name, value);
        } else {
            SQLClientInfoException ex = new SQLClientInfoException();
            ex.initCause(new SQLException("Connection already released from pool"));
            throw ex;
        }
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        if (locked) {
            delegate.setClientInfo(properties);
        } else {
            SQLClientInfoException ex = new SQLClientInfoException();
            ex.initCause(new SQLException("Connection already released from pool"));
            throw ex;
        }
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        if (locked) {
            return delegate.getClientInfo(name);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        if (locked) {
            return delegate.getClientInfo();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        if (locked) {
            return delegate.createArrayOf(typeName, elements);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        if (locked) {
            return delegate.createStruct(typeName, attributes);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        if (locked) {
            delegate.setSchema(schema);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public String getSchema() throws SQLException {
        if (locked) {
            return delegate.getSchema();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        if (locked) {
            delegate.abort(executor);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        if (locked) {
            delegate.setNetworkTimeout(executor, milliseconds);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        if (locked) {
            return delegate.getNetworkTimeout();
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (locked) {
            return delegate.unwrap(iface);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (locked) {
            return delegate.isWrapperFor(iface);
        } else {
            throw new SQLException("Connection already released from pool");
        }
    }
}