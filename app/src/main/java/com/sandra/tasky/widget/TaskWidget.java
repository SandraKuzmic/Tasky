package com.sandra.tasky.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sandra.tasky.R;
import com.sandra.tasky.activities.TaskActivity;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Implementation of App Widget functionality.
 */
public class TaskWidget extends AppWidgetProvider {
    public static String KEY_TASK_WIDGET = "key_widget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        JodaTimeAndroid.init(context);
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent svcIntent = new Intent(context, TaskWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.task_widget);

            widget.setRemoteAdapter(R.id.widget_list, svcIntent);

            //open activity from widget
            Intent clickIntent = new Intent(context, TaskActivity.class);
            PendingIntent clickPI = PendingIntent.getActivity(context, 0, clickIntent, 0);
            widget.setPendingIntentTemplate(R.id.widget_list, clickPI);

            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}