package com.dynocodes.grafify.ScreenTimeUtils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
class ScreenTimeHelper(val context: Context) {

    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val calendar = Calendar.getInstance()


    fun getScreenTimeForDay(context: Context, date: Date): Long {
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis
        val stats =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        var totalTime = 0L
        for (usageStats in stats) {
            totalTime += usageStats.totalTimeInForeground
        }
        val duration = if (totalTime > 0) totalTime else 0 // convert milliseconds to minutes
        return screenTimeByEvents(startTime, endTime)
    }

    fun getScreenTimeDataForDayOfApp(date: Date): HashMap<String, Long> {
        val screenTimeDat = HashMap<String, Long>()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis

        var totalTime = 0L
        var count = 0
        screenTimeDat["other"] = 0L
        val stats = getUserInstalledAppPackageNames(context)


        for (packageName in stats) {
            val sT = screenTimeByEventsDataOfApp(startTime, endTime, packageName)
            if (sT > 1000*60*15L && count < 6) {
                totalTime += sT
                count++
                screenTimeDat.set(packageName, sT)
            } else {
                screenTimeDat.set("other", screenTimeDat["other"]!! + sT)
            }
        }
        Log.d("TAG", "getScreenTimeDataForDayOfApp: ${formatDuration(totalTime)}")
        return screenTimeDat

    }

    fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration - TimeUnit.HOURS.toMillis(hours))
        return String.format("%02d Hr %02d Mins", hours, minutes)
    }

    fun getScreenTimeForWeek(context: Context): Map<String, Float> {
        val calendar = Calendar.getInstance()
        val screenTimeMap = mutableMapOf<String, Float>()
        for (i in 1..7) {
            calendar.set(Calendar.DAY_OF_WEEK, i)
            val screenTime = getScreenTimeForDay(context, calendar.time)
            val dayOfWeekString =
                SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
            screenTimeMap[dayOfWeekString.take(3)] = convertMillisecondsToHours(screenTime)
        }
        return screenTimeMap
    }

    fun convertMillisecondsToHours(milliseconds: Long): Float {
        val hours = (milliseconds / (1000 * 60 * 60)).toFloat()
        return hours
    }


    fun getScreenTimeForMonth(context: Context, year: Int, month: Int): Map<String, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        val screenTimeMap = mutableMapOf<String, Long>()
        for (i in 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar.set(Calendar.DAY_OF_MONTH, i)
            val screenTime = getScreenTimeForDay(context, calendar.time)
            val dayOfMonthString = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            screenTimeMap[dayOfMonthString] = screenTime
        }
        return screenTimeMap
    }

    fun getUserInstalledAppPackageNames(context: Context): List<String> {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val userInstalledApps = mutableListOf<String>()

        for (packageInfo in packages) {
//            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                // This is not a system app
//                if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0) {
                    // This app was not pre-installed on the device
                    userInstalledApps.add(packageInfo.packageName)
                }
//            }
//        }

        return userInstalledApps
    }

    fun screenTimeByEvents(startTime: Long, endTime: Long): Long {
        val events = usageStatsManager.queryEvents(startTime, endTime)
        var totalTime: Long = 0
        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                val eventStartTime = event.timeStamp
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                        totalTime += event.timeStamp - eventStartTime
                        break
                    }
                }
            }
        }

        return totalTime

    }

    fun screenTimeByEventsDataOfApp(startTime: Long, endTime: Long, packageName: String): Long {
        val events = usageStatsManager.queryEvents(startTime, endTime)
        var totalTime: Long = 0
        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && event.packageName == packageName) {
                val eventStartTime = event.timeStamp
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND && event.packageName == packageName) {
                        totalTime += event.timeStamp - eventStartTime
                        break
                    }
                }
            }
        }

        return totalTime

    }


}


