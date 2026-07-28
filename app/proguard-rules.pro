# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Firebase
-keep class com.google.firebase.** { *; }

# Agora
-keep class io.agora.** { *; }

# Keep data models
-keep class tj.safaraligroup.sim.data.model.** { *; }
