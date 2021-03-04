package me.shetj.messager

import android.content.Context

internal interface IController {
    fun bindService(context: Context, packageName: String, service: String): Int
    fun sendToServer(key: String, msg: String)
    fun unBindService()
    fun setCallBack(callBack: ICallBack?)
}