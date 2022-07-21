package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TextAdapter extends BaseAdapter {
    private List<String> data = new ArrayList<>();

    Context context;

    void setData(List<String> mData){
        data.clear();
        data.addAll(mData);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item,parent,false);
            convertView.setTag(new ViewHolder((TextView)convertView.findViewById(R.id.myItem)));
        }
        ViewHolder holder = (ViewHolder)convertView.getTag();
        final String item = data.get(position);
        holder.info.setText(item.substring(item.lastIndexOf('/')+1));
        return convertView;
    }
}
