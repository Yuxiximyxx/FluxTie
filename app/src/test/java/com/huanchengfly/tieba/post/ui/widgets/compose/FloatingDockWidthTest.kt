package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 悬浮 Dock 壳宽公式（fork.3：按按钮数量自适应）：
 * 期望宽 = 72dp × 数量 + 16dp 内距；封顶 = min(屏宽 − 32dp, 480dp)。
 */
class FloatingDockWidthTest {

    @Test
    fun fourTabsOnPhone() {
        // 393dp 屏：72×4+16 = 304dp，未触任何封顶（旧 58% 规格仅 228dp）
        assertEquals(304.dp, dockShellWidth(393.dp, 4))
    }

    @Test
    fun threeTabsOnPhone() {
        // 隐藏「动态」时：72×3+16 = 232dp
        assertEquals(232.dp, dockShellWidth(393.dp, 3))
    }

    @Test
    fun narrowScreenCappedByEdgeAllowance() {
        // 320dp 屏：期望 304dp 超过屏宽封顶 320−32 = 288dp
        assertEquals(288.dp, dockShellWidth(320.dp, 4))
    }

    @Test
    fun manyTabsCappedByAbsoluteMax() {
        // 大屏 + 多按钮：期望 72×8+16 = 592dp，触绝对上限 480dp
        assertEquals(480.dp, dockShellWidth(1024.dp, 8))
    }

    @Test
    fun degenerateCountsDoNotBreak() {
        // 数量下限钳到 1，不出现负宽/零宽崩溃
        assertEquals(88.dp, dockShellWidth(393.dp, 0))
        assertEquals(88.dp, dockShellWidth(393.dp, 1))
    }
}
