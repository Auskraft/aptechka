package ru.aptechka.ui.forms

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import ru.aptechka.R
import kotlin.math.abs

private const val DAY_MS = 24L * 60 * 60 * 1000

/**
 * Localized expiry phrase for a batch expiration timestamp.
 * [short] uses the compact "через …" form (lists) vs. "истекает через …" (cards).
 * Switches days→months past a 60-day cutoff.
 */
@Composable
fun expiryLabel(expirationDate: Long, short: Boolean = false): String {
    val days = ((expirationDate - System.currentTimeMillis()) / DAY_MS).toInt()
    val inRes = if (short) R.string.expiry_in_short else R.string.expiry_in
    return when {
        days < 0  -> stringResource(
            R.string.expiry_expired,
            pluralStringResource(R.plurals.plural_days, abs(days), abs(days)),
        )
        days == 0 -> stringResource(R.string.expiry_today)
        days < 60 -> stringResource(inRes, pluralStringResource(R.plurals.plural_days, days, days))
        else      -> {
            val months = days / 30
            stringResource(inRes, pluralStringResource(R.plurals.plural_months, months, months))
        }
    }
}
