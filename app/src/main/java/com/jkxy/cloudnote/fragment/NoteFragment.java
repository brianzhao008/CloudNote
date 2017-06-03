package com.jkxy.cloudnote.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.jkxy.cloudnote.activity.LoginActivity;
import com.jkxy.cloudnote.adapter.InfoAdapter;
import com.jkxy.cloudnote.bean.NoteInfo;
import com.jkxy.cloudnote.R;
import com.jkxy.cloudnote.adapter.RvAdapter;
import com.jkxy.cloudnote.activity.AddNoteActivity;
import com.jkxy.cloudnote.activity.MainActivity;
import com.jkxy.cloudnote.activity.ShowNoteActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by brian on 2017/4/26.
 */

public class NoteFragment extends Fragment {

    private ListView mlistView;
    private InfoAdapter mAdapter;
    private List<NoteInfo> lists = new ArrayList<>();
    private RvAdapter mRvAdapter;

    private NoteInfo infos;
    private String ObjectID;
    private MainActivity mainActivity;

    public static Set<Integer> positionSets = new HashSet<>();

    private ActionMode actionMode;
    private MainActivity activity = new MainActivity();

    private BmobUser mUser = BmobUser.getCurrentUser();

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_cloudnote,container,false);

        //放弃的listview
//        mlistView = (ListView) rootView.findViewById(R.id.listView);
//        mAdapter = new InfoAdapter(lists,inflater);
//        mlistView.setAdapter(mAdapter);
//
//        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                infos = (NoteInfo) mAdapter.getItem(position);
//                ObjectID = infos.getObjectId();
//                Intent i = new Intent(getActivity(),ShowNoteActivity.class);
//                i.putExtra("_id",ObjectID);
//                startActivity(i);
//            }
//        });


        // recyclerview
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.lvRecycler);
        mRvAdapter = new RvAdapter(lists,getContext());
        recyclerView.setAdapter(mRvAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mRvAdapter.setOnItemClickListener(new RvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (actionMode != null){
                    addAddRemove(position);
                }else {
                    infos = mRvAdapter.getItem(position);
                    ObjectID = infos.getObjectId();
                    Intent i = new Intent(getActivity(),ShowNoteActivity.class);
                    i.putExtra("_id",ObjectID);
                    startActivity(i);
                }
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                if (actionMode == null){
                    activity = (MainActivity) getActivity(); // 还是用getactivity,不然会报错，没有了一些必要的东西
                    actionMode = activity.startSupportActionMode(new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            if (actionMode == null){
                                actionMode = mode;
                                MenuInflater inflater = mode.getMenuInflater();
                                inflater.inflate(R.menu.menu_delete,menu);
                                return true;
                            }else {
                                return false;
                            }
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_delete:
                                    // 删除已选
                                    Set<NoteInfo> valueSet = new HashSet<>();
                                    for (int position : positionSets) {
                                        valueSet.add(mRvAdapter.getItem(position));
                                    }
                                    for (NoteInfo val : valueSet) {
                                        val.delete(new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                if (e == null){
                                                    Log.i("bomb delete","success");
                                                }else {
                                                    Toast.makeText(getActivity(),R.string.cloudnote_delete_success,Toast.LENGTH_SHORT);
                                                }
                                            }
                                        });
                                        mRvAdapter.remove(val);
                                    }
                                    mode.finish();
                                    return true;
                                default:
                                    return false;
                            }
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            actionMode = null;
                            positionSets.clear();
                            mRvAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        if (mUser != null){
            queryAllByUser(mUser.getUsername());
        }else {
            startActivity(new Intent(getActivity(),LoginActivity.class));
        }


        return rootView;
    }


    private void addAddRemove(int position) {
        if (positionSets.contains(position)) {
            // 如果包含，则撤销选择
            positionSets.remove(position);
        } else {
            // 如果不包含，则添加
            positionSets.add(position);
        }
        if (positionSets.size() == 0) {
            // 如果没有选中任何的item，则退出多选模式
            actionMode.finish();
        } else {
            // 设置ActionMode标题
            actionMode.setTitle(positionSets.size() + " 已选择");
            // 更新列表界面，否则无法显示已选的item
            mRvAdapter.notifyDataSetChanged();
        }
    }

    // 通过user查找数据
    private void queryAllByUser(String uName){
        BmobQuery<NoteInfo> query = new BmobQuery<>();
        query.order("-createdAt");// 按照时间降序
        query.addWhereEqualTo("userName",uName);
        query.findObjects(new FindListener<NoteInfo>() {
            @Override
            public void done(List<NoteInfo> list, BmobException e) {
                if (list == null){
                    startActivity(new Intent(getActivity(),AddNoteActivity.class));
                }
                if (e == null){
                    lists.clear();

                    lists.addAll(list);
                }
                mRvAdapter.notifyDataSetChanged();
            }
        });
    }

    // 刷新fragment
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        mainActivity.setHandler(mhandler);
    }

    // handler通知
    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                queryAllByUser(mUser.getUsername());
            }
        }
    };

}










//                    for (NoteInfo ifs : list){
//                        NoteInfo noteInfo = new NoteInfo();
//                        noteInfo.setContent(ifs.getUpdatedAt());
//                        System.out.println("notefragment + " + ifs.getUpdatedAt());
//                        noteInfo.setTitle(ifs.getTitle());
//                        System.out.println("notefragment + " + ifs.getTitle());
//                        lists.add(noteInfo);
//                    }
//                    Log.e("NoteFragment", "queryall no error");