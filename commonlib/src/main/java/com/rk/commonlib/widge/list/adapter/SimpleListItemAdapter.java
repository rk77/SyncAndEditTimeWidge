package com.rk.commonlib.widge.list.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rk.commonlib.R;

import java.util.List;

public class SimpleListItemAdapter extends ArrayAdapter<String> {
    private List<String> dataList;

    public SimpleListItemAdapter(Context context, List<String> list) {
        super(context, R.layout.list_item_layout, list);
        dataList = list;
    }

    public void setDataList(List<String> list) {
        dataList = list;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String zone = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        ImageView image = (ImageView) view.findViewById(R.id.image_icon);
        TextView textView = (TextView) view.findViewById(R.id.title);
        if (dataList != null && position < dataList.size()) {
            textView.setText(dataList.get(position));
        }
        return view;
    }

    @Override
    public int getCount() {
        if (dataList == null) {
            return 0;
        } else {
            return dataList.size();
        }
    }

    @Override
    public String getItem(int position) {
        if (dataList != null && position < dataList.size()) {
            return dataList.get(position);
        } else {
            return null;
        }
    }
}
