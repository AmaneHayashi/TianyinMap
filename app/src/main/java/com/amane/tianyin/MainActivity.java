package com.amane.tianyin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.amane.adapter.MainAdapter;
import com.amane.adapter.SearchDetailListAdapter;
import com.amane.adapter.SearchItemListAdapter;
import com.amane.bean.SearchDetail;
import com.amane.bean.SearchItem;
import com.amane.components.MyPoiView;
import com.amane.components.MySearchView;
import com.amane.overlay.BusLineOverlay;
import com.amane.utils.BaseUtils;
import com.amane.utils.DetailFactory;
import com.amane.utils.MapUtils;
import com.amane.utils.PresentUtils;
import com.amane.utils.SharedPreferencesUtils;
import com.amane.utils.ToastUtils;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusStationQuery;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.amap.api.services.core.AMapException.CODE_AMAP_SUCCESS;

/**
 * 地图(含地图显示、搜索[ID/Keyword/BusLine/BusStation]、POI、逆地理搜索、输入提示)的Activity
 * @author Amane Hayashi
 */
public final class MainActivity extends MainAdapter {

    /* Activity */
    private Context context;

    /* 地图系列 */
    private MapView mapView;
    private AMap aMap;
    private UiSettings uiSettings;
    private MyLocationStyle myLocationStyle;
    private BusLineOverlay busLineOverlay;

    /* View组件系列 */
    private View mainView;
    private ListView searchResultList, detailListView, searchHistoryList;
    private AutoCompleteTextView mainSearchText;
    private TextView poiName, poiDistance, poiAddress, poiType;
    private Button searchButton, presentText, poiCancel, poiRoute;
    private ImageView compass;

    /* 自定义View组件系列 */
    private SlidingUpPanelLayout layout;
    private MyPoiView poiView;
    private MySearchView mainSearch;

    /* 全局静态基本类型变量系列 */
    private static int touchTimes = 0;
    private static long mExitTime = 0;
    private static float lastBearing = 0.0f;
    private static boolean notSetMyCity = true;
    private static boolean searchFinished = false;

    /* 全局静态其它变量系列 */
    private static String myCity = null;
    private static LatLng myLatLng = null;
    private static LatLng poiLatLng = null;

    private static final int SEARCH_PAGE_SIZE = 10;

    /* 常量·缩放视角系列 */
    private static final int DEFAULT_ZOOM = 15;
    private static final int DETAIL_ZOOM = 13;

    /* 常量· POI ID系列 */
    private static final boolean POI_AFTER_CLICK = false;
    private static final boolean POI_AFTER_SEARCH = true;

    /* 常量·Message系列 */
    private static final int INIT = 0;
    private static final int POI_ID = 1;
    private static final int POI_KEYWORD = 2;
    private static final int BUS_LINE = 3;
    private static final int BUS_STATION = 4;

