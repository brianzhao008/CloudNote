package com.jkxy.cloudnote.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jkxy.cloudnote.bean.NoteInfo;
import com.jkxy.cloudnote.util.PicEditText;
import com.jkxy.cloudnote.R;
import com.jkxy.cloudnote.util.UriUtils;
import com.jkxy.cloudnote.util.VideoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class AddNoteActivity extends AppCompatActivity {

    private NoteInfo infos;
    private Button btnAddNoteSave;
    private EditText etAddNoteTitle;
    private PicEditText etAddNoteContent;

    private ImageView ivBtnPhoto;
    private ImageView ivBtnCamera;

    private Bitmap bitmap;

    private BmobUser mUser = BmobUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_note);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        // 另一种返回方式
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        etAddNoteTitle = (EditText) findViewById(R.id.etAddNoteTitile);
        etAddNoteContent = (PicEditText) findViewById(R.id.etAddNoteContent);

        btnAddNoteSave = (Button) findViewById(R.id.btnAddNoteSave);
        btnAddNoteSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etAddNoteTitle.getText().toString().isEmpty() && !etAddNoteContent.getText().toString().isEmpty()){
                    addData();
                    startActivity(new Intent(AddNoteActivity.this,MainActivity.class));
                }else {
                    if (etAddNoteTitle.getText().toString().isEmpty()){
                        Toast.makeText(AddNoteActivity.this,R.string.addnote_title_no_empty,Toast.LENGTH_SHORT).show();
                    }else if (etAddNoteContent.getText().toString().isEmpty()) {
                        Toast.makeText(AddNoteActivity.this,R.string.addnote_content_no_empty,Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(AddNoteActivity.this,R.string.addnote_text_no_empty,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ivBtnPhoto = (ImageView) findViewById(R.id.ivBtnPhoto);
        ivBtnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        ivBtnCamera = (ImageView) findViewById(R.id.ivBtnCamera);
        ivBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoCamera();
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
    // 获得视频和图片的uri
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
                        etAddNoteContent.insertVideoBitmap(uriVideoJsonString, VideoUtils.getVideoThumbnail(videoPath));
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
                        etAddNoteContent.insertBitmap(uriPicJsonString,bitmap);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    // 上传数据
    private void addData() {
        infos = new NoteInfo();
        infos.setTitle(etAddNoteTitle.getText().toString());
        infos.setContent(etAddNoteContent.getText().toString());
        infos.setUserName(mUser.getUsername());
        infos.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null){
                    toast(getString(R.string.addnote_note_success));
                }else {
                    toast(getString(R.string.addnote_note_failed)+e.toString());
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }



}
