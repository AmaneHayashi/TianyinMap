package com.amane.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amane.bean.SearchItem;
import com.amane.tianyin.R;

import java.util.List;

/**
 * 搜索结果列表适配器
 * <br><code>com.amane.bean.SearchItem</code>适配
 * @author Amane Hayashi
 */
public class SearchItemListAdapter extends BaseAdapter {

    // 当前的Activity
    private Context context;
    // SearchItem列表
    private List<SearchItem> searchItemList;
    // 点击监听
    private View.OnClickListener onClickListener;

    @Override
    public int getCount() {
        return searchItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.view_my_search_item, null);
            holder.imageView = convertView.findViewById(R.id.my_item_img);
            holder.titleText = convertView.findViewById(R.id.my_item_name);
            holder.detailText = convertView.findViewById(R.id.my_item_detail);
            convertView.setTag(R.id.tag_holder, holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag(R.id.tag_holder);
        }
        final SearchItem searchItem = searchItemList.get(position);
        holder.imageView.setImageResource(searchItem.getImageResId());
        holder.titleText.setText(searchItem.getName());
        holder.detailText.setText(searchItem.getDetail());
        // 该Tag设置为搜索历史，用于在SharedPreferences记录该条目
        convertView.setTag(R.id.tag_history, searchItem);
        if(onClickListener != null){
            convertView.setOnClickListener(onClickListener);
        }
        return convertView;
    }

    public SearchItemListAdapter(Context context, List<SearchItem> searchItemList, View.OnClickListener onClickListener){
        this.context = context;
        this.searchItemList = searchItemList;
        this.onClickListener = onClickListener;
    }

    /**
     * View缓存
     * <code>R.layout.view_my_search_item</code>适配
     */
    class ViewHolder {
        ImageView imageView;
        TextView titleText, detailText;
    }
}
