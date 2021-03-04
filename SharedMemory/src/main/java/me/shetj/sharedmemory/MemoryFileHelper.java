package me.shetj.sharedmemory;

import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.SharedMemory;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;

public class MemoryFileHelper {
    public MemoryFileHelper() {
    }

    public static MemoryFile createMemoryFile(String name, int length) {
        try {
            return new MemoryFile(name, length);
        } catch (IOException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static MemoryFile openMemoryFile(ParcelFileDescriptor pfd, int length, int mode) {
        if (pfd == null) {
            throw new IllegalArgumentException("ParcelFileDescriptor 不能为空");
        } else {
            FileDescriptor fd = pfd.getFileDescriptor();
            return openMemoryFile(fd, length, mode);
        }
    }

    public static MemoryFile openMemoryFile(FileDescriptor fd, int length, int mode) {
        MemoryFile memoryFile = null;

        try {
            memoryFile = new MemoryFile("tem", length);
            if (!Utils.isMoreThanAPI27()) {
                Class<?> c = MemoryFile.class;
                Method native_mmap = null;
                Method[] ms = c.getDeclaredMethods();

                int address;
                for (address = 0; ms != null && address < ms.length; ++address) {
                    if (ms[address].getName().equals("native_mmap")) {
                        native_mmap = ms[address];
                    }
                }

                ReflectUtils.setField("android.os.MemoryFile", memoryFile, "mFD", fd);
                ReflectUtils.setField("android.os.MemoryFile", memoryFile, "mLength", length);
                if (Utils.isMoreThanAPI21()) {
                    long addresss = (Long) ReflectUtils.invokeMethod((Object) null, native_mmap, new Object[]{fd, length, mode});
                    ReflectUtils.setField("android.os.MemoryFile", memoryFile, "mAddress", addresss);
                } else {
                    address = (Integer) ReflectUtils.invokeMethod((Object) null, native_mmap, new Object[]{fd, length, mode});
                    ReflectUtils.setField("android.os.MemoryFile", memoryFile, "mAddress", address);
                }
            } else {
                SharedMemory sharedMemory = SharedMemory.create("tem", length);
                ReflectUtils.setField(SharedMemory.class.getName(), sharedMemory, "mFileDescriptor", fd);
                ReflectUtils.setField(SharedMemory.class.getName(), sharedMemory, "mSize", length);
                ReflectUtils.setField(MemoryFile.class.getName(), memoryFile, "mSharedMemory", sharedMemory);
                ReflectUtils.setField(MemoryFile.class.getName(), memoryFile, "mMapping", sharedMemory.mapReadWrite());
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return memoryFile;
    }

    public static ParcelFileDescriptor getParcelFileDescriptor(MemoryFile memoryFile) {
        if (memoryFile == null) {
            throw new IllegalArgumentException("memoryFile 不能为空");
        } else {
            FileDescriptor fd = getFileDescriptor(memoryFile);
            ParcelFileDescriptor pfd = (ParcelFileDescriptor) ReflectUtils.getInstance("android.os.ParcelFileDescriptor", new Object[]{fd});
            return pfd;
        }
    }

    public static FileDescriptor getFileDescriptor(MemoryFile memoryFile) {
        if (memoryFile == null) {
            throw new IllegalArgumentException("memoryFile 不能为空");
        } else {
            FileDescriptor fd = (FileDescriptor) ReflectUtils.invoke("android.os.MemoryFile", memoryFile, "getFileDescriptor", new Object[0]);
            return fd;
        }
    }



}
