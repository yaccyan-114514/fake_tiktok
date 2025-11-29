package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Draft表的DAO类，负责draft表的所有数据库操作
 */
public class DraftDao {
    
    public static final String TABLE_NAME = "draft";
    public static final String COL_ID = "id";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";
    
    /**
     * 创建draft表
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CREATED_AT + " INTEGER NOT NULL, " +
                COL_UPDATED_AT + " INTEGER NOT NULL" +
                ")");
    }
    
    /**
     * 插入新的草稿记录
     */
    public static long insert(SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();
            long currentTime = System.currentTimeMillis();
            values.put(COL_CREATED_AT, currentTime);
            values.put(COL_UPDATED_AT, currentTime);
            long result = db.insert(TABLE_NAME, null, values);
            android.util.Log.d("DraftDao", "插入草稿记录，返回ID: " + result);
            if (result == -1) {
                android.util.Log.e("DraftDao", "插入草稿记录失败");
            }
            return result;
        } catch (Exception e) {
            android.util.Log.e("DraftDao", "插入草稿记录异常", e);
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 更新草稿的更新时间
     */
    public static int update(SQLiteDatabase db, long draftId) {
        ContentValues values = new ContentValues();
        values.put(COL_UPDATED_AT, System.currentTimeMillis());
        return db.update(TABLE_NAME, values, COL_ID + "=?", 
                new String[]{String.valueOf(draftId)});
    }
    
    /**
     * 删除草稿
     */
    public static int delete(SQLiteDatabase db, long draftId) {
        return db.delete(TABLE_NAME, COL_ID + "=?", 
                new String[]{String.valueOf(draftId)});
    }
    
    /**
     * 根据ID查询草稿
     */
    public static Cursor queryById(SQLiteDatabase db, long draftId) {
        return db.query(TABLE_NAME, 
                new String[]{COL_ID, COL_CREATED_AT, COL_UPDATED_AT},
                COL_ID + "=?",
                new String[]{String.valueOf(draftId)},
                null, null, null);
    }
    
    /**
     * 查询最新的草稿
     */
    public static Cursor queryLatest(SQLiteDatabase db) {
        return db.query(TABLE_NAME, 
                new String[]{COL_ID, COL_CREATED_AT, COL_UPDATED_AT},
                null, null, null, null,
                COL_UPDATED_AT + " DESC", "1");
    }
    
    /**
     * 检查是否有草稿
     */
    public static boolean hasDraft(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COL_ID},
                null, null, null, null, null, "1");
        boolean hasDraft = cursor.getCount() > 0;
        cursor.close();
        return hasDraft;
    }
}

