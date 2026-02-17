plugins {
    // NO android.application plugin here!
    id("com.google.gms.google-services") version "4.4.4" apply false
    alias(libs.plugins.android.application) apply false
    id ("org.jetbrains.kotlin.plugin.parcelize") version "2.3.10" apply false
}