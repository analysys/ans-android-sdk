
## [**易观简介**](https://www.analysys.cn)

易观是中国领先的大数据公司，始终追求客户成功的经营宗旨。自成立以来，易观打造了以海量数字用户资产及算法模型为核心的大数据产品、平台及解决方案，可以帮助企业高效管理数字用户资产和对产品进行精细化运营，通过数据驱动营销闭环，从而实现收入增长、成本降低和效率提升，并显著规避经营风险，实现精益成长。易观的数据平台是易观方舟，产品家族包括易观千帆、易观万像以及行业解决方案。


## SDK 简介

ans-android-sdk 是一款商用级别的用户行为采集项目，目前支持代码埋点、可视化全埋点、点击热图等。使用简单，功能模块结构清晰，可按需添加。

## 一分钟上手

1.清单文件配置

``` xml
<!--  权限设置  -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

<application >

    ......
    <provider
    android:name="com.analysys.database.AnsContentProvider"
    android:authorities="[应用包名].AnsContentProvider"
    android:enabled="true"
    android:exported="false" />
    <!--  设置appKey  -->
    <meta-data
    android:name="ANALYSYS_APPKEY"
    android:value="4g6dp3d9fh5r0s3jr87j3ej94k04" />
    <!--  设置渠道  -->
    <meta-data
    android:name="ANALYSYS_CHANNEL"
    android:value="WanDouJia" />
    <!--  服务端加密证书（在assets上预制）  -->
    <meta-data
    android:name="ANALYSYS_KEYSTONE"
    android:value="analysys.crt"/>
</application>
```

2.在**Application**中始化
    
``` java

// 初始化
AnalysysAgent.init(this, config);
// 热图功能开关
AnalysysAgent.setAutoHeatMap(false);
// 设置数据上传/更新地址
AnalysysAgent.setUploadURL(this, UPLOAD_URL);

// 初始化可视化特色功能（可选）
// 设置长连接Url
AnalysysAgent.setVisitorDebugURL(context, SOCKET_URL);
// 设置配置下发Url
AnalysysAgent.setVisitorConfigURL(context, CONFIG_URL);

```

3.可视化埋点功能

- 在项目根目录文件gradle.properties 设置 Build_Type=visual
- 运行apidemo项目，启动后进入可视化demo入口
- 打开 https://growth.analysys.cn/project-management/visual-choice 左上角选中电商Demo，选Android平台
- 在app打开的情况下摇一摇，设备连接成功后点击设备进入埋点界面
- 点击View添加事件，点击右下方部署下发配置



## [详细文档](https://docs.analysys.cn/ark/integration/sdk/android)


## 讨论

扫码后加入官方谈论群，和我们一起探索！

![](./img/ans.png)


## License

[gpl-3.0](https://www.gnu.org/licenses/gpl-3.0.txt)
