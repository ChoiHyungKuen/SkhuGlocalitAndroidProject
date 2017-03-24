package com.example.user_16.skhuglocalitandroidproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by user-16 on 2017-03-24.
 */

public class DBManager extends SQLiteOpenHelper {

    SQLiteDatabase db;
    String query = "";
    public DBManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        query = "create table app_data (no integer primary key autoincrement, id text, name text, email text);";
        Log.d("쿼리",query);
        db.execSQL(query);
        Log.d("테이블","생성");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String id, String name, String email){
        db = getWritableDatabase();
        query = "insert into app_data values (null, '" + id + "', '" + name + "', '" + email + "');";
        Log.d("쿼리",query);
        db.execSQL(query);
        Log.d("회원정보","추가");
    }

    public void delete(String id) {
        db = getWritableDatabase();
        query =  "delete from app_data where id = '" + id + "';";
        Log.d("쿼리",query);
        db.execSQL(query);
        Log.d("회원정보","삭제");
    }

    public HashMap<String, String> getMemberInfo() {
        db = getReadableDatabase();
        HashMap<String, String> memberInfo = new HashMap<>();
        query = "select * from app_data;";
        Log.d("쿼리",query);
        Cursor cursor = db.rawQuery(query, null);
        Log.d("테이블","읽기");
        while (cursor.moveToNext()) {
            memberInfo.put("id",cursor.getString(1));
            memberInfo.put("name",cursor.getString(2));
            memberInfo.put("email",cursor.getString(3));
        }
        Log.d("회원정보",memberInfo.toString());
        return memberInfo;
    }

}
