package id.madsaz.app.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import id.madsaz.app.R

/**
 * Lima pola tangisan Dunstan Baby Language yang dikenali model. [code] adalah nama pola dan tidak diterjemahkan;
 * arti dan isi panduannya diambil dari resource agar ikut bahasa aplikasi (label dari BE hanya dalam satu bahasa).
 */
enum class CryCue(
    val code: String,
    @StringRes val label: Int,
    @StringRes val summary: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int,
    val signs: List<Int>,
    /** Saran penanganan dari Madsaz lama (satu baris per butir); dipakai di halaman Petunjuk dan di hasil analisis. */
    val solution: List<Int>,
) {
    NEH(
        "Neh", R.string.cue_neh_meaning, R.string.cue_neh_metric_detail, R.string.cue_neh_description, R.drawable.cue_neh,
        listOf(R.string.cue_neh_feature_1, R.string.cue_neh_feature_2, R.string.cue_neh_feature_3),
        listOf(R.string.cue_neh_sol_1, R.string.cue_neh_sol_2, R.string.cue_neh_sol_3, R.string.cue_neh_sol_4),
    ),
    OWH(
        "Owh", R.string.cue_owh_meaning, R.string.cue_owh_metric_detail, R.string.cue_owh_description, R.drawable.cue_owh,
        listOf(R.string.cue_owh_feature_1, R.string.cue_owh_feature_2, R.string.cue_owh_feature_3),
        listOf(R.string.cue_owh_sol_1, R.string.cue_owh_sol_2, R.string.cue_owh_sol_3, R.string.cue_owh_sol_4),
    ),
    EH(
        "Eh", R.string.cue_eh_meaning, R.string.cue_eh_metric_detail, R.string.cue_eh_description, R.drawable.cue_eh,
        listOf(R.string.cue_eh_feature_1, R.string.cue_eh_feature_2, R.string.cue_eh_feature_3),
        listOf(R.string.cue_eh_sol_1, R.string.cue_eh_sol_2, R.string.cue_eh_sol_3, R.string.cue_eh_sol_4, R.string.cue_eh_sol_5, R.string.cue_eh_sol_6),
    ),
    HEH(
        "Heh", R.string.cue_heh_meaning, R.string.cue_heh_metric_detail, R.string.cue_heh_description, R.drawable.cue_heh,
        listOf(R.string.cue_heh_feature_1, R.string.cue_heh_feature_2, R.string.cue_heh_feature_3),
        listOf(R.string.cue_heh_sol_1, R.string.cue_heh_sol_2, R.string.cue_heh_sol_3),
    ),
    EAIRH(
        "Eairh", R.string.cue_eairh_meaning, R.string.cue_eairh_metric_detail, R.string.cue_eairh_description, R.drawable.cue_eairh,
        listOf(R.string.cue_eairh_feature_1, R.string.cue_eairh_feature_2, R.string.cue_eairh_feature_3),
        listOf(R.string.cue_eairh_sol_1, R.string.cue_eairh_sol_2, R.string.cue_eairh_sol_3, R.string.cue_eairh_sol_4, R.string.cue_eairh_sol_5, R.string.cue_eairh_sol_6),
    );

    companion object {
        /** Kode dari BE ("Neh", atau "Neh - Lapar" pada `category`), tanpa membedakan huruf besar; null untuk pola di luar lima ini. */
        fun fromCode(code: String?): CryCue? {
            val name = code?.substringBefore(" - ")?.trim() ?: return null
            return entries.firstOrNull { it.code.equals(name, ignoreCase = true) }
        }
    }
}
