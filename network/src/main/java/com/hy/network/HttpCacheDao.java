package com.hy.network;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created time : 2018/7/23 17:01.
 *
 * @author HY
 */
public class HttpCacheDao {
    private Dao<HttpResponseBean, Integer> cacheDao;

    private HttpResponseCacheUtils dbHelp;

    public HttpCacheDao(Context context) {
        dbHelp = HttpResponseCacheUtils.getInstance(context);
        try {
            cacheDao = dbHelp.getCacheDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(HttpResponseBean responseBean) {
        try {
            int id = queryId(responseBean);
            if (id == -1) {
                cacheDao.create(responseBean);
            } else {
                responseBean.setId(id);
                cacheDao.update(responseBean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(HttpResponseBean responseBean) {
        try {
            cacheDao.delete(responseBean);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(HttpResponseBean responseBean) {
        try {
            int id = queryId(responseBean);
            if (id == -1) {
                cacheDao.create(responseBean);
            } else {
                responseBean.setId(id);
                cacheDao.update(responseBean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clear(List<HttpResponseBean> productHistories) {
        try {
            cacheDao.delete(productHistories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int queryId(HttpResponseBean responseBean) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("url", responseBean.getUrl());
        map.put("params", responseBean.getParams());

        List<HttpResponseBean> histories = cacheDao.queryForFieldValues(map);
        int id = -1;
        if (!histories.isEmpty()) {
            id = histories.get(0).id;
        }
        return id;
    }

    public HttpResponseBean query(String url, String params) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("params", params);

        List<HttpResponseBean> responseBeans = null;
        try {
            responseBeans = cacheDao.queryForFieldValues(map);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (null == responseBeans || responseBeans.isEmpty()) {
            return null;
        }
        return responseBeans.get(0);
    }

    public HttpResponseBean queryForId(int id) {
        HttpResponseBean responseBean = null;
        try {
            responseBean = cacheDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return responseBean;
    }
}
