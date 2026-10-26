package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import top.yukonga.miuix.kmp.basic.SmallTopAppBar

/**
 * 主页标签屏专用 Miuix 顶栏（一期 MIUIX 迁移）：替代共享 [Toolbar]（[Toolbar] 本体不动，内页继续用）。
 *
 * - 标题/导航/操作区语义与旧 Toolbar 一致，图标内容色统一为 [ExtendedTheme] 的 onTopBar；
 * - 底色与应用顶栏色（ExtendedTheme.colors.topBar）一致，Miuix 组件色板由主题层从现有配色映射；
 * - [content] 对应旧 Toolbar 的下方扩展槽（如消息页的 TabRow），渲染在顶栏之下、同一底色；
 * - [insets] = false 时关闭 Miuix 自带的状态栏内边距（动态页外层已有折叠容器自行处理状态栏）。
 */
@Composable
fun MiuixMainTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    insets: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val topBarColor = ExtendedTheme.colors.topBar
    val onTopBarColor = ExtendedTheme.colors.onTopBar
    Column(
        modifier = modifier.background(topBarColor),
    ) {
        SmallTopAppBar(
            title = title,
            color = topBarColor,
            titleColor = onTopBarColor,
            navigationIcon = {
                navigationIcon?.let { icon ->
                    // Miuix 的 navigationIcon 槽把内容顶齐左上角放置（起始边距 0、
                    // 不与标题行垂直居中），旧 Material TopAppBar 时代靠内容自带 Spacer
                    // 撑边距的账号头像在这里会贴边上飘（fork.2 真机截图实证）。
                    // 套一层占满槽高、居中靠左的容器把内容收进顶栏行内，起始边距统一 16dp；
                    // 内容自带的 Spacer 在 Box 内只是重叠的兄弟节点，不再生效，不会叠距。
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 16.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        ProvideContentColor(color = onTopBarColor) {
                            icon()
                        }
                    }
                }
            },
            actions = {
                ProvideContentColor(color = onTopBarColor) {
                    actions()
                }
            },
            defaultWindowInsetsPadding = insets,
        )
        content?.invoke(this)
    }
}
