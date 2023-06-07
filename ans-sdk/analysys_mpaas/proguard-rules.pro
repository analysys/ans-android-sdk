#################################################################
########################### 易观混淆 ############################
#################################################################

# 混淆到包名下
-dontwarn com.analysys.mpaas.**
# 保证API不混淆
-keep class com.analysys.mpaas.AnalysysMpaas{
 *;
}
-keep class com.analysys.mpaas.AnalysysH5Plugin{
 *;
}
