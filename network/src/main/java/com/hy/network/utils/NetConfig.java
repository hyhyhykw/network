package com.hy.network.utils;

/**
 * Created time : 2018/8/8 11:36.
 *
 * @author HY
 */
public class NetConfig {
    private static boolean isLog = true;

    public static boolean isIsLog() {
        return isLog;
    }

    public static void setIsLog(boolean isLog) {
        NetConfig.isLog = isLog;
    }
}
