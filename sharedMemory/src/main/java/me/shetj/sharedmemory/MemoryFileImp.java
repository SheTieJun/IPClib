package me.shetj.sharedmemory;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.MemoryFile;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;

public class MemoryFileImp implements IMemoryFile {
    private IReadBufferCallBack mReadBufferCallBack;
    private Handler mHander;
    private static MemoryFileImp sMemoryFile;
    protected final String TAG = Utils.TAG;
    protected MemoryFile mMemoryFile;
    private byte[] isCanRead = new byte[1];
    private byte[] mBuffer = null;
    private byte[] FIleBuffer = null;
    private HandlerThread mHandlerThread;
    private boolean needRead;
    private ParcelFileDescriptor parcelFileDescriptor;
    protected boolean mIsStream;
    private int mReadNum;

    public static MemoryFileImp getInstance() {
        if (sMemoryFile == null) {
            Class var0 = MemoryFileImp.class;
            synchronized(MemoryFileImp.class) {
                if (sMemoryFile == null) {
                    sMemoryFile = new MemoryFileImp();
                }
            }
        }

        return sMemoryFile;
    }

    protected MemoryFileImp() {
        try {
            this.mMemoryFile = this.getMemoryFile();
            this.mBuffer = this.getBuffer();
            this.FIleBuffer = this.getFIleBuffer();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        this.mHandlerThread = this.getHandlerThread();
        this.mHandlerThread.start();
        this.mHander = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg != null) {
                    switch(msg.what) {
                    case 0:
                        MemoryFileImp.this.readShareBufferMsg();
                        break;
                    case 1:
                        MemoryFileImp.this.readShareBufferCallback();
                    }

                }
            }
        };
    }


    protected HandlerThread getHandlerThread() {
        return new HandlerThread("video_thread");
    }

    protected byte[] getFIleBuffer() {
        return new byte[13729413];
    }

    protected byte[] getBuffer() {
        return new byte[13729413];
    }

    protected MemoryFile getMemoryFile() {
        try {
            return new MemoryFile(this.TAG, 13729414);
        } catch (IOException var2) {
            var2.printStackTrace();
            Log.w(this.TAG, "create MemoryFile Fail");
            return null;
        }
    }

    public void readShareBuffer() {
        if (this.mHander != null) {
            this.mHander.sendEmptyMessage(1);
        }

    }

    private void readShareBufferCallback() {
        try {
            if (this.mMemoryFile != null) {
                int count = this.mMemoryFile.readBytes(this.isCanRead, 0, 0, 1);
                if (this.mReadNum++ % 255 == 0) {
                    Log.d(this.TAG, " isCanRead = " + this.isCanRead[0] + " mReadNum:" + this.mReadNum);
                }

                if (this.isCanRead[0] == 1) {
                    this.mMemoryFile.readBytes(this.mBuffer, 1, 0, this.mBuffer.length);
                    System.arraycopy(this.mBuffer, 0, this.FIleBuffer, 0, this.mBuffer.length);
                    this.processData();
                    this.isCanRead[0] = 0;
                    this.mMemoryFile.writeBytes(this.isCanRead, 0, 0, 1);
                }
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    protected void readShareBufferMsg() {
        try {
            if (this.mMemoryFile != null) {
                int count = this.mMemoryFile.readBytes(this.isCanRead, 0, 0, 1);
                if (this.mReadNum++ % 5 == 0) {
                    Log.d(this.TAG, " isCanRead = " + this.isCanRead[0]);
                }

                if (this.isCanRead[0] == 1) {
                    this.mMemoryFile.readBytes(this.mBuffer, 1, 0, this.mBuffer.length);
                    System.arraycopy(this.mBuffer, 0, this.FIleBuffer, 0, this.mBuffer.length);
                    this.processData();
                    this.isCanRead[0] = 0;
                    this.mMemoryFile.writeBytes(this.isCanRead, 0, 0, 1);
                }
            }

            if (this.mHander != null && this.needRead) {
                this.mHander.removeCallbacksAndMessages(0);
                this.mHander.sendEmptyMessageDelayed(0, 65L);
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    private void processData() {
        if (this.mReadBufferCallBack != null) {
            this.mReadBufferCallBack.onReadBuffer(this.FIleBuffer, this.FIleBuffer.length);
        }

    }

    public void startStream() {
        if (!this.mIsStream) {
            this.mIsStream = true;
            this.mHander.removeCallbacksAndMessages(0);
            this.needRead = true;
        }
    }

    public void stopStream() {
        this.needRead = false;
        this.mIsStream = false;
        this.mHander.removeCallbacksAndMessages(0);
    }

    public void release() {
        this.mHander.removeCallbacksAndMessages(0);
        this.mHandlerThread.quit();
        this.mHander = null;
        if (this.mMemoryFile != null) {
            this.mMemoryFile.close();
            this.mMemoryFile = null;
        }

    }

    public ParcelFileDescriptor getParcelFileDescriptor() {
        if (this.parcelFileDescriptor == null) {
            this.parcelFileDescriptor = MemoryFileHelper.getParcelFileDescriptor(this.mMemoryFile);
        }

        return this.parcelFileDescriptor;
    }

    public void setReadBufferCallBack(IReadBufferCallBack callBack) {
        this.mReadBufferCallBack = callBack;
        if (callBack == null) {
            this.stopStream();
        } else {
            this.startStream();
        }

    }
}
