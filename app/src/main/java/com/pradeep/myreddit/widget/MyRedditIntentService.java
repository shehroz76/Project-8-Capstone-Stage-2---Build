package com.pradeep.myreddit.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.pradeep.myreddit.MainActivity;
import com.pradeep.myreddit.R;

public class MyRedditIntentService  extends IntentService {

    public MyRedditIntentService() {
        super("MyRedditIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //This just adds some favourite selected subreddits from the app to the home screen
        if (intent != null) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(this, MyRedditWidget.class));

            Cursor data = getContentResolver().query(
                    MainActivity.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            data.moveToFirst();
            String subr = "";
            try {
                while (data.moveToNext()) {
                    subr += data.getString(0) + ",";
                }
            } finally {
                data.close();
            }

            for (int appWidgetId : appWidgetIds) {
                Intent launchIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

                // Construct the RemoteViews object
                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.myreddit_widget);
                views.setTextViewText(R.id.appwidget_text,   subr );
                views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
}
