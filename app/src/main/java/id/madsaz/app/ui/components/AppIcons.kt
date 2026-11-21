package id.madsaz.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.res.vectorResource
import com.composables.icons.lucide.R as LucideR

/**
 * Semua ikon UI memakai Lucide (com.composables:icons-lucide-android, lisensi ISC). Ilustrasi (hero, gambar pola tangis) tidak termasuk.
 * Ikon berarah dicerminkan saat bahasa RTL (Arab), sama seperti ikon AutoMirrored Material yang digantikannya.
 */
object AppIcons {
    val ArrowBack @Composable get() = mirrored(LucideR.drawable.lucide_ic_arrow_left)
    val ArrowForward @Composable get() = mirrored(LucideR.drawable.lucide_ic_arrow_right)
    val ChevronRight @Composable get() = mirrored(LucideR.drawable.lucide_ic_chevron_right)
    val LogOut @Composable get() = mirrored(LucideR.drawable.lucide_ic_log_out)

    val Baby @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_baby)
    val Bell @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_bell)
    val BookOpen @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_book_open)
    val Camera @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_camera)
    val Check @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_check)
    val Close @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_x)
    val Crown @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_crown)
    val Eye @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_eye)
    val EyeOff @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_eye_off)
    val Frown @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_frown)
    val Globe @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_globe)
    val HeartPulse @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_heart_pulse)
    val History @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_history)
    val House @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_house)
    val Lock @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_lock)
    val Mail @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_mail)
    val Meh @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_meh)
    val ShieldCheck @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_shield_check)
    val Smile @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_smile)
    val StopCircle @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_circle_stop)
    val SwitchCamera @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_switch_camera)
    val Trash @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_trash_2)
    val User @Composable get() = ImageVector.vectorResource(LucideR.drawable.lucide_ic_user)
}

/** Vector drawable Lucide tidak menandai autoMirrored; salinan ini ditandai agar Icon membaliknya pada tata letak RTL. */
@Composable
private fun mirrored(@DrawableRes id: Int): ImageVector {
    val source = ImageVector.vectorResource(id)
    return remember(source) {
        ImageVector.Builder(
            name = source.name,
            defaultWidth = source.defaultWidth,
            defaultHeight = source.defaultHeight,
            viewportWidth = source.viewportWidth,
            viewportHeight = source.viewportHeight,
            tintColor = source.tintColor,
            tintBlendMode = source.tintBlendMode,
            autoMirror = true,
        ).apply { copyChildren(source.root) }.build()
    }
}

private fun ImageVector.Builder.copyChildren(group: VectorGroup) {
    for (node in group) {
        when (node) {
            is VectorPath -> addPath(
                pathData = node.pathData,
                pathFillType = node.pathFillType,
                name = node.name,
                fill = node.fill,
                fillAlpha = node.fillAlpha,
                stroke = node.stroke,
                strokeAlpha = node.strokeAlpha,
                strokeLineWidth = node.strokeLineWidth,
                strokeLineCap = node.strokeLineCap,
                strokeLineJoin = node.strokeLineJoin,
                strokeLineMiter = node.strokeLineMiter,
                trimPathStart = node.trimPathStart,
                trimPathEnd = node.trimPathEnd,
                trimPathOffset = node.trimPathOffset,
            )
            is VectorGroup -> {
                addGroup(node.name, node.rotation, node.pivotX, node.pivotY, node.scaleX, node.scaleY, node.translationX, node.translationY, node.clipPathData)
                copyChildren(node)
                clearGroup()
            }
        }
    }
}
