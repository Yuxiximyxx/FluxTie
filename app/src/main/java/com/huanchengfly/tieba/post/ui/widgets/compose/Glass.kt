package com.huanchengfly.tieba.post.ui.widgets.compose

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.post.rememberPreferenceAsState
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule

/**
 * 玻璃材质三档（设置键 `glass_material`，取值即 [key]）。
 *
 * 实现路线：Kyant0/backdrop 2.0.1 真库（背景采样 + 饱和/模糊 RenderEffect + AGSL 边缘折射透镜）。
 * 其 Android 变体经 JetBrains Compose 1.12 薄壳转发 androidx.compose.ui:ui:1.12.0，
 * 与本应用的 AndroidX Compose 共存无 duplicate class（已核 module 元数据）。
 *
 * 校准与酷安 miuix16 同表（小乐已真机认可「进步很大」的数值身份）：
 * 玻璃感来自「饱和 + 小模糊打底 + 强透镜折射 + 边缘高光」，纱极薄——
 * 深色 #121212 / 浅色 #FAFAFA 低透明叠色，绝不走厚乳纱路线（泛白的锅）。
 */
enum class GlassMaterial(val key: String) {
    /** 毛玻璃：最清透——小模糊、最高饱和、纱最薄、高光最亮、弱透镜。 */
    Clear("clear"),

    /** 磨砂玻璃（默认）：大模糊、纱较厚、饱和最低、透镜最弱，哑光。 */
    Frosted("frosted"),

    /** 液态玻璃：Kyant 原方——约 8dp 小模糊 + 强透镜折射（24/24）+ 色散。 */
    Liquid("liquid");

    companion object {
        fun fromKey(key: String?): GlassMaterial =
            entries.firstOrNull { it.key == key } ?: Frosted
    }
}

/** 单档材质的渲染参数（配方层，与渲染库解耦；自定义滑杆写的也是这组分量）。 */
data class GlassSpec(
    /** 叠纱不透明度：深/浅两套（基色见 [glassTintColor]）。 */
    val tintAlphaDark: Float,
    val tintAlphaLight: Float,
    val blurRadius: Dp,
    val saturation: Float,
    /** 折射带高 / 位移量，任一为 0 表示关闭透镜折射。 */
    val refractionHeight: Dp,
    val refractionAmount: Dp,
    /** 色散强度 0–0.5；Kyant 透镜的色散是开关量，>0.02 视为开启。 */
    val dispersion: Float,
    /** 高光强度倍率 0–2（1 = Kyant Highlight 默认亮度，映射时钳到 alpha 0–1）。 */
    val highlightScale: Float,
)

/** 三档预设数值表（与酷安 miuix16 同口径；身份：毛最清透 / 磨最哑 / 液折射最强）。 */
fun GlassMaterial.spec(): GlassSpec = when (this) {
    GlassMaterial.Clear -> GlassSpec(
        tintAlphaDark = 0.16f,
        tintAlphaLight = 0.18f,
        blurRadius = 6.dp,
        saturation = 1.8f,
        refractionHeight = 16.dp,
        refractionAmount = 10.dp,
        dispersion = 0f,
        highlightScale = 1.0f,
    )

    GlassMaterial.Frosted -> GlassSpec(
        tintAlphaDark = 0.55f,
        tintAlphaLight = 0.58f,
        blurRadius = 20.dp,
        saturation = 1.3f,
        refractionHeight = 20.dp,
        refractionAmount = 6.dp,
        dispersion = 0f,
        highlightScale = 0.35f,
    )

    GlassMaterial.Liquid -> GlassSpec(
        tintAlphaDark = 0.32f,
        tintAlphaLight = 0.32f,
        blurRadius = 8.dp,
        saturation = 1.5f,
        refractionHeight = 24.dp,
        refractionAmount = 24.dp,
        dispersion = 0.2f,
        highlightScale = 0.85f,
    )
}

