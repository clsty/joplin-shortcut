# Add project specific ProGuard rules here.
-keep class io.github.clsty.joplinshortcut.data.api.** { *; }
-keep class io.github.clsty.joplinshortcut.data.db.entities.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
