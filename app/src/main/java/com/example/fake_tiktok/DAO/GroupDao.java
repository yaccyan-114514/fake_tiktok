package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Group表的DAO类，负责group表的所有数据库操作
 */
public class GroupDao {
    
    public static final String TABLE_NAME = "draft_group";
    public static final String COL_ID = "id";
    public static final String COL_GROUPNAME = "groupname";
    
    /**
     * 创建group表
     * 注意：group是SQLite关键字，需要使用方括号包裹表名
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "] (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GROUPNAME + " TEXT NOT NULL" +
                ")");
    }
    
    /**
     * 插入分组
     */
    public static long insert(SQLiteDatabase db, String groupname) {
        ContentValues values = new ContentValues();
        values.put(COL_GROUPNAME, groupname);
        return db.insert("[" + TABLE_NAME + "]", null, values);
    }
    
    /**
     * 批量插入分组
     */
    public static void insertAll(SQLiteDatabase db, List<String> groupnames) {
        if (groupnames != null && !groupnames.isEmpty()) {
            for (String groupname : groupnames) {
                if (groupname != null && !groupname.isEmpty()) {
                    insert(db, groupname);
                }
            }
        }
    }
    
    /**
     * 查询所有分组
     */
    public static Cursor queryAll(SQLiteDatabase db) {
        return db.query("[" + TABLE_NAME + "]",
                new String[]{COL_ID, COL_GROUPNAME},
                null, null, null, null,
                COL_GROUPNAME + " ASC");
    }
    
    /**
     * 根据ID查询分组
     */
    public static Cursor queryById(SQLiteDatabase db, long groupId) {
        return db.query("[" + TABLE_NAME + "]",
                new String[]{COL_ID, COL_GROUPNAME},
                COL_ID + "=?",
                new String[]{String.valueOf(groupId)},
                null, null, null);
    }
    
    /**
     * 从Cursor中获取分组名称列表
     */
    public static List<String> getGroupNamesFromCursor(Cursor cursor) {
        List<String> groups = new ArrayList<>();
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndexOrThrow(COL_GROUPNAME);
            while (cursor.moveToNext()) {
                groups.add(cursor.getString(nameIndex));
            }
        }
        return groups;
    }
}

