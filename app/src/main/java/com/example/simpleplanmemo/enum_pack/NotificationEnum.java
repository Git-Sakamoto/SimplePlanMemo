package com.example.simpleplanmemo.enum_pack;

public enum NotificationEnum {
    CLOSE_NOTIFICATION("com.example.CloseNotification"),
    START_ALARM("com.example.StartAlarm"),
    NOTIFY_ID("NotifyId");

    private final String text;
    NotificationEnum(String text){
        this.text = text;
    }
    public String getString(){
        return this.text;
    }
}
