package com.evilyin.gengen.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.evilyin.gengen.AppManager;
import com.evilyin.gengen.R;

/**
 * @author evilyin(ChenZhixi)
 * @since 2015-7-8
 */

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final EditText contentEditText = (EditText) findViewById(R.id.settings_content);
        final EditText scantimeEditText = (EditText) findViewById(R.id.settings_scantime);
        final CheckBox sendmailCheckBox = (CheckBox) findViewById(R.id.settings_checkbox_sendmail);

        final SharedPreferences preferences = this.getSharedPreferences("settings", MODE_APPEND);
        contentEditText.setText(preferences.getString("content","sb楼主又来犯贱了"));
        scantimeEditText.setText(preferences.getInt("scantime", 600)+"");
        sendmailCheckBox.setChecked(preferences.getBoolean("sendmail", true));

        findViewById(R.id.settings_button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                String content="sb楼主又来犯贱了";
                if (!contentEditText.getText().toString().equals("")) {
                    content = contentEditText.getText().toString();
                }
                int scantime=600;
                if (!scantimeEditText.getText().toString().equals("")) {
                    scantime = Integer.parseInt(scantimeEditText.getText().toString());
                }
                boolean sendmail = sendmailCheckBox.isChecked();
                editor.putString("content", content);
                editor.putInt("scantime", scantime);
                editor.putBoolean("sendmail", sendmail);
                editor.apply();
                AppManager.loadSettings(SettingsActivity.this);
                finish();
            }
        });
    }
}
