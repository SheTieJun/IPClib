package me.shetj.sharedmemory;

import android.content.Context;

public class SharedMemorySDK {

    private static volatile SharedMemorySDK mInstance;
    private Controller mController;



    private SharedMemorySDK() {
    }

    public static SharedMemorySDK getInstance() {
        if (mInstance == null) {
            Class var0 = SharedMemorySDK.class;
            synchronized(SharedMemorySDK.class) {
                if (mInstance == null) {
                    mInstance = new SharedMemorySDK();
                }
            }
        }

        return mInstance;
    }


    public int bindService(Context context,String packageName,String service) {
        this.mController = ControllerImp.getInstance();
        return this.mController.bindService(context,packageName,service);
    }


    public void unBindService(){
        this.mController.unBindService();
    }

    public void readFile(String msg) {
        if (this.mController != null) {
            this.mController.readFile(msg);
        }

    }

    public void setBackBufferCallBack(IReadBufferCallBack callBack) {
        if (this.mController != null) {
            this.mController.setBackBufferCallBack(callBack);
        }

    }
}