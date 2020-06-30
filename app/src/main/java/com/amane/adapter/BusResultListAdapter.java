package com.amane.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amane.route.BusRouteDetailActivity;
import com.amane.tianyin.R;
import com.amane.utils.PresentUtils;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;

import java.util.List;

/**
 * 公交结果列表适配
 * @author Amane Hayashi
 */
public class BusResultListAdapter extends BaseAdapter {

	// 当前的Activity
	private Context mContext;
	// 公交路线列表
	private List<BusPath> busPathList;
	// 公交规划结果
	private BusRouteResult busRouteResult;

	public BusResultListAdapter(Context context, BusRouteResult busRouteResult) {
		mContext = context;
		this.busRouteResult = busRouteResult;
		busPathList = busRouteResult.getPaths();
	}
	
	@Override
	public int getCount() {
		return busPathList.size();
	}

	@Override
	public Object getItem(int position) {
		return busPathList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.view_my_bus_result_item, null);
			holder.busPathText = convertView.findViewById(R.id.bus_path_title);
			holder.busDesText = convertView.findViewById(R.id.bus_path_des);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final BusPath item = busPathList.get(position);
		// 设置线路(a路 > b路 > c路 > ...)
		holder.busPathText.setText(PresentUtils.getEasyBusPath(item));
		// 设置线路描述(时间 | 距离 | 需步行距离)
		holder.busDesText.setText(PresentUtils.getEasyBusPathDes(mContext, item));
		convertView.setOnClickListener(v -> {
			Intent intent = new Intent(mContext, BusRouteDetailActivity.class);
			intent.putExtra("bus_path", item);
			intent.putExtra("bus_result", busRouteResult);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		});
		return convertView;
	}

	/**
	 * View缓存
	 * <code>R.layout.view_my_bus_result_item</code>适配
	 */
	private class ViewHolder {
		TextView busPathText, busDesText;
	}
}
