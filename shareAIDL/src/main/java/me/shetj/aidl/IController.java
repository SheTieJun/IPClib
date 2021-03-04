package me.shetj.aidl;


import android.content.Context;

interface IController {
    int bindService(Context context,String clientName, String packageName, String service);

    void sendToServer(String file);

    void unBindService();

    void setCallBack(ICallBack callBack);
}
