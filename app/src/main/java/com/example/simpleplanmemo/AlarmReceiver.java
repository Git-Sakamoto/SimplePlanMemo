package com.example.simpleplanmemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static com.example.simpleplanmemo.enum_pack.PrefEnum.*;
import static com.example.simpleplanmemo.enum_pack.NotificationEnum.*;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME.getString(), context.MODE_PRIVATE);
        boolean notificationFlg = pref.getBoolean(NOTIFICATION_FLG.getString(),false);

        if (intent.getAction()!=null) {
            MethodList methodList = new MethodList();
            if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && notificationFlg) {
                methodList.startAlarm(context, false);
            }else if(intent.getAction().equals(START_ALARM.getString()) && notificationFlg) {
                methodList.showNotification(context);
                methodList.startAlarm(context, true);
            }else if(intent.getAction().equals(CLOSE_NOTIFICATION.getString())) {
                int notifyId = intent.getIntExtra(NOTIFY_ID.getString(), 0);
                Log.d("id", toString().valueOf(notifyId));
                methodList.closeNotification(context, notifyId);
            }
        }
    }
}
