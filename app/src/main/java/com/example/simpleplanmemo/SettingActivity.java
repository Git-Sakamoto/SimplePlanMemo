package com.example.simpleplanmemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import static com.example.simpleplanmemo.enum_pack.PrefEnum.*;

public class SettingActivity extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    MethodList methodList;
    int hourOfDay,minute;
    Boolean notificationFlg;
    private static final int TIME_NULL = Num.TIME_NULL.getInt();
    TextView textNotificationSetting,textNotificationTime;
    Button buttonNotificationSetting,buttonTimeChange,buttonTest1,buttonTest2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        methodList = new MethodList();

        textNotificationSetting = findViewById(R.id.text_notification_setting);
        textNotificationTime = findViewById(R.id.text_notification_time);
        buttonNotificationSetting = findViewById(R.id.button_notification_setting);
        buttonTimeChange = findViewById(R.id.button_time_change);

        pref = getSharedPreferences(FILE_NAME.getString(), MODE_PRIVATE);
        editor = pref.edit();

        notificationFlg = pref.getBoolean(NOTIFICATION_FLG.getString(),false);
        notificationTextSetting(notificationFlg);

        hourOfDay = pref.getInt(HOUR_OF_DAY.getString(),TIME_NULL);
        minute = pref.getInt(MINUTE.getString(),TIME_NULL);
        textNotificationTime.setText(hourOfDay+"時"+minute+"分");

        buttonNotificationSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(notificationFlg){
                    notificationFlg = false;
                    editor.putBoolean(NOTIFICATION_FLG.getString(),notificationFlg);
                    editor.commit();
                    methodList.cancelAlarm(SettingActivity.this);
                }else{
                    notificationFlg = true;
                    editor.putBoolean(NOTIFICATION_FLG.getString(),notificationFlg);
                    editor.commit();
                    methodList.startAlarm(SettingActivity.this,false);
                }
                notificationTextSetting(notificationFlg);
            }
        });

        buttonTimeChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hh, int mm) {
                        hourOfDay = hh;
                        minute= mm;
                        editor.putInt(HOUR_OF_DAY.getString(),hourOfDay);
                        editor.putInt(MINUTE.getString(),minute);
                        editor.commit();
                        textNotificationTime.setText(hourOfDay+"時"+minute+"分");
                        if(notificationFlg){
                            methodList.startAlarm(SettingActivity.this,false);
                        }
                    }
                };
                TimePickerDialog dialog = new TimePickerDialog(SettingActivity.this,onTimeSetListener, hourOfDay, minute, true);
                dialog.show();
            }
        });

        buttonTest1 = findViewById(R.id.button_test1);
        buttonTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methodList.showNotification(SettingActivity.this);
            }
        });

        buttonTest2 = findViewById(R.id.button_test2);
        buttonTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(methodList.isWorkingPending(SettingActivity.this)){
                    Toast.makeText(SettingActivity.this,"アラームは登録されています",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SettingActivity.this,"アラームは登録されていません",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void notificationTextSetting(Boolean notificationFlg){
        if(notificationFlg){
            textNotificationSetting.setText("有効");
            buttonNotificationSetting.setText("通知を無効にする");
        }else if(notificationFlg == false){
            textNotificationSetting.setText("無効");
            buttonNotificationSetting.setText("通知を有効にする");
        }
    }
}