package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 玻璃变体缩放（fork.4：苹果规范，透镜强度随面积减弱）：
 * 只缩放折射带高/位移量；小面 ×1.0 原样、菜单 ×0.6、大面 ×0.35 且带高封顶 14dp；
 * 其余分量（纱/模糊/饱和/色散/高光）一律不动，保证与预设/滑杆身份一致。
 */
class GlassLensScaleTest {

    private val liquid = GlassMaterial.Liquid.spec() // 带高 24 / 位移 24

    @Test
    fun smallSurfaceKeepsSpecIdentity() {
        assertEquals(liquid, liquid.withLensScale(1.0f))
    }

    @Test
    fun menuScaleSixtyPercent() {
        val scaled = liquid.withLensScale(0.6f)
        assertEquals(14.4f, scaled.refractionHeight.value, 0.001f)
        assertEquals(14.4f, scaled.refractionAmount.value, 0.001f)
    }

    @Test
    fun largeSurfaceBandCappedAt14() {
        // 48 × 0.35 = 16.8 超过 14dp 封顶，带高应被钳到 14；位移量不封顶
        val wide = liquid.copy(refractionHeight = 48.dp, refractionAmount = 48.dp)
        val scaled = wide.withLensScale(0.35f)
        assertEquals(14f, scaled.refractionHeight.value, 0.001f)
        assertEquals(16.8f, scaled.refractionAmount.value, 0.001f)
    }

    @Test
    fun otherComponentsUntouched() {
        val scaled = liquid.withLensScale(0.6f)
        assertEquals(liquid.tintAlphaDark, scaled.tintAlphaDark)
        assertEquals(liquid.tintAlphaLight, scaled.tintAlphaLight)
        assertEquals(liquid.blurRadius, scaled.blurRadius)
        assertEquals(liquid.saturation, scaled.saturation)
        assertEquals(liquid.dispersion, scaled.dispersion)
        assertEquals(liquid.highlightScale, scaled.highlightScale)
    }
}
