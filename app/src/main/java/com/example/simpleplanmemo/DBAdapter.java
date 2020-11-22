package com.example.simpleplanmemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DBAdapter {
    private static final String DB_NAME = "plan.db";
    private static final String DB_TABLE = "plan";
    private static final int DB_VERSION = 1;

    public final static String COL_ID = "_id";//ID
    public final static String COL_TITLE = "title";//タイトル
    public final static String COL_CONTENTS = "contents";//内容
    public final static String COL_CREATE_DATE = "create_date";//作成日
    public final static String COL_DEADLINE_DATE = "deadline_date";//期日
    public final static String COL_STATUS = "status";//状態

    public final static String STATUS_COMPLETE = "complete";//完了
    public final static String STATUS_INCOMPLETE = "incomplete";//未完了

    public final static String EDIT_EMPTY = "未登録";

    final String TITLE_PLAN = "予定";
    final String TITLE_HISTORY = "履歴";


    private SQLiteDatabase db = null;
    private DatabaseHelper dbHelper = null;
    protected Context context;

    public DBAdapter(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(this.context);
    }

    public DBAdapter openDB() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void closeDB() {
        db.close();
        db = null;
    }

    public void insertPlan(String title,String contents,String deadlineDate){
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();

            if(TextUtils.isEmpty(title)){
                title = EDIT_EMPTY;
            }
            values.put(COL_TITLE, title);

            if(TextUtils.isEmpty(contents)){
                contents = EDIT_EMPTY;
            }
            values.put(COL_CONTENTS, contents);

            if(TextUtils.isEmpty(deadlineDate)){
                deadlineDate = EDIT_EMPTY;
            }
            values.put(COL_DEADLINE_DATE, deadlineDate);

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            String dateNow = df.format(new Date());
            values.put(COL_CREATE_DATE,dateNow);//作成日

            values.put(COL_STATUS,STATUS_INCOMPLETE);

            db.insert(DB_TABLE, null, values);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updatePlan(String id,String title,String contents,String deadlineDate,String status){
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();

            if (TextUtils.isEmpty(title)) {
                title = EDIT_EMPTY;
            }
            values.put(COL_TITLE, title);

            if (TextUtils.isEmpty(contents)) {
                contents = EDIT_EMPTY;
            }
            values.put(COL_CONTENTS, contents);

            if (TextUtils.isEmpty(deadlineDate)) {
                deadlineDate = EDIT_EMPTY;
            }
            values.put(COL_DEADLINE_DATE, deadlineDate);

            if (TextUtils.isEmpty(status)) {
                status = EDIT_EMPTY;
            }
            values.put(COL_STATUS, status);

            db.update(DB_TABLE, values, COL_ID + " = ?", new String[]{id});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    //通知の「予定を終了ボタン」用
    public void updateStatus(String id){
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_STATUS, STATUS_COMPLETE);
            db.update(DB_TABLE, values, COL_ID + " = ?", new String[]{id});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void deletePlan(String id){
        db.beginTransaction();
        try {
            db.delete(DB_TABLE, COL_ID + " = ?", new String[]{id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public Cursor selectPlanList(String titleNow){
        String status=null;
        String orderBy=null;
        switch(titleNow){
            case TITLE_PLAN:
                status = STATUS_INCOMPLETE;
                orderBy = COL_DEADLINE_DATE + " ASC," + COL_CREATE_DATE + " DESC";
                break;
            case TITLE_HISTORY:
                status = STATUS_COMPLETE;
                orderBy = COL_DEADLINE_DATE + " DESC," + COL_CREATE_DATE + " DESC";
                break;
        }
        return db.query(DB_TABLE,null,COL_STATUS + " = ?",new String[]{status},null,null,orderBy);
    }

    public Cursor selectPlanAlarm(String today){
        return db.query(DB_TABLE,null,COL_DEADLINE_DATE + " = ? and " + COL_STATUS + " = 'incomplete'",new String[]{today},null,null,null);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            final String CREATE_TABLE =
                    "create table " + DB_TABLE + "("
                            + COL_ID + " integer primary key,"
                            + COL_TITLE +" text not null,"
                            + COL_CONTENTS +" text,"
                            + COL_CREATE_DATE +" text,"
                            + COL_DEADLINE_DATE +" text,"
                            + COL_STATUS +" text);";

            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS" + DB_TABLE);
            onCreate(db);
        }
    }
}
