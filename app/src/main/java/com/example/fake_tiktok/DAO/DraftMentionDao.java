package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * DraftMention表的DAO类，负责draft_mention表的所有数据库操作
 */
public class DraftMentionDao {
    
    public static final String TABLE_NAME = "draft_mention";
    public static final String COL_ID = "id";
    public static final String COL_DRAFT_ID = "draft_id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_USERNAME = "username";
    
    /**
     * 创建draft_mention表
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DRAFT_ID + " INTEGER NOT NULL, " +
                COL_USER_ID + " TEXT NOT NULL, " +
                COL_USERNAME + " TEXT, " +
                "FOREIGN KEY (" + COL_DRAFT_ID + ") REFERENCES " + DraftDao.TABLE_NAME + "(" + DraftDao.COL_ID + ") ON DELETE CASCADE" +
                ")");
    }
    
    /**
     * 插入@对象
     */
    public static long insert(SQLiteDatabase db, long draftId, String userId, String username) {
        ContentValues values = new ContentValues();
        values.put(COL_DRAFT_ID, draftId);
        values.put(COL_USER_ID, userId);
        values.put(COL_USERNAME, username);
        return db.insert(TABLE_NAME, null, values);
    }
    
    /**
     * 批量插入@对象列表
     */
    public static void insertAll(SQLiteDatabase db, long draftId, List<String> mentions) {
        if (mentions != null && !mentions.isEmpty()) {
            for (String mention : mentions) {
                if (mention != null && !mention.isEmpty()) {
                    insert(db, draftId, mention, mention);
                }
            }
        }
    }
    
    /**
     * 删除指定草稿的所有@对象
     */
    public static int deleteByDraftId(SQLiteDatabase db, long draftId) {
        return db.delete(TABLE_NAME, COL_DRAFT_ID + "=?", 
                new String[]{String.valueOf(draftId)});
    }
    
    /**
     * 查询指定草稿的所有@对象
     */
    public static Cursor queryByDraftId(SQLiteDatabase db, long draftId) {
        return db.query(TABLE_NAME,
                new String[]{COL_USERNAME},
                COL_DRAFT_ID + "=?",
                new String[]{String.valueOf(draftId)},
                null, null, null);
    }
    
    /**
     * 从Cursor中获取@对象列表
     */
    public static List<String> getMentionsFromCursor(Cursor cursor) {
        List<String> mentions = new ArrayList<>();
        if (cursor != null) {
            int usernameIndex = cursor.getColumnIndexOrThrow(COL_USERNAME);
            while (cursor.moveToNext()) {
                mentions.add(cursor.getString(usernameIndex));
            }
        }
        return mentions;
    }
}

