package com.amane.route;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.amane.adapter.SegmentListAdapter;
import com.amane.bean.Segment;
import com.amane.tianyin.R;
import com.amane.utils.PresentUtils;
import com.amane.utils.SegmentFactory;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;

import java.util.List;

/**
 * 驾驶路线详情
 * @author Amane Hayashi
 */
public class DriveRouteDetailActivity extends Activity {

	/* Activity */
	private Context context;

	/* View组件系列 */
	private TextView titleText, infoText, costText;
	private ListView driveSegmentList;

	/* 路径系列 */
	private DrivePath drivePath;
	private DriveRouteResult driveRouteResult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_detail);
		context = getApplicationContext();
		// 获得RouteActivity传入的数据
		getIntentData();
		// 初始化
		initId();
		initProperty();
	}

	/**
	 * 从Intent中获得数据(驾驶路径、驾驶结果)
	 */
	private void getIntentData() {
		Intent intent = getIntent();
		// 获得数据
		drivePath = intent.getParcelableExtra("drive_path");
		driveRouteResult = intent.getParcelableExtra("drive_result");
	}

	/**
	 * 初始化组件
	 */
	private void initId() {
		titleText = findViewById(R.id.title_name);
		infoText = findViewById(R.id.info_text);
		costText = findViewById(R.id.cost_text);
		driveSegmentList = findViewById(R.id.segment_list);
	}

	/**
	 * 初始化组件属性
	 */
	private void initProperty(){
		titleText.setText("驾车路线详情");
		infoText.setText(PresentUtils.getEasyRouteInfo(context, drivePath.getDuration(), drivePath.getDistance()));
		costText.setText(PresentUtils.getEasyCostInfo(driveRouteResult.getTaxiCost()));
		costText.setVisibility(View.VISIBLE);
		List<Segment> segmentList = SegmentFactory.fromDriveSteps(drivePath.getSteps());
		driveSegmentList.setAdapter(new SegmentListAdapter(context, segmentList, PresentUtils::getDriveDrawableID));
	}

	/**
	 * 返回键的点击监听(回退到上一Activity)
	 * @param view 视图
	 */
	public void onBackClick(View view) {
		finish();
	}

    public void onMapButtonClick(View view) {}
}
