package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * DraftImage表的DAO类，负责draft_image表的所有数据库操作
 */
public class DraftImageDao {
    
    public static final String TABLE_NAME = "draft_image";
    public static final String COL_ID = "id";
    public static final String COL_DRAFT_ID = "draft_id";
    public static final String COL_IMAGE_URI = "image_uri";
    public static final String COL_SORT_ORDER = "sort_order";
    
    /**
     * 创建draft_image表
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DRAFT_ID + " INTEGER NOT NULL, " +
                COL_IMAGE_URI + " TEXT NOT NULL, " +
                COL_SORT_ORDER + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (" + COL_DRAFT_ID + ") REFERENCES " + DraftDao.TABLE_NAME + "(" + DraftDao.COL_ID + ") ON DELETE CASCADE" +
                ")");
    }
    
    /**
     * 插入图片URI
     */
    public static long insert(SQLiteDatabase db, long draftId, String imageUri, int sortOrder) {
        try {
            if (imageUri == null || imageUri.isEmpty()) {
                android.util.Log.e("DraftImageDao", "【错误】图片URI为空，无法保存");
                return -1;
            }
            
            // 记录详细信息
            android.util.Log.d("DraftImageDao", "【插入图片】draftId=" + draftId + ", sortOrder=" + sortOrder);
            android.util.Log.d("DraftImageDao", "【插入图片】URI长度=" + imageUri.length() + ", URI前50字符=" + 
                         (imageUri.length() > 50 ? imageUri.substring(0, 50) + "..." : imageUri));
            
            ContentValues values = new ContentValues();
            values.put(COL_DRAFT_ID, draftId);
            values.put(COL_IMAGE_URI, imageUri);
            values.put(COL_SORT_ORDER, sortOrder);
            
            android.util.Log.d("DraftImageDao", "【插入图片】准备调用db.insert()");
            long result = db.insert(TABLE_NAME, null, values);
            android.util.Log.d("DraftImageDao", "【插入图片】db.insert()返回结果: " + result);
            
            if (result == -1) {
                android.util.Log.e("DraftImageDao", "【错误】保存图片失败，返回-1");
                android.util.Log.e("DraftImageDao", "【错误】draftId=" + draftId + ", URI=" + imageUri);
                // 尝试获取更多错误信息
                try {
                    // 检查表是否存在
                    android.database.sqlite.SQLiteException testException = null;
                    try {
                        android.database.Cursor testCursor = db.query(TABLE_NAME, new String[]{COL_ID}, 
                                null, null, null, null, null, "1");
                        if (testCursor != null) {
                            testCursor.close();
                            android.util.Log.d("DraftImageDao", "【检查】表存在且可查询");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("DraftImageDao", "【错误】表查询失败", e);
                    }
                } catch (Exception e) {
                    android.util.Log.e("DraftImageDao", "【错误】检查表时异常", e);
                }
            } else {
                android.util.Log.d("DraftImageDao", "【成功】保存图片成功，ID: " + result);
            }
            return result;
        } catch (android.database.sqlite.SQLiteException e) {
            android.util.Log.e("DraftImageDao", "【SQLite异常】插入图片URI失败", e);
            android.util.Log.e("DraftImageDao", "【SQLite异常】错误信息: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } catch (Exception e) {
            android.util.Log.e("DraftImageDao", "【异常】插入图片URI失败", e);
            android.util.Log.e("DraftImageDao", "【异常】类型: " + e.getClass().getName());
            android.util.Log.e("DraftImageDao", "【异常】信息: " + e.getMessage());
            if (e.getCause() != null) {
                android.util.Log.e("DraftImageDao", "【异常】原因: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * 批量插入图片URI列表
     * 即使部分图片保存失败，也会继续保存其他图片
     */
    public static void insertAll(SQLiteDatabase db, long draftId, List<Uri> imageUris) {
        if (imageUris == null || imageUris.isEmpty()) {
            android.util.Log.d("DraftImageDao", "图片列表为空，无需保存");
            return;
        }
        
        android.util.Log.d("DraftImageDao", "开始批量保存图片，数量: " + imageUris.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < imageUris.size(); i++) {
            try {
                Uri imageUri = imageUris.get(i);
                if (imageUri != null) {
                    String uriString = imageUri.toString();
                    if (uriString != null && !uriString.isEmpty()) {
                        // 限制URI字符串长度，防止SQLite错误
                        if (uriString.length() > 2000) {
                            android.util.Log.w("DraftImageDao", "第 " + (i + 1) + " 张图片URI过长(" + uriString.length() + "字符)，截断");
                            uriString = uriString.substring(0, 2000);
                        }
                        
                        long result = insert(db, draftId, uriString, i);
                        if (result == -1) {
                            failCount++;
                            android.util.Log.e("DraftImageDao", "保存第 " + (i + 1) + " 张图片失败: " + uriString);
                            // 继续保存其他图片，不中断
                        } else {
                            successCount++;
                        }
                    } else {
                        failCount++;
                        android.util.Log.w("DraftImageDao", "第 " + (i + 1) + " 张图片URI为空，跳过");
                    }
                } else {
                    failCount++;
                    android.util.Log.w("DraftImageDao", "第 " + (i + 1) + " 张图片Uri对象为null，跳过");
                }
            } catch (Exception e) {
                failCount++;
                android.util.Log.e("DraftImageDao", "保存第 " + (i + 1) + " 张图片时发生异常", e);
                android.util.Log.e("DraftImageDao", "异常信息: " + e.getMessage());
                // 继续保存其他图片，不中断
            }
        }
        
        android.util.Log.d("DraftImageDao", "批量保存图片完成，成功: " + successCount + " 张，失败: " + failCount + " 张");
        
        if (failCount > 0 && successCount == 0) {
            // 如果所有图片都保存失败，记录警告但不抛出异常
            android.util.Log.w("DraftImageDao", "所有图片保存失败，但继续保存其他数据");
        }
    }
    
    /**
     * 删除指定草稿的所有图片
     */
    public static int deleteByDraftId(SQLiteDatabase db, long draftId) {
        return db.delete(TABLE_NAME, COL_DRAFT_ID + "=?", 
                new String[]{String.valueOf(draftId)});
    }
    
    /**
     * 查询指定草稿的所有图片URI
     */
    public static Cursor queryByDraftId(SQLiteDatabase db, long draftId) {
        return db.query(TABLE_NAME,
                new String[]{COL_IMAGE_URI},
                COL_DRAFT_ID + "=?",
                new String[]{String.valueOf(draftId)},
                null, null,
                COL_SORT_ORDER + " ASC");
    }
    
    /**
     * 从Cursor中获取图片URI列表
     */
    public static List<Uri> getImageUrisFromCursor(Cursor cursor) {
        List<Uri> images = new ArrayList<>();
        if (cursor != null) {
            try {
                int uriIndex = cursor.getColumnIndexOrThrow(COL_IMAGE_URI);
                while (cursor.moveToNext()) {
                    try {
                        String uriString = cursor.getString(uriIndex);
                        if (uriString != null && !uriString.isEmpty()) {
                            // 验证 URI 字符串是否有效
                            Uri uri = Uri.parse(uriString);
                            if (uri != null) {
                                images.add(uri);
                                android.util.Log.d("DraftImageDao", "成功解析图片URI: " + uriString);
                            } else {
                                android.util.Log.w("DraftImageDao", "URI解析返回null: " + uriString);
                            }
                        } else {
                            android.util.Log.w("DraftImageDao", "图片URI字符串为空，跳过");
                        }
                    } catch (IllegalArgumentException e) {
                        android.util.Log.e("DraftImageDao", "URI字符串格式无效，跳过: " + e.getMessage());
                        // 继续处理下一个URI，不中断
                    } catch (Exception e) {
                        android.util.Log.e("DraftImageDao", "解析图片URI时发生异常，跳过", e);
                        // 继续处理下一个URI，不中断
                    }
                }
            } catch (IllegalArgumentException e) {
                android.util.Log.e("DraftImageDao", "Cursor中找不到image_uri列", e);
            } catch (Exception e) {
                android.util.Log.e("DraftImageDao", "从Cursor获取图片URI列表时发生异常", e);
            }
        }
        android.util.Log.d("DraftImageDao", "从数据库加载了 " + images.size() + " 张图片");
        return images;
    }
}

