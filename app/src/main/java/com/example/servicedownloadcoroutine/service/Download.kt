package com.example.servicedownloadcoroutine.service

import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.SystemClock
import android.util.Log
import com.example.servicedownloadcoroutine.util.Constants.KEY_PROGRESS
import com.example.servicedownloadcoroutine.util.Constants.LOG

/**
 * 下载任务
 * 我这里就是模拟下载，就没有解析传递过来的URL了。
 */

class Download(private val service: Service, private var intent: Intent, private val saveProgress: Int): AsyncTask<String?, Void?, String?>() {

    override fun doInBackground(vararg p0: String?): String? { // url

        Log.d(LOG + "url ", p0[0].toString())

        if((saveProgress) in 1..9){
            for(i in saveProgress until 11){
                putData(i)
            }
        }else{
            for (i in 1..10) {
                putData(i)
            }
        }

        return null

    }

    private fun putData(i: Int){

        //100%
        intent.putExtra(KEY_PROGRESS, 10 * i)
        //休息一秒
        SystemClock.sleep(1000)
        //发送广播
        service.sendBroadcast(intent)

    }

}