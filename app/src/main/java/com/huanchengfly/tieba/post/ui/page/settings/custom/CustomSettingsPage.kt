package com.huanchengfly.tieba.post.ui.page.settings.custom

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Upcoming
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.post.App
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.AppFontSizeActivity
import com.huanchengfly.tieba.post.collectPreferenceAsState
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.rememberPreferenceAsMutableState
import com.huanchengfly.tieba.post.rememberPreferenceAsState
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.settings.components.AppearanceCard
import com.huanchengfly.tieba.post.ui.page.settings.components.CardDivider
import com.huanchengfly.tieba.post.ui.page.settings.components.SectionLabel
import com.huanchengfly.tieba.post.ui.page.settings.components.SettingRow
import com.huanchengfly.tieba.post.ui.page.settings.components.SwitchSettingRow
import com.huanchengfly.tieba.post.ui.page.settings.custom.components.ColorDotGrid
import com.huanchengfly.tieba.post.ui.page.settings.custom.components.HctColorPickerDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.GlassMaterial
import com.huanchengfly.tieba.post.ui.widgets.compose.GlassPrefs
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberGlassSettings
import com.huanchengfly.tieba.post.ui.widgets.compose.spec
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Slider
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.formatColorHex
import com.huanchengfly.tieba.post.utils.parseColorHexOrNull
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

private fun parseSeedColor(hex: String): Color =
    Color(parseColorHexOrNull(hex) ?: 0xFF2196F3.toInt())

private data class DarkStyleOption(
    val id: String,
    val name: String,
)

// 深色样式可选项（旧的静谧蓝已随前代主题一并移除）
private val DarkStyleOptions = listOf(
    DarkStyleOption(ThemeUtil.THEME_GREY_DARK, "grey"),
    DarkStyleOption(ThemeUtil.THEME_AMOLED_DARK, "amoled"),
)

