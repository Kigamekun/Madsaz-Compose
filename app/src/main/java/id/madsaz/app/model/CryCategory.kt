package id.madsaz.app.model

import androidx.annotation.StringRes
import id.madsaz.app.R

enum class CryCategory(
    val code: String,
    @StringRes val meaningRes: Int,
    @StringRes val actionRes: Int,
) {
    NEH("Neh", R.string.category_neh_meaning, R.string.category_neh_action),
    OWH("Owh", R.string.category_owh_meaning, R.string.category_owh_action),
    EH("Eh", R.string.category_eh_meaning, R.string.category_eh_action),
    EAIRH("Eairh", R.string.category_eairh_meaning, R.string.category_eairh_action),
    HEH("Heh", R.string.category_heh_meaning, R.string.category_heh_action),
}
