# kotlinx.serialization: 자판 JSON 모델의 serializer를 보존한다.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class io.github.boronare.dragkeyboard.core.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.boronare.dragkeyboard.core.**$$serializer { *; }
