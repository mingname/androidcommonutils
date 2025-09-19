package com.comm.library.utils.base;

/**
 * @author xqm
 * @date 2025/7/16 18:03
 * @description AppManager 类功能说明
 */
import android.app.Activity;

import java.util.Stack;

public class AppManager {
    private static AppManager instance;
    private Stack<Activity> activityStack = new Stack<>();

    private AppManager() {}

    public static AppManager getInstance() {
        if (instance == null) instance = new AppManager();
        return instance;
    }

    public void addActivity(Activity activity) {
        activityStack.push(activity);
    }

    public void removeActivity(Activity activity) {
        activityStack.remove(activity);
    }

    public void finishAll() {
        for (Activity activity : activityStack) {
            activity.finish();
        }
        activityStack.clear();
    }
}
