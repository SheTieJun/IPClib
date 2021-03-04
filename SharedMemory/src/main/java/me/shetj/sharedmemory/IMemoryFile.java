package me.shetj.sharedmemory;

import android.os.ParcelFileDescriptor;

public interface IMemoryFile {
    void setReadBufferCallBack(IReadBufferCallBack var1);

    ParcelFileDescriptor getParcelFileDescriptor();

    void readShareBuffer();

    void release();
}
