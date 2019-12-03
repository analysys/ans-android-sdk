package com.analysys.demo.allgroTest.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.analysys.allgro.annotations.AnalysysIgnorePage;
import com.analysys.demo.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by lenovo on 2016/9/1.
 */
@AnalysysIgnorePage
public class FragmentTwo extends Fragment {


    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_two, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.fraTwo_btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 白名单测试1：注解自动上报
            }
        });

        view.findViewById(R.id.fraTwo_btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 黑名单测试1：注解自动忽略
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public void onClick(View v) {

    }
}
