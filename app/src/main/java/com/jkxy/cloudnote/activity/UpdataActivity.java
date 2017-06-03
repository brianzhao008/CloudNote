package com.jkxy.cloudnote.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jkxy.cloudnote.BuildConfig;
import com.jkxy.cloudnote.bean.NoteInfo;
import com.jkxy.cloudnote.R;
import com.jkxy.cloudnote.util.PicEditText;
import com.jkxy.cloudnote.util.UriUtils;
import com.jkxy.cloudnote.util.VideoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class UpdataActivity extends AppCompatActivity {

    private EditText etUpdataTitle;
    private PicEditText etUpdataContent;

    private ImageView ivBtnUpdataPhoto;
    private ImageView ivBtnUpdataCamera;

    private Button btnUpdataSave;
    private NoteInfo infos;

    private String OBJECTID;

    private BmobUser mUser = BmobUser.getCurrentUser();
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata);
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

        etUpdataTitle = (EditText) findViewById(R.id.etUpdataTitile);
        etUpdataContent = (PicEditText) findViewById(R.id.etUpdataContent);

        btnUpdataSave = (Button) findViewById(R.id.btnUpdataSave);
        btnUpdataSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updataData();
                startActivity(new Intent(UpdataActivity.this,MainActivity.class));
            }
        });

        ivBtnUpdataPhoto = (ImageView) findViewById(R.id.ivBtnUpdataPhoto);
        ivBtnUpdataPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        ivBtnUpdataCamera = (ImageView) findViewById(R.id.ivBtnUpdataCamera);
        ivBtnUpdataCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCamera();
            }
        });

        loadOldContent();
    }

    // 上传字符串
    private void updataData() {
        OBJECTID = getIntent().getStringExtra("_id");
        infos = new NoteInfo();
        infos.setContent(etUpdataContent.getText().toString());
        infos.setTitle(etUpdataTitle.getText().toString());
        infos.setUserName(mUser.getUsername());
        infos.update(OBJECTID, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null){
                    toast(getString(R.string.cloudnote_updata_success));
                }else {
                    toast(getString(R.string.cloudnote_updata_failed));
                }
            }
        });
    }

    // 调用摄像
    private void gotoCamera() {
        Intent mI = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        mI.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.5);
        startActivityForResult(mI, 1);
    }

    private void getImage(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, 0);
    }

    // 添加图片和视屏的回掉
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 0:
                if (resultCode == RESULT_OK && data != null){
                    // 不知道为什么不行
//                    Uri selectedImage = data.getData();
//                    String imageurl = getRealFilePath(this,selectedImage);
//                    Bitmap originalBitmap = BitmapFactory.decodeFile("/storage/emulated/0/Download/timg.jpg");
//                    System.out.println(imageurl + ">>>>>>>>>>>>>>>>>>>>>");
//                    etAddNoteContent.insertBitmap(imageurl,originalBitmap);

                    ContentResolver resolver = getContentResolver();
                    Uri selectedImage = data.getData();
                    try {
                        bitmap = BitmapFactory.decodeStream(resolver.openInputStream(selectedImage));
                        String imageUrlPath = UriUtils.getImageAbsolutePath(this, selectedImage);

                        addPic(imageUrlPath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 1:
                if (resultCode == RESULT_OK && data != null){
                    Uri photoCameraUri = data.getData();
                    String videoUriPath = UriUtils.getRealFilePath(this,photoCameraUri);

                    addVideo(videoUriPath);
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 新加的视屏
    private void addVideo(final String videoPath){
        File fVideo = new File(videoPath);
        final BmobFile bmobVideoFile = new BmobFile(fVideo);
        bmobVideoFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null){
                    String uriFileVideo = bmobVideoFile.getFileUrl();
                    String uriFileName = bmobVideoFile.getFilename();
                    try {
                        JSONObject uriVideoJson = new JSONObject();
                        uriVideoJson.put("videoName",uriFileName);
                        uriVideoJson.put("videoFile",uriFileVideo);
                        String uriVideoJsonString = uriVideoJson.toString();
                        etUpdataContent.insertVideoBitmap(uriVideoJsonString, VideoUtils.getVideoThumbnail(videoPath));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    // 新加的图片
    private void addPic(String picPath){
        File fPic = new File(picPath);
        final BmobFile bmobPicFile = new BmobFile(fPic);
        bmobPicFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null){
                    String uriFileImage = bmobPicFile.getFileUrl();
                    String uriFileName = bmobPicFile.getFilename();
                    try {
                        JSONObject uriPicJson = new JSONObject();
                        uriPicJson.put("fileName",uriFileName);
                        uriPicJson.put("fileImage",uriFileImage);
                        String uriPicJsonString = uriPicJson.toString();
                        etUpdataContent.insertBitmap(uriPicJsonString,bitmap);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }


    public void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String loadOldTitle;
    private String loadOldContent;

    // 导入原来的content
    private void loadOldContent(){
        BmobQuery<NoteInfo> query = new BmobQuery<>();
        query.getObject(getIntent().getStringExtra("_id"), new QueryListener<NoteInfo>() {
            @Override
            public void done(NoteInfo noteInfo, BmobException e) {
                if (e == null){
                    loadOldContent = noteInfo.getContent();
                    loadOldTitle = noteInfo.getTitle();
                }else {
                    Log.e("shownoteactivity","loadcontent query problem");
                }
                getImageDownLoad(loadOldContent);
                getVideoDownLoad(loadOldContent);
                etUpdataTitle.setText(loadOldTitle);
                etUpdataContent.setText(loadOldContent);
            }
        });
    }

    String videoName;
    String videoFile;
    String uriDownloadVideo;

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

//

    String fileImage;
    String fileName;
    String uriDownloadImage;

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
    private SpannableString insertVideoBitmap(Bitmap bitmap, String loadContent, int uriLength, String uri) {
        int index = loadContent.indexOf(ShowNoteActivity.mVideoBitmapTag);
        SpannableString spannableString = new SpannableString(loadContent);
        ClickSpan clickSpan = new ClickSpan(uri);
        spannableString.setSpan(clickSpan, index, uriLength + index + 2 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        spannableString.setSpan(imageSpan, index, uriLength + index + 2 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etUpdataContent.setText(spannableString);
        etUpdataContent.setHighlightColor(Color.TRANSPARENT);//消除点击时的背景色
        etUpdataContent.setMovementMethod(LinkMovementMethod.getInstance());
        return spannableString;
    }

    // 用bitmap替代字符串
    private SpannableString insertBitmap(Bitmap bitmap,String loadContent,int uriLength) {
        int index = loadContent.indexOf(ShowNoteActivity.mBitmapTag);
        SpannableString spannableString = new SpannableString(loadContent);
        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        spannableString.setSpan(imageSpan, index, uriLength + index + 2 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etUpdataContent.setText(spannableString);
        return spannableString;
    }

    // 图片点击事件
    private class ClickSpan extends ClickableSpan {

        private String uriText;

        public ClickSpan(String text){
            this.uriText = text;
        }

        // Android 7.0 FileProvider方式
        // 不能直接使用file://...  会出现  FileUriExposedException
        @Override
        public void onClick(View widget) {
            File file = new File(uriText);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(UpdataActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
                intent.setDataAndType(contentUri, "video/*");
            } else {
                intent.setDataAndType(Uri.fromFile(file), "video/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
        }

        // 设置颜色和下划线
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);

            super.updateDrawState(ds);
        }
    }

}
