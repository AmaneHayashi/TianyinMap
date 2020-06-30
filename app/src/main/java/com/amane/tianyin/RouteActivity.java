package com.amane.tianyin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.amane.adapter.BusResultListAdapter;
import com.amane.overlay.DrivingRouteOverlay;
import com.amane.overlay.WalkRouteOverlay;
import com.amane.route.DriveRouteDetailActivity;
import com.amane.route.WalkRouteDetailActivity;
import com.amane.utils.MapUtils;
import com.amane.utils.PresentUtils;
import com.amane.utils.ToastUtils;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

import java.util.Objects;

/**
 * 路径规划(驾车规划、公交规划、步行规划)的Activity
 * @author Amane Hayashi
 */
public final class RouteActivity extends Activity implements OnRouteSearchListener {

    /* Activity */
    private Context context;

    /* 地图系列 */
    private AMap aMap;
    private MapView mapView;

    /* View组件系列 */
    private ConstraintLayout routeBusLayout, routeMoreLayout;
    private TextView routeInfoText, routeCostText;
    private RadioButton bus, drive, walk;
    private ListView busResultList;

    /* 路径系列 */
    private RouteSearch routeSearch;

    /* 全局静态变量系列 */
    private static LatLonPoint startLatLon = null;
    private static LatLonPoint endLatLon = null;
    private static String myCity = null;

    /* 常量·步行模式系列 */
    private static final int WALK_DEFAULT = 0;

