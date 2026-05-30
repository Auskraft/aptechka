package ru.aptechka.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import ru.aptechka.domain.model.FormKey

// ── Status colors (fixed — never change with Material You dynamic theming) ────

@Stable
data class StatusColors(
    val expiredFg: Color,
    val expiredContainer: Color,
    val expiringFg: Color,
    val expiringContainer: Color,
    val okFg: Color,
    val okContainer: Color,
)

val LightStatusColors = StatusColors(
    expiredFg        = Color(0xFFC4452D),
    expiredContainer = Color(0xFFF8DAD2),
    expiringFg        = Color(0xFFB57A14),
    expiringContainer = Color(0xFFFBEDC9),
    okFg        = Color(0xFF4A7C59),
    okContainer = Color(0xFFCFE9D5),
)

val DarkStatusColors = StatusColors(
    expiredFg        = Color(0xFFF0786A),
    expiredContainer = Color(0xFF4A1F18),
    expiringFg        = Color(0xFFF5C26B),
    expiringContainer = Color(0xFF4A370E),
    okFg        = Color(0xFF7DBC8F),
    okContainer = Color(0xFF1F3A28),
)

val LocalStatusColors = compositionLocalOf { LightStatusColors }

// ── Pharmacy accent palette (8 swatches) ─────────────────────────────────────

@Stable
data class AccentPalette(val fg: Color, val container: Color)

private val green  = AccentPalette(Color(0xFF4A7C59), Color(0xFFCFE9D5))
private val blue   = AccentPalette(Color(0xFF426EA3), Color(0xFFD7E5F7))
private val orange = AccentPalette(Color(0xFFB56A2C), Color(0xFFF6DDC2))
private val violet = AccentPalette(Color(0xFF6E5A9B), Color(0xFFE0D7F2))
private val pink   = AccentPalette(Color(0xFFA65082), Color(0xFFF4D7E5))
private val teal   = AccentPalette(Color(0xFF347D7D), Color(0xFFC9E5E3))
private val amber  = AccentPalette(Color(0xFFB58300), Color(0xFFF6E1A5))
private val grey   = AccentPalette(Color(0xFF6E6A60), Color(0xFFDCD8CE))

private val greenDark  = AccentPalette(Color(0xFFA8D5B4), Color(0xFF2A4634))
private val blueDark   = AccentPalette(Color(0xFF95B8E0), Color(0xFF243A56))
private val orangeDark = AccentPalette(Color(0xFFE2A574), Color(0xFF4F3318))
private val violetDark = AccentPalette(Color(0xFFBBA6E1), Color(0xFF382E54))
private val pinkDark   = AccentPalette(Color(0xFFE2A2BD), Color(0xFF4F2842))
private val tealDark   = AccentPalette(Color(0xFF87C5C2), Color(0xFF1F3D3B))
private val amberDark  = AccentPalette(Color(0xFFE2C075), Color(0xFF3D2F0A))
private val greyDark   = AccentPalette(Color(0xFFB5B1A6), Color(0xFF38362F))

// ── Form-of-medicine tile colors (one accent per form, theme-swappable) ───────

@Stable
data class FormColors(
    val tablet: Color,
    val capsule: Color,
    val syrup: Color,
    val drops: Color,
    val ointment: Color,
    val injection: Color,
    val spray: Color,
    val patch: Color,
    val suppository: Color,
    val other: Color,
) {
    fun forForm(form: FormKey): Color = when (form) {
        FormKey.TABLET      -> tablet
        FormKey.CAPSULE     -> capsule
        FormKey.SYRUP       -> syrup
        FormKey.DROPS       -> drops
        FormKey.OINTMENT    -> ointment
        FormKey.INJECTION   -> injection
        FormKey.SPRAY       -> spray
        FormKey.PATCH       -> patch
        FormKey.SUPPOSITORY -> suppository
        FormKey.OTHER       -> other
    }
}

val LightFormColors = FormColors(
    tablet      = Color(0xFF4A7C59),
    capsule     = Color(0xFF426EA3),
    syrup       = Color(0xFFB57A14),
    drops       = Color(0xFF4E6868),
    ointment    = Color(0xFF836A47),
    injection   = Color(0xFF6E5A9B),
    spray       = Color(0xFF347D7D),
    patch       = Color(0xFFA65082),
    suppository = Color(0xFF6E6A60),
    other       = Color(0xFF6E6A60),
)

val DarkFormColors = FormColors(
    tablet      = Color(0xFF3E6B4B),
    capsule     = Color(0xFF3A5E89),
    syrup       = Color(0xFF9A6912),
    drops       = Color(0xFF435A5A),
    ointment    = Color(0xFF6F5A3C),
    injection   = Color(0xFF5E4D85),
    spray       = Color(0xFF2C6B6B),
    patch       = Color(0xFF8E4570),
    suppository = Color(0xFF5E5B52),
    other       = Color(0xFF5E5B52),
)

val LocalFormColors = compositionLocalOf { LightFormColors }

object KitColors {
    val keys = listOf("green", "blue", "orange", "violet", "pink", "teal", "amber", "grey")

    fun get(key: String, dark: Boolean): AccentPalette = when (key) {
        "green"  -> if (dark) greenDark  else green
        "blue"   -> if (dark) blueDark   else blue
        "orange" -> if (dark) orangeDark else orange
        "violet" -> if (dark) violetDark else violet
        "pink"   -> if (dark) pinkDark   else pink
        "teal"   -> if (dark) tealDark   else teal
        "amber"  -> if (dark) amberDark  else amber
        "grey"   -> if (dark) greyDark   else grey
        else     -> if (dark) greenDark  else green
    }

    @Composable
    fun get(key: String): AccentPalette = get(key, isSystemInDarkTheme())
}