@Destination
@Composable
fun CustomSettingsPage(
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val darkStyleNames = mapOf(
        ThemeUtil.THEME_GREY_DARK to stringResource(id = R.string.title_dark_style_grey),
        ThemeUtil.THEME_AMOLED_DARK to stringResource(id = R.string.title_dark_style_amoled),
    )
    val currentTheme by remember { ThemeUtil.themeState }
    var followSystemNight by rememberPreferenceAsMutableState(
        key = booleanPreferencesKey("follow_system_night"),
        defaultValue = true
    )
    var darkTheme by rememberPreferenceAsMutableState(
        key = stringPreferencesKey(ThemeUtil.KEY_DARK_THEME),
        defaultValue = ThemeUtil.THEME_AMOLED_DARK
    )
    var schemeVariant by rememberPreferenceAsMutableState(
        key = stringPreferencesKey("theme_scheme_variant"),
        defaultValue = "TonalSpot"
    )
    var useSeedTheme by rememberPreferenceAsMutableState(
        key = booleanPreferencesKey("use_seed_theme"),
        defaultValue = true
    )
    var seedColorPref by rememberPreferenceAsMutableState(
        key = stringPreferencesKey("custom_primary_color"),
        defaultValue = "#FF2196F3"
    )
    var customColorsRaw by rememberPreferenceAsMutableState(
        key = stringPreferencesKey(KEY_CUSTOM_SEED_COLORS),
        defaultValue = ""
    )
    val customColors = remember(customColorsRaw) { parseCustomSeedColors(customColorsRaw) }
    // 「跟随系统」= Monet（与种子色互斥：选色卡/自定义色会关掉 dynamic）
    val useDynamicColorTheme by context.dataStore.collectPreferenceAsState(
        key = booleanPreferencesKey("useDynamicColorTheme"),
        defaultValue = false
    )
    val dynamicSelected = useDynamicColorTheme && !useSeedTheme

    val currentIsNight = ThemeUtil.isNightMode()
    val oldTheme by rememberPreferenceAsState(
        key = stringPreferencesKey(ThemeUtil.KEY_OLD_THEME),
        defaultValue = ThemeUtil.THEME_CUSTOM
    )
    val lightPreviewTheme = if (currentIsNight) oldTheme else currentTheme
    val hapticFeedback = LocalHapticFeedback.current
    val schemeStyleLabel = stringResource(id = R.string.title_scheme_style)
    val variantTonalSpot = stringResource(id = R.string.scheme_tonal_spot)
    val variantNeutral = stringResource(id = R.string.scheme_neutral)
    val variantVibrant = stringResource(id = R.string.scheme_vibrant)
    val variantExpressive = stringResource(id = R.string.scheme_expressive)
    val variantFidelity = stringResource(id = R.string.scheme_fidelity)
    val variantContent = stringResource(id = R.string.scheme_content)
    val variantMonochrome = stringResource(id = R.string.scheme_monochrome)
    val variantRainbow = stringResource(id = R.string.scheme_rainbow)
    val variantFruitSalad = stringResource(id = R.string.scheme_fruit_salad)

    val schemeVariants = listOf(
        "TonalSpot", "Neutral", "Vibrant", "Expressive", "Fidelity",
        "Content", "Monochrome", "Rainbow", "FruitSalad",
    )
    val variantLabels = mapOf(
        "TonalSpot" to variantTonalSpot,
        "Neutral" to variantNeutral,
        "Vibrant" to variantVibrant,
        "Expressive" to variantExpressive,
        "Fidelity" to variantFidelity,
        "Content" to variantContent,
        "Monochrome" to variantMonochrome,
        "Rainbow" to variantRainbow,
        "FruitSalad" to variantFruitSalad,
    )

    // HCT 自定义取色器
    val hctPickerDialogState = rememberDialogState()
    val initialPickerColor = parseSeedColor(seedColorPref)

    /** 选定种子色的统一写入（预设/自定义共用） */
    fun applySeedColor(hex: String) {
        seedColorPref = hex
        useSeedTheme = true
        ThemeUtil.setUseDynamicTheme(false)
        ThemeUtil.switchTheme(ThemeUtil.THEME_CUSTOM)
    }

    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_settings_custom),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // ── 主题模式
            item {
                SectionLabel(text = stringResource(id = R.string.title_theme_mode), icon = Icons.Rounded.BrightnessAuto)
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    ModeCard(
                        label = stringResource(id = R.string.title_theme_mode_auto),
                        icon = Icons.Rounded.BrightnessAuto,
                        selected = followSystemNight,
                        onClick = { followSystemNight = true },
                        modifier = Modifier.weight(1f),
                        preview = {
                            Row(Modifier.fillMaxSize()) {
                                MiniScreenPreview(theme = lightPreviewTheme, modifier = Modifier.weight(1f))
                                MiniScreenPreview(theme = darkTheme, modifier = Modifier.weight(1f))
                            }
                        }
                    )
                    ModeCard(
                        label = stringResource(id = R.string.title_theme_mode_light),
                        icon = Icons.Rounded.LightMode,
                        selected = !followSystemNight && !currentIsNight,
                        onClick = {
                            followSystemNight = false
                            if (ThemeUtil.isNightMode()) ThemeUtil.switchNightMode()
                        },
                        modifier = Modifier.weight(1f),
                        preview = { MiniScreenPreview(theme = lightPreviewTheme, modifier = Modifier.fillMaxSize()) }
                    )
                    ModeCard(
                        label = stringResource(id = R.string.title_theme_mode_dark),
                        icon = Icons.Rounded.DarkMode,
                        selected = !followSystemNight && currentIsNight,
                        onClick = {
                            followSystemNight = false
                            if (!ThemeUtil.isNightMode()) ThemeUtil.switchNightMode()
                        },
                        modifier = Modifier.weight(1f),
                        preview = { MiniScreenPreview(theme = darkTheme, modifier = Modifier.fillMaxSize()) }
                    )
                }
            }

            // ── 深色样式
            item {
                SectionLabel(text = stringResource(id = R.string.title_dark_style), icon = Icons.Rounded.DarkMode)
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    DarkStyleOptions.forEach { option ->
                        DarkStyleCard(
                            name = darkStyleNames[option.id] ?: option.id,
                            theme = option.id,
                            selected = darkTheme == option.id,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                darkTheme = option.id
                                if (ThemeUtil.isNightMode()) {
                                    ThemeUtil.switchTheme(option.id, recordOldTheme = false)
                                }
                            }
                        )
                    }
                }
            }

            // ── 主题色彩（MD3 种子色点网格 + 跟随系统 + HCT 自定义取色器）
            item {
                SectionLabel(text = stringResource(id = R.string.title_theme_color), icon = Icons.Rounded.Palette)
            }
            item {
                ColorDotGrid(
                    currentSeedHex = seedColorPref,
                    dynamicSelected = dynamicSelected,
                    showFollowSystem = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    isNight = currentIsNight,
                    schemeVariant = schemeVariant,
                    customColors = customColors,
                    onSelectFollowSystem = {
                        useSeedTheme = false
                        ThemeUtil.setUseDynamicTheme(true)
                        ThemeUtil.switchTheme(ThemeUtil.THEME_CUSTOM)
                    },
                    onSelectColor = { hex -> applySeedColor(hex) },
                    onAddColor = { hctPickerDialogState.show() },
                    onDeleteColor = { hex ->
                        customColorsRaw = serializeCustomSeedColors(customColors - hex)
                    },
                )
            }

            // ── 配色风格（MD3 DynamicSchemeVariant，色卡即预览）
            item {
                SectionLabel(text = schemeStyleLabel)
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    schemeVariants.forEach { variantId ->
                        val style = runCatching { PaletteStyle.valueOf(variantId) }
                            .getOrDefault(PaletteStyle.TonalSpot)
                        val variantScheme = remember(seedColorPref, variantId, currentIsNight) {
                            dynamicColorScheme(
                                seedColor = parseSeedColor(seedColorPref),
                                isDark = currentIsNight,
                                style = style,
                            )
                        }
                        VariantCard(
                            label = variantLabels[variantId] ?: variantId,
                            primary = variantScheme.primary,
                            secondary = variantScheme.secondary,
                            tertiary = variantScheme.tertiary,
                            selected = schemeVariant == variantId,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                schemeVariant = variantId
                                if (useSeedTheme && currentTheme != ThemeUtil.THEME_CUSTOM) {
                                    ThemeUtil.switchTheme(ThemeUtil.THEME_CUSTOM)
                                }
                            }
                        )
                    }
                }
            }

            // ── 更多
            item {
                SectionLabel(text = stringResource(id = R.string.title_theme_more))
            }
            item {
                AppearanceCard {
                    SwitchSettingRow(
                        icon = Icons.Rounded.PhotoSizeSelectActual,
                        title = stringResource(id = R.string.title_settings_status_bar_darker),
                        summary = stringResource(id = R.string.summary_settings_status_bar_darker),
                        key = "status_bar_darker",
                        defaultValue = true,
                    )
                    CardDivider()
                    SwitchSettingRow(
                        icon = Icons.Rounded.FormatColorFill,
                        title = stringResource(id = R.string.tip_toolbar_primary_color),
                        summary = stringResource(id = R.string.tip_toolbar_primary_color_summary),
                        key = "toolbarPrimaryColor",
                        defaultValue = false,
                    )
                    CardDivider()
                    SwitchSettingRow(
                        icon = Icons.Rounded.ViewAgenda,
                        title = stringResource(id = R.string.settings_forum_single),
                        key = "listSingle",
                        defaultValue = false,
                    )
                    CardDivider()
                    SwitchSettingRow(
                        icon = Icons.Rounded.Explore,
                        title = stringResource(id = R.string.title_hide_explore),
                        key = "hideExplore",
                        defaultValue = false,
                    )
                    CardDivider()
                    SwitchSettingRow(
                        icon = Icons.Rounded.Dock,
                        title = stringResource(id = R.string.title_floating_bottom_nav),
                        summary = stringResource(id = R.string.summary_floating_bottom_nav),
                        key = "floatingBottomNav",
                        defaultValue = true,
                    )
                    CardDivider()
                    SwitchSettingRow(
                        icon = Icons.Rounded.Upcoming,
                        title = stringResource(id = R.string.title_lift_up_bottom_bar),
                        summary = stringResource(id = R.string.summary_lift_up_bottom_bar),
                        key = "liftUpBottomBar",
                        defaultValue = true,
                    )
                }
            }

            // ── 玻璃材质（悬浮底栏与悬浮按钮）
            item {
                SectionLabel(text = stringResource(id = R.string.title_glass_material))
            }
            item {
                GlassMaterialCard()
            }

            // ── 字体
            item {
                SectionLabel(text = stringResource(id = R.string.title_custom_font_size))
            }
            item {
                AppearanceCard {
                    SettingRow(
                        icon = Icons.Rounded.FontDownload,
                        title = stringResource(id = R.string.title_custom_font_size),
                        onClick = { context.goToActivity<AppFontSizeActivity>() }
                    )
                }
            }
        }
    }

    HctColorPickerDialog(
        dialogState = hctPickerDialogState,
        initialColor = initialPickerColor,
        isNight = currentIsNight,
        schemeVariant = schemeVariant,
        onConfirm = { color ->
            val hex = formatColorHex(color.toArgb())
            applySeedColor(hex)
            customColorsRaw = serializeCustomSeedColors(customColors + hex)
        }
    )
}

