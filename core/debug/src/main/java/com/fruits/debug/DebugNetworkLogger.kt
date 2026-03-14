package com.fruits.debug

import android.os.Build
import androidx.annotation.RequiresApi
import com.fruits.network.util.NetworkEventLogger

class DebugNetworkLogger : NetworkEventLogger {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun log(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
    }
}