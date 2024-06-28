import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.fol.FolApp
import fol.composeapp.generated.resources.Res
import fol.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
    ) {
        FolApp()
    }
}