package com.msd.appwidget.ip

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UpdateWidgetWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.i(TAG, "From Update Widget Worker")
        updateAppWidget(appContext)
        return Result.success()
    }
}