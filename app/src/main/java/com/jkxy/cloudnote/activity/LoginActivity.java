package com.jkxy.cloudnote.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jkxy.cloudnote.R;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private Button mBtnRegist;

    private boolean LOGIN = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.toolbar_login);
        TextView tvLogTitle = (TextView) findViewById(R.id.toolbar_login_title);
        tvLogTitle.setText(R.string.cloudnote_login);
        setSupportActionBar(mToolbarTb);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mEtUsername = (EditText) findViewById(R.id.et_username);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mBtnLogin = (Button) findViewById(R.id.btnLogin);
        mBtnRegist = (Button) findViewById(R.id.btnRegist);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin(LOGIN);
            }
        });

        mBtnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGIN = false;
                attemptLogin(LOGIN);
            }
        });

    }

    // 填入规则
    private boolean attemptLogin(boolean LOGIN) {

        // Reset errors.
        mEtUsername.setError(null);
        mEtPassword.setError(null);

        // Store values at the time of the login attempt.
        String userName = mEtUsername.getText().toString();
        String password = mEtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 检查密码
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mEtPassword.setError(getString(R.string.pwd_longerthan_five));
            focusView = mEtPassword;
            cancel = true;
        }

        // 检查用户名
        if (TextUtils.isEmpty(userName)) {
            mEtUsername.setError(getString(R.string.username_necessary));
            focusView = mEtUsername;
            cancel = true;
        } else if (!isUserNameValid(userName)) {
            mEtUsername.setError(getString(R.string.username_biggerthan_four));
            focusView = mEtUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }else {
            if (LOGIN){
                loginInBmob(userName,password);
            }else {
                signupInBmob(userName,password);
            }
        }

        return cancel;
    }

    /**
     * 用户名必须大于4位
     */
    private boolean isUserNameValid(String userName) {
        return userName.length() >= 4;
    }

    /**
     * 密码必须大于5位
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= 5;
    }

    /**
     * 向Bmob 提交登陆数据
     */
    private void loginInBmob(final String name, String pwd) {
        final BmobUser user = new BmobUser();
        user.setUsername(name);
        user.setPassword(pwd);
        user.login(new SaveListener<BmobUser>() {

            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null){
                    toast(getString(R.string.login_success));
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    toast(e.toString());
                }
            }

        });
    }

    /**
     * 在Bmob 注册数据
     */
    private void signupInBmob(final String userName, String password) {
        // 向Bmob后端注册数据
        BmobUser user = new BmobUser();
        user.setUsername(userName);
        user.setPassword(password);
        user.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null){
                    toast(getString(R.string.register_success));
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    toast(e.toString());
                }
            }
        });
    }

    public void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
