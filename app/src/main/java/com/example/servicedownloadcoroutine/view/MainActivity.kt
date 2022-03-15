package com.example.servicedownloadcoroutine.view

import android.app.ProgressDialog
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.servicedownloadcoroutine.base.BaseActivity
import com.example.servicedownloadcoroutine.databinding.ActivityMainBinding
import com.example.servicedownloadcoroutine.service.AsyncTaskForgeLocal
import com.example.servicedownloadcoroutine.service.HandlerRemote
import com.example.servicedownloadcoroutine.service.JobBindRemote
import com.example.servicedownloadcoroutine.util.Constants.ASYNC
import com.example.servicedownloadcoroutine.util.Constants.HANDLER
import com.example.servicedownloadcoroutine.util.Constants.JOB
import com.example.servicedownloadcoroutine.util.Constants.LOG
import com.example.servicedownloadcoroutine.util.Constants.MAIN_ACTIVITY
import com.example.servicedownloadcoroutine.util.Constants.START
import com.example.servicedownloadcoroutine.util.Constants.STOP
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var listBtn: List<Button>

    private val serviceAsync: AsyncTaskForgeLocal by lazy {
        AsyncTaskForgeLocal()
    }

    private val serviceConnection: ServiceConnection by lazy {
        com.example.servicedownloadcoroutine.service.ServiceConnection(serviceAsync)
    }

    private val sP:SharedPreferences by lazy {
        this.getSharedPreferences("Main", MODE_PRIVATE)
    }

    private val editor:SharedPreferences.Editor by lazy {
        sP.edit()
    }

    private val scopeWork = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Job()
    }

    private val set: Set by lazy {
        Set()
    }

    /**
     * 接收Service发送的进度数据
     * 動態註冊廣播
     */

    private val broadcastReceiver:BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

            set.receive(this@MainActivity, p1, editor, progressDialog)

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(LOG + MAIN_ACTIVITY, "onCreate")

        init()

        button()

    }

    override fun onResume() {
        super.onResume()

        Log.d(LOG + MAIN_ACTIVITY, "onResume")

    }

    override fun init() {

        progressDialog = ProgressDialog(this)

        set.text(START, ASYNC, binding.btnStartAsync)
        set.text(STOP, ASYNC, binding.btnStopAsync)
        set.text(START, JOB, binding.btnStartJob)
        set.text(STOP, JOB, binding.btnStopJob)
        set.text(START, HANDLER, binding.btnStartHandler)
        set.text(STOP, HANDLER, binding.btnStopHandler)
    }

    fun unRegister(){
        unregisterReceiver(broadcastReceiver)
        for(i in 0 until 3){
            listBtn[i].isEnabled = true
        }
    }

    private fun unBind(){
        unbindService(serviceConnection)
    }

    /**
     * 按钮事件
     */

    private fun button() {

        val intentAsync = Intent(this, serviceAsync.javaClass)
        val intentJobBindRemote = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent(this, JobBindRemote().javaClass)
        } else {
            Intent(this, serviceAsync.javaClass)
        }
        val intentHandlerRemote= Intent(this, HandlerRemote().javaClass)

        val listIntent = listOf(intentAsync, intentJobBindRemote, intentHandlerRemote)
        listBtn = listOf(binding.btnStartAsync, binding.btnStartJob, binding.btnStartHandler, binding.btnStopAsync, binding.btnStopJob, binding.btnStopHandler)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            set.click(listBtn, listIntent, this, broadcastReceiver, progressDialog, sP, serviceConnection)
        }

    }

    override fun onDestroy() {
        unRegister()
        unBind()
        editor.clear().apply()
        scopeWork.cancel()
        super.onDestroy()
    }

}