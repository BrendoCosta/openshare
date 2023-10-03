package io.github.brendocosta.openshare.util

import io.github.brendocosta.openshare.app.App

import android.util.Log;
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.stackTraceToString

object Log {

    @JvmStatic
    private val appName: String = App.getInstance().getAppName()
    
    @JvmStatic
    private val logText: MutableList<String> = mutableListOf<String>()

    @JvmStatic
    public fun debug(message: String) {

        debug(message, null)
    }

    @JvmStatic
    public fun debug(message: String, exception: Throwable?) {

        val logType: String = "DEBUG"
        val logTag: String = getLogTag(logType)
        val logTagWithDate: String = getLogTagWithDate(logType)

        if (exception == null) {

            logText.add("$logTagWithDate :: $message")
            android.util.Log.d(logTag, message)

        } else {

            logText.add("$logTagWithDate :: $message :: ${exception.stackTraceToString()}")
            android.util.Log.d(logTag, message, exception)

        }

    }

    @JvmStatic
    private fun getLogDateFormatted(): String {

        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

    }

    @JvmStatic
    private fun getLogTag(type: String): String {

        return "$appName [$type]"

    }

    @JvmStatic
    private fun getLogTagWithDate(type: String): String {

        return "$appName [$type] ${getLogDateFormatted()}"

    }

    @JvmStatic
    public fun getFullLogText(): String {

        return logText.joinToString("\n")

    }

}