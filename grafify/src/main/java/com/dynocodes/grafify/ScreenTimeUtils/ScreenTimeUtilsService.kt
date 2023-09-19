package com.dynocodes.beoffline.ScreenTimeUtils


import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat

class ScreenTimeUsageAccessService : AccessibilityService() {



    override fun onServiceConnected() {
        super.onServiceConnected()
        // enable usage access for your app
        val info = AccessibilityServiceInfo()
        info.packageNames = arrayOf(packageName)
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // do nothing
    }

    override fun onInterrupt() {
        // do nothing
    }
}
