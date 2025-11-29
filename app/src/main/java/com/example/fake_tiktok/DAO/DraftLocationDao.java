package com.example.fake_tiktok.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * DraftLocation表的DAO类，负责draft_location表的所有数据库操作
 * 根据高德地图API返回的字段设计表结构
 */
public class DraftLocationDao {
    
    public static final String TABLE_NAME = "draft_location";
    public static final String COL_ID = "id";
    public static final String COL_DRAFT_ID = "draft_id";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_ADDRESS = "address";
    public static final String COL_COUNTRY = "country";
    public static final String COL_PROVINCE = "province";
    public static final String COL_CITY = "city";
    public static final String COL_DISTRICT = "district";
    public static final String COL_STREET = "street";
    public static final String COL_STREET_NUM = "street_num";
    public static final String COL_CITY_CODE = "city_code";
    public static final String COL_AD_CODE = "ad_code";
    public static final String COL_LOCATION_TYPE = "location_type";
    public static final String COL_ACCURACY = "accuracy";
    
    /**
     * 创建draft_location表
     * 所有字段都允许为空，防止某些环境下高德API返回不完全
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DRAFT_ID + " INTEGER NOT NULL, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL, " +
                COL_ADDRESS + " TEXT, " +
                COL_COUNTRY + " TEXT, " +
                COL_PROVINCE + " TEXT, " +
                COL_CITY + " TEXT, " +
                COL_DISTRICT + " TEXT, " +
                COL_STREET + " TEXT, " +
                COL_STREET_NUM + " TEXT, " +
                COL_CITY_CODE + " TEXT, " +
                COL_AD_CODE + " TEXT, " +
                COL_LOCATION_TYPE + " INTEGER, " +
                COL_ACCURACY + " REAL, " +
                "FOREIGN KEY (" + COL_DRAFT_ID + ") REFERENCES " + DraftDao.TABLE_NAME + "(" + DraftDao.COL_ID + ") ON DELETE CASCADE" +
                ")");
    }
    
    /**
     * 插入位置信息（使用LocationData对象）
     */
    public static long insert(SQLiteDatabase db, long draftId, LocationData locationData) {
        ContentValues values = new ContentValues();
        values.put(COL_DRAFT_ID, draftId);
        if (locationData.latitude != null) {
            values.put(COL_LATITUDE, locationData.latitude);
        }
        if (locationData.longitude != null) {
            values.put(COL_LONGITUDE, locationData.longitude);
        }
        if (locationData.address != null) {
            values.put(COL_ADDRESS, locationData.address);
        }
        if (locationData.country != null) {
            values.put(COL_COUNTRY, locationData.country);
        }
        if (locationData.province != null) {
            values.put(COL_PROVINCE, locationData.province);
        }
        if (locationData.city != null) {
            values.put(COL_CITY, locationData.city);
        }
        if (locationData.district != null) {
            values.put(COL_DISTRICT, locationData.district);
        }
        if (locationData.street != null) {
            values.put(COL_STREET, locationData.street);
        }
        if (locationData.streetNum != null) {
            values.put(COL_STREET_NUM, locationData.streetNum);
        }
        if (locationData.cityCode != null) {
            values.put(COL_CITY_CODE, locationData.cityCode);
        }
        if (locationData.adCode != null) {
            values.put(COL_AD_CODE, locationData.adCode);
        }
        if (locationData.locationType != null) {
            values.put(COL_LOCATION_TYPE, locationData.locationType);
        }
        if (locationData.accuracy != null) {
            values.put(COL_ACCURACY, locationData.accuracy);
        }
        return db.insert(TABLE_NAME, null, values);
    }
    
    /**
     * 插入位置信息（兼容旧版本，只保存基本字段）
     */
    public static long insert(SQLiteDatabase db, long draftId, double latitude, double longitude, String address) {
        LocationData locationData = new LocationData();
        locationData.latitude = latitude;
        locationData.longitude = longitude;
        locationData.address = address;
        return insert(db, draftId, locationData);
    }
    
    /**
     * 删除指定草稿的位置信息
     */
    public static int deleteByDraftId(SQLiteDatabase db, long draftId) {
        return db.delete(TABLE_NAME, COL_DRAFT_ID + "=?", 
                new String[]{String.valueOf(draftId)});
    }
    
