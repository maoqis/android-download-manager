package com.yyxu.download.db;  
  

import java.util.ArrayList;
import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
  
/** 
 *  
 * һ��ҵ���� 
 */  
public class Dao {    
    private static Dao dao=null;  
    private Context context;   
    private  Dao(Context context) {   
        this.context=context;  
    }  
    public static  Dao getInstance(Context context){  
        if(dao==null){  
            dao=new Dao(context);   
        }  
        return dao;  
    }  
    public  SQLiteDatabase getConnection() {  
        SQLiteDatabase sqliteDatabase = null;  
        try {   
            sqliteDatabase= new DBHelper(context).getReadableDatabase();  
        } catch (Exception e) {    
        }  
        return sqliteDatabase;  
    }  
  
    /** 
     * �鿴��ݿ����Ƿ������ 
     */  
    public synchronized boolean isHasInfors(String urlstr,int state) {  
        SQLiteDatabase database = getConnection();  
        boolean  ishas = false;
        Cursor cursor = null;  
        try {  
            String sql = "select * from download_info where url=? and state=?";  
            cursor = database.rawQuery(sql, new String[] { urlstr ,String.valueOf(state)});  
            if (cursor.moveToFirst()) {  
                if (cursor!=null) {
                	ishas =true;
				} 
            }   
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (null != database) {  
                database.close();  
            }  
            if (null != cursor) {  
                cursor.close();  
            }  
        }  
        return ishas;  
    }  
    
    public synchronized int stateCount(int state) {  
        SQLiteDatabase database = getConnection();  
        int count = -1;  
        Cursor cursor = null;  
        try {  
            String sql = "select count(*)  from download_info where state=?";  
            cursor = database.rawQuery(sql, new String[] { String.valueOf(state)});  
            if (cursor.moveToFirst()) {  
                count = cursor.getInt(0);  
            }   
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (null != database) {  
                database.close();  
            }  
            if (null != cursor) {  
                cursor.close();  
            }  
        }  
        return count;  
    }  

    
  
    /** 
     * ���� ���صľ�����Ϣ 
     */  
    public synchronized void saveInfos(DownloadInfo infos) {  
        SQLiteDatabase database = getConnection();  
        try {  
                String sql = "insert into download_info(load_id,url,state,loadsize,totalsize) values (?,?,?,?,?)";  
                Object[] bindArgs = { infos.getLoad_id(),infos.getUrl(),infos.getState(),infos.getLoadsize(),infos.getTotalsize() };  
                database.execSQL(sql, bindArgs);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (null != database) {  
                database.close();  
            }  
        }  
    }  
  
    /** 
     * �õ����ؾ�����Ϣ 
     */  
    public synchronized DownloadInfo getInfos(String urlstr,int state) {  
        SQLiteDatabase database = getConnection();
        DownloadInfo info = null;
        Cursor cursor = null;  
        try {  
            String sql = "select load_id,url,state,loadsize,totalsize from download_info where url=? and state=?";  
            cursor = database.rawQuery(sql, new String[] { urlstr,String.valueOf(state) });  
            if (cursor.moveToFirst()) {  
            	info =new DownloadInfo(cursor.getInt(0),cursor.getString(1),cursor.getInt(2),cursor.getLong(3),cursor.getLong(4));
            }   
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (null != database) {  
                database.close();  
            }  
            if (null != cursor) {  
                cursor.close();  
            }  
        }  
        return info;  
    }  
    
    public synchronized ArrayList<DownloadInfo> getList(int state) {  
    	ArrayList<DownloadInfo> list = new ArrayList<DownloadInfo>();
        SQLiteDatabase database = getConnection();
        Cursor cursor = null;  
        try {  
            String sql = "select load_id,url,state,loadsize,totalsize from download_info where state=?";  
            cursor = database.rawQuery(sql, new String[] { String.valueOf(state) });  
            while (cursor.moveToNext()) {  
            	DownloadInfo info =new DownloadInfo(cursor.getInt(0),cursor.getString(1),cursor.getInt(2),cursor.getLong(3),cursor.getLong(4));
            	list.add(info);
            }   
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (null != database) {  
                database.close();  
            }  
            if (null != cursor) {  
                cursor.close();  
            }  
        }  
        return list;  
    }  
    

  
    /** 
     * ������ݿ��е�������Ϣ 
     */  
    public synchronized void updataInfos(String url, int state,long loadsize,long totalsize ) {  
        SQLiteDatabase database = getConnection();  
        try {  
            String sql = "update download_info set state=? loadsize=? totalsize=? where url=?";  
            Object[] bindArgs = { state,loadsize,totalsize };  
            database.execSQL(sql, bindArgs);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (null != database) {  
                database.close();  
            }  
        }  
    }  
  
    /** 
     * ������ɺ�ɾ����ݿ��е���� 
     */  
    public synchronized void delete(String url,int state) {  
        SQLiteDatabase database = getConnection();  
        try {  
            database.delete("download_info", "url=? and state=?", new String[] { url, String.valueOf(state)});  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (null != database) {  
                database.close();  
            }  
        }  
    }  
} 