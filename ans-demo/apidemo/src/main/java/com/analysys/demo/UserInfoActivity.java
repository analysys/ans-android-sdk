package com.analysys.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;
import com.analysys.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoActivity extends BaseActivity {

    class UserAction {
        String name;
        String eventName;
        Runnable runnable;
        String msg;

        UserAction(String name, String eventName, String msg, Runnable runnable) {
            this.name = name;
            this.eventName = eventName;
            this.msg = msg;
            this.runnable = runnable;
        }

        public void doAction() {
            if (runnable != null) {
                runnable.run();
            }
            if (!TextUtils.isEmpty(msg)) {
                Toast.makeText(UserInfoActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ListView listView = findViewById(R.id.list_view);
        List<UserAction> list = new ArrayList<>();
        list.add(new UserAction("设置匿名ID", Constants.PROFILE_SET, "匿名ID设置为 WechatID_1", new Runnable() {
            @Override
            public void run() {
                //淘宝店铺使用该功能时，只关注访客用户或店铺会员，不关注设备信息
                AnalysysAgent.identify(UserInfoActivity.this, "WechatID_1");
            }
        }));
        list.add(new UserAction("匿名ID与用户关联", Constants.PROFILE_SET, "关联ID设置为 18688886666", new Runnable() {
            @Override
            public void run() {
                AnalysysAgent.alias(UserInfoActivity.this, "18688886666");
            }
        }));
        list.add(new UserAction("获取匿名ID", null, null, new Runnable() {
            @Override
            public void run() {
                Toast.makeText(UserInfoActivity.this, "匿名ID为 " + AnalysysAgent.getDistinctId(UserInfoActivity.this), Toast.LENGTH_LONG).show();
            }
        }));
        list.add(new UserAction("设置单个用户属性", Constants.PROFILE_SET, "用户属性[Email = yonghu@163.com]", new Runnable() {
            @Override
            public void run() {
                //要统计分析使用邮箱登录的用户
                AnalysysAgent.profileSet(UserInfoActivity.this, "Email", "yonghu@163.com");
            }
        }));
        list.add(new UserAction("设置多个用户属性", Constants.PROFILE_SET, "用户属性[Email = yonghu@163.com, WeChatID = weixinhao]", new Runnable() {
            @Override
            public void run() {
                //要统计用户的登录方式邮箱登录,微信登录
                Map<String, Object> profileSet = new HashMap<>();
                profileSet.put("Email", "yonghu@163.com");
                profileSet.put("WeChatID", "weixinhao");
                AnalysysAgent.profileSet(UserInfoActivity.this, profileSet);
            }
        }));
        list.add(new UserAction("设置单个用户固有属性", Constants.PROFILE_SET_ONCE, "固有属性[activationTime = 1521594686781]", new Runnable() {
            @Override
            public void run() {
                //要统计用户的激活时间
                AnalysysAgent.profileSetOnce(UserInfoActivity.this, "activationTime", "1521594686781");
            }
        }));
        list.add(new UserAction("设置多个用户固有属性", Constants.PROFILE_SET_ONCE, "固有属性[activationTime = 1521594686781, loginTime = 1521594726781]", new Runnable() {
            @Override
            public void run() {
                //要统计激活时间和首次登陆时间
                Map<String, Object> setOnceProfile = new HashMap<>();
                setOnceProfile.put("activationTime", "1521594686781");
                setOnceProfile.put("loginTime", "1521594726781");
                AnalysysAgent.profileSetOnce(UserInfoActivity.this, setOnceProfile);
            }
        }));
        list.add(new UserAction("设置单个用户属性相对变化值", Constants.PROFILE_INCREMENT, "用户属性 age 加1", new Runnable() {
            @Override
            public void run() {
                //用户年龄增加了一岁
                AnalysysAgent.profileIncrement(UserInfoActivity.this, "age", 1);
            }
        }));
        list.add(new UserAction("设置多个用户属性相对变化值", Constants.PROFILE_INCREMENT, "用户属性 age 加1，integral 加200", new Runnable() {
            @Override
            public void run() {
                //用户年龄增加了一岁积分增加了200
                Map<String, Number> incrementProfile = new HashMap<>();
                incrementProfile.put("age", 1);
                incrementProfile.put("integral", 200);
                AnalysysAgent.profileIncrement(UserInfoActivity.this, incrementProfile);
            }
        }));
        list.add(new UserAction("增加单个列表类型的属性", Constants.PROFILE_APPEND, "列表属性 hobby 加入 Music", new Runnable() {
            @Override
            public void run() {
                //添加用户爱好
                AnalysysAgent.profileAppend(UserInfoActivity.this, "hobby", "Music");
            }
        }));
        list.add(new UserAction("增加多个列表类型的属性", Constants.PROFILE_APPEND, "列表属性 hobby 加入 PlayBasketball, Music", new Runnable() {
            @Override
            public void run() {
                List<Object> list = new ArrayList<>();
                list.add("PlayBasketball");
                list.add("Music");
                AnalysysAgent.profileAppend(UserInfoActivity.this, "hobby", list);
            }
        }));
        list.add(new UserAction("删除用户单个属性值", Constants.PROFILE_UNSET, "删除用户属性 age", new Runnable() {
            @Override
            public void run() {
                // 删除用户年龄属性
                AnalysysAgent.profileUnset(UserInfoActivity.this, "age");
            }
        }));
        list.add(new UserAction("删除用户所有属性值", Constants.PROFILE_DELETE, "删除成功", new Runnable() {
            @Override
            public void run() {
                AnalysysAgent.profileDelete(UserInfoActivity.this);
            }
        }));

        List<String> listStr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            listStr.add(list.get(i).name);
        }
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listStr) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                float scale = getResources().getDisplayMetrics().density;
                view.getLayoutParams().height = (int) (40 * scale + 0.5f);
                return view;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list.get(position).doAction();
            }
        });
    }
}