    /**
     * 查询指定草稿的位置信息
     */
    public static Cursor queryByDraftId(SQLiteDatabase db, long draftId) {
        return db.query(TABLE_NAME,
                new String[]{
                    COL_LATITUDE, COL_LONGITUDE, COL_ADDRESS,
                    COL_COUNTRY, COL_PROVINCE, COL_CITY, COL_DISTRICT,
                    COL_STREET, COL_STREET_NUM, COL_CITY_CODE, COL_AD_CODE,
                    COL_LOCATION_TYPE, COL_ACCURACY
                },
                COL_DRAFT_ID + "=?",
                new String[]{String.valueOf(draftId)},
                null, null, null);
    }
    
    /**
     * 位置信息数据类
     * 所有字段都允许为空，防止某些环境下高德API返回不完全
     */
    public static class LocationData {
        public Double latitude;
        public Double longitude;
        public String address;
        public String country;
        public String province;
        public String city;
        public String district;
        public String street;
        public String streetNum;
        public String cityCode;
        public String adCode;
        public Integer locationType;
        public Float accuracy;
        
        /**
         * 无参构造函数，所有字段初始化为null
         */
        public LocationData() {
        }
        
        /**
         * 构造函数（兼容旧版本）
         */
        public LocationData(double latitude, double longitude, String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }
    }
    
    /**
     * 从Cursor中获取位置信息
     */
    public static LocationData getLocationFromCursor(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            LocationData locationData = new LocationData();
            
            // 基本位置信息
            int latitudeIndex = cursor.getColumnIndex(COL_LATITUDE);
            if (latitudeIndex >= 0 && !cursor.isNull(latitudeIndex)) {
                locationData.latitude = cursor.getDouble(latitudeIndex);
            }
            
            int longitudeIndex = cursor.getColumnIndex(COL_LONGITUDE);
            if (longitudeIndex >= 0 && !cursor.isNull(longitudeIndex)) {
                locationData.longitude = cursor.getDouble(longitudeIndex);
            }
            
            int addressIndex = cursor.getColumnIndex(COL_ADDRESS);
            if (addressIndex >= 0 && !cursor.isNull(addressIndex)) {
                locationData.address = cursor.getString(addressIndex);
            }
            
            // 地址详细信息
            int countryIndex = cursor.getColumnIndex(COL_COUNTRY);
            if (countryIndex >= 0 && !cursor.isNull(countryIndex)) {
                locationData.country = cursor.getString(countryIndex);
            }
            
            int provinceIndex = cursor.getColumnIndex(COL_PROVINCE);
            if (provinceIndex >= 0 && !cursor.isNull(provinceIndex)) {
                locationData.province = cursor.getString(provinceIndex);
            }
            
            int cityIndex = cursor.getColumnIndex(COL_CITY);
            if (cityIndex >= 0 && !cursor.isNull(cityIndex)) {
                locationData.city = cursor.getString(cityIndex);
            }
            
            int districtIndex = cursor.getColumnIndex(COL_DISTRICT);
            if (districtIndex >= 0 && !cursor.isNull(districtIndex)) {
                locationData.district = cursor.getString(districtIndex);
            }
            
            int streetIndex = cursor.getColumnIndex(COL_STREET);
            if (streetIndex >= 0 && !cursor.isNull(streetIndex)) {
                locationData.street = cursor.getString(streetIndex);
            }
            
            int streetNumIndex = cursor.getColumnIndex(COL_STREET_NUM);
            if (streetNumIndex >= 0 && !cursor.isNull(streetNumIndex)) {
                locationData.streetNum = cursor.getString(streetNumIndex);
            }
            
            int cityCodeIndex = cursor.getColumnIndex(COL_CITY_CODE);
            if (cityCodeIndex >= 0 && !cursor.isNull(cityCodeIndex)) {
                locationData.cityCode = cursor.getString(cityCodeIndex);
            }
            
            int adCodeIndex = cursor.getColumnIndex(COL_AD_CODE);
            if (adCodeIndex >= 0 && !cursor.isNull(adCodeIndex)) {
                locationData.adCode = cursor.getString(adCodeIndex);
            }
            
            int locationTypeIndex = cursor.getColumnIndex(COL_LOCATION_TYPE);
            if (locationTypeIndex >= 0 && !cursor.isNull(locationTypeIndex)) {
                locationData.locationType = cursor.getInt(locationTypeIndex);
            }
            
            int accuracyIndex = cursor.getColumnIndex(COL_ACCURACY);
            if (accuracyIndex >= 0 && !cursor.isNull(accuracyIndex)) {
                locationData.accuracy = cursor.getFloat(accuracyIndex);
            }
            
            return locationData;
        }
        return null;
    }
}

