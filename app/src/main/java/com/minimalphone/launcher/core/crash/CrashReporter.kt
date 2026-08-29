package com.minimalphone.launcher.core.crash

/**
 * Pluggable Crash and Telemetry Reporting contract.
 * Allows switching or attaching Firebase Crashlytics, Sentry, Bugsnag, ACRA,
 * or custom privacy-first local crash loggers without touching business logic.
 */
interface CrashReporter {
    fun initialize()
    fun logException(throwable: Throwable, message: String? = null)
    fun logBreadcrumb(category: String, message: String)
    fun setCustomKey(key: String, value: String)
}
