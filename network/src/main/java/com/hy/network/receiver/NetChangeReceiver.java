package com.hy.network.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hy.library.utils.Logger;
import com.hy.network.entity.NetChangeEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created time : 2018/4/3 10:02.
 *
 * @author HY
 */
public class NetChangeReceiver extends BroadcastReceiver {

    public enum NetType {
        NONE,
        WIFI,
        GPRS
    }

    public static NetType netType = NetType.NONE;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Logger.i("CONNECTIVITY_ACTION");

            if (null == manager) {
                return;
            }

            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        netType = NetType.WIFI;
                        EventBus.getDefault().post(new NetChangeEvent(NetType.WIFI));
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                        netType = NetType.GPRS;
                        EventBus.getDefault().post(new NetChangeEvent(NetType.GPRS));
                    }
                } else {
                    netType = NetType.NONE;
                    EventBus.getDefault().post(new NetChangeEvent(NetType.NONE));
                }
                Logger.v("info.getTypeName()===" + activeNetwork.getTypeName());
                Logger.v("getSubtypeName()===" + activeNetwork.getSubtypeName());
                Logger.v("getState()===" + activeNetwork.getState());
                Logger.v("getDetailedState()===" + activeNetwork.getDetailedState().name());
                Logger.v("getDetailedState()===" + activeNetwork.getExtraInfo());
                Logger.v("getType()===" + activeNetwork.getType());
                Logger.v("netType========="+netType.name());
            } else {   // not connected to the internet
                netType = NetType.NONE;
                EventBus.getDefault().post(new NetChangeEvent(NetType.NONE));
            }
        }
    }
}