/** 玻璃设置的 DataStore 键（与酷安 miuix17 同模型：预设写数值、滑杆进自定义、可恢复预设）。 */
object GlassPrefs {
    const val KEY_MATERIAL = "glass_material"
    const val KEY_CUSTOM_ACTIVE = "glass_custom_active"
    const val KEY_TINT_DARK = "glass_custom_tint_dark"
    const val KEY_TINT_LIGHT = "glass_custom_tint_light"
    const val KEY_BLUR = "glass_custom_blur"
    const val KEY_SATURATION = "glass_custom_saturation"
    const val KEY_REFRACTION_HEIGHT = "glass_custom_refraction_height"
    const val KEY_REFRACTION_AMOUNT = "glass_custom_refraction_amount"
    const val KEY_DISPERSION = "glass_custom_dispersion"
    const val KEY_HIGHLIGHT = "glass_custom_highlight"
}

/** 当前生效的玻璃设置：预设 + 是否处于自定义态 + 解析后的有效参数。 */
data class GlassSettings(
    val material: GlassMaterial,
    val customActive: Boolean,
    val spec: GlassSpec,
)

/**
 * 读取玻璃设置并解析有效参数（全应用唯一解析点，Dock/FAB/设置页共用，即时生效）：
 * 未激活自定义时逐值等于预设表；激活后用滑杆写入的自定义分量。
 * 自定义分量的缺省值取磨砂预设（设置页打开时会把当前有效值整表写回，见设置页实现）。
 */
@Composable
fun rememberGlassSettings(): GlassSettings {
    val materialKey by rememberPreferenceAsState(
        key = stringPreferencesKey(GlassPrefs.KEY_MATERIAL),
        defaultValue = GlassMaterial.Frosted.key,
    )
    val customActive by rememberPreferenceAsState(
        key = booleanPreferencesKey(GlassPrefs.KEY_CUSTOM_ACTIVE),
        defaultValue = false,
    )
    val fallback = GlassMaterial.Frosted.spec()
    val tintDark by rememberPreferenceAsState(
        key = floatPreferencesKey(GlassPrefs.KEY_TINT_DARK),
        defaultValue = fallback.tintAlphaDark,
    )
    val tintLight by rememberPreferenceAsState(
        key = floatPreferencesKey(GlassPrefs.KEY_TINT_LIGHT),
        defaultValue = fallback.tintAlphaLight,
    )
    val blur by rememberPreferenceAsState(
        key = floatPreferencesKey(GlassPrefs.KEY_BLUR),
        defaultValue = fallback.blurRadius.value,
    )
    val saturation by rememberPreferenceAsState(
        key = floatPreferencesKey(GlassPrefs.KEY_SATURATION),
        defaultValue = fallback.saturation,
    )
    val refractionHeight by rememberPreferenceAsState(
        key = floatPreferencesKey(GlassPrefs.KEY_REFRACTION_HEIGHT),
        defaultValue = fallback.refractionHeight.value,
    )
    val refractionAmount by rememberPreferenceAsState(
        key = floatPreferencesKey(GlassPrefs.KEY_REFRACTION_AMOUNT),
        defaultValue = fallback.refractionAmount.value,
    )
    val dispersion by rememberPreferenceAsState(
        key = floatPreferencesKey(GlassPrefs.KEY_DISPERSION),
        defaultValue = fallback.dispersion,
    )
    val highlight by rememberPreferenceAsState(
        key = floatPreferencesKey(GlassPrefs.KEY_HIGHLIGHT),
        defaultValue = fallback.highlightScale,
    )
    val material = GlassMaterial.fromKey(materialKey)
    val spec = if (customActive) {
        GlassSpec(
            tintAlphaDark = tintDark,
            tintAlphaLight = tintLight,
            blurRadius = blur.dp,
            saturation = saturation,
            refractionHeight = refractionHeight.dp,
            refractionAmount = refractionAmount.dp,
            dispersion = dispersion,
            highlightScale = highlight,
        )
    } else {
        material.spec()
    }
    return GlassSettings(material = material, customActive = customActive, spec = spec)
}

