#-ignorewarnings                     # 忽略警告，避免打包时某些警告出现
#-dontshrink
#-optimizationpasses 5               # 指定代码的压缩级别
#-dontusemixedcaseclassnames         # 是否使用大小写混合
#-dontskipnonpubliclibraryclasses    # 是否混淆第三方jar
#-dontpreverify                      # 混淆时是否做预校验
#-verbose                            # 混淆时是否记录日志
#
## 保持哪些类不被混淆
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class * extends android.content.BroadcastReceiver
#
##-keepattributes Exceptions,InnerClasses
#
#-keep,allowshrinking class com.analysys.AnalysysAgent{
# *;
#}
#-keep class com.analysys.AnalysysConfig{
# *;
#}
#-keep class com.analysys.utils.InternalAgentent {
# *;
# }
#-keep class com.analysys.EncryptEnum{
# *;
#}
#-keep,allowshrinking class com.analysys.push.** {
# *;
# }
#-keep,allowshrinking class com.analysys.push.ThirdPushManager$* {
#     <methods>;
# }
#
#-keep,allowshrinking class com.analysys.hybrid.HybridBridge {
#     <methods>;
# }
#
#-keep,allowshrinking class com.analysys.push.PushProvider {
#    <fields>;
# }
#-keepclasseswithmembernames class * {       # 保持 native 方法不被混淆
#     native <methods>;
# }
#
#-keepclasseswithmembers class * {            # 保持自定义控件类不被混淆
#     public <init>(android.content.Context, android.util.AttributeSet);
# }
#
#-keepclasseswithmembers class * {            # 保持自定义控件类不被混淆
#     public <init>(android.content.Context, android.util.AttributeSet, int);
# }
#
#-keepclassmembers class * extends android.app.Activity { #保持类成员
#    public void *(android.view.View);
# }
#
#-keepclassmembers enum * {                  # 保持枚举 enum 类不被混淆
#     public static **[] values();
#     public static ** valueOf(java.lang.String);
# }
#
#-keep class * implements android.os.Parcelable {    # 保持 Parcelable 不被混淆
#   public static final android.os.Parcelable$Creator *;
# }
#
#-keep,allowshrinking class com.analysys.push.ThirdPushManager$* {
#     <methods>;
# }
#
#-keep,allowshrinking class com.analysys.hybrid.HybridBridge {
#     <methods>;
# }
#
#-keep,allowshrinking class com.analysys.push.PushProvider {
#    <fields>;
# }
#
##过滤泛型
#-keepattributes Signature,InnerClasses
#

-optimizationpasses 5
##打印混淆的详细信息
#-verbose
##声明不压缩输入文件。
#-dontshrink
 # 不进行预校验,Android不需要,可加快混淆速度。
-dontpreverify
#将次选项去除，可以混淆时进行优化
-dontoptimize
# 屏蔽警告
-ignorewarnings
# 混淆时不会产生形形色色的类名(混淆时不使用大小写混合类名)
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库类(不跳过library中的非public的类)
-dontskipnonpubliclibraryclasses
# 指定不去忽略包可见的库类的成员
-dontskipnonpubliclibraryclassmembers
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*
# 避免混淆泛型, 这在JSON实体映射时非常重要
-keepattributes Signature
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.job.JobService
-keep public class * extends android.accessibilityservice.AccessibilityService
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

#################################################################
########################### 易观混淆 ############################
#################################################################
# 混淆到包名下
-dontwarn com.analysys.**
# 保证API不混淆
-keep class com.analysys.**{*;}
