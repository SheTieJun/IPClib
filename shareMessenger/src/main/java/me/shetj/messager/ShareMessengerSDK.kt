package me.shetj.messager

import android.content.Context
import android.util.Log


/**
 *
 */
open class ShareMessengerSDK {

    companion object {
        @JvmField
        val TAG = "ShareAIDLSDK"
        const val MESSAGE_FROM_CLIENT = 6001
        const val MESSAFE_FROM_SERVER = 6002

        @Volatile
        private var mInstance: ShareMessengerSDK? = null

        @JvmStatic
        fun getInstance(): ShareMessengerSDK {
            return mInstance ?: synchronized(ShareMessengerSDK::class.java) {
                mInstance ?: ShareMessengerSDK().also {
                    mInstance = it
                }
            }
        }
    }

    private var mController: IController? = null


    fun bindService(
        context: Context,
        packageName: String,
        service: String
    ): Int {
        mController = ControllerImp.instance
        return mController!!.bindService(context, packageName, service)
    }

    fun sendToServer(key: String, msg: String) {
        mController?.sendToServer(key, msg)
            ?: Log.i(TAG, "error : sendToServer: u should bindService first")
    }

    fun unBindService() {
        mController?.unBindService()
    }

    fun setCallBack(callBack: ICallBack?) {
        mController?.setCallBack(callBack)
            ?: Log.i(TAG, "error : setCallBack: u should bindService first")
    }

    fun destroy() {
        mInstance?.unBindService()
        ControllerImp.destroy()
        mInstance = null
    }

}