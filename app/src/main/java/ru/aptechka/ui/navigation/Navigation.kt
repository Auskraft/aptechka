package ru.aptechka.ui.navigation

sealed class Screen(val route: String) {
    object Kits       : Screen("kits")
    object KitDetail  : Screen("kit/{kitId}") {
        fun go(kitId: Long) = "kit/$kitId"
    }
    object MedDetail  : Screen("med/{drugId}") {
        fun go(drugId: Long) = "med/$drugId"
    }
    object AddDrug    : Screen("add_drug/{kitId}?catalogId={catalogId}") {
        fun go(kitId: Long, catalogId: Long = -1L) = "add_drug/$kitId?catalogId=$catalogId"
    }
    object Scanner    : Screen("scanner/{kitId}") {
        fun go(kitId: Long) = "scanner/$kitId"
    }
    object Expiry     : Screen("expiry")
    object Shopping   : Screen("shopping")
    object Settings   : Screen("settings")
}
