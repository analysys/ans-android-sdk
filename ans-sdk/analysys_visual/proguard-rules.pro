#################################################################
########################### 易观混淆 ############################
#################################################################

# 混淆到包名下
-dontwarn com.analysys.visual.**
# 保证API不混淆
-keep class com.analysys.visual.VisualAgent{
 *;
}
-keep class com.analysys.visual.bind.VisualASMListener{
 *;
}
-keep class com.analysys.visual.utils.VisualIpc{
 *;
}
-keep class com.analysys.visual.bind.VisualBindManager{
 *;
}
