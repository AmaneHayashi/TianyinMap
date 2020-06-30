package com.amane.route;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.amane.adapter.BusSegmentListAdapter;
import com.amane.overlay.BusRouteOverlay;
import com.amane.tianyin.R;
import com.amane.utils.PresentUtils;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;

/**
 * 公交路线详情
 * @author Amane Hayashi & AMap
 */
public class BusRouteDetailActivity extends Activity {

	/* Activity */
	private Context context;

	/* 地图系列 */
	private AMap aMap;
	private MapView mapView;

	/* View组件系列 */
	private ConstraintLayout layout;
	private TextView titleText, infoText, costText;
	private ListView busSegmentList;
	private Button showMapButton;

	/* 路径系列 */
	private BusPath busPath;
	private BusRouteResult busRouteResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_detail);
		context = getApplicationContext();
		mapView = findViewById(R.id.route_map);
		mapView.onCreate(savedInstanceState);
		// 获得RouteActivity传入的数据
		getIntentData();
		// 初始化
		initId();
		initProperty();
	}

	/**
	 * 从Intent中获得数据(公交路径、公交结果)
	 */
	private void getIntentData() {
		Intent intent = getIntent();
		busPath = intent.getParcelableExtra("bus_path");
		busRouteResult = intent.getParcelableExtra("bus_result");
	}

	/**
	 * 初始化组件
	 */
	private void initId() {
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		titleText = findViewById(R.id.title_name);
		infoText = findViewById(R.id.info_text);
		costText = findViewById(R.id.cost_text);
		showMapButton = findViewById(R.id.title_show_map);
		busSegmentList = findViewById(R.id.segment_list);
	}

	/**
	 * 初始化组件属性
	 */
	private void initProperty(){
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
		aMap.setMyLocationEnabled(true);
		aMap.setMyLocationStyle(myLocationStyle);
		titleText.setText("公交路线详情");
		infoText.setText(PresentUtils.getEasyRouteInfo(context, busPath.getDuration(), busPath.getDistance()));
		costText.setText(PresentUtils.getEasyCostInfo(busRouteResult.getTaxiCost()));
		costText.setVisibility(View.VISIBLE);
		showMapButton.setVisibility(View.VISIBLE);
		layout = findViewById(R.id.route_detail);
		busSegmentList.setAdapter(new BusSegmentListAdapter(
				this.getApplicationContext(), busPath.getSteps()));
	}

	/**
	 * 返回键的点击监听(如果为详情视图，回退到上一Activity；如果是地图视图，回退到详情视图)
	 * @param view 视图
	 */
	public void onBackClick(View view) {
		if(mapView.getVisibility() == View.VISIBLE){
			showMapButton.setVisibility(View.VISIBLE);
			layout.setVisibility(View.VISIBLE);
			mapView.setVisibility(View.GONE);
		}
		else {
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackClick(null);
		}
		return false;
	}

	/**
	 * 地图按钮的点击监听(切换到mapView)
	 * @param view 视图
	 */
	public void onMapButtonClick(View view) {
		// 设置BusPathList、标题栏不可见，设置地图视图可见
		mapView.setVisibility(View.VISIBLE);
		layout.setVisibility(View.GONE);
		showMapButton.setVisibility(View.GONE);
		aMap.clear(true);
		// 新增Overlay，移走原来的，添加现在的
		BusRouteOverlay busRouteOverlay = new BusRouteOverlay(this, aMap, busPath,
				busRouteResult.getStartPos(), busRouteResult.getTargetPos());
		busRouteOverlay.removeFromMap();
		busRouteOverlay.addToMap();
		busRouteOverlay.zoomToSpan();
}

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
