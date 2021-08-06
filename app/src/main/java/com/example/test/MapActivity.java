package com.example.test;

import android.Manifest;
import android.content.Context;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.test.utils.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MapActivity extends AppCompatActivity {
    private RxPermissions rxPermissions;//权限请求框架
    private GeoCoder geoCoder;//百度地址解析
    private MapView mMapView = null;
    private LocationClient mLocationClient;//定位
    private BaiduMap mBaiduMap;//百度地图
    private Context context;

    private Marker marker;//标点也可以说是覆盖物
    private BitmapDescriptor bitmap;//标点的图标
    private double markerLatitude = 0;//标点纬度
    private double markerLongitude = 0;//标点经度
    private double latitude;//定位纬度
    private double longitude;//定位经度

    ImageView image_back;

    FloatingActionButton btnAutoLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        context=getApplicationContext();

        btnAutoLocation=findViewById(R.id.btn_auto_location);
        mMapView = (MapView) findViewById(R.id.bmapView);
        image_back=findViewById(R.id.to_back);


        rxPermissions = new RxPermissions(this);//实例化这个权限请求框架，否则会报错
        mBaiduMap=mMapView.getMap();
        mMapView.removeViewAt(1);
        permissionVersion();//权限判断
        geoCoder = GeoCoder.newInstance();//赋值
        geoCoder.setOnGetGeoCodeResultListener(onGetGeoCoderResultListener);//反编码结果监听

        btnAutoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerLatitude = 0;
                markerLongitude = 0;
                marker.remove();//清除标点
                initLocation();
            }
        });

        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }




    private void initMapOnClick() {
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            //地图内 Poi 单击事件回调函数  那么poi是什么呢？你可以当做兴趣点，
            // 比如我想知道我当前所在地有那些餐厅，那么餐厅就是poi，
            // 而你点击这个poi就会拿到详情的信息数据，当然不在我的业务需求之内，所以只做解释
            @Override
            public void onMapPoiClick(MapPoi mapPoi) {

            }

            //地图单击事件回调函数

            @Override
            public void onMapClick(LatLng latLng) {
                bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_marka);// 设置marker图标
                //通过LatLng获取经纬度
                markerLatitude = latLng.latitude;//获取纬度
                markerLongitude = latLng.longitude;//获取经度
                mBaiduMap.clear();//清除之前的图层

                MarkerOptions options = new MarkerOptions()//创建标点marker设置对象
                        .position(latLng)//设置标点的定位
                        .icon(bitmap);//设置标点图标

                marker = (Marker) mBaiduMap.addOverlay(options);//在地图上显示标点
                //点击地图之后重新定位
                initLocation();
            }
        });
    }


    private OnGetGeoCoderResultListener onGetGeoCoderResultListener = new OnGetGeoCoderResultListener() {
        /**
         * 编码结果返回  就是通过具体位置信息获取坐标
         * @param geoCodeResult  编码返回结果
         */
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

        }

        /**
         * 反编码结果返回  就是通过坐标获取具体位置信息
         * @param reverseGeoCodeResult 反编码返回结果
         */
        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
            if (reverseGeoCodeResult == null
                    || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                // 没有检测到结果
                return;
            }
            ReverseGeoCodeResult.AddressComponent addressDetail = reverseGeoCodeResult.getAddressDetail();
            //需要的地址信息就在AddressComponent 里
            ToastUtils.showShortToast(context,"地址信息："+addressDetail.province+addressDetail.city+addressDetail.district+addressDetail.street);
            Log.d("dataStr",addressDetail.province+addressDetail.city+addressDetail.district+addressDetail.street);

        }
    };


    private void permissionVersion(){
        if(Build.VERSION.SDK_INT >= 23){//6.0或6.0以上
            //动态权限申请
            permissionsRequest();
        }else {//6.0以下
            //发现只要权限在AndroidManifest.xml中注册过，均会认为该权限granted  提示一下即可
            ToastUtils.showShortToast(this,"你的版本在Android6.0以下，不需要动态申请权限。");
        }
    }


    //动态权限申请
    private void permissionsRequest() {
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {//申请成功
                        initLocation();
                        initMapOnClick();
                    } else {//申请失败
                        ToastUtils.showShortToast(this, "权限未开启");
                    }
                });
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe();
    }


    private class MyLocationListener implements BDLocationListener {

        /**
         * 监听返回数据 MapView 销毁后不在处理新接收的位置
         * @param bdLocation 定位信息
         */
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mMapView == null) {//做null处理，避免APP崩溃
                return;
            }

            if(markerLatitude==0){
                latitude=bdLocation.getLatitude();
                longitude=bdLocation.getLongitude();
                btnAutoLocation.hide();
            }else {
                latitude=markerLatitude;
                longitude=markerLongitude;
                btnAutoLocation.show();
            }

            MyLocationData locationData = new MyLocationData.Builder()//定位构造器
                    .accuracy(bdLocation.getRadius())//设置定位数据的精度信息，单位：米
                    .direction(bdLocation.getDirection())//设置定位数据的方向信息
                    .latitude(latitude)//设置定位数据的纬度
                    .longitude(longitude)//设置定位数据的经度
                    .build();//构建生成定位数据对象
            mBaiduMap.setMyLocationData(locationData);//设置定位数据，只有开启定位图层之后才会生效

            //创建一个经纬度构造对象，传入定位返回的经纬度，Latitude是纬度，Longitude是经度,一对经纬度值代表地球上一个地点。
            LatLng latLng = new LatLng(latitude,longitude);

            geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng).pageNum(0).pageSize(100));

            MapStatus.Builder builder = new MapStatus.Builder()//创建地图状态构造器
                    .target(latLng)//设置地图中心点，传入经纬度对象
                    .zoom(13.0f);//设置地图缩放级别 13 表示  比例尺/2000米 2公里

            //改变地图状态，使用地图状态更新工厂中的新地图状态方法，传入状态构造器
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }

    private void initLocation() {
        mBaiduMap.setMyLocationEnabled(true);//开启定位图层
        mLocationClient = new LocationClient(context);//定位初始化

        MyLocationListener listener = new MyLocationListener();//创建定位监听器
        mLocationClient.registerLocationListener(listener);//注册定位监听，否则监听无效

        LocationClientOption option = new LocationClientOption();//创建定位设置
        option.setOpenGps(true);//打开GPS
        option.setCoorType("bd09ll");//设置坐标类型  可以设置BD09LL和GCJ02两种坐标
        option.setScanSpan(0);//设置扫描间隔，单位是毫秒，0  则表示只定位一次，设置毫秒不能低于1000，也就是1秒

        mLocationClient.setLocOption(option);//传入定位设置
        mLocationClient.start();//开始定位

    }


    @Override
    protected void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mMapView.onDestroy();
            mLocationClient.stop();// 退出时销毁定位
            mBaiduMap.setMyLocationEnabled(false);// 关闭定位图层
        }


        super.onDestroy();
    }

}
