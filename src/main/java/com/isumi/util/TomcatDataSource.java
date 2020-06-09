package com.isumi.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import com.isumi.AbstractDataSource;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;


public class TomcatDataSource extends AbstractDataSource {


    private DataSource tomcatDataSource = null;

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return tomcatDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        tomcatDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        tomcatDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return tomcatDataSource.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return tomcatDataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return tomcatDataSource.isWrapperFor(iface);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return tomcatDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return tomcatDataSource.getConnection(username, password);
    }

    @Override
    protected long getMaxWait() {
        return tomcatDataSource.getPoolProperties().getMaxWait();
    }

    @Override
    protected void setProperties(Properties properties) {
        if (tomcatDataSource != null) {
            tomcatDataSource.close();
        }
        tomcatDataSource = new DataSource(DataSourceFactory.parsePoolProperties(properties));
        super.setProperties(properties);
    }

    @Override
    public void close() {
        super.close();
        if (tomcatDataSource != null) {
            tomcatDataSource.close();
        }
    }

    @Override
    protected int getMaxActive() {
        return tomcatDataSource.getMaxActive();
    }

    @Override
    protected javax.sql.DataSource getRealDataSource() {
        return tomcatDataSource;
    }

    @Override
    protected ConnectionPool getTomcatConnPool() {
        try {
            return tomcatDataSource.createPool();
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
