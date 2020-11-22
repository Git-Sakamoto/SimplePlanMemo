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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InsertActivity extends AppCompatActivity {

    EditText editTitle,editContents,editDeadlineDate;
    Button insertButton;
    InputMethodManager inputMethodManager;
    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        layout = findViewById(R.id.insert_layout);

        //戻るボタン用
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        editTitle = findViewById(R.id.edit_title);
        editContents = findViewById(R.id.edit_contents);

        editDeadlineDate = findViewById(R.id.edit_deadline_date);
        editDeadlineDate.setOnClickListener(new View.OnClickListener(){
            //日付入力ダイアログを表示
            @Override
            public void onClick(View v){
                closeKeyboard();
                setDeadlineDate();
            }
        });

        //登録ボタン
        insertButton = findViewById(R.id.button_insert);
        insertButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                insertPlan();
            }
        });

    }

    //戻るボタン
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

    void setDeadlineDate(){
        String[] menulist = {"未登録","日付入力","キャンセル"};
        AlertDialog.Builder builder = new AlertDialog.Builder(InsertActivity.this);
        builder.setTitle("期日の入力");
        builder.setItems(menulist,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                switch(index){
                    case 0:
                        editDeadlineDate.setText("未登録");
                        break;
                    case 1:
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                InsertActivity.this,
                                new DatePickerDialog.OnDateSetListener(){
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
                                        editDeadlineDate.setText(String.format("%d/%02d/%02d",year,monthOfYear+1,dayOfMonth));
                                    }
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DATE)
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

    void insertPlan(){
        String strTitle = editTitle.getText().toString();
        String strContents = editContents.getText().toString();
        String strDeadlineDate = editDeadlineDate.getText().toString();

        if(TextUtils.isEmpty(strTitle)){
            Toast.makeText(InsertActivity.this,"タイトル名は必須です",Toast.LENGTH_LONG).show();
        }else{
            DBAdapter dbAdapter = new DBAdapter(this);
            dbAdapter.openDB();
            dbAdapter.insertPlan(strTitle,strContents,strDeadlineDate);
            //dbAdapter.closeDB();

            editTitle.setText("");
            editDeadlineDate.setText("");
            editContents.setText("");

            setResult(RESULT_OK);

            closeKeyboard();
            Toast.makeText(InsertActivity.this,"登録が完了しました",Toast.LENGTH_LONG).show();

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            String dateNow = df.format(new Date());
            if(strDeadlineDate.equals(dateNow)) {
                MethodList methodList = new MethodList();
                methodList.startAlarm(this, false);
            }
        }
    }

}
