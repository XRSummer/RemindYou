package com.androidcourse.remindyou;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends BaseAdapter implements Filterable {
    private Context mContext;

    private List<Note> backList;//用来备份原始数据
    private List<Note> noteList;
    private MyFilter myFilter;

    public NoteAdapter(Context mContext, List<Note> noteList) {
        this.mContext = mContext;
        this.noteList = noteList;
        backList = noteList;
    }

    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Object getItem(int i) {
        return noteList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = View.inflate(mContext,R.layout.note_layout,null);
        TextView tv_title = (TextView)v.findViewById(R.id.tv_title);
        TextView tv_content = (TextView)v.findViewById(R.id.tv_content);
        TextView tv_time = (TextView)v.findViewById(R.id.tv_time);
        TextView tv_loca = v.findViewById(R.id.tv_loca);
        ImageView iv = v.findViewById(R.id.iv);

        String allText = noteList.get(i).getContent();
        tv_title.setText(noteList.get(i).getTitle());
        tv_content.setText(allText);
        tv_time.setText(noteList.get(i).getTime());
        tv_loca.setText(noteList.get(i).getLoca());
        String path = noteList.get(i).getPath();
        Log.e("adapter_path", path);
        if(path.equals("0")){
            iv.setVisibility(View.INVISIBLE);
        }else{
            Uri uri = getMediaUriFromPath(mContext, path);
            iv.setImageURI(uri);

            Log.e("show","11");
            iv.setVisibility(View.VISIBLE);
        }
        v.setTag(noteList.get(i).getId());
        return v;
    }

    public static Uri getMediaUriFromPath(Context context, String path) {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri,
                null,
                MediaStore.Images.Media.DISPLAY_NAME + "= ?",
                new String[] {path.substring(path.lastIndexOf("/") + 1)},
                null);

        Uri uri = null;
        if(cursor.moveToFirst()) {
            uri = ContentUris.withAppendedId(mediaUri,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        }
        cursor.close();
        return uri;
    }


    @Override
    public Filter getFilter() {
        if(myFilter == null){
            myFilter = new MyFilter();
        }
        return myFilter;
    }


    class MyFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
           FilterResults result = new FilterResults();
           List<Note> list;
           if(TextUtils.isEmpty(charSequence)){//当过滤的关键字为空时，我们显示所有数据
               list = backList;
           }else{//把符合条件的数据显示
               list = new ArrayList<>();
               for(Note note : backList){
                   if(note.getContent().contains(charSequence) || note.getTitle().contains(charSequence)){
                       list.add(note);
                   }
               }
           }


            result.values = list;
            result.count = list.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            noteList = (List<Note>) filterResults.values;
            if(filterResults.count > 0){
                notifyDataSetChanged();
            }else{
                notifyDataSetInvalidated();
            }
        }
    }



}
