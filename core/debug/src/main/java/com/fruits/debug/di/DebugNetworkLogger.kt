package com.fruits.debug.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.fruits.debug.DebugLogStorage
import com.fruits.network.user.logger.NetworkEventLogger

class DebugNetworkLogger : NetworkEventLogger {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun log(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
    }
}