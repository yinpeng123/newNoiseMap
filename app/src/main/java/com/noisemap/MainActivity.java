package com.noisemap;

import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.content.pm.PackageManager;
import android.annotation.TargetApi;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.cloud.CloudListener;
import com.baidu.mapapi.cloud.CloudManager;
import com.baidu.mapapi.cloud.CloudPoiInfo;
import com.baidu.mapapi.cloud.CloudRgcResult;
import com.baidu.mapapi.cloud.CloudSearchResult;
import com.baidu.mapapi.cloud.DetailSearchResult;
import com.baidu.mapapi.cloud.LocalSearchInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;


public class MainActivity extends Activity implements CloudListener{

    private final int SDK_PERMISSION_REQUEST = 127;
    private static final String TAG = "MainActivity";
    static CLoc cLoc;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private String permissionInfo;
    private TextView mres;

    private static final String LTAG = MainActivity.class.getSimpleName();

    public Location location;
    //定位
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //不能放在加载布局后
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        getPersimmions();
        //初始化控件
        initView();
        Log.i("y", "onCreate");

    }


    private void initView() {
        cLoc=new CLoc();
        mMapView = (MapView) findViewById(R.id.bmapView);
        mres=(TextView)findViewById(R.id.mres);
        mBaiduMap = mMapView.getMap();
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        //初始化定位
        initLocation();
        //开启定位
        mLocationClient.start();

        /**
         * CloudManager:LBS云检索管理类
         * getInstance():获取唯一可用实例
         * init(CloudListener listener):初始化
         * 需要实现CloudListener接口的onGetDetailSearchResult和onGetSearchResult方法
         * */
        CloudManager.getInstance().init(MainActivity.this);
        //本地搜索
        findViewById(R.id.regionSearch).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * LocalSearchInfo:设置云检索中本地检索的参数，继承自 BaseCloudSearchInfo
                         * */
                        LocalSearchInfo info = new LocalSearchInfo();

                        //access_key（必须），最大长度50
                        info.ak = "AXI0cm3LRMgxmkDOm3LjvjSuxSp20jg0";

                        //geo table 表主键（必须）
                        info.geoTableId = 169399;

                        //标签，可选，空格分隔的多字符串，最长45个字符，样例：美食 小吃
                        info.tags = "";

                        //检索关键字，可选。最长45个字符。
                        info.q = "";

                        //检索区域名称，必选。市或区的名字，如北京市，海淀区。最长25个字符。
                        info.region = "武汉市";

                        /**
                         * localSearch(LocalSearchInfo info)
                         * 区域检索，如果所有参数都合法，返回true，否则返回 fasle，
                         * 检索的结果在 CloudListener 中的 onGetSearchResult() 函数中。
                         * */
                        CloudManager.getInstance().localSearch(info);

                    }
                });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //从marker中获取info信息
                Bundle bundle = marker.getExtraInfo();
                CLoc info = (CLoc) bundle.getSerializable("info");
                mres.setText("地址："+info.getAddress()+"\n"+"分贝："+Integer.valueOf(info.getnoise())+"db");
                return true;
            }
        });
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        ////可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        int span = 0;
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(span);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，默认false,设置是否使用gps
        option.setOpenGps(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);
        mLocationClient.setLocOption(option);
    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }
            //麦克风权限
            if (addPermission(permissions, Manifest.permission.RECORD_AUDIO)) {
                permissionInfo += "Manifest.permission.RECORD_AUDIO Deny \n";

            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }

        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    /**
     * CloudSearchResult:
     * java.util.List<CloudPoiInfo>
     * poiList
     * poi结果列表
     * */
    @Override
    public void onGetSearchResult(CloudSearchResult result, int error) {
        if (result != null && result.poiList != null && result.poiList.size() > 0) {
            Log.d(LTAG, "onGetSearchResult, result length: " + result.poiList.size());

            //清空地图所有的 Overlay 覆盖物以及 InfoWindow
            mBaiduMap.clear();


            /**
             * public static BitmapDescriptor fromResource(int resourceId)
             * 根据资源 Id 创建 bitmap 描述信息
             * */
            BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);

            Marker marker;
            LatLng ll;
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int noise;
            for (CloudPoiInfo info : result.poiList) {
                ll = new LatLng(info.latitude, info.longitude);
                noise=(int)info.extras.get("noise");
                /**
                 * OverlayOptions:地图覆盖物选型基类
                 *
                 * public MarkerOptions icon(BitmapDescriptor icon):
                 * 设置 Marker 覆盖物的图标，相同图案的 icon 的 marker
                 * 最好使用同一个 BitmapDescriptor 对象以节省内存空间。
                 * @param icon - Marker 覆盖物的图标
                 * @return 该 Marker 选项对象
                 *
                 * public MarkerOptions position(LatLng position):
                 * 设置 marker 覆盖物的位置坐标
                 * @param position - marker 覆盖物的位置坐标
                 * @param 该 Marker 选项对象
                 * */
                CLoc cLoc=new CLoc();
                cLoc.setAddress(info.address);
                cLoc.setName(info.direction);
                cLoc.setDb(noise);

                 OverlayOptions oo = new MarkerOptions().icon(bd).position(ll);

                /**
                 * addOverlay(OverlayOptions options):
                 * 向地图添加一个 Overlay
                 * */
                //mBaiduMap.addOverlay(oo);
                //添加marker
                marker = (Marker) mBaiduMap.addOverlay(oo);
                //使用marker携带info信息，当点击事件的时候可以通过marker获得info信息
                Bundle bundle = new Bundle();
                //info必须实现序列化接口
                bundle.putSerializable("info", cLoc);
                marker.setExtraInfo(bundle);

                /**
                 * public LatLngBounds.Builder include(LatLng point)
                 * 让该地理范围包含一个地理位置坐标
                 * @param point - 地理位置坐标
                 * @return 该构造器对象
                 * */
                builder.include(ll);
            }
            /**
             * public LatLngBounds build()
             * 创建地理范围对象
             * @return 创建出的地理范围对象
             * */
            LatLngBounds bounds = builder.build();

            /**
             * MapStatusUpdateFactory:生成地图状态将要发生的变化
             *
             * public static MapStatusUpdate newLatLngBounds(LatLngBounds bounds)
             * 设置显示在屏幕中的地图地理范围
             * @param bounds - 地图显示地理范围，不能为 null
             * @return 返回构造的 MapStatusUpdate， 如果 bounds 为 null 则返回空。
             * */
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(bounds);
            mBaiduMap.animateMapStatus(u);
        }
    }



    @Override
    public void onGetDetailSearchResult(DetailSearchResult detailSearchResult, int i) {

    }

    @Override
    public void onGetCloudRgcResult(CloudRgcResult cloudRgcResult, int i) {

    }


    //定位的回调
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            mres.setText(location.getAddrStr());
            //Receive Location
             cLoc.setCLoc(location);
            //移动到我的位置
            //设置缩放，确保屏幕内有我
            MapStatusUpdate mapUpdate = MapStatusUpdateFactory.zoomTo(16);
            mBaiduMap.setMapStatus(mapUpdate);
            //开始移动
            MapStatusUpdate mapLatlng = MapStatusUpdateFactory.
                    newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            mBaiduMap.setMapStatus(mapLatlng);
            //绘制图层
            //定义Maker坐标点
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_location);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
//TODO
        }
        public BDLocation getlocation(BDLocation location){
            return location;
        }


    }

    public void getNoise(View v) {
        Toast toast = Toast.makeText(getApplicationContext(), "开始测量噪音分贝", Toast.LENGTH_SHORT);
        toast.show();
        Intent mIntent = new Intent(MainActivity.this,NoiseMeasure.class);
        mIntent.putExtra("CLoc",cLoc);
        startActivity(mIntent);

    }
    //刷新页面
    public void refresh(View v){
        Intent mIntent = new Intent(MainActivity.this,MainActivity.class);
        startActivity(mIntent);
        onDestroy();

    }

    @Override
    protected void onStop() {
        Log.i("y", "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("y", "onDestroy");
        super.onDestroy();
        mMapView.onDestroy();
        CloudManager.getInstance().destroy();

    }

    @Override
    protected void onResume() {
        Log.i("y", "onResume");
        super.onResume();
        mMapView.onResume();

    }

    @Override
    protected void onStart() {
        Log.i("y", "onStert");
        super.onStart();



    }

    @Override
    protected void onPause() {
        Log.i("y", "onPause");
        super.onPause();
        mMapView.onPause();
    }
}
