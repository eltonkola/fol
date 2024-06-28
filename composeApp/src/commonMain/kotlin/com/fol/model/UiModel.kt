package com.fol.com.fol.model

import fol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class AppsScreen(val title: StringResource) {
    Splash(title = Res.string.screen_splash),
    Landing(title = Res.string.screen_landing),
    CreateAccount(title = Res.string.screen_create_account),
    RecoverAccount(title = Res.string.screen_recover_account),
    Main(title = Res.string.screen_main),
    Thread(title = Res.string.screen_thread),
    AddContact(title = Res.string.screen_add_contact),
    Profile(title = Res.string.screen_profile),
    Settings(title = Res.string.screen_settings)
}
