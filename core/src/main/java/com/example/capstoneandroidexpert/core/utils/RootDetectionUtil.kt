package com.example.capstoneandroidexpert.core.utils

import android.content.Context
import com.scottyab.rootbeer.RootBeer

object RootDetectionUtil {
    fun isDeviceRooted(context: Context): Boolean {
        return RootBeer(context).isRooted
    }
}
