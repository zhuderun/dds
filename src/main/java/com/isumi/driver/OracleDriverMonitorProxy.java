package com.isumi.driver;//package com.md.driver;
//
//import java.io.Serializable;
//import java.sql.Driver;
//import java.sql.SQLException;
//import java.sql.SQLFeatureNotSupportedException;
//import java.util.logging.Logger;
//
//import com.alibaba.druid.pool.vendor.OracleExceptionSorter;
//
//public class OracleDriverMonitorProxy extends JdbcDriverMonitorProxy implements Serializable {
//    /**
//     * 
//     */
//    private static final long           serialVersionUID = 5683119253420028256L;
//    private final Driver                ORACLE_DRIVER    = new oracle.jdbc.driver.OracleDriver();
//    private final OracleExceptionSorter exceptionSorter  = new OracleExceptionSorter();
//
//    @Override
//    protected Driver getRealDriver() {
//        return ORACLE_DRIVER;
//    }
//
//    @Override
//    protected boolean isExceptionFatal(SQLException e) {
//        return exceptionSorter.isExceptionFatal(e);
//    }
//
//    @Override
//    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
//        return null;
//    }
//
//}