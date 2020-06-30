package com.amane.adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.amane.bean.SearchDetail;
import com.amane.tianyin.R;

import java.util.List;

/**
 * 搜索详情列表适配器
 * <br><code>com.amane.bean.SearchDetail</code>适配
 * @author Amane Hayashi
 */
public class SearchDetailListAdapter extends BaseAdapter {

    // 当前的Activity
    private Context context;
    // Detail列表
    private List<SearchDetail> searchDetailList;
    // 点击监听
    private View.OnClickListener onRouteClickListener, onLayoutClickListener;

    public SearchDetailListAdapter(Context context, List<SearchDetail> searchDetailList,
                                   View.OnClickListener onRouteClickListener, View.OnClickListener onLayoutClickListener){
        this.context = context;
        this.searchDetailList = searchDetailList;
        this.onRouteClickListener = onRouteClickListener;
        this.onLayoutClickListener = onLayoutClickListener;
    }

    @Override
    public int getCount() {
        return searchDetailList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchDetailList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchDetailListAdapter.ViewHolder holder;
        if(convertView == null){
            holder = new SearchDetailListAdapter.ViewHolder();
            convertView = View.inflate(context, R.layout.view_my_search_detail, null);
            holder.layout = convertView.findViewById(R.id.my_detail_layout);
            holder.titleText = convertView.findViewById(R.id.my_detail_name);
            holder.distanceText = convertView.findViewById(R.id.my_detail_distance);
            holder.addressText = convertView.findViewById(R.id.my_detail_address);
            holder.typeText = convertView.findViewById(R.id.my_detail_type);
            holder.splitText = convertView.findViewById(R.id.my_detail_split);
            holder.routeButton = convertView.findViewById(R.id.my_detail_route);
            convertView.setTag(R.id.tag_holder, holder);
        }
        else {
            holder = (SearchDetailListAdapter.ViewHolder) convertView.getTag(R.id.tag_holder);
        }
        final SearchDetail searchDetail = searchDetailList.get(position);
        holder.titleText.setText(searchDetail.getTitle());
        holder.distanceText.setText(searchDetail.getDistance());
        holder.addressText.setText(searchDetail.getAddress());
        holder.typeText.setText(searchDetail.getType());
        // 该Tag设置为详情的地理坐标，用于路线规划
        holder.layout.setTag(R.id.tag_dest_point, searchDetail.getPoint());
        holder.routeButton.setTag(R.id.tag_dest_point, searchDetail.getPoint());
        holder.routeButton.setOnClickListener(onRouteClickListener);
        holder.layout.setOnClickListener(onLayoutClickListener);
        if(searchDetail.getOverlay() != null){
            // 该Tag设置为详情的Overlay，用于显示公交路线的线路
            holder.routeButton.setTag(R.id.tag_overlay, searchDetail.getOverlay());
        }
        if(searchDetail.getAddress() == null || searchDetail.getAddress().isEmpty()){
            holder.splitText.setVisibility(View.GONE);
        }
        return convertView;
    }

    /**
     * View缓存
     * <code>R.layout.view_my_search_detail</code>适配
     */
    class ViewHolder {
        ConstraintLayout layout;
        TextView titleText, distanceText, addressText, typeText, splitText;
        Button routeButton;
    }
}
