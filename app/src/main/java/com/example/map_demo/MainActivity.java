package com.example.map_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private TextView lon,lat,nowAddress,type;
    private Button GPS, NET,PASSIVE;
    private Double longitude, latitude;
    private Switch continuousUpdateSwitch;
    private boolean isContinuousUpdateEnabled = false;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        initview();

        // 点击事件的监听
        GPS.setOnClickListener(this);
        NET.setOnClickListener(this);
        PASSIVE.setOnClickListener(this);

        // 初始化LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 检查GPS权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 设置 Switch 的监听器
        continuousUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isContinuousUpdateEnabled = isChecked;
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "持续更新模式已开启", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "持续更新模式已关闭，获取一次位置后移除位置监听器。", Toast.LENGTH_SHORT).show();
                    if (locationManager != null) {
                        removeUpdates();
                    }
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "未获取定位权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View v) {
        int id = v.getId();

        // 初始化
        type.setText("当前模式：");
        nowAddress.setText("当前位置：");
        lat.setText("纬度：");
        lon.setText("经度：");

        removeUpdates();

        if (id == R.id.GPS) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startLocationUpdates(LocationManager.GPS_PROVIDER);
                type.setText("当前模式：GPS");
            } else {
                Toast.makeText(this, "请打开GPS！", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.NET) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                startLocationUpdates(LocationManager.NETWORK_PROVIDER);
                type.setText("当前模式：NETWORK");
            } else {
                Toast.makeText(this, "请启用网络！", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.PASSIVE) {
            if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                startLocationUpdates(LocationManager.PASSIVE_PROVIDER);
                type.setText("当前模式：PASSIVE");
            } else {
                Toast.makeText(this, "请打开GPS并确认网络良好！", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates(String provider) {
        try {
            locationManager.requestLocationUpdates(provider, 0, 0, this);
            Log.d(TAG, "位置开始更新 " + provider);
        } catch (SecurityException e) {
            Log.e(TAG, "请求位置更新时发生安全异常：" + e.getMessage());
            Toast.makeText(this, "定位权限被拒绝！", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "位置已更新: " + location.getLatitude() + ", " + location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lat.setText("纬度：" + latitude);
        lon.setText("经度：" + longitude);
        Log.d(TAG, "纬度：" + latitude + " 经度：" + longitude);
        Toast.makeText(MainActivity.this, "定位成功！", Toast.LENGTH_SHORT).show();

        // 地理位置解析
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            // 获取经纬度对于的位置
            // getFromLocation(纬度, 经度, 最多获取的位置数量)
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // 拼接详细的当前位置信息
                StringBuilder fullAddress = new StringBuilder();
                // 优先使用 getAddressLine(0)，它通常是格式化的完整地址
                String info = address.getAddressLine(0);
                if (info != null) {
                    fullAddress.append(info);
                } else {
                    if (address.getCountryName() != null) {
                        fullAddress.append(address.getCountryName());
                    }
                    if (address.getAdminArea() != null) { // 省
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(address.getAdminArea());
                    }
                    if (address.getLocality() != null) { // 市
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(address.getLocality());
                    }
                    if (address.getSubLocality() != null) { // 区/县
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(address.getSubLocality());
                    }
                    if (address.getThoroughfare() != null) { // 街道
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(address.getThoroughfare());
                    }
                    if (address.getSubThoroughfare() != null) { // 门牌号
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(address.getSubThoroughfare());
                    }
                    if (address.getFeatureName() != null && fullAddress.toString().isEmpty()) { // 如果以上都没有，尝试获取地标名称
                        fullAddress.append(address.getFeatureName());
                    }
                }
                if (fullAddress.length() > 0) {
                    nowAddress.setText("当前位置：" + fullAddress.toString());
                } else {
                    nowAddress.setText("当前位置：无法解析详细地址");
                }
            } else {
                nowAddress.setText("当前位置：获取位置信息失败");
            }
        } catch (IOException e) {
            Log.e(TAG, "地理编码器异常: " + e.getMessage());
            e.printStackTrace();
            nowAddress.setText("当前位置：获取位置信息失败");
            Toast.makeText(MainActivity.this, "地址解析失败: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        // 根据开关状态决定是否移除监听器
        if (!isContinuousUpdateEnabled) { // 如果持续更新未开启 (即为单次获取模式)
            removeUpdates();

        } else {

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: " + provider + ", status: " + status);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
    }

    public void removeUpdates(){
        if (locationManager != null) {
            locationManager.removeUpdates(this);
            Log.d(TAG, "位置监听器已移除");
        }
    }

    public void initview(){
        GPS = findViewById(R.id.GPS);
        NET = findViewById(R.id.NET);
        type = findViewById(R.id.type);
        PASSIVE = findViewById(R.id.PASSIVE);
        lat = findViewById(R.id.tv_latitude);
        lon = findViewById(R.id.tv_longitude);
        nowAddress = findViewById(R.id.tv_nowAddress);
        continuousUpdateSwitch = findViewById(R.id.continuous_update_switch);
        isContinuousUpdateEnabled = continuousUpdateSwitch.isChecked();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeUpdates(); // 在 Activity 暂停时移除监听器，避免后台不必要消耗
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeUpdates(); // 在 Activity 销毁时移除监听器
    }
}