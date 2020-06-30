package com.amane.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amane.bean.SegBusStep;
import com.amane.tianyin.R;
import com.amane.utils.BaseUtils;
import com.amane.utils.PresentUtils;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RailwayStationItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 公交Segment适配
 * @author AMap
 */
public class BusSegmentListAdapter extends BaseAdapter {

	// 当前的Activity
	private Context mContext;
	// 格式化公交路径列表
	private List<SegBusStep> sBusStepList = new ArrayList<>();

	public BusSegmentListAdapter(Context context, List<BusStep> list) {
		this.mContext = context;
		// 添加起点
		SegBusStep start = new SegBusStep(null);
		start.setStart(true);
		sBusStepList.add(start);
		for (BusStep busStep : list) {
			// 如果是步行
			if (busStep.getWalk() != null && busStep.getWalk().getDistance() > 0) {
				SegBusStep walk = new SegBusStep(busStep);
				walk.setWalk(true);
				sBusStepList.add(walk);
			}
			// 如果是公交
			if (busStep.getBusLines() != null && busStep.getBusLines().size() > 0) {
				SegBusStep bus = new SegBusStep(busStep);
				bus.setBus(true);
				sBusStepList.add(bus);
			}
			// 如果是铁路
			if (busStep.getRailway() != null) {
				SegBusStep railway = new SegBusStep(busStep);
				railway.setRailway(true);
				sBusStepList.add(railway);
			}
			// 如果是出租
			if (busStep.getTaxi() != null) {
				SegBusStep taxi = new SegBusStep(busStep);
				taxi.setTaxi(true);
				sBusStepList.add(taxi);
			}
		}
		// 添加终点
		SegBusStep end = new SegBusStep(null);
		end.setEnd(true);
		sBusStepList.add(end);
	}

	@Override
	public int getCount() {
		return sBusStepList.size();
	}

