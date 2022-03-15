package com.example.servicedownloadcoroutine.service

import android.app.job.JobService
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.servicedownloadcoroutine.util.Constants.JOB_BIND_REMOTE
import com.example.servicedownloadcoroutine.util.Constants.KEY_PROGRESS
import com.example.servicedownloadcoroutine.util.Constants.KEY_URL
import com.example.servicedownloadcoroutine.util.Constants.LOG
import java.util.concurrent.TimeUnit

class Schedule {

    private var mJob: JobService? = null
    private var mJobParams: JobParameters? = null

    @RequiresApi(Build.VERSION_CODES.N)
    fun create(context: Context, currentId: Int, url:String, saveProgress: Int): JobScheduler {
        Log.d(LOG + JOB_BIND_REMOTE, "createSchedule")

        val jobScheduler = context.getSystemService(JobScheduler::class.java)

        val set = Set()
        val bundle = set.bundle(url, saveProgress)

        val componentName = ComponentName(context, JobBindRemote::class.java) //指定哪个JobService执行操作

        val builder = set.builder(bundle, jobScheduler, currentId, componentName)

        jobScheduler.schedule(builder.build())

        return jobScheduler

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun cancel(currentId: Int, jobScheduler: JobScheduler) {
        jobScheduler.cancel(currentId)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun finish() {
        mJob?.jobFinished(mJobParams, false)
    }

    fun start(job: JobService?, params: JobParameters?) {
        mJob = job
        mJobParams = params
    }

    fun enqueue() {}

    private class Set{

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bundle(url: String, saveProgress: Int): PersistableBundle{

            val bundle = PersistableBundle()
            bundle.putString(KEY_URL, url)
            bundle.putInt(KEY_PROGRESS, saveProgress)
            return bundle

        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun builder(bundle: PersistableBundle, jobScheduler: JobScheduler, currentId: Int, componentName: ComponentName): JobInfo.Builder{

            val builder = JobInfo.Builder(generateNextJobId(jobScheduler, currentId), componentName)
            //builder.setPeriodic(TimeUnit.MILLISECONDS.toMillis(20)) //定時
            builder.setPersisted(false) //持續存在
            builder.setMinimumLatency(TimeUnit.MILLISECONDS.toMillis(1)) //执行的最小延迟时间
            builder.setOverrideDeadline(TimeUnit.MILLISECONDS.toMillis(5)) //执行的最长延时时间
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) //非漫游网络状态
            builder.setBackoffCriteria(TimeUnit.MINUTES.toMillis(10), JobInfo.BACKOFF_POLICY_LINEAR) //线性重试方案
            builder.setRequiresCharging(false) //未充电状态
            builder.setRequiresDeviceIdle(false)
            builder.setExtras(bundle)

            return builder

        }

        /**
         * generate a jobId by currentId
         *
         * @param currentId current job's id
         * @return  next job's id
         **/

        @RequiresApi(Build.VERSION_CODES.N)
        private fun generateNextJobId(jobScheduler: JobScheduler, currentId: Int): Int {
            //avoid crossing the border
            var tempId: Int = if (currentId == Int.MAX_VALUE) {
                1
            } else {
                currentId + 1
            }

            // avoid new id is used, if so old id plus 1
            while (jobScheduler.getPendingJob(currentId) != null) {
                if (tempId == Int.MAX_VALUE) {
                    1
                } else {
                    tempId++
                }
            }
            return tempId
        }

    }

}