package com.hy.network;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created time : 2018/7/23 16:18.
 *
 * @author HY
 */
public class HttpResponseCacheUtils extends OrmLiteSqliteOpenHelper {
    /**
     * 数据库名字
     */
    private static final String DB_NAME = "http-response.db";
    /**
     * 数据库版本
     */
    private static final int DB_VERSION = 1;


    private static HttpResponseCacheUtils instance;

    /**
     * 获取单例
     *
     * @param context 上下文对象
     * @return 数据库帮助类实例
     */
    public synchronized static HttpResponseCacheUtils getInstance(Context context) {
        context = context.getApplicationContext();
        if (instance == null) {
            instance = new HttpResponseCacheUtils(context);
        }
        return instance;
    }

    private HttpResponseCacheUtils(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private Dao<HttpResponseBean, Integer> cacheDao;

    public Dao<HttpResponseBean, Integer> getCacheDao() throws SQLException {
        if (cacheDao == null) {
            cacheDao = getDao(HttpResponseBean.class);
        }
        return cacheDao;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, HttpResponseBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() {
        super.close();
        cacheDao = null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        try {
            TableUtils.dropTable(connectionSource, HttpResponseBean.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
