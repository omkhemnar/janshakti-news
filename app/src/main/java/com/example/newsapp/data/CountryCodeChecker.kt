package com.example.newsapp.data

// Matches the given country string from geocoder to its corresponding country code (ISO-3166 alpha2)
// Only includes countries supported by NewsAPI
class CountryCodeChecker {
    fun checkCountryCode(country: String): String {
        val countryCode = when (country) {
            "United Arab Emirates" -> "ae"
            "Argentina" -> "ar"
            "Austria" -> "at"
            "Australia" -> "au"
            "Belgium" -> "be"
            "Bulgaria" -> "bg"
            "Brazil" -> "br"
            "Canada" -> "ca"
            "Switzerland" -> "ch"
            "China" -> "cn"
            "Colombia" -> "co"
            "Cuba" -> "cu"
            "Czech Republic" -> "cz"
            "Germany" -> "de"
            "Egypt" -> "eg"
            "France" -> "fr"
            "United Kingdom" -> "gb"
            "Greece" -> "gr"
            "Hong Kong" -> "hk"
            "Hungary" -> "hu"
            "Indonesia" -> "id"
            "Ireland" -> "ie"
            "Israel" -> "il"
            "India" -> "in"
            "Italy" -> "it"
            "Japan" -> "jp"
            "Republic of Korea" -> "kr"
            "South Korea" -> "kr" // Both names for South Korea
            "Lithuania" -> "lt"
            "Latvia" -> "lv"
            "Morocco" -> "ma"
            "Mexico" -> "mx"
            "Malaysia" -> "my"
            "Nigeria" -> "ng"
            "Netherlands" -> "nl"
            "Norway" -> "no"
            "New Zealand" -> "nz"
            "Philippines" -> "ph"
            "Poland" -> "pl"
            "Portugal" -> "pt"
            "Romania" -> "ro"
            "Serbia" -> "rs"
            "Russia" -> "ru"
            "Saudi Arabia" -> "sa"
            "Sweden" -> "se"
            "Singapore" -> "sg"
            "Slovenia" -> "si"
            "Slovakia" -> "sk"
            "Thailand" -> "th"
            "Türkiye" -> "tr"
            "Turkey" -> "tr" // Both spellings for Türkiye
            "Taiwan" -> "tw"
            "Ukraine" -> "ua"
            "United States" -> "us"
            "Venezuela" -> "ve"
            "South Africa" -> "za"

            else -> "us" // Default case
        }

        return countryCode
    }
}