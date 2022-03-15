package com.example.servicedownloadcoroutine.service

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.servicedownloadcoroutine.util.Constants.ASYNC_FORGE_LOCAL
import com.example.servicedownloadcoroutine.util.Constants.DOWNLOAD_START
import com.example.servicedownloadcoroutine.util.Constants.KEY_PROGRESS
import com.example.servicedownloadcoroutine.util.Constants.KEY_UNREGISTER
import com.example.servicedownloadcoroutine.util.Constants.KEY_URL
import com.example.servicedownloadcoroutine.util.Constants.LOG
import com.example.servicedownloadcoroutine.view.Set
import java.util.*

class AsyncTaskForgeLocal : Service() {

    private var toast: Toast? = null
    private var binder: Binder? = null
    private lateinit var intentMain: Intent
    private val generator: Random by lazy {
        Random()
    }
    private val set: Set by lazy {
        Set()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG + ASYNC_FORGE_LOCAL, "onCreate")
        binder = ServiceBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(LOG + ASYNC_FORGE_LOCAL, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(LOG + ASYNC_FORGE_LOCAL, "onBind")
        //throw UnsupportedOperationException("Not yet implemented")
        //接收从Activity发来的数据
        val url = intent.getStringExtra(KEY_URL)
        val saveProgress = intent.getIntExtra(KEY_PROGRESS, 0)
        toast = set.toast(applicationContext, DOWNLOAD_START)
        //启动下载任务
        intentMain = set.intent(KEY_PROGRESS)
        sendBroadcast(intentMain)
        val download = Download(this, intentMain, saveProgress)
        download.execute(url)
        return binder
    }

    override fun unbindService(conn: ServiceConnection) {
        Log.d(LOG + ASYNC_FORGE_LOCAL, "unBind")
        intentMain = set.intent(KEY_UNREGISTER)
        sendBroadcast(intentMain)
        super.unbindService(conn)
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }

    override fun onDestroy() {
        Log.d(LOG + ASYNC_FORGE_LOCAL, "onDestroy")
        binder = null
        toast?.cancel()
        intentMain = set.intent(KEY_UNREGISTER)
        sendBroadcast(intentMain)
        super.onDestroy()
    }

    //在Service中暴露出去的方法，供client调用
    fun getRandomNumber(): Int {
        return generator.nextInt()
    }

    open class ServiceBinder: Binder() {
        fun getService():AsyncTaskForgeLocal {
            return AsyncTaskForgeLocal()
        }
    }

}