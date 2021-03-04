package me.shetj.sharedmemory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ControllerImp implements Controller {
    protected MemoryFileImp mMemoryFile;
    private IReadBufferCallBack mCallBack;
    private IRemoteCtrl mMyRemoteCtrl;
    private boolean isBind;
    private static volatile Controller mInstance;
    protected Context mContext;
    private IReadDataCallBack mFrameDataCallBack = new IReadDataCallBack.Stub() {
        public void canReadFileData() throws RemoteException {
            if (ControllerImp.this.mMemoryFile != null) {
                Log.d(Utils.TAG, " 服务端返回  sdk  mFrameDataCallBack  ");
                ControllerImp.this.mMemoryFile.readShareBuffer();
            }

        }
    };

    private ControllerImp() {
    }

    private int initService(String packageName, String service) {
        try {
            Log.d(Utils.TAG, " sdk  initService  :packageName = " + packageName + ", service = " + service);
            Intent intent = new Intent(service);
            intent.setPackage(packageName);
            intent.setClassName(packageName, service);
            this.isBind = this.mContext.bindService(intent, this, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT
                    | Context.BIND_WAIVE_PRIORITY | Context.BIND_ABOVE_CLIENT);
            Log.d(Utils.TAG, " sdk  initService  :isBind = " + isBind);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public int bindService(Context context, String packageName, String service) {
        if (isBind){
            unBindService();
        }
        this.mContext = context;
        return this.initService(packageName, service);
    }


    /**
     * unbind
     */
    public void unBindService() {
        if (isBind) {
            this.mContext.unbindService(this);
            isBind = false;
        }
    }


    public void readFile(String msg) {
        if (!isBind) {
            Log.d(Utils.TAG, " sdk u must should bindService ");
            return;
        }
        try {
            Log.d(Utils.TAG, " sdk  setMessage  ");
            this.mMyRemoteCtrl.readFile(msg);
        } catch (RemoteException var3) {
            var3.printStackTrace();
        }

    }

    public void setBackBufferCallBack(IReadBufferCallBack callBack) {
        this.mCallBack = callBack;
        this.mMemoryFile = MemoryFileImp.getInstance();
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(Utils.TAG, " sdk  onServiceConnected  ");
        if (service == null) {
            if (this.mMyRemoteCtrl != null) {
                try {
                    this.mMyRemoteCtrl.unlinkToDeath(this.mFrameDataCallBack.asBinder());
                } catch (RemoteException var5) {
                    var5.printStackTrace();
                }
            }

            this.mMyRemoteCtrl = null;
        } else {
            this.mMyRemoteCtrl = IRemoteCtrl.Stub.asInterface(service);
            if (this.mMyRemoteCtrl != null) {
                try {
                    this.mMyRemoteCtrl.linkToDeath(this.mFrameDataCallBack.asBinder());
                    Log.d(Utils.TAG, " sdk  onServiceConnected  setBackBufferCallBack ");
                    if (this.mCallBack != null) {
                        this.mMyRemoteCtrl.setParcelFileDescriptor(this.mMemoryFile.getParcelFileDescriptor());
                        this.mMyRemoteCtrl.registerFrameByteCallBack(this.mFrameDataCallBack);
                        this.mMemoryFile.setReadBufferCallBack(this.mCallBack);
                    } else {
                        this.mMyRemoteCtrl.unregisterFrameByteCallBack(this.mFrameDataCallBack);
                        this.mMemoryFile.release();
                    }

                    Log.d(Utils.TAG, " sdk  onServiceConnected  setBackBufferCallBack  eld ");
                } catch (RemoteException var4) {
                    var4.printStackTrace();
                }
            }
        }

    }

    public void onServiceDisconnected(ComponentName name) {
        Log.d(Utils.TAG, " sdk  onServiceDisconnected  ");
        if (this.mMyRemoteCtrl != null) {
            try {
                this.mMyRemoteCtrl.unlinkToDeath(this.mFrameDataCallBack.asBinder());
                this.mMyRemoteCtrl.unregisterFrameByteCallBack(this.mFrameDataCallBack);
            } catch (RemoteException var3) {
                var3.printStackTrace();
            }
        }

        this.mMyRemoteCtrl = null;
    }

    public static Controller getInstance() {
        if (mInstance == null) {
            Class var0 = ControllerImp.class;
            synchronized (ControllerImp.class) {
                if (mInstance == null) {
                    mInstance = new ControllerImp();
                }
            }
        }

        return mInstance;
    }
}
