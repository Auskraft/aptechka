package ru.aptechka.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.aptechka.ui.theme.LocalDimens

/**
 * A swipe action revealed behind a row.
 * @param dismiss whether confirming the swipe removes the row (true) or snaps
 * back after running [onAction] (false — e.g. for "add to shopping").
 */
class SwipeAction(
    val icon: ImageVector,
    val container: Color,
    val iconTint: Color,
    val dismiss: Boolean,
    val onAction: () -> Unit,
)

/**
 * Wraps [content] (a rounded row) with optional swipe actions: [startToEnd]
 * revealed on swipe-right, [endToStart] on swipe-left. Trigger threshold 70%.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeRow(
    startToEnd: SwipeAction?,
    endToStart: SwipeAction?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dims = LocalDimens.current
    val state = rememberSwipeToDismissBoxState(
        positionalThreshold = { total -> total * 0.7f },
        confirmValueChange = { value ->
            val action = when (value) {
                SwipeToDismissBoxValue.StartToEnd -> startToEnd
                SwipeToDismissBoxValue.EndToStart -> endToStart
                SwipeToDismissBoxValue.Settled -> null
            }
            action?.onAction?.invoke()
            action?.dismiss == true
        },
    )

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = startToEnd != null,
        enableDismissFromEndToStart = endToStart != null,
        backgroundContent = {
            val action = when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> startToEnd
                SwipeToDismissBoxValue.EndToStart -> endToStart
                SwipeToDismissBoxValue.Settled -> null
            }
            if (action != null) {
                val alignment =
                    if (state.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                        Alignment.CenterStart
                    } else {
                        Alignment.CenterEnd
                    }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(dims.radiusMd))
                        .background(action.container)
                        .padding(horizontal = dims.xl),
                    contentAlignment = alignment,
                ) {
                    Icon(action.icon, contentDescription = null, tint = action.iconTint, modifier = Modifier.size(24.dp))
                }
            }
        },
        content = { content() },
    )
}
