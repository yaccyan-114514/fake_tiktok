package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Friend表的DAO类，负责friend表的所有数据库操作
 */
public class FriendDao {
    
    public static final String TABLE_NAME = "draft_friend";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    
    /**
     * 创建friend表
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL" +
                ")");
    }
    
    /**
     * 插入好友
     */
    public static long insert(SQLiteDatabase db, String name) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        return db.insert(TABLE_NAME, null, values);
    }
    
    /**
     * 批量插入好友
     */
    public static void insertAll(SQLiteDatabase db, List<String> names) {
        if (names != null && !names.isEmpty()) {
            for (String name : names) {
                if (name != null && !name.isEmpty()) {
                    insert(db, name);
                }
            }
        }
    }
    
    /**
     * 查询所有好友
     */
    public static Cursor queryAll(SQLiteDatabase db) {
        return db.query(TABLE_NAME,
                new String[]{COL_ID, COL_NAME},
                null, null, null, null,
                COL_NAME + " ASC");
    }
    
    /**
     * 根据ID查询好友
     */
    public static Cursor queryById(SQLiteDatabase db, long friendId) {
        return db.query(TABLE_NAME,
                new String[]{COL_ID, COL_NAME},
                COL_ID + "=?",
                new String[]{String.valueOf(friendId)},
                null, null, null);
    }
    
    /**
     * 从Cursor中获取好友名称列表
     */
    public static List<String> getFriendNamesFromCursor(Cursor cursor) {
        List<String> friends = new ArrayList<>();
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndexOrThrow(COL_NAME);
            while (cursor.moveToNext()) {
                friends.add(cursor.getString(nameIndex));
            }
        }
        return friends;
    }
    
    /**
     * 初始化Mock数据（5个朋友）
     */
    public static void initMockData(SQLiteDatabase db) {
        // 检查是否已有数据
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, null, null, null, null, null, "1");
        if (cursor.getCount() > 0) {
            cursor.close();
            return; // 已有数据，不重复插入
        }
        cursor.close();
        
        // 插入5个Mock好友
        List<String> mockFriends = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            mockFriends.add("friend" + i);
        }
        insertAll(db, mockFriends);
    }
}

