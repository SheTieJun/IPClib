// IMyRemoteCtrl1.aidl
package me.shetj.sharedmemory;

// Declare any non-default types here with import statements
import me.shetj.sharedmemory.IReadDataCallBack;
interface IRemoteCtrl {

        void setParcelFileDescriptor(in ParcelFileDescriptor pfd);
        void registerFrameByteCallBack(IReadDataCallBack frameDataCallBack);
        void unregisterFrameByteCallBack(IReadDataCallBack frameDataCallBack);
        void readFile(String msg);
        void linkToDeath(IBinder binder);
        void unlinkToDeath(IBinder binder);
}