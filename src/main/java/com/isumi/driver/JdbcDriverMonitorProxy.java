package com.isumi.driver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

public abstract class JdbcDriverMonitorProxy implements Driver {

    public class PhyGetConnException extends SQLException {
        
        private static final long serialVersionUID = 5682606873854732047L;
        
        private boolean exceptionFatal = false;

        public boolean isExceptionFatal() {
            return exceptionFatal;
        }

        public void setExceptionFatal(boolean exceptionFatal) {
            this.exceptionFatal = exceptionFatal;
        }

        public PhyGetConnException() {
            super();
        }

        public PhyGetConnException(String reason, String sqlState, int vendorCode, Throwable cause) {
            super(reason, sqlState, vendorCode, cause);
        }

        public PhyGetConnException(String reason, String SQLState, int vendorCode) {
            super(reason, SQLState, vendorCode);
        }

        public PhyGetConnException(String reason, String sqlState, Throwable cause) {
            super(reason, sqlState, cause);
        }

        public PhyGetConnException(String reason, String SQLState) {
            super(reason, SQLState);
        }

        public PhyGetConnException(String reason, Throwable cause) {
            super(reason, cause);
        }

        public PhyGetConnException(String reason) {
            super(reason);
        }

        public PhyGetConnException(Throwable cause) {
            super(cause);
        }

    }


    public JdbcDriverMonitorProxy() {
        super();
    }


    protected abstract Driver getRealDriver();

    protected abstract boolean isExceptionFatal(SQLException e);

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        try {
            return getRealDriver().connect(url, info);
        } catch (SQLException e) {
            PhyGetConnException phyGetConnException = new PhyGetConnException(e.getMessage(), e.getSQLState(),
                    e.getErrorCode(), e);
            if (isExceptionFatal(e)) {
                phyGetConnException.setExceptionFatal(true);
            }
            throw phyGetConnException;
        }
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return getRealDriver().acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return getRealDriver().getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return getRealDriver().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return getRealDriver().getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return getRealDriver().jdbcCompliant();
    }

}
