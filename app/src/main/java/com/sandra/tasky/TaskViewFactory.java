package com.sandra.tasky;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

import java.util.List;


public class TaskViewFactory implements RemoteViewsService.RemoteViewsFactory {
    TaskDatabase db;

    private List<SimpleTask> list;

    private Context context;
    private int appWidgetId;

    public TaskViewFactory(Context applicationContext, Intent intent) {
        this.context = applicationContext;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        db = new TaskDatabase(context);
        this.list = db.getActiveTasks();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        list = db.getActiveTasks();
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
        RemoteViews row;
        row = new RemoteViews(context.getPackageName(), R.layout.widget_list_layout);
        row.setTextViewText(R.id.tw__widget_title, list.get(position).getTitle());
        row.setTextViewText(R.id.tw_widget_status, "Status: " + (list.get(position).isCompleted() ? "Done" : "To do"));
        if (list.get(position).getDueDate() != null)
            row.setTextViewText(R.id.tw_widget_due_date, getDateText(list.get(position)));
        else
            row.setTextViewText(R.id.tw_widget_due_date, "No due date");

        //open task detail in TaskActivity
        Intent fillIntent = new Intent();
        fillIntent.putExtra(SimpleTask.TASK_BUNDLE_KEY, list.get(position));
        row.setOnClickFillInIntent(R.id.tw__widget_title, fillIntent);
        row.setOnClickFillInIntent(R.id.tw_widget_status, fillIntent);
        row.setOnClickFillInIntent(R.id.tw_widget_due_date, fillIntent);

        return row;
    }

    private String getDateText(SimpleTask task) {
        String date;
        DateTime dataDate = task.getDueDate();
        dataDate = dataDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime currentDate = new DateTime();
        currentDate = currentDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

        long diffDays = Days.daysBetween(currentDate, dataDate).getDays();
        if (diffDays == 0) {
            if (!task.isTimePresent())
                date = "today";
            else if (Hours.hoursBetween(new DateTime(), task.getDueDate()).getHours() >= 0)
                date = "today at " + task.parseTime();
            else
                date = "expired";
        }
        else if (diffDays == 1)
            date = "tomorrow" + (task.isTimePresent() ? " at " + task.parseTime() : "");
        else if (diffDays <= 10) date = "in " + diffDays + " days";
        else date = task.parseDate();
        return "Due date: " + date;
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
