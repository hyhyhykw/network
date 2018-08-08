package com.hy.network.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hy.library.utils.Logger;
import com.hy.network.entity.NetChangeEvent;
import com.hy.network.receiver.NetChangeReceiver;

import org.greenrobot.eventbus.EventBus;

/**
 * Created time : 2018/4/12 9:11.
 *
 * @author HY
 */
public class NetService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private NetChangeReceiver mReceiver;

    private NetChangeCallback mNetChangeCallback;
    private ConnectivityManager manager;

    @Override
    public void onCreate() {
        Logger.d("服务创建");
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Logger.i("CONNECTIVITY_ACTION");
            mNetChangeCallback = new NetChangeCallback();
            if (null != manager) {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                NetworkRequest request = builder.build();
                manager.registerNetworkCallback(request, mNetChangeCallback);
            }
        } else {
            mReceiver = new NetChangeReceiver();
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mReceiver, mFilter);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (null != manager && null != mNetChangeCallback) {
                manager.unregisterNetworkCallback(mNetChangeCallback);
            }
        } else {
            if (null != mReceiver) unregisterReceiver(mReceiver);
        }

    }

    /**
     * Created time : 2018/4/14 15:22.
     *
     * @author HY
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public class NetChangeCallback extends ConnectivityManager.NetworkCallback {

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            if (null == manager) {
                return;
            }

            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        NetChangeReceiver.netType = NetChangeReceiver.NetType.WIFI;
                        EventBus.getDefault().post(new NetChangeEvent(NetChangeReceiver.NetType.WIFI));
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                        NetChangeReceiver.netType = NetChangeReceiver.NetType.GPRS;
                        EventBus.getDefault().post(new NetChangeEvent(NetChangeReceiver.NetType.GPRS));
                    }
                } else {
                    NetChangeReceiver.netType = NetChangeReceiver.NetType.NONE;
                    EventBus.getDefault().post(new NetChangeEvent(NetChangeReceiver.NetType.NONE));
                }
                Logger.v("info.getTypeName()===" + activeNetwork.getTypeName());
                Logger.v("getSubtypeName()===" + activeNetwork.getSubtypeName());
                Logger.v("getState()===" + activeNetwork.getState());
                Logger.v("getDetailedState()===" + activeNetwork.getDetailedState().name());
                Logger.v("getDetailedState()===" + activeNetwork.getExtraInfo());
                Logger.v("getType()===" + activeNetwork.getType());
                Logger.v("netType=========" + NetChangeReceiver.netType.name());
            } else {   // not connected to the internet
                NetChangeReceiver.netType = NetChangeReceiver.NetType.NONE;
                EventBus.getDefault().post(new NetChangeEvent(NetChangeReceiver.NetType.NONE));
            }

        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            NetChangeReceiver.netType = NetChangeReceiver.NetType.NONE;
            EventBus.getDefault().post(new NetChangeEvent(NetChangeReceiver.NetType.NONE));
        }
    }
}
