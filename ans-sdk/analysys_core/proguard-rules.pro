#################################################################
########################### 易观混淆 ############################
#################################################################

# 混淆到包名下
-dontwarn com.analysys.**

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保证API不混淆
-keep class com.analysys.AnalysysAgent{
 *;
}
-keep public interface com.analysys.AnalysysAgent$AnalysysNetworkType{
 *;
}
-keep public interface com.analysys.ObserverListener{
 *;
}
-keep public interface com.analysys.LogObserverListener{
 *;
}
-keep class com.analysys.ANSAutoPageTracker{
 *;
}
-keep public interface com.analysys.ANSAutoPageTracker{
 *;
}
-keep class com.analysys.AnalysysConfig{
 *;
}
-keep class com.analysys.utils.InternalAgent {
 *;
 }
-keep class com.analysys.EncryptEnum{
 *;
}
-keep class com.analysys.push.** {
 *;
 }
-keep class com.analysys.utils.ActivityLifecycleUtils$BaseLifecycleCallback {
 *;
 }
-keep class com.analysys.utils.ActivityLifecycleUtils {
 *;
 }

-keep class com.analysys.utils.AThreadPool {
 *;
 }

 -keep class com.analysys.thread.AnsLogicThread {
 *;
 }

  -keep class com.analysys.thread.AnsLogicThread$PriorityLevel {
  *;
  }

 -keep class com.analysys.utils.ExceptionUtil {
  *;
  }

-keep class com.analysys.utils.Constants {
 *;
 }
 -keep class com.analysys.utils.CommonUtils {
  *;
 }
 -keep class com.analysys.utils.AnalysysUtil {
  *;
 }
  -keep class com.analysys.utils.SharedUtil {
   *;
  }
 -keep class com.analysys.utils.AnsReflectUtils {
  *;
 }
 -keep class com.analysys.process.AgentProcess {
  *;
 }
 -keep class com.analysys.process.AgentProcess$* {
  *;
 }
 -keep class com.analysys.process.SystemIds {
  *;
}
 -keep class com.analysys.process.PathGeneral {
  *;
}
-keep class com.analysys.database.AnsContentProvider {
  *;
}
-keep class com.analysys.push.ThirdPushManager$* {
     <methods>;
 }

-keep class com.analysys.hybrid.HybridBridge {
     <methods>;
 }

-keep class com.analysys.hybrid.BaseWebViewInjector {
 *;
}

-keep class com.analysys.hybrid.WebViewInjectManager {
 *;
}

-keep class com.analysys.hybrid.HybridObject {
 *;
}

-keep class com.analysys.push.PushProvider {
    <fields>;
 }

-keep class com.analysys.allgro.plugin.ASMProbeHelp {
*;
}

-keep class com.analysys.allgro.plugin.ASMHookAdapter {
 *;
}

-keep class com.analysys.ui.RootView {
 *;
}

-keep class com.analysys.ui.UniqueViewHelper {
 *;
}

-keep class com.analysys.ui.WindowUIHelper {
 *;
}

-keep class com.analysys.ui.WindowUIHelper$* {
 *;
}

-keep class com.analysys.utils.ANSLog {
 *;
}

-keep class com.analysys.network.NetworkUtils {
 *;
}

-keep class com.analysys.strategy.PolicyManager {
 *;
}
-keep class com.analysys.AnsRamControl {
 *;
}

-keep class com.analysys.ipc.IIpcProxy {
 *;
}

-keep class com.analysys.ipc.IAnalysysClient {
 *;
}

-keep class com.analysys.ipc.IAnalysysMain {
 *;
}

-keep class com.analysys.ipc.IpcManager {
 *;
}

-keep class com.analysys.ipc.BytesParcelable {
 *;
}

-keep class com.analysys.process.HeatMap {
 *;
}
