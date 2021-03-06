package com.sandra.tasky.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import java.util.List;


public class TaskViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private TaskDatabase db;

    private List<SimpleTask> list;

    private Context context;
    private int appWidgetId;

    public TaskViewFactory(Context applicationContext, Intent intent) {
        this.context = applicationContext;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        this.db = new TaskDatabase(context);
        this.list = getTasksInWidget();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        this.list = getTasksInWidget();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_item);
        SimpleTask task = list.get(position);
        row.setTextViewText(R.id.tw__widget_title, task.getTitle());
        if (task.getDueDate() != null) {
            setDueDateVisible(row);
            row.setTextViewText(R.id.tw_widget_due_date, getDateText(task));
        } else {
            setDueDateGone(row);
        }

        //open task detail in TaskActivity
        Intent fillIntent = new Intent();
        fillIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, task);
        row.setOnClickFillInIntent(R.id.widget_layout_parent, fillIntent);
//        row.setOnClickFillInIntent(R.id.tw__widget_title, fillIntent);
//        row.setOnClickFillInIntent(R.id.tw_widget_status, fillIntent);
//        row.setOnClickFillInIntent(R.id.tw_widget_due_date, fillIntent);

        return row;
    }

    private void setDueDateVisible(RemoteViews row) {
        row.setViewVisibility(R.id.tw_widget_due_date, View.VISIBLE);
        row.setViewVisibility(R.id.widget_space, View.GONE);
    }

    private void setDueDateGone(RemoteViews row) {
        row.setViewVisibility(R.id.tw_widget_due_date, View.GONE);
        row.setViewVisibility(R.id.widget_space, View.VISIBLE);
    }

    private String getDateText(SimpleTask task) {
        String date;
        DateTime dataDate = task.getDueDate();
        dataDate = dataDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime currentDate = new DateTime();
        currentDate = currentDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

        long diffDays = Days.daysBetween(currentDate, dataDate).getDays();
        if (diffDays == 0) {
            if (!task.isTimePresent()) {
                date = context.getString(R.string.today);
            } else {
                boolean isExpired = Hours.hoursBetween(new DateTime(), task.getDueDate()).getHours() < 0
                        || Minutes.minutesBetween(new DateTime(), task.getDueDate()).getMinutes() < 0;
                date = (isExpired ? context.getString(R.string.expired) : context.getString(R.string.today))
                        + " " + context.getString(R.string.at) + " " + task.parseTime();
            }
        } else if (diffDays < 0) {
            date = context.getString(R.string.expired);
        } else if (diffDays == 1) {
            date = context.getString(R.string.tomorrow) + (task.isTimePresent() ? " " + context.getString(R.string.at) + " " + task.parseTime() : "");
        } else if (diffDays <= 10) {
            date = context.getString(R.string.in) + " " + diffDays + " " + context.getString(R.string.days);
        } else {
            date = task.parseDate();
        }
        return context.getString(R.string.due_date) + ": " + date;
    }

    private List<SimpleTask> getTasksInWidget() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return db.getTasksInWidget(
                preferences.getBoolean(context.getString(R.string.pref_show_expired_key), context.getResources().getBoolean(R.bool.pref_show_expired_default)),
                preferences.getString(context.getString(R.string.pref_time_span_key), context.getString(R.string.pref_time_span_default)));
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
