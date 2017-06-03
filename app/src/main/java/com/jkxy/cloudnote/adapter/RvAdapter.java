package com.jkxy.cloudnote.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jkxy.cloudnote.bean.NoteInfo;
import com.jkxy.cloudnote.R;

import java.util.List;

/**
 * Created by brian on 2017/5/7.
 * the new recyclerview
 */

public class RvAdapter extends RecyclerView.Adapter<RvHolder> {

    private List<NoteInfo> list;
    private Context context;
    LayoutInflater inflater;

    // 回调
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public RvAdapter(List<NoteInfo> list, Context context){
        this.list = list;
//        this.inflater = inflater;
//        context = inflater.getContext();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public RvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.listview_cell,parent,false);
        RvHolder holder = new RvHolder(view,context);
        return holder;
    }

    @Override
    public void onBindViewHolder(RvHolder holder, final int position) {
        holder.setDataView(list.get(position),position);
        if (onItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v,position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v,position);
                    return false;
                }
            });
        }
    }

    public void remove (NoteInfo ifs){
        list.remove(ifs);
        notifyDataSetChanged();
    }

    public NoteInfo getItem(int pos){
        return list.get(pos);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // list的点击事件
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view,int position);
    }
}
