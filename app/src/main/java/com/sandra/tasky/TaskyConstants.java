package com.sandra.tasky;


import com.sandra.tasky.entity.SimpleTask;

public class TaskyConstants {

    public static final String NOTIFICATION_ACTION = "com.sandra.tasky.notification.ACTION";
    public static final String NOTIFICATION_TASK_BUNDLE_KEY = "com.sandra.tasky.notification.TASK_BUNDLE_KEY";

    public static int NOTIFICATION_PI_REQUEST_CODE(SimpleTask task) {
        return TASK_BUNDLE_KEY.hashCode() + task.getId();
    }

    public static final String WIDGET_TASK_UPDATE_ACTION = "com.sandra.tasky.widget.WIDGET_TASK_UPDATE_ACTION";

    public static int WIDGET_PI_REQUEST_CODE(SimpleTask task) {
        return ALARM_EXTRA_TASK.hashCode() + task.getId();
    }

    public static final String WIDGET_MIDNIGHT_UPDATE_ACTION = "com.sandra.tasky.widget.WIDGET_MIDNIGHT_UPDATE_ACTION";
    public static final int MIDNIGHT_UPDATER_PI_REQUEST_CODE = 18310;

    public static final String WIDGET_PREF = "first_run_of_widget_1.19";
    public static final String PREFS_LAST_UPDATE = "prefs_last_update";
    public static final String PREFS_IS_WIDGET_ENABLED = "PREFS_IS_WIDGET_ENABLED";
    public static final boolean WIDGET_ENABLED = true;
    public static final boolean WIDGET_DISABLED = false;
    public static final boolean WIDGET_DEFAULT = WIDGET_DISABLED;

    public static final String ALARM_EXTRA_TASK = "com.sandra.tasky.ALARM_EXTRA_TASK";
    public static final String ALARM_EXTRA_REPEATABLE = "ALARM_EXTRA_REPEATABLE";
    public static final String ALARM_EXTRA_TIME = "ALARM_EXTRA_TIME";

    public static final int EMPTY_ID = -1;
    public static final String TASK_BUNDLE_KEY = "com.sandra.tasky.task";
    public static final String TASK_TIME_KEY = "com.sandra.tasky.TASK_TIME_KEY";

    public static final int MAX_TITLE_LENGTH = 70;
    public static final int MAX_TEXT_LENGTH = 100;

    public static final long ALL_CATEGORY_ID = -1L;
    public static final long OTHERS_CATEGORY_ID = -2L;

    public static final String SELECTED_CATEGORY_KEY = "SELECTED_CATEGORY_KEY";

    public static final String PREF_GENERAL = "PREF_GENERAL";

    public static String PREF_SORT = "PREF_SORT";
    public static final int SORT_DUE_DATE = 0;
    public static final int SORT_TITLE = 1;
    public static final int SORT_COMPLETED = 2;
    public static final int SORT_DEFAULT = SORT_DUE_DATE;

    public static final int REPEAT_ONCE = 0;
    public static final int REPEAT_DAY = 1;
    public static final int REPEAT_WEEK = 2;
    public static final int REPEAT_MONTH = 3;
    public static final int REPEAT_YEAR = 4;

}
