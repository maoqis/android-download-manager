package com.yyxu.download.db;  
   
 import android.content.Context;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
   
     /** 
      * ����һ����ݿ������ 
      */  
 public class DBHelper extends SQLiteOpenHelper {  
     //download.db-->��ݿ���  
     public DBHelper(Context context) {  
         super(context, "download.db", null, 1);  
     }  
       
     /** 
      * ��download.db��ݿ��´���һ��download_info��洢������Ϣ 
      */  
     @Override  
     public void onCreate(SQLiteDatabase db) {  
         db.execSQL("create table download_info(_id integer PRIMARY KEY AUTOINCREMENT, load_id integer, "  
                 + " url char ,state integer,loadsize integer,totalsize integer)");  
     }  
     @Override  
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
   
     }  
   
 }