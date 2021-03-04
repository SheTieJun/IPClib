package me.shetj.ipclibdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import me.shetj.messager.ShareMessengerSDK;

public class MessengerServerService extends Service {
    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case ShareMessengerSDK.MESSAGE_FROM_CLIENT:
                    Log.d(ShareMessengerSDK.TAG,"client msg");
                    //获取客户端传递过来的Messenger，通过这个Messenger回传消息给客户端
                    Messenger client = message.replyTo;
                    //当然，回传消息还是要通过message
                    Message msg = Message.obtain(null, ShareMessengerSDK.MESSAFE_FROM_SERVER);
                    Bundle bundle = new Bundle();
                    bundle.putString("Toast", "hello client, I have received your message!");
                    msg.setData(bundle);
                    try {
                        client.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(message);
                    break;
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

}