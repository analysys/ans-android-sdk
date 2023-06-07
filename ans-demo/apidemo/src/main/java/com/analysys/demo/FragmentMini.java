package com.analysys.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.analysys.ANSAutoPageTracker;
import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/7/21 5:22 PM
 * @author: huchangqing
 */
public class FragmentMini extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnNormal = view.findViewById(R.id.btn_track_normal);
        btnNormal.setOnClickListener(this);
        Button btnTrackName = view.findViewById(R.id.btn_track_name);
        btnTrackName.setOnClickListener(this);
        Button btnTrackProperty = view.findViewById(R.id.btn_track_property);
        btnTrackProperty.setOnClickListener(this);

        edtName = view.findViewById(R.id.edt_event_name);
        edtKey = view.findViewById(R.id.edt_key);
        edtValue = view.findViewById(R.id.edt_value);
    }

    private EditText edtName;
    private EditText edtKey;
    private EditText edtValue;

    @Override
    public void onClick(View view) {
        String eventName = null;
        if (view.getId() == R.id.btn_track_normal) {
            eventName = "track_test";
            AnalysysAgent.track(getContext(), eventName);
        } else if (view.getId() == R.id.btn_track_name) {
            eventName = edtName.getText().toString();
            if (TextUtils.isEmpty(eventName)) {
                Toast.makeText(getContext(), "名称不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            AnalysysAgent.track(getContext(), eventName);
        } else if (view.getId() == R.id.btn_track_property) {
            String key = edtKey.getText().toString();
            if (TextUtils.isEmpty(key)) {
                Toast.makeText(getContext(), "Key不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String value = edtValue.getText().toString();
            if (TextUtils.isEmpty(value)) {
                Toast.makeText(getContext(), "Value不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> data = new HashMap<>();
            data.put(key, value);
            eventName = "track_property";
            AnalysysAgent.track(getContext(), eventName, data);
            Toast.makeText(getContext(), "触发事件: " + eventName + "[" + key + "=" + value + "]", Toast.LENGTH_LONG).show();
        }

        Toast.makeText(getContext(), "触发事件: " + eventName, Toast.LENGTH_LONG).show();
    }



}
