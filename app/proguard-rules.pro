# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-printconfiguration proguard-merged-config.txt
-dontwarn org.slf4j.impl.**
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

-dontwarn java.beans.ConstructorProperties.**
-dontwarn java.beans.Transient.**
-dontwarn org.bouncycastle.jsse.BCSSLParameters.**
-dontwarn org.bouncycastle.jsse.BCSSLSocket.**
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider.**
-dontwarn org.conscrypt.Conscrypt$Version.**
-dontwarn org.conscrypt.Conscrypt.**
-dontwarn org.conscrypt.ConscryptHostnameVerifier.**
-dontwarn org.openjsse.javax.net.ssl.SSLParameters.**
-dontwarn org.openjsse.javax.net.ssl.SSLSocket.**
-dontwarn org.openjsse.net.ssl.OpenJSSE.**
-dontwarn org.slf4j.impl.StaticLoggerBinder.**
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry.**

-keep public class * extends com.app.plutope.ui.base.BaseActivity
-keep class * extends androidx.fragment.app.Fragment{}
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

-keepattributes Signature
-keepattributes *Annotation*

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.examples.android.model.** { <fields>; }

-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class * implements java.io.Serializable { *; }


# Gson specific classes
-keep public class com.google.gson.Gson
-keep class com.google.gson.stream.** { *; }
-keep class com.app.plutope.model.Wallets { *; }
-keep class com.app.plutope.model.Wallet { *; }
-keep class com.app.plutope.model.Tokens { *; }
-keep class com.app.plutope.model.CurrencyList { *; }
-keep class com.app.plutope.model.CurrencyModel { *; }
-keep class com.app.plutope.model.CountryListModel { *; }
-keep class com.app.plutope.model.ContactModel { *; }
-keep class com.app.plutope.model.CoinGeckoMarketsResponse { *; }
-keep class com.app.plutope.model.NFTListModel { *; }
-keep class com.app.plutope.model.NFTModel { *; }
-keep class com.app.plutope.model.Metadata { *; }
-keep class com.app.plutope.model.Attribute { *; }
-keep class com.app.plutope.networkConfig.Chain { *; }
-keep class com.app.plutope.utils.contractWrapperClass.MyContract { *; }
-keep class com.app.plutope.utils.contractWrapperClass.MyContractMainnet { *; }

-keepclassmembers class com.app.plutope.model.Tokens { <fields>; }
-keepclassmembers class com.app.plutope.model.CurrencyModel { <fields>; }
-keepclassmembers class com.app.plutope.networkConfig.Chain { <fields>; }
-keepclassmembers class com.app.plutope.model.NFTListModel { <fields>; }
-keepclassmembers class com.app.plutope.utils.contractWrapperClass.MyContract { <fields>; }
-keepclassmembers class com.app.plutope.utils.contractWrapperClass.MyContractMainnet { <fields>; }



-keep class com.app.plutope.model.**
-keep class com.squareup.okhttp.** { *; }
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn com.sun.javadoc.Doclet.*


# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

-dontwarn java8.util.**
-dontwarn jnr.posix.**
#-dontwarn com.kenai.**

#-keep class org.bouncycastle.**
-dontwarn org.bouncycastle.jce.provider.X509LDAPCertStoreSpi
-dontwarn org.bouncycastle.x509.util.LDAPStoreHelper

-keepclassmembers class org.web3j.protocol.** { *; }
-keepclassmembers class org.web3j.crypto.* { *; }

-keep class * extends org.web3j.abi.TypeReference
-keep class * extends org.web3j.abi.datatypes.Type

#-dontwarn java.lang.SafeVarargs
-dontwarn org.slf4j.**

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Uncomment for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

