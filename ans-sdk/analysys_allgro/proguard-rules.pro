#################################################################
########################### 易观混淆 ############################
#################################################################

# 混淆到包名下
-dontwarn com.analysys.allgro.**
# 全埋点相混淆
 -keep class com.analysys.allgro.plugin.ASMProbeHelp {*;}
 -keep class com.analysys.allgro.annotations** {*;}