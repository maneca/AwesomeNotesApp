package com.joao.awesomenotesapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.joao.awesomenotesapp.viewmodel.SyncViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted params: WorkerParameters,
    val viewModel: SyncViewModel
) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try{
            if(FirebaseAuth.getInstance().currentUser != null){
                viewModel.syncToBackend(FirebaseAuth.getInstance().currentUser!!.uid)
            }
        }catch (e: HttpException){
            Result.failure()
        }

        return Result.success()
    }
}
