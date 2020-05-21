#################################################################
########################### 易观混淆 ############################
#################################################################

-dontwarn com.analysys.**
-keep class com.analysys** {*;}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#-keep public class [您的应用包名].R$*{
#public static final int *;
#}