package me.shetj.sharedmemory;

import android.content.Context;
import android.content.ServiceConnection;

public interface Controller extends ServiceConnection {
    int bindService(Context context, String packageName, String service);

    void readFile(String file);

    void setBackBufferCallBack(IReadBufferCallBack var1);

    void unBindService();
}
