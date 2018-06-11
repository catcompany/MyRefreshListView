package com.refresh.customrefreshlistview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuangZc on 2018/5/18.
 */

public class MyCustomAdapter extends BaseAdapter {

    private Context context;

    private List<String> list;

    public MyCustomAdapter(Context context) {
        this.context = context;
        this.list = getList();
    }

    public MyCustomAdapter(Context context, List<String> listData) {
        this.context = context;
        this.list = listData;
    }

    public void updateFirstList(String str) {
        if (list != null && list.size() > 0)
            list.set(0, str);
    }

    private List<String> getList() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add("这是第" + i + "个item");
        }
        return list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
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
            convertView = View.inflate(context, R.layout.personal_item_setting, null);
            holder.tvContent = (TextView) convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvContent.setText(list.get(position));
        return convertView;
    }

    public class ViewHolder {
        TextView tvContent;
    }

}