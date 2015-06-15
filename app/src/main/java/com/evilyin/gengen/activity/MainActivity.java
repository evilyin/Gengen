package com.evilyin.gengen.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.evilyin.gengen.AccessTokenKeeper;
import com.evilyin.gengen.R;
import com.evilyin.gengen.service.ScanService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.byr.bbs.sdk.api.UserApi;
import cn.byr.bbs.sdk.auth.BBSAuth;
import cn.byr.bbs.sdk.auth.BBSAuthListener;
import cn.byr.bbs.sdk.auth.Oauth2AccessToken;
import cn.byr.bbs.sdk.exception.BBSException;
import cn.byr.bbs.sdk.net.RequestListener;

/**
 * @author evilyin(ChenZhixi)
 * @since 2015-5-28
 */
public class MainActivity extends Activity {

    private static final String appKey = "48bf3ea8aab5256baf22d175aebce5fa";
    private static final String redirectUrl = "http://bbs.byr.cn/Oauth2/callback";
    private static final String scope = "article,mail,favor,refer,blacklist";

    private TextView userText;
    private BBSAuth mAuth;
    private Oauth2AccessToken mAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = (Button) findViewById(R.id.button_login);
        Button goButton = (Button) findViewById(R.id.button_go);
        userText = (TextView) findViewById(R.id.textview_user);
        mAuth = new BBSAuth(this, appKey, redirectUrl, scope);

        //获取已存储的登录信息
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        if (mAccessToken.isSessionValid()) {
            setUserText();
            loginButton.setClickable(false);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用sdk登录
                mAuth.authorize(new AuthListener());
            }
        });

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAccessToken.isSessionValid()) {
                    startService(new Intent(MainActivity.this, ScanService.class));
                }else {
                    Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    class AuthListener implements BBSAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // show it
                setUserText();
                // store Token into SharedPreferences
                AccessTokenKeeper.writeAccessToken(MainActivity.this, mAccessToken);
                Toast.makeText(MainActivity.this,
                        R.string.bbsDemo_oauth_success, Toast.LENGTH_SHORT).show();
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.bbsDemo_oauth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(MainActivity.this,
                    R.string.bbsDemo_oauth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onException(BBSException e) {
            Toast.makeText(MainActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setUserText() {
        final String validDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA).format(new Date(mAccessToken.getExpiresTime()));

        UserApi mUserApi = new UserApi(mAccessToken);
        mUserApi.show(new RequestListener() {
            @Override
            public void onComplete(String s) {
                try {
                    JSONObject object = new JSONObject(s);
                    String userId = object.getString("id");
                    userText.setText(String.format("已登录用户：%1$s\n登录有效期：%2$s", userId, validDate));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onException(BBSException e) {
                Toast.makeText(MainActivity.this, "查询用户信息出错", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
