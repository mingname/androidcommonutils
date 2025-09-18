package com.comm.library.utils;

/**
 * @author xqm
 * @date 2025/7/16 18:37
 * @description LogUtils 日志工具类
 */

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtils {
    private static final String DEFAULT_TAG = "AppLog";
    private static boolean isDebug = true;
    private static boolean writeToFile = true;

    private static final String LOG_FILE_PREFIX = "log_"; // 文件名前缀
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA);

    // 记录应用上下文，初始化时调用
    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void setWriteToFile(boolean write) {
        writeToFile = write;
    }

    public static void d(String msg) {
        d(DEFAULT_TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug && msg != null) {
            Log.d(tag, msg);
            writeLogToFile("D", tag, msg);
        }
    }

    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (isDebug && msg != null) {
            Log.i(tag, msg);
            writeLogToFile("I", tag, msg);
        }
    }

    public static void w(String msg) {
        w(DEFAULT_TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (isDebug && msg != null) {
            Log.w(tag, msg);
            writeLogToFile("W", tag, msg);
        }
    }

    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug && msg != null) {
            Log.e(tag, msg);
            writeLogToFile("E", tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug && msg != null) {
            Log.e(tag, msg, tr);
            writeLogToFile("E", tag, msg + "\n" + Log.getStackTraceString(tr));
        }
    }

    private static void writeLogToFile(String level, String tag, String message) {
        //存储到sp start
//        Type type = new TypeToken<List<String>>() {}.getType();
//        List<String> savedList = SPUtils.getObject("log", type);
//        if (savedList == null) {
//            savedList = new ArrayList<>();
//        }
//        // 格式化当前时间
//        String timess = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
//
//        // 拼接时间 + 日志内容
//        String logEntry = "[" + timess + "] " + message;
//
//        savedList.add(logEntry);
//        SPUtils.putObject("log",savedList);

        //存储到sp end
        if (!writeToFile || sContext == null) return;

        try {
            String today = dateFormat.format(new Date());
            String time = timeFormat.format(new Date());

            // 使用应用私有外部存储目录： /Android/data/你的包名/files/AppLogs/
            String OUTPATH = Environment.getExternalStorageDirectory() + "/htnova/logs/";
            File logDir = new File(OUTPATH);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File logFile = new File(logDir, LOG_FILE_PREFIX + today + ".txt");
            FileWriter writer = new FileWriter(logFile, true);
            writer.write(String.format("[%s][%s][%s]: %s\n", time, level, tag, message));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("LogUtils", "writeLogToFile failed: " + e.getMessage());
        }
    }
}


