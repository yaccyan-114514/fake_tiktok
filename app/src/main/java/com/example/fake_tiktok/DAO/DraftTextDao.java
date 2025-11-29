package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * DraftText表的DAO类，负责draft_text表的所有数据库操作
 */
public class DraftTextDao {
    
    public static final String TABLE_NAME = "draft_text";
    public static final String COL_ID = "id";
    public static final String COL_DRAFT_ID = "draft_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    
    /**
     * 创建draft_text表
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DRAFT_ID + " INTEGER NOT NULL, " +
                COL_TITLE + " TEXT, " +
                COL_CONTENT + " TEXT, " +
                "FOREIGN KEY (" + COL_DRAFT_ID + ") REFERENCES " + DraftDao.TABLE_NAME + "(" + DraftDao.COL_ID + ") ON DELETE CASCADE" +
                ")");
    }
    
    /**
     * 插入标题和文本内容
     */
    public static long insert(SQLiteDatabase db, long draftId, String title, String content) {
        try {
            ContentValues values = new ContentValues();
            values.put(COL_DRAFT_ID, draftId);
            values.put(COL_TITLE, title != null ? title : "");
            values.put(COL_CONTENT, content != null ? content : "");
            long result = db.insert(TABLE_NAME, null, values);
            android.util.Log.d("DraftTextDao", "插入文本，draftId: " + draftId + ", 返回ID: " + result);
            if (result == -1) {
                android.util.Log.e("DraftTextDao", "插入文本失败，draftId: " + draftId);
            }
            return result;
        } catch (Exception e) {
            android.util.Log.e("DraftTextDao", "插入文本异常", e);
            android.util.Log.e("DraftTextDao", "异常信息: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 插入文本内容（兼容旧版本，将内容作为标题）
     */
    public static long insert(SQLiteDatabase db, long draftId, String content) {
        return insert(db, draftId, "", content);
    }
    
    /**
     * 删除指定草稿的所有文本内容
     */
    public static int deleteByDraftId(SQLiteDatabase db, long draftId) {
        return db.delete(TABLE_NAME, COL_DRAFT_ID + "=?", 
                new String[]{String.valueOf(draftId)});
    }
    
    /**
     * 查询指定草稿的文本内容
     */
    public static Cursor queryByDraftId(SQLiteDatabase db, long draftId) {
        return db.query(TABLE_NAME,
                new String[]{COL_TITLE, COL_CONTENT},
                COL_DRAFT_ID + "=?",
                new String[]{String.valueOf(draftId)},
                null, null, null);
    }
    
    /**
     * 从Cursor中获取文本内容（兼容旧版本）
     */
    public static String getContentFromCursor(Cursor cursor) {
        TextData data = getTextDataFromCursor(cursor);
        return data != null ? data.content : null;
    }
    
    /**
     * 从Cursor中获取标题和文本内容
     */
    public static TextData getTextDataFromCursor(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            String title = null;
            String content = null;
            try {
                int titleIndex = cursor.getColumnIndex(COL_TITLE);
                if (titleIndex >= 0) {
                    title = cursor.getString(titleIndex);
                }
            } catch (Exception e) {
                // 兼容旧版本数据库，可能没有title字段
            }
            try {
                int contentIndex = cursor.getColumnIndexOrThrow(COL_CONTENT);
                content = cursor.getString(contentIndex);
            } catch (Exception e) {
                // 忽略
            }
            return new TextData(title, content);
        }
        return null;
    }
    
    /**
     * 文本数据类
     */
    public static class TextData {
        public String title;
        public String content;
        
        public TextData(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
}

