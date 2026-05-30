package ru.aptechka.ui.forms

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ru.aptechka.R
import ru.aptechka.domain.model.CategoryKey
import ru.aptechka.domain.model.FormKey
import ru.aptechka.ui.theme.LocalFormColors

/**
 * Single source of truth for how a medicine [FormKey] / [CategoryKey] is presented:
 * tile colour (from the theme), icon, and localized label.
 */
object Forms {

    fun icon(form: FormKey): ImageVector = when (form) {
        FormKey.TABLET      -> Icons.Outlined.Medication
        FormKey.CAPSULE     -> Icons.Outlined.Medication
        FormKey.SYRUP       -> Icons.Outlined.LocalDrink
        FormKey.DROPS       -> Icons.Outlined.Opacity
        FormKey.OINTMENT    -> Icons.Outlined.Spa
        FormKey.INJECTION   -> Icons.Outlined.Vaccines
        FormKey.SPRAY       -> Icons.Outlined.Air
        FormKey.PATCH       -> Icons.Outlined.Healing
        FormKey.SUPPOSITORY -> Icons.Outlined.Medication
        FormKey.OTHER       -> Icons.Outlined.Medication
    }

    @Composable
    fun color(form: FormKey): Color = LocalFormColors.current.forForm(form)

    @Composable
    fun label(form: FormKey): String = stringResource(labelRes(form))

    @Composable
    fun label(category: CategoryKey): String = stringResource(labelRes(category))

    @StringRes
    private fun labelRes(form: FormKey): Int = when (form) {
        FormKey.TABLET      -> R.string.form_tablet
        FormKey.CAPSULE     -> R.string.form_capsule
        FormKey.SYRUP       -> R.string.form_syrup
        FormKey.DROPS       -> R.string.form_drops
        FormKey.OINTMENT    -> R.string.form_ointment
        FormKey.INJECTION   -> R.string.form_injection
        FormKey.SPRAY       -> R.string.form_spray
        FormKey.PATCH       -> R.string.form_patch
        FormKey.SUPPOSITORY -> R.string.form_suppository
        FormKey.OTHER       -> R.string.form_other
    }

    @StringRes
    private fun labelRes(category: CategoryKey): Int = when (category) {
        CategoryKey.ANALGESIC        -> R.string.category_analgesic
        CategoryKey.ANTIBIOTIC       -> R.string.category_antibiotic
        CategoryKey.ANTIVIRAL        -> R.string.category_antiviral
        CategoryKey.ANTIFUNGAL       -> R.string.category_antifungal
        CategoryKey.ANTIHISTAMINE    -> R.string.category_antihistamine
        CategoryKey.CARDIOVASCULAR   -> R.string.category_cardiovascular
        CategoryKey.GASTROINTESTINAL -> R.string.category_gastrointestinal
        CategoryKey.RESPIRATORY      -> R.string.category_respiratory
        CategoryKey.DERMATOLOGICAL   -> R.string.category_dermatological
        CategoryKey.VITAMINS         -> R.string.category_vitamins
        CategoryKey.HORMONAL         -> R.string.category_hormonal
        CategoryKey.NEUROLOGICAL     -> R.string.category_neurological
        CategoryKey.OPHTHALMIC       -> R.string.category_ophthalmic
        CategoryKey.OTHER            -> R.string.category_other
    }
}
