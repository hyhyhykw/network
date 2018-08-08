package com.hy.network;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.hy.library.utils.Logger;
import com.hy.library.utils.ToastWrapper;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created time : 2018/7/23 16:35.
 *
 * @author HY
 */
@SuppressWarnings("unchecked")
public abstract class UICallBack<T> {

    final Handler mHandler = new Handler(Looper.getMainLooper());

    public void onFailedInUI() {
        ToastWrapper.show("网络错误");
    }

    protected void onResponse(String json) {
        if (isNeedParse()) {
            mHandler.post(() -> {
                try {
                    final T t = (T) new Gson().fromJson(json, getSuperClassGenericType());
                    if (null == t) {
                        Logger.d("数据解析错误");
                        ToastWrapper.show("未获取到数据");
                        onFailedInUI();
                        return;
                    }

                    onResponse(t);
                } catch (final Exception e) {
                    //ToastWrapper.show("服务器异常");
                    onFailedInUI();
                    Logger.e(e.getMessage(), e);
                }
            });
        }
    }

    protected  boolean isSave(){
        return true;
    }
    protected boolean isNeedParse() {
        return true;
    }

    void onFailure(@NonNull IOException e) {
        mHandler.post(() -> {
            Logger.d(e.getMessage(), e);

            onFailedInUI();
        });
    }

    protected abstract void onResponse(@NonNull T t);

    private Class<Object> getSuperClassGenericType() {

        //返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
        Type genType = getClass().getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        //返回表示此类型实际类型参数的 Type 对象的数组。
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (0 >= params.length) {
            return Object.class;
        }
        if (!(params[0] instanceof Class)) {
            return Object.class;
        }

        return (Class<Object>) params[0];
    }
}
