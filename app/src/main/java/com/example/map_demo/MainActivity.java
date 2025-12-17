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

        // 检查GPS权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int id = v.getId();

        // 初始化
        type.setText("当前模式：");
        nowAddress.setText("当前位置：");
        lat.setText("纬度：");
        lon.setText("经度：");

        if (id == R.id.GPS) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.removeUpdates(this);
                startLocationUpdates(LocationManager.GPS_PROVIDER);
                type.setText("当前模式：GPS");
            } else {
                Toast.makeText(this, "请打开GPS！", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.NET) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.removeUpdates(this);
                startLocationUpdates(LocationManager.NETWORK_PROVIDER);
                type.setText("当前模式：NETWORK");
            } else {
                Toast.makeText(this, "请启用网络！", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.PASSIVE) {
            if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                locationManager.removeUpdates(this);
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
            Log.e(TAG, "SecurityException requesting location updates: " + e.getMessage());
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "Location changed: " + location.getLatitude() + ", " + location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lat.setText("纬度：" + latitude);
        lon.setText("经度：" + longitude);
        Log.e(TAG, "纬度：" + latitude + " 经度：" + longitude);
        Toast.makeText(MainActivity.this, "定位成功！", Toast.LENGTH_SHORT).show();

        // 地理位置解析
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            // 获取经纬度对于的位置
            // getFromLocation(纬度, 经度, 最多获取的位置数量)
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            // 得到第一个经纬度位置解析信息
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // 获取到详细的当前位置
                String info = address.getAddressLine(0) + // 获取国家名称
                        address.getAddressLine(1) + // 获取省市县(区)
                        address.getAddressLine(2);  // 获取镇号(地址名称)
                nowAddress.setText("当前位置："+info);
            }
            else{
                nowAddress.setText("当前位置：获取位置信息失败");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder exception: " + e.getMessage());
            e.printStackTrace();
            nowAddress.setText("当前位置：获取位置信息失败");
        }
        // 移除位置管理器
        locationManager.removeUpdates(this);
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

    public void initview(){
        GPS = findViewById(R.id.GPS);
        NET = findViewById(R.id.NET);
        type = findViewById(R.id.type);
        PASSIVE = findViewById(R.id.PASSIVE);
        lat = findViewById(R.id.tv_latitude);
        lon = findViewById(R.id.tv_longitude);
        nowAddress = findViewById(R.id.tv_nowAddress);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}