package com.example.simpleplanmemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PlanInfoActivity extends AppCompatActivity {
    EditText editTitle,editContents,editDeadlineDate;
    Spinner spinnerStatus;
    Button editButton,cancelButton,updateButton;
    InputMethodManager inputMethodManager;
    ConstraintLayout layout;
    Plan plan;
    String strId,strTitle,strContents,strDeadlineDate,strStatus;
    final String COMPLETE = "complete";
    final String INCOMPLETE = "incomplete";
    int spinnerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_info);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        layout = findViewById(R.id.plan_info_layout);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        plan = (Plan) getIntent().getSerializableExtra("info");

        editTitle = findViewById(R.id.edit_title);
        editContents = findViewById(R.id.edit_contents);
        editDeadlineDate = findViewById(R.id.edit_deadline_date);
        spinnerStatus = (Spinner)findViewById(R.id.spinner_status);

        setText();

        //編集ボタン
        editButton = findViewById(R.id.button_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
                updateButton.setVisibility(View.VISIBLE);

                editTitle.setCursorVisible(true);
                editTitle.setFocusable(true);
                editTitle.setFocusableInTouchMode(true);

                editContents.setCursorVisible(true);
                editContents.setFocusable(true);
                editContents.setFocusableInTouchMode(true);

                spinnerStatus.setClickable(true);

                editDeadlineDate.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        closeKeyboard();
                        setDeadlineDate();
                    }
                });
            }
        });

        //キャンセルボタン
        cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editStop();
            }
        });

        //更新ボタン
        updateButton = findViewById(R.id.button_update);
        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlanInfoActivity.this);
                builder.setTitle("登録情報の更新");
                builder.setMessage("登録情報を更新しますか？");

                builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updatePlan();
                    }
                });

                builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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

    void closeKeyboard(){
        //キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(layout.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        //背景にフォーカスを移す
        layout.requestFocus();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        closeKeyboard();
        return true;
    }

    void setText(){
        editTitle.setText(plan.getTitle(), TextView.BufferType.NORMAL);
        if(plan.getContents().equals("未登録")){
            editContents.setText("");
        }else{
            editContents.setText(plan.getContents(), TextView.BufferType.NORMAL);
        }
        editDeadlineDate.setText(plan.getDeadlineDate(), TextView.BufferType.NORMAL);
        strDeadlineDate = plan.getDeadlineDate();

        strStatus = plan.getStatus();
        switch (strStatus){
            case COMPLETE:
                spinnerPosition = 0;
                break;
            case INCOMPLETE:
                spinnerPosition = 1;
                break;
        }
        spinnerStatus.setSelection(spinnerPosition);
    }

    //日付入力ダイアログ
    void setDeadlineDate(){
        String[] menulist = {"未登録","日付入力","キャンセル"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PlanInfoActivity.this);
        builder.setTitle("期日の入力");
        builder.setItems(menulist,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                switch(index){
                    case 0:
                        editDeadlineDate.setText("未登録");
                        break;
                    case 1:
                        Calendar calendar = Calendar.getInstance();
                        int year,month,day;
                        if(editDeadlineDate.getText().toString().equals("未登録")){
                            year = calendar.get(Calendar.YEAR);
                            month = calendar.get(Calendar.MONTH) + 1;
                            day = calendar.get(Calendar.DATE);
                        }else {
                            year = Integer.parseInt(strDeadlineDate.substring(0, 4));
                            month = Integer.parseInt(strDeadlineDate.substring(5, 7));
                            Log.d("month",String.valueOf(month));
                            day = Integer.parseInt(strDeadlineDate.substring(8, 10));
                        }

                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                PlanInfoActivity.this,
                                new DatePickerDialog.OnDateSetListener(){
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
                                        editDeadlineDate.setText(String.format("%d/%02d/%02d",year,monthOfYear+1,dayOfMonth));
                                        strDeadlineDate = String.format("%d/%02d/%02d",year,monthOfYear+1,dayOfMonth);
                                        Log.d("deadline_date",strDeadlineDate);
                                    }
                                },
                                year, month-1, day
                        );
                        datePickerDialog.show();
                        break;
                    case 2:
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void updatePlan(){
        strId = plan.getId();
        strTitle = editTitle.getText().toString();
        if(TextUtils.isEmpty(strTitle)){
            Toast.makeText(PlanInfoActivity.this,"タイトル名は必須です",Toast.LENGTH_LONG).show();
        }else {
            strContents = editContents.getText().toString();
            strDeadlineDate = editDeadlineDate.getText().toString();
            spinnerPosition = spinnerStatus.getSelectedItemPosition();
            switch (spinnerPosition) {
                case 0:
                    strStatus = COMPLETE;
                    break;
                case 1:
                    strStatus = INCOMPLETE;
                    break;
                default:
                    strStatus = "未登録";
                    break;
            }

            DBAdapter dbAdapter = new DBAdapter(this);
            dbAdapter.openDB();
            dbAdapter.updatePlan(strId,strTitle,strContents,strDeadlineDate,strStatus);

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            String dateNow = df.format(new Date());

            //状態を未完了から完了に更新した場合に通知を消去
            if(plan.getStatus().equals(INCOMPLETE)&&strStatus.equals(COMPLETE)){
                MethodList methodList = new MethodList();
                methodList.closeNotification(this,Integer.parseInt(strId));
            }

            //更新後の予定が通知に影響あれば
            if(strStatus.equals(INCOMPLETE)&&strDeadlineDate.equals(dateNow)){
                MethodList methodList = new MethodList();
                methodList.startAlarm(this,false);
            }

            plan = new Plan(
                    plan.getId(),
                    strTitle,
                    strContents,
                    plan.getCreateDate(),
                    strDeadlineDate,
                    strStatus
            );

            setResult(RESULT_OK);

            closeKeyboard();

            editStop();

            Toast.makeText(PlanInfoActivity.this, "更新が完了しました", Toast.LENGTH_LONG).show();

        }
    }

    //キャンセルボタン
    void editStop(){
        editButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.GONE);
        updateButton.setVisibility(View.GONE);

        editTitle.setCursorVisible(false);
        editTitle.setFocusable(false);
        editTitle.setFocusableInTouchMode(false);

        editContents.setCursorVisible(false);
        editContents.setFocusable(false);
        editContents.setFocusableInTouchMode(false);

        editDeadlineDate.setOnClickListener(null);

        spinnerStatus.setClickable(false);

        setText();
    }
}