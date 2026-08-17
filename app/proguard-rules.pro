# Add project specific ProGuard rules here.
# Keep Room entities
-keep class com.moazzam.muntakhabahadith.data.db.** { *; }

# Keep PDF viewer library
-keep class com.rajat.pdfviewer.** { *; }

# Keep Kotlin metadata (required by libraries using reflection)
-keepattributes *Annotation*
-keepclassmembers class ** {
    @androidx.room.* <methods>;
}
