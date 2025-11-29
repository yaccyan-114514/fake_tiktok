package com.example.fake_tiktok;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

    /**
     * 定位服务管理类
     */
public class LocationActivity {
    
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1002;
    
    // 声明AMapLocationClient类对象
    private AMapLocationClient locationClient;
    // 声明定位回调监听器
    private AMapLocationListener locationListener;
    // 声明AMapLocationClientOption对象
    private AMapLocationClientOption locationOption;
    private Activity activity;
    private LocationCallback callback;
    
    /**
     * 定位结果回调接口
     */
    public interface LocationCallback {
        /**
         * 定位成功回调
         * @param location 位置信息（经纬度）
         * @param address 街道级别地址
         * @param locationData 完整的位置数据（包含所有高德地图返回的字段）
         */
        void onLocationSuccess(Draft.Location location, String address, com.example.fake_tiktok.DAO.DraftLocationDao.LocationData locationData);
        
        /**
         * 定位失败回调
         * @param errorInfo 错误信息
         */
        void onLocationFailed(String errorInfo);
    }
    
    public LocationActivity(Activity activity, LocationCallback callback) {
        android.util.Log.d("LocationActivity", "========== LocationActivity构造函数开始 ==========");
        try {
            android.util.Log.d("LocationActivity", "参数检查: activity=" + (activity != null ? "非null" : "null") + 
                         ", callback=" + (callback != null ? "非null" : "null"));
            this.activity = activity;
            this.callback = callback;
            android.util.Log.d("LocationActivity", "准备调用initLocationServices");
            initLocationServices();
            if (locationClient == null) {
                android.util.Log.e("LocationActivity", "========== 初始化后locationClient仍为null ==========");
            } else {
                android.util.Log.d("LocationActivity", "========== 初始化成功，locationClient不为null ==========");
            }
        } catch (Exception e) {
            android.util.Log.e("LocationActivity", "========== LocationActivity构造函数异常 ==========", e);
            android.util.Log.e("LocationActivity", "异常类型: " + e.getClass().getName());
            android.util.Log.e("LocationActivity", "异常消息: " + e.getMessage());
            e.printStackTrace();
            locationClient = null;
        }
    }
    
