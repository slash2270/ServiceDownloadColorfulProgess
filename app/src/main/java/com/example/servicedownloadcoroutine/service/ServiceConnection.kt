package com.example.servicedownloadcoroutine.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.servicedownloadcoroutine.util.Constants

class ServiceConnection(var asyncTaskForgeLocal: AsyncTaskForgeLocal?): ServiceConnection {

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        asyncTaskForgeLocal = (service as AsyncTaskForgeLocal.ServiceBinder).getService()
        val number = asyncTaskForgeLocal?.getRandomNumber()
        Log.d(Constants.LOG + Constants.ASYNC_FORGE_LOCAL, "onServiceConnected: mService = $service, random number = $number")
    }

    override fun onServiceDisconnected(name: ComponentName) {
        asyncTaskForgeLocal = null
    }

}