	@Override
	public Object getItem(int position) {
		return sBusStepList.get(position);
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
			convertView = View.inflate(mContext, R.layout.view_my_bus_segment_item, null);
			holder.parent = convertView.findViewById(R.id.bus_item);
			holder.detailText = convertView.findViewById(R.id.bus_line_name);
			holder.directionIcon = convertView.findViewById(R.id.bus_dir);
			holder.stationNumText = convertView.findViewById(R.id.bus_stations);
			holder.stationDetailIcon = convertView.findViewById(R.id.bus_more);
			holder.upConcatLine = convertView.findViewById(R.id.bus_up_line);
			holder.downConcatLine = convertView.findViewById(R.id.bus_down_line);
			holder.topSplitLine = convertView.findViewById(R.id.bus_seg_split_line);
			holder.detailLayout = convertView.findViewById(R.id.expand_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final SegBusStep item = sBusStepList.get(position);
		// 如果是起点
		if (position == 0) {
			holder.directionIcon.setImageResource(R.drawable.dir_start);
			holder.detailText.setText("出发");
			holder.upConcatLine.setVisibility(View.INVISIBLE);
			holder.downConcatLine.setVisibility(View.VISIBLE);
			holder.topSplitLine.setVisibility(View.GONE);
			holder.stationNumText.setVisibility(View.GONE);
			holder.stationDetailIcon.setVisibility(View.GONE);
			return convertView;
		}
		// 如果是终点
		else if (position == sBusStepList.size() - 1) {
			holder.directionIcon.setImageResource(R.drawable.dir_end);
			holder.detailText.setText("到达终点");
			holder.upConcatLine.setVisibility(View.VISIBLE);
			holder.downConcatLine.setVisibility(View.INVISIBLE);
			holder.stationNumText.setVisibility(View.INVISIBLE);
			holder.stationDetailIcon.setVisibility(View.INVISIBLE);
			return convertView;
		}
		// 如果是中间路段
		else {
			// 如果是走路
			if (item.isWalk() && item.getWalk() != null && item.getWalk().getDistance() > 0) {
				holder.directionIcon.setImageResource(R.drawable.dir13);
				holder.upConcatLine.setVisibility(View.VISIBLE);
				holder.downConcatLine.setVisibility(View.VISIBLE);
				holder.detailText.setText(BaseUtils.getResourceString(mContext, R.string.walk,
						(int)item.getWalk().getDistance()));
				holder.stationNumText.setVisibility(View.GONE);
				holder.stationDetailIcon.setVisibility(View.GONE);
				return convertView;
			}
			// 如果是公交
			else if (item.isBus() && item.getBusLines().size() > 0) {
				holder.directionIcon.setImageResource(R.drawable.dir14);
				holder.upConcatLine.setVisibility(View.VISIBLE);
				holder.downConcatLine.setVisibility(View.VISIBLE);
				holder.detailText.setText(item.getBusLines().get(0).getBusLineName());
				holder.stationNumText.setVisibility(View.VISIBLE);
				holder.stationNumText.setText(BaseUtils.getResourceString(mContext, R.string.stations,
						item.getBusLines().get(0).getPassStationNum() + 1));
				holder.stationDetailIcon.setVisibility(View.VISIBLE);
				// 点击查看详情时的类
				MoreDetailClick mdClick = new MoreDetailClick(holder, item);
				holder.parent.setTag(position);
				holder.parent.setOnClickListener(mdClick);
				return convertView;
			}
			// 如果是铁路
			else if (item.isRailway() && item.getRailway() != null) {
				holder.directionIcon.setImageResource(R.drawable.dir16);
				holder.upConcatLine.setVisibility(View.VISIBLE);
				holder.downConcatLine.setVisibility(View.VISIBLE);
				holder.detailText.setText(item.getRailway().getName());
				holder.stationNumText.setVisibility(View.VISIBLE);
				holder.stationNumText.setText(BaseUtils.getResourceString(mContext, R.string.stations,
						item.getRailway().getViastops().size() + 1));
				holder.stationDetailIcon.setVisibility(View.VISIBLE);
				MoreDetailClick mdClick = new MoreDetailClick(holder, item);
				holder.parent.setTag(position);
				holder.parent.setOnClickListener(mdClick);
				return convertView;
			}
			// 如果是打车
			else if (item.isTaxi() && item.getTaxi() != null) {
				holder.directionIcon.setImageResource(R.drawable.dir14);
				holder.upConcatLine.setVisibility(View.VISIBLE);
				holder.downConcatLine.setVisibility(View.VISIBLE);
				holder.detailText.setText("打车到终点");
				holder.stationNumText.setVisibility(View.GONE);
				holder.stationDetailIcon.setVisibility(View.GONE);
				return convertView;
			}
		}
		return convertView;
	}

	/**
	 * View缓存
	 * <code>R.layout.view_my_bus_segment_item</code>适配
	 */
	private class ViewHolder {
		public RelativeLayout parent;
		TextView detailText, stationNumText;
		ImageView directionIcon, stationDetailIcon, upConcatLine, downConcatLine, topSplitLine;
		LinearLayout detailLayout;
		boolean detailShown = false;
	}

	/**
	 * 点击详情按钮，查看公交/铁路的车站途径信息
	 */
	private class MoreDetailClick implements OnClickListener {
		private ViewHolder mHolder;
		private SegBusStep mItem;

		MoreDetailClick(final ViewHolder holder, final SegBusStep item) {
			mHolder = holder;
			mItem = item;
		}

		@Override
		public void onClick(View v) {
			int position = Integer.parseInt(String.valueOf(v.getTag()));
			// 获得当前的路段对应的SchemeBusStep
			mItem = sBusStepList.get(position);
			if (mItem.isBus()) {
				// 如果详情未展开，设置为已展开
				if (!mHolder.detailShown) {
					mHolder.detailShown = true;
					// 向下图标改为向上
					mHolder.stationDetailIcon.setImageResource(R.drawable.up);
					// 添加出发站
					addBusStation(mItem.getBusLines().get(0).getDepartureBusStation());
					// 添加途经站
					for (BusStationItem station : mItem.getBusLines().get(0).getPassStations()) {
						addBusStation(station);
					}
					// 添加终到站
					addBusStation(mItem.getBusLines().get(0).getArrivalBusStation());
				// 否则设置为未展开
				} else {
					mHolder.detailShown = false;
					// 向上图标改为向下
					mHolder.stationDetailIcon.setImageResource(R.drawable.down);
					// 移除详情
					mHolder.detailLayout.removeAllViews();
				}
			} else if (mItem.isRailway()) {
				if (!mHolder.detailShown) {
					mHolder.detailShown = true;
					mHolder.stationDetailIcon.setImageResource(R.drawable.up);
					addRailwayStation(mItem.getRailway().getDeparturestop());
					for (RailwayStationItem station : mItem.getRailway().getViastops()) {
						addRailwayStation(station);
					}
					addRailwayStation(mItem.getRailway().getArrivalstop());

				} else {
					mHolder.detailShown = false;
					mHolder.stationDetailIcon.setImageResource(R.drawable.down);
					mHolder.detailLayout.removeAllViews();
				}
			}
		}

		/**
		 * 添加公交车站
		 * @param station 公交车站
		 */
		private void addBusStation(BusStationItem station) {
			// 添加新的公交车站对应的视图
			LinearLayout layout = (LinearLayout) View.inflate(mContext, R.layout.view_my_bus_segment_ex_item, null);
			TextView tv = layout.findViewById(R.id.bus_line_station_name);
			tv.setText(station.getBusStationName());
			mHolder.detailLayout.addView(layout);
		}

		/**
		 * 添加火车站
		 * @param station 火车站
		 */
		private void addRailwayStation(RailwayStationItem station) {
			// 添加新的铁路车站对应的视图
			LinearLayout layout = (LinearLayout) View.inflate(mContext, R.layout.view_my_bus_segment_ex_item, null);
			TextView tv = layout.findViewById(R.id.bus_line_station_name);
			tv.setText(PresentUtils.getEasyRailwayInfo(station.getName(), station.getTime()));
			mHolder.detailLayout.addView(layout);
		}
	}
}
