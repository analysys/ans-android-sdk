#################################################################
########################### 易观混淆 ############################
#################################################################

# 打通所有的包
-repackageclass com.analysys.aesencrypt
# 混淆到包名下
-dontwarn com.analysys.aesencrypt.**
# 保证API不混淆
-keep class com.analysys.aesencrypt.EncryptAgent{
 *;
}