@Composable
private fun ThemeMiniColors(theme: String): Triple<Color, Color, Color> {
    val context = LocalContext.current
    val background = remember(theme) {
        Color(App.ThemeDelegate.getColorByAttr(context, R.attr.colorBackground, theme))
    }
    val primary = remember(theme) {
        Color(App.ThemeDelegate.getColorByAttr(context, R.attr.colorNewPrimary, theme))
    }
    val text = remember(theme) {
        Color(App.ThemeDelegate.getColorByAttr(context, R.attr.colorText, theme))
    }
    return Triple(background, primary, text)
}

@Composable
private fun MiniScreenPreview(
    theme: String,
    modifier: Modifier = Modifier,
) {
    val (background, primary, text) = ThemeMiniColors(theme)
    Column(
        modifier = modifier
            .background(background)
            .padding(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(primary)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp)
                .clip(CircleShape)
                .background(text.copy(alpha = 0.55f))
        )
        Spacer(modifier = Modifier.size(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(3.dp))
                .background(text.copy(alpha = 0.12f))
        )
        Spacer(modifier = Modifier.size(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(4.dp)
                .clip(CircleShape)
                .background(text.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun ModeCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    preview: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val borderColor by animateColorAsState(
        targetValue = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.divider,
        animationSpec = tween(durationMillis = 200),
        label = "modeCardBorder"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = if (selected) {
                    ExtendedTheme.colors.primary.copy(alpha = 0.10f)
                } else {
                    ExtendedTheme.colors.card
                }
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.35f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            preview()
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(id = R.string.desc_checked),
                    tint = ExtendedTheme.colors.background,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(ExtendedTheme.colors.primary)
                        .padding(3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.textSecondary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.text
            )
        }
    }
}


@Composable
private fun DarkStyleCard(
    name: String,
    theme: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val (background, primary, _) = ThemeMiniColors(theme)
    val borderColor by animateColorAsState(
        targetValue = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.divider,
        animationSpec = tween(durationMillis = 200),
        label = "darkStyleBorder"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(background)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.NightsStay,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(18.dp)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(id = R.string.desc_checked),
                    tint = background,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(primary)
                        .padding(2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.text
        )
    }
}

@Composable
private fun VariantCard(
    label: String,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val borderColor by animateColorAsState(
        targetValue = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.divider,
        animationSpec = tween(durationMillis = 200),
        label = "variantBorder"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color = ExtendedTheme.colors.card)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(primary)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(primary, secondary, tertiary).forEach { color ->
                Spacer(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.text,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
}

/**
 * 「玻璃材质」设置卡（与酷安 miuix17 同模型）：
 * 三预设（毛玻璃/磨砂/液态）+ 七滑杆（透明度/折射强度/折射范围/模糊/饱和度/色散/高光）。
 * 点预设 = 把预设数值整表写入滑杆并退出自定义态；拖任一滑杆 = 进入自定义态；
 * 「恢复预设值」= 把当前预设的数值重新写入。全部经 DataStore 即时生效于 Dock 与悬浮按钮。
 */
@Composable
private fun GlassMaterialCard() {
    val context = LocalContext.current
    val dataStore = context.dataStore
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val settings = rememberGlassSettings()
    val eff = settings.spec

    var materialKey by rememberPreferenceAsMutableState(
        key = stringPreferencesKey(GlassPrefs.KEY_MATERIAL),
        defaultValue = GlassMaterial.Frosted.key,
    )
    var customActive by rememberPreferenceAsMutableState(
        key = booleanPreferencesKey(GlassPrefs.KEY_CUSTOM_ACTIVE),
        defaultValue = false,
    )
    var tintDark by rememberPreferenceAsMutableState(
        key = floatPreferencesKey(GlassPrefs.KEY_TINT_DARK),
        defaultValue = eff.tintAlphaDark,
    )
    var tintLight by rememberPreferenceAsMutableState(
        key = floatPreferencesKey(GlassPrefs.KEY_TINT_LIGHT),
        defaultValue = eff.tintAlphaLight,
    )
    var blur by rememberPreferenceAsMutableState(
        key = floatPreferencesKey(GlassPrefs.KEY_BLUR),
        defaultValue = eff.blurRadius.value,
    )
    var saturation by rememberPreferenceAsMutableState(
        key = floatPreferencesKey(GlassPrefs.KEY_SATURATION),
        defaultValue = eff.saturation,
    )
    var refractionHeight by rememberPreferenceAsMutableState(
        key = floatPreferencesKey(GlassPrefs.KEY_REFRACTION_HEIGHT),
        defaultValue = eff.refractionHeight.value,
    )
    var refractionAmount by rememberPreferenceAsMutableState(
        key = floatPreferencesKey(GlassPrefs.KEY_REFRACTION_AMOUNT),
        defaultValue = eff.refractionAmount.value,
    )
    var dispersion by rememberPreferenceAsMutableState(
        key = floatPreferencesKey(GlassPrefs.KEY_DISPERSION),
        defaultValue = eff.dispersion,
    )
    var highlight by rememberPreferenceAsMutableState(
        key = floatPreferencesKey(GlassPrefs.KEY_HIGHLIGHT),
        defaultValue = eff.highlightScale,
    )

    fun selectPreset(material: GlassMaterial) {
        val s = material.spec()
        materialKey = material.key
        customActive = false
        scope.launch {
            dataStore.edit { prefs ->
                prefs[floatPreferencesKey(GlassPrefs.KEY_TINT_DARK)] = s.tintAlphaDark
                prefs[floatPreferencesKey(GlassPrefs.KEY_TINT_LIGHT)] = s.tintAlphaLight
                prefs[floatPreferencesKey(GlassPrefs.KEY_BLUR)] = s.blurRadius.value
                prefs[floatPreferencesKey(GlassPrefs.KEY_SATURATION)] = s.saturation
                prefs[floatPreferencesKey(GlassPrefs.KEY_REFRACTION_HEIGHT)] =
                    s.refractionHeight.value
                prefs[floatPreferencesKey(GlassPrefs.KEY_REFRACTION_AMOUNT)] =
                    s.refractionAmount.value
                prefs[floatPreferencesKey(GlassPrefs.KEY_DISPERSION)] = s.dispersion
                prefs[floatPreferencesKey(GlassPrefs.KEY_HIGHLIGHT)] = s.highlightScale
            }
        }
    }

    fun markCustom() {
        if (!customActive) customActive = true
    }

    AppearanceCard {
        // 预设三选一
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
        ) {
            GlassMaterial.entries.forEach { material ->
                val selected = !settings.customActive && settings.material == material
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) {
                                ExtendedTheme.colors.primary.copy(alpha = 0.16f)
                            } else {
                                ExtendedTheme.colors.chip
                            }
                        )
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectPreset(material)
                        }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(
                            id = when (material) {
                                GlassMaterial.Clear -> R.string.title_glass_material_clear
                                GlassMaterial.Frosted -> R.string.title_glass_material_frosted
                                GlassMaterial.Liquid -> R.string.title_glass_material_liquid
                            }
                        ),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) {
                            ExtendedTheme.colors.primary
                        } else {
                            ExtendedTheme.colors.text
                        },
                    )
                }
            }
        }
        if (settings.customActive) {
            Text(
                text = stringResource(id = R.string.summary_glass_material_custom),
                fontSize = 12.sp,
                color = ExtendedTheme.colors.textSecondary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)
            )
        }

        // 滑杆组（Miuix Slider）：值即自定义分量，拖动即时全局生效
        GlassSliderRow(
            title = stringResource(id = R.string.title_glass_tint),
            valueText = "%.2f".format(tintDark),
            value = tintDark,
            valueRange = 0f..0.8f,
            onValueChange = { value ->
                // 透明度以深色纱为准，浅色纱按当前深/浅比同比例缩放（miuix17 口径）
                val ratio = if (eff.tintAlphaDark > 0f) {
                    eff.tintAlphaLight / eff.tintAlphaDark
                } else 1f
                tintDark = value
                tintLight = (value * ratio).coerceIn(0f, 0.8f)
                markCustom()
            },
        )
        GlassSliderRow(
            title = stringResource(id = R.string.title_glass_refraction_amount),
            valueText = "${refractionAmount.toInt()} dp",
            value = refractionAmount,
            valueRange = 0f..48f,
            onValueChange = { refractionAmount = it; markCustom() },
        )
        GlassSliderRow(
            title = stringResource(id = R.string.title_glass_refraction_height),
            valueText = "${refractionHeight.toInt()} dp",
            value = refractionHeight,
            valueRange = 0f..40f,
            onValueChange = { refractionHeight = it; markCustom() },
        )
        GlassSliderRow(
            title = stringResource(id = R.string.title_glass_blur),
            valueText = "${blur.toInt()}",
            value = blur,
            valueRange = 0f..30f,
            onValueChange = { blur = it; markCustom() },
        )
        GlassSliderRow(
            title = stringResource(id = R.string.title_glass_saturation),
            valueText = "%.2f".format(saturation),
            value = saturation,
            valueRange = 1.0f..2.2f,
            onValueChange = { saturation = it; markCustom() },
        )
        GlassSliderRow(
            title = stringResource(id = R.string.title_glass_dispersion),
            valueText = "%.2f".format(dispersion),
            value = dispersion,
            valueRange = 0f..0.5f,
            onValueChange = { dispersion = it; markCustom() },
        )
        GlassSliderRow(
            title = stringResource(id = R.string.title_glass_highlight),
            valueText = "${(highlight * 100).toInt()}%",
            value = highlight,
            valueRange = 0f..2f,
            onValueChange = { highlight = it; markCustom() },
        )

        CardDivider()
        SettingRow(
            icon = Icons.Rounded.RestartAlt,
            title = stringResource(id = R.string.title_glass_restore_preset),
            summary = stringResource(
                id = when (settings.material) {
                    GlassMaterial.Clear -> R.string.title_glass_material_clear
                    GlassMaterial.Frosted -> R.string.title_glass_material_frosted
                    GlassMaterial.Liquid -> R.string.title_glass_material_liquid
                }
            ),
            onClick = { selectPreset(settings.material) },
        )
    }
}

@Composable
private fun GlassSliderRow(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.body2,
                color = ExtendedTheme.colors.textSecondary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
