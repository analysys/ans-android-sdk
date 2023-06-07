package com.analysys.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.analysys.allgro.annotations.AnalysysIgnorePage;
import com.analysys.apidemo.R;

@AnalysysIgnorePage
public class FragmentProbeTwo extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_probe_two, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String pageName = this.getClass().getName();
        TextView tv = view.findViewById(R.id.tv_page_name);
        tv.setText("页面名字：" + pageName);
    }
}
