package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * DraftTag表的DAO类，负责draft_tag表的所有数据库操作
 */
public class DraftTagDao {
    
    public static final String TABLE_NAME = "draft_tag";
    public static final String COL_ID = "id";
    public static final String COL_DRAFT_ID = "draft_id";
    public static final String COL_TAG = "tag";
    
    /**
     * 创建draft_tag表
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DRAFT_ID + " INTEGER NOT NULL, " +
                COL_TAG + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COL_DRAFT_ID + ") REFERENCES " + DraftDao.TABLE_NAME + "(" + DraftDao.COL_ID + ") ON DELETE CASCADE" +
                ")");
    }
    
    /**
     * 插入标签
     */
    public static long insert(SQLiteDatabase db, long draftId, String tag) {
        ContentValues values = new ContentValues();
        values.put(COL_DRAFT_ID, draftId);
        values.put(COL_TAG, tag);
        return db.insert(TABLE_NAME, null, values);
    }
    
    /**
     * 批量插入标签列表
     */
    public static void insertAll(SQLiteDatabase db, long draftId, List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            android.util.Log.d("DraftTagDao", "准备保存 " + tags.size() + " 个标签");
            int successCount = 0;
            int failCount = 0;
            
            for (String tag : tags) {
                try {
                    if (tag != null && !tag.isEmpty()) {
                        long result = insert(db, draftId, tag);
                        if (result > 0) {
                            successCount++;
                            android.util.Log.d("DraftTagDao", "成功保存标签: " + tag);
                        } else {
                            failCount++;
                            android.util.Log.e("DraftTagDao", "保存标签失败，返回ID: " + result + ", 标签: " + tag);
                        }
                    } else {
                        android.util.Log.w("DraftTagDao", "标签为空，跳过");
                    }
                } catch (Exception e) {
                    failCount++;
                    android.util.Log.e("DraftTagDao", "保存标签时发生异常: " + tag, e);
                    // 继续保存其他标签，不中断
                }
            }
            
            android.util.Log.d("DraftTagDao", "标签保存完成，成功: " + successCount + " 个，失败: " + failCount + " 个");
        } else {
            android.util.Log.d("DraftTagDao", "标签列表为空，无需保存");
        }
    }
    
    /**
     * 删除指定草稿的所有标签
     */
    public static int deleteByDraftId(SQLiteDatabase db, long draftId) {
        return db.delete(TABLE_NAME, COL_DRAFT_ID + "=?", 
                new String[]{String.valueOf(draftId)});
    }
    
    /**
     * 查询指定草稿的所有标签
     */
    public static Cursor queryByDraftId(SQLiteDatabase db, long draftId) {
        return db.query(TABLE_NAME,
                new String[]{COL_TAG},
                COL_DRAFT_ID + "=?",
                new String[]{String.valueOf(draftId)},
                null, null, null);
    }
    
    /**
     * 从Cursor中获取标签列表
     */
    public static List<String> getTagsFromCursor(Cursor cursor) {
        List<String> tags = new ArrayList<>();
        if (cursor != null) {
            try {
                int tagIndex = cursor.getColumnIndexOrThrow(COL_TAG);
                while (cursor.moveToNext()) {
                    try {
                        String tag = cursor.getString(tagIndex);
                        if (tag != null && !tag.isEmpty()) {
                            tags.add(tag);
                            android.util.Log.d("DraftTagDao", "成功加载标签: " + tag);
                        } else {
                            android.util.Log.w("DraftTagDao", "标签字符串为空，跳过");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("DraftTagDao", "读取标签时发生异常，跳过", e);
                        // 继续处理下一个标签，不中断
                    }
                }
            } catch (IllegalArgumentException e) {
                android.util.Log.e("DraftTagDao", "Cursor中找不到tag列", e);
            } catch (Exception e) {
                android.util.Log.e("DraftTagDao", "从Cursor获取标签列表时发生异常", e);
            }
        }
        android.util.Log.d("DraftTagDao", "从数据库加载了 " + tags.size() + " 个标签");
        return tags;
    }
}

