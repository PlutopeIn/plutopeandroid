package com.app.plutope.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

class CountryDetailList : ArrayList<CountryDetailList.CountryDetailListItem>() {
    @Parcelize
    data class CountryDetailListItem(
        @SerializedName("alpha2Code")
        var alpha2Code: String?,
        @SerializedName("alpha3Code")
        var alpha3Code: String?,
        @SerializedName("altSpellings")
        var altSpellings: List<String?>?,
        @SerializedName("area")
        var area: Double?,
        @SerializedName("borders")
        var borders: List<String?>?,
        @SerializedName("callingCodes")
        var callingCodes: List<String?>?,
        @SerializedName("capital")
        var capital: String?,
        @SerializedName("cioc")
        var cioc: String?,
        @SerializedName("currencies")
        var currencies: List<Currency?>?,
        @SerializedName("demonym")
        var demonym: String?,
        @SerializedName("flag")
        var flag: String?,
        @SerializedName("flags")
        var flags: Flags?,
        @SerializedName("gini")
        var gini: Double?,
        @SerializedName("independent")
        var independent: Boolean?,
        @SerializedName("languages")
        var languages: List<Language?>?,
        @SerializedName("latlng")
        var latlng: List<Double?>?,
        @SerializedName("name")
        var name: String?,
        @SerializedName("nativeName")
        var nativeName: String?,
        @SerializedName("numericCode")
        var numericCode: String?,
        @SerializedName("population")
        var population: Int?,
        @SerializedName("region")
        var region: String?,
        @SerializedName("regionalBlocs")
        var regionalBlocs: List<RegionalBloc?>?,
        @SerializedName("subregion")
        var subregion: String?,
        @SerializedName("timezones")
        var timezones: List<String?>?,
        @SerializedName("topLevelDomain")
        var topLevelDomain: List<String?>?,
        @SerializedName("translations")
        var translations: Translations?
    ) : Parcelable {
        @Parcelize
        data class Currency(
            @SerializedName("code")
            var code: String?,
            @SerializedName("name")
            var name: String?,
            @SerializedName("symbol")
            var symbol: String?
        ) : Parcelable

        @Parcelize
        data class Flags(
            @SerializedName("png")
            var png: String?,
            @SerializedName("svg")
            var svg: String?
        ) : Parcelable

        @Parcelize
        data class Language(
            @SerializedName("iso639_1")
            var iso6391: String?,
            @SerializedName("iso639_2")
            var iso6392: String?,
            @SerializedName("name")
            var name: String?,
            @SerializedName("nativeName")
            var nativeName: String?
        ) : Parcelable

        @Parcelize
        data class RegionalBloc(
            @SerializedName("acronym")
            var acronym: String?,
            @SerializedName("name")
            var name: String?,
            @SerializedName("otherAcronyms")
            var otherAcronyms: List<String?>?,
            @SerializedName("otherNames")
            var otherNames: List<String?>?
        ) : Parcelable

        @Parcelize
        data class Translations(
            @SerializedName("br")
            var br: String?,
            @SerializedName("de")
            var de: String?,
            @SerializedName("es")
            var es: String?,
            @SerializedName("fa")
            var fa: String?,
            @SerializedName("fr")
            var fr: String?,
            @SerializedName("hr")
            var hr: String?,
            @SerializedName("hu")
            var hu: String?,
            @SerializedName("it")
            var `it`: String?,
            @SerializedName("ja")
            var ja: String?,
            @SerializedName("nl")
            var nl: String?,
            @SerializedName("pt")
            var pt: String?
        ) : Parcelable
    }
}