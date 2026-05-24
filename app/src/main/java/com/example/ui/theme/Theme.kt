package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = DarkRoastBackground,
    surface = DarkRoastSurface,
    onBackground = PrimaryDark,
    onSurface = PrimaryDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EspressoBrown,
    secondary = CappuccinoBeige,
    tertiary = MintGreen,
    error = GoldenHoney,
    background = LightBackground,
    surface = LightSurface,
    onBackground = OnLightSurface,
    onSurface = OnLightSurface
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to prioritize our handcrafted espresso-cafe theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
