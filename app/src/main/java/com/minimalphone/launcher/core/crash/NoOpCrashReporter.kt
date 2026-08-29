package com.minimalphone.launcher.core.crash

import android.util.Log

/**
 * Default offline, privacy-first Crash Reporter.
 * Logs locally to Logcat without sending data across the network.
 */
class NoOpCrashReporter : CrashReporter {
    companion object {
        private const val TAG = "MiniMalCrashReporter"
    }

    override fun initialize() {
        Log.i(TAG, "NoOpCrashReporter initialized (Local logging active)")
    }

    override fun logException(throwable: Throwable, message: String?) {
        val detail = message?.let { "$it: " } ?: ""
        Log.e(TAG, "Caught exception: $detail${throwable.localizedMessage}", throwable)
    }

    override fun logBreadcrumb(category: String, message: String) {
        Log.d(TAG, "[$category] $message")
    }

    override fun setCustomKey(key: String, value: String) {
        Log.d(TAG, "CustomKey: $key = $value")
    }
}
