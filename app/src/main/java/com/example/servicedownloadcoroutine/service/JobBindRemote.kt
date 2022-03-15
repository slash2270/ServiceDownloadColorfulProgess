package com.example.servicedownloadcoroutine.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.servicedownloadcoroutine.util.Constants.DOWNLOAD_START
import com.example.servicedownloadcoroutine.util.Constants.DOWNLOAD_STOP
import com.example.servicedownloadcoroutine.util.Constants.JOB_BIND_REMOTE
import com.example.servicedownloadcoroutine.util.Constants.KEY_PROGRESS
import com.example.servicedownloadcoroutine.util.Constants.KEY_UNREGISTER
import com.example.servicedownloadcoroutine.util.Constants.KEY_URL
import com.example.servicedownloadcoroutine.util.Constants.LOG
import com.example.servicedownloadcoroutine.view.Set

/**
 * JobService用于执行一些需要满足特定条件但不紧急的后台任务，利用 JobScheduler 来执行这些特殊的后台任务来减少电量的消耗。
 * 开发者可以设定需要执行的任务JobService，以及任务执行的条件 JobInfo，JobScheduler 会将任务加入到队列。
 * 在特定的条件满足时 Android 系统会去批量的执行所有应用的这些任务，而非对每个应用的每个任务单独处理。这样可以减少设备被唤醒的次数
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class JobBindRemote : JobService() {

    private var toast: Toast? = null
    private lateinit var intentMain: Intent
    private val schedule: Schedule by lazy {
        Schedule()
    }
    private val set: Set by lazy {
        Set()
    }

    override fun onCreate() {
        Log.d(LOG + JOB_BIND_REMOTE, "onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(LOG + JOB_BIND_REMOTE, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * @param jobParameters 系统创建回调给我们的，不用自己创建，用于获得一些job的参数。例如job id
     * @return true，你的job为活跃状态。表明你的工作需要继续执行。一般为false。
     * 1、你可以主动调用jobFinished 去告诉系统job工作已经完成。结束当前job。
     * 2、job需要的约束条件不在满足时也会结束当前job。例如用户使用 JobInfo.Builder为job添加了setRequiresCharging(boolean)电量约束。
     * 当用户吧设备电源关闭时，系统会立刻停止改job，该job的onStopJob方法会被回调。
     * <p>
     * 3、只要你的job正在执行时，系统就会持有你app的唤醒锁。（此方法调用之前，系统就会获得唤醒锁）在您主动调用obFinished或者系统调用
     * onStopJob后这把锁才被释放。
     * 4、返回false代表您写在此方法体中的工作已经完成，系统将释放该job的锁。系统不会去调用onStopJob
     * @function job执行时调用此方法。这个方法运行在app的主线程中。你要重写这个方法，做一些自己的逻辑工作。
     */

    // 触发build的开启机制时走此方法.返回值一般为false.走完后系统不会调用onStopJob.
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Log.d(LOG + JOB_BIND_REMOTE, "onStartJob, param = $jobParameters")
        val url = jobParameters.extras.getString(KEY_URL)
        val saveProgress = jobParameters.extras.getInt(KEY_PROGRESS, 0)
        schedule.start(this, jobParameters)
        //接收从Activity发来的数据
        toast = set.toast(applicationContext, DOWNLOAD_START)
        //启动下载任务
        intentMain = set.intent(KEY_PROGRESS)
        val download = Download(this, intentMain, saveProgress)
        download.execute(url)
        return true
    }

    /**
     * 1、当系统确定停止job时会调用此方法，如果不满足build设置的相关要求时会触发此方法。
     * 例如：你设置了setRequiredNetworkType（JobInfo.NETWORK_TYPE_ANY），执行任务期间你切换了wifi。
     * 2、这个方法回调时，系统将释放app的唤醒锁。
     *
     * @param jobParameters 系统创建回调给我们的，不用自己创建，用于获得一些job的参数。例如job id
     * @return true 向JobManager 表明你要基于创建工作时的重试条件重新 schedule 这个job。false表明
     * 彻底结束了这个job。
     * 3、无论返回值如何都表明当前的job执行完毕啦。
     */

    // 当系统确定停止job时会调用此方法，如果不满足build设置的相关要求时会触发此方法.
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStopJob(jobParameters: JobParameters): Boolean {
        Log.d(LOG + JOB_BIND_REMOTE, "onStopJob")
        toast = set.toast(applicationContext, "暫停stopJob")
        //schedule.cancel(0)
        //toast = set.toast(applicationContext, DOWNLOAD_STOP)
        return true
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(LOG + JOB_BIND_REMOTE, "onUnbind")
        toast = set.toast(applicationContext, "暫停unBind")
        return super.onUnbind(intent)
    }

    override fun stopService(name: Intent?): Boolean {
        Log.d(LOG + JOB_BIND_REMOTE, "stopService")
        toast = set.toast(applicationContext, "暫停stopService")
        intentMain = set.intent(KEY_UNREGISTER)
        sendBroadcast(intentMain)
        return true
    }

    override fun onDestroy() {
        Log.d(LOG + JOB_BIND_REMOTE, "onDestroy")
        schedule.finish()
        super.onDestroy()
    }

}