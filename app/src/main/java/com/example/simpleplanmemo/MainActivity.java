package com.example.simpleplanmemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

import static android.app.AlertDialog.*;

import static com.example.simpleplanmemo.enum_pack.PrefEnum.*;

public class MainActivity extends AppCompatActivity {

    Plan plan;
    MyBaseAdapter myBaseAdapter;
    List<Plan> items;
    ListView planListView;
    Menu menu;
    MenuItem listChangeButton;
    Toolbar toolbar;

    private static final int REQUEST_CODE = 1;

    final String TITLE_PLAN = "予定";
    final String TITLE_HISTORY = "履歴";

    String titleNow = TITLE_PLAN;//初期値は予定

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    MethodList methodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        methodList = new MethodList();

        pref = getSharedPreferences(FILE_NAME.getString(), MODE_PRIVATE);
        editor = pref.edit();

        editor.putBoolean(LIST_RELOAD.getString(),false);

        String firstStartFlg = pref.getString(FIRST_START.getString(),null);
        if(firstStartFlg==null){
            firstStartFlg = "started";
            editor.putString(FIRST_START.getString(),firstStartFlg);

            int hourOfDay = 0;
            int minute = 5;
            editor.putInt(HOUR_OF_DAY.getString(),hourOfDay);
            editor.putInt(MINUTE.getString(),minute);

            editor.putBoolean(NOTIFICATION_FLG.getString(),true);

            methodList.startAlarm(this,false);

        }

        editor.commit();

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        menu = toolbar.getMenu();
        listChangeButton = menu.findItem(R.id.button_list_change);
        setTitle(titleNow);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.button_insert_page) {
                    //登録画面に移動
                    Intent intent = new Intent(MainActivity.this,InsertActivity.class);
                    startActivityForResult(intent,REQUEST_CODE);
                    return true;
                } else if (id == R.id.button_list_change) {
                    //リスト内容切り替えボタン
                    switch(titleNow){
                        case TITLE_PLAN:
                            titleNow = TITLE_HISTORY;
                            break;
                        case TITLE_HISTORY:
                            titleNow = TITLE_PLAN;
                            break;
                    }
                    setTitle(titleNow);
                    loadMyList(titleNow);
                    return true;
                }else if(id == R.id.button_setting){
                    //設定画面に移動
                    Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                    startActivity(intent);
                    return true;
                }
                return true;
            }
        });

        planListView = findViewById(R.id.plan_list);

        items = new ArrayList<>();

        myBaseAdapter = new MyBaseAdapter(this, items);

        loadMyList(titleNow);

        planListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Builder builder = new Builder(MainActivity.this);
                builder.setTitle("データの削除");
                builder.setMessage("削除しますか？");

                builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        plan = items.get(position);
                        String listId = plan.getId();
                        DBAdapter dbAdapter = new DBAdapter(MainActivity.this);
                        dbAdapter.openDB();
                        dbAdapter.deletePlan(listId);
                        loadMyList(titleNow);
                    }
                });

                builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        //詳細画面に移動
        planListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?>parent,View view,int position,long id){
                plan = items.get(position);
                Intent intent = new Intent(MainActivity.this, PlanInfoActivity.class);
                intent.putExtra("info", plan);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });

    }

    /*このアプリを起動中に通知を消去した場合に使用
    メインアクティビティにフォーカスが当たった時にリストを更新する*/
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            if(pref.getBoolean(LIST_RELOAD.getString(),false)){
                loadMyList(titleNow);
                editor.putBoolean(LIST_RELOAD.getString(),false);
                editor.commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //ツールバーのタイトルと、リスト内容切り替えボタンのテキストを変更
    void setTitle(String titleNow){
        toolbar.setTitle(titleNow);
        switch(titleNow){
            case TITLE_PLAN:
                listChangeButton.setTitle(TITLE_HISTORY);
                break;
            case TITLE_HISTORY:
                listChangeButton.setTitle(TITLE_PLAN);
                break;
        }
    }

    //データの更新があった場合にリストを再読み込み
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                if (RESULT_OK == resultCode) {
                    loadMyList(titleNow);
                }
                break;
        }
    }

    //リスト読み込み
    void loadMyList(String titleNow){
        items.clear();
        DBAdapter dbAdapter = new DBAdapter(this);
        dbAdapter.openDB();
        Cursor c = dbAdapter.selectPlanList(titleNow);
        if(c.moveToFirst()){
            do{
                plan = new Plan(
                        String.valueOf(c.getInt(0)),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5));
                items.add(plan);
            }while(c.moveToNext());

        }
        c.close();
        //dbAdapter.closeDB();
        planListView.setAdapter(myBaseAdapter);
        myBaseAdapter.notifyDataSetChanged();
    }

    public class MyBaseAdapter extends BaseAdapter {
        private Context context;
        private List<Plan>items;

        private class ViewHolder{
            TextView textTitle;
            TextView textDeadlineDate;
        }

        public MyBaseAdapter(Context context,List<Plan>items){
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount(){
            return items.size();
        }

        @Override
        public Object getItem(int position){
            return items.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            plan = items.get(position);

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.row_sheet_listview, parent, false);

                TextView textTitle = view.findViewById(R.id.text_title);
                TextView textDeadlineDate = view.findViewById(R.id.text_deadline_date);

                holder = new ViewHolder();
                holder.textTitle = textTitle;
                holder.textDeadlineDate = textDeadlineDate;
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.textTitle.setText(plan.getTitle());
            String yobi;
            if(!plan.getDeadlineDate().equals("未登録")) {
                yobi = methodList.getYobi(plan.getDeadlineDate());
            }else{
                yobi="";
            }
            holder.textDeadlineDate.setText(plan.getDeadlineDate()+yobi);

            return view;
        }
    }

}
