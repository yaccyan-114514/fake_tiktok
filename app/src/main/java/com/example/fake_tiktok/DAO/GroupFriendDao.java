package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * GroupFriend表的DAO类，负责group_friend表的所有数据库操作（连接表）
 */
public class GroupFriendDao {
    
    public static final String TABLE_NAME = "draft_group_friend";
    public static final String COL_ID = "id";
    public static final String COL_GROUP_ID = "group_id";
    public static final String COL_FRIEND_ID = "friend_id";
    
    /**
     * 创建group_friend表
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GROUP_ID + " INTEGER NOT NULL, " +
                COL_FRIEND_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COL_GROUP_ID + ") REFERENCES [" + GroupDao.TABLE_NAME + "](" + GroupDao.COL_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COL_FRIEND_ID + ") REFERENCES " + FriendDao.TABLE_NAME + "(" + FriendDao.COL_ID + ") ON DELETE CASCADE" +
                ")");
    }
    
    /**
     * 插入分组-好友关联
     */
    public static long insert(SQLiteDatabase db, long groupId, long friendId) {
        ContentValues values = new ContentValues();
        values.put(COL_GROUP_ID, groupId);
        values.put(COL_FRIEND_ID, friendId);
        return db.insert(TABLE_NAME, null, values);
    }
    
    /**
     * 查询指定分组的所有好友ID
     */
    public static Cursor queryFriendsByGroupId(SQLiteDatabase db, long groupId) {
        return db.query(TABLE_NAME,
                new String[]{COL_FRIEND_ID},
                COL_GROUP_ID + "=?",
                new String[]{String.valueOf(groupId)},
                null, null, null);
    }
    
    /**
     * 查询指定好友所在的所有分组ID
     */
    public static Cursor queryGroupsByFriendId(SQLiteDatabase db, long friendId) {
        return db.query(TABLE_NAME,
                new String[]{COL_GROUP_ID},
                COL_FRIEND_ID + "=?",
                new String[]{String.valueOf(friendId)},
                null, null, null);
    }
    
    /**
     * 删除指定分组的关联
     */
    public static int deleteByGroupId(SQLiteDatabase db, long groupId) {
        return db.delete(TABLE_NAME, COL_GROUP_ID + "=?", 
                new String[]{String.valueOf(groupId)});
    }
    
    /**
     * 删除指定好友的关联
     */
    public static int deleteByFriendId(SQLiteDatabase db, long friendId) {
        return db.delete(TABLE_NAME, COL_FRIEND_ID + "=?", 
                new String[]{String.valueOf(friendId)});
    }
    
    /**
     * 从Cursor中获取好友ID列表
     */
    public static List<Long> getFriendIdsFromCursor(Cursor cursor) {
        List<Long> friendIds = new ArrayList<>();
        if (cursor != null) {
            int friendIdIndex = cursor.getColumnIndexOrThrow(COL_FRIEND_ID);
            while (cursor.moveToNext()) {
                friendIds.add(cursor.getLong(friendIdIndex));
            }
        }
        return friendIds;
    }
    
    /**
     * 从Cursor中获取分组ID列表
     */
    public static List<Long> getGroupIdsFromCursor(Cursor cursor) {
        List<Long> groupIds = new ArrayList<>();
        if (cursor != null) {
            int groupIdIndex = cursor.getColumnIndexOrThrow(COL_GROUP_ID);
            while (cursor.moveToNext()) {
                groupIds.add(cursor.getLong(groupIdIndex));
            }
        }
        return groupIds;
    }
}

