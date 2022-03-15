package com.example.servicedownloadcoroutine.view

import android.app.ProgressDialog
import android.app.job.JobScheduler
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.servicedownloadcoroutine.R
import com.example.servicedownloadcoroutine.service.Schedule
import com.example.servicedownloadcoroutine.util.Constants
import com.example.servicedownloadcoroutine.util.Constants.DOWNLOAD_FINISH
import com.example.servicedownloadcoroutine.util.Constants.DOWNLOAD_ING
import com.example.servicedownloadcoroutine.util.Constants.DOWNLOAD_STOP
import com.example.servicedownloadcoroutine.util.Constants.DOWNLOAD_WAITING
import com.example.servicedownloadcoroutine.util.Constants.KEY_PROGRESS
import com.example.servicedownloadcoroutine.util.Constants.KEY_UNREGISTER
import com.example.servicedownloadcoroutine.util.Constants.KEY_URL

class Set {

    fun intent(action: String): Intent{
        val intent = Intent()
        intent.action = action
        return intent
    }

    fun toast(context: Context, string: String): Toast{

        var toast = Toast(context)
        toast.cancel()
        toast = Toast.makeText(context, string, Toast.LENGTH_SHORT)
        toast.show()

        return toast
    }

    fun text(str1: String, str2: String, button: Button){

        val stringBuffer = StringBuffer()
        stringBuffer.append(str1).append(str2)
        button.text = stringBuffer

    }

    private lateinit var jobScheduler: JobScheduler

    @RequiresApi(Build.VERSION_CODES.M)
    fun click(listBtn: List<Button>, listIntent: List<Intent>, activity: MainActivity, broadcastReceiver: BroadcastReceiver, progressDialog: ProgressDialog, sP: SharedPreferences, serviceConnection: ServiceConnection) {

        listIntent.forEach {

            //携带额外数据
            it.putExtra(KEY_URL,"http://www.baidu.com/xxx.txt")

        }

        var title = ""
        val schedule by lazy {  Schedule() }

        listBtn.forEach {

            it.setOnClickListener { view->

                when(view.id){

                    R.id.btnStartAsync -> {
                        title = DOWNLOAD_ING
                        bindService(activity, broadcastReceiver, listIntent[0], sP, serviceConnection)
                    }
                    R.id.btnStopAsync ->{
                        title = DOWNLOAD_WAITING
                        unBindService(activity, progressDialog, sP, serviceConnection, listBtn[0])
                    }
                    R.id.btnStartJob -> {
                        title = DOWNLOAD_ING
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            jobScheduler = schedule.create(activity,0, "http://www.baidu.com/xxx.txt", sP.getInt(KEY_PROGRESS, 0)/10)
                            startService(activity, broadcastReceiver, listIntent[1], sP)
                        }
                    }
                    R.id.btnStopJob -> {
                        title = DOWNLOAD_WAITING
                        stopService(activity, listIntent[1], progressDialog, sP, listBtn[1])
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            schedule.cancel(0, jobScheduler)
                            stopService(activity, listIntent[1], progressDialog, sP, listBtn[1])
                        }
                    }
                    R.id.btnStartHandler -> {
                        title = DOWNLOAD_ING
                        startService(activity, broadcastReceiver, listIntent[2], sP)
                    }
                    R.id.btnStopHandler -> {
                        title = DOWNLOAD_WAITING
                        stopService(activity, listIntent[2], progressDialog, sP, listBtn[2])
                    }

                }

                progress(activity, progressDialog, title)

            }

        }

    }

    private fun register(activity: MainActivity, broadcastReceiver: BroadcastReceiver){

        //注册广播接收者
        val intentFilter = IntentFilter()
        intentFilter.addAction(KEY_PROGRESS)
        intentFilter.addAction(KEY_UNREGISTER)
        activity.registerReceiver(broadcastReceiver, intentFilter)

    }

    private fun startService(activity: MainActivity, broadcastReceiver: BroadcastReceiver, intent: Intent, sP: SharedPreferences){

        register(activity, broadcastReceiver)
        //发送数据给service
        intent.putExtra(KEY_PROGRESS, sP.getInt(KEY_PROGRESS, 0)/10)
        activity.startService(intent)

    }

    private fun stopService(activity: MainActivity, intent: Intent, progressDialog: ProgressDialog, sP: SharedPreferences, btnStart: Button){

        btnStart.isEnabled = false
        //发送数据给service
        activity.stopService(intent)
        progressDialog.progress = sP.getInt(KEY_PROGRESS, 0)

    }

    private fun bindService(activity: MainActivity, broadcastReceiver: BroadcastReceiver, intent: Intent, sP: SharedPreferences, serviceConnection: ServiceConnection){

        register(activity, broadcastReceiver)
        intent.putExtra(KEY_PROGRESS, sP.getInt(KEY_PROGRESS, 0)/10)
        //发送数据给service
        activity.bindService(intent, serviceConnection, BIND_AUTO_CREATE) // bind_auto_create 內建常數

    }

    private fun unBindService(activity: MainActivity, progressDialog: ProgressDialog, sP: SharedPreferences, serviceConnection: ServiceConnection, btnStart: Button) {

        btnStart.isEnabled = false
        //发送数据给service
        activity.unbindService(serviceConnection)
        progressDialog.progress = sP.getInt(KEY_PROGRESS, 0)

    }

    private fun progress(activity: MainActivity, progressDialog: ProgressDialog, title: String){

        //创建下载进度条
        //设置最大值
        progressDialog.max = 100
        //设置为水平
        progressDialog.setTitle(title)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setIndeterminateDrawable(activity.resources.getDrawable(R.drawable.progress_layer_list))
        progressDialog.setProgressDrawable(activity.resources.getDrawable(R.drawable.progress_layer_list))
        //显示进度条
        progressDialog.show()

    }

    fun receive(activity: MainActivity, intent: Intent?, editor: SharedPreferences.Editor, progressDialog: ProgressDialog){

        Log.d(Constants.LOG + Constants.MAIN_ACTIVITY, "action = ${intent?.action}")

        if(intent?.action?.isNotEmpty() == true){

            when(intent.action){

                KEY_PROGRESS -> {

                    //更新进度
                    val progress = intent.getIntExtra(KEY_PROGRESS, 0)
                    Log.d(Constants.LOG + Constants.MAIN_ACTIVITY, "progress = $progress")

                    if (progress == 100 or 0) {

                        toast(activity, DOWNLOAD_FINISH)
                        progressDialog.progress = 0
                        //关闭进度条
                        progressDialog.dismiss()

                    } else {
                        progressDialog.progress = progress
                    }
                    editor.putInt(KEY_PROGRESS, progress).apply()

                }

                KEY_UNREGISTER -> {
                    activity.unRegister()
                    toast(activity, DOWNLOAD_STOP)
                }

            }

        }

    }

}