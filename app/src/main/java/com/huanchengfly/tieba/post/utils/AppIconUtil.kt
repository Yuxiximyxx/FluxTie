package com.huanchengfly.tieba.post.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.huanchengfly.tieba.post.App
import kotlinx.collections.immutable.persistentListOf


object LauncherIcons {
    const val NEW_ICON = "com.huanchengfly.tieba.post.MainActivityV2"
    const val NEW_ICON_THEMED = "com.huanchengfly.tieba.post.MainActivityIconThemed"
    const val NEW_ICON_INVERT = "com.huanchengfly.tieba.post.MainActivityIconInvert"
    const val OLD_ICON = "com.huanchengfly.tieba.post.MainActivityIconOld"

    // 夜间别名：不在设置里单独列出，仅当图标选择为「默认」时由系统夜间状态自动启用
    const val NEW_ICON_NIGHT = "com.huanchengfly.tieba.post.MainActivityIconNight"

    const val DEFAULT_ICON = NEW_ICON

    // 设置里可选的图标
    val ICONS = persistentListOf(NEW_ICON, NEW_ICON_THEMED, NEW_ICON_INVERT, OLD_ICON)

    // 全部可切换的启动器组件（含夜间别名）：启用其一时其余必须全部禁用
    val ALL_COMPONENTS = persistentListOf(
        NEW_ICON,
        NEW_ICON_THEMED,
        NEW_ICON_INVERT,
        OLD_ICON,
        NEW_ICON_NIGHT
    )

    val SUPPORT_THEMED_ICON = persistentListOf(NEW_ICON)
    val THEMED_ICON_MAPPING = mapOf(
        NEW_ICON to NEW_ICON_THEMED,
    )

    const val OLD_LAUNCHER_ICON = "com.huanchengfly.tieba.post.activities.MainActivity"
}

object AppIconUtil {
    const val PREF_KEY_APP_ICON = "app_icon"

    private val context: Context
        get() = App.INSTANCE

    private val appPreferences: AppPreferencesUtils
        get() = context.appPreferences

    fun setIcon(
        icon: String = appPreferences.appIcon ?: LauncherIcons.NEW_ICON,
        isThemed: Boolean = appPreferences.useThemedIcon,
    ) {
        val useThemedIcon = isThemed && LauncherIcons.SUPPORT_THEMED_ICON.contains(icon)
        var newIcon = if (LauncherIcons.ICONS.contains(icon)) {
            icon
        } else LauncherIcons.DEFAULT_ICON
        if (useThemedIcon) {
            newIcon = LauncherIcons.THEMED_ICON_MAPPING[newIcon] ?: newIcon
        }
        // 默认图标跟随系统日夜间：仅当最终选择仍是默认图标时才自动切换，
        // 用户手动选了主题/反色/旧版图标时不干预
        if (newIcon == LauncherIcons.NEW_ICON && App.isSystemNight) {
            newIcon = LauncherIcons.NEW_ICON_NIGHT
        }
        LauncherIcons.ALL_COMPONENTS.forEach {
            if (it == newIcon) {
                context.packageManager.enableComponent(ComponentName(context, it))
            } else {
                context.packageManager.disableComponent(ComponentName(context, it))
            }
        }
        context.packageManager.disableComponent(
            ComponentName(
                context,
                LauncherIcons.OLD_LAUNCHER_ICON
            )
        )
    }

    /**
     * 启用组件（已是启用态则跳过，避免日夜间反复同步时桌面图标闪烁/消失）
     *
     * @param componentName 组件名
     */
    fun PackageManager.enableComponent(componentName: ComponentName) {
        if (getComponentEnabledSetting(componentName) ==
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        ) return
        setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * 禁用组件（已是禁用态则跳过，同上）
     *
     * @param componentName 组件名
     */
    fun PackageManager.disableComponent(componentName: ComponentName) {
        if (getComponentEnabledSetting(componentName) ==
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        ) return
        setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}