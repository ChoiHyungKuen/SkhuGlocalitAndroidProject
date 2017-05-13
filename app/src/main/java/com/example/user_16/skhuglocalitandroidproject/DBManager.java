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
        query = "create table board_data (no integer primary key autoincrement, name text, context text, date text);";//수정하기
        Log.d("쿼리",query);
        db.execSQL(query);
        Log.d("테이블","board_data 생성");

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


    //[자유게시판] - 글쓰기
    public void write(int no, String name, String text, String date){ //primarry 키와 유저이름, 글 내용
        db = getWritableDatabase();
        query = "insert into board_data values ('" + no + "', '" + name + "', '" + text + "', '" + date + "');";//
        Log.d("쿼리",query);
        db.execSQL(query);
        Log.d("게시판 글",text + "추가");

    }

    //[자유게시판] - 게시된 내용 받아오기
    public HashMap<String, HashMap<String,String>> getBoard() {
        db = getReadableDatabase();
        HashMap<String, HashMap<String,String>> boardInfo = new HashMap<>();
        query = "select * from board_data;";
        Log.d("쿼리",query);
        Cursor cursor = db.rawQuery(query, null);
        Log.d("테이블","읽기");
        int i = 0;
        while (cursor.moveToNext()) { //Cursor의 다음 내용이 있을 때 까지
            Log.d("i값",i+"-------------------------------");
            HashMap<String, String> boardcontent = new HashMap<>();
            boardcontent.put("index",cursor.getString(0));
            boardcontent.put("name",cursor.getString(1));
            boardcontent.put("context",cursor.getString(2));
            boardcontent.put("date",cursor.getString(3));//
            Log.d("boardcontent : ",boardcontent.toString());
            boardInfo.put(i+"",boardcontent);
            Log.d("boardinfo : ",boardInfo.toString());
            i++;
        }
        Log.d("게시판 내용 : ",boardInfo.toString());
        return boardInfo;
    }

}
