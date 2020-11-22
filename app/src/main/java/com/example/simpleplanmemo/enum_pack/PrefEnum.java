package com.example.simpleplanmemo.enum_pack;

public enum PrefEnum {
    FILE_NAME("pref"),
    LIST_RELOAD("list_reload"),
    HOUR_OF_DAY("hour_of_day"),
    MINUTE("minute"),
    NOTIFICATION_FLG("notification_flg"),
    FIRST_START("first_start");

    private final String text;

    PrefEnum(String text){
        this.text = text;
    }

    public String getString(){
        return this.text;
    }

    public enum Num {
        TIME_NULL(99);
        private final int id;
        Num(int id){
            this.id = id;
        }
        public int getInt(){
            return this.id;
        }
    }
}
