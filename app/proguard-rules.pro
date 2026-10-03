# kotlinx.serialization: 자판 JSON 모델의 serializer를 보존한다.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.naremotion.dragkeyboard.core.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.naremotion.dragkeyboard.core.**$$serializer { *; }
