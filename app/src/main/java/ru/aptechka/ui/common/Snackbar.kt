package ru.aptechka.ui.common

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.aptechka.R

/** One-shot snackbar message; carries a string resource + format args (resolved in the UI). */
sealed class SnackbarMessage(@StringRes val resId: Int, vararg val args: Any) {
    class ToShopping(name: String) : SnackbarMessage(R.string.snack_to_shopping, name)
    class DeletedNamed(name: String) : SnackbarMessage(R.string.snack_deleted_named, name)
    class KitDeleted(name: String) : SnackbarMessage(R.string.snack_kit_deleted, name)
    data object BatchDeleted : SnackbarMessage(R.string.snack_batch_deleted)
    data object BatchAdded : SnackbarMessage(R.string.snack_batch_added)
}

/** Composable into a ViewModel to emit one-shot snackbar messages. */
class SnackbarDispatcher {
    private val _messages = MutableSharedFlow<SnackbarMessage>(extraBufferCapacity = 1)
    val messages: SharedFlow<SnackbarMessage> = _messages.asSharedFlow()

    fun show(message: SnackbarMessage) {
        _messages.tryEmit(message)
    }
}

/** Builds a [SnackbarHostState] that shows each message from [messages] as it arrives. */
@Composable
fun rememberMessageSnackbarHostState(messages: Flow<SnackbarMessage>): SnackbarHostState {
    val hostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(messages) {
        messages.collect { msg ->
            hostState.showSnackbar(context.getString(msg.resId, *msg.args))
        }
    }
    return hostState
}
