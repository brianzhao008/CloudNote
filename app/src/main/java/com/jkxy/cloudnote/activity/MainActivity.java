package com.jkxy.cloudnote.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jkxy.cloudnote.fragment.NoteFragment;
import com.jkxy.cloudnote.R;
import com.jkxy.cloudnote.fragment.SetFragment;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

public class MainActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private TabLayout.Tab notecloud;
    private TabLayout.Tab setting;

    private ImageView ivLogin;
    private LogInListener loginListener = new LogInListener();

    private Handler mhandler;

    private static final String BMOB_API_KEY = "78723b5b4e1e08f36c8e01e5ca0a6e6a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化bmob
        Bmob.initialize(this,BMOB_API_KEY);

        BmobUser user = BmobUser.getCurrentUser();
        if (user == null){
            startActivity(new Intent(this,LoginActivity.class));
        }

        initViews();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AddNoteActivity.class));
            }
        });
    }

    public class LogInListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            BmobUser user = BmobUser.getCurrentUser();
            if (user == null){
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }else {
                Toast.makeText(MainActivity.this,R.string.username_already_login,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews(){
        //自定义toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView textView = (TextView) findViewById(R.id.toolbar_title);
        textView.setText(R.string.cloudnote_note);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        BmobUser user = BmobUser.getCurrentUser();
        if (user == null){
            startActivity(new Intent(this,LoginActivity.class));
        }

        // 头像登入
        ivLogin = (ImageView) findViewById(R.id.LoginView);
        ivLogin.setOnClickListener(loginListener);

        // tab fragment
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            private String[] mTitles = new String[]{"云笔记","设置"};

            @Override
            public Fragment getItem(int position) {
                if (position == 0){
                    return new NoteFragment(); //main
                }else {
                    return new SetFragment(); //setting
                }
            }

            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles[position];
            }
        });

        mTabLayout.setupWithViewPager(mViewPager);

        notecloud = mTabLayout.getTabAt(0);
        setting = mTabLayout.getTabAt(1);

        // 图标
//        home.setIcon(getResources().getDrawable(R.drawable.tab_icon_home_selected));
//        order.setIcon(getResources().getDrawable(R.drawable.tab_icon_dingdan));

        // 选中变化
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //选择是的图标
//                if (tab == mTabLayout.getTabAt(0)){
//                    home.setIcon(getResources().getDrawable(R.drawable.tab_icon_home_selected));
//                    mViewPager.setCurrentItem(0);
//                }else if (tab == mTabLayout.getTabAt(1)){
//                    order.setIcon(getResources().getDrawable(R.drawable.tab_icon_dingdan_selected));
//                    mViewPager.setCurrentItem(1);
//                }else if (tab == mTabLayout.getTabAt(2)){
//                    mine.setIcon(getResources().getDrawable(R.drawable.tab_icon_me_selected));
//                    mViewPager.setCurrentItem(2);
//                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //没选择的图标
//                if (tab == mTabLayout.getTabAt(0)){
//                    home.setIcon(getResources().getDrawable(R.drawable.tab_icon_home));
//                }else if (tab == mTabLayout.getTabAt(1)){
//                    order.setIcon(getResources().getDrawable(R.drawable.tab_icon_dingdan));
//                }else if (tab == mTabLayout.getTabAt(2)){
//                    mine.setIcon(getResources().getDrawable(R.drawable.tab_icon_me));
//                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_changeUser){
            startActivity(new Intent(this,LoginActivity.class));
        }

        // refresh
        if (id == R.id.action_refresh){
            Message msg = new Message();
            msg.what = 1;
            mhandler.sendMessage(msg);
        }

        return super.onOptionsItemSelected(item);
    }

    public void setHandler(Handler handler){
        this.mhandler = handler;
    }
}
