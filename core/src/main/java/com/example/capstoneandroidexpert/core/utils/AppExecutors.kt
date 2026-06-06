package com.example.capstoneandroidexpert.core.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors(
    private val diskIO: Executor = Executors.newSingleThreadExecutor(),
    private val networkIO: Executor = Executors.newFixedThreadPool(3),
    private val mainThread: Executor = MainThreadExecutor()
) {
    fun diskIO(): Executor = diskIO
    fun networkIO(): Executor = networkIO
    fun mainThread(): Executor = mainThread

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
