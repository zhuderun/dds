package com.isumi.util;

/**
 * @author Hikin Yao
 * @version 1.0
 */
public class DynamicDbContext {
    /**
     * 主库类型
     */
    private static String MASTER_DB_TYPE = "master_db";
    /**
     * 备库
     */
    private static String SLAVE_DB_TYPE = "slave_db";

    private static final ThreadLocal<String> bsDBContext = new ThreadLocal<String>();

    public static boolean switchToMasterDB() {
        bsDBContext.set(MASTER_DB_TYPE);
        return true;
    }

    public static boolean switchToSlaveDB() {
        bsDBContext.set(SLAVE_DB_TYPE);
        return true;
    }

    public static boolean isMasterDB() {
        boolean result = false;
        String dbType = bsDBContext.get();
        if (dbType != null && dbType.equals(MASTER_DB_TYPE)) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public static void reset() {
        bsDBContext.remove();
    }
}
