package com.example.fake_tiktok.DAO;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类，负责数据库的创建和版本管理
 */
public class AppDatabase extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "temp_storage.db3";
    private static final int DATABASE_VERSION = 2; // 升级版本号，添加tag表
    
    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 启用外键约束
        db.execSQL("PRAGMA foreign_keys = ON");
        // 创建所有表
        DraftDao.createTable(db);
        DraftTextDao.createTable(db);
        DraftImageDao.createTable(db);
        DraftTagDao.createTable(db);
        DraftMentionDao.createTable(db);
        DraftLocationDao.createTable(db);
        FriendDao.createTable(db);
        GroupDao.createTable(db);
        GroupFriendDao.createTable(db);
        TagDao.createTable(db);
        
        // 初始化Mock数据
        FriendDao.initMockData(db);
        TagDao.initMockData(db);
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // 确保外键约束已启用
        db.execSQL("PRAGMA foreign_keys = ON");
        
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库升级逻辑
        android.util.Log.d("AppDatabase", "数据库升级：从版本 " + oldVersion + " 到 " + newVersion);
        
        if (oldVersion < 2) {
            // 版本2：添加tag表（标签库）
            try {
                TagDao.createTable(db);
                android.util.Log.d("AppDatabase", "已创建tag表");
                
                // 初始化Mock数据
                TagDao.initMockData(db);
                android.util.Log.d("AppDatabase", "已初始化tag表的Mock数据");
            } catch (Exception e) {
                android.util.Log.e("AppDatabase", "升级数据库时创建tag表失败", e);
            }
        }
    }
    
    /**
     * 获取数据库文件的完整路径（用于调试和查看）
     */
    public static String getDatabaseFilePath(Context context) {
        return context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }
}

