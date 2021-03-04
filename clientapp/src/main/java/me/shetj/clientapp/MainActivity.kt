package me.shetj.clientapp

import android.os.Bundle
import android.util.AndroidException
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import me.shetj.aidl.ShareAIDLSDK
import me.shetj.messager.ShareMessengerSDK
import me.shetj.sharedmemory.IReadBufferCallBack
import me.shetj.sharedmemory.SharedMemorySDK
import me.shetj.sharedmemory.Utils


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.TAG = "client"
        SharedMemorySDK.getInstance().bindService(this,"me.shetj.ipclibdemo","me.shetj.ipclibdemo.service.ServerClientService")
        SharedMemorySDK.getInstance().setBackBufferCallBack { bytes, i ->
            Log.d(Utils.TAG, " 客户端 读取到客户写到共享内存的大小为: " + bytes.size)
        }
        findViewById<View>(R.id.btn_send).setOnClickListener {
            SharedMemorySDK.getInstance().readFile("这是客户端的消息")
        }
        ShareAIDLSDK.getInstance().bindService(this,"me.shetj.ipclibdemo","me.shetj.ipclibdemo.service.AIDLClientService")
        ShareAIDLSDK.getInstance().setCallBack {
            Log.i(ShareAIDLSDK.TAG, "onCreate: $it")
            Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btn_send2).setOnClickListener {
            ShareAIDLSDK.getInstance().sendToServer("这是客户端2的消息：${System.currentTimeMillis()}")
        }
        ShareMessengerSDK.getInstance().bindService(this,"me.shetj.ipclibdemo","me.shetj.ipclibdemo.service.MessengerServerService")
        ShareMessengerSDK.getInstance().setCallBack {
            Log.i(ShareAIDLSDK.TAG, "onCreate: $it.data.getString(\"Toast\")")
            Toast.makeText(this,it.data.getString("Toast"),Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btn_send3).setOnClickListener {
            ShareMessengerSDK.getInstance().sendToServer("msg","这是客户端3的消息：${System.currentTimeMillis()}")
        }
    }
}