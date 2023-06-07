package com.analysys.demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.analysys.apidemo.R;
import com.analysys.process.AgentProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/7/22 11:04 AM
 * @author: huchangqing
 */
public class ModifyConfigActivity extends BaseActivity {

    private EditText edtAppKey;
    private EditText edtChannelId;
    private EditText edtUploadUrl;
    private EditText edtVisualDebugUrl;
    private EditText edtVisualConfigUrl;
    private SharedPreferences sp;

    public static final String PREF_FILE = "ans_demo_pref";
    public static final String PREF_KEY_APP_KEY = "app_key";
    public static final String PREF_KEY_CHANNEL_ID = "channel_id";
    public static final String PREF_KEY_UPLOAD_URL = "upload_url";
    public static final String PREF_KEY_VISUAL_DEBUG_URL = "visual_debug_url";
    public static final String PREF_KEY_VISUAL_CONFIG_URL = "visual_config_url";

    class ConfigAction {
        EditText view;
        String prefKey;

        ConfigAction(EditText v, String key, String defaultValue) {
            view = v;
            prefKey = key;
            String config = sp.getString(prefKey, defaultValue);
            view.setText(config);
        }

        @SuppressLint("ApplySharedPref")
        void saveConfig() {
            sp.edit().putString(prefKey, view.getText().toString()).commit();
        }

        public void clear() {
            sp.edit().remove(prefKey).commit();
        }
    }

    private List<ConfigAction> mConfigList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_config);
        sp = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        edtAppKey = findViewById(R.id.edt_app_key);
        ConfigAction configAction = new ConfigAction(edtAppKey, PREF_KEY_APP_KEY, AgentProcess.getInstance().getConfig().getAppKey());
        mConfigList.add(configAction);

        edtChannelId = findViewById(R.id.edt_channel_id);
        configAction = new ConfigAction(edtChannelId, PREF_KEY_CHANNEL_ID, AgentProcess.getInstance().getConfig().getChannel());
        mConfigList.add(configAction);

        edtUploadUrl = findViewById(R.id.edt_upload_url);
        configAction = new ConfigAction(edtUploadUrl, PREF_KEY_UPLOAD_URL, AgentProcess.getInstance().getUploadURL());
        mConfigList.add(configAction);

        edtVisualDebugUrl = findViewById(R.id.edt_visual_debug_url);
        configAction = new ConfigAction(edtVisualDebugUrl, PREF_KEY_VISUAL_DEBUG_URL, AgentProcess.getInstance().getVisitorDebugURL());
        mConfigList.add(configAction);

        edtVisualConfigUrl = findViewById(R.id.edt_visual_config_url);
        configAction = new ConfigAction(edtVisualConfigUrl, PREF_KEY_VISUAL_CONFIG_URL, AgentProcess.getInstance().getVisitorConfigURL());
        mConfigList.add(configAction);
    }

    public void onClick(View view) {
        boolean isSave = view.getId() == R.id.btn_save;
        for (ConfigAction config : mConfigList) {
            if (isSave) {
                config.saveConfig();
            } else {
                config.clear();
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        StringBuilder sb = new StringBuilder();
        sb.append("AppKey: ").append(edtAppKey.getText().toString()).append("\n");
        sb.append("Channel ID: ").append(edtChannelId.getText().toString()).append("\n");
        sb.append("Upload Url: ").append(edtUploadUrl.getText().toString()).append("\n");
        sb.append("Visual Debug Url: ").append(edtVisualDebugUrl.getText().toString()).append("\n");
        sb.append("Visual Config Url: ").append(edtVisualConfigUrl.getText().toString()).append("\n");
        builder.setMessage(sb.toString());
        builder.setTitle("修改配置后需要重启才能生效");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        }).setCancelable(false).create().show();
    }
}