/** Kyant 定调的叠纱基色：深色模式用近黑、浅色用近白，靠 [GlassSpec] 的 alpha 控制厚薄。 */
fun glassTintColor(isNightMode: Boolean, spec: GlassSpec): Color =
    if (isNightMode) {
        Color(0xFF121212).copy(alpha = spec.tintAlphaDark)
    } else {
        Color(0xFFFAFAFA).copy(alpha = spec.tintAlphaLight)
    }

/**
 * 玻璃采样门槛：模糊走 RenderEffect 需 API 31+（透镜 AGSL 在库内按 33+ 自行分级）。
 * 低于门槛时调用方传 null backdrop，走半透纯色降级，观感与旧毛玻璃降级一致。
 */
fun supportsGlassSampling(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** 玻璃采样源（内容层录制句柄）：内容侧用 [glassBackdropSource] 挂载，玻璃面侧采样。 */
@Composable
fun rememberGlassBackdrop(): LayerBackdrop = rememberLayerBackdrop()

/** 把当前内容录制为玻璃采样源（挂在内容根容器上；玻璃面本身不要挂，避免自采样）。 */
fun Modifier.glassBackdropSource(backdrop: LayerBackdrop): Modifier =
    this.layerBackdrop(backdrop)

/**
 * 统一玻璃表面：有 [backdrop] 时用 Kyant 真采样（饱和→模糊→透镜折射→高光，逐级降级），
 * 无 backdrop 时退化为半透纯色 + [shape] 裁切。
 *
 * 阴影一律由调用方负责（库内 shadow 关），避免与调用方既有投影叠成双影。
 * [drawTint] = false 时不画叠纱（调用方自己在 drawBehind 里叠其它填充，如 Dock 指示器）。
 */
fun Modifier.glassSurface(
    backdrop: LayerBackdrop?,
    spec: GlassSpec,
    isNightMode: Boolean,
    shape: Shape,
    backdropShape: () -> Shape = { Capsule() },
    drawTint: Boolean = true,
): Modifier {
    val tint = glassTintColor(isNightMode, spec)
    if (backdrop == null) {
        // 降级：提高纱层不透明度替代采样（无内容透出时薄纱会看穿到刺眼的纯色块）
        val fallbackTint = tint.copy(alpha = (tint.alpha + 0.38f).coerceAtMost(0.92f))
        return this
            .clip(shape)
            .background(fallbackTint)
    }
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = backdropShape,
        effects = {
            colorControls(saturation = spec.saturation)
            blur(spec.blurRadius.toPx())
            if (spec.refractionHeight > 0.dp && spec.refractionAmount > 0.dp) {
                lens(
                    refractionHeight = spec.refractionHeight.toPx(),
                    refractionAmount = spec.refractionAmount.toPx(),
                    chromaticAberration = spec.dispersion > 0.02f,
                )
            }
        },
        highlight = {
            if (spec.highlightScale > 0f) {
                Highlight(alpha = spec.highlightScale.coerceIn(0f, 1f))
            } else {
                null
            }
        },
        shadow = { null },
        onDrawSurface = if (drawTint) {
            {
                // 叠纱：胶囊/圆的圆角 = 最小边一半，按形状内切绘制避免溢出方角
                drawRoundRect(
                    color = tint,
                    cornerRadius = CornerRadius(size.minDimension / 2f, size.minDimension / 2f),
                )
            }
        } else {
            null
        },
    )
}

/**
 * 玻璃悬浮按钮（吧页发帖 FAB 的玻璃版）：有采样源时为真玻璃圆钮，
 * 无采样源时回落到原 Material [FloatingActionButton]（颜色与旧实现一致）。
 */
@Composable
fun GlassFloatingActionButton(
    onClick: () -> Unit,
    backdrop: LayerBackdrop?,
    spec: GlassSpec,
    isNightMode: Boolean,
    fallbackBackgroundColor: Color,
    fallbackContentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (backdrop == null) {
        FloatingActionButton(
            onClick = onClick,
            backgroundColor = fallbackBackgroundColor,
            contentColor = fallbackContentColor,
            modifier = modifier,
            content = content,
        )
        return
    }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
            .glassSurface(
                backdrop = backdrop,
                spec = spec,
                isNightMode = isNightMode,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides fallbackContentColor) {
            content()
        }
    }
}
