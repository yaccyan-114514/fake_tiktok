package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tag表的DAO类，负责tag表的所有数据库操作（标签库）
 */
public class TagDao {
    
    public static final String TABLE_NAME = "tag";
    public static final String COL_ID = "id";
    public static final String COL_TAGNAME = "tagname";
    
    /**
     * 创建tag表
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TAGNAME + " TEXT NOT NULL UNIQUE" +
                ")");
    }
    
    /**
     * 插入标签
     */
    public static long insert(SQLiteDatabase db, String tagname) {
        ContentValues values = new ContentValues();
        values.put(COL_TAGNAME, tagname);
        try {
            return db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            // 如果标签已存在（UNIQUE约束），返回-1
            return -1;
        }
    }
    
    /**
     * 批量插入标签
     */
    public static void insertAll(SQLiteDatabase db, List<String> tagnames) {
        if (tagnames != null && !tagnames.isEmpty()) {
            for (String tagname : tagnames) {
                if (tagname != null && !tagname.isEmpty()) {
                    insert(db, tagname);
                }
            }
        }
    }
    
    /**
     * 模糊查询标签
     */
    public static Cursor searchTags(SQLiteDatabase db, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键词为空，返回所有标签
            return db.query(TABLE_NAME,
                    new String[]{COL_ID, COL_TAGNAME},
                    null, null, null, null,
                    COL_TAGNAME + " ASC");
        } else {
            // 模糊查询
            return db.query(TABLE_NAME,
                    new String[]{COL_ID, COL_TAGNAME},
                    COL_TAGNAME + " LIKE ?",
                    new String[]{"%" + keyword + "%"},
                    null, null,
                    COL_TAGNAME + " ASC");
        }
    }
    
    /**
     * 查询所有标签
     */
    public static Cursor queryAll(SQLiteDatabase db) {
        return db.query(TABLE_NAME,
                new String[]{COL_ID, COL_TAGNAME},
                null, null, null, null,
                COL_TAGNAME + " ASC");
    }
    
    /**
     * 根据ID查询标签
     */
    public static Cursor queryById(SQLiteDatabase db, long tagId) {
        return db.query(TABLE_NAME,
                new String[]{COL_ID, COL_TAGNAME},
                COL_ID + "=?",
                new String[]{String.valueOf(tagId)},
                null, null, null);
    }
    
    /**
     * 从Cursor中获取标签名称列表
     */
    public static List<String> getTagNamesFromCursor(Cursor cursor) {
        List<String> tags = new ArrayList<>();
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndexOrThrow(COL_TAGNAME);
            while (cursor.moveToNext()) {
                tags.add(cursor.getString(nameIndex));
            }
        }
        return tags;
    }
    
    /**
     * 初始化Mock数据（可选）
     */
    public static void initMockData(SQLiteDatabase db) {
        // 检查是否已有数据
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, null, null, null, null, null, "1");
        if (cursor.getCount() > 0) {
            cursor.close();
            return; // 已有数据，不重复插入
        }
        cursor.close();
        
        // 可以在这里添加一些常用的Mock标签
        List<String> mockTags = new ArrayList<>();
        mockTags.add("美食");
        mockTags.add("旅行");
        mockTags.add("音乐");
        mockTags.add("运动");
        mockTags.add("摄影");
        mockTags.add("时尚");
        mockTags.add("科技");
        mockTags.add("搞笑");
        insertAll(db, mockTags);
    }
}