    /* 常量·Color系列 */
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);

    /* 其它常量系列 */
    private static final Map<Integer, Integer> MAP_STYLE_MAP = new HashMap<Integer, Integer>(){
        {
            put(R.string.map_style_normal, AMap.MAP_TYPE_NORMAL);
            put(R.string.map_style_traffic, AMap.MAP_TYPE_NORMAL);
            put(R.string.map_style_satellite, AMap.MAP_TYPE_SATELLITE);
            put(R.string.map_style_night, AMap.MAP_TYPE_NIGHT);
        }
    };

    /* Handler系列 */
    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case INIT:
                    editLocationStrategy();
                    break;
                case POI_ID:
                    showPoiView();
                    break;
                case POI_KEYWORD:
                case BUS_LINE:
                case BUS_STATION:
                    showDetailListView();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        // 获得总布局layout
        layout = findViewById(R.id.layout);
        // 设置不可滑动
        layout.setTouchEnabled(false);
        // 地图视图
        mapView = findViewById(R.id.map_view);
        // 重写onCreate
        mapView .onCreate(savedInstanceState);
        // 初始化
        initMapView();
        initLocationStyle();
        initMapListener();
        initId();
        initListener();
    }

    /* ************************************************************************************************
     *
     * 以下为基本的初始化方法，依次为初始化地图、定位风格、地图监听、组件、组件监听。
     *
     * ************************************************************************************************/

    /**
     * 初始化地图控件
     */
    private void initMapView() {
        if (aMap == null) {
            aMap = mapView.getMap();
            uiSettings = aMap.getUiSettings();
        }
        /* 将地图的缩放级别调整为15
         * 显示我的位置
         * 显示室内地图
         * 将LOGO放到右下侧
         * 不显示比例尺、指南针、缩放控制、定位按钮
         */
        aMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        aMap.setMyLocationEnabled(true);
        aMap.showIndoorMap(true);
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setScaleControlsEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        // 地图初始化500ms后通过子线程调用句柄，修改定位策略并获得我所在的城市信息
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendMessage(INIT);
        }).start();
    }

    /**
     * 初始化定位风格
     */
    private void initLocationStyle() {
        myLocationStyle = new MyLocationStyle();
        /* 设置连续定位模式下的定位间隔，单位为毫秒
         * 设置是否显示定位小蓝点
         * 自定义精度范围的圆形边框颜色
         * 自定义蓝点图标的锚点
         * 设置圆形的填充颜色
         * 自定义精度范围的圆形边框宽度
         * 自定义定位蓝点图标
         * 定位类型：连续定位、视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动
         */
        myLocationStyle .interval(2000)
                        .showMyLocation(true)
                        .strokeColor(STROKE_COLOR)
                        .anchor(0.5f, 0.5f)
                        .radiusFillColor(FILL_COLOR)
                        .strokeWidth(0.01f)
                        .myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked))
                        .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        aMap.setMyLocationStyle(myLocationStyle);
        // 启动显示定位蓝点
        aMap.setMyLocationEnabled(true);
        SharedPreferencesUtils.truncate(context, "history");
    }

    /**
     * 初始化地图监听
     */
    private void initMapListener() {
        // 点击地图监听
        aMap.setOnMapClickListener(this);
        // 点击POI监听
        aMap.setOnPOIClickListener(this);
        // 我的位置改变监听
        aMap.setOnMyLocationChangeListener(this);
        // 地图手势监听
        aMap.setAMapGestureListener(this);
        // 视角改变监听
        aMap.setOnCameraChangeListener(this);
    }

    /**
     * 初始化组件
     */
    void initId() {
        /* 地图(上部)视图系列(mapLayout)
         * [presentText]
         *
         *                          [compass]
         * [searchButton]           [me]
         */
        presentText = findViewById(R.id.present_text);
        searchButton = findViewById(R.id.search_button);
        compass = findViewById(R.id.main_map_compass);
        /* 面板(下部)视图系列(panelLayout，包括搜索视图mainView，POI视图poiView，详细信息视图detailListView) */
        /* 搜索视图(mainView) - COLLAPSED
        *  [mainFloat]
        *  [mainSearch]
        *  [mapStyleText]
        *  [mapStyleChoices...(normal, traffic, satellite, night, personal)]
        */
        mainView = findViewById(R.id.main_view);
        mainSearch = findViewById(R.id.main_search);
        mainSearchText = mainSearch.getSearchText();
        /* 搜索视图(mainView) - EXPANDED NOT SEARCHED
         * {mainSearch}
         * [searchChoices...(food, hotel, scene, stop, cinema, supermarket)]
         * ===================================================
         * [searchHistoryList]
         * [searchHistoryClear]
         * */
        searchHistoryList = findViewById(R.id.search_history_list);
        /* 搜索视图(mainView) - EXPANDED SEARCHING
         * {mainSearch}
         * [searchResultList]
         */
        searchResultList = findViewById(R.id.search_result_list);
        /* POI信息视图系列(poiView)
         *
         * poiName                               [poiCancel]
         * poiDistance |(poiSplit) poiAddress
         * poiType
         * —————————————————————————————————————————————————
         *                        [poiNavigate]   [poiRoute]
         */
        poiView = findViewById(R.id.poi_view);
        poiName = findViewById(R.id.poi_name);
        poiCancel = findViewById(R.id.poi_cancel);
        poiDistance = findViewById(R.id.poi_distance);
        poiAddress = findViewById(R.id.poi_address);
        poiType = findViewById(R.id.poi_type);
        poiRoute = findViewById(R.id.poi_route);
        /* 详细信息视图系列(detailListView)
         * [lists of details...]
         */
        detailListView = findViewById(R.id.detail_list_view);
    }

    /**
     * 初始化组件监听
     */
    @SuppressLint("ClickableViewAccessibility")
    void initListener() {
        // 触摸监听
        mainSearchText.setOnTouchListener(this);
        // 输入监听
        mainSearchText.addTextChangedListener(new TextChangeListener());
        presentText.setOnTouchListener(this);
    }

    /* ************************************************************************************************
     *
     * 以下为各种组件的OnClickListener的监听处理系列，除地图外，OnClick监听都在.xml文件中进行了设置
     *
     * ************************************************************************************************/

    /**
     * 点击地图的监听(非搜索状态下，若POI视图可见，点击地图，收起POI视图)
     * @param latLng 点击处对应的地理位置
     */
    @Override
    public void onMapClick(LatLng latLng) {
        if (presentText.getVisibility() == View.GONE && poiView.getVisibility() == View.VISIBLE) {
            hidePoiView(null);
        }
    }

    /**
     * 点击地图上POI的监听(搜索视图下屏蔽;点击后调起POI异步查询，并显示POI卡片)
     * @param poi 点击的POI
     */
    public void onPOIClick(Poi poi) {
        if(presentText.getVisibility() == View.GONE) {
            searchByPoiId(poi, POI_AFTER_CLICK);
        }
    }

    /**
     * 点击地图风格按钮的监听(点击后根据文本找到ID，根据MAP_STYLE_MAP找到对应的风格)
     * @param v 视图
     */
    public void onMapStyleClick(View v) {
        String s = ((RadioButton)v).getText().toString();
        int id = MAP_STYLE_MAP  .keySet()
                .stream()
                .filter(k  -> BaseUtils.getResourceString(context, k).equals(s))
                .findFirst()
                .orElse(-1);
        if(id == -1){
            aMap.setCustomMapStyle(new com.amap.api.maps.model.CustomMapStyleOptions()
                    .setEnable(true).setStyleId("466d3bc2e86cc322944d6136173a2697"));
        }
        else {
            aMap.setMapType(Objects.requireNonNull(MAP_STYLE_MAP.get(id)));
        }
        aMap.setTrafficEnabled(id == R.string.map_style_traffic);
    }

    /**
     * 点击POI上的路线按钮的监听
     * @param v 视图
     */
    public void onPoiRouteClick(View v) {
        startRoute(poiLatLng);
    }

    /**
     * 点击POI上的导航按钮的监听
     * @param v 视图
     */
    public void onNavigateClick(View v) {
        NaviPara naviPara = new NaviPara();
        // 设置终点位置
        naviPara.setTargetPoint(poiLatLng);
        // 设置导航策略，这里是避免拥堵
        naviPara.setNaviStyle(AMapUtils.DRIVING_AVOID_CONGESTION);
        try {
            // 调起高德地图导航
            AMapUtils.openAMapNavi(naviPara, getApplicationContext());
        } catch (com.amap.api.maps.AMapException e) {
            // 如果没安装会进入异常，调起下载页面
            AMapUtils.getLatestAMapApp(getApplicationContext());
        }
    }

    /**
     * 点击我的位置的监听(点击后，地图将视角移动到以我的位置为中心，并旋转到正北角度)
     * @param v 视图
     */
    public void onMeClick(View v){
        aMap.animateCamera(CameraUpdateFactory.changeBearing(360), 125, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, DEFAULT_ZOOM), 125, null);
            }
            @Override
            public void onCancel() {}
        });
    }

    /**
     * 点击指南针的监听(点击后，地图将旋转到以正北方向)
     * @param v 视图
     */
    public void onCompassClick(View v){
        aMap.animateCamera(CameraUpdateFactory.changeBearing(360), 250, null);
    }

    /**
     * 点击搜索图标的监听(若图标为返回键，点击后收起搜索视图)
     * @param v 视图
     */
    public void onSearchImgClick(View v) {
        if (mainSearch.getImgResId() == R.drawable.back) {
            hideSearchView(null);
        }
    }

    /**
     * 点击清除搜索文本按钮的监听(点击后清除文本)
     * @param v 视图
     */
    public void onClearTextClick(View v) {
        mainSearchText.setText("");
    }

    /**
     * 点击搜索按钮的监听(点击后调起POIs异步查询，并显示DetailList)
     * @param v 视图
     */
    public void onSearchButtonClick(View v) {
        String text = mainSearchText.getText().toString();
        SearchItem searchItem = new SearchItem(null, R.drawable.poi_area, text, "关键词", POI_KEYWORD, null);
        v.setTag(R.id.tag_history, searchItem);
        onSearchItemClick(v);
    }

    /**
     * 点击搜索界面的关键词的监听(点击后调起POIs异步查询，并显示DetailList)
     * @param v 视图
     */
    public void onKeywordsClick(View v) {
        String text = ((RadioButton) v).getText().toString();
        SearchItem searchItem = new SearchItem(null, R.drawable.poi_area, text, "关键词", POI_KEYWORD, null);
        v.setTag(R.id.tag_history, searchItem);
        onSearchItemClick(v);
    }

    /**
     * 点击搜索项目的监听(点击后，根据SearchItem的类型进入不同的异步搜索策略，并将该项搜索内容保存到SharedPreferences的历史记录中)
     * @param v 视图
     */
    public void onSearchItemClick(View v) {
        // 获得搜索项目的tag
        SearchItem searchItem = (SearchItem) v.getTag(R.id.tag_history);
        int type = searchItem.getType();
        String name = searchItem.getName();
        switch (type) {
            case POI_ID:
                // 如果type为POI_ID搜索，则构造一个POI进入查询
                Poi poi = new Poi(name, MapUtils.stringToLatLng(searchItem.getPoint()), searchItem.getId());
                searchByPoiId(poi, POI_AFTER_SEARCH);
                break;
            case POI_KEYWORD:
                searchByPoiKeyword(name);
                break;
            case BUS_LINE:
                searchByBusLine(name);
                break;
            case BUS_STATION:
                searchByBusStation(name);
                break;
            default:
                throw new RuntimeException("no such element");
        }
        SharedPreferencesUtils.save(this, "history", searchItem);
    }

    /**
     * 点击清空历史记录的监听(点击后清空SharedPreferences的history表)
     * @param v 视图
     */
    public void onClearHistoryClick(View v) {
        SharedPreferencesUtils.truncate(context, "history");
        searchHistoryList.setVisibility(View.GONE);
    }

    /**
     * 点击PresentText左侧图标(返回图标)的监听(点击后，清空搜索文本，收起Present视图)
     */
    public void onPresentTextLeftClick() {
        mainSearchText.setText("");
        hidePresentView();
    }

    /**
     * 点击Detail上的路线按钮的监听(若为线路的路线按钮，则在地图上增加路线的Overlay；否则，进入路线规划)
     * @param v 视图
     */
    public void onDetailRouteClick(View v) {
        BusLineItem busLineItem = (BusLineItem)v.getTag(R.id.tag_overlay);
        if(busLineItem == null){
            startRoute((LatLng) v.getTag(R.id.tag_dest_point));
        }
        else {
            if (busLineOverlay != null) {
                busLineOverlay.removeFromMap();
            }
            busLineOverlay = new BusLineOverlay(aMap, busLineItem);
            busLineOverlay.addToMap();
            busLineOverlay.zoomToSpan();
        }
    }

    /**
     * 点击Detail的监听(地图中心移动到对应的POIs中心)
     * @param v 视图
     */
    public void onDetailLayoutClick(View v){
        LatLng latLng = (LatLng) v.getTag(R.id.tag_dest_point);
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM), 250, null);
    }

    /* ************************************************************************************************
     *
     * 以下为视图切换方法，包括搜索视图(收起)、搜索视图(伸展，无文本)、搜索视图(伸展，有文本)、POI视图、Detail视图
     *
     * ************************************************************************************************/

    /**
     * 显示POI视图
     */
    private void showPoiView() {
        mainView.setVisibility(View.GONE);
        poiView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏POI视图
     * @param v 视图
     */
    public void hidePoiView(View v) {
        mainView.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        poiView.setVisibility(View.GONE);
        aMap.clear(true);
    }

    /**
     * 显示Detail视图
     */
    private void showDetailListView() {
        mainView.setVisibility(View.GONE);
        detailListView.setVisibility(View.VISIBLE);
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poiLatLng, DETAIL_ZOOM), 250, null);
    }

    /**
     * 隐藏Detail视图(同时清除所有Detail的marker)
     */
    private void hideDetailListView() {
        mainView.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        detailListView.setVisibility(View.GONE);
        aMap.clear(true);
    }

    /**
     * 扩展Search视图(收起其它视图，隐藏mapStyle，扩展panel，改变mainSearch的图标、文字、光标，显示searchHistoryView)
     * @param v 视图
     */
    public void showSearchView(View v) {
        if (poiView.getVisibility() == View.VISIBLE) {
            hidePoiView(null);
        }
        if (detailListView.getVisibility() == View.VISIBLE) {
            hideDetailListView();
        }
        findViewById(R.id.main_map_style).setVisibility(View.GONE);
        layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        mainSearch.setSearchImg(R.drawable.back);
        mainSearch.setSearchText("搜索");
        findViewById(R.id.search_history_view).setVisibility(View.VISIBLE);
        adaptHistoryList();
    }

    /**
     * 收起Search视图(收起panel，改变mainSearch的图标、文字、收起软键盘，隐藏searchHistoryView，显示mapStyle)
     * @param v 视图
     */
    public void hideSearchView(View v) {
        layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        mainSearch.setSearchImg(R.drawable.search_input);
        mainSearchText.setText("");
        mainSearch.setSearchText(BaseUtils.getResourceString(this, R.string.search_hint_default));
        BaseUtils.hideKeyboard(this);
        mainSearchText.clearFocus();
        findViewById(R.id.search_history_view).setVisibility(View.GONE);
        findViewById(R.id.main_map_style).setVisibility(View.VISIBLE);
    }

    /**
     * 显示有文本后的搜索视图
     */
    public void showAfterSearchView(){
        mainSearch.setGroupVisible(View.VISIBLE);
        findViewById(R.id.search_history_view).setVisibility(View.GONE);
        searchResultList.setVisibility(View.VISIBLE);
    }

    /**
     * 收起有文本后的搜索视图
     */
    public void hideAfterSearchView(){
        mainSearch.setGroupVisible(View.GONE);
        findViewById(R.id.search_history_view).setVisibility(View.VISIBLE);
        searchResultList.setVisibility(View.GONE);
    }

    /**
     * 显示Present视图(收起软键盘，显示presentText及其文字，收起panel)
     * @param text PresentText的文字
     */
    private void showPresentView(String text) {
        BaseUtils.hideKeyboard(this);
        presentText.setVisibility(View.VISIBLE);
        presentText.setText(text);
        layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    /**
     * 隐藏Present视图(清除presentText文字并隐藏，显示软键盘，隐藏其它View，显示mainSearch与searchHistoryList，伸展panel)
     */
    public void hidePresentView() {
        if (poiView.getVisibility() == View.VISIBLE) {
            hidePoiView(null);
        }
        if (detailListView.getVisibility() == View.VISIBLE) {
            hideDetailListView();
        }
        presentText.setText("");
        presentText.setVisibility(View.GONE);
        mainSearchText.requestFocus();
        layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        adaptHistoryList();
    }

    /* ************************************************************************************************
     *
     * 以下为核心搜索方法，包括POI ID搜索、POI关键字搜索、车站搜索、线路搜索、逆地理编码搜索、输入提示搜索
     *
     * ************************************************************************************************/

    /**
     * POI ID查询方法
     * @param poi 当前的POI
     * @param flag 外部传入的标志，判断查询场景(<code>true</code> - 搜索后的查询；<code>false</code> - 点击后的查询)
     */
    public void searchByPoiId(Poi poi, boolean flag) {
        // 设置查询标志
        searchFinished = false;
        // 清除原有标记
        aMap.clear(true);
        // 添加标记
        poiLatLng = poi.getCoordinate();
        MapUtils.addSingleMapMarker(aMap, poiLatLng);
        // 将POI移动到地图中心
        aMap.animateCamera(CameraUpdateFactory.changeLatLng(poiLatLng), 250, null);
        // 异步查询监听
        startPoiIdSearch(poi.getPoiId(), new OnPoiIDSearchListener());
        // 子线程查询结果检测
        searchDetection(POI_ID);
        // 依据查询的场景进行附加的显示操作
        if (flag) {
            showPresentView(poi.getName());
            searchButton.setVisibility(View.GONE);
            poiCancel.setVisibility(View.GONE);
        } else {
            searchButton.setVisibility(View.VISIBLE);
            poiCancel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * POI 关键字查询方法
     * @param keyword 关键字
     */
    public void searchByPoiKeyword(String keyword) {
        searchFinished = false;
        aMap.clear(true);
        startPoiKeywordSearch(keyword, new OnPoiKeywordSearchListener());
        searchDetection(POI_KEYWORD);
        // 一定是在查询中用到的，因此无需判断
        searchButton.setVisibility(View.GONE);
        showPresentView(keyword);
    }

    /**
     * 车站查询方法
     * @param stationName 车站名
     */
    public void searchByBusStation(String stationName) {
        searchFinished = false;
        aMap.clear(true);
        startBusStationSearch(stationName, new OnBusStationSearchListener());
        searchDetection(BUS_STATION);
        showPresentView(stationName);
    }

    /**
     * 线路查询方法
     * @param lineName 线路名
     */
    public void searchByBusLine(String lineName) {
        searchFinished = false;
        aMap.clear(true);
        startBusLineSearch(lineName, new OnBusLineSearchListener());
        searchDetection(BUS_LINE);
        showPresentView(lineName);
    }

    /**
     * POI ID查询
     * @param poiId POI ID
     * @param onPoiSearchListener 查询结果回调
     */
    void startPoiIdSearch(String poiId, PoiSearch.OnPoiSearchListener onPoiSearchListener) {
        PoiSearch poiSearch = new PoiSearch(this, null);
        poiSearch.setOnPoiSearchListener(onPoiSearchListener);
        poiSearch.searchPOIIdAsyn(poiId);
    }

    /**
     * POI 关键字查询
     * @param keyword 关键字
     * @param onPoiSearchListener 查询结果回调
     */
    void startPoiKeywordSearch(String keyword, PoiSearch.OnPoiSearchListener onPoiSearchListener) {
        // 第一个参数表示关键字，第二参数表示POI类型，第三个参数表示是所在城市
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", myCity);
        query.setPageSize(SEARCH_PAGE_SIZE);
        query.setPageNum(0);
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(onPoiSearchListener);
        poiSearch.searchPOIAsyn();
    }

    /**
     * 逆地理编码查询
     * @param latLng 地理坐标
     * @param onGeocodeSearchListener 查询结果回调
     */
    void startReGeocodeSearch(LatLng latLng, GeocodeSearch.OnGeocodeSearchListener onGeocodeSearchListener) {
        LatLonPoint latLonPoint = MapUtils.latLngToLatLonPoint(latLng);
        // 第一个参数表示一个LatLngPoint，第二参数表示查询范围，第三个参数表示是坐标系类型
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 2000, GeocodeSearch.AMAP);
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(onGeocodeSearchListener);
        geocodeSearch.getFromLocationAsyn(query);
    }

    /**
     * 线路查询
     * @param busCode 线路码(线路名)
     * @param onBusLineSearchListener 查询结果回调
     */
    void startBusLineSearch(String busCode, BusLineSearch.OnBusLineSearchListener onBusLineSearchListener) {
        // 第一个参数表示公交线路名，第二个参数表示公交线路查询，第三个参数表示所在城市名或者城市区号
        BusLineQuery busLineQuery = new BusLineQuery(busCode, BusLineQuery.SearchType.BY_LINE_NAME, myCity);
        busLineQuery.setPageSize(SEARCH_PAGE_SIZE);
        busLineQuery.setPageNumber(0);
        BusLineSearch busLineSearch = new BusLineSearch(this, busLineQuery);
        busLineSearch.setOnBusLineSearchListener(onBusLineSearchListener);
        busLineSearch.searchBusLineAsyn();
    }

    /**
     * 车站查询
     * @param stopName 车站名
     * @param onBusStationSearchListener 查询结果回调
     */
    void startBusStationSearch(String stopName, BusStationSearch.OnBusStationSearchListener onBusStationSearchListener) {
        // 第一个参数表示公交车站名，第二个参数表示所在城市名或者城市区号
        BusStationQuery busStationQuery = new BusStationQuery(stopName, myCity);
        busStationQuery.setPageSize(SEARCH_PAGE_SIZE);
        busStationQuery.setPageNumber(0);
        BusStationSearch busStationSearch = new BusStationSearch(this, busStationQuery);
        busStationSearch.setOnBusStationSearchListener(onBusStationSearchListener);
        busStationSearch.searchBusStationAsyn();
    }

    /**
     * 输入提示查询
     * @param s 输入字符串
     * @param inputTipsListener 查询结果回调
     */
    void startInputSearch(String s, Inputtips.InputtipsListener inputTipsListener) {
        InputtipsQuery inputQuery = new InputtipsQuery(s, myCity);
        inputQuery.setCityLimit(true);
        Inputtips inputTips = new Inputtips(this, inputQuery);
        inputTips.setInputtipsListener(inputTipsListener);
        inputTips.requestInputtipsAsyn();
    }

    /* ************************************************************************************************
     *
     * 以下为其它重写的监听，包括我的位置改变监听、触摸监听、长按监听、回退监听、视角监听等
     *
     * ************************************************************************************************/

    /**
     * 我的位置改变监听(更新myLatLng)
     * @param location 我的位置
     */
    @Override
    public void onMyLocationChange(Location location) {
        myLatLng = MapUtils.locationToLatLng(location);
    }

    /**
     * 视角改变监听
     * @param cameraPosition 当前视角
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        rotateCompass(cameraPosition.bearing);
    }

    /**
     * 地图长按监听(搜索视图下屏蔽，长按显示长按点的地理位置)
     * @param v 坐标X
     * @param v1 坐标Y
     */
    @Override
    public void onLongPress(float v, float v1) {
        if(presentText.getVisibility() == View.GONE) {
            /*清除原有标记*/
            aMap.clear(true);
            /*添加新标记*/
            Point point = new Point((int) v, (int) v1);
            poiLatLng = MapUtils.screenToGeoLocation(aMap, point);
            MapUtils.addSingleMapMarker(aMap, poiLatLng);
            /*获取标记的信息*/
            startReGeocodeSearch(poiLatLng, new MainActivity.OnGeocodeSearchListener());
            /*设置标记信息框可见*/
            showPoiView();
        }
    }

    /**
     * 触摸监听
     * @param v 视图
     * @param event 触摸事件
     * @return boolean
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        if (layout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
            touchTimes++;
            if (touchTimes == 2) {
                touchTimes = 0;
                switch (v.getId()) {
                    // 点击搜索文本，显示扩展搜索视图
                    case R.id.my_search_text:
                        showSearchView(null);
                        break;
                    // 点击PresentText，根据点击的区域确定方法
                    case R.id.present_text:
                        Drawable drawableLeft = presentText.getCompoundDrawables()[0];
                        float x = event.getX();
                        // left drawable click
                        if (x < presentText.getPaddingStart() + drawableLeft.getIntrinsicWidth()) {
                            onPresentTextLeftClick();
                        } else {
                            mainSearchText.setText(presentText.getText());
                            hidePresentView();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return false;
    }

    /**
     * 按键的监听(回退键，用于在不同视图下回退的情形)
     * @param keyCode 按键类型
     * @param event 按键事件
     * @return boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果为搜索结果界面
            if (presentText.getVisibility() == View.VISIBLE) {
                onPresentTextLeftClick();
            }
            // 如果为POI显示界面
            else if (poiView.getVisibility() == View.VISIBLE) {
                poiCancel.performClick();
            }
            // 如果为搜索界面
            else if (layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                onSearchImgClick(null);
            }
            // 都不是
            else {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    ToastUtils.show(context, "再按一次退出程序");
                    mExitTime = System.currentTimeMillis();
                } else {
                    this.finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /* ************************************************************************************************
     *
     * 以下为Activity的跳转方法
     *
     * ************************************************************************************************/

    /**
     * 进入RouteActivity
     * @param destLatLng 路径规划目的地地理坐标点
     */
    private void startRoute(LatLng destLatLng) {
        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtra("from", myLatLng);
        intent.putExtra("to", destLatLng);
        intent.putExtra("city", myCity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /* ************************************************************************************************
     *
     * 以下为其它方法，包括子线程句柄相关、城市设置、类的适配与转化等
     *
     * ************************************************************************************************/

    /**
     * 异步查询结果检测(开始异步查询后开启子线程，每100ms检测一次查询是否结束，如果查询结束终止线程，发送消息给句柄)
     * @param message 信息
     */
    private void searchDetection(int message) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (searchFinished) {
                    sendMessage(message);
                    break;
                }
            }
        }).start();
    }

    /**
     * 子线程发送Message
     * @param what 信息内容
     */
    private void sendMessage(int what) {
        Message message = new Message();
        message.what = what;
        handler.sendMessage(message);
    }

    /**
     * 修改定位策略(地图初始化结束后1500ms，发送信息修改定位策略，并设置城市信息)
     */
    private void editLocationStrategy() {
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        /*修改定位策略*/
        aMap.setMyLocationStyle(myLocationStyle);
        setCity();
    }

    /**
     * 设置我的城市信息(逆地理编码查询)
     */
    private void setCity() {
        if (notSetMyCity) {
            startReGeocodeSearch(myLatLng, new AdaptGeocodeSearchListener() {
                @Override
                public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                    if (i == CODE_AMAP_SUCCESS) {
                        myCity = regeocodeResult.getRegeocodeAddress().getCity();
                        notSetMyCity = false;
                    }
                    else {
                        ToastUtils.showError(context, i);
                    }
                }
            });
            myCity = Objects.isNull(myCity) ? "北京市" : myCity;
        }
    }

    /**
     * 将SharedPreferences中的内容取出，并适配于SearchHistoryList
     */
    private void adaptHistoryList() {
        /*设置sharedPreferences中的搜索历史*/
        List<Object> list = SharedPreferencesUtils.getObjectsInCapacity(context, "history");
        searchHistoryList.setVisibility(list.size() == 0 ? View.GONE : View.VISIBLE);
        List<SearchItem> searchItemList = list.stream().map(o -> (SearchItem) o).collect(Collectors.toList());
        if (searchItemList.size() != 0) {
            searchHistoryList.setAdapter(new SearchItemListAdapter(context, searchItemList, MainActivity.this::onSearchItemClick));
        }
    }

    /**
     * 将<code>Tip</code>转换为<code>SearchItem</code><br>
     * 用于在输入搜索文本后，将得到的搜索提示转换为需要的搜索结果，并展示在前端
     * @param context 当前的Activity
     * @param tipList <code>tip</code>列表(<code>tip</code>来自于搜索提示)
     * @return List(搜索结果类列表)
     */

    public List<SearchItem> tipToSearchItem(Context context, List<Tip> tipList){
        List<SearchItem> list = new ArrayList<>();
        for(Tip tip : tipList){
            SearchItem searchItem = new SearchItem();
            String detail, name = tip.getName(), address = tip.getAddress();
            int imgResId, type;
            if (tip.getPoint() == null) {
                if (tip.getPoiID().isEmpty()) {
                    // 此时tip为关键字
                    type = POI_KEYWORD;
                    imgResId = R.drawable.poi_area;
                    detail = "关键词";
                } else {
                    // 此时tip为公交线路
                    type = BUS_LINE;
                    imgResId = R.drawable.poi_busline;
                    detail = "公交线路";
                }
            } else if (name.contains("地铁站") || name.contains("公交站")) {
                // 此时tip为确定的车站
                type = BUS_STATION;
                imgResId = name.contains("地铁站") ? R.drawable.poi_subway : R.drawable.poi_bus;
                detail = BaseUtils.getResourceString(context, R.string.search_result_ex,
                        "车站", PresentUtils.getEasyBusLines(address));
            } else {
                // 此时tip为确定的POI
                type = POI_ID;
                imgResId = R.drawable.poi_poi;
                detail = address.isEmpty() ? "地点" : BaseUtils.getResourceString(context, R.string.search_result_ex,
                        "地点", address);
            }
            if(tip.getPoint() != null){
                searchItem.setPoint(tip.getPoint().toString());
            }
            searchItem.setId(tip.getPoiID());
            searchItem.setImageResId(imgResId);
            searchItem.setName(name);
            searchItem.setDetail(detail);
            searchItem.setType(type);
            list.add(searchItem);
        }
        return list;
    }

    /**
     * 根据DetailList中各个Detail的位置信息，在地图上添加Marker
     * @param searchDetailList DetailList
     */
    private void addMapMarkerByDetails(List<SearchDetail> searchDetailList) {
        int feed = 1;
        for (SearchDetail searchDetail : searchDetailList) {
            MapUtils.addSeriesMapMarker(aMap, searchDetail.getPoint(), feed++);
        }
    }

    /**
     * 旋转指南针适配当前地图方向
     * @param bearing 旋转角度
     */
    private void rotateCompass(float bearing) {
        bearing = 360 - bearing;
        // 按中心旋转，从lastBearing度旋转到bearing度
        RotateAnimation rotateAnimation = new RotateAnimation(lastBearing, bearing, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // 固定到动画结束的位置
        rotateAnimation.setFillAfter(true);
        compass.startAnimation(rotateAnimation);
        lastBearing = bearing;
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

    /* ************************************************************************************************
     *
     * 以下为异步查询结果的监听，包括文本改变、输入提示、POI ID与关键字查询、车站查询、线路查询、逆地理编码查询
     *
     * ************************************************************************************************/

    /**
     * 输入文本改变监听
     */
    class TextChangeListener extends AdaptTextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String str = s.toString().trim();
            if (str.length() > 0) {
                showAfterSearchView();
                // 开始进行输入提示查询
                startInputSearch(str, (tipList, i) -> {
                    if (i == CODE_AMAP_SUCCESS){
                        if(tipList != null && tipList.size() > 0){
                            // 得到的tips结果转换为SearchItems，并适配于ListView
                            List<SearchItem> searchItemList = tipToSearchItem(context, tipList);
                            searchResultList.setAdapter(new SearchItemListAdapter(context, searchItemList,
                                    MainActivity.this::onSearchItemClick));
                        } else {
                            ToastUtils.show(context, R.string.no_result);
                        }
                    } else {
                        ToastUtils.showError(context, i);
                    }
                });
            } else {
                hideAfterSearchView();
            }
        }
    }

    /**
     * POI ID查询结果监听
     */
    class OnPoiIDSearchListener extends AdaptPoiIDSearchListener {

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {
            if (i == CODE_AMAP_SUCCESS) {
                if(poiItem != null) {
                    // 将查询到的poiItem转换为SearchDetail，并赋值
                    SearchDetail searchDetail = DetailFactory.fromPoi(context, poiItem, myLatLng);
                    poiType.setText(searchDetail.getType());
                    poiName.setText(searchDetail.getTitle());
                    poiAddress.setText(searchDetail.getAddress());
                    poiDistance.setText(searchDetail.getDistance());
                    poiRoute.setTag(searchDetail.getPoint());
                    searchFinished = true;
                } else {
                    ToastUtils.show(context, R.string.no_result);
                }
            } else {
                ToastUtils.showError(context, i);
            }
        }
    }

    /**
     * POI 关键字查询结果监听
     */
    class OnPoiKeywordSearchListener extends AdaptPoiKeywordSearchListener {

        @Override
        public void onPoiSearched(PoiResult poiResult, int i) {
            if (i == CODE_AMAP_SUCCESS){
                if(poiResult != null && poiResult.getPois() != null && poiResult.getPois().size() > 0){
                    // 将查询到的poiItems转换为SearchDetails，适配ListView，并添加Markers
                    List<SearchDetail> searchDetailList = DetailFactory.fromPoiList(context, poiResult.getPois(), myLatLng);
                    detailListView.setAdapter(new SearchDetailListAdapter(context, searchDetailList,
                            MainActivity.this::onDetailRouteClick, MainActivity.this::onDetailLayoutClick));
                    addMapMarkerByDetails(searchDetailList);
                    poiLatLng = searchDetailList.get(0).getPoint();
                    searchFinished = true;
                } else {
                    ToastUtils.show(context, R.string.no_result);
                }
            } else {
                ToastUtils.showError(context, i);
            }
        }
    }

    /**
     * 线路查询结果监听
     */
    class OnBusLineSearchListener implements BusLineSearch.OnBusLineSearchListener {

        @Override
        public void onBusLineSearched(BusLineResult busLineResult, int i) {
            if (i == CODE_AMAP_SUCCESS) {
                if(busLineResult.getBusLines() != null && busLineResult.getBusLines().size() > 0){
                    // 将查询到的poiItems转换为SearchDetails，适配ListView
                    List<SearchDetail> searchDetailList = DetailFactory.fromBusLine(context, busLineResult.getBusLines(), myLatLng);
                    detailListView.setAdapter(new SearchDetailListAdapter(context, searchDetailList,
                            MainActivity.this::onDetailRouteClick, MainActivity.this::onDetailLayoutClick));
                    poiLatLng = searchDetailList.get(0).getPoint();
                    searchFinished = true;
                } else {
                    ToastUtils.show(context, R.string.no_result);
                }
            } else {
                ToastUtils.showError(context, i);
            }
        }
    }

    /**
     * 车站查询结果监听
     */
    class OnBusStationSearchListener implements BusStationSearch.OnBusStationSearchListener {

        @Override
        public void onBusStationSearched(BusStationResult busStationResult, int i) {
            if (i == CODE_AMAP_SUCCESS) {
                if(busStationResult.getBusStations() != null && busStationResult.getBusStations().size() > 0){
                    // 将查询到的poiItems转换为SearchDetails，适配ListView，并添加Markers
                    List<SearchDetail> searchDetailList = DetailFactory.fromBusStation(context, busStationResult.getBusStations(), myLatLng);
                    detailListView.setAdapter(new SearchDetailListAdapter(context, searchDetailList,
                            MainActivity.this::onDetailRouteClick, MainActivity.this::onDetailLayoutClick));
                    addMapMarkerByDetails(searchDetailList);
                    poiLatLng = searchDetailList.get(0).getPoint();
                    searchFinished = true;
                } else {
                    ToastUtils.show(context, R.string.no_result);
                }
            } else {
                ToastUtils.showError(context, i);
            }
        }
    }

    /**
     * 逆地理编码查询结果监听
     */
    class OnGeocodeSearchListener extends AdaptGeocodeSearchListener {
        @Override
        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
            if (i == CODE_AMAP_SUCCESS) {
                RegeocodeAddress r = regeocodeResult.getRegeocodeAddress();
                // 如果找不到
                if (r.getFormatAddress().isEmpty()) {
                    ToastUtils.show(context, R.string.no_result);
                    poiName.setText("地图选点");
                    poiAddress.setText("");
                } else {
                    // 否则，构造地理信息
                    List<String> interestList = r.getPois()
                            .parallelStream()
                            .map(PoiItem::getTitle)
                            .collect(Collectors.toList());
                    String address = BaseUtils.getShortestOrElse(interestList, interestList.get(0), null);
                    poiName.setText(PresentUtils.getEasyRegeoAddress(regeocodeResult.getRegeocodeAddress()));
                    poiAddress.setText(BaseUtils.getResourceString(context, R.string.poi_nearby, address));
                }
                float distance = MapUtils.getLineDistance(myLatLng, poiLatLng);
                poiDistance.setText(PresentUtils.getEasyDistance(context, distance));
                poiType.setText("");
            }
            else {
                ToastUtils.showError(context, i);
            }
        }
    }
}