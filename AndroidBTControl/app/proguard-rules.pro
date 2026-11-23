# Project-specific R8 / ProGuard rules for NTUT CSL Light Control.
# Jetpack Compose, Coroutines, and AndroidX ship embedded consumer rules.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn kotlin.coroutines.jvm.internal.**
