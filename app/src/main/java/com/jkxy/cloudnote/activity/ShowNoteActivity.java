package com.jkxy.cloudnote.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jkxy.cloudnote.BuildConfig;
import com.jkxy.cloudnote.bean.NoteInfo;
import com.jkxy.cloudnote.R;
import com.jkxy.cloudnote.util.VideoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class ShowNoteActivity extends AppCompatActivity {

    private TextView tvShowNoteTitle;
    private TextView tvSHowNoteContent;
    private ImageView ivShowNoteEdit;
    private ImageView ivShowNoteDelete;

    private String objectID = "";
    private String loadTitle;
    private String loadContent;

    // getVideoDownLoad()
    private String videoName;
    private String videoFile;
    private String uriDownloadVideo;

    // getImageDownLoad()
    private String fileImage;
    private String fileName;
    private String uriDownloadImage;

    public static final String mBitmapTag = "☆";
    public static final String mVideoBitmapTag = "★";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        objectID = getIntent().getStringExtra("_id");

        initView();

        loadContent(); // 下载note

        modifyNote();// 更改note
        deleteNote();// 删除note
    }

    private void initView(){
        tvSHowNoteContent = (TextView) findViewById(R.id.tvShowNoteContent);
        tvSHowNoteContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvShowNoteTitle = (TextView) findViewById(R.id.tvShowNoteTitle);
        ivShowNoteDelete = (ImageView) findViewById(R.id.ivShowNoteDelete);
        ivShowNoteEdit = (ImageView) findViewById(R.id.ivShowNoteEdit);

    }

    private void modifyNote() {
        ivShowNoteEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ShowNoteActivity.this,UpdataActivity.class);
                i.putExtra("_id",objectID);
                startActivity(i);
            }
        });
    }

    private void deleteNote(){
        ivShowNoteDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoteInfo items = new NoteInfo();
                items.setObjectId(objectID);
                items.delete(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null){
                            Log.i("bomb delete","success");
                            startActivity(new Intent(ShowNoteActivity.this,MainActivity.class));
                        }else {
                            Toast.makeText(ShowNoteActivity.this,"delete success",Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });
    }

    // 下载note
    private void loadContent(){
        BmobQuery<NoteInfo> query = new BmobQuery<>();
        query.getObject(objectID, new QueryListener<NoteInfo>() {
            @Override
            public void done(NoteInfo noteInfo, BmobException e) {
                if (e == null){
                    loadContent = noteInfo.getContent();
                    loadTitle = noteInfo.getTitle();
                }else {
                    Log.e("shownoteactivity","loadcontent query problem");
                }
                getImageDownLoad(loadContent);
                getVideoDownLoad(loadContent);
                tvShowNoteTitle.setText(loadTitle);
                tvSHowNoteContent.setText(loadContent);
            }
        });
    }

    // 下载视屏
    private void getVideoDownLoad(final String loadVideoContent){
        String regexVi = "★\\{.*?\\}★";
        Pattern p = Pattern.compile(regexVi);
        Matcher m = p.matcher(loadVideoContent);
        List<String> matchVideoJson = new ArrayList<>();
        while (m.find()){
            matchVideoJson.add(m.group());
//            System.out.println(m.group()+"??????????m.group()");
        }
        for (int i = 0; i < matchVideoJson.size() ; i ++){
            uriDownloadVideo = matchVideoJson.get(i);
            uriDownloadVideo = uriDownloadVideo.replace("★","");
            JSONObject jsonDownloadVideo = null;
            try {
                jsonDownloadVideo = new JSONObject(uriDownloadVideo);
                videoName = jsonDownloadVideo.getString("videoName");
                videoFile = jsonDownloadVideo.getString("videoFile");
                BmobFile bmobFile = new BmobFile(videoName,"",videoFile);
                bmobFile.download(new DownloadFileListener() {
                    @Override
                    public void done(String s, BmobException e) {
                        Bitmap bitmap = VideoUtils.getVideoThumbnail(s);
                        insertVideoBitmap(bitmap,loadVideoContent,uriDownloadVideo.length(),s);
                    }

                    @Override
                    public void onProgress(Integer integer, long l) {

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //下载图片
    private void getImageDownLoad(final String loadImageContent) {
        String regexIm = "☆\\{.*?\\}☆";
        Pattern p = Pattern.compile(regexIm);
        Matcher m = p.matcher(loadImageContent);
        List<String> matchJson = new ArrayList<>();
        while (m.find()){
            matchJson.add(m.group());
            System.out.println(m.group()+"??????????m.group()");
//            System.out.println(m.group(0)+"????????????m.group(0)");
//            System.out.println(m.group(1)+"m.group(1)");
//            System.out.println(m.groupCount()+"~~~~~~~~~~~m.groupCount()");
        }

        for (int i = 0; i < matchJson.size() ; i ++){
            uriDownloadImage = matchJson.get(i);
            uriDownloadImage = uriDownloadImage.replace("☆","");
            JSONObject jsonDownloadImage = null;
            try {
                jsonDownloadImage = new JSONObject(uriDownloadImage);
                fileName = jsonDownloadImage.getString("fileName");
                fileImage = jsonDownloadImage.getString("fileImage");
                BmobFile bmobFile = new BmobFile(fileName,"",fileImage);
                bmobFile.download(new DownloadFileListener() {
                    @Override
                    public void done(String s, BmobException e) {
                        Bitmap bitmap = BitmapFactory.decodeFile(s);
                        insertBitmap(bitmap,loadImageContent,uriDownloadImage.length());
                    }

                    @Override
                    public void onProgress(Integer integer, long l) {

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 先用clickablespan 替代字符串 再用bitmap 替代字符串，就可以实现点击图片调用系统播放器
    private SpannableString insertVideoBitmap(Bitmap bitmap,String loadContent,int uriLength,String uri) {
        int index = loadContent.indexOf(mVideoBitmapTag);
        SpannableString spannableString = new SpannableString(loadContent);
        ClickSpan clickSpan = new ClickSpan(uri);
        spannableString.setSpan(clickSpan, index, uriLength + index + 2 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        spannableString.setSpan(imageSpan, index, uriLength + index + 2 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSHowNoteContent.setText(spannableString);
        tvSHowNoteContent.setHighlightColor(Color.TRANSPARENT);//消除点击时的背景色
        tvSHowNoteContent.setMovementMethod(LinkMovementMethod.getInstance());
        return spannableString;
    }

    // 用bitmap替代字符串
    private SpannableString insertBitmap(Bitmap bitmap,String loadContent,int uriLength) {
        int index = loadContent.indexOf(mBitmapTag);
        SpannableString spannableString = new SpannableString(loadContent);
        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        spannableString.setSpan(imageSpan, index, uriLength + index + 2 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSHowNoteContent.setText(spannableString);
        return spannableString;
    }

    // 视屏图片点击事件
    private class ClickSpan extends ClickableSpan {

        private String uriText;

        public ClickSpan(String text){
            this.uriText = text;
        }

        // Android 7.0 FileUriExposedException
        // FileProvider方式
        @Override
        public void onClick(View widget) {
            File file = new File(uriText);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(ShowNoteActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
                intent.setDataAndType(contentUri, "video/*");
            } else {
                intent.setDataAndType(Uri.fromFile(file), "video/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);

            super.updateDrawState(ds);
        }
    }

}
