package com.amane.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amane.bean.Segment;
import com.amane.tianyin.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 通用的SegmentList适配
 * @author Amane Hayashi
 */
public class SegmentListAdapter extends BaseAdapter {

    // 当前的Activity
    private Context context;
    // 驾驶路径列表
    private List<Segment> stepList = new ArrayList<>();
    // 获得drawable的函数逻辑
    private Function<String, Integer> function;

    public SegmentListAdapter(Context context, List<Segment> list, Function<String, Integer> function) {
        this.context = context;
        this.function = function;
        // 添加起点
        stepList.add(new Segment());
        // 添加驾驶路径
        stepList.addAll(list);
        // 添加终点
        stepList.add(new Segment());
    }

    @Override
    public int getCount() {
        return stepList.size();
    }

    @Override
    public Object getItem(int position) {
        return stepList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SegmentListAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new SegmentListAdapter.ViewHolder();
            convertView = View.inflate(context, R.layout.view_my_bus_segment_item, null);
            holder.directionIcon = convertView.findViewById(R.id.bus_dir);
            holder.detailText = convertView.findViewById(R.id.bus_line_name);
            holder.upConcatLine = convertView.findViewById(R.id.bus_up_line);
            holder.downConcatLine = convertView.findViewById(R.id.bus_down_line);
            holder.topSplitLine = convertView.findViewById(R.id.bus_seg_split_line);
            convertView.setTag(holder);
        } else {
            holder = (SegmentListAdapter.ViewHolder) convertView.getTag();
        }
        final Segment segment = stepList.get(position);
        // 此时为起点
        if (position == 0) {
            holder.directionIcon.setImageResource(R.drawable.dir_start);
            holder.detailText.setText("出发");
            holder.upConcatLine.setVisibility(View.GONE);
            holder.downConcatLine.setVisibility(View.VISIBLE);
            holder.topSplitLine.setVisibility(View.GONE);
            return convertView;
        }
        // 此时为终点
        else if (position == stepList.size() - 1) {
            holder.directionIcon.setImageResource(R.drawable.dir_end);
            holder.detailText.setText("到达终点");
            holder.upConcatLine.setVisibility(View.VISIBLE);
            holder.downConcatLine.setVisibility(View.GONE);
            holder.topSplitLine.setVisibility(View.VISIBLE);
            return convertView;
        }
        // 此时为中间的各行驶路段
        else {
            // 根据传入Function设置对应的resID
            holder.directionIcon.setImageResource(function.apply(segment.getAction()));
            holder.detailText.setText(segment.getInstruction());
            holder.upConcatLine.setVisibility(View.VISIBLE);
            holder.downConcatLine.setVisibility(View.VISIBLE);
            holder.topSplitLine.setVisibility(View.VISIBLE);
            return convertView;
        }
    }

    /**
     * View缓存
     * <code>R.layout.view_my_bus_segment_item</code>适配
     */
    private class ViewHolder {
        TextView detailText;
        ImageView directionIcon, upConcatLine, downConcatLine, topSplitLine;
    }
}
