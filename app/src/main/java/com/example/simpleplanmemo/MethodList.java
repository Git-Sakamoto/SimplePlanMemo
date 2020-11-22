package com.example.simpleplanmemo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.simpleplanmemo.enum_pack.PrefEnum.*;
import static com.example.simpleplanmemo.enum_pack.NotificationEnum.*;

public class MethodList {
    static final int REQUEST_CODE = 1;
    static final int TIME_NULL = 99;

    void showNotification(Context context){
        final String CHANNEL_ID = context.getString(R.string.app_name);
        final String GROUP_KEY = context.getString(R.string.app_name);
        final int SUMMARY_ID = 0;
        final String alarmTitle = context.getString(R.string.app_name);

        NotificationCompat.Builder notification;

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String today = df.format(new Date());

        DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.openDB();
        Cursor c= dbAdapter.selectPlanAlarm(today);
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME.getString(), context.MODE_PRIVATE);
        boolean notificationFlg = pref.getBoolean(NOTIFICATION_FLG.getString(),false);
        //通知する予定が見つかり、通知設定が有効の場合
        if(c.getCount() > 0 && notificationFlg){
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancelAll();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, alarmTitle, NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableVibration(true);
                channel.canShowBadge();
                channel.enableLights(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                channel.setShowBadge(true);
                notificationManager.createNotificationChannel(channel);
            }

            if(c.moveToFirst()){
                do{
                    int id = c.getInt(0);
                    String title = c.getString(1);
                    String contents = c.getString(2);
                    String deadlineDate = c.getString(4);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                        notification = new NotificationCompat.Builder(context,CHANNEL_ID);
                    }else{
                        notification = new NotificationCompat.Builder(context);
                    }

                    notification.setContentTitle(deadlineDate+"："+title);

                    if(!contents.equals("未登録")){
                        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                        String[] messageArray = contents.split("\n");
                        for (int i=0; i < messageArray.length; i++) {
                            inboxStyle.addLine(messageArray[i]);
                        }
                        notification.setStyle(inboxStyle);
                        //notification.setContentText(contents);
                    }

                    notification.setSmallIcon(android.R.drawable.ic_lock_idle_alarm);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        notification.setGroup(GROUP_KEY);
                    }

                    notification.setOngoing(true);

                    notification.setShowWhen(false);

                    Intent closeIntent = new Intent(context,AlarmReceiver.class);
                    closeIntent.setAction(CLOSE_NOTIFICATION.getString());
                    closeIntent.putExtra(NOTIFY_ID.getString(),id);
                    Log.d("id", toString().valueOf(id));
                    Log.d("タイトル",title);
                    PendingIntent pendingIntentClose = PendingIntent.getBroadcast(context, id, closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    notification.addAction(android.R.drawable.ic_menu_delete, "予定を終了", pendingIntentClose);
                    notificationManager.notify(id, notification.build());
                }while(c.moveToNext());
            }

            //API26以上の時に通知をグループ表示
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Notification notificationSummary = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setShowWhen(false)
                        .setGroup(GROUP_KEY)
                        .setGroupSummary(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .setSummaryText("今日の予定"))
                        .build();
                notificationManager.notify(SUMMARY_ID, notificationSummary);
            }
        }
        c.close();
    }

    void startAlarm(Context context,boolean setNextDayAlarm){
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME.getString(), context.MODE_PRIVATE);
        if(pref.getBoolean(NOTIFICATION_FLG.getString(),false)){
            int hourOfDay = pref.getInt(HOUR_OF_DAY.getString(),TIME_NULL);
            int minute = pref.getInt(MINUTE.getString(),TIME_NULL);
            if(!(hourOfDay == TIME_NULL | minute == TIME_NULL)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                if(setNextDayAlarm) {
                    calendar.add(Calendar.DATE, 1);
                }

                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.setAction(START_ALARM.getString());

                PendingIntent pending = PendingIntent.getBroadcast(
                    context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

                if (am != null) {
                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
                    }else{
                        am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
                    }
                    //Toast.makeText(context, "通知を有効にしました", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    void cancelAlarm(Context context){
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context,REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if(isWorkingPending(context)){pending.cancel();}
        AlarmManager am = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        if(am!=null){am.cancel(pending);}
        Toast.makeText(context,"通知を無効にしました",Toast.LENGTH_SHORT).show();
    }

    boolean isWorkingPending(Context context){
        Intent intent = new Intent(context, AlarmReceiver.class);
        boolean isWorking = (PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE) != null);
        return isWorking;
    }

    void closeNotification(Context context,int notifyId){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifyId);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
            Log.d("残り通知数", String.valueOf(activeNotifications.length));
            if (activeNotifications.length == 1) {
                notificationManager.cancel(0);
            }
        }
        DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.openDB();
        dbAdapter.updateStatus(String.valueOf(notifyId));
        dbAdapter.closeDB();
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME.getString(), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(LIST_RELOAD.getString(),true);
        editor.commit();
    }

    public static String getYobi(String ymd){
        try{
            String yobi[] = {"(日)","(月)","(火)","(水)","(木)","(金)","(土)"};

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            sdf.setLenient(false);
            sdf.parse(ymd);

            int y = Integer.parseInt(ymd.substring(0,4));
            int m = Integer.parseInt(ymd.substring(5,7))-1;
            int d = Integer.parseInt(ymd.substring(8,10));

            Calendar cal = Calendar.getInstance();
            cal.set(y, m, d);

            return yobi[cal.get(Calendar.DAY_OF_WEEK)-1];

        }catch(Exception ex){
            return null;
        }
    }

}