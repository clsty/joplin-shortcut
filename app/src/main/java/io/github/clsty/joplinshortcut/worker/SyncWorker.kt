package io.github.clsty.joplinshortcut.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.clsty.joplinshortcut.data.api.JoplinRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: JoplinRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CONFIG_ID_KEY = "config_id"

        fun buildPeriodicRequest(configId: Long, intervalMinutes: Int): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setInputData(workDataOf(CONFIG_ID_KEY to configId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        }

        fun getWorkName(configId: Long) = "sync_config_$configId"
    }

    override suspend fun doWork(): Result {
        val configId = inputData.getLong(CONFIG_ID_KEY, -1L)
        if (configId < 0) return Result.failure()
        return try {
            repository.syncConfig(configId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
