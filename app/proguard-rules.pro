#############################################
# UNIVERSAL PROGUARD CONFIG FOR ANDROID APP #
#############################################

# ========== Сохраняем классы для Firebase =============
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ========== Retrofit/OkHttp (REST) ====================
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ========== Gson, Moshi, Jackson (JSON) ===============
-keep class com.google.gson.** { *; }
-keep class com.squareup.moshi.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep class com.ensias.healthcareapp.model.** { *; }

# ========== Room Database =============================
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# ========== Glide и Picasso (Загрузка изображений) =====
-keep class com.bumptech.glide.** { *; }
-keep class com.squareup.picasso.** { *; }

# ========== ViewBinding и DataBinding ==================
-keep class **.databinding.*Binding { *; }


# ========== AndroidX и компоненты Jetpack ==============
-keep class androidx.** { *; }
-dontwarn androidx.**

# ========== Activities, Fragments, Services ============
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.app.Application
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment

# ========== WorkManager (Задания в фоне) ===============
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ========== Локализация и ресурсы ======================
-keepclassmembers class ** {
    @android.webkit.JavascriptInterface <methods>;
}

# ========== Сохраняем аннотации, сигнатуры и line numbers (для стека ошибок) ===
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# ========== Скрываем оригинальные имена файлов (опционально, для безопасности) ==
#-renamesourcefileattribute SourceFile

# ========== Для WebView JavaScript Bridge ==============
#-keepclassmembers class com.yourpackage.YourJSInterface {
#   public *;
#}

# ========== General ==============
-dontwarn sun.misc.**
-dontwarn org.codehaus.mojo.**

# ====================== END ============================

