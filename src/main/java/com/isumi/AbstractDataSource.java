package com.isumi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.isumi.driver.JdbcDriverMonitorProxy;
import com.isumi.util.ResetableCountDownLatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractDataSource implements DataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataSource.class);

    public final static int DB_ERR_MAX_COUNT = 20;

    private final ResetableCountDownLatch dbErrCount = new ResetableCountDownLatch(
        DB_ERR_MAX_COUNT);

    private Semaphore activeCount;

    private final static String CON_RETRY_TIME_STR = "conRetryTime";

    private int conRetryTime;

    private final static int DEFAULT_TEST_CONNECTION_COUNT = 2;

    private String dsName;

    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    private DynamicDataSource dynamicDataSource;

    protected abstract long getMaxWait();

    protected abstract int getMaxActive();

    protected abstract ConnectionPool getTomcatConnPool();

    protected void setProperties(Properties properties) {
        String tmpValue = properties.getProperty(CON_RETRY_TIME_STR);
        if (StringUtils.isNotEmpty(tmpValue) && NumberUtils.isDigits(tmpValue)) {
            conRetryTime = Integer.parseInt(tmpValue);
            if (conRetryTime < 1) {
                conRetryTime = 0;
            }
        }

        activeCount = new Semaphore(DEFAULT_TEST_CONNECTION_COUNT, true);
    }

    protected void close() {

    }

    protected int flushMonitorData(boolean isTest) {
        return 0;
    }

    protected abstract DataSource getRealDataSource();

    protected Connection getConnectionWithLock(boolean needParams, String username, String password,
                                               String transactionIsolation) throws SQLException {
        Connection connection = null;
        int retryTime = this.conRetryTime;
        long startTime = 0, usedTime = 0, tmpUsedTime = 0, sleepTime = 0;

        boolean needActiveCountRelease = false;
        try {
            startTime = System.currentTimeMillis();
            if (dbErrCount.getCount() <= 0) {
                if (!activeCount.tryAcquire(getMaxWait(), TimeUnit.MILLISECONDS)) {
                    throw new SQLException("Get connection fail due to DB error," + "pool id:"
                                           + dynamicDataSource.getPoolId() + ",data id:"
                                           + dynamicDataSource.getDataId());
                } else {
                    if (dbErrCount.getCount() <= 0) {
                        LOGGER.warn("[Try to recover DB Connection.]");
                        needActiveCountRelease = true;
                    } else {
                        activeCount.release();
                    }
                }
            }
            tmpUsedTime = System.currentTimeMillis() - startTime;
            usedTime += tmpUsedTime;

            while (retryTime >= 0) {
                startTime = System.currentTimeMillis();
                try {
                    if (needParams) {
                        connection = getConnection(username, password);
                    } else {
                        connection = getConnection();
                    }
                    if (dbErrCount.getCount() < AbstractDataSource.DB_ERR_MAX_COUNT) {
                        LOGGER.info("[Recover DB Connection success.]");
                        dbErrCount.resetCount();
                    }

                    if (StringUtils.isNotBlank(transactionIsolation)) {
                        int leave = 0;
                        if (transactionIsolation.equals("TRANSACTION_READ_UNCOMMITTED"))
                            leave = 1;
                        else if (transactionIsolation.equals("TRANSACTION_READ_COMMITTED"))
                            leave = 2;
                        else if (transactionIsolation.equals("TRANSACTION_REPEATABLE_READ"))
                            leave = 4;
                        else if (transactionIsolation.equals("TRANSACTION_SERIALIZABLE"))
                            leave = 8;

                        if (leave != 0)
                            connection.setTransactionIsolation(leave);
                    }
                    return connection;
                } catch (SQLException e) {
                    tmpUsedTime = System.currentTimeMillis() - startTime;
                    usedTime += tmpUsedTime;

                    Throwable rootCause = e;
                    boolean isDBErr = false;
                    while (rootCause != null) {
                        if (e instanceof JdbcDriverMonitorProxy.PhyGetConnException) {
                            isDBErr = true;
                            JdbcDriverMonitorProxy.PhyGetConnException phyGetConnException = (JdbcDriverMonitorProxy.PhyGetConnException) e;
                            if (phyGetConnException.isExceptionFatal()) {
                                dbErrCount.clearCount();
                            } else {
                                dbErrCount.countDown();
                            }
                            break;
                        } else {
                            rootCause = rootCause.getCause();
                        }
                    }

                    if (usedTime < getMaxWait() && tmpUsedTime < getMaxWait()) {
                        sleepTime = usedTime + getMaxWait() >> 2 <= getMaxWait() ? getMaxWait() >> 2
                            : getMaxWait() - usedTime;
                        try {
                            Thread.sleep(sleepTime);
                            usedTime += sleepTime;
                        } catch (InterruptedException e1) {
                            throw new SQLException(
                                "Thread has been canceled,pool id:" + dynamicDataSource.getPoolId()
                                                   + ",data id:" + dynamicDataSource.getDataId(),
                                e1);
                        }
                    } else {// Get connection timeout error or other error.
                        retryTime--;
                    }
                    if (retryTime < 0) {
                        if (isDBErr) {
                            throw new SQLException("Get connection with DB error,pool id:"
                                                   + dynamicDataSource.getPoolId() + ",data id:"
                                                   + dynamicDataSource.getDataId(),
                                e);
                        } else {
                            throw new SQLException(
                                "Get connection timeout,pool id:" + dynamicDataSource.getPoolId()
                                                   + ",data id:" + dynamicDataSource.getDataId(),
                                e);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new SQLException(
                "Thread has been canceled,pool id:" + dynamicDataSource.getPoolId() + ",data id:"
                                   + dynamicDataSource.getDataId(),
                e);
        } finally {
            if (needActiveCountRelease) {
                activeCount.release();
            }
        }
        throw new SQLException("Unknow error.");
    }

    public DynamicDataSource getDynamicDataSource() {
        return dynamicDataSource;
    }

    public void setDynamicDataSource(DynamicDataSource dynamicDataSource) {
        this.dynamicDataSource = dynamicDataSource;
    }

    public boolean needChange(long waitTime) throws InterruptedException {
        return dbErrCount.await(waitTime, TimeUnit.MILLISECONDS);
    }

    protected boolean setMonitorProperties(Properties properties) {
        return false;
    }
}