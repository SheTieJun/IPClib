package me.shetj.ipclibdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import me.shetj.aidl.IClientInterface;
import me.shetj.aidl.IServerInterface;
import me.shetj.aidl.ShareAIDLSDK;

public class AIDLClientService extends Service {
    private ServerImpl sever = new ServerImpl();
    private Map<String,IClientInterface> mClient = new HashMap<>();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sever.asBinder();
    }


    public class ServerImpl extends IServerInterface.Stub {

        @Override
        public void readFromClientMsg(String msg) throws RemoteException {
            Log.i(ShareAIDLSDK.TAG, "readFromClientMsg: "+msg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mClient.forEach((s, iClientInterface) -> {
                    try {
                        iClientInterface.readFromServerMsg("这是来时服务的消息："+System.currentTimeMillis());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        @Override
        public void registerClientInterface(IClientInterface client) throws RemoteException {
            mClient.put(client.getName(),client);
        }

        @Override
        public void unregisterClientInterface(IClientInterface client) throws RemoteException {
            mClient.remove(client);
        }
    }

}