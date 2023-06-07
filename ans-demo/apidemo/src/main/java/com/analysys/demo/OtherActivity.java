package com.analysys.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.analysys.AnalysysAgent;
import com.analysys.AnsRamControl;
import com.analysys.EncryptEnum;
import com.analysys.apidemo.R;
import com.analysys.process.AgentProcess;
import com.analysys.strategy.PolicyManager;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OtherActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText("App Version: " + CommonUtils.getVersionName(this) + "\nSDK Version: " + Constants.DEV_SDK_VERSION);

        Switch enableDataCollect = findViewById(R.id.enable_data_collect);
        enableDataCollect.setChecked(AgentProcess.getInstance().isDataCollectEnable());
        enableDataCollect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AnalysysAgent.setDataCollectEnable(isChecked);
            }
        });

        Spinner spinner = findViewById(R.id.sp_network);
        List<String> listStr = new ArrayList<>();
        listStr.add("不允许上传");
        listStr.add("允许移动网络上传");
        listStr.add("允许wifi网络上传");
        listStr.add("允许所有网络上传");
        List<Integer> listValues = new ArrayList<>();
        listValues.add(AnalysysAgent.AnalysysNetworkType.AnalysysNetworkNONE);
        listValues.add(AnalysysAgent.AnalysysNetworkType.AnalysysNetworkWWAN);
        listValues.add(AnalysysAgent.AnalysysNetworkType.AnalysysNetworkWIFI);
        listValues.add(AnalysysAgent.AnalysysNetworkType.AnalysysNetworkALL);
        initSpinner(spinner, listStr, new IOnItemSelected() {
            @Override
            public void onSelected(int pos) {
                AnalysysAgent.setUploadNetworkType(listValues.get(pos));
            }
        }, listValues.indexOf(AnsRamControl.getInstance().getUploadNetworkType()));

        spinner = findViewById(R.id.sp_encrypt);
        listStr = new ArrayList<>();
        listStr.add("EMPTY");
        listStr.add("AES_ECB");
        listStr.add("AES_CBC");
        List<EncryptEnum> listValuesEncrypt = new ArrayList<>();
        listValuesEncrypt.add(EncryptEnum.EMPTY);
        listValuesEncrypt.add(EncryptEnum.AES);
        listValuesEncrypt.add(EncryptEnum.AES_CBC);
        initSpinner(spinner, listStr, new IOnItemSelected() {
            @Override
            public void onSelected(int pos) {
                AgentProcess.getInstance().getConfig().setEncryptType((listValuesEncrypt.get(pos)));
            }
        }, listValuesEncrypt.indexOf(AgentProcess.getInstance().getConfig().getEncryptType()));

        spinner = findViewById(R.id.sp_debug_mode);
        listStr = new ArrayList<>();
        listStr.add("关闭debug");
        listStr.add("打开Debug，不计统计");
        listStr.add("打开Debug，计入统计");
        List<Integer> listValuesDebugMode = new ArrayList<>();
        listValuesDebugMode.add(0);
        listValuesDebugMode.add(1);
        listValuesDebugMode.add(2);
        initSpinner(spinner, listStr, new IOnItemSelected() {
            @Override
            public void onSelected(int pos) {
                AnalysysAgent.setDebugMode(OtherActivity.this, listValuesDebugMode.get(pos));
            }
        }, listValuesDebugMode.indexOf(CommonUtils.getDebugMode(this)));

        EditText editText = findViewById(R.id.edt_interval);
        editText.setText(PolicyManager.getIntervalTime(this) / 1000 + "");
        editText = findViewById(R.id.edt_max_event);
        editText.setText(PolicyManager.getEventCount(this) + "");
        editText = findViewById(R.id.edt_max_cache);
        editText.setText(AnalysysAgent.getMaxCacheSize(this) + "");

        Switch autoTimeCheck = findViewById(R.id.time_check);
        autoTimeCheck.setChecked(AgentProcess.getInstance().getConfig().isTimeCheck());
        autoTimeCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AgentProcess.getInstance().getConfig().setAllowTimeCheck(isChecked);
            }
        });

        editText = findViewById(R.id.edt_time_check);
        editText.setText(AgentProcess.getInstance().getConfig().getMaxDiffTimeInterval() / 1000 + "");

        Switch autoDeviceInfo = findViewById(R.id.device_info);
        autoDeviceInfo.setChecked(AgentProcess.getInstance().getConfig().isAutoTrackDeviceId());
        autoDeviceInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AgentProcess.getInstance().getConfig().setAutoTrackDeviceId(isChecked);
            }
        });

        Switch autoTrackCrash = findViewById(R.id.auto_track_crash);
        autoTrackCrash.setChecked(AgentProcess.getInstance().getConfig().isAutoTrackCrash());
        autoTrackCrash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AgentProcess.getInstance().getConfig().setAutoTrackCrash(isChecked);
            }
        });

        Switch autoTrack = findViewById(R.id.auto_track_page_view);
        autoTrack.setChecked(AgentProcess.getInstance().getConfig().isAutoTrackPageView());
        autoTrack.setOnCheckedChangeListener((buttonView, isChecked)
                -> AgentProcess.getInstance().getConfig().setAutoTrackPageView(isChecked));

        autoTrack = findViewById(R.id.auto_track_probe_click);
        autoTrack.setChecked(AgentProcess.getInstance().getConfig().isAutoTrackClick());
        autoTrack.setOnCheckedChangeListener((buttonView, isChecked)
                -> AgentProcess.getInstance().getConfig().setAutoTrackClick(isChecked));

        autoTrack = findViewById(R.id.auto_track_heat_map);
        autoTrack.setChecked(AgentProcess.getInstance().getConfig().isAutoHeatMap());
        autoTrack.setOnCheckedChangeListener((buttonView, isChecked)
                -> AgentProcess.getInstance().getConfig().setAutoHeatMap(isChecked));
    }

    private interface IOnItemSelected {
        void onSelected(int pos);
    }

    private void initSpinner(Spinner spinner, List<String> listStr, IOnItemSelected onItemSelectedCallabck, int initPos) {
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listStr));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                onItemSelectedCallabck.onSelected(pos);
                Toast.makeText(OtherActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setSelection(initPos);
    }

    private long edt2Long(int resId, long min, long max) {
        long value = -1;
        try {
            EditText editText = findViewById(resId);
            value = Long.parseLong(editText.getText().toString());
        } catch (Exception e) {
        }
        if (value > max || value < min) {
            value = -1;
        }
        return value;
    }

    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_interval) {
            long value = edt2Long(R.id.edt_interval, 1, Long.MAX_VALUE);
            if (value == -1) {
                Toast.makeText(this, "输入错误", Toast.LENGTH_SHORT).show();
            } else {
                AnalysysAgent.setIntervalTime(this, value);
                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
            }
        } else if (viewId == R.id.btn_max_event) {
            long value = edt2Long(R.id.edt_max_event, 1, Long.MAX_VALUE);
            if (value == -1) {
                Toast.makeText(this, "输入错误", Toast.LENGTH_SHORT).show();
            } else {
                AnalysysAgent.setMaxEventSize(this, value);
                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
            }
        } else if (viewId == R.id.btn_max_cache) {
            long value = edt2Long(R.id.edt_max_cache, 1, Long.MAX_VALUE);
            if (value == -1) {
                Toast.makeText(this, "输入错误", Toast.LENGTH_SHORT).show();
            } else {
                AnalysysAgent.setMaxCacheSize(this, value);
                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
            }
        } else if (viewId == R.id.btn_time_check) {
            long value = edt2Long(R.id.edt_time_check, 1, Long.MAX_VALUE);
            if (value == -1) {
                Toast.makeText(this, "输入错误", Toast.LENGTH_SHORT).show();
            } else {
                AgentProcess.getInstance().getConfig().setMaxDiffTimeInterval(value);
                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
            }
        } else if (viewId == R.id.btn_crash) {
            Toast.makeText(this, "操作成功，在logcat中查看xwhat为$app_crash的日志", Toast.LENGTH_LONG).show();
            int crash = 1 / 0;
        } else if (viewId == R.id.btn_get_property) {
            Map<String, Object> getProperty = AnalysysAgent.getPresetProperties(this);
            StringBuilder properties = new StringBuilder();
            if (getProperty != null) {
                for (Map.Entry<String, Object> entry : getProperty.entrySet()) {
                    properties.append("\n").append(entry.getKey()).append(" = " + entry.getValue());
                }
            }
            String props = properties.toString();
            Toast.makeText(this, "所有预置属性值为" + (TextUtils.isEmpty(props) ? "空" : props), Toast.LENGTH_LONG).show();
        } else if (viewId == R.id.btn_clean_db) {
            AnalysysAgent.cleanDBCache();
            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.btn_reset) {
            AnalysysAgent.reset(this);
            Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
        }
    }
}
