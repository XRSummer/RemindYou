package com.androidcourse.remindyou;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.LayoutInflaterCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private NoteDatabase dbHelper;
    private NoteAdapter adapter;

    private Context context = this;

    FloatingActionButton btn;

    private ListView lv;
    private List<Note> noteList = new ArrayList<>();

    private Toolbar toolbar;

    //弹出菜单
    private PopupWindow popupWindow;
    private RelativeLayout main;
    private ViewGroup customView;
    private LayoutInflater layoutInflater;
    private WindowManager vm;
    private DisplayMetrics metrics;

    //蒙版
    private PopupWindow popupCover;
    private ViewGroup coverView;

    private FloatingActionButton fab2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPopupView();
        //设置toolbar
        toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupView();
            }
        });


        btn = (FloatingActionButton) findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("mode",4);
                intent.putExtra("path_0", "0");
                Log.e("new", "4");
                startActivityForResult(intent,0);
            }
        });

        lv = findViewById(R.id.lv);
        adapter = new NoteAdapter(getApplicationContext(),noteList);
        refreshListView();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);

    }

    //初始化左侧弹窗
    public void initPopupView(){
        layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView = (ViewGroup) layoutInflater.inflate(R.layout.setting_layout,null);
        main = findViewById(R.id.main_layout);

        vm = getWindowManager();
        metrics = new DisplayMetrics();
        vm.getDefaultDisplay().getMetrics(metrics);


        //蒙版初始化
        coverView = (ViewGroup) layoutInflater.inflate(R.layout.setting_cover,null);
    }
    //
    public void showPopupView(){
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        //蒙版出现
        popupCover = new PopupWindow(coverView,width,height,false);

        popupWindow = new PopupWindow(customView,(int)(width * 0.7),height,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        //加载完主页面再加载弹出页面
        findViewById(R.id.main_layout).post(new Runnable() {
            @Override
            public void run() {
                popupCover.showAtLocation(main, Gravity.NO_GRAVITY,0,0);
                popupWindow.showAtLocation(main, Gravity.NO_GRAVITY,0,0);

                coverView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        popupCover.dismiss();
                        return true;
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        popupCover.dismiss();
                    }
                });
            }
        });
    }

    private void refreshListView() {
        CRUD op = new CRUD(context);
        op.open();
        if(noteList.size()>0)noteList.clear();
        noteList.addAll(op.getAllNotes());
        op.close();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        int return_mode;
        long note_Id;
        return_mode = data.getExtras().getInt("mode",-1);
        note_Id = data.getExtras().getLong("id",0);
        if(return_mode == 1){//update current note
            String title = data.getExtras().getString("title");
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag",1);
            String loca = data.getExtras().getString("loca");
            String path = data.getExtras().getString("path");
            Log.e("data1_path", path);
            Note note = new Note(title,content,time,tag,loca,path);
            note.setId(note_Id);

            CRUD op = new CRUD(context);
            op.open();
            op.updateNote(note);
            op.close();
        }else if(return_mode == 0) {//create a new note
            String title = data.getExtras().getString("title");
            String content = data.getStringExtra("content");
            String time = data.getStringExtra("time");
            int tag = data.getExtras().getInt("tag",1);
            String loca = data.getExtras().getString("loca");
            String path = data.getExtras().getString("path");
            Log.e("data0_path", path);
            Note note = new Note(title,content,time,tag,loca,path);

            CRUD op = new CRUD(context);
            op.open();
            op.addNote(note);
            op.close();
        }else if(return_mode == 2){
            Note curNote = new Note();
            curNote.setId(note_Id);
            CRUD op = new CRUD(context);
            op.open();
            op.removeNote(curNote);
            op.close();

        }else{

        }

        refreshListView();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.lv:
                Note curNote = (Note) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("title", curNote.getTitle());
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode", 3);
                intent.putExtra("tag", curNote.getTag());
                intent.putExtra("path",curNote.getPath());
                Log.e("path_put", curNote.getPath());
                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search...");

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_clear:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("确定删除全部吗？")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dbHelper = new NoteDatabase(context);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.delete("notes",null,null);
                                db.execSQL("update sqlite_sequence set seq =0 where name = 'notes'");
                                refreshListView();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
