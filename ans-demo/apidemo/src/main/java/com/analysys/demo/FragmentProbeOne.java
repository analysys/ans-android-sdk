package com.analysys.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;
import com.analysys.process.AgentProcess;

import java.util.ArrayList;
import java.util.List;

public class FragmentProbeOne extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_probe_one, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String pageName = this.getClass().getName();
        TextView tv = view.findViewById(R.id.tv_page_name);
        tv.setText("页面名字：" + pageName);
        CheckBox checkBox = view.findViewById(R.id.cb_ignore_page);
        checkBox.setChecked(AgentProcess.getInstance().isThisPageInPageViewBlackList(pageName));
        checkBox.setText(checkBox.getText() + pageName);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    List<String> list = new ArrayList<>();
                    list.add(pageName);
                    AnalysysAgent.setPageViewBlackListByPages(list);
                } else {
                    AnalysysAgent.setPageViewBlackListByPages(null);
                }
            }
        });
    }
}
