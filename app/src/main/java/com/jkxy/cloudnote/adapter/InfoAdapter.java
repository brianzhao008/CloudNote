package com.jkxy.cloudnote.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jkxy.cloudnote.bean.NoteInfo;
import com.jkxy.cloudnote.R;

import java.util.List;

/**
 * Created by brian on 2017/4/27.
 * listview's adapter wad abandoned
 */

public class InfoAdapter extends BaseAdapter {

    private List<NoteInfo> list;
    private Context context;
    LayoutInflater inflater;
    private String TAG_Infoadapter = "infoadapter";

    public InfoAdapter(List<NoteInfo> list, LayoutInflater inflater){
        this.list = list;
        this.inflater = inflater;
    }

    public Context getContext() {
        return context;
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

        ViewHolder holder = null;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_cell,null);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (list.size() == 0){
            Log.e(TAG_Infoadapter , "something is worry");
        }

        holder.tvTitle.setText(list.get(position).getTitle());
        holder.tvDate.setText(list.get(position).getUpdatedAt());


        return convertView;
    }

    public class ViewHolder{
        TextView tvTitle;
        TextView tvDate;
    }


}

