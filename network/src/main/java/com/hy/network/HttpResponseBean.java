package com.hy.network;

import com.hy.library.utils.ObjectsUtils;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created time : 2018/7/23 16:21.
 *
 * @author HY
 */
@DatabaseTable(tableName = "response")
public class HttpResponseBean {
    /**
     * 表id，主键 ，自动生成
     */
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = "url", dataType = DataType.STRING, canBeNull = false)
    private String url;

    @DatabaseField(columnName = "params", dataType = DataType.STRING, canBeNull = true)
    private String params;

    @DatabaseField(columnName = "response", dataType = DataType.STRING, canBeNull = false)
    private String response;

    /**
     * 浏览时间戳 不能为空
     */
    @DatabaseField(columnName = "stamp", dataType = DataType.STRING, canBeNull = false)
    public String stamp;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getStamp() {
        return stamp;
    }

    public void setStamp(String stamp) {
        this.stamp = stamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpResponseBean that = (HttpResponseBean) o;
        return ObjectsUtils.equals(url, that.url) &&
                ObjectsUtils.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return ObjectsUtils.hash(url, params);
    }
}