    /**
     * 初始化高德地图定位服务
     */
    private void initLocationServices() {
        android.util.Log.d("LocationActivity", "initLocationServices方法开始执行");
        try {
            android.util.Log.d("LocationActivity", "准备创建AMapLocationClient，context=" + 
                         (activity != null && activity.getApplicationContext() != null ? "非null" : "null"));
            
            // 高德地图SDK隐私合规设置（必须在创建AMapLocationClient之前调用）
            // 1. 设置包含隐私政策，并展示用户授权弹窗
            // isContains: 隐私权政策是否包含高德开平隐私权政策 true是包含
            // isShow: 隐私权政策是否弹窗展示告知用户 true是展示
            android.util.Log.d("LocationActivity", "开始设置隐私合规");
            AMapLocationClient.updatePrivacyShow(activity.getApplicationContext(), true, true);
            
            // 2. 设置是否同意用户授权政策
            // isAgree: 隐私权政策是否取得用户同意 true是用户同意
            AMapLocationClient.updatePrivacyAgree(activity.getApplicationContext(), true);
            android.util.Log.d("LocationActivity", "隐私合规设置完成");
            
            // 3. 构造AMapLocationClient调用时要捕获异常信息（按照官方文档要求）
            android.util.Log.d("LocationActivity", "准备构造AMapLocationClient");
            locationClient = new AMapLocationClient(activity.getApplicationContext());
            android.util.Log.d("LocationActivity", "AMapLocationClient创建成功");
            
            // 初始化AMapLocationClientOption对象
            locationOption = new AMapLocationClientOption();
            
            // 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

            locationOption.setInterval(2000);
            
            // 设置是否返回地址信息（默认返回地址信息）
            locationOption.setNeedAddress(true);
            // 设置是否只定位一次
            locationOption.setOnceLocation(true);
            // 设置是否强制刷新WIFI，默认为强制刷新
            locationOption.setWifiActiveScan(true);
            // 设置是否允许模拟位置，默认为false，不允许模拟位置
            locationOption.setMockEnable(false);
            
            // 给定位客户端对象设置定位参数（使用前进行空指针判断，按照官方文档要求）
            if (locationClient != null) {
                locationClient.setLocationOption(locationOption);
            } else {
                android.util.Log.e("LocationActivity", "locationClient为null，无法设置定位参数");
                throw new RuntimeException("AMapLocationClient初始化失败");
            }

            locationListener = new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation amapLocation) {
                    // 按照官方文档，首先判断AMapLocation对象不为空
                    if (amapLocation != null) {
                        // 当定位错误码类型为0时定位成功
                        if (amapLocation.getErrorCode() == 0) {
                            // 定位成功，解析amapLocation获取相应内容
                            
                            // 获取定位结果来源
                            int locationType = amapLocation.getLocationType();
                            // 获取纬度
                            double latitude = amapLocation.getLatitude();
                            // 获取经度
                            double longitude = amapLocation.getLongitude();
                            // 获取精度信息
                            float accuracy = amapLocation.getAccuracy();
                            
                            Draft.Location location = new Draft.Location(latitude, longitude);
                            
                            // 获取地址信息（如果option中设置isNeedAddress为false，则没有此结果）
                            String address = amapLocation.getAddress();
                            // 获取国家信息
                            String country = amapLocation.getCountry();
                            // 获取省信息
                            String province = amapLocation.getProvince();
                            // 获取城市信息
                            String city = amapLocation.getCity();
                            // 获取城区信息
                            String district = amapLocation.getDistrict();
                            // 获取街道信息
                            String street = amapLocation.getStreet();
                            // 获取街道门牌号信息
                            String streetNum = amapLocation.getStreetNum();
                            // 获取城市编码
                            String cityCode = amapLocation.getCityCode();
                            // 获取地区编码
                            String adCode = amapLocation.getAdCode();
                            
                            // 构建完整的LocationData对象，包含所有高德地图返回的字段
                            com.example.fake_tiktok.DAO.DraftLocationDao.LocationData locationData = 
                                    new com.example.fake_tiktok.DAO.DraftLocationDao.LocationData();
                            locationData.latitude = latitude;
                            locationData.longitude = longitude;
                            locationData.address = address; // 完整地址
                            locationData.country = country;
                            locationData.province = province;
                            locationData.city = city;
                            locationData.district = district;
                            locationData.street = street;
                            locationData.streetNum = streetNum;
                            locationData.cityCode = cityCode;
                            locationData.adCode = adCode;
                            locationData.locationType = locationType;
                            locationData.accuracy = accuracy;
                            
                            // 构建街道级别的地址
                            StringBuilder addressBuilder = new StringBuilder();
                            if (province != null && !province.isEmpty()) {
                                addressBuilder.append(province);
                            }
                            if (city != null && !city.isEmpty()) {
                                addressBuilder.append(city);
                            }
                            if (district != null && !district.isEmpty()) {
                                addressBuilder.append(district);
                            }
                            if (street != null && !street.isEmpty()) {
                                addressBuilder.append(street);
                            }
                            if (streetNum != null && !streetNum.isEmpty()) {
                                addressBuilder.append(streetNum);
                            }
                            
                            // 如果地址为空，使用完整地址
                            if (addressBuilder.length() == 0 && address != null && !address.isEmpty()) {
                                addressBuilder.append(address);
                            }
                            
                            String locationAddress = addressBuilder.toString();
                            
                            android.util.Log.d("LocationActivity", "定位成功 - 类型:" + locationType + 
                                             ", 纬度:" + latitude + ", 经度:" + longitude + 
                                             ", 精度:" + accuracy + ", 地址:" + locationAddress);
                            
                            // 定位成功后停止定位（按照官方文档，停止定位后本地定位服务并不会被销毁）
                            if (locationClient != null) {
                                locationClient.stopLocation();
                            }
                            
                            // 通过回调返回定位结果（包含完整的位置数据）
                            if (callback != null) {
                                callback.onLocationSuccess(location, locationAddress, locationData);
                            }
                        } else {
                            // 定位失败时，可通过ErrCode（错误码）信息来确定失败的原因
                            // errInfo是错误信息，详见错误码表
                            int errorCode = amapLocation.getErrorCode();
                            String errorInfo = amapLocation.getErrorInfo();
                            
                            android.util.Log.e("LocationActivity", "location Error, ErrCode:" + 
                                             errorCode + ", errInfo:" + errorInfo);
                            
                            // 根据错误码提供更友好的错误提示
                            String userFriendlyError = getErrorDescription(errorCode, errorInfo);
                            
                            if (callback != null) {
                                callback.onLocationFailed(userFriendlyError);
                            }
                        }
                    } else {
                        android.util.Log.e("LocationActivity", "amapLocation为null");
                        if (callback != null) {
                            callback.onLocationFailed("无法获取位置信息");
                        }
                    }
                }
            };
            
            // 设置定位回调监听（按照官方文档，在初始化后立即设置）
            // 使用前进行空指针判断，按照官方文档要求
            if (locationClient != null) {
                locationClient.setLocationListener(locationListener);
            } else {
                android.util.Log.e("LocationActivity", "locationClient为null，无法设置监听器");
                throw new RuntimeException("AMapLocationClient初始化失败");
            }
            
