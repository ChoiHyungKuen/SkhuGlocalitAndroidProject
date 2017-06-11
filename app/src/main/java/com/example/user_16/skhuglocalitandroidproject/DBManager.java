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
        query = "create table favorites_data (no integer primary key autoincrement, title text, branch text, longitude text, latitude text, up text, down text);";
        Log.d("쿼리",query);
        db.execSQL(query);
        Log.d("즐찾 테이블","생성");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert(String id, String name, String email){
        db = getWritableDatabase();
        query = "insert into app_data values (null, '" + id + "', '" + name + "', '" + email + "');";
        Log.d("쿼리",query);
        db.execSQL(query);
        Log.d("회원정보","insrt추가");
    }

    public void deleteAll(){
        this.db.delete("app_data",null,null);
        Log.d("회원정보","전부삭제");
    }

    public HashMap<String, String> getMemberInfo() {
        db = getReadableDatabase();
        HashMap<String, String> memberInfo = new HashMap<>();
        query = "select * from app_data;";
        Log.d("쿼리",query);
        Cursor cursor = db.rawQuery(query, null);
        Log.d("테이블","읽기");
        int cnt=0;
        while (cursor.moveToNext()) {
            memberInfo.put("id",cursor.getString(1));
            memberInfo.put("name",cursor.getString(2));
            memberInfo.put("email",cursor.getString(3));
            cnt++;
            Log.d("회원정보",memberInfo.toString()+"");
        }
        Log.d("회원정보",cnt+"개");
        return memberInfo;
    }

    public void add_favorites(int no, String title, String branch, String longitude, String latitude, String up, String down){
        db = getWritableDatabase();
        query = "insert into favorites_data values ('" + no + "', '" + title + "', '" + branch + "', '" + longitude + "', '" + latitude + "', '" + up + "', '" + down + "');";
        Log.d("쿼리",query);
        db.execSQL(query);
        Log.d("즐찾정보",title+"추가");
    }

    public boolean isFavorite(String title, String longitude, String latitude) {
        Boolean flag = false;
        db = getWritableDatabase();
        query = "select * from favorites_data where title = '" + title + "' and longitude = '" + longitude + "' and latitude = '" + latitude + "';";
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        if (count == 1){
            flag = true;
        }
        return flag;
    }
    public HashMap<Integer, HashMap<String, String>> getFavoritesList(){
        db = getReadableDatabase();
        HashMap<Integer, HashMap<String, String>> favoriteList = new HashMap<>();
        HashMap<String, String> favorites;
        query = "select * from favorites_data;";
        Log.d("쿼리",query);
        Cursor cursor = db.rawQuery(query, null);
        Log.d("테이블","읽기");
        int no;
        while (cursor.moveToNext()) {
            favorites = new HashMap<>();
            no = cursor.getInt(0);
            favorites.put("title",cursor.getString(1));
            favorites.put("branch",cursor.getString(2));
            favorites.put("longitude",cursor.getString(3));
            favorites.put("latitude",cursor.getString(4));
            favorites.put("up",cursor.getString(5));
            favorites.put("down",cursor.getString(6));
            favoriteList.put(no, favorites);

        }
        Log.d("즐찾 정보", favoriteList.toString());
        return favoriteList;
    }
    public int getFavoriteNo(String title, String longitude, String latitude){
        db = getReadableDatabase();
        query = "select no from favorites_data where title = '" + title + "' and longitude = '" + longitude + "' and latitude = '" + latitude + "';";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToNext();
        int no= cursor.getInt(0);

        return no;
    }
    public void updateFavorite(int no, String up, String down){
        db = getWritableDatabase();
        query = "update favorites_data set up = " + up + ", down = " + down +" where no = " + no + ";";
        db.execSQL(query);
    }

    public void deleteFavorite(int no){
        db = getWritableDatabase();
        if (getCount() > 1){
            query = "delete from favorites_data where no = " + no + ";";
            db.execSQL(query);
            for (int i = no+1; i <= getCount(); i++){
                int updateNo = i - 1;
                query = "update favorites_data set no = " + updateNo + " where no = " + i + ";";
                db.execSQL(query);
            }
        } else if (getCount() == 1){
            query = "delete from favorites_data where no = " + no + ";";
            db.execSQL(query);
        }

    }

    public int getCount(){
        db = getReadableDatabase();
        query = "select count(*) from favorites_data";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToNext();
        int count = cursor.getInt(0);

        return count;
    }

}