    /* 常量·Route类型系列 */
    private final static int ROUTE_TYPE_BUS = 1;
    private final static int ROUTE_TYPE_DRIVE = 2;
    private final static int ROUTE_TYPE_WALK = 3;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_route);
        context = getApplicationContext();
        // 地图视图
        mapView = findViewById(R.id.route_map);
        // 重写OnCreate
        mapView.onCreate(bundle);
        // 获得MainActivity传入的数据
        getIntentData();
        // 初始化
        initMapView();
        initId();
        initListener();
        // 调用驾驶路线规划
        onDriveClick(null);
    }

    /* ************************************************************************************************
     *
     * 以下为基本的初始化方法，依次为获得intent数据、初始化地图、组件、组件监听。
     *
     * ************************************************************************************************/

    /**
     * 从Intent中获得数据(起始点坐标、目的地坐标、当前城市)
     */
    private void getIntentData(){
        Intent intent = getIntent();
        startLatLon = MapUtils.latLngToLatLonPoint(Objects.requireNonNull(intent.getParcelableExtra("from")));
        endLatLon = MapUtils.latLngToLatLonPoint(Objects.requireNonNull(intent.getParcelableExtra("to")));
        myCity = intent.getStringExtra("city");
    }

    /**
     * 初始化地图控件，添加起始点、目的地的Marker
     */
    private void initMapView() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationStyle(myLocationStyle);
        MapUtils.addMarker(aMap, MapUtils.latLonPointToLatLng(startLatLon), BitmapDescriptorFactory.fromResource(R.drawable.start));
        MapUtils.addMarker(aMap, MapUtils.latLonPointToLatLng(endLatLon), BitmapDescriptorFactory.fromResource(R.drawable.end));
        routeSearch = new RouteSearch(this);
    }

    /**
     * 初始化组件
     */
    private void initId(){
        routeMoreLayout = findViewById(R.id.route_more_layout);
        routeBusLayout = findViewById(R.id.route_bus_layout);
        routeInfoText = findViewById(R.id.info_text);
        routeCostText = findViewById(R.id.cost_text);
        drive = findViewById(R.id.route_drive);
        bus = findViewById(R.id.route_bus);
        walk = findViewById(R.id.route_walk);
        busResultList = findViewById(R.id.bus_result_list);
    }

    /**
     * 初始化组件监听
     */
    private void initListener() {
        routeSearch.setRouteSearchListener(this);
    }

    /* ************************************************************************************************
     *
     * 以下为各种组件的OnClickListener的监听处理系列，OnClick监听都在.xml文件中进行了设置
     *
     * ************************************************************************************************/

    /**
     * 公交路线点击监听(搜索公交路线)
     * @param view 视图
     */
    public void onBusClick(View view) {
        // 开始搜索
        searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BUS_DEFAULT);
        // 将公交的图片变为蓝色，其它不变，以下同理
        setDrawableTop(drive, R.drawable.route_drive_normal);
        setDrawableTop(bus, R.drawable.route_bus_select);
        setDrawableTop(walk, R.drawable.route_walk_normal);
        // 显示公交结果详情页，隐藏地图
        mapView.setVisibility(View.GONE);
        routeBusLayout.setVisibility(View.VISIBLE);
        // 设置(驾驶/步行的)详情页不可见
        routeMoreLayout.setVisibility(View.GONE);
    }

    /**
     * 驾驶路线点击监听(搜索驾驶路线)
     * @param view 视图
     */
    public void onDriveClick(View view) {
        searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DRIVING_SINGLE_DEFAULT);
        setDrawableTop(drive, R.drawable.route_drive_select);
        setDrawableTop(bus, R.drawable.route_bus_normal);
        setDrawableTop(walk, R.drawable.route_walk_normal);
        // 显示驾驶结果详情(overlay在地图上)，隐藏公交结果详情页
        mapView.setVisibility(View.VISIBLE);
        routeBusLayout.setVisibility(View.GONE);
    }

    /**
     * 步行路线点击监听(搜索步行路线)
     * @param view 视图
     */
    public void onWalkClick(View view) {
        searchRouteResult(ROUTE_TYPE_WALK, WALK_DEFAULT);
        setDrawableTop(drive, R.drawable.route_drive_normal);
        setDrawableTop(bus, R.drawable.route_bus_normal);
        setDrawableTop(walk, R.drawable.route_walk_select);
        // 显示步行结果详情(overlay在地图上)，隐藏公交结果详情页
        mapView.setVisibility(View.VISIBLE);
        routeBusLayout.setVisibility(View.GONE);
    }

    /**
     * 路径搜索统一入口
     * @param routeType 路径类型
     * @param mode 搜索类型
     */
    public void searchRouteResult(int routeType, int mode) {
        // 定义起点和终点
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startLatLon, endLatLon);
        // 公交路径规划
        if (routeType == ROUTE_TYPE_BUS) {
            // 第一个参数表示起点和终点，第二个参数表示公交查询模式，
            // 第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            BusRouteQuery query = new BusRouteQuery(fromAndTo, mode, myCity, 0);
            routeSearch.calculateBusRouteAsyn(query);
        }
        // 驾驶路径规划
        else if (routeType == ROUTE_TYPE_DRIVE) {
            // 第一个参数表示起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，
            // 第四个参数表示避让区域，第五个参数表示避让道路
            DriveRouteQuery query = new DriveRouteQuery(fromAndTo, mode, null, null, "");
            routeSearch.calculateDriveRouteAsyn(query);
        }
        // 步行路径规划
        else if (routeType == ROUTE_TYPE_WALK) {
            WalkRouteQuery query = new WalkRouteQuery(fromAndTo);
            routeSearch.calculateWalkRouteAsyn(query);
        }
    }

    /* ************************************************************************************************
     *
     * 以下为异步查询结果的监听，包括公交、驾驶、步行、骑行。
     *
     * ************************************************************************************************/

    /**
     * 公交搜索结果监听
     * @param result 搜索结果
     * @param i 状态码
     */
    @Override
    public void onBusRouteSearched(BusRouteResult result, int i) {
        // 取消进度框
        // 清除地图Marker
        aMap.clear(true);
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                // 添加BusResult适配器
                busResultList.setAdapter(new BusResultListAdapter(context, result));
            } else {
                ToastUtils.show(context, R.string.no_result);
            }
        } else {
            ToastUtils.showError(context, i);
        }
    }

    /**
     * 驾驶索结果监听
     * @param result 搜索结果
     * @param i 状态码
     */
    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int i) {
        aMap.clear(true);
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                // 获得驾驶路线
                final DrivePath drivePath = result.getPaths().get(0);
                if(drivePath == null) {
                    return;
                }
                // 创建驾驶Overlay
                // 第一个参数为当前Activity，第二个参数为Map，第三个为路径，第四个为起点，第五个为终点，第六个为途经点
                DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(context, aMap, drivePath,
                        result.getStartPos(), result.getTargetPos(), null);
                // 设置显示驾驶节点
                drivingRouteOverlay.setNodeIconVisibility(true);
                // 设置颜色展示交通拥堵情况
                drivingRouteOverlay.setIsColorfulline(true);
                // 移去上次的，添加本次的
                drivingRouteOverlay.removeFromMap();
                drivingRouteOverlay.addToMap();
                // 缩放地图到起终点处
                drivingRouteOverlay.zoomToSpan();
                // 显示详情页
                routeMoreLayout.setVisibility(View.VISIBLE);
                // 显示打车信息
                routeCostText.setVisibility(View.VISIBLE);
                // 处理并显示
                routeInfoText.setText(PresentUtils.getEasyRouteInfo(context, drivePath.getDuration(), drivePath.getDistance()));
                routeCostText.setText(PresentUtils.getEasyCostInfo(result.getTaxiCost()));
                // 设置点击监听
                routeMoreLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(context, DriveRouteDetailActivity.class);
                    intent.putExtra("drive_path", drivePath);
                    intent.putExtra("drive_result", result);
                    startActivity(intent);
                });
            }
            else {
                ToastUtils.show(context, R.string.no_result);
            }
        } else {
            ToastUtils.showError(context, i);
        }
    }

    /**
     * 步行搜索结果监听
     * @param result 搜索结果
     * @param i 状态码
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int i) {
        aMap.clear(true);
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                final WalkPath walkPath = result.getPaths().get(0);
                if(walkPath == null) {
                    return;
                }
                // 创建步行Overlay
                // 第一个参数为当前Activity，第二个参数为Map，第三个为路径，第四个为起点，第五个为终点
                WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(context, aMap, walkPath,
                        result.getStartPos(), result.getTargetPos());
                walkRouteOverlay.removeFromMap();
                walkRouteOverlay.addToMap();
                walkRouteOverlay.zoomToSpan();
                routeMoreLayout.setVisibility(View.VISIBLE);
                // 此时不显示打车费用
                routeCostText.setVisibility(View.GONE);
                // 处理并显示
                routeInfoText.setText(PresentUtils.getEasyRouteInfo(context, walkPath.getDuration(), walkPath.getDistance()));
                // 设置点击监听
                routeMoreLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(context, WalkRouteDetailActivity.class);
                    intent.putExtra("walk_path", walkPath);
                    intent.putExtra("walk_result", result);
                    startActivity(intent);
                });
            } else {
                ToastUtils.show(context, R.string.no_result);
            }
        } else {
            ToastUtils.showError(context, i);
        }
    }

    /**
     * 骑行搜索结果监听
     * @param result 搜索结果
     * @param i 状态码
     */
    @Override
    public void onRideRouteSearched(RideRouteResult result, int i) {}

    /* ************************************************************************************************
     *
     * 以下为其它方法
     *
     * ************************************************************************************************/

    /**
     * 设置View的顶部Drawable
     * @param textView 视图
     * @param drawableId 图片ID
     */
    private void setDrawableTop(TextView textView, int drawableId){
        textView.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(drawableId), null, null);
    }

    /* ************************************************************************************************
     *
     * 以下为Map必须重写的方法
     *
     * ************************************************************************************************/

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}

