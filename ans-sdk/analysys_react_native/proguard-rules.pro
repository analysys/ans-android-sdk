#################################################################
########################### 易观混淆 ############################
#################################################################

# 混淆到包名下
-dontwarn com.analysys.rn.**
# 保证API不混淆
-keep class com.analysys.rn.RNAnalysysAgentModule{
 *;
}
-keep class com.analysys.rn.RNAnalysysAgentPackage{
 *;
}
