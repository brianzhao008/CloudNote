package com.jkxy.cloudnote.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by brian on 2017/4/27.
 * 内容beans
 */

public class NoteInfo extends BmobObject {

    private String title;
    private String content;
    private String userName;

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }


    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUserName() {
        return userName;
    }
}
