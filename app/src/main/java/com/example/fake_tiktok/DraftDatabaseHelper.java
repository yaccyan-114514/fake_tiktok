package com.example.fake_tiktok;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.fake_tiktok.DAO.AppDatabase;
import com.example.fake_tiktok.DAO.DraftDao;
import com.example.fake_tiktok.DAO.DraftTextDao;
import com.example.fake_tiktok.DAO.DraftImageDao;
import com.example.fake_tiktok.DAO.DraftTagDao;
import com.example.fake_tiktok.DAO.DraftMentionDao;
import com.example.fake_tiktok.DAO.DraftLocationDao;

import java.util.List;

/**
 * 草稿数据库帮助类，使用DAO模式进行数据库操作
 */
public class DraftDatabaseHelper {
    
    private static final String DATABASE_NAME = "temp_storage.db3";
    private AppDatabase appDatabase;
    private Context context;
    
    public DraftDatabaseHelper(Context context) {
        this.context = context;
        this.appDatabase = new AppDatabase(context);
    }
    
    /**
     * 获取数据库文件的完整路径（用于调试和查看）
     */
    public static String getDatabaseFilePath(Context context) {
        return AppDatabase.getDatabaseFilePath(context);
    }
    
    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = appDatabase.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys = ON");
        return db;
    }
    
    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = appDatabase.getReadableDatabase();
        db.execSQL("PRAGMA foreign_keys = ON");
        return db;
    }
    
    /**
     * 保存草稿
     */
    public long saveDraft(Draft draft) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        
        android.util.Log.d("DraftDatabaseHelper", "========== 开始保存草稿 ==========");
        android.util.Log.d("DraftDatabaseHelper", "草稿ID: " + draft.getId());
        android.util.Log.d("DraftDatabaseHelper", "标题: " + (draft.getTitle() != null ? draft.getTitle() : "null"));
        android.util.Log.d("DraftDatabaseHelper", "内容长度: " + (draft.getContent() != null ? draft.getContent().length() : 0));
        android.util.Log.d("DraftDatabaseHelper", "图片数量: " + (draft.getImages() != null ? draft.getImages().size() : 0));
        
        try {
            long draftId;
            
            if (draft.getId() > 0) {
                // 更新现有草稿
                draftId = draft.getId();
                android.util.Log.d("DraftDatabaseHelper", "更新现有草稿，ID: " + draftId);
                DraftDao.update(db, draftId);
                // 删除旧数据
                deleteDraftData(db, draftId);
                android.util.Log.d("DraftDatabaseHelper", "已删除旧数据");
            } else {
                // 创建新草稿
                draftId = DraftDao.insert(db);
                android.util.Log.d("DraftDatabaseHelper", "创建新草稿，ID: " + draftId);
                if (draftId <= 0) {
                    throw new RuntimeException("创建草稿失败，返回ID: " + draftId);
                }
            }
            
            // 保存标题和文本内容（使用DraftTextDao）
            String title = draft.getTitle() != null ? draft.getTitle() : "";
            String content = draft.getContent() != null ? draft.getContent() : "";
            android.util.Log.d("DraftDatabaseHelper", "准备保存文本，标题长度: " + title.length() + ", 内容长度: " + content.length());
            if (!title.isEmpty() || !content.isEmpty()) {
                long textResult = DraftTextDao.insert(db, draftId, title, content);
                android.util.Log.d("DraftDatabaseHelper", "文本保存结果: " + textResult);
                if (textResult <= 0) {
                    android.util.Log.e("DraftDatabaseHelper", "文本保存失败，返回ID: " + textResult);
                }
            } else {
                android.util.Log.d("DraftDatabaseHelper", "标题和内容都为空，跳过文本保存");
            }
            
            // 保存图片
            if (draft.getImages() != null && !draft.getImages().isEmpty()) {
                android.util.Log.d("DraftDatabaseHelper", "【保存图片】准备保存 " + draft.getImages().size() + " 张图片");
                android.util.Log.d("DraftDatabaseHelper", "【保存图片】draftId=" + draftId);
                
                // 打印每张图片的URI信息
                for (int i = 0; i < draft.getImages().size(); i++) {
                    Uri uri = draft.getImages().get(i);
                    if (uri != null) {
                        String uriStr = uri.toString();
                        android.util.Log.d("DraftDatabaseHelper", "【保存图片】第" + (i+1) + "张: " + 
                                     (uriStr.length() > 100 ? uriStr.substring(0, 100) + "..." : uriStr));
                    } else {
                        android.util.Log.w("DraftDatabaseHelper", "【保存图片】第" + (i+1) + "张URI为null");
                    }
                }
                
                try {
                    DraftImageDao.insertAll(db, draftId, draft.getImages());
                    android.util.Log.d("DraftDatabaseHelper", "【保存图片】insertAll调用完成");
                    
                    // 验证图片是否真的保存了
                    android.database.Cursor verifyCursor = DraftImageDao.queryByDraftId(db, draftId);
                    int savedCount = verifyCursor != null ? verifyCursor.getCount() : 0;
                    if (verifyCursor != null) {
                        verifyCursor.close();
                    }
                    android.util.Log.d("DraftDatabaseHelper", "【验证图片】数据库中实际保存了 " + savedCount + " 张图片");
                    
                    if (savedCount == 0 && draft.getImages().size() > 0) {
                        android.util.Log.e("DraftDatabaseHelper", "【警告】图片保存失败，数据库中没有任何图片记录");
                    }
                } catch (Exception e) {
                    android.util.Log.e("DraftDatabaseHelper", "【异常】保存图片时发生异常", e);
                    android.util.Log.e("DraftDatabaseHelper", "【异常】类型: " + e.getClass().getName());
                    android.util.Log.e("DraftDatabaseHelper", "【异常】信息: " + e.getMessage());
                    if (e.getCause() != null) {
                        android.util.Log.e("DraftDatabaseHelper", "【异常】原因: " + e.getCause().getMessage());
                    }
                    e.printStackTrace();
                    // 不抛出异常，允许其他数据继续保存，但记录错误
                }
            } else {
                android.util.Log.d("DraftDatabaseHelper", "【保存图片】没有图片需要保存");
            }
            
            // 保存标签（只保存到draft_tag表，不添加到tag表）
            // tag表和draft_tag表不互通，tag表只用于标签库，draft_tag表只用于草稿
            if (draft.getTags() != null && !draft.getTags().isEmpty()) {
                DraftTagDao.insertAll(db, draftId, draft.getTags());
                android.util.Log.d("DraftDatabaseHelper", "已将 " + draft.getTags().size() + " 个标签保存到草稿");
            }
            
            // 保存@对象
            if (draft.getMentions() != null && !draft.getMentions().isEmpty()) {
                DraftMentionDao.insertAll(db, draftId, draft.getMentions());
            }
            
            // 保存位置信息
            if (draft.getLocation() != null) {
                // 尝试从Draft中获取完整的LocationData，如果没有则使用基本字段构建
                DraftLocationDao.LocationData locationData = draft.getLocationData();
                if (locationData == null) {
                    // 兼容旧版本，使用基本字段构建LocationData
                    locationData = new DraftLocationDao.LocationData();
                    locationData.latitude = draft.getLocation().latitude;
                    locationData.longitude = draft.getLocation().longitude;
                    locationData.address = draft.getLocationAddress();
                }
                DraftLocationDao.insert(db, draftId, locationData);
            }
            
            // 在提交事务前验证数据是否已插入
            android.util.Log.d("DraftDatabaseHelper", "【验证数据】开始验证保存的数据");
            
            Cursor verifyCursor = DraftDao.queryById(db, draftId);
            boolean draftExists = verifyCursor != null && verifyCursor.getCount() > 0;
            if (verifyCursor != null) {
                verifyCursor.close();
            }
            android.util.Log.d("DraftDatabaseHelper", "【验证数据】草稿记录存在: " + draftExists);
            
            // 验证文本数据
            Cursor textVerifyCursor = DraftTextDao.queryByDraftId(db, draftId);
            int textCount = textVerifyCursor != null ? textVerifyCursor.getCount() : 0;
            if (textVerifyCursor != null) {
                textVerifyCursor.close();
            }
            android.util.Log.d("DraftDatabaseHelper", "【验证数据】文本记录数量: " + textCount);
            
            // 验证图片数据
            Cursor imageVerifyCursor = DraftImageDao.queryByDraftId(db, draftId);
            int imageCount = imageVerifyCursor != null ? imageVerifyCursor.getCount() : 0;
            if (imageVerifyCursor != null) {
                imageVerifyCursor.close();
            }
            android.util.Log.d("DraftDatabaseHelper", "【验证数据】图片记录数量: " + imageCount);
            
            db.setTransactionSuccessful();
            android.util.Log.d("DraftDatabaseHelper", "========== 事务标记为成功 ==========");
            android.util.Log.d("DraftDatabaseHelper", "【保存完成】草稿ID: " + draftId);
            android.util.Log.d("DraftDatabaseHelper", "【保存完成】文本记录: " + textCount + " 条");
            android.util.Log.d("DraftDatabaseHelper", "【保存完成】图片记录: " + imageCount + " 条");
            
            // 显示数据库文件路径
            String dbPath = getDatabaseFilePath(context);
            android.util.Log.d("DraftDatabaseHelper", "【数据库路径】" + dbPath);
            
            return draftId;
        } catch (Exception e) {
            // 使用多种方式输出错误，确保能被看到
            System.out.println("========================================");
            System.out.println("【DraftDatabaseHelper】保存草稿失败！");
            System.out.println("异常类型: " + e.getClass().getName());
            System.out.println("异常信息: " + e.getMessage());
            System.out.println("========================================");
            e.printStackTrace(System.out);
            
            android.util.Log.e("DraftDatabaseHelper", "========== 保存草稿失败 ==========", e);
            android.util.Log.e("DraftDatabaseHelper", "异常类型: " + e.getClass().getName());
            android.util.Log.e("DraftDatabaseHelper", "异常信息: " + e.getMessage());
            android.util.Log.wtf("DraftDatabaseHelper", "【严重错误】保存草稿失败: " + e.getMessage(), e);
            if (e.getCause() != null) {
                android.util.Log.e("DraftDatabaseHelper", "异常原因: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw e;
        } finally {
            db.endTransaction();
            android.util.Log.d("DraftDatabaseHelper", "========== 事务结束 ==========");
        }
    }
    
    /**
     * 获取最新的草稿
     */
    public Draft getLatestDraft() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = DraftDao.queryLatest(db);
        
        if (cursor.moveToFirst()) {
            long draftId = cursor.getLong(cursor.getColumnIndexOrThrow(DraftDao.COL_ID));
            cursor.close();
            return getDraftById(draftId);
        }
        
        cursor.close();
        return null;
    }
    
    /**
     * 根据ID获取草稿
     */
    public Draft getDraftById(long draftId) {
        SQLiteDatabase db = getReadableDatabase();
        Draft draft = new Draft();
        draft.setId(draftId);
        
        // 获取标题和文本内容（使用DraftTextDao）
        Cursor textCursor = DraftTextDao.queryByDraftId(db, draftId);
        DraftTextDao.TextData textData = DraftTextDao.getTextDataFromCursor(textCursor);
        if (textData != null) {
            if (textData.title != null && !textData.title.isEmpty()) {
                draft.setTitle(textData.title);
            }
            if (textData.content != null && !textData.content.isEmpty()) {
                draft.setContent(textData.content);
            }
        }
        textCursor.close();
        
        // 获取图片
        Cursor imageCursor = DraftImageDao.queryByDraftId(db, draftId);
        List<Uri> images = DraftImageDao.getImageUrisFromCursor(imageCursor);
        draft.setImages(images);
        imageCursor.close();
        
        // 获取标签
        Cursor tagCursor = DraftTagDao.queryByDraftId(db, draftId);
        List<String> tags = DraftTagDao.getTagsFromCursor(tagCursor);
        draft.setTags(tags);
        tagCursor.close();
        
        // 获取@对象
        Cursor mentionCursor = DraftMentionDao.queryByDraftId(db, draftId);
        List<String> mentions = DraftMentionDao.getMentionsFromCursor(mentionCursor);
        draft.setMentions(mentions);
        mentionCursor.close();
        
        // 获取位置信息
        Cursor locationCursor = DraftLocationDao.queryByDraftId(db, draftId);
        DraftLocationDao.LocationData locationData = DraftLocationDao.getLocationFromCursor(locationCursor);
        if (locationData != null) {
            // 设置完整的位置数据
            draft.setLocationData(locationData);
            // 设置基本位置信息（兼容旧版本）
            if (locationData.latitude != null && locationData.longitude != null) {
                draft.setLocation(new Draft.Location(locationData.latitude, locationData.longitude));
            }
            // 设置地址（优先使用构建的地址，如果没有则使用完整地址）
            if (locationData.address != null && !locationData.address.isEmpty()) {
                draft.setLocationAddress(locationData.address);
            } else {
                // 如果没有完整地址，尝试构建地址
                StringBuilder addressBuilder = new StringBuilder();
                if (locationData.province != null && !locationData.province.isEmpty()) {
                    addressBuilder.append(locationData.province);
                }
                if (locationData.city != null && !locationData.city.isEmpty()) {
                    addressBuilder.append(locationData.city);
                }
                if (locationData.district != null && !locationData.district.isEmpty()) {
                    addressBuilder.append(locationData.district);
                }
                if (locationData.street != null && !locationData.street.isEmpty()) {
                    addressBuilder.append(locationData.street);
                }
                if (locationData.streetNum != null && !locationData.streetNum.isEmpty()) {
                    addressBuilder.append(locationData.streetNum);
                }
                draft.setLocationAddress(addressBuilder.toString());
            }
        }
        locationCursor.close();
        
        return draft;
    }
    
    /**
     * 删除草稿
     */
    public void deleteDraft(long draftId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        
        try {
            deleteDraftData(db, draftId);
            DraftDao.delete(db, draftId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    
    /**
     * 删除草稿的相关数据
     */
    private void deleteDraftData(SQLiteDatabase db, long draftId) {
        DraftTextDao.deleteByDraftId(db, draftId);
        DraftImageDao.deleteByDraftId(db, draftId);
        DraftTagDao.deleteByDraftId(db, draftId);
        DraftMentionDao.deleteByDraftId(db, draftId);
        DraftLocationDao.deleteByDraftId(db, draftId);
    }
    
    /**
     * 检查是否有草稿
     */
    public boolean hasDraft() {
        SQLiteDatabase db = getReadableDatabase();
        return DraftDao.hasDraft(db);
    }
    
    /**
     * 获取指定草稿的图片数量（用于验证保存是否成功）
     */
    public int getImageCount(long draftId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = DraftImageDao.queryByDraftId(db, draftId);
        int count = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) {
            cursor.close();
        }
        return count;
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        if (appDatabase != null) {
            appDatabase.close();
        }
    }
}