            android.util.Log.d("LocationActivity", "高德地图定位服务初始化成功");
        } catch (Exception e) {
            android.util.Log.e("LocationActivity", "高德地图定位服务初始化失败", e);
            android.util.Log.e("LocationActivity", "异常类型: " + e.getClass().getName());
            android.util.Log.e("LocationActivity", "异常消息: " + e.getMessage());
            e.printStackTrace();
            locationClient = null; // 确保初始化失败时locationClient为null
            android.util.Log.e("LocationActivity", "初始化失败后，locationClient已设置为null");
        }
    }
    
    /**
     * 请求定位权限并开始定位
     */
    public void requestLocation() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            getCurrentLocation();
        }
    }
    
    /**
     * 处理权限请求结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(activity, "需要位置权限才能获取地址", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 根据错误码获取用户友好的错误描述
     */
    private String getErrorDescription(int errorCode, String errorInfo) {
        switch (errorCode) {
            case 0:
                return "定位成功";
            case 1:
                return "重要参数为空，请检查定位服务是否开启";
            case 2:
                return "WIFI信息不足，请检查WIFI是否开启";
            case 3:
                return "请求被拒绝，请检查权限设置";
            case 4:
                return "定位服务启动失败，请检查定位服务是否开启";
            case 5:
                return "定位服务返回异常，请稍后重试";
            case 6:
                return "定位结果错误，请检查：\n1. 定位服务是否开启\n2. 网络连接是否正常\n3. 是否授予位置权限";
            case 7:
                return "KEY鉴权失败，请检查API Key配置";
            case 8:
                return "定位服务异常，请稍后重试";
            case 9:
                return "定位初始化失败，请重启应用";
            case 10:
                return "定位客户端启动失败，请检查定位服务";
            case 11:
                return "定位请求超时，请检查网络连接";
            case 12:
                return "定位请求被拦截，请检查权限设置";
            case 13:
                return "定位服务启动失败，请检查定位服务是否开启";
            case 14:
                return "定位服务异常，请稍后重试";
            case 15:
                return "定位服务不可用，请检查定位服务是否开启";
            case 16:
                return "定位服务异常，请稍后重试";
            case 18:
                return "定位服务异常，请稍后重试";
            default:
                return "定位失败（错误码:" + errorCode + "），" + errorInfo;
        }
    }
    
    /**
     * 检查定位服务是否开启
     */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        android.util.Log.d("LocationActivity", "GPS开启状态: " + gpsEnabled + ", 网络定位开启状态: " + networkEnabled);
        return gpsEnabled || networkEnabled;
    }
    
    /**
     * 获取当前位置
     */
    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.e("LocationActivity", "缺少位置权限");
            return;
        }
        
        // 检查定位服务是否开启
        if (!isLocationEnabled()) {
            android.util.Log.e("LocationActivity", "定位服务未开启");
            Toast.makeText(activity, "请开启定位服务（GPS或网络定位）", Toast.LENGTH_LONG).show();
            // 可以引导用户打开定位设置
            try {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            } catch (Exception e) {
                android.util.Log.e("LocationActivity", "无法打开定位设置", e);
            }
            return;
        }
        
        if (locationClient == null) {
            android.util.Log.e("LocationActivity", "locationClient为null，尝试重新初始化");
            // 尝试重新初始化
            initLocationServices();
            if (locationClient == null) {
                Toast.makeText(activity, "定位服务未初始化", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // 使用locationClient时进行空指针判断，按照官方文档要求
        if (locationClient != null) {
            // 确保监听器已设置（虽然初始化时已设置，但为了安全再次设置）
            if (locationListener != null) {
                locationClient.setLocationListener(locationListener);
            }
            
            android.util.Log.d("LocationActivity", "准备启动定位");
            // 启动定位
            locationClient.startLocation();
        } else {
            android.util.Log.e("LocationActivity", "locationClient为null，无法启动定位");
            Toast.makeText(activity, "定位服务未初始化", Toast.LENGTH_SHORT).show();
        }
    }
    public void stopLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
            android.util.Log.d("LocationActivity", "定位已停止");
        }
    }

    public void destroy() {
        if (locationClient != null) {
            // 先停止定位
            locationClient.stopLocation();
            // 销毁定位客户端，同时销毁本地定位服务
            locationClient.onDestroy();
            locationClient = null;
            android.util.Log.d("LocationActivity", "定位客户端已销毁");
        }
        locationListener = null;
        locationOption = null;
        activity = null;
        callback = null;
    }
}
