## 使用Messenger通信

#### 服务端
```
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
```
```
        <service android:name=".service.MessengerServerService"
            android:exported="true"
            android:process=":messenger"/>

```

#### 客户端
```
  ShareMessengerSDK.getInstance().bindService(this,"me.shetj.testPlugin","me.shetj.testPlugin.service.MessengerServerService")
        ShareMessengerSDK.getInstance().setCallBack {
            Log.i(ShareAIDLSDK.TAG, "onCreate: $it.data.getString(\"Toast\")")
            Toast.makeText(this,it.data.getString("Toast"),Toast.LENGTH_SHORT).show()
        }
        
  findViewById<View>(R.id.btn_send3).setOnClickListener {
        ShareMessengerSDK.getInstance().sendToServer("msg","这是客户端3的消息：${System.currentTimeMillis()}")
   }
```
