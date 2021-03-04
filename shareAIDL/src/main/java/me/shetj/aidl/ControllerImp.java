package me.shetj.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ControllerImp implements Controller {


    public static Controller getInstance() {
        if (mInstance == null) {
            synchronized (ControllerImp.class) {
                if (mInstance == null) {
                    mInstance = new ControllerImp();
                }
            }
        }

        return mInstance;
    }

    public static void destroy(){
        mInstance = null;
    }


    private ICallBack callBack;
    private IServerInterface iServerInterface;
    private boolean isBind = false;
    private static volatile Controller mInstance;
    protected Context mContext;
    private final IClientInterface iClientInterface = new IClientInterface.Stub() {
        @Override
        public String getName() throws RemoteException {
            return mClientName;
        }

        @Override
        public void readFromServerMsg(String msg) throws RemoteException {
            if (callBack != null) {
                callBack.onCall(msg);
            }
        }
    };
    private String packageName = null;
    private String service = null;
    private String mClientName = "clientName";

    private ControllerImp() {
    }

    private int initService(String packageName, String service) {
        try {
            Log.d(ShareAIDLSDK.TAG, " sdk  initService  :packageName = " + packageName + ", service = " + service);
            Intent intent = new Intent(service);
            intent.setPackage(packageName);
            intent.setClassName(packageName, service);

            this.isBind = this.mContext.bindService(intent, this, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT
                    | Context.BIND_WAIVE_PRIORITY | Context.BIND_ABOVE_CLIENT);
            Log.d(ShareAIDLSDK.TAG, " sdk  initService  :isBind = " + isBind);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public int bindService(Context context, String clientName, String packageName, String service) {
        if (!isBind) {
            this.mContext = context;
            if (clientName != null) {
                this.mClientName = clientName;
            } else {
                this.mClientName = mContext.getPackageName();
            }
            this.packageName = packageName;
            this.service = service;
            return this.initService(packageName, service);
        } else {
            Log.d(ShareAIDLSDK.TAG, " sdk is has bindService ");
            return 0;
        }
    }

    @Override
    public void sendToServer(String msg) {
        if (!isBind) {
            Log.d(ShareAIDLSDK.TAG, " sdk u must should bindService ");
            if (packageName != null && service != null && mContext != null) {
                bindService(mContext, mClientName, packageName, service);
            }
            return;
        }
        try {
            Log.d(ShareAIDLSDK.TAG, " sdk  setMessage  ");
            if (iServerInterface != null) {
                this.iServerInterface.readFromClientMsg(msg);
            }
        } catch (RemoteException var3) {
            var3.printStackTrace();
        }
    }


    /**
     * unbind
     */
    public void unBindService() {
        if (isBind) {
            packageName = null;
            service = null;
            this.mContext.unbindService(this);
            isBind = false;
        }
    }

    @Override
    public void setCallBack(ICallBack callBack) {
        this.callBack = callBack;
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(ShareAIDLSDK.TAG, " sdk  onServiceConnected  ");
        if (service == null) {
            this.iClientInterface.asBinder().unlinkToDeath(mDeathRecipient, 0);
            this.iServerInterface = null;
        } else {
            this.iServerInterface = IServerInterface.Stub.asInterface(service);
            if (this.iServerInterface != null) {
                try {
                    this.iClientInterface.asBinder().linkToDeath(mDeathRecipient, 0);
                    this.iServerInterface.registerClientInterface(this.iClientInterface);
                } catch (RemoteException var4) {
                    var4.printStackTrace();
                }
            }
        }

    }

    public void onServiceDisconnected(ComponentName name) {
        if (this.iServerInterface != null) {
            try {
                this.iServerInterface.unregisterClientInterface(this.iClientInterface);
            } catch (RemoteException var3) {
                var3.printStackTrace();
            }
        }
        isBind = false;
        this.iServerInterface = null;
    }


    IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (iClientInterface == null)
                return;
            iClientInterface.asBinder().unlinkToDeath(mDeathRecipient, 0);
            isBind = false;
        }
    };


}
