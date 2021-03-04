## AIDL 逻辑

1. 可以传递aidl
2. 服务必须 android:exported="true"


### 服务端
```
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
```

### 客户端
```
   ShareAIDLSDK.getInstance().bindService(this,"me.shetj.testPlugin","me.shetj.testPlugin.service.AIDLClientService")
        ShareAIDLSDK.getInstance().setCallBack {
            Log.i(ShareAIDLSDK.TAG, "onCreate: $it")
            Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btn_send2).setOnClickListener {
            ShareAIDLSDK.getInstance().sendToServer("这是客户端2的消息：${System.currentTimeMillis()}")
        }
```
