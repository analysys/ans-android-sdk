#################################################################
########################### 易观混淆 ############################
#################################################################

# 混淆到包名下
-dontwarn com.analysys.allgro.**
# 全埋点相混淆
 -keep class com.analysys.allgro.AnalysysProbe {*;}
 -keep class com.analysys.allgro.annotations** {*;}
 -keep class com.analysys.allgro.plugin.ViewClickProbe{*;}
 -keep class com.analysys.allgro.plugin.PageViewProbe{*;}