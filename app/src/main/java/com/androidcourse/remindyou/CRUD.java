package com.androidcourse.remindyou;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CRUD {
    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    private static final String[] colums = {
            NoteDatabase.ID,
            NoteDatabase.TITLE,
            NoteDatabase.CONTENT,
            NoteDatabase.TIME,
            NoteDatabase.MODE,
            NoteDatabase.LOCA,
            NoteDatabase.PATH
    };

    public CRUD(Context context) {
        dbHandler = new NoteDatabase(context);
    }

    public void open() {
        db = dbHandler.getWritableDatabase();
    }

    public void close() {
        dbHandler.close();
    }

    //把Note加入到Database
    public Note addNote(Note note) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(NoteDatabase.TITLE, note.getTitle());
        contentValues.put(NoteDatabase.CONTENT, note.getContent());
        contentValues.put(NoteDatabase.TIME, note.getTime());
        contentValues.put(NoteDatabase.MODE, note.getTag());
        contentValues.put(NoteDatabase.LOCA, note.getLoca());
        contentValues.put(NoteDatabase.PATH, note.getPath());

        long insertId = db.insert(NoteDatabase.TABLE_NAME, null, contentValues);
        note.setId(insertId);
        return note;
    }

    //查
    public Note getNote(long id) {
        Cursor cursor = db.query(NoteDatabase.TABLE_NAME, colums, NoteDatabase.ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        Note e = new Note(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4),cursor.getString(5),cursor.getString(6));
        return e;

    }

    //返回note的list
    public List<Note> getAllNotes() {
        Cursor cursor = db.query(NoteDatabase.TABLE_NAME, colums, null, null, null, null, null);
        List<Note> notes = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(NoteDatabase.TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.MODE)));
                note.setLoca(cursor.getString(cursor.getColumnIndex(NoteDatabase.LOCA)));
                note.setPath(cursor.getString(cursor.getColumnIndex(NoteDatabase.PATH)));
                notes.add(note);
            }
        }
        return notes;
    }

    //更新
    public int updateNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteDatabase.TITLE, note.getTitle());
        values.put(NoteDatabase.CONTENT, note.getContent());
        values.put(NoteDatabase.TIME, note.getTime());
        values.put(NoteDatabase.MODE, note.getTag());
        values.put(NoteDatabase.LOCA, note.getLoca());
        values.put(NoteDatabase.PATH, note.getPath());
        Log.e("update_path", note.getPath());
        return db.update(NoteDatabase.TABLE_NAME, values,
                NoteDatabase.ID + "=?", new String[]{String.valueOf(note.getId())});
    }

    //删除
    public void removeNote(Note note) {
        db.delete(NoteDatabase.TABLE_NAME, NoteDatabase.ID + "=" + note.getId(), null);
    }

}
