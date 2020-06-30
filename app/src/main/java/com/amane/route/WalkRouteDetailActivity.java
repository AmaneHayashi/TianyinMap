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
import com.amap.api.services.route.WalkPath;

import java.util.List;

/**
 * 步行路线详情
 * @author Amane Hayashi
 */
public class WalkRouteDetailActivity extends Activity {

	/* Activity */
	private Context context;

	/* View组件系列 */
	private TextView titleText, infoText;
	private ListView walkSegmentList;

	/* 路径系列 */
	private WalkPath walkPath;

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
	 * 从Intent中获得数据(步行路径)
	 */
	private void getIntentData() {
		Intent intent = getIntent();
		// 获得数据
		walkPath = intent.getParcelableExtra("walk_path");
	}

	/**
	 * 初始化组件
	 */
	private void initId(){
		titleText = findViewById(R.id.title_name);
		infoText = findViewById(R.id.info_text);
		walkSegmentList = findViewById(R.id.segment_list);
	}

	/**
	 * 初始化组件属性
	 */
	private void initProperty(){
		titleText.setText("步行路线详情");
		infoText.setText(PresentUtils.getEasyRouteInfo(context, walkPath.getDuration(), walkPath.getDistance()));
		List<Segment> segmentList = SegmentFactory.fromWalkSteps(walkPath.getSteps());
		walkSegmentList.setAdapter(new SegmentListAdapter(context, segmentList, PresentUtils::getWalkDrawableID));
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
