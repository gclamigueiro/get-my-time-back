package com.example.getmytimeback.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.getmytimeback.data.BlockedSites

/**
 * Worker to reset the consumed time for all blocked sites.
 */
class ResetTimeWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        BlockedSites.blockedSites.values.forEach {
            it.consumedTime = 0
        }
        return Result.success()
    }
}