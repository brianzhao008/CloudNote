package com.jkxy.cloudnote.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jkxy.cloudnote.bean.NoteInfo;
import com.jkxy.cloudnote.R;
import com.jkxy.cloudnote.fragment.NoteFragment;

import java.util.Set;

/**
 * Created by brian on 2017/5/7.
 */

public class RvHolder extends RecyclerView.ViewHolder {

    private TextView tvTitle;
    private TextView tvDate;

    private Context context;

    public RvHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
        tvDate = (TextView) itemView.findViewById(R.id.tvDate);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setDataView(NoteInfo infos, int position){
        Set<Integer> positionSet = NoteFragment.positionSets;
        if (positionSet.contains(position)){
            itemView.setBackground(context.getResources().getDrawable(R.drawable.bg_selected));
        }else {
            itemView.setBackground(context.getResources().getDrawable(R.drawable.btn_common));
        }
        tvTitle.setText(infos.getTitle());
        tvDate.setText(infos.getUpdatedAt());
    }
}
