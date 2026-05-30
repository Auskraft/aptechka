package ru.aptechka.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layout-rhythm tokens (spacing + corner radii). Component-intrinsic sizes
 * (icon 24dp, form tile 56dp, status stripe 4dp) stay inline at call sites.
 *
 * Provided via [LocalDimens]; a larger set can be supplied for wide screens
 * (WindowSizeClass) without touching the screens.
 */
@Stable
data class Dimens(
    // Spacing scale (4-multiples)
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,

    // Semantic spacing aliases
    val screenPadding: Dp = 16.dp,
    val sectionPadding: Dp = 20.dp,
    val itemGap: Dp = 8.dp,
    val cardGap: Dp = 12.dp,

    // Corner radii (design r-xs … r-pill)
    val radiusXs: Dp = 8.dp,
    val radiusSm: Dp = 12.dp,
    val radiusMd: Dp = 16.dp,
    val radiusLg: Dp = 20.dp,
    val radiusXl: Dp = 28.dp,
    val radiusPill: Dp = 999.dp,
)

val LocalDimens = staticCompositionLocalOf { Dimens() }